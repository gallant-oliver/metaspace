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

package io.zeta.metaspace.web.config;

import org.apache.atlas.ApplicationProperties;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FiletableConfig {

    private static final Logger LOG = LoggerFactory.getLogger(HiveJdbcUtils.class);
    public static final long MB = 1048576L;
    public static final long GB = 1073741824L;
    private static Long uploadMaxFileSize;
    private static String uploadPath;
    private static String uploadHdfsPath;

    static {
        try {
            Configuration conf = ApplicationProperties.get();
            uploadMaxFileSize = conf.getLong("metaspace.filetable.upload.maxFileSize", 100 * MB);
            uploadPath = conf.getString("metaspace.filetable.uploadPath", System.getProperty("java.io.tmpdir"));
            uploadHdfsPath = conf.getString("metaspace.filetable.uploadHdfsPath", System.getProperty("java.io.tmpdir"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Long getUploadMaxFileSize() {
        return uploadMaxFileSize;
    }

    public static String getUploadPath() {
        return uploadPath;
    }

    public static String getUploadHdfsPath() {
        return uploadHdfsPath;
    }
}
