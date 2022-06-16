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
import io.zeta.metaspace.web.model.TemplateEnum;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.exception.ReadExcelException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;


/*
 * @description
 * @author sunhaoning
 * @date 2019/1/10 13:37
 */


/**
 * 生成Excel的工具类
 */
public class PoiExcelUtils {
    private PoiExcelUtils() {
    }

    private static Logger logger = LoggerFactory.getLogger(PoiExcelUtils.class);

    // 扩展名
    public static final String XLS = "xls";
    public static final String XLSX = "xlsx";

    //参数，表示读取全部行或列
    public static final int ALL = -1;

    private static HashMap<String, List<List<String>>> dataMap = new HashMap<>();


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
        return readFileToList(workbook, startRow, ALL);
    }

    /**
     * 读取excel文件
     *
     * @param inputStream
     * @param fileName
     * @param startRow
     * @param lastCellNum
     * @return
     * @throws IOException
     */
    public static List<String[]> readExcelFile(InputStream inputStream, String fileName, int startRow, int lastCellNum) {
        // 获得工作簿对象
        Workbook workbook = getWorkBookFile(inputStream, fileName);
        // 创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        return readFileToList(workbook, startRow, lastCellNum);
    }


    /**
     * 读取excel文件
     *
     * @param file
     * @param startRow    开始行数
     * @param lastCellNum 列数
     * @return
     * @throws IOException
     */
    public static List<String[]> readExcelFile(File file, int startRow, int lastCellNum) {
        // 获得工作簿对象
        Workbook workbook = getWorkBookFile(file);
        // 创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        return readFileToList(workbook, startRow, lastCellNum);
    }

    public static List<String[]> readExcelFile(Workbook workbook) {
        // 创建返回对象，把每行中的值作为一个数组，所有的行作为一个集合返回
        List<String[]> list = new ArrayList<>();
        if (workbook == null) {
            return list;
        }

        // 获取当前sheet工作表
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return list;
        }
        int startRow = 0;
        // 获得当前sheet的结束行
        int lastRowNum = sheet.getLastRowNum();
        // 循环除了第一行之外的所有行
        for (int rowNum = startRow; rowNum <= lastRowNum; rowNum++) {
            // 获得当前行
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                return list;
            }
            // 获得当前行的开始列
            int firstCellNum = row.getFirstCellNum();
            int lastCellNum = row.getLastCellNum();
            // 获得当前行的列数
            String[] cells = new String[lastCellNum];
            boolean isBlank = false;
            // 循环当前行
            for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
                Cell cell = row.getCell(cellNum);
                cells[cellNum] = getCellValue(cell);
                if (!isBlank && StringUtils.isNotBlank(cells[cellNum])){
                    isBlank = true;
                }
            }
            if (isBlank){
                list.add(cells);
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
        if (workbook != null)
            createSheet(workbook, "sheet1", attributes, data);
        return workbook;
    }

    public static void createSheet(Workbook workbook, String sheetName, List<String> attributes, List<List<String>> data) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));
        }
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(attributes.get(i).trim());
        }
        fillData(sheet, data, 1);
    }

    public static void createSheet(Workbook workbook, String sheetName, List<String> attributes, List<List<String>> data, CellStyle cellStyle, int column) {
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

    public static void createSheet(Workbook workbook, String sheetName, List<String> attributes, Map<String, List<List<String>>> dataMap) {
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < attributes.size(); i++) {
            Cell cell = row0.createCell(i);
            cell.setCellValue(attributes.get(i).trim());
        }
        mergeRegion(sheet, dataMap);
        if (dataMap.size() > 0) {
            int startIndex = 1;
            for (Map.Entry<String, List<List<String>>> key : dataMap.entrySet()) {
                List<List<String>> dataList = key.getValue();
                fillData(sheet, dataList, startIndex);
                startIndex += dataList.size();
            }
        }
    }

    private static void fillData(Sheet sheet, List<List<String>> data, int startIndex) {
        if (CollectionUtils.isNotEmpty(data)) {
            for (List<String> rowInfo : data) {
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
            convertListToMap(columnData);
            createSheet(workbook, columnSheetName, columnAttributes, dataMap);
        }
        return workbook;
    }

    private static void mergeRegion(Sheet sheet, Map<String, List<List<String>>> dataMap) {
        int startMergeIndex = 1;
        for (Map.Entry<String, List<List<String>>> entry : dataMap.entrySet()) {
            List<List<String>> dataList = entry.getValue();
            int size = dataList.size();
            int endMergeIndex = startMergeIndex + size - 1;
            if (startMergeIndex < endMergeIndex)
                sheet.addMergedRegion(new CellRangeAddress(startMergeIndex, endMergeIndex, 0, 0));
            startMergeIndex = endMergeIndex + 1;
        }
    }

    private  static synchronized void convertListToMap(List<List<String>> lists) {
        dataMap.clear();
        for (List<String> list : lists) {
            String columnName = list.get(0);
            if (dataMap.containsKey(columnName)) {
                List<List<String>> columnList = dataMap.get(columnName);
                columnList.add(list);
            } else {
                List<List<String>> columnList = new ArrayList<>();
                columnList.add(list);
                dataMap.put(columnName, columnList);
            }
        }
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
        return StringUtils.trim(cellValue);
    }


    /**
     * 获得工作簿对象
     *
     * @param excelFile excel文件
     * @return 工作簿对象
     */
    private static Workbook getWorkBook(MultipartFile excelFile) {
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
            logger.error("read from " + fileName + " failed", e);
        }
        return workbook;
    }

    /**
     * 获得工作簿对象
     *
     * @param file excel文件
     * @return 工作簿对象
     */
    private static Workbook getWorkBookFile(File file) {
        // 获得文件名
        String fileName = file.getName();
        // 创建Workbook工作簿对象，表示整个excel
        Workbook workbook = null;
        try (InputStream is = new FileInputStream(file)) {
            // 根据文件后缀名不同(xls和xlsx)获得不同的workbook实现类对象
            if (fileName.endsWith(XLS)) {
                // 2003版本
                workbook = new HSSFWorkbook(is);
            } else if (fileName.endsWith(XLSX)) {
                // 2007版本
                workbook = new XSSFWorkbook(is);
            } else {
                // 2007版本
                workbook = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            logger.error("getWorkBookFile exception is ", e);
        }
        return workbook;
    }


    /**
     * 获得工作簿对象
     *
     * @param input
     * @param fileName
     * @return
     */
    private static Workbook getWorkBookFile(InputStream input, String fileName) {
        // 创建Workbook工作簿对象，表示整个excel
        Workbook workbook = null;
        try {
            // 根据文件后缀名不同(xls和xlsx)获得不同的workbook实现类对象
            if (fileName.endsWith(XLS)) {
                // 2003版本
                workbook = new HSSFWorkbook(input);
            } else if (fileName.endsWith(XLSX)) {
                // 2007版本
                workbook = new XSSFWorkbook(input);
            } else {
                // 2007版本
                workbook = new XSSFWorkbook(input);
            }
        } catch (IOException e) {
            logger.error("getWorkBookFile exception is ", e);
        }
        return workbook;
    }


    /**
     * 检查文件
     *
     * @param excelFile excel文件
     * @throws IOException
     */
    private static void checkFile(MultipartFile excelFile) throws IOException {
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
            if (Files.notExists(Paths.get(tmpDir))) {
                Files.createDirectories(Paths.get(tmpDir));
            }
            Files.createFile(template);

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet();
            XSSFCellStyle textStyle = genContextStyle(workbook);
            XSSFCellStyle titleStyle = genTitleStyle(workbook);
            XSSFDataFormat format = workbook.createDataFormat();
            textStyle.setDataFormat(format.getFormat("@"));
            titleStyle.setDataFormat(format.getFormat("@"));

            String[][] content = templateEnum.getContent();
            for (int i = 0; i < content[0].length; i++) {
                sheet.setDefaultColumnStyle(i, textStyle);
                sheet.setColumnWidth(i, 4000);
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
    private static XSSFCellStyle genContextStyle(XSSFWorkbook workbook) {
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
    private static XSSFCellStyle genTitleStyle(XSSFWorkbook workbook) {

        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);

        //标题居中，没有边框，所以这里没有设置边框，设置标题文字样式
        XSSFFont titleFont = workbook.createFont();
        //加粗
        titleFont.setBold(true);
        //文字尺寸
        titleFont.setFontHeight((short) 10);
        titleFont.setFontHeightInPoints((short) 10);
        style.setFont(titleFont);
        return style;
    }

    /**
     * 创建带有下拉列表的 excel
     *
     * @param attributes
     * @param data       值包含分号; 的则使用下拉框处理
     * @return
     */
    public static Workbook createExcelFileWithDropDown(List<String> attributes, List<Object> data, String sheetName) {
        // 1. 创建workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));
        //标题头
        IndexPo header = createHeader(sheet, attributes, workbook);
        //设置内容
        createValues(sheet, data, workbook, header);

        return workbook;
    }

    /**
     * 解决下拉框过长不显示问题
     *
     * @param workbook
     * @param deptList   下拉数据数组
     * @param sheet
     * @param firstRow   开始行
     * @param endRow     结束行
     * @param cellNum    下拉框所在的列
     * @param sheetIndex 隐藏sheet名称
     */
    private static void setLongXSSFValidation(XSSFWorkbook workbook, String[] deptList, XSSFSheet sheet,
                                              int firstRow, int endRow, int cellNum, int sheetIndex) {
        String hiddenName = "hidden" + cellNum;
        //1.创建隐藏的sheet页。        起个名字吧！叫"hidden"！
        XSSFSheet hidden = workbook.createSheet(hiddenName);
        //2.循环赋值（为了防止下拉框的行数与隐藏域的行数相对应，将隐藏域加到结束行之后）
        for (int i = 0, length = deptList.length; i < length; i++) {
            hidden.createRow(endRow + i).createCell(cellNum).setCellValue(deptList[i]);
        }
        Name category1Name = workbook.createName();
        category1Name.setNameName(hiddenName);
        //3 A1:A代表隐藏域创建第N列createCell(N)时。以A1列开始A行数据获取下拉数组
        category1Name.setRefersToFormula(hiddenName + "!A1:A" + (deptList.length + endRow));
        //
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(hiddenName);
        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, endRow, cellNum, cellNum);
        DataValidation dataValidation = helper.createValidation(constraint, addressList);
        if (dataValidation instanceof XSSFDataValidation) {
            // 数据校验
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }
        // 作用在目标sheet上
        sheet.addValidationData(dataValidation);
        // 设置hiddenSheet隐藏
        workbook.setSheetHidden(sheetIndex, true);
    }

    private static void addCascade(XSSFWorkbook book, XSSFSheet sheetPro, String[] fathers, Map<String, Set<String>> areaMap, int pIndex, int curIndex) {
        Sheet hideSheet = book.createSheet("area");
        //这一行作用是将此sheet隐藏，功能未完成时注释此行,可以查看隐藏sheet中信息是否正确
        book.setSheetHidden(book.getSheetIndex(hideSheet), true);

        int rowId = 0;
        // 设置第一行，存省的信息
        Row provinceRow = hideSheet.createRow(rowId++);
        provinceRow.createCell(0).setCellValue("数据源类型");
        for (int i = 0; i < fathers.length; i++) {
            Cell provinceCell = provinceRow.createCell(i + 1);
            provinceCell.setCellValue(fathers[i]);
        }
        // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
        for (String key : fathers) {
            Set<String> set = areaMap.get(key);
            String[] son = set.toArray(new String[0]);
            Row row = hideSheet.createRow(rowId++);
            row.createCell(0).setCellValue(key);
            for (int j = 0; j < son.length; j++) {
                Cell cell = row.createCell(j + 1);
                cell.setCellValue(son[j]);
            }

            // 添加名称管理器
            String range = getRange(1, rowId, son.length);
            Name name = book.createName();
            //key不可重复
            name.setNameName(key);
            String formula = "area!" + range;
            name.setRefersToFormula(formula);
        }
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheetPro);

        DataValidationConstraint provConstraint = dvHelper.createExplicitListConstraint(fathers);
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList provRangeAddressList = new CellRangeAddressList(1, 100, pIndex, pIndex);
        DataValidation provinceDataValidation = dvHelper.createValidation(provConstraint, provRangeAddressList);
        //验证
        provinceDataValidation.createErrorBox("error", "请选择正确的类型");
        provinceDataValidation.setShowErrorBox(true);
        provinceDataValidation.setSuppressDropDownArrow(true);
        sheetPro.addValidationData(provinceDataValidation);

        //对前100行设置有效性
        for (int i = 2; i < 100; i++) {
            setDataValidation(getAlphabet(pIndex), sheetPro, i, curIndex + 1);
        }

    }

    private static String getAlphabet(int index) {
        String[] strArr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        return strArr[index];
    }

    /**
     * 设置有效性
     *
     * @param offset 主影响单元格所在列，即此单元格由哪个单元格影响联动
     * @param sheet
     * @param rowNum 行数
     * @param colNum 列数
     */
    private static void setDataValidation(String offset, XSSFSheet sheet, int rowNum, int colNum) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        DataValidation dataValidationList;
        dataValidationList = getDataValidationByFormula(
                "INDIRECT($" + offset + (rowNum) + ")", rowNum, colNum, dvHelper);
        sheet.addValidationData(dataValidationList);
    }

    /**
     * 加载下拉列表内容
     *
     * @param formulaString
     * @param naturalRowIndex
     * @param naturalColumnIndex
     * @param dvHelper
     * @return
     */
    private static DataValidation getDataValidationByFormula(
            String formulaString, int naturalRowIndex, int naturalColumnIndex, XSSFDataValidationHelper dvHelper) {
        // 加载下拉列表内容
        // 举例：若formulaString = "INDIRECT($A$2)" 表示规则数据会从名称管理器中获取key与单元格 A2 值相同的数据，
        //如果A2是江苏省，那么此处就是江苏省下的市信息。
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(formulaString);
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        int firstRow = naturalRowIndex - 1;
        int lastRow = naturalRowIndex - 1;
        int firstCol = naturalColumnIndex - 1;
        int lastCol = naturalColumnIndex - 1;
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                lastRow, firstCol, lastCol);
        // 数据有效性对象
        // 绑定
        XSSFDataValidation dataValidationList = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
        dataValidationList.setEmptyCellAllowed(false);
        dataValidationList.setSuppressDropDownArrow(true);
        dataValidationList.setShowErrorBox(true);
        // 设置输入信息提示信息
        dataValidationList.createPromptBox("下拉选择提示", "请使用下拉方式选择合适的值！");
        // 设置输入错误提示信息
        return dataValidationList;
    }

    /**
     * 计算formula
     *
     * @param offset   偏移量，如果给0，表示从A列开始，1，就是从B列
     * @param rowId    第几行
     * @param colCount 一共多少列
     * @return 如果给入参 1,1,10. 表示从B1-K1。最终返回 $B$1:$K$1
     */
    private static String getRange(int offset, int rowId, int colCount) {
        char start = (char) ('A' + offset);
        if (colCount <= 25) {
            char end = (char) (start + colCount - 1);
            return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
        } else {
            char endPrefix = 'A';
            char endSuffix;
            if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
                if ((colCount - 25) % 26 == 0) {// 边界值
                    endSuffix = (char) ('A' + 25);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                }
            } else {// 51以上
                if ((colCount - 25) % 26 == 0) {
                    endSuffix = (char) ('A' + 25);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26);
                }
            }
            return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
        }
    }

    //读取excel返回list
    private static List<String[]> readFileToList(Workbook workbook, int startRow, int lastCellNum) {
        if (workbook == null) {
            throw new ReadExcelException("workbook为空，读取失败");
        }
        List<String[]> list = new ArrayList<>();
        // 获取当前sheet工作表
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return list;
        }
        // 获得当前sheet的结束行
        int lastRowNum = sheet.getLastRowNum();
        if (startRow < 0 || startRow > lastRowNum) {
            throw new ReadExcelException("wrong startRow");
        }
        for (int rowNum = startRow; rowNum <= lastRowNum; rowNum++) {
            String[] cells = getCells(rowNum, sheet, lastCellNum);
            if (cells.length > 0) {
                list.add(cells);
            }
        }
        try {
            workbook.close();
        } catch (IOException e) {
            logger.error("close workbook failed");
        }
        return list;
    }

    //读取每一列的方法，返回数组
    private static String[] getCells(int rowNum, Sheet sheet, int lastCellNum) {
        // 获得当前行
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            return new String[]{};
        }
        // 获得当前行的开始列
        int firstCellNum = row.getFirstCellNum();
        if (lastCellNum == ALL) {
            lastCellNum = row.getLastCellNum();
        }
        // 获得当前行的列数
        String[] cells = new String[lastCellNum];
        // 循环当前行
        for (int cellNum = firstCellNum; cellNum < lastCellNum; cellNum++) {
            Cell cell = row.getCell(cellNum);
            String value = getCellValue(cell);
            cells[cellNum] = value;
        }
        return cells;
    }

    /**
     * 设置标头
     */
    private static IndexPo createHeader(XSSFSheet sheet, List<String> attributes, XSSFWorkbook workbook) {
        int pIndex = 0;
        int curIndex = 0;
        //标题头
        Row row0 = sheet.createRow(0);
        for (int i = 0; i < attributes.size(); i++) {
            String title = attributes.get(i).trim();
            if ("数据库类型".equalsIgnoreCase(title)) {
                pIndex = i;
            }
            if ("数据源".equalsIgnoreCase(title)) {
                curIndex = i;
            }
            Cell cell = row0.createCell(i);
            sheet.setColumnWidth(i, title.length() * 520);
            cell.setCellValue(title);
            cell.setCellStyle(genTitleStyle(workbook));
        }
        return new IndexPo(pIndex, curIndex);
    }

    private static void createValues(XSSFSheet sheet, List<Object> data, XSSFWorkbook workbook, IndexPo header) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        CellRangeAddressList addressList = null;
        XSSFDataValidation validation = null;
        if (CollectionUtils.isNotEmpty(data)) {
            return;
        }
        Row row = sheet.createRow(1);
        int colSize = data.size();
        int sheetIndex = 1;
        for (int i = 0; i < colSize; i++) {
            Object obj = data.get(i);
            if (obj instanceof String) {
                row.createCell(i).setCellValue(obj.toString());
            }
            if (obj instanceof List) {
                List<String> list = (List<String>) obj;
                int length = list.size();
                String[] dates = list.toArray(new String[length]);
                if (length < 10) {
                    XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
                            .createExplicitListConstraint(dates);
                    addressList = new CellRangeAddressList(1, 100, i, i);
                    validation = (XSSFDataValidation) dvHelper.createValidation(
                            dvConstraint, addressList);
                    sheet.addValidationData(validation);
                } else {
                    setLongXSSFValidation(workbook, dates, sheet, 1, 100, i, sheetIndex++);
                }

            }
            if (obj instanceof Map) {
                Map<String, Set<String>> dataSourceMap = (Map<String, Set<String>>) obj;
                if (!dataSourceMap.isEmpty()) {
                    Set<String> set = dataSourceMap.keySet();
                    addCascade(workbook, sheet, set.toArray(new String[0]), dataSourceMap, header.getpIndex(),
                            header.getCurIndex());
                }
            }
        }
    }

    static class IndexPo {
        int pIndex;
        int curIndex;

        int getpIndex() {
            return pIndex;
        }

        int getCurIndex() {
            return curIndex;
        }

        IndexPo(int pIndex, int curIndex) {
            this.pIndex = pIndex;
            this.curIndex = curIndex;
        }
    }
}