// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package org.apache.atlas.web.common.filetable.job;

import static org.apache.atlas.web.common.filetable.ActionType.INSERT;
import static org.apache.atlas.web.common.filetable.ActionType.OVERWRITE;

import com.gridsum.gdp.library.commons.data.type.DataType;
import com.gridsum.gdp.library.commons.exception.NotSupportedException;
import com.gridsum.gdp.library.commons.utils.FileUtils;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.common.filetable.ActionType;
import org.apache.atlas.web.common.filetable.ColumnExt;
import org.apache.atlas.web.common.filetable.Constants;
import org.apache.atlas.web.common.filetable.FileType;
import org.apache.atlas.web.common.filetable.UploadFileCache;
import org.apache.atlas.web.common.filetable.job.listener.AsyncTaskListener;
import org.apache.atlas.web.config.FiletableConfig;
import org.apache.atlas.web.model.filetable.TaskInfo;
import org.apache.atlas.web.model.filetable.UploadJobInfo;
import org.apache.atlas.web.service.filetable.TaskService;
import org.apache.atlas.web.util.BooleanValueUtils;
import org.apache.atlas.web.util.HdfsUtils;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parquet.avro.AvroSchemaConverter;
import parquet.avro.AvroWriteSupport;
import parquet.hadoop.ParquetWriter;
import parquet.hadoop.api.WriteSupport;
import parquet.hadoop.metadata.CompressionCodecName;
import parquet.schema.MessageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UploadJob extends AnalysisJob {

    static final Logger LOGGER = LoggerFactory.getLogger(UploadJob.class);

    static final Float[] TASK_PROGRESS = new Float[]{
            Float.valueOf(0.25F),
            Float.valueOf(0.5F),
            Float.valueOf(0.75F),
            Float.valueOf(1.0F)
    };

    private UploadJobInfo uploadJobInfo;
    private Schema avroSchema;
    private boolean result;
    /**
     * table-column -> file-column-index
     */
    private HashMap<String, Integer> columnMapping = new HashMap<>();
    /**
     * 为了解决大数据传输问题
     */
    private List<List<String>> sheet;


    public void init(TaskService taskService, UploadJobInfo jobInfo) {
        super.init(taskService, jobInfo.getJobId());

        Preconditions.checkNotNull(jobInfo, "jobInfo should not be null.");
        this.addListener(new AsyncTaskListener(this.taskService, jobInfo.getTaskInfo().getTaskId()), MoreExecutors.directExecutor());
        this.uploadJobInfo = jobInfo;
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();

        LOGGER.info("UploadJob startUp");

        for (ColumnExt columnExt : this.uploadJobInfo.getColumns()) {
            Integer sourceIndex = columnExt.getSourceIndex();
            // 如果目标表的column 没有对应的数据文件中sourceIndex， 则sourceIndex为null 或者 -1
            if (sourceIndex == null || sourceIndex.equals(-1)) {
                continue;
            }
            this.columnMapping.put(columnExt.getName(), sourceIndex);

            LOGGER.info("columnMapping.put[{}, {}]", columnExt.getName(), sourceIndex);
        }
        if (this.columnMapping.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "at least one column of data file should be selected");
        }
        LOGGER.info("columnMapping [{}]", this.columnMapping);

        this.avroSchema = new Schema.Parser().parse(this.uploadJobInfo.getFieldDescribe());

        LOGGER.info("uploadJobInfo.getFieldDescribe(): [{}]", this.uploadJobInfo.getFieldDescribe());
        LOGGER.info("avroSchema: [{}]", this.avroSchema);

        UploadFileCache uploadFileCache = UploadFileCache.create();
        if (FileType.CSV.equals(this.uploadJobInfo.getFileType()) || FileType.ZIP.equals(this.uploadJobInfo.getFileType())) {
            this.sheet = new ArrayList<List<String>>(uploadFileCache.get(this.uploadJobInfo.getJobId()).getSheet("csv"));
        } else {
            this.sheet = new ArrayList<List<String>>(uploadFileCache.get(this.uploadJobInfo.getJobId()).getSheet(this.uploadJobInfo.getSheetName()));
        }
        if (this.uploadJobInfo.isIncludeHeaderLine()) {
            this.sheet.remove(0);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
    }

    @Override
    public String description() {
        return " upload file to table[" + uploadJobInfo.getDatabase() + "." + uploadJobInfo.getTableName() + "]";
    }

    @Override
    public void tryClose() throws Exception {
        if (result) {
            FileUtils.deleteFile(uploadJobInfo.getFilePath());
        }
    }

    private Path createParquetFileProxy(String username) throws IOException, InterruptedException {
        //todo kerberos
        return createParquetFile();
    }

    private void removeParquetFileProxy(String username) throws IOException, InterruptedException, AtlasBaseException {
        //todo kerberos
        FileSystem fileSystem = HdfsUtils.fs();
        Path parquetFilePath = new Path(getParquetFileDir());
        fileSystem.delete(parquetFilePath, true);
    }

    private void moveParquetFileProxy(String username, final Path src, final Path dest) throws IOException, InterruptedException, AtlasBaseException {
        //todo kerberos
        FileSystem fs = HdfsUtils.fs();
        fs.rename(src, dest);
    }

    @Override
    protected void run() throws Exception {
        String username = "";
        try {
            // 使用HDFS用户模仿创建parquet文件
            LOGGER.info("execute file upload step 1");
            Path srcPath = createParquetFileProxy(username);
            updateTaskProgress(TASK_PROGRESS[0]);

            // 根据action完成初始化
            LOGGER.info("execute file upload step 2");
            ActionType actionType = uploadJobInfo.getActionType();
            String columns = generateCreateTableColumns();
            if (!(actionType == OVERWRITE || actionType == INSERT)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "unknow actionType: " + actionType);
            }
            if (actionType == OVERWRITE) {
                HiveJdbcUtils.execute(generateDropTableSQL());
            }
            HiveJdbcUtils.execute(generateCreateTableSQL(columns));
            updateTaskProgress(TASK_PROGRESS[1]);

            // 移动parquet文件到目标目录
            LOGGER.info("execute file upload step 3");
            Path destPath = new Path(Constants.HIVE_WAREHOUSE_BASE_DIR + uploadJobInfo.getDatabase() + ".db/" + uploadJobInfo.getTableName());
            moveParquetFileProxy(username, srcPath, destPath);
            updateTaskProgress(TASK_PROGRESS[2]);

            // 刷新表
            LOGGER.info("execute file upload step 4");
            updateTaskProgress(TASK_PROGRESS[3]);
            this.result = true;

            UploadFileCache uploadFileCache = UploadFileCache.create();
            uploadFileCache.get(uploadJobInfo.getJobId()).remove(uploadJobInfo.getSheetName());
            LOGGER.info(uploadFileCache.toString());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.error("execute file upload failed.");
            HiveJdbcUtils.execute(generateDropTableSQL());
            removeParquetFileProxy(username);
            throw new RuntimeException(e);
        }
    }

    /**
     * 更新任务进度
     *
     * @param progress
     */
    private void updateTaskProgress(Float progress) {
        TaskInfo taskInfo = uploadJobInfo.getTaskInfo();
        taskInfo.setProgress(progress);
        taskService.updateTaskProgress(taskInfo.getTaskId(), progress);
    }

    private String generateCreateTableColumns() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (ColumnExt column : uploadJobInfo.getColumns()) {
            builder.append('`').append(column.getName()).append("`")
                    .append(" ");
            builder.append(column.getType().avroType);
            builder.append(", ");
        }
        builder.setLength(builder.length() - ", ".length());
        builder.append(")");
        return builder.toString();
    }

    private String generateCreateTableSQL(String columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ")
                .append("`").append(uploadJobInfo.getDatabase()).append("`")
                .append(".")
                .append("`").append(uploadJobInfo.getTableName()).append("`")
                .append(columns)
                .append(" STORED AS PARQUET");
        return builder.toString();
    }

    private String generateDropTableSQL() {
        StringBuilder builder = new StringBuilder();
        builder.append("DROP TABLE IF EXISTS ")
                .append("`").append(uploadJobInfo.getDatabase()).append("`")
                .append(".")
                .append("`").append(uploadJobInfo.getTableName()).append("`");
        return builder.toString();
    }

    private String getParquetFileDir() {
        return FiletableConfig.getUploadHdfsPath() + "/" +
               getUploadJobInfo().getTaskInfo().getJobId();
    }

    /**
     * /tmp/{taskId}.parquet
     *
     * @return
     */
    private String getParquetFilePath() {
        return getParquetFileDir() + "/" + getUploadJobInfo().getTaskInfo().getTaskId() + ".parquet";
    }

    private Path createParquetFile() {
        MessageType parquetSchema = new AvroSchemaConverter().convert(avroSchema);
        Path outputPath = new Path(getParquetFilePath());
        LOGGER.info("create parquet file: [{}]", getParquetFilePath());
        WriteSupport<IndexedRecord> writeSupport = new AvroWriteSupport(parquetSchema, avroSchema);
        ParquetWriter<IndexedRecord> parquetWriter = null;
        try {
            parquetWriter = new ParquetWriter<>(outputPath, writeSupport, CompressionCodecName.SNAPPY,
                                                Constants.PARQUET_BLOCK_SIZE,
                                                Constants.PARQUET_PAGE_SIZE,
                                                Constants.PARQUET_DICTIONARY_PAGE_SIZE,
                                                Constants.PARQUET_ENABLE_DICTIONARY,
                                                Constants.PARQUET_VALIDATING,
                                                HdfsUtils.conf());

            if (null == sheet || sheet.isEmpty()) {
                throw new RuntimeException("current sheet is not exist or empty!");
            }
            int index = 0;
            for (List<String> row : sheet) {
                index++;
                GenericRecord genericRecord = buildGenericRecord(row, columnMapping, index);
                parquetWriter.write(genericRecord);
            }
        } catch (Exception e) {
            throw new RuntimeException("create parquet file failed.", e);
        } finally {
            if (parquetWriter != null) {
                try {
                    parquetWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("close parquet writer failed.", e);
                }
            }
        }

        return outputPath;
    }

    private GenericRecord buildGenericRecord(List<String> row, Map<String, Integer> columnMapping, int index) {
        GenericRecord record = new GenericData.Record(avroSchema);
        for (Schema.Field field : avroSchema.getFields()) {
            String fieldName = field.name();
            Integer cellNum = columnMapping.get(fieldName);
            if (columnMapping.containsKey(fieldName) && cellNum < row.size()) {
                try {
                    String cell = row.get(cellNum);
                    String value;
                    if (cell == null) {
                        value = "";
                    } else {
                        value = cell;
                    }
                    DataType dataType = DataType.parseOf(field.schema().getTypes().get(0).getName());
                    if (DataType.UNKNOWN.equals(dataType)) {
                        throw NotSupportedException.notSupport("DataType", dataType.name());
                    }

                    // Boolean类型数值预处理
                    String convertedValue = (dataType == DataType.BOOLEAN ? BooleanValueUtils.parseValue(value) : value);

                    record.put(fieldName, dataType.value(convertedValue));
                } catch (Exception e) {
                    String msg;
                    if (FileType.CSV.equals(uploadJobInfo.getFileType())) {
                        msg = String.format("csv文件第[%s]行 第[%s]列 处理失败, 详情： " + e.getMessage(), index, cellNum + 1);
                    } else {
                        msg = String.format("%s文件第[%s]行 第[%s]列 处理失败, 详情： " + e.getMessage(), uploadJobInfo.getSheetName(), index, cellNum + 1);
                    }

                    // 删除目标表
                    LOGGER.error(msg, e);
                    throw new RuntimeException(msg);
                }
            }
        }
        return record;
    }

    public UploadJobInfo getUploadJobInfo() {
        return uploadJobInfo;
    }
}
