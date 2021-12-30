// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/9/18 13:47
 */
package io.zeta.metaspace.web.util;

import com.google.common.io.Files;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.model.result.DownloadUri;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/18 13:47
 */
public class ExportDataPathUtils {
    public static final String SEPARATOR = ",";
    public static final String EXCEL_FORMAT_XLSX = ".xlsx";
    public static final String EXCEL_FORMAT_XLS = ".xls";
    public static final int MAX_EXCEL_FILE_SIZE = 10 * 1024 * 1024;
    public static String tmpFilePath;
    
    static {
        tmpFilePath = System.getProperty("java.io.tmpdir");
        if (tmpFilePath.endsWith(String.valueOf(File.separatorChar))) {
            tmpFilePath = tmpFilePath + "metaspace";
        } else {
            tmpFilePath = tmpFilePath + File.separatorChar + "metaspace";
        }
    }
    
    public static DownloadUri generateURL(String address, List<String> ids) throws AtlasBaseException {
        String downloadId = UUID.randomUUID().toString();
        String downURL = address + "/" + downloadId;
        ExportDataPathUtils.generatePath2DataCache(downloadId, ids);
        DownloadUri uri = new DownloadUri();
        uri.setDownloadUri(downURL);
        return uri;
    }

    public static void generatePath2DataCache(String urlId, List<String> ids) throws AtlasBaseException {
        try {
            File dir = new File(tmpFilePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir, urlId);
            String idsStr = com.google.common.base.Joiner.on(SEPARATOR).join(ids);
            FileWriter fw = null;
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(file, true);
            fw.write(idsStr + System.getProperty("line.separator"));
            fw.close();
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public static List<String> getDataIdsByUrlId(String urlId) throws AtlasBaseException {
        File dir = new File(tmpFilePath);
        File file = new File(dir, urlId);
        BufferedReader reader = null;
        String line = null;
        try {
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                line = reader.readLine();
            }
            if (null == line) {
                return new ArrayList<>();
            }
            return Arrays.asList(line.split(SEPARATOR));
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                file.delete();
            } catch (Exception e) {

            }
        }
    }

    public static String transferTo(File file) throws AtlasBaseException, IOException {
        String uploadId = UUIDUtils.alphaUUID();
        String filePath = tmpFilePath + File.separatorChar + uploadId;
        FileUtils.forceMkdir(new File(tmpFilePath));
        File uploadFile = new File(filePath);
        File absoluteFile = uploadFile.getAbsoluteFile();
        try {
            Files.copy(file, absoluteFile);
        } catch (IOException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return uploadId;
    }

    public static File fileCheck(String name, InputStream fileInputStream) throws AtlasBaseException, IOException {
        if (!(name.endsWith(EXCEL_FORMAT_XLSX) || name.endsWith(EXCEL_FORMAT_XLS))) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
        }

        File file = new File(name);
        FileUtils.copyInputStreamToFile(fileInputStream, file);
        if (file.length() > MAX_EXCEL_FILE_SIZE) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
        }
        return file;
    }

    public static File fileCheckUuid(String name, InputStream fileInputStream) throws AtlasBaseException, IOException {
        if (!(name.endsWith(EXCEL_FORMAT_XLSX) || name.endsWith(EXCEL_FORMAT_XLS))) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
        }
        String filePath = tmpFilePath + File.separatorChar + UUIDUtils.alphaUUID() + File.separatorChar;
        FileUtils.forceMkdir(new File(filePath));
        File uploadFile = new File(filePath + name);
        FileUtils.copyInputStreamToFile(fileInputStream, uploadFile);
        if (uploadFile.length() > MAX_EXCEL_FILE_SIZE) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
        }
        return uploadFile;
    }
}
