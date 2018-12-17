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

package io.zeta.metaspace.web.service.filetable;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import com.google.common.util.concurrent.Service;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.web.common.filetable.ActionType;
import io.zeta.metaspace.web.common.filetable.UploadConfig;
import io.zeta.metaspace.web.common.filetable.job.UploadJob;
import io.zeta.metaspace.web.model.filetable.TaskInfo;
import io.zeta.metaspace.web.model.filetable.UploadJobInfo;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import io.zeta.metaspace.web.service.MetaDataService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 作业服务实现
 */
@AtlasService
public class JobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private MetaDataService.UploadJobService uploadJobService;

    @Autowired
    private TaskService taskService;

    public UploadJobInfo newUploadJobInfo(String jobId, UploadConfig uploadConfig) throws AtlasBaseException, SQLException, IOException {
        // 从提交的请求中得到任务配置，目标表schema，以及column的mapping关系
        UploadJobInfo uploadJobInfo = toUploadJobInfo(jobId, uploadConfig);
        uploadJobInfo.setJobId(jobId);
        uploadJobInfo.setUid("");
        LOGGER.info("CheckTable: " + "database: " + uploadJobInfo.getDatabase() + ", table: " + uploadJobInfo.getTableName() + ", action = " + uploadJobInfo.getActionType());

        checkTable(uploadJobInfo.getDatabase(), uploadJobInfo.getTableName(), uploadJobInfo.getActionType());

        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setJobId(jobId);
        // new task uuid
        taskInfo.setTaskId(UUIDUtils.alphaUUID());
        taskInfo.setState(Service.State.NEW.name());
        taskInfo.setProgress(0F);
        taskInfo.setUid("");

        uploadJobInfo.setTaskInfo(taskInfo);

        // 数据库中添加数据上传记录
        this.taskService.createUploadTask(uploadJobInfo.getTaskInfo());

        // 创建UploadJob
        UploadJob uploadJob = new UploadJob();
        uploadJob.init(taskService, uploadJobInfo);
        uploadJob.startAsync();
        return uploadJobInfo;
    }

    /**
     * 从提交的请求中得到任务配置，目标表schema，以及column的mapping关系
     *
     * @param jobId
     * @param uploadConfig
     * @return
     */
    private UploadJobInfo toUploadJobInfo(String jobId, UploadConfig uploadConfig) {
        UploadJobInfo uploadJobInfo = new UploadJobInfo();
        uploadJobInfo.setDatabase(uploadConfig.getDatabase());
        uploadJobInfo.setFieldDelimiter(uploadConfig.getFieldDelimiter());
        uploadJobInfo.setFileEncode(uploadConfig.getFileEncode());
        uploadJobInfo.setTableName(uploadConfig.getTableName());
        // 原始数据文件临时路径
        String filePath = this.uploadJobService.getPath(jobId);
        uploadJobInfo.setFilePath(filePath);
        uploadJobInfo.setFileEncode(uploadConfig.getFileEncode());
        uploadJobInfo.setIncludeHeaderLine(uploadConfig.isIncludeHeaders());
        uploadJobInfo.setFieldDelimiter(uploadConfig.getFieldDelimiter());
        uploadJobInfo.setFieldDescribe(this.uploadJobService.getAvroSchemaJson(uploadConfig));
        uploadJobInfo.setFileType(uploadConfig.getFileType());
        uploadJobInfo.setSheetName(uploadConfig.getSheetName());
        uploadJobInfo.setActionType(uploadConfig.getActionType());
        uploadJobInfo.setColumns(uploadConfig.getColumns());
        return uploadJobInfo;
    }

    /**
     * 校验此表是否已经存在
     *
     * @param databaseName
     * @param tableName
     */
    private void checkTable(String databaseName, String tableName) throws AtlasBaseException, SQLException, IOException {
        boolean existsTable = HiveJdbcUtils.tableExists(databaseName, tableName);
        if (existsTable) {
            throw new AtlasBaseException("表 " + databaseName + "." + tableName + " 已存在");
        }
    }

    /**
     * 校验此表是否已经存在</p>
     * INSERT，表存在，抛出异常</p>
     * APPEND|OVERWRITE，表不存在，抛出异常</p>
     *
     * @param databaseName
     * @param tableName
     */
    private void checkTable(String databaseName, String tableName, ActionType actionType) throws AtlasBaseException, SQLException, IOException {
        boolean existsTable = HiveJdbcUtils.tableExists(databaseName, tableName);
        if (existsTable && ActionType.INSERT.equals(actionType)) {
            throw new AtlasBaseException("表 " + databaseName + "." + tableName + " 已存在");
        }
        if (!existsTable && !ActionType.INSERT.equals(actionType)) {
            throw new AtlasBaseException("表 " + databaseName + "." + tableName + " 不存在");
        }
    }
}
