package io.zeta.metaspace.web.util.office.word;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.HTML;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * doc->pdf 逻辑：doc->html->pdf
 */
public class DocConvertToPdf {
    /**
     * html内容 转pdf文件
     * @param htmlContent html内容字符串
     * @param file
     * @throws Exception
     */
    public static void htmlTopdf(String htmlContent, File file) throws Exception {
        try {
            // step 1
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.BaseFont bfChinese
                    = com.itextpdf.text.pdf.BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", false);
            MyFontProvider myFontProvider = new MyFontProvider(BaseColor.BLACK, "", "", false, false, 16, 1, bfChinese);
            // step 2
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            // step 3
            document.open();

            final TagProcessorFactory tagProcessorFactory = Tags.getHtmlTagProcessorFactory();
            tagProcessorFactory.removeProcessor(HTML.Tag.IMG);
            tagProcessorFactory.addProcessor(new ImageTagProcessor(), HTML.Tag.IMG);

            final CssFilesImpl cssFiles = new CssFilesImpl();
            cssFiles.add(XMLWorkerHelper.getInstance().getDefaultCSS());
            final StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver(cssFiles);
            final HtmlPipelineContext hpc = new HtmlPipelineContext(new CssAppliersImpl(myFontProvider));
            hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(tagProcessorFactory);
            final HtmlPipeline htmlPipeline = new HtmlPipeline(hpc, new PdfWriterPipeline(document, writer));
            final Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);

            final XMLWorker worker = new XMLWorker(pipeline, true);

            final Charset charset = Charset.forName("UTF-8");
            final XMLParser xmlParser = new XMLParser(true, worker, charset);

            ByteArrayInputStream bis = new ByteArrayInputStream(htmlContent.getBytes("UTF-8"));
            xmlParser.parse(bis, charset);

           /* InputStreamReader isr = new InputStreamReader(new FileInputStream(html), "UTF-8");
            XMLWorkerHelper.getInstance().parseXHtml(writer,document,isr );
            */
            // step 5
            document.close();
            bis.close();
        } catch (Exception e) {
            throw new Exception(e);
        }
    }



    // 不闭合标签列表
    private static List<String> _targetTagName = Arrays.asList("((<br)([^<>]*?)(>))","((<hr)([^<>]*?)(>))","((<img)([^<>]*?)(>))",
            "((<input)([^<>]*?)(>))","((<link)([^<>]*?)(>))","((<meta)([^<>]*?)(>))",
            "((<area)([^<>]*?)(>))","((<base)([^<>]*?)(>))","((<col)([^<>]*?)(>))",
            "((<wbr)([^<>]*?)(>))","((<command)([^<>]*?)(>))","((<embed)([^<>]*?)(>))",
            "((<keygen)([^<>]*?)(>))","((<param)([^<>]*?)(>))","((<source)([^<>]*?)(>))",
            "((<track)([^<>]*?)(>))");

//    public static void main(String[] args) throws Exception {
//        convertToHtml("aaa.doc", "aaa.html","D:/test");
//    }

    /**
     *
     * @param docFileName doc文件名
     * @param htmlFileName 输出html文件名
     * @param catalogue 文件存储目录
     * @throws TransformerException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws Exception
     */
    public static void convertToHtml(String docFileName, String htmlFileName, String catalogue) {
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            String docFilePath = catalogue + File.separator + docFileName;
            String htmlFilePath = catalogue + File.separator + htmlFileName;
            // 输入流
            in = new FileInputStream(docFilePath);
            // 输出流
            out = new ByteArrayOutputStream();

            HWPFDocument wordDocument = new HWPFDocument(in);
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                    DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .newDocument());
            wordToHtmlConverter.setPicturesManager( new PicturesManager(){
                public String savePicture(byte[] content,
                                          PictureType pictureType, String suggestedName,
                                          float widthInches, float heightInches ){
                    return suggestedName;
                }
            } );
            // 保存图片
            savePictures(wordDocument, catalogue);
            // 转换对象
            wordToHtmlConverter.processDocument(wordDocument);
            Document htmlDocument = wordToHtmlConverter.getDocument();
            DOMSource domSource = new DOMSource(htmlDocument);

            StreamResult streamResult = new StreamResult(out);

            TransformerFactory tf = TransformerFactory.newInstance();
            // 设置编码格式，及转换文档类型
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "HTML");
            serializer.transform(domSource, streamResult);
            // 输出转换完成的html内容
            writeFile(new String(out.toByteArray()), htmlFilePath);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ParserConfigurationException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    // 关闭流
                    in.close();
                } catch (IOException e) { }
            }
            if (out != null) {
                try {
                    // 关闭流
                    out.close();
                } catch (IOException e) { }
            }
        }

    }

    /**
     * 保存文档图片
     *
     * @param wordDocument 文档
     * @param catalogue 保存目录
     */
    public static void savePictures(HWPFDocument wordDocument, String catalogue) {
        List<Picture> pics=wordDocument.getPicturesTable().getAllPictures();
        if(pics!=null){
            for(int i=0;i<pics.size();i++){
                Picture pic = pics.get(i);
                try {
                    String picturePath = catalogue + File.separator + pic.suggestFullFileName();
                    pic.writeImageContent(new FileOutputStream(picturePath));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 输出html文件
     * @param content 文件内容
     * @param path 输出路径
     */
    public static void writeFile(String content, String path) {
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        String utf8Content;
        try {
            utf8Content = new String(content.getBytes("UTF-8"), "UTF-8");
            org.jsoup.nodes.Document doc = Jsoup.parse(utf8Content);
            utf8Content = doc.outerHtml();
            utf8Content = setHtmlFont(utf8Content);
            utf8Content = handleTagClose(utf8Content);

            File file = new File(path);
            fos = new FileOutputStream(file);
            bw = new BufferedWriter(new OutputStreamWriter(fos,"UTF-8"));
            bw.write(utf8Content);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fos != null)
                    fos.close();
            } catch (IOException ie) {
            }
        }
    }

    /**
     * 正则处理html字符串标签闭合 关闭不闭合的标签
     * @param htmlStr html文件内容字符串
     * @return
     */
    public static String handleTagClose(String htmlStr) {
        // 需要替换的字符串
        String oldStr;
        // 替换成的字符串
        String newStr;
        // 需要处理闭合的标签表达式
        String reg;
        Pattern p;
        Matcher m;
        int num = _targetTagName.size();
        for (int i = 0; i < num; i ++) {
            reg = _targetTagName.get(i);
            p = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
            m = p.matcher(htmlStr);
            while (m.find()) {
                oldStr = m.group(1);
                newStr = oldStr.substring(0, oldStr.length() - 1) + " />";
                htmlStr = htmlStr.replace(oldStr, newStr);
            }
        }
        return htmlStr;
    }

    /**
     * 正则替换html字符串中的字体为指定字体
     *
     * @param htmlStr html文件内容字符串
     * @return
     */
    public static String setHtmlFont(String htmlStr) {
        String fontId = "STSong";
        // 字体替换正则表达式
        String reg = "((font-family\\s?:\\s?)([a-zA-z\\u4E00-\\u9FA5]*?)([;}]))";
        Pattern fontP = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher fontM = fontP.matcher(htmlStr);
        String fontName; // 匹配到的字体名称
        String oldStr; // 匹配到的字体样式字符串
        // 替换全文字体
        while (fontM.find()) {
            fontName = fontM.group(3);
            oldStr = fontM.group(1);
            // 和指定的字体相同，则不做处理
            if (fontName.equalsIgnoreCase(fontId)) {
                break;
            }
            // 判断替换的字符串位置是否是末尾
            if (oldStr.indexOf(";") != -1) {
                htmlStr = htmlStr.replace(oldStr, "font-family:" + fontId + ";");
            } else {
                htmlStr = htmlStr.replace(oldStr, "font-family:" + fontId + ";");
            }
        }
        // 设置body的字体，此处必须设置全局字体文件，不设置全局字体则设置的字体无效
        reg = "<style type=\"text/css\">";
        // 设置正则表达式和匹配规则为不区分大小写
        Pattern cssP = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        Matcher cssM = cssP.matcher(htmlStr);
        // 匹配body
        if (cssM.find()) {
            oldStr = cssM.group();
            htmlStr = htmlStr.replace(oldStr, oldStr + "body{font-family:" + fontId + ";}");
        }
        return htmlStr;
    }


}
