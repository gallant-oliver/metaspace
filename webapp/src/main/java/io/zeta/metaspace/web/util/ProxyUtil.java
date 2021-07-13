package io.zeta.metaspace.web.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ProxyUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ProxyUtil.applicationContext = applicationContext;
    }

    public static <T> T getProxy(String name, Class<T> cls) {
        return applicationContext.getBean(name, cls);
    }

    public static <T> T getProxy(Class<T> cls) {
        return applicationContext.getBean(cls);
    }
}
