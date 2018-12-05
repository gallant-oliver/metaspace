package org.zeta.metaspace.web.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.zeta.metaspace.web.common.filetable.CsvHeader;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gridsum.gdp.library.commons.exception.VerifyException;

public final class ExcelUtils {

    static final Logger LOGGER = LoggerFactory.getLogger(ExcelUtils.class);

    private ExcelUtils() {
        //do nothing else
    }

    public static Workbook isExcelFile(String filePath) {
        try {
            Workbook hssfWorkbook = isExcelForXSL(new FileInputStream(filePath));
            if (hssfWorkbook != null) {
                return hssfWorkbook;
            } else {
                Workbook xssfWorkbook = isExcelForXLSX(new FileInputStream(filePath));
                if (xssfWorkbook != null) {
                    return xssfWorkbook;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static Workbook isExcelForXSL(InputStream inputStream) {
        try {
            return new HSSFWorkbook(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    public static Workbook isExcelForXLSX(InputStream inputStream) {
        try {
            return new XSSFWorkbook(inputStream);
        } catch (Exception e) {
            return null;
        }
    }

    public static CsvHeader readerExcelHeader(List<String> header,boolean isIncludeHeader){
        int headerSize=header.size();
        String[] headers = new String[headerSize];
        String[] comments = null;
        if (isIncludeHeader) {
            comments = new String[headerSize];
            for(int i = 0; i < headerSize; i++){
                comments[i]= header.get(i);
            }
        }
        return new CsvHeader(headers, comments);
    }

    public static CsvHeader readExcelHerder(Sheet sheet, boolean includeHeader) {
        Iterator<Row> iterator = sheet.rowIterator();
        if (!iterator.hasNext()) {
            throw new VerifyException("至少有一行数据！");
        }
        if (includeHeader) {
            Row row = iterator.next();
            int columnNum = row.getLastCellNum();
            String[] header = new String[columnNum];
            for (int i = 0; i < columnNum; ++i) {
                Cell cell = row.getCell(i);
                if (cell != null) {
                    header[i] = cell.toString();
                } else {
                    header[i] = "";
                }
            }
            return new CsvHeader(new String[columnNum], header);
        } else {
            int cellNum = iterator.next().getLastCellNum();
            return new CsvHeader(new String[cellNum], null);
        }
    }

    public static List<String> getAllSheetNames(Workbook workbook) {
        int sheetSize = workbook.getNumberOfSheets();
        List<String> sheetNames = new ArrayList<>();
        for (int i = 0; i < sheetSize; ++i) {
            String sheetName = workbook.getSheetAt(i).getSheetName();
            sheetNames.add(sheetName);
        }
        return sheetNames;
    }

    /**
     * 读取表头信息（俞青云）
     * @param sheet
     * @param headerSize
     * @return
     */
    public static List<String> readTableHeads(Sheet sheet,int headerSize){
        List<String> tableHeads=new ArrayList<String>();
        Iterator<Row> iterator=sheet.rowIterator();
        if(!iterator.hasNext()){
            throw new VerifyException("至少有一行数据");
        }
        Row row=iterator.next();
        for(int cellNum=0;cellNum<headerSize;cellNum++){
            Cell cell=row.getCell(cellNum);
            if(cell==null){
                tableHeads.add("NULL");
                continue;
            }else{
                if(cell instanceof HSSFCell){
                    tableHeads.add(((HSSFCell)cell).toString());
                }else if(cell instanceof XSSFCell){
                    tableHeads.add(((XSSFCell)cell).toString());
                }else{
                    LOGGER.warn("unsupported Cell Type:[{}]", cell);
                    throw new VerifyException("不支持的Excel类型！");
                }
            }
        }
        return tableHeads;
    }

    public static List<List<String>> readExcelDatas(Sheet sheet, int previewSize, int headerSize, boolean includeHeader) {
        List<List<String>> previewValues = Lists.newArrayListWithExpectedSize(previewSize);
        Iterator<Row> iterator = sheet.rowIterator();
        if (!iterator.hasNext()) {
            throw new VerifyException("至少有一行数据！");
        }
        if (includeHeader) {
            iterator.next();
        }
        int rowNum = 0;
        while (iterator.hasNext() && rowNum < previewSize) {
            List<String> values = Lists.newArrayListWithCapacity(headerSize);
            Row row = iterator.next();
            for (int cellNum = 0; cellNum < headerSize; ++cellNum) {
                Cell cell=null;
                try{
                    cell = row.getCell(cellNum);
                }catch(Exception e){
                    throw new VerifyException(String.format("Excel格式错误 sheet名:%s 行:%s 列:%s",sheet.getSheetName(),rowNum+1,cellNum+1));
                }
                if (cell == null) {
                    values.add("");
                    continue;
                } else {
                    if (cell instanceof HSSFCell) {
                        values.add(((HSSFCell) cell).toString());
                    } else if (cell instanceof XSSFCell) {
                        values.add(((XSSFCell) cell).toString());
                    } else {
                        LOGGER.warn("unsupported Cell Type:[{}]", cell);
                        throw new VerifyException("不支持的Excel类型！");
                    }
                }
            }
            previewValues.add(values);
            ++rowNum;
        }
        return previewValues;
    }

    public static Integer getDatasSize(Sheet sheet, boolean includeHeader) {
        int totleRowNums = sheet.getPhysicalNumberOfRows();
        if (includeHeader) {
            return totleRowNums - 1;
        }
        return totleRowNums;
    }
    public static Integer getDatasSize(List<List<String>> datas, boolean includeHeader) {
        int totleRowNums = datas.size();
        if (includeHeader) {
            return totleRowNums - 1;
        }
        return totleRowNums;
    }

    public static Object getHSSFCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case 0:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", LocaleUtil.getUserLocale());
                    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                    return sdf.format(cell.getDateCellValue());
                }
                return cell.getNumericCellValue();
            case 1:
                return cell.getStringCellValue();
            case 2:
                return cell.getCellFormula();
            case 3:
                return "";
            case 4:
                return cell.getBooleanCellValue();
            case 5:
            default:
                throw new RuntimeException("第[" + (cell.getRowIndex() + 1) + "]行[" + (cell.getColumnIndex() + 1) + "]列\n\r值是未知的类型！");
        }
    }

    public static Object getXSSFCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case 0:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy", LocaleUtil.getUserLocale());
                    sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                    return sdf.format(cell.getDateCellValue());
                }

                return cell.getNumericCellValue();
            case 1:
                return cell.getRichStringCellValue().toString();
            case 2:
                return cell.getCellFormula();
            case 3:
                return "";
            case 4:
                return cell.getBooleanCellValue();
            case 5:
            default:
                throw new RuntimeException("第[" + (cell.getRowIndex() + 1) + "]行[" + (cell.getColumnIndex() + 1) + "]列\n\r值是未知的类型！");
        }
    }
}
