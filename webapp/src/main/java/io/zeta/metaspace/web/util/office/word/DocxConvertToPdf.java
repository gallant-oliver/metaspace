package io.zeta.metaspace.web.util.office.word;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;

/**
 * docx 转 pdf
 */
public class DocxConvertToPdf {
    public static void main(String argv[]) throws Exception{
        //convertDocxToPdf("topdf.docx", "docx-pdf.pdf", "C:\\Users\\Gridsum\\Desktop\\daily_file");
        DocConvertToPdf.convertToHtml("topdftest.doc", "topdfhtml.html", "C:\\Users\\Gridsum\\Desktop\\daily_file");


        byte[] bytes = FileUtils.readFileToByteArray(new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\topdfhtml.html"));

        DocConvertToPdf.htmlTopdf(new String(bytes),new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\doc-pdf.pdf"));
    }

    /**
     * docx 转成 pdf
     *
     * @param inFileName 待转换docx文件名称
     * @param outFileName 输出pdf文件名称
     * @param catalogue 操作目录
     * @throws Exception
     */
    public static void convertDocxToPdf(String inFileName, String outFileName, String catalogue) {
        FileInputStream source = null;
        OutputStream target = null;
        try {
            String inPath = catalogue + File.separator + inFileName;
            String outPath = catalogue + File.separator + outFileName;
            // 待转换文档输入流
            source = new FileInputStream(new File(inPath));
            // 输出目标
            target = new FileOutputStream(outPath);
            // 转换配置
            PdfOptions options = PdfOptions.create();
            // 兼容中文配置
            options.fontProvider(new IFontProvider() {
                public Font getFont(String familyName, String encoding, float size, int style, java.awt.Color color) {
                    try {
                        ClassPathResource cpr = new ClassPathResource("fonts/STSONG.TTF");
                        BaseFont bfChinese = BaseFont.createFont(cpr.getPath(),
                                BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
//                        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                        Font fontChinese = new Font(bfChinese, size, style, color);
                        if (familyName != null)
                            fontChinese.setFamily(familyName);
                        return fontChinese;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
            // 待转换的docx文档对象
            XWPFDocument doc = new XWPFDocument(source);
            // 修改间距
            updateLineRuleInParagraph(doc);
            // 转换成pdf
            PdfConverter.getInstance().convert(doc, target, options);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            close(source);
            close(target);
        }
    }

    /**
     * 修改文档的行间距
     * @param doc 文档
     */
    private static void updateLineRuleInParagraph(XWPFDocument doc) {
        // 获取文档段落
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        XWPFParagraph para;
        // 遍历修改段落行间距
        while (iterator.hasNext()) {
            para = iterator.next();
            updateLineRuleInParagraph(para);
        }
    }

    /**
     * 修改段落的行间距
     * @param para 段落
     */
    private static void updateLineRuleInParagraph(XWPFParagraph para) {
        // 设置段落样式存储于行内
        para.setSpacingLineRule(LineSpacingRule.AT_LEAST);
        // 获取段落的CTPPr
        CTPPr pPPr = para.getCTP().getPPr();
        if (pPPr == null) {
            return;
        }
        // 获取段落的CTSpacing
        CTSpacing pSpacing = pPPr.getSpacing();
        if (pSpacing == null) {
            return;
        }
        // 修改行间距
        pSpacing.setLine(new BigInteger("240"));
        pSpacing.setLineRule(STLineSpacingRule.Enum.forString("auto")); // 自动行间距
    }

    /**
     * 关闭输入流
     * @param is
     */
    private static void close(InputStream is) {
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
     * @param os
     */
    private static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
