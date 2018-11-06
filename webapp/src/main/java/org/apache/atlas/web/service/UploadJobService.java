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

package org.apache.atlas.web.service;

import com.gridsum.gdp.library.commons.exception.VerifyException;
import com.gridsum.gdp.library.commons.utils.FileUtils;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.model.metadata.Column;
import org.apache.atlas.web.common.filetable.ColumnExt;
import org.apache.atlas.web.common.filetable.CsvEncode;
import org.apache.atlas.web.common.filetable.CsvHeader;
import org.apache.atlas.web.common.filetable.CsvUtils;
import org.apache.atlas.web.common.filetable.ExcelReader;
import org.apache.atlas.web.common.filetable.FileType;
import org.apache.atlas.web.common.filetable.UploadConfig;
import org.apache.atlas.web.common.filetable.UploadFileInfo;
import org.apache.atlas.web.common.filetable.UploadPreview;
import org.apache.atlas.web.config.FiletableConfig;
import org.apache.atlas.web.model.UploadJobInfo;
import org.apache.atlas.web.util.ExcelUtils;
import org.apache.atlas.web.util.StringUtils;
import org.apache.avro.Schema;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;


@AtlasService
public class UploadJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadJobService.class);

    public String getPath(String jobId) {
        return FiletableConfig.getUploadPath() + jobId + ".upload";
    }

    /**
     * 将文件从临时文件写入本地（返回jobId和filePath）
     *
     * @param tempFile
     * @return
     */
    public UploadFileInfo uploadFile(File tempFile) {
        String jobId = UUID.randomUUID().toString().replace("-", "");
        String filePath = StringUtils.obtainFilePath(jobId);
        try {
            org.apache.commons.io.FileUtils.forceMkdir(new File(FiletableConfig.getUploadPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File uploadFile = new File(filePath);
        while (uploadFile.exists()) {//确保文件名不重名
            jobId = UUID.randomUUID().toString().replace("-", "");
            filePath = StringUtils.obtainFileType(jobId);
            uploadFile = new File(filePath);
        }
        try {
            org.apache.commons.io.FileUtils.copyFile(tempFile, uploadFile);
            org.apache.commons.io.FileUtils.forceDelete(tempFile);
        } catch (IOException e) {
            uploadFile.delete();
            throw new RuntimeException(e);
        }
        UploadFileInfo twoTuple = new UploadFileInfo();
        twoTuple.setFilePath(filePath);
        twoTuple.setJobId(jobId);

        return twoTuple;
    }

    public UploadPreview previewUpload(String jobId, int size) {
        String filePath = getPath(jobId);
        try {
            return previewExcelForXLSX(jobId, null, size);
        } catch (OLE2NotOfficeXmlFileException e) {

        } catch (NotOfficeXmlFileException e) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Workbook workbook = ExcelUtils.isExcelFile(filePath);
        if (workbook != null) {
            Iterator<Sheet> iterator = workbook.iterator();
            if (!iterator.hasNext()) {
                throw new VerifyException("至少有一个sheet！");
            }
            List<String> emptySheet = new ArrayList<>();
            UploadPreview preview = null;
            while (iterator.hasNext()) {
                Sheet sheet = iterator.next();
                try {
                    UploadPreview tempResult = previewExcel(workbook, null, size, sheet.getSheetName(), true);
                    if (preview == null) {
                        preview = tempResult;
                    }
                } catch (VerifyException e) {
                    LOGGER.warn("previewExcel sheet[{}]: empty", sheet.getSheetName(), e);
                    emptySheet.add(sheet.getSheetName());
                }
            }
            if (preview == null) {
                throw new VerifyException("至少有一行数据！");
            }
            if (!emptySheet.isEmpty()) {
                preview.getSheets().removeAll(emptySheet);
                StringBuffer stringBuffer = new StringBuffer("Excel文件中" + emptySheet.size() + "个sheet: ");
                for (String s : emptySheet) {
                    stringBuffer.append(s + ",");
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                stringBuffer.append(" 没有数据，忽略");
                preview.setPreviewInfo(stringBuffer.toString());
            }
            return preview;
        }
        try {
            String fileCode = FileUtils.fileCode(filePath);
            if (fileCode == null) {
                fileCode = "UTF8";
            }
            CsvEncode csvEncode = CsvEncode.of(fileCode);
            String delimiter = CsvUtils.detectDelimiter(filePath, csvEncode.name());
            final boolean includeHeader = true;
            final CsvHeader csvHeader = CsvUtils.detectCsvHeader(filePath, fileCode, delimiter, includeHeader);
            return previewUpload(jobId, fileCode, delimiter, includeHeader, csvHeader, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 预览Excel xls数据</p>
     *
     * @param workbook
     * @param headers
     * @param size
     * @param sheetName     if null，sheet = workbook.getSheetAt(0)
     * @param includeHeader
     * @return
     */
    private UploadPreview previewExcel(Workbook workbook, CsvHeader headers, int size, String sheetName, boolean includeHeader) {
        Sheet sheet;
        if (sheetName == null) {
            sheet = workbook.getSheetAt(0);
        } else {
            sheet = workbook.getSheet(sheetName);
        }
        if (headers == null) {
            headers = ExcelUtils.readExcelHerder(sheet, includeHeader);
        }
        List<List<String>> previewValues = ExcelUtils.readExcelDatas(sheet, size, headers.size(), includeHeader);
        List<String> tableHeads = ExcelUtils.readTableHeads(sheet, headers.size());//读取表头信息（修改时间：20170503 修改人：俞青云）
        UploadPreview preview = new UploadPreview();
        preview.setIncludeHeader(includeHeader);
        preview.setHeaders(headers.getColumnList());
        preview.setRows(previewValues);
        preview.setSize(ExcelUtils.getDatasSize(sheet, includeHeader));
        preview.setTableHeads(tableHeads);
        if (workbook instanceof HSSFWorkbook) {
            preview.setFileType(FileType.XLS);
        } else if (workbook instanceof XSSFWorkbook) {
            preview.setFileType(FileType.XLSX);
        }
        preview.setSheets(ExcelUtils.getAllSheetNames(workbook));
        return preview;
    }


    public UploadPreview previewUpload(String jobId, String fileCode, String delimiter, boolean includeHeader, CsvHeader csvHeader, int size) {
        String filePath = getPath(jobId);
        try {
            CsvEncode csvEncode = CsvEncode.of(fileCode);
            return CsvUtils.getHeadersWithPreview(filePath, csvEncode.name(), delimiter, includeHeader, csvHeader, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UploadPreview previewUpload(String jobId, UploadConfig uploadConfig, int size) {
        FileType fileType = uploadConfig.getFileType();
        if (fileType != null && FileType.XLS.equals(fileType)) {
            return previewExcel(jobId, uploadConfig, size);
        } else if (fileType != null && FileType.XLSX.equals(fileType)) {
            try {
                return previewExcelForXLSX(jobId, uploadConfig, size);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        CsvHeader csvHeader;
        if (uploadConfig.getColumns() != null && uploadConfig.getColumns().size() > 0) {
            csvHeader = new CsvHeader(uploadConfig.getColumns());
        } else {
            try {
                csvHeader = CsvUtils.detectCsvHeader(getPath(jobId), uploadConfig.getFileEncode(), uploadConfig.getFieldDelimiter(), uploadConfig.isIncludeHeaders());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return previewUpload(jobId, uploadConfig.getFileEncode(), uploadConfig.getFieldDelimiter(), uploadConfig.isIncludeHeaders(), csvHeader, size);
    }

    private UploadPreview previewExcel(String jobId, UploadConfig uploadConfig, int size) {
        String filePath = getPath(jobId);
        CsvHeader csvHeader = null;
        if (uploadConfig.getColumns() != null && uploadConfig.getColumns().size() > 0) {
            csvHeader = new CsvHeader(uploadConfig.getColumns());
        }
        Workbook workbook = ExcelUtils.isExcelFile(filePath);
        if (workbook != null) {
            return previewExcel(workbook, csvHeader, size, uploadConfig.getSheetName(), uploadConfig.isIncludeHeaders());
        } else {
            throw new VerifyException("不支持的文件类型！");
        }
    }

    /**
     * 获取xlsx 的数据用于大数据传输
     *
     * @param jobId
     * @param uploadConfig
     * @param size
     * @return
     * @throws Exception
     */
    private UploadPreview previewExcelForXLSX(String jobId, UploadConfig uploadConfig, final int size) throws Exception {
        String filePath = getPath(jobId);
        ExcelReader reader = new ExcelReader() {
            public void getRows(int sheetIndex, int curRow, List<String> rowList) {
                if (rowList != null && !rowList.isEmpty() && this.getPreviewRows().size() < size) {
                    List<String> tempList = new ArrayList<String>(rowList);
                    this.add(tempList);
                }
                if (rowList != null && !rowList.isEmpty()) {
                    this.totalNum++;
                }
            }
        };
        reader.processTableName(filePath);
        String sheetName = null;
        if (uploadConfig != null) {
            sheetName = uploadConfig.getSheetName();
        }
        int index = 0;
        for (String name : reader.getTableNames()) {
            index++;
            if (sheetName == null || sheetName.equals(name)) {
                break;
            }
        }
        reader.process(filePath, index);
        List<List<String>> previewValues = reader.getPreviewRows();
        UploadPreview preview = new UploadPreview();
        preview.setIncludeHeader(false);
        preview.setHeaders(preview.getHeaders());
        preview.setRows(previewValues);
        preview.setSize(reader.getAllSize(false));
        preview.setTableHeads(previewValues.get(0));
        preview.setFileType(FileType.XLSX);
        preview.setSheets(reader.getTableNames());
        return preview;
    }

    public UploadJobInfo createUploadJob(UploadJobInfo uploadJobInfo) {
        Preconditions.checkNotNull(uploadJobInfo, "uploadJobInfo should not be null");
        //        try (SqlSession session = this.sessionFactory.get()) {
        //            UploadJobDao uploadJobDao = session.getMapper(UploadJobDao.class);
        //            TaskDao taskDao = session.getMapper(TaskDao.class);
        //            uploadJobDao.insert(uploadJobInfo);
        //            LOGGER.info("uploadJobDao.insert [{}]", uploadJobInfo);
        //            taskDao.insert(uploadJobInfo.getTaskInfo());
        //            LOGGER.info("taskDao.insert [{}]", uploadJobInfo.getTaskInfo());
        //            session.commit();
        //            return uploadJobInfo;
        //        }
        return null;
    }

    public String getAvroSchemaJson(UploadConfig uploadConfig) {
        // 判断是否有重复的列名
        CsvHeader.valid(uploadConfig.getColumns().toArray(new ColumnExt[uploadConfig.getColumns().size()]));
        JSONObject schemaJson = new JSONObject();
        try {
            schemaJson.put("namespace", "com.gridsum.datahub." + uploadConfig.getDatabase());
            schemaJson.put("name", uploadConfig.getTableName());
            schemaJson.put("type", "record");
            JSONArray fields = new JSONArray();
            for (ColumnExt column : uploadConfig.getColumns()) {
                JSONObject field = new JSONObject();
                field.put("name", Ascii.toLowerCase(column.getName()));
                field.put("type", new String[]{column.getType().avroType, Schema.Type.NULL.getName()});
                fields.put(field);
            }
            schemaJson.put("fields", fields);
        } catch (JSONException e) {
            LOGGER.error("create avro schema failed", e);
        }
        return schemaJson.toString();
    }

}
