package io.zeta.metaspace.web.util.office.word;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.core.FileImageExtractor;
import fr.opensagres.poi.xwpf.converter.core.FileURIResolver;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLConverter;
import fr.opensagres.poi.xwpf.converter.xhtml.XHTMLOptions;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import io.zeta.metaspace.web.util.Base64Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

/**
 * docx 转 pdf
 */
public class DocxConvertToPdf {
    private static final Logger log = LoggerFactory.getLogger(DocxConvertToPdf.class);
    public static void main(String argv[]) throws Exception{
        System.out.println(Base64Utils.fileToBase64("C:\\Users\\Gridsum\\Desktop\\daily_file\\docx-pdf.pdf"));
//        convertDocxToPdf(new FileInputStream("C:\\Users\\Gridsum\\Desktop\\daily_file\\topdf.docx"),
//                new FileOutputStream("C:\\Users\\Gridsum\\Desktop\\daily_file\\docx-pdf.pdf") );
//        //DocConvertToPdf.convertToHtml("topdftest.doc", "topdfhtml.html", "C:\\Users\\Gridsum\\Desktop\\daily_file");
//        File out = new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\docx-pdf.html");
//        String content = docx2Html(new FileInputStream("C:\\Users\\Gridsum\\Desktop\\daily_file\\topdf.docx"),out);
//        DocConvertToPdf.htmlTopdf(content/*new String(FileUtils.readFileToByteArray(out))*/,new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\docx-pdf.pdf"));
       // byte[] bytes = FileUtils.readFileToByteArray(new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\topdfhtml.html"));

       // DocConvertToPdf.htmlTopdf(new String(bytes),new File("C:\\Users\\Gridsum\\Desktop\\daily_file\\doc-pdf.pdf"));
    }

    /**
     * doc转pdf （合并了doc转html，html转pdf的操作）
     * @param in
     * @param dest
     */
    public static void docxToPdf(InputStream in ,File dest){
        File tmpHtml = null;
        try {
            tmpHtml = File.createTempFile("tmpHtml",".html");
            //转换doc到html成功
            String content = docx2Html(in,tmpHtml);

            DocConvertToPdf.htmlTopdf(content,dest);
            if(in != null){
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(tmpHtml != null && tmpHtml.exists()){
                tmpHtml.delete();
            }
        }
    }

    /**
     * docx格式word转换为html
     *
     * @param in
     *            docx文件路径
     * @param outFile
     *            html输出文件路径
     */
    public static String docx2Html(InputStream in, File outFile) throws  IOException {
        XWPFDocument document = new XWPFDocument(in);
        XHTMLOptions options = XHTMLOptions.create().indent(4);
        // 导出图片
        File imageFolder = FileUtils.getTempDirectory();

        options.setExtractor(new FileImageExtractor(imageFolder));
        // URI resolver
        options.URIResolver(new FileURIResolver(imageFolder));
        OutputStream out = new FileOutputStream(outFile);
        XHTMLConverter.getInstance().convert(document, out, options);
        out.close();
        Document doc = Jsoup.parse(outFile,"utf-8");
        String style = doc.body().attr("style");
        if(StringUtils.isNotBlank(style) && style.indexOf("width") != -1){
            doc.body().attr("style","");
        }
        Elements divs = doc.select("div");
        for(int i = 0;i < divs.size();i++){
            Element div = divs.get(i);
            style = div.attr("style");
            if(StringUtils.isNotBlank(style) && style.indexOf("width") != -1){
                div.attr("style","");
            }
        }
        String utf8Content = doc.outerHtml();
        utf8Content = DocConvertToPdf.setHtmlFont(utf8Content);
        utf8Content = DocConvertToPdf.handleTagClose(utf8Content);
        return utf8Content;
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
                        FontFactory.register(cpr.getPath());
                        Set set = FontFactory.getRegisteredFonts();
                        log.info("Registered Fonts :{}",set);
//                        Font fontChinese = FontFactory.getFont("STSong");
                        BaseFont bfChinese = BaseFont.createFont(cpr.getPath(),
                                BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                        Font fontChinese = new Font(bfChinese, size, style, color);
                        if (familyName != null)
                            fontChinese.setFamily(familyName);
                       // log.info("中文字体注册成功.");
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
