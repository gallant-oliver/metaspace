package org.apache.atlas.web.common.filetable;

import com.gridsum.gdp.library.commons.exception.VerifyException;

import org.apache.atlas.web.util.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class ExcelReader extends DefaultHandler {
    private String colFlag = "A";
    // 共享字符串表
    private SharedStringsTable sst;

    // 上一次的内容
    private String lastContents;
    private boolean nextIsString;

    private int sheetIndex = -1;
    private List<String> rowList = new ArrayList<>();

    // 当前行
    private int curRow = 0;
    // 当前列
    private int curCol = 0;
    // 日期标志
    private boolean dateFlag;
    // 数字标志
    private boolean numberFlag;

    private boolean isTElement;

    private List<List<String>> rows = null;

    private List<String> tableNames = new ArrayList<>();

    protected int totalNum = 0;

    private List<List<List<String>>> tableList = new ArrayList<>();

    public List<List<List<String>>> getTableList() {
        return tableList;
    }

    public void setTableList(List<List<List<String>>> tableList) {
        this.tableList = tableList;
    }

    /**
     * 遍历工作簿table name
     *
     * @param filename
     * @throws Exception
     */
    public void processTableName(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst);
        InputStream workbook = r.getWorkbookData();
        InputSource workbookSource = new InputSource(workbook);
        parser.parse(workbookSource);
        workbook.close();
    }

    /**
     * 遍历工作簿中所有的电子表格（解决大数据上传内存溢出问题）
     *
     * @param filename
     * @throws Exception
     */
    public void process(String filename) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst);
        InputStream workbook = r.getWorkbookData();
        InputSource workbookSource = new InputSource(workbook);
        parser.parse(workbookSource);
        workbook.close();
        Iterator<InputStream> sheets = r.getSheetsData();
        while (sheets.hasNext()) {
            curRow = 0;
            sheetIndex++;
            rows = new ArrayList<List<String>>();
            InputStream sheet = sheets.next();
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
            sheet.close();
            tableList.add(rows);
        }
    }

    /**
     * 只遍历一个电子表格，其中sheetId为要遍历的sheet索引，从1开始，1-3
     *
     * @param filename
     * @param sheetId
     * @throws Exception
     */
    public void process(String filename, int sheetId) throws Exception {
        OPCPackage pkg = OPCPackage.open(filename);
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        XMLReader parser = fetchSheetParser(sst);
        // 根据 rId# 或 rSheet# 查找sheet
        InputStream sheet2 = r.getSheet("rId" + sheetId);
        sheetIndex++;
        InputSource sheetSource = new InputSource(sheet2);
        parser.parse(sheetSource);
        sheet2.close();
    }

    public XMLReader fetchSheetParser(SharedStringsTable sst)
            throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader("com.sun.org.apache.xerces.internal.parsers.SAXParser");
        this.sst = sst;
        parser.setContentHandler(this);
        return parser;
    }

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {

//      System.out.println("startElement: " + localName + ", " + name + ", " + attributes);

        // c => 单元格
        if ("c".equals(name)) {
            // 如果下一个元素是 SST 的索引，则将nextIsString标记为true
            String colTag = StringUtils.extractString(attributes.getValue("r"));
            boolean notFit = true;
            while (notFit) {
                if (colFlag.equals(colTag)) {
                    notFit = false;
                } else {
                    rowList.add(curCol, " ");
                    curCol++;
                    colFlag = StringUtils.autoIncrementString(colFlag);
                }
            }
            String cellType = attributes.getValue("t");
            if ("s".equals(cellType)) {
                nextIsString = true;
            } else {
                nextIsString = false;
            }
            // 日期格式
            String cellDateType = attributes.getValue("s");
            if ("1".equals(cellDateType)) {
                dateFlag = true;
            } else {
                dateFlag = false;
            }
            String cellNumberType = attributes.getValue("s");
            if ("2".equals(cellNumberType)) {
                numberFlag = true;
            } else {
                numberFlag = false;
            }

        }
        // 当元素为t时
        if ("t".equals(name)) {
            isTElement = true;
        } else {
            isTElement = false;
        }

        if ("sheet".equals(name)) {
            tableNames.add(attributes.getValue("name"));
        }

        // 置空
        lastContents = "";
    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {

//      System.out.println("endElement: " + localName + ", " + name);

        // 根据SST的索引值的到单元格的真正要存储的字符串
        // 这时characters()方法可能会被调用多次
        if (nextIsString) {
            try {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx))
                        .toString();
            } catch (Exception e) {
            }
        }
        // t元素也包含字符串
        if (isTElement) {
            String value = lastContents.trim();
            rowList.add(curCol, value);
            curCol++;
            isTElement = false;
            // v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
            // 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
        } else if ("v".equals(name)) {
            String value = lastContents.trim();
            value = value.equals("") ? " " : value;
            try {
                // 日期格式处理
                if (dateFlag) {
                    Date date = HSSFDateUtil.getJavaDate(Double.valueOf(value));
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    value = dateFormat.format(date);
                }
                // 数字类型处理
                if (numberFlag) {
                    BigDecimal bd = new BigDecimal(value);
                    value = bd.setScale(3, BigDecimal.ROUND_UP).toString();
                }
            } catch (Exception e) {
                // 转换失败仍用读出来的值
            }
            rowList.add(curCol, value);
            curCol++;
            colFlag = StringUtils.autoIncrementString(colFlag);
        } else {
            // 如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
            if (name.equals("row")) {
                getRows(sheetIndex + 1, curRow, rowList);
                rowList.clear();
                curRow++;
                curCol = 0;
                colFlag = "A";
            }
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // 得到单元格内容的值
        lastContents += new String(ch, start, length);
    }

    /**
     * 获取行数据回调
     *
     * @param sheetIndex
     * @param curRow
     * @param rowList
     */
    public abstract void getRows(int sheetIndex, int curRow, List<String> rowList);

    public List<List<String>> getPreviewRows() {
        return this.rows;
    }

    public void add(List<String> line) {
        this.rows.add(line);
    }

    public List<String> getTableNames() {
        return tableNames;
    }

    public int getAllSize(boolean isIncludeHead) {
        if (isIncludeHead) {
            return this.totalNum - 1;
        }
        return this.totalNum;
    }

    /**
     * 获取头信息
     *
     * @param isIncludeHeader
     * @return
     */
    public CsvHeader getCsvHeader(boolean isIncludeHeader) {
        if (this.rows.get(0).isEmpty()) {
            throw new VerifyException("至少有一行数据");
        }
        int headerSize = this.rows.get(0).size();
        String[] headers = new String[headerSize];
        String[] comments = null;
        if (isIncludeHeader) {
            comments = new String[headerSize];
            for (int i = 0; i < headerSize; i++) {
                comments[i] = (String) this.rows.get(0).get(i);
            }
        }
        return new CsvHeader(headers, comments);
    }

}
