package io.zeta.metaspace.web.util;

import org.apache.commons.codec.binary.Base64;

import java.io.*;

/**
 * 文件和Base64之间的相互转化工具类
 */
public class Base64Utils {
    /**
     *
     * @param path  文件全路径(加文件名)
     * @return String
     * @description 将文件转base64字符串
     */
    public static String fileToBase64(String path) {
        String base64 = null;
        InputStream in = null;
        try {
            File file = new File(path);
            in = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            in.read(bytes);
            base64 = new String(Base64.encodeBase64(bytes),"UTF-8");
            //System.out.println("将文件["+path+"]转base64字符串:"+base64);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return base64;
    }

    /**
     * @param outFilePath  输出文件路径,  base64   base64文件编码字符串,  outFileName  输出文件名
     * @return String
     * @description BASE64解码成File文件
     */
    public static void base64ToFile(String outFilePath,String base64, String outFileName) {
        //System.out.println("BASE64:["+base64+"]解码成File文件["+outFilePath+"\\"+outFileName+"]");
        File file = null;
        //创建文件目录
        String filePath=outFilePath;
        File  dir=new File(filePath);
        if (!dir.exists() && !dir.isDirectory()) {
            dir.mkdirs();
        }
        BufferedOutputStream bos = null;
        java.io.FileOutputStream fos = null;
        try {
            byte[] bytes = Base64.decodeBase64(base64);
            file=new File(filePath+"/"+outFileName);
            fos = new java.io.FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 单元测试
     */
    public static void main(String[] args) {

        //定义文件路径
        String filePath = "E:\\FileTest\\logs\\log4jtest.log";
        //将文件转base64字符串
        String base64 = fileToBase64(filePath);

        System.out.println();
        //定义输出文件的路径outFilePath和输出文件名outoutFileName
        String outFilePath = "E:\\FileTest\\logs";
        String outFileName = "test.log";
        //将BASE64解码成File文件
        base64ToFile(outFilePath, base64, outFileName);

    }
}
