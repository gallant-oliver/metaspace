package io.zeta.metaspace.web.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.*;
import java.net.InetAddress;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class HdfsUtils {
    //定义要解析的变量
    private static final String HOSTNAME_PATTERN = "_HOST";

    public String proxyUser;
    private FileSystem fs;
    private static final Configuration configuration;

    static {
        try {
            configuration = getHadoopConf();
        } catch (Exception e) {
            log.error("初始化 Hdfs 工具类失败", e);
            throw new AtlasBaseException(e);
        }
    }

    /**
     * 获取Hadoop配置
     * @return
     */
    public static Configuration getHadoopConf(){
        Configuration configuration = new Configuration();
        configuration.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
        String hadoopConfDir;
        try {
            hadoopConfDir = ApplicationProperties.get().getString("metaspace.hdfs.conf");
        } catch (AtlasException e) {
            log.error("初始化 Hdfs 工具类失败", e);
            throw new AtlasBaseException(e);
        }
        if (hadoopConfDir == null) {
            log.error("ENV HADOOP_CONF_DIR {} is not setting");
        }
        else {
            for (String file : new String[] {"yarn-site.xml", "core-site.xml", "hdfs-site.xml"}) {
                File site = new File(hadoopConfDir, file);
                if (site.exists() && site.isFile()) {
                    configuration.addResource(new org.apache.hadoop.fs.Path(site.toURI()));
                }
                else {
                        throw new AtlasBaseException("NOT Found HADOOP file: " + site);
                }
            }
        }
        return configuration;
    }


    public HdfsUtils() {
        this("metasapce");
    }

    public HdfsUtils(String proxyUser) {
        this.proxyUser = proxyUser;
        initFs();
    }

    private void initFs() {
        try {
            UserGroupInformation user = UserGroupInformation.createProxyUser(proxyUser, UserGroupInformation.getLoginUser());

            fs = user.doAs((PrivilegedAction<FileSystem>) () -> {
                try {
                    return FileSystem.get(configuration);
                } catch (IOException e) {
                    log.error("get hadoop FileSystem error", e);
                }
                return null;
            });
        } catch (IOException e) {
            throw new AtlasBaseException("初始化 Hdfs 异常", e);
        }
    }

    public FileSystem getFs() {
        if (fs == null) {
            throw new AtlasBaseException("HdfsUtils 初始化失败");
        }
        return fs;
    }

    public boolean exists(String hdfsFilePath) throws IOException {
        return fs.exists(new Path(hdfsFilePath));
    }

    public List<String> catFile(String hdfsFilePath, long lines) throws IOException {
        if (StringUtils.isBlank(hdfsFilePath) || lines == 0) {
            log.error("hdfs file path:{} is blank", hdfsFilePath);
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(new Path(hdfsFilePath)), UTF_8))) {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < lines || lines < 0; i++) {
                String line = reader.readLine();
                if (line == null)
                    break;
                result.add(line);
            }
            return result;
        }
    }

    public int getFileLine(String hdfsFilePath) throws IOException {
        if (!fs.exists(new Path(hdfsFilePath))){
            return 0;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(new Path(hdfsFilePath)), UTF_8));
             LineNumberReader lineReader = new LineNumberReader(reader);) {
            long skipSize = Long.MAX_VALUE;
            while(skipSize==Long.MAX_VALUE){
                skipSize = lineReader.skip(Long.MAX_VALUE);
            }
            int lineNumber = lineReader.getLineNumber();
            return lineNumber;
        }
    }

    public BufferedWriter getFileBufferWriter(String hdfsFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(hdfsFilePath)), UTF_8));
        return writer;
    }

    /**
     * 进行principal name(_HOST)的转换
     * @param principalConfig
     * @return
     * @throws IOException
     */
    public static String getServerPrincipal(String principalConfig) throws IOException {
        if(StringUtils.isBlank(principalConfig)){
            return principalConfig;
        }
        String[] components = principalConfig.split("/|@");
        if (components == null || components.length != 3
                || !components[1].equals(HOSTNAME_PATTERN)) {
            return principalConfig;
        } else {
            InetAddress addr = InetAddress.getLocalHost();
            if (addr == null) {
                throw new IOException("Can't replace " + HOSTNAME_PATTERN
                        + " pattern since client address is null");
            }
            return replacePattern(components, addr.getCanonicalHostName());
        }
    }
    private static String replacePattern(String[] components, String hostname)
            throws IOException {
        String fqdn = hostname;
        if (fqdn == null || fqdn.isEmpty() || fqdn.equals("0.0.0.0")) {
            fqdn = InetAddress.getLocalHost().getCanonicalHostName();
        }
        return components[0] + "/" +
                StringUtils.lowerCase(fqdn) + "@" + components[2];
    }
}
