package io.zeta.metaspace.web.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 文件和Base64之间的相互转化工具类
 * @author Gridsum
 */
public class Base64Utils {

    private Base64Utils() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(Base64Utils.class);

    /**
     *
     * @param path  文件全路径(加文件名)
     * @return String
     * @description 将文件转base64字符串
     */
    public static String fileToBase64(String path) {
        String base64 = null;
        try {
            File file = new File(path);
            try (InputStream in = new FileInputStream(file)) {
                byte[] bytes = new byte[(int) file.length()];
                while (in.read(bytes) > 0) {
                    base64 = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            LOG.error("将文件转base64字符串失败！", e);
        }
        return base64;
    }

    /**
     * InputStream转Base64
     *
     * @param inputStream 输入流
     * @return 返回Base64字符串
     */
    public static String streamToBase64(InputStream inputStream) {
        try {
            //转换为base64
            byte[] bytes = IOUtils.toByteArray(inputStream);
            return new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("InputStream转Base64失败！", e);
            return "";
        }
    }

    /**
     * @param outFilePath  输出文件路径,  base64   base64文件编码字符串,  outFileName  输出文件名
     * @return String
     * @description BASE64解码成File文件
     */
    public static void base64ToFile(String outFilePath,String base64, String outFileName) {
        File file;
        //创建文件目录
        File dir = new File(outFilePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        try {
            byte[] bytes = Base64.decodeBase64(base64);
            file = new File(outFilePath + "/" + outFileName);
            try (java.io.FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(bytes);
            }
        } catch (Exception e) {
            LOG.error("BASE64解码成File文件失败！", e);
        }
    }

}
