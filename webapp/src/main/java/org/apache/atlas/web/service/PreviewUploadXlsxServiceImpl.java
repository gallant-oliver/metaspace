package org.apache.atlas.web.service;

import edu.npu.fastexcel.ExcelException;
import org.apache.atlas.web.common.filetable.CsvHeader;
import org.apache.atlas.web.common.filetable.ExcelReader;
import org.apache.atlas.web.common.filetable.FileType;
import org.apache.atlas.web.common.filetable.UploadConfig;
import org.apache.atlas.web.common.filetable.UploadFileCache;
import org.apache.atlas.web.common.filetable.UploadPreview;
import org.apache.atlas.web.model.Workbook;
import org.apache.atlas.web.util.CollectionUtil;
import org.apache.atlas.web.util.ExcelUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class PreviewUploadXlsxServiceImpl implements PreviewUploadService {

    @Override
    public Workbook parser(String filePath) throws Exception {
        return obtainWorkbook(filePath);
    }

    @Override
    public UploadPreview previewUpload(String jobId, int size) {
        try {
            return previewExcelForXLSX(jobId, null, size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UploadPreview previewUpload(String filePath, UploadConfig uploadConfig, int size) throws ExcelException {
        try {
            return previewExcelForXLSX(filePath, uploadConfig, size);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    private UploadPreview previewExcelForXLSX(String jobId, UploadConfig uploadConfig, int size) throws Exception {
        LOGGER.info("PreviewExcelForXLSX: jobId = [" + jobId + "], uploadConfig = " + ToStringBuilder.reflectionToString(uploadConfig));

        String sheetName = null;
        try {
            UploadFileCache uploadFileCache = UploadFileCache.create();
            Workbook workbook = uploadFileCache.get(jobId);
            if (uploadConfig != null) {
                sheetName = uploadConfig.getSheetName();
            }
            LOGGER.info("jobId=" + jobId + ", sheetName=" + sheetName + ", sheetNames=" + CollectionUtil.formatCollection(workbook.getSheetNames()));

            int index = 0;
            for (String name : workbook.getSheetNames()) {
                if (null == sheetName || sheetName.equals(name)) {
                    break;
                }
                index++;
            }

            LOGGER.info("jobId=" + jobId + ", sheetName=" + sheetName + ", index=" + index);
            List<List<String>> sheet = new ArrayList<>(workbook.getSheet(index));
            List<String> headInfo = sheet.get(0);
            boolean isIncludeHeader = false;
            if (uploadConfig != null) {
                isIncludeHeader = uploadConfig.isIncludeHeaders();
            }
            if (isIncludeHeader) {
                sheet.remove(0);
            }
            List<List<String>> previewSheet = null;
            if (sheet.size() > size) {//生成预览数据，如果超过size则只输出size条数据
                previewSheet = sheet.subList(0, size);
            } else {
                previewSheet = sheet;
            }

            CsvHeader csvHeader = ExcelUtils.readerExcelHeader(headInfo, isIncludeHeader);//生成表头信息
            UploadPreview preview = new UploadPreview();
            preview.setIncludeHeader(isIncludeHeader);
            preview.setHeaders(csvHeader.getColumnList());
            preview.setTableHeads(headInfo);
            preview.setRows(previewSheet);
            preview.setSize(sheet.size());
            preview.setFileType(FileType.XLSX);
            preview.setSheets(workbook.getSheetNames());
            LOGGER.info("jobId=" + jobId + ", sheetName=" + sheetName + ", 预览数据生成完毕");
            return preview;
        } catch (Exception e) {
            LOGGER.error("PreviewExcelForXLSX failure, jobId=" + jobId + ", sheetName=" + sheetName + ", message=" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取xlsx 的数据用于大数据传输
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    private Workbook obtainWorkbook(String filePath) throws Exception {
        try {
            ExcelReader reader = new ExcelReader() {
                public void getRows(int sheetIndex, int curRow, List<String> rowList) {
                    if (rowList != null && !rowList.isEmpty()) {
                        List<String> tempList = new ArrayList<>(rowList);
                        this.add(tempList);
                    }
                }
            };
            reader.process(filePath);
            List<List<List<String>>> table = reader.getTableList();
            List<String> sheetNames = reader.getTableNames();
            List<String> validSheetNames = new ArrayList<>(sheetNames);
            Workbook workbook = new Workbook();
            for (int i = 0; i < sheetNames.size(); i++) {
                String sheetName = sheetNames.get(i);
                List<List<String>> sheet = table.get(i);
                if (sheet.isEmpty()) {
                    validSheetNames.remove(i);
                    continue;
                }
                workbook.addSheet(sheetName, sheet);
            }
            workbook.setFileType(FileType.XLSX);
            workbook.addSheetNames(validSheetNames);
            LOGGER.info("{}:解析完毕", filePath);
            return workbook;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
