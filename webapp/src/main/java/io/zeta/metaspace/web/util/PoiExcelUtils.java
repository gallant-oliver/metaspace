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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/1/10 13:37
 */
package io.zeta.metaspace.web.util;

import io.zeta.metaspace.model.dataquality.ExcelReport;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.web.model.TemplateEnum;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


/*
 * @description
 * @author sunhaoning
 * @date 2019/1/10 13:37
 */



/**
 * 生成Excel的工具类
 */
public class PoiExcelUtils {

    private static Logger logger = LoggerFactory.getLogger(PoiExcelUtils.class);

    // 扩展名
    public final static String XLS = "xls";
    public final static String XLSX = "xlsx";


    /**
     * * 读取excel文件
     *
     * @param excelFile excel文件
     * @param startRow  读取数据的起始行, 行号从0开始
     * @return
     * @throws IOException
     */
    public static List<String[]> readExcelFile(MultipartFile excelFile, int startRow) throws IOException {
        // 检查文件
        checkFile(excelFile);
        // 获得工作簿对象
        Workbook workbook = getWorkBook(excelFile);
        // 创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        List<String[]> list = new ArrayList<>();
        if (workbook != null) {
            for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                // 获取当前sheet工作表
                Sheet sheet = workbook.getSheetAt(sheetNum);
                if (sheet == null) {
                    continue;
                }
                // 获得当前sheet的结束行
                int lastRowNum = sheet.getLastRowNum();
                if (startRow < 0 || startRow > lastRowNum) {
                    throw new RuntimeException("wrong startRow");
                }
                // 循环除了第一行之外的所有行
                for (int rowNum = startRow; rowNum <= lastRowNum; rowNum++) {
                    // 获得当前行
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    // 获得当前行的开始列
                    int firstCellNum = row.getFirstCellNum();
                    // 获得当前行的列数
                    int lastCellNum = row.getPhysicalNumberOfCells();
                    String[] cells = new String[row.getPhysicalNumberOfCells()];
                    // 循环当前行
                    for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                        Cell cell = row.getCell(cellNum);
                        cells[cellNum] = getCellValue(cell);
                    }
                    list.add(cells);
                }
            }
        }
        return list;
    }


    /**
     * 生成excel文件
     *
     * @param data      数据
     * @param extension 文件扩展
     * @return
     */
    public static Workbook createExcelFile(List<String> attributes, List<List<String>> data, String extension) {
        // 1. 创建workbook
        Workbook workbook = null;
        if (StringUtils.isBlank(extension)) {
            return null;
        }

        if (extension.equalsIgnoreCase(XLS)) {
            // 2003版本
            workbook = new HSSFWorkbook();
        } else if (extension.equalsIgnoreCase(XLSX)) {
            // 2007版本
            workbook = new XSSFWorkbook();
        }
        if(workbook != null)
            createSheet(workbook, "sheet1", attributes, data);
        return workbook;
    }

    public static  void createSheet(Workbook workbook, String sheetName, List<String> attributes, List<List<String>> data) {
        Sheet sheet = workbook.getSheet(sheetName);
        if(sheet==null){
            sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));
        }
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(attributes.get(i).trim());
        }
        fillData(sheet, data, 1);
    }

    public static  void createSheet(Workbook workbook, String sheetName, List<String> attributes, List<List<String>> data,CellStyle cellStyle,int column) {
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));
        sheet.setDefaultColumnWidth(column);
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = row0.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(attributes.get(i).trim());
        }
        fillData(sheet, data, 1);
    }

    public static  void createSheet(Workbook workbook, String sheetName, List<String> attributes, Map<String,List<List<String>>> dataMap) {
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(attributes.get(i).trim());
        }
        mergeRegion(sheet, dataMap);
        if(Objects.nonNull(dataMap) && dataMap.size()>0) {
            int startIndex = 1;
            for(String key : dataMap.keySet()) {
                List<List<String>> dataList = dataMap.get(key);
                fillData(sheet, dataList, startIndex);
                startIndex += dataList.size();
            }
        }
    }

    public static void fillData(Sheet sheet, List<List<String>> data, int startIndex) {
        if (CollectionUtils.isNotEmpty(data)) {
            for (int i = 0; i < data.size(); i++) {
                List<String> rowInfo = data.get(i);
                Row row = sheet.createRow(startIndex++);
                // 添加数据
                for (int j = 0; j < rowInfo.size(); j++) {
                    row.createCell(j).setCellValue(rowInfo.get(j));
                }
            }
        }
    }



    public static Workbook createExcelFile(ExcelReport report, String extension) {
        String tableSheetName = report.getTableSheetName();
        String columnSheetName = report.getColumnSheetName();
        List<String> tableAttributes = report.getTableAttributes();
        List<List<String>> tableData = report.getTableData();
        List<String> columnAttributes = report.getColumnAttributes();
        List<List<String>> columnData = report.getColumnData();

        Workbook workbook = null;
        if (StringUtils.isBlank(extension)) {
            return null;
        }

        if (extension.equalsIgnoreCase(XLS)) {
            // 2003版本
            workbook = new HSSFWorkbook();
        } else if (extension.equalsIgnoreCase(XLSX)) {
            // 2007版本
            workbook = new XSSFWorkbook();
        }

        if (workbook != null) {
            createSheet(workbook, tableSheetName, tableAttributes, tableData);

            HashMap<String, List<List<String>>> dataMap = convertListToMap(columnData);
            createSheet(workbook, columnSheetName, columnAttributes, dataMap);
            //createSheet(workbook, columnSheetName, columnAttributes, columnData);
        }
        return workbook;
    }

    public static  void mergeRegion(Sheet sheet, Map<String,List<List<String>>> dataMap) {
        int startMergeIndex = 1;
        for(String key : dataMap.keySet()) {
            List<List<String>> dataList = dataMap.get(key);
            int size = dataList.size();
            int endMergeIndex = startMergeIndex + size - 1;
            if(startMergeIndex < endMergeIndex)
                sheet.addMergedRegion(new CellRangeAddress(startMergeIndex, endMergeIndex, 0, 0));
            startMergeIndex = endMergeIndex + 1;
        }
    }

    public static HashMap<String, List<List<String>>> convertListToMap(List<List<String>> lists) {
        HashMap<String, List<List<String>>> columnMap = new HashMap<>();
        for(List<String> list: lists) {
            String columnName = list.get(0);
            if(columnMap.containsKey(columnName)) {
                List<List<String>> columnList = columnMap.get(columnName);
                columnList.add(list);
            } else {
                List<List<String>> columnList = new ArrayList<>();
                columnList.add(list);
                columnMap.put(columnName, columnList);
            }
        }
        return columnMap;
    }


    /**
     * 获取当前列数据
     *
     * @param cell 列
     * @return 列值
     */
    public static String getCellValue(Cell cell) {
        String cellValue = "";

        if (cell == null) {
            return cellValue;
        }
        // 把数字当成String来读，避免出现1读成1.0的情况
        if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            cell.setCellType(CellType.STRING);
        }
        // 判断数据的类型
        switch (cell.getCellTypeEnum()) {
            case NUMERIC:
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case STRING:
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case BLANK:
                cellValue = "";
                break;
            case ERROR:
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }


    /**
     * 获得工作簿对象
     *
     * @param excelFile excel文件
     * @return 工作簿对象
     */
    public static Workbook getWorkBook(MultipartFile excelFile) {
        // 获得文件名
        String fileName = excelFile.getOriginalFilename();
        // 创建Workbook工作簿对象，表示整个excel
        Workbook workbook = null;
        try {
            // 获得excel文件的io流
            InputStream is = excelFile.getInputStream();
            // 根据文件后缀名不同(xls和xlsx)获得不同的workbook实现类对象
            if (fileName.endsWith(XLS)) {
                // 2003版本
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(XLSX)) {
                // 2007版本
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }


    /**
     * 检查文件
     *
     * @param excelFile excel文件
     * @throws IOException
     */
    public static void checkFile(MultipartFile excelFile) throws IOException {
        //判断文件是否存在
        if (null == excelFile) {
            throw new FileNotFoundException("文件不存在");
        }
        //获得文件名
        String fileName = excelFile.getOriginalFilename();
        //判断文件是否是excel文件
        if (!fileName.endsWith(XLS) && !fileName.endsWith(XLSX)) {
            throw new IOException(fileName + "不是excel文件");
        }
    }

    public static InputStream getTemplateInputStream(TemplateEnum templateEnum) throws AtlasBaseException, AtlasException, IOException {
        if (templateEnum == null) {
            throw new AtlasBaseException("无效模板");
        }

        Configuration configuration = ApplicationProperties.get();
        String tmpDir = configuration.getString("metaspace.tmp.filepath");

        if (tmpDir == null || tmpDir.isEmpty()) {
            tmpDir = System.getProperty("java.io.tmpdir");
        }

        Path template = Paths.get(tmpDir, templateEnum.getFileName());
        if (Files.notExists(template)) {
            if(Files.notExists(Paths.get(tmpDir))){
                Files.createDirectories(Paths.get(tmpDir));
            }
            Files.createFile(template);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            XSSFCellStyle textStyle=  genContextStyle(workbook);
            XSSFCellStyle titleStyle=genTitleStyle(workbook);
            XSSFDataFormat format=workbook.createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));
            titleStyle.setDataFormat(format.getFormat("@"));

            String[][] content = templateEnum.getContent();
            for(int i=0;i<content[0].length;i++){
                sheet.setDefaultColumnStyle(i,textStyle);
                sheet.setColumnWidth(i,4000);
            }

            for (int i = 0; i < content.length; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < content[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellStyle(titleStyle);
                    cell.setCellValue(content[i][j]);
                }
            }

            try (OutputStream outputStream = Files.newOutputStream(template)) {
                workbook.write(outputStream);
                outputStream.flush();
            }
        }
        return Files.newInputStream(template);
    }

    //创建文本样式
    public static XSSFCellStyle genContextStyle(XSSFWorkbook workbook){
        XSSFCellStyle style = workbook.createCellStyle();
        //文本水平居中显示
        style.setAlignment(HorizontalAlignment.CENTER);
        //文本竖直居中显示
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //文本自动换行
        style.setWrapText(true);
        return style;
    }

    //生成标题样式
    public static XSSFCellStyle genTitleStyle(XSSFWorkbook workbook){

        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        //标题居中，没有边框，所以这里没有设置边框，设置标题文字样式
        XSSFFont titleFont = workbook.createFont();
        //加粗
        titleFont.setBold(true);
        //文字尺寸
        titleFont.setFontHeight((short)10);
        titleFont.setFontHeightInPoints((short)10);
        style.setFont(titleFont);
        return style;
    }
}