package io.zeta.metaspace.web.service;

import io.zeta.metaspace.web.util.HdfsUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.solr.common.util.ContentStreamBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.util.List;

/**
 * hdfs 相关操作
 */
@Service
public class HdfsService {
    private static final Logger log = LoggerFactory.getLogger(HdfsService.class);
    private UserGroupInformation loginUser = null;
    private String username;
    private String keyTabPath;
    private String hdfsBasePath;
    private boolean enableKerberos = false;

    /**
     * 初始化 hdfs FileSystem
     * @return
     */
    private FileSystem initProxyFs(){
        Configuration conf = HdfsUtils.getHadoopConf();
        try{
            username = ApplicationProperties.get().getString("atlas.authentication.principal");
            keyTabPath = ApplicationProperties.get().getString("atlas.authentication.keytab");
            enableKerberos = ApplicationProperties.get().getBoolean("atlas.authentication.method.kerberos");
            //新增参数
            hdfsBasePath = ApplicationProperties.get().getString("metaspace.upload.hdfs.path");
        }catch (AtlasException e){
            log.error("初始化 Hdfs 失败", e);
            throw new AtlasBaseException(e);
        }
        FileSystem fs = null;
        log.info("Kerberos 是否开启验证:{}",enableKerberos);
        if(enableKerberos){
            UserGroupInformation loginUser = getLoginUser();
            UserGroupInformation proxyUser = UserGroupInformation.createProxyUser("user", loginUser);
            fs = proxyUser.doAs((PrivilegedAction<FileSystem>) () -> {
                try {
                    return FileSystem.get(conf);
                } catch (IOException e) {
                    log.error("get hadoop FileSystem error,{}",e);
                }
                return null;
            });
        }else{
            try {
                return FileSystem.get(conf);
            } catch (IOException e) {
                log.error("get hadoop FileSystem error,{}",e);
            }
            return null;
        }

        return fs;
    }

    private UserGroupInformation getLoginUser() {
        if(null == loginUser) {
            synchronized (HdfsService.class) {
                if (loginUser == null) {
                    try {
                        newUserGroupInformation();
                    } catch (IOException e) {
                        log.error("kerberos认证失败");
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return loginUser;
    }

    /**
     * 登录验证
     */
    private UserGroupInformation newUserGroupInformation() throws IOException {
        if (keyTabPath == null) {
            throw new RuntimeException("keytab file path is null");
        }
        Configuration conf = new Configuration();
        conf.set("hadoop.security.authentication", "Kerberos");
        UserGroupInformation.setConfiguration(conf);
        log.info("login kerberos,user={},keytab={}", username, keyTabPath);
        UserGroupInformation.loginUserFromKeytab(username, keyTabPath);
        loginUser = UserGroupInformation.getLoginUser();
        return loginUser;
    }

    /**
     * 上传文件方法
     * @param fileInputStream 上传的文件
     * @param destDir 上传路径（*基准路径不用添加）
     * @throws Exception 上传失败抛出异常
     *
     * @return 返回上传的目录路径
     */
    public String uploadFile(InputStream fileInputStream,String fileName,String destDir) throws IOException{
        FileSystem fileSystem = initProxyFs();
        if(fileSystem == null){
            log.error("hdfs 初始化为空");
            throw new RuntimeException("hdfs 初始化失败.");
        }

        //处理文件路径
        destDir = getHdfsAbsoluteFilePath(destDir,true);
        //String fileName = file.getOriginalFilename();
        try{
            String filePath = destDir+ fileName;
            Path destPath = new Path(destDir);
            if(!fileSystem.exists(destPath)){
                log.info("上传路径 {} 不存在，进行创建",destDir);
                fileSystem.mkdirs(destPath);
            }
            FSDataOutputStream fSDataOutputStream = fileSystem.create(new Path(filePath));
            IOUtils.copyBytes(fileInputStream,fSDataOutputStream,4096,true);
            return destDir+ fileName;
        }catch (IOException e) {
            log.error("上传文件 {} 出错",fileName);
            throw e;
        }
    }
    //组装 hdfs 的全路径
    private String getHdfsAbsoluteFilePath(String destPath,boolean isDir){
        if(isDir){//目录路径
            if(StringUtils.isBlank(destPath)){
                destPath = "/";
            }else{
                destPath = destPath.startsWith("/") ? destPath : "/"+destPath;
                destPath = destPath.endsWith("/") ? destPath : destPath+"/";
            }

            return hdfsBasePath+destPath;
        }

        //文件路径组装
        return hdfsBasePath+(destPath.startsWith("/") ? destPath : "/"+destPath);
    }
    public InputStream getFileInputStream(String filePath) throws IOException {
        FileSystem fileSystem = initProxyFs();
       // filePath = getHdfsAbsoluteFilePath(filePath,false);
        Path path = new Path(filePath);
        if (!fileSystem.exists(path)) {
            throw new RuntimeException("文件" + filePath + "不存在");
        }
        //FileStatus fileStatus = fileSystem.getFileStatus(path);
        //long len = fileStatus.getLen();
        return fileSystem.open(path);
    }

    /**
     * 读取hdfs excel文件，并读取数据到 list
     * @param filePath
     * @return
     * @throws IOException
     */
    public List<String[]> readExcelFile(String filePath) throws IOException {
        log.info("读物文件路径:{}",filePath);
        InputStream is = getFileInputStream(filePath);
        Workbook workbook = null;
        try{
            if(PoiExcelUtils.XLS.equalsIgnoreCase(FilenameUtils.getExtension(filePath))){
                // 2003版本
                workbook = new HSSFWorkbook(is);
            }else{
                // 2007版本
                workbook = new XSSFWorkbook(is);
            }
        }catch (IOException e){
            log.error("excel 文件读取失败");
            throw e;
        }

        List<String[]> excelList = PoiExcelUtils.readExcelFile(workbook);
        log.info("读取excel数据文件成功。");
        return excelList;
    }
}
