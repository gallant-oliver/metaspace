package io.zeta.metaspace.web.util.office.word;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @Title: WordExport.java
 * @Description:  根据模板导出word文档
 * @version 1.0
 */
public class WordExport {
    private static final Logger logger = LoggerFactory.getLogger(WordExport.class);

    private String templatePath;
    private XWPFDocument doc = null;
    private FileInputStream is = null;
    private OutputStream os = null;


    public static boolean generateAttachTemplate(String demoTemplate){
        // 创建Word文件
        try(XWPFDocument docx = new XWPFDocument()) {
            XWPFParagraph p = docx.createParagraph();
            // 设置段落的对齐方式
            p.setAlignment(ParagraphAlignment.LEFT);
            //创建段落文本
            XWPFRun r = p.createRun();
            r.setText("元数据版本差异清单");
            r.setBold(true);//设置为粗体
            r.setColor("FF0000");//设置颜色
            p = docx.createParagraph();
            r = p.createRun();
            r.setBold(true);//设置为粗体
            r.setText("表名-基础信息");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("Ip：${ip}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("端口：${port}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("实例名：${instance}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("数据库名：${dbName}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("数据库类型：${dbType}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("业务负责人：${bizLeader}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("衍生表设计人：${deriveTableDesigner}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("技术目录路径：${techenicalPath}");
            p = docx.createParagraph();
            r = p.createRun();
            r.setText("业务目录路径：${businessPath}");

            p = docx.createParagraph();
            r = p.createRun();
            r.setBold(true);
            r.setText("模型变更详情");
            XWPFTable table = docx.createTable(2, 4);//创建一个表格
            CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
            width.setType(STTblWidth.DXA);
            width.setW(BigInteger.valueOf(9072));

            table.getRow(0).getCell(0).setText("变更列");
            table.getRow(0).getCell(1).setText("变更前");
            table.getRow(0).getCell(2).setText("变更类型");
            table.getRow(0).getCell(3).setText("变更后");
            table.getRow(1).getCell(0).setText("${changeColumn}");
            table.getRow(1).getCell(1).setText("${beforeInfo}");
            table.getRow(1).getCell(2).setText("${changeType}");
            table.getRow(1).getCell(3).setText("${afterInfo}");

            p = docx.createParagraph();
            r = p.createRun();
            r.setText("变更类型：列名称变更，列删除，列添加，列类型变更");

            logger.info("开始生成模板文档...");
            try (FileOutputStream out = new FileOutputStream(demoTemplate);) {
                docx.write(out);
                logger.info("生成模板文档成功.");
                return true;
            } catch (IOException e) {
                logger.error("生成模板文档失败。", e);
                return false;
            }
        }
        catch (Exception e){
            logger.error("生成模板文档失败。", e);
            return false;
        }
    }

    /**
     * 提供填充模板数据的方法
     * @param paragraphMap
     * @param excelMapList
     * @param demoTemplate 模板路径
     * @param targetPath 生成的目标文件
     */
    public static void fillDataTemplate(Map<String,Object> paragraphMap ,List<Map<String,String>> excelMapList,
                                        String demoTemplate ,String targetPath){
        //初始化导出
        WordExport export = new WordExport(demoTemplate);
        try {
            logger.info("word模板开始初始化...");
            export.init();
            logger.info("填充模板段落数据");
            export.export(paragraphMap);
            logger.info("填充模板表格数据");
            //0为表格的下标，第一个表格下标为0，以此类推
            export.export(excelMapList, 0);
            logger.info("模板表格数据文档导出目标路径 ");
            export.generate(targetPath);
        } catch (Exception e) {
            logger.error("生成模板文档失败,{}",e);
        }
    }
    public WordExport(String templatePath) {
        this.templatePath = templatePath;

    }

    public void init() throws IOException {
        is = new FileInputStream(new File(this.templatePath));
        doc = new XWPFDocument(is);
    }

    /**
     * 替换掉占位符
     *
     * @param params
     * @return
     * @throws Exception
     */
    public boolean export(Map<String, Object> params) throws Exception {
        this.replaceInPara(doc, params);
        return true;
    }



    /**
     * 循环生成表格
     *
     * @param params
     * @param tableIndex
     * @return
     * @throws Exception
     */
    public boolean export(List<Map<String, String>> params, int tableIndex) throws Exception {

        return export(params, tableIndex, false);
    }

    public boolean export(List<Map<String, String>> params, int tableIndex, Boolean hasTotalRow) throws Exception {
        this.insertValueToTable(doc, params, tableIndex, hasTotalRow);
        return true;
    }


    /**
     * 生成word文档
     *
     * @param outDocPath
     * @return
     * @throws IOException
     */
    public boolean generate(String outDocPath) throws IOException {
        os = new FileOutputStream(outDocPath);
        doc.write(os);
        this.close(os);
        this.close(is);
        return true;
    }

    /**
     * 替换段落里面的变量
     *
     * @param doc    要替换的文档
     * @param params 参数
     * @throws Exception
     */
    private void replaceInPara(XWPFDocument doc, Map<String, Object> params) throws Exception {
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        XWPFParagraph para;
        while (iterator.hasNext()) {
            para = iterator.next();
            this.replaceInPara(para, params);
        }
    }

    /**
     * 替换段落里面的变量
     *
     * @param para   要替换的段落
     * @param params 参数
     * @throws Exception
     * @throws IOException
     * @throws InvalidFormatException
     */
    private boolean replaceInPara(XWPFParagraph para, Map<String, Object> params) throws Exception {
        boolean data = false;
        List<XWPFRun> runs;
        //有符合条件的占位符
        if (this.matcher(para.getParagraphText()).find()) {
            runs = para.getRuns();
            data = true;
            Map<Integer, String> tempMap = new HashMap<>();
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString();
                //以"$"开头
                boolean begin = runText.indexOf("$") > -1;
                boolean end = runText.indexOf("}") > -1;
                if (begin && end) {
                    tempMap.put(i, runText);
                    fillBlock(para, params, tempMap, i);
                    continue;
                } else if (begin && !end) {
                    tempMap.put(i, runText);
                    continue;
                } else if (!begin && end) {
                    tempMap.put(i, runText);
                    fillBlock(para, params, tempMap, i);
                    continue;
                } else {
                    if (tempMap.size() > 0) {
                        tempMap.put(i, runText);
                        continue;
                    }
                    continue;
                }
            }
        } else if (this.matcherRow(para.getParagraphText())) {
            runs = para.getRuns();
            data = true;
        }
        return data;
    }

    /**
     * 填充run内容
     *
     * @param para
     * @param params
     * @param tempMap
     * @throws InvalidFormatException
     * @throws IOException
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    private void fillBlock(XWPFParagraph para, Map<String, Object> params,
                           Map<Integer, String> tempMap, int index)
            throws InvalidFormatException, IOException, Exception {
        Matcher matcher;
        if (tempMap != null && tempMap.size() > 0) {
            String wholeText = "";
            List<Integer> tempIndexList = new ArrayList<Integer>();
            for (Map.Entry<Integer, String> entry : tempMap.entrySet()) {
                tempIndexList.add(entry.getKey());
                wholeText += entry.getValue();
            }
            if (wholeText.equals("")) {
                return;
            }
            matcher = this.matcher(wholeText);
            if (matcher.find()) {
                boolean isPic = false;
                int width = 0;
                int height = 0;
                int picType = 0;
                String path = null;
                String keyText = matcher.group().substring(2, matcher.group().length() - 1);
                Object value = params.get(keyText);
                String newRunText = "";
                if (value instanceof String) {
                    newRunText = matcher.replaceFirst(String.valueOf(value));
                } else if (value instanceof Map) {//插入图片
                    isPic = true;
                    Map pic = (Map) value;
                    width = Integer.parseInt(pic.get("width").toString());
                    height = Integer.parseInt(pic.get("height").toString());
                    picType = getPictureType(pic.get("type").toString());
                    path = pic.get("path").toString();
                }
    
                //模板样式
                XWPFRun tempRun = null;
                // 直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
                // 所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
                for (Integer pos : tempIndexList) {
                    tempRun = para.getRuns().get(pos);
                    tempRun.setText("", 0);
                }
                Assert.notNull(tempRun, "模板样式为空");
                if (isPic) {
                    //addPicture方法的最后两个参数必须用Units.toEMU转化一下
                    //para.insertNewRun(index).addPicture(getPicStream(path), picType, "测试",Units.toEMU(width), Units.toEMU(height));
                    tempRun.addPicture(getPicStream(path), picType, "测试", Units.toEMU(width), Units.toEMU(height));
                } else {
                    //样式继承
                    if (newRunText.indexOf("\n") > -1) {
                        String[] textArr = newRunText.split("\n");
                        if (textArr.length > 0) {
                            //设置字体信息
                            String fontFamily = tempRun.getFontFamily();
                            int fontSize = tempRun.getFontSize();
                            //logger.info("------------------"+fontSize);
                            for (int i = 0; i < textArr.length; i++) {
                                if (i == 0) {
                                    tempRun.setText(textArr[0], 0);
                                } else {
                                    if (StringUtils.isNotEmpty(textArr[i])) {
                                        XWPFRun newRun = para.createRun();
                                        //设置新的run的字体信息
                                        newRun.setFontFamily(fontFamily);
                                        if (fontSize == -1) {
                                            newRun.setFontSize(10);
                                        } else {
                                            newRun.setFontSize(fontSize);
                                        }
                                        newRun.addBreak();
                                        newRun.setText(textArr[i], 0);
                                    }
                                }
                            }
                        }
                    } else {
                        tempRun.setText(newRunText, 0);
                    }
                }
            }
            tempMap.clear();
        }
    }

    /**
     * Clone Object
     *
     * @param obj
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private Object cloneObject(Object obj) throws Exception {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(obj);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);

        return in.readObject();
    }


    private void insertValueToTable(XWPFDocument doc, List<Map<String, String>> params, int tableIndex, Boolean hasTotalRow) throws Exception {
        List<XWPFTable> tableList = doc.getTables();
        if (tableList.size() <= tableIndex) {
            throw new Exception("tableIndex对应的表格不存在");
        }
        XWPFTable table = tableList.get(tableIndex);
        List<XWPFTableRow> rows = table.getRows();
        if (rows.size() < 2) {
            throw new Exception("tableIndex对应表格应该为2行");
        }
        //模板行
        XWPFTableRow tmpRow = rows.get(1);
        List<XWPFTableCell> tmpCells = null;
        List<XWPFTableCell> cells = null;
        XWPFTableCell tmpCell = null;

        tmpCells = tmpRow.getTableCells();
        String cellText = null;
        String cellTextKey = null;

        Map<String, Object> totalMap = null;

        for (int i = 0, len = params.size(); i < len; i++) {
            Map<String, String> map = params.get(i);
            if (map.containsKey("total")) {
                totalMap = new HashMap<>();
                totalMap.put("total", map.get("total"));
                continue;
            }
            XWPFTableRow row = table.createRow();
            row.setHeight(tmpRow.getHeight());
            cells = row.getTableCells();
            // 插入的行会填充与表格第一行相同的列数
            for (int k = 0, klen = cells.size(); k < klen; k++) {
                tmpCell = tmpCells.get(k);
                XWPFTableCell cell = cells.get(k);
                cellText = tmpCell.getText();
                if (StringUtils.isNotBlank(cellText)) {
                    //转换为mapkey对应的字段
                    cellTextKey = cellText.replace("$", "").replace("{", "").replace("}", "");
                    if (map.containsKey(cellTextKey)) {
                        setCellText(tmpCell, cell, map.get(cellTextKey));
                    }
                }
            }
        }
        // 删除模版行
        table.removeRow(1);
        if (hasTotalRow && totalMap != null) {
            XWPFTableRow row = table.getRow(1);
            XWPFTableCell cell = row.getCell(0);
            replaceInPara(cell.getParagraphs().get(0), totalMap);
            table.addRow(row);
            table.removeRow(1);
        }
    }

    private void setCellText(XWPFTableCell tmpCell, XWPFTableCell cell,
                             String text) throws Exception {

        CTTc cttc2 = tmpCell.getCTTc();
        CTTcPr ctPr2 = cttc2.getTcPr();

        CTTc cttc = cell.getCTTc();
        CTTcPr ctPr = cttc.addNewTcPr();
        //cell.setColor(tmpCell.getColor());
        // cell.setVerticalAlignment(tmpCell.getVerticalAlignment());
        if (ctPr2 != null && ctPr2.getTcW() != null) {
            ctPr.addNewTcW().setW(ctPr2.getTcW().getW());
        }
        if (ctPr2 != null && ctPr2.getVAlign() != null) {
            ctPr.addNewVAlign().setVal(ctPr2.getVAlign().getVal());
        }
        if (cttc2.getPList().size() > 0) {
            CTP ctp = cttc2.getPList().get(0);
            if (ctp.getPPr() != null) {
                if (ctp.getPPr().getJc() != null) {
                    cttc.getPList().get(0).addNewPPr().addNewJc()
                            .setVal(ctp.getPPr().getJc().getVal());
                }
            }
        }

        if (ctPr2 != null && ctPr2.getTcBorders() != null) {
            ctPr.setTcBorders(ctPr2.getTcBorders());
        }

        XWPFParagraph tmpP = tmpCell.getParagraphs().get(0);
        XWPFParagraph cellP = cell.getParagraphs().get(0);
        XWPFRun tmpR = null;
        if (tmpP.getRuns() != null && tmpP.getRuns().size() > 0) {
            tmpR = tmpP.getRuns().get(0);
        }

        List<XWPFRun> runList = new ArrayList<XWPFRun>();
        if (text == null) {
            XWPFRun cellR = cellP.createRun();
            runList.add(cellR);
            cellR.setText("");
        } else {
            //这里的处理思路是：$b认为是段落的分隔符，分隔后第一个段落认为是要加粗的
            if (text.indexOf("\b") > -1) {//段落，加粗，主要用于产品行程
                String[] bArr = text.split("\b");
                for (int b = 0; b < bArr.length; b++) {
                    XWPFRun cellR = cellP.createRun();
                    runList.add(cellR);
                    if (b == 0) {//默认第一个段落加粗
                        cellR.setBold(true);
                    }
                    if (bArr[b].indexOf("\n") > -1) {
                        String[] arr = bArr[b].split("\n");
                        for (int i = 0; i < arr.length; i++) {
                            if (i > 0) {
                                cellR.addBreak();
                            }
                            cellR.setText(arr[i]);
                        }
                    } else {
                        cellR.setText(bArr[b]);
                    }
                }
            } else {
                XWPFRun cellR = cellP.createRun();
                runList.add(cellR);
                if (text.indexOf("\n") > -1) {
                    String[] arr = text.split("\n");
                    for (int i = 0; i < arr.length; i++) {
                        if (i > 0) {
                            cellR.addBreak();
                        }
                        cellR.setText(arr[i]);
                    }
                } else {
                    cellR.setText(text);
                }
            }

        }

        // 复制字体信息
        if (tmpR != null) {
            //cellR.setBold(tmpR.isBold());
            //cellR.setBold(true);
            for (XWPFRun cellR : runList) {
                if (!cellR.isBold()) {
                    cellR.setBold(tmpR.isBold());
                }
                cellR.setItalic(tmpR.isItalic());
                cellR.setStrike(tmpR.isStrike());
                cellR.setUnderline(tmpR.getUnderline());
                cellR.setColor(tmpR.getColor());
                cellR.setTextPosition(tmpR.getTextPosition());
                if (tmpR.getFontSize() != -1) {
                    cellR.setFontSize(tmpR.getFontSize());
                }
                if (tmpR.getFontFamily() != null) {
                    cellR.setFontFamily(tmpR.getFontFamily());
                }
                if (tmpR.getCTR() != null) {
                    if (tmpR.getCTR().isSetRPr()) {
                        CTRPr tmpRPr = tmpR.getCTR().getRPr();
                        if (tmpRPr.isSetRFonts()) {
                            CTFonts tmpFonts = tmpRPr.getRFonts();
                            CTRPr cellRPr = cellR.getCTR().isSetRPr() ? cellR
                                    .getCTR().getRPr() : cellR.getCTR().addNewRPr();
                            CTFonts cellFonts = cellRPr.isSetRFonts() ? cellRPr
                                    .getRFonts() : cellRPr.addNewRFonts();
                            cellFonts.setAscii(tmpFonts.getAscii());
                            cellFonts.setAsciiTheme(tmpFonts.getAsciiTheme());
                            cellFonts.setCs(tmpFonts.getCs());
                            cellFonts.setCstheme(tmpFonts.getCstheme());
                            cellFonts.setEastAsia(tmpFonts.getEastAsia());
                            cellFonts.setEastAsiaTheme(tmpFonts.getEastAsiaTheme());
                            cellFonts.setHAnsi(tmpFonts.getHAnsi());
                            cellFonts.setHAnsiTheme(tmpFonts.getHAnsiTheme());
                        }
                    }
                }
            }
        }
        // 复制段落信息
        cellP.setAlignment(tmpP.getAlignment());
        cellP.setVerticalAlignment(tmpP.getVerticalAlignment());
        cellP.setBorderBetween(tmpP.getBorderBetween());
        cellP.setBorderBottom(tmpP.getBorderBottom());
        cellP.setBorderLeft(tmpP.getBorderLeft());
        cellP.setBorderRight(tmpP.getBorderRight());
        cellP.setBorderTop(tmpP.getBorderTop());
        cellP.setPageBreak(tmpP.isPageBreak());
        if (tmpP.getCTP() != null) {
            if (tmpP.getCTP().getPPr() != null) {
                CTPPr tmpPPr = tmpP.getCTP().getPPr();
                CTPPr cellPPr = cellP.getCTP().getPPr() != null ? cellP
                        .getCTP().getPPr() : cellP.getCTP().addNewPPr();
                // 复制段落间距信息
                CTSpacing tmpSpacing = tmpPPr.getSpacing();
                if (tmpSpacing != null) {
                    CTSpacing cellSpacing = cellPPr.getSpacing() != null ? cellPPr
                            .getSpacing() : cellPPr.addNewSpacing();
                    if (tmpSpacing.getAfter() != null) {
                        cellSpacing.setAfter(tmpSpacing.getAfter());
                    }
                    if (tmpSpacing.getAfterAutospacing() != null) {
                        cellSpacing.setAfterAutospacing(tmpSpacing
                                .getAfterAutospacing());
                    }
                    if (tmpSpacing.getAfterLines() != null) {
                        cellSpacing.setAfterLines(tmpSpacing.getAfterLines());
                    }
                    if (tmpSpacing.getBefore() != null) {
                        cellSpacing.setBefore(tmpSpacing.getBefore());
                    }
                    if (tmpSpacing.getBeforeAutospacing() != null) {
                        cellSpacing.setBeforeAutospacing(tmpSpacing
                                .getBeforeAutospacing());
                    }
                    if (tmpSpacing.getBeforeLines() != null) {
                        cellSpacing.setBeforeLines(tmpSpacing.getBeforeLines());
                    }
                    if (tmpSpacing.getLine() != null) {
                        cellSpacing.setLine(tmpSpacing.getLine());
                    }
                    if (tmpSpacing.getLineRule() != null) {
                        cellSpacing.setLineRule(tmpSpacing.getLineRule());
                    }
                }
                // 复制段落缩进信息
                CTInd tmpInd = tmpPPr.getInd();
                if (tmpInd != null) {
                    CTInd cellInd = cellPPr.getInd() != null ? cellPPr.getInd()
                            : cellPPr.addNewInd();
                    if (tmpInd.getFirstLine() != null) {
                        cellInd.setFirstLine(tmpInd.getFirstLine());
                    }
                    if (tmpInd.getFirstLineChars() != null) {
                        cellInd.setFirstLineChars(tmpInd.getFirstLineChars());
                    }
                    if (tmpInd.getHanging() != null) {
                        cellInd.setHanging(tmpInd.getHanging());
                    }
                    if (tmpInd.getHangingChars() != null) {
                        cellInd.setHangingChars(tmpInd.getHangingChars());
                    }
                    if (tmpInd.getLeft() != null) {
                        cellInd.setLeft(tmpInd.getLeft());
                    }
                    if (tmpInd.getLeftChars() != null) {
                        cellInd.setLeftChars(tmpInd.getLeftChars());
                    }
                    if (tmpInd.getRight() != null) {
                        cellInd.setRight(tmpInd.getRight());
                    }
                    if (tmpInd.getRightChars() != null) {
                        cellInd.setRightChars(tmpInd.getRightChars());
                    }
                }
            }
        }
    }

    /**
     * 删除表中的行
     *
     * @param tableAndRowsIdxMap 表的索引和行索引集合
     */
    public void deleteRow(Map<Integer, List<Integer>> tableAndRowsIdxMap) {
        if (tableAndRowsIdxMap != null && !tableAndRowsIdxMap.isEmpty()) {
            List<XWPFTable> tableList = doc.getTables();
            for (Map.Entry<Integer, List<Integer>> map : tableAndRowsIdxMap.entrySet()) {
                Integer tableIdx = map.getKey();
                List<Integer> rowIdxList = map.getValue();
                if (rowIdxList != null && rowIdxList.size() > 0) {
                    if (tableList.size() <= tableIdx) {
                        logger.error("表格" + tableIdx + "不存在");
                        continue;
                    }
                    XWPFTable table = tableList.get(tableIdx);
                    List<XWPFTableRow> rowList = table.getRows();
                    for (int i = rowList.size() - 1; i >= 0; i--) {
                        if (rowIdxList.contains(i)) {
                            table.removeRow(i);
                        }
                    }

                }
            }
        }
    }

    /**
     * 正则匹配字符串
     *
     * @param str
     * @return
     */
    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    /**
     * 正则匹配字符串
     *
     * @param str
     * @return
     */
    private boolean matcherRow(String str) {
        Pattern pattern = Pattern.compile("\\$\\[(.+?)\\]",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 根据图片类型，取得对应的图片类型代码
     *
     * @param picType
     * @return int
     */
    private int getPictureType(String picType) {
        int res = XWPFDocument.PICTURE_TYPE_PICT;
        if (picType != null) {
            if (picType.equalsIgnoreCase("png")) {
                res = XWPFDocument.PICTURE_TYPE_PNG;
            } else if (picType.equalsIgnoreCase("dib")) {
                res = XWPFDocument.PICTURE_TYPE_DIB;
            } else if (picType.equalsIgnoreCase("emf")) {
                res = XWPFDocument.PICTURE_TYPE_EMF;
            } else if (picType.equalsIgnoreCase("jpg") || picType.equalsIgnoreCase("jpeg")) {
                res = XWPFDocument.PICTURE_TYPE_JPEG;
            } else if (picType.equalsIgnoreCase("wmf")) {
                res = XWPFDocument.PICTURE_TYPE_WMF;
            }
        }
        return res;
    }

    private InputStream getPicStream(String picPath) throws Exception {
        URL url = new URL(picPath);
        //打开链接
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为5秒
        conn.setConnectTimeout(5 * 1000);
        //通过输入流获取图片数据
        InputStream is = conn.getInputStream();
        return is;
    }

    /**
     * 关闭输入流
     *
     * @param is
     */
    private void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭输出流
     *
     * @param os
     */
    private void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
