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

package org.apache.atlas.web.util;

import org.apache.atlas.ApplicationProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class HdfsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HdfsUtils.class);
    private static FileSystem fs;
    private static Configuration configuration = new Configuration();
    private static boolean kerberosEnable=false;
    private static String hdfsConf="/etc/hadoop/conf";


    static {
        try {
            org.apache.commons.configuration.Configuration conf = ApplicationProperties.get();
            hdfsConf = conf.getString("metaspace.hdfs.conf")==null?hdfsConf:conf.getString("metaspace.hdfs.conf");
            //默认kerberos关闭
            kerberosEnable=!(conf.getString("metaspace.kerberos.enable")==null||(!conf.getString("metaspace.kerberos.enable").equals("true")));

            configuration.addResource(new Path(hdfsConf,"core-site.xml"));
            configuration.addResource(new Path(hdfsConf,"hdfs-site.xml"));
            if(kerberosEnable) {
                if(
                        conf.getString("metaspace.kerberos.admin")  ==null||
                        conf.getString("metaspace.kerberos.keytab")  ==null||
                        conf.getString("metaspace.kerberos.admin")  .equals("")||
                        conf.getString("metaspace.kerberos.keytab")  .equals("")
                ){
                    LOG.error("kerberos info incomplete");
                }else {
                    configuration.set("hadoop.security.authentication", "Kerberos");
                    UserGroupInformation.setConfiguration(configuration);
                    UserGroupInformation.loginUserFromKeytab(conf.getString("metaspace.kerberos.admin"), conf.getString("metaspace.kerberos.keytab"));
                }
            }else{
                configuration.set("HADOOP_USER_NAME","metaspace");
            }
            fs = FileSystem.get(configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration conf(){
        return configuration;
    }

    public static FileSystem fs(){
        return fs;
    }

    public static FileStatus[] listStatus(String filePath) throws Exception {
        return fs.listStatus(new Path(filePath));
    }

    public static RemoteIterator<LocatedFileStatus> listFiles(String fileName, boolean recursive) throws Exception {
        return fs.listFiles(new Path("/"), recursive);
    }

    public static boolean exist(String filePath) throws IOException {
        boolean exists = fs.exists(new Path(filePath));
        return exists;
    }

    public static void uploadFile(InputStream inputStream, String filePath) throws IOException {
        FSDataOutputStream fsDataOutputStream = fs.create(new Path(filePath));
        IOUtils.copyBytes(inputStream, fsDataOutputStream, 4096, true);
    }

    public static InputStream downloadFile(String filePath) throws IOException {
        FSDataInputStream fsDataInputStream = fs.open(new Path(filePath));
        return fsDataInputStream;
    }

}
