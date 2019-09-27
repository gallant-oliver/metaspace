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
 * @date 2019/9/4 18:57
 */
package io.zeta.metaspace.web.util;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.atlas.ApplicationProperties;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.InitializingBean;

import java.util.Iterator;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/4 18:57
 */
public class HystrixInitConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        PropertiesConfiguration hystrixConfig = new PropertiesConfiguration();
        try {
            System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");
            Iterator<String> hystrixKeys =  ApplicationProperties.get().getKeys("hystrix");
            while(hystrixKeys.hasNext()) {
                String key = hystrixKeys.next();
                String value = ApplicationProperties.get().getString(key);
                hystrixConfig.addProperty(key, value);
            }
            ConfigurationManager.install(hystrixConfig);
        }  catch (Exception e) {
            throw new IllegalArgumentException("安装hystrix配置项目失败。" + e.getMessage(), e);
        }
    }
}
