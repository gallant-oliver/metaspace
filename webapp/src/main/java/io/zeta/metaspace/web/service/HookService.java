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

package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.HookCheck;
import io.zeta.metaspace.web.rest.HookREST;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.kafka.KafkaNotification;
import org.apache.atlas.kafka.NotificationProvider;
import org.apache.atlas.notification.NotificationHookConsumer;
import org.apache.atlas.notification.NotificationInterface;
import org.apache.commons.configuration.Configuration;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author lixiang03
 * @Data 2020/4/14 10:48
 */
@Service
public class HookService {
    @Autowired
    NotificationHookConsumer notificationHookConsumer;
    private static final Logger LOG = LoggerFactory.getLogger(HookService.class);

    private final static String hookKeyTab = "atlas.jaas.KafkaClient.option.keyTab";
    private final static String hiveConfig = "metaspace.hive.conf";
    private final static String hiveBin = "metaspace.hive.bin";
    private final static List<String> hookStringConfig = new ArrayList<String>(){
        {
            add("atlas.cluster.name");
            add("atlas.jaas.KafkaClient.loginModuleControlFlag");
            add("atlas.jaas.KafkaClient.loginModuleName");
            add("atlas.jaas.KafkaClient.option.serviceName");
            add("atlas.kafka.bootstrap.servers");
            add("atlas.kafka.hook.group.id");
            add("atlas.kafka.sasl.kerberos.service.name");
            add("atlas.kafka.security.protocol");
            add("atlas.kafka.zookeeper.connect");
            add("atlas.notification.topics");
            add("atlas.notification.replicas");
            add("atlas.rest.address");
        }
    };
    private final static List<String> hookBooleanConfig = new ArrayList<String>(){
        {
            add("atlas.authentication.method.kerberos");
            add("atlas.jaas.KafkaClient.option.storeKey");
            add("atlas.notification.create.topics");
            add("atlas.jaas.KafkaClient.option.useKeyTab");
        }
    };

    /**
     * 获取hook的jar包加载情况
     * @return
     * @throws AtlasException
     * @throws AtlasBaseException
     * @throws IOException
     */
    public String hookJar() throws IOException, AtlasException, AtlasBaseException {
        List<String> hookPaths = new ArrayList<>();
        String metaspaceHook = System.getProperty(ApplicationProperties.ATLAS_CONFIGURATION_DIRECTORY_PROPERTY) + "/../hook/hive";
        Configuration applicationProperties = ApplicationProperties.get();
        String hiveBinPath = applicationProperties.getString(hiveBin,"/usr/bin");
        File file = new File(hiveBinPath, "/hiveserver2");
        if (!file.exists()){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "hiveserver2脚本不存在，请正确配置" + hiveBin);
        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        while((line=bufferedReader.readLine())!=null){
            if (line.matches(".*export.*HADOOP_CLASSPATH.*/hook/hive.*")) {
                char separatorChar = File.separatorChar;
                int start = line.indexOf(separatorChar);
                int end = line.lastIndexOf(separatorChar);
                String hookPath = line.substring(start, end);
                hookPaths.add(hookPath);
            }
        }

        for (String hookPath:hookPaths){
            if (equalsChildFile(hookPath,metaspaceHook)){
                return "hookJar包加载无误";
            }
        }
        return "hookJar包加载错误，可能hiveserver2不在本台机器或jar包文件未放置正确";
    }
    public boolean equalsChildFile(String path1,String path2){
        File file1 = new File(path1);
        File file2 = new File(path2);
        Set<String> file1Child = getChildFileName(file1);
        Set<String> file2Child = getChildFileName(file2);
        if (file1Child.size()!=file2Child.size()){
            LOG.warn("jar包个数不同");
            return false;
        }
        for (String childName : file1Child){
            if (!file2Child.contains(childName)){
                LOG.warn("jar包不同");
                return false;
            }
        }
        return true;
    }
    public Set<String> getChildFileName(File file){
        Set<String> childFileNames = new HashSet<>();
        if (!file.exists()){
            return childFileNames;
        }
        List<File> dirs = new ArrayList<>();
        if (file.isDirectory()){
            dirs.add(file);
        }else{
            childFileNames.add(file.getName());
        }
        while(dirs.size()!=0){
            File file1 = dirs.get(0);
            dirs.remove(0);
            File[] files = file1.listFiles();
            for (File childFile:files){
                if (childFile.isDirectory()){
                    dirs.add(childFile);
                }else if (childFile.getName().endsWith(".jar")){
                    childFileNames.add(childFile.getName());
                }
            }
        }
        return childFileNames;
    }

    /**
     * 获取hook配置情况
     * @return
     * @throws AtlasException
     * @throws AtlasBaseException
     */
    public List<String> hookConfigCheck() throws AtlasException, AtlasBaseException {
        String message= "%s 配置不同\n metaspace的配置为：%s\n hook的配置为：%s";
        Configuration applicationProperties = ApplicationProperties.get();
        String hiveConfPath = applicationProperties.getString(hiveConfig);
        File file = new File(hiveConfPath+"/atlas-application.properties");
        if (!file.exists()){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"hive的配置目录下没有atlas-application.properties文件，请正确配置hook");
        }
        Configuration configuration = ApplicationProperties.getByFile(file);
        if (configuration.getString(hookKeyTab,null)==null){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"未配置hook，请在参照安装手册大数据管理中对hook进行配置");
        }
        List<String> error = new ArrayList<>();
        for (String key:hookStringConfig){
            String metaspace = applicationProperties.getString(key,"");
            String hook = configuration.getString(key,"");
            if (!metaspace.equals(hook)){
                error.add(String.format(message,key,metaspace,hook));
            }
        }
        for (String key:hookBooleanConfig){
            Boolean metaspace = applicationProperties.getBoolean(key,null);
            Boolean hook = configuration.getBoolean(key,null);
            if (!(Objects.equals(metaspace, hook))){
                error.add(String.format(message,key,message,hook));
            }
        }
        return error;
    }

    /**
     * 获取kafka消费积压情况
     * @return
     * @throws AtlasBaseException
     */
    public long  kafkaCheck() throws AtlasBaseException {
        KafkaNotification kafkaNotification = NotificationProvider.get();
        try{
            KafkaConsumer kafkaConsumer = kafkaNotification.getNewKafkaConsumer(kafkaNotification.getConsumerProperties(NotificationInterface.NotificationType.HOOK), NotificationInterface.NotificationType.HOOK, false);
            //获取topic和分区
            Map<String, List<PartitionInfo>> topics = kafkaConsumer.listTopics();
            List<PartitionInfo> atlasHook = topics.get(KafkaNotification.ATLAS_HOOK_TOPIC);
            List<TopicPartition> assignment = new ArrayList<>();
            atlasHook.forEach(partitionInfo -> assignment.add(new TopicPartition(partitionInfo.topic(),partitionInfo.partition())));
            //最后提交的的offset
            Map<TopicPartition, Long> map = kafkaConsumer.endOffsets(assignment);
            long sum =0;
            long sumOffset=0;
            for (TopicPartition topicPartition : assignment){
                //最后提交的offset
                Long partitionOffset = map.get(topicPartition);
                //消费的offset；
                OffsetAndMetadata committed = kafkaConsumer.committed(topicPartition);
                //最后消费的offset
                long  readOffset = committed.offset();
                sum+=partitionOffset;
                sumOffset+=readOffset;
            }
            long lag = sum-sumOffset;
            kafkaConsumer.close();
            return lag;
        }catch (Exception e){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取kafka消费积压情况失败:"+e.getMessage());
        }
    }

    /**
     * 获取消费者线程情况
     * @return
     */
    public Map<String, Boolean> consumerThread(){
        return notificationHookConsumer.isAlive();
    }

    /**
     * 获取hook的所有检验
     * @return
     * @throws AtlasException
     * @throws AtlasBaseException
     * @throws IOException
     */
    public HookCheck all() throws AtlasException, AtlasBaseException, IOException {
        HookCheck hookCheck = new HookCheck();
//        hookCheck.setConsumerThread(notificationHookConsumer.isAlive());
//        hookCheck.setHookConfigCheck(hookConfigCheck());
//        hookCheck.setHookJar(hookJar());
        hookCheck.setKafkaCheck(kafkaCheck());
        return hookCheck;
    }

}
