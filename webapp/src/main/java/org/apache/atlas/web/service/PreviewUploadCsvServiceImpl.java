package org.apache.atlas.web.service;

import com.gridsum.gdp.library.commons.utils.FileUtils;

import edu.npu.fastexcel.ExcelException;
import org.apache.atlas.web.common.filetable.CsvEncode;
import org.apache.atlas.web.common.filetable.CsvHeader;
import org.apache.atlas.web.common.filetable.CsvUtils;
import org.apache.atlas.web.common.filetable.FileType;
import org.apache.atlas.web.common.filetable.UploadConfig;
import org.apache.atlas.web.common.filetable.UploadFileCache;
import org.apache.atlas.web.common.filetable.UploadPreview;
import org.apache.atlas.web.model.filetable.Workbook;
import org.apache.atlas.web.util.ExcelUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviewUploadCsvServiceImpl implements PreviewUploadService{

    @Override
    public Workbook parser(String filePath) throws IOException {
        try{
            String fileCode= FileUtils.fileCode(filePath);
            if (fileCode == null) {
                fileCode = "UTF8";
            }
            CsvEncode csvEncode = CsvEncode.of(fileCode);
            String delimiter = CsvUtils.detectDelimiter(filePath, csvEncode.name());
            Workbook workbook=CsvUtils.obtainWorkbook(filePath,csvEncode.name(),delimiter);
            LOGGER.info("{}:解析完毕",filePath);
            return workbook;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public UploadPreview previewUpload(String jobId, int size) {
        try{
            return previewToUpload(jobId,null,size);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public UploadPreview previewUpload(String jobId, UploadConfig uploadConfig, int size) throws ExcelException {
        try{
            return previewToUpload(jobId,uploadConfig,size);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private UploadPreview previewUpload(String filePath, String fileCode, String delimiter, boolean includeHeader, CsvHeader csvHeader, int size) {
        try {
            CsvEncode csvEncode = CsvEncode.of(fileCode);
            return CsvUtils.getHeadersWithPreview(filePath, csvEncode.name(), delimiter, includeHeader, csvHeader, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UploadPreview previewToUpload(String jobId,UploadConfig uploadConfig,int size) {
        try{
            UploadFileCache uploadFileCache=UploadFileCache.create();
            Workbook workbook=uploadFileCache.get(jobId);
            List<List<String>> sheet=new ArrayList<>(workbook.getSheet("csv"));
            List<String> headInfo=sheet.get(0);
            boolean isIncludeHead=false;
            if(uploadConfig!=null){
                isIncludeHead=uploadConfig.isIncludeHeaders();
            }
            if(isIncludeHead){
                sheet.remove(0);
            }
            List<List<String>> previewSheet;
            //生成预览数据，如果超过size则只输出size条数据
            if(sheet.size()>size){
                previewSheet=sheet.subList(0,size);
            }else{
                previewSheet=sheet;
            }
            //生成表头信息
            CsvHeader csvHeader= ExcelUtils.readerExcelHeader(headInfo, isIncludeHead);
            UploadPreview preview=new UploadPreview();
            preview.setHeaders(csvHeader.getColumnExtList());
            preview.setFieldDelimiter(workbook.getFieldDelimiter());
            preview.setFileEncode(workbook.getFileEncode());
            preview.setIncludeHeader(false);
            preview.setTableHeads(headInfo);
            preview.setRows(previewSheet);
            preview.setSize(sheet.size());
            preview.setFileType(FileType.CSV);
            LOGGER.info("{}:预览数据生成完毕",jobId);
            return preview;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) throws Exception {
        String filePath="D:\\openbi\\测试用例\\ip.csv";
        PreviewUploadCsvServiceImpl previewUploadCvsServiceImpl=new PreviewUploadCsvServiceImpl();
        Workbook workbook=previewUploadCvsServiceImpl.parser(filePath);
        System.out.println(workbook.getSheet("csv"));
    }
}
