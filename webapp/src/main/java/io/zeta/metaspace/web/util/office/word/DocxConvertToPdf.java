package io.zeta.metaspace.web.util.office.word;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Iterator;

/**
 * docx 转 pdf
 */
public class DocxConvertToPdf {
    private static final Logger log = LoggerFactory.getLogger(DocxConvertToPdf.class);
    public static void main(String argv[]) throws Exception{
        //convertDocxToPdf("topdf.docx", "docx-pdf.pdf", "C:\\Users\\Gridsum\\Desktop\\daily_file");
        //DocConvertToPdf.convertToHtml("topdftest.doc", "topdfhtml.html", "C:\\Users\\Gridsum\\Desktop\\daily_file");


       // byte[] bytes = FileUtils.readFileToByteArray(new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\topdfhtml.html"));

       // DocConvertToPdf.htmlTopdf(new String(bytes),new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\doc-pdf.pdf"));
    }

    /**
     * docx 转成 pdf
     *
     * @param source 待转换docx文件流
     * @param target 输出pdf文件名称
     * @throws Exception
     */
    public static void convertDocxToPdf(InputStream source, OutputStream target) {
        try {
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
                        log.info("中文字体注册成功.");
                        return fontChinese;
                    } catch (Exception e) {
                        log.error("中文字体注册失败.",e);
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
            log.error("docx->pdf 转换失败.",e);
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
