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

import com.carrotsearch.ant.tasks.junit4.dependencies.com.google.common.base.Joiner;
import io.zeta.metaspace.model.result.DownloadUri;
import org.apache.atlas.Atlas;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/18 13:47
 */
public class ExportDataPathUtils {

    public static String SEPARATOR = ",";

    public static DownloadUri generateURL(String address,List<String> ids) throws AtlasBaseException {
        String downloadId = UUID.randomUUID().toString();
        String downURL = address + "/" + downloadId;
        ExportDataPathUtils.generatePath2DataCache(downloadId, ids);
        DownloadUri uri = new DownloadUri();
        uri.setDownloadUri(downURL);
        return uri;
    }

    public static void generatePath2DataCache(String urlId, List<String> ids) throws AtlasBaseException {
        try {
            File dir = new File("/tmp/metaspace");
            if (!dir.exists()){
                dir.mkdir();
            }
            File file = new File(dir,urlId);
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
        File dir = new File("/tmp/metaspace");
        File file = new File(dir,urlId);
        BufferedReader reader = null;
        String line = null;
        try {
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                line = reader.readLine();
            }
            if(null == line) {
                return new ArrayList<>();
            }
            return Arrays.asList(line.split(SEPARATOR));
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        } finally {
            try {
                reader.close();
                file.delete();
            } catch (Exception e) {

            }
        }
    }
}
