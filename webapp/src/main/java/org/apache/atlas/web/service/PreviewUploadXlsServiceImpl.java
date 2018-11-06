package org.apache.atlas.web.service;

import com.gridsum.gdp.library.commons.exception.VerifyException;

import edu.npu.fastexcel.ExcelException;
import edu.npu.fastexcel.FastExcel;
import edu.npu.fastexcel.Sheet;
import edu.npu.fastexcel.Workbook;
import org.apache.atlas.web.common.filetable.CsvHeader;
import org.apache.atlas.web.common.filetable.FileType;
import org.apache.atlas.web.common.filetable.UploadConfig;
import org.apache.atlas.web.common.filetable.UploadFileCache;
import org.apache.atlas.web.common.filetable.UploadPreview;
import org.apache.atlas.web.util.ExcelUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewUploadXlsServiceImpl implements PreviewUploadService {
    @Override
    public org.apache.atlas.web.model.Workbook parser(String filePath) throws IOException, ExcelException {
        Workbook workbook = FastExcel.createReadableWorkbook(new File(filePath));
        org.apache.atlas.web.model.Workbook gWorkbook = new org.apache.atlas.web.model.Workbook();
        try {
            workbook.open();
            if (workbook != null) {
                String[] sheetNames = workbook.sheetNames();
                if (sheetNames.length == 0) {
                    throw new VerifyException("至少有一个sheet！");
                }
                List<String> shNames = new ArrayList<>();
                for (String sheetName : sheetNames) {
                    Sheet sheet = null;
                    try {
                        sheet = workbook.getSheet(sheetName);
                    } catch (ExcelException e) {
                        e.printStackTrace();
                    }
                    List<List<String>> tempLists = obtainTableBody(sheet);
                    if (!tempLists.isEmpty()) {
                        gWorkbook.addSheet(sheetName, tempLists);
                        shNames.add(sheetName);
                    }
                }
                gWorkbook.addSheetNames(shNames);
                gWorkbook.setFileType(FileType.XLS);
                LOGGER.info("{}:解析完毕", filePath);
                return gWorkbook;
            } else {
                throw new RuntimeException(new Exception(filePath + ":解析出错"));
            }
        } catch (ExcelException e) {
            throw new RuntimeException(e);
        } finally {
            workbook.close();
        }
    }

    @Override
    public UploadPreview previewUpload(String jobId, int size) throws Exception {
        try {
            return getUploadPreview(jobId, null, size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UploadPreview previewUpload(String jobId, UploadConfig uploadConfig, int size) throws Exception {
        try {
            return getUploadPreview(jobId, uploadConfig, size);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<List<String>> obtainTableBody(Sheet sheet) throws ExcelException {
        List<List<String>> tempLists = new ArrayList<>();
        for (int i = sheet.getFirstRow(); i < sheet.getLastRow(); i++) {
            List<String> tempList = new ArrayList<>();
            for (int j = sheet.getFirstColumn(); j < sheet.getLastColumn(); j++) {
                String cell = sheet.getCell(i, j);
                if (cell == null) {
                    tempList.add("");
                } else {
                    tempList.add(cell);
                }
            }
            if (!tempList.isEmpty()) {
                tempLists.add(tempList);
            }
        }
        return tempLists;
    }

    private UploadPreview getUploadPreview(String jobId, UploadConfig uploadConfig, int size) {
        try {
            UploadFileCache uploadFileCache = UploadFileCache.create();
            org.apache.atlas.web.model.Workbook workbook = uploadFileCache.get(jobId);
            String sheetName = null;
            if (uploadConfig != null) {
                sheetName = uploadConfig.getSheetName();
            }
            int index = 0;
            for (String name : workbook.getSheetNames()) {
                if (sheetName == null || sheetName.equals(name)) {
                    break;
                }
                index++;
            }
            List<List<String>> sheet = new ArrayList<>(workbook.getSheet(index));
            List<String> headInfo = sheet.get(0);
            boolean isIncludeHeader = false;
            if (uploadConfig != null) {//uploadConfig为null则说明是第一次预览
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
            LOGGER.info("{}:预览数据生成完毕", jobId);
            return preview;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
