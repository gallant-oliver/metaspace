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

package org.apache.atlas;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author lixiang03
 * @Data 2020/7/17 16:05
 */
public class ApolloConfig {
    private static final Logger LOG = LoggerFactory.getLogger(ApolloConfig.class);


    public static void getConfig(Configuration instance){
        //获取配置中心配置
        String meta = instance.getString("apollo.meta");
        System.setProperty("apollo.meta", meta);
        String cluster = instance.getString("apollo.cluster");
        System.setProperty("apollo.cluster", cluster);
        String id = instance.getString("app.id");
        System.setProperty("app.id", id);
        String cachedir = instance.getString("apollo.cacheDir");
        System.setProperty("apollo.cacheDir", cachedir);
        String secret = instance.getString("apollo.accesskey.secret");
        if (secret!=null&&secret.length()!=0){
            System.setProperty("apollo.accesskey.secret",secret);
        }
        String[] namespaces = instance.getStringArray("apollo.bootstrap.namespaces");
        //String namespace = instance.getString("apollo.bootstrap.namespaces");
        for(String namespace:namespaces){
            Config appConfig = ConfigService.getConfig(namespace);
            appConfig.addChangeListener(new ConfigChangeListener() {
                @Override
                public void onChange(ConfigChangeEvent changeEvent) {
                    for (String key : changeEvent.changedKeys()) {
                        ConfigChange change = changeEvent.getChange(key);
                        instance.setProperty(change.getPropertyName(),change.getNewValue());
                        LOG.info(String.format("Found change - key: %s, oldValue: %s, newValue: %s, changeType: %s", change.getPropertyName(), change.getOldValue(), change.getNewValue(), change.getChangeType()));
                    }
                }
            });
            //添加配置
            Set<String> propertyNames = appConfig.getPropertyNames();
            for (String key:propertyNames){
                String property = appConfig.getProperty(key, null);
                instance.setProperty(key,property);
            }
        }
    }
}
