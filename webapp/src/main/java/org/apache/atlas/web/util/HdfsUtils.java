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
import java.security.PrivilegedExceptionAction;

public class HdfsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(HdfsUtils.class);
    private static FileSystem fs;
    private static Configuration configuration = new Configuration();
    private static boolean kerberosEnable=false;
    private static String hdfsConf="/etc/hadoop/conf";
    private static String user="";


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
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static FileSystem getFs() throws IOException, InterruptedException {
        user=AdminUtils.getUserName();
        if(kerberosEnable) {
            UserGroupInformation proxyUser = UserGroupInformation.createProxyUser(user, UserGroupInformation.getLoginUser());
             fs = proxyUser.doAs(new PrivilegedExceptionAction<FileSystem>() {

                public FileSystem run() throws Exception {
                    return FileSystem.get(configuration);
                }
            });
        }else {
            configuration.set("HADOOP_USER_NAME",user);
            fs = FileSystem.get(configuration);
        }
        return fs;
    }
    public static FileSystem fs() throws IOException, InterruptedException {
        return getFs();
    }
    public static Configuration conf(){
        return configuration;
    }


    public static FileStatus[] listStatus(String filePath) throws Exception {
        return getFs().listStatus(new Path(filePath));
    }

    public static RemoteIterator<LocatedFileStatus> listFiles(String fileName, boolean recursive) throws Exception {
        return getFs().listFiles(new Path("/"), recursive);
    }

    public static boolean exist(String filePath) throws IOException, InterruptedException {
        boolean exists = getFs().exists(new Path(filePath));
        return exists;
    }

    public static void uploadFile(InputStream inputStream, String filePath) throws IOException, InterruptedException {
        FSDataOutputStream fsDataOutputStream = getFs().create(new Path(filePath));
        IOUtils.copyBytes(inputStream, fsDataOutputStream, 4096, true);
    }

    public static InputStream downloadFile(String filePath) throws IOException, InterruptedException {
        FSDataInputStream fsDataInputStream = getFs().open(new Path(filePath));
        return fsDataInputStream;
    }

}
