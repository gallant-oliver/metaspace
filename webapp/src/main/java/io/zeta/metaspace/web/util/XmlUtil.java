package io.zeta.metaspace.web.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * @author huangrongwen
 * @Description: xml工具类
 * @date 2022/8/3017:29
 */
public class XmlUtil {

    private static XmlMapper xmlMapper = new XmlMapper();

    private static XmlMapper getXmlMapper() {
        xmlMapper = new XmlMapper.Builder(xmlMapper).defaultUseWrapper(false).build();
        //字段为null，自动忽略，不再序列化
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //XML标签名:使用骆驼命名的属性名，
        xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        //设置转换模式
        xmlMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        return xmlMapper;
    }

    //格式化
    public static String toFormatXml(Object value) {
        try {
            return formatXml(getXmlMapper().writeValueAsString(value));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toXml(Object value) {
        try {
            return getXmlMapper().writeValueAsString(value);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String formatXml(String str) throws Exception {
        Document document = null;
        document = DocumentHelper.parseText(str);
        // 格式化输出格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("gb2312");
        StringWriter writer = new StringWriter();
        // 格式化输出流
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        // 将document写入到输出流
        xmlWriter.write(document);
        xmlWriter.close();
        return writer.toString();
    }
}

