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
<<<<<<< HEAD
 * @date 2019/7/22 10:15
=======
 * @date 2019/7/16 18:11
>>>>>>> feature/1.5.0-cache-redis
 */
package io.zeta.metaspace.web.cache;

import org.apache.atlas.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/22 10:15
=======

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/16 18:11
>>>>>>> feature/1.5.0-cache-redis
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CacheConfig.class);


    private static String hostName;
    private static int port;
    private static String engine;
    private static int expiration;
    private final static String CACHE_ON_REDIS = "redis";

    static {
        try {
            org.apache.commons.configuration.Configuration configuration = ApplicationProperties.get();
            engine = configuration.getString("metaspace.cache.type");
            hostName = configuration.getString("metaspace.cache.redis.host");
            port = configuration.getInt("metaspace.cache.redis.port");
            expiration = configuration.getInt("metaspace.cache.redis.expiration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
        // Defaults
        redisConnectionFactory.setHostName(hostName);
        redisConnectionFactory.setPort(port);
        return redisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }


    @Bean("customKeyGenerator")
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }


    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        if(CACHE_ON_REDIS.equals(engine)) {
            RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
            cacheManager.setDefaultExpiration(expiration);
            return cacheManager;
        } else {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource location = resolver.getResource("classpath:ehcache-setting.xml");
            return new EhCacheCacheManager(EhCacheManagerUtils.buildCacheManager(location));
        }
    }
}
