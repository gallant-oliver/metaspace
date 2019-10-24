package io.zeta.metaspace.web.cache;

import org.apache.atlas.ApplicationProperties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.cache.interceptor.CacheErrorHandler;
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
import redis.clients.jedis.JedisPoolConfig;

import org.slf4j.Logger;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/16 18:11
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static String hostName;
    private static int port;
    private static String engine;
    private static int expiration;
    private final static String CACHE_ON_REDIS = "redis";
    private static String password;
    private static int maxClient;

    static {
        try {
            org.apache.commons.configuration.Configuration configuration = ApplicationProperties.get();
            engine = configuration.getString("metaspace.cache.type");
            hostName = configuration.getString("metaspace.cache.redis.host");
            port = configuration.getInt("metaspace.cache.redis.port");
            expiration = configuration.getInt("metaspace.cache.redis.expiration");
            password = configuration.getString("metaspace.cache.redis.password");
            maxClient = configuration.getInt("metaspace.cache.redis.client.max");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxClient);
        // Defaults
        redisConnectionFactory.setHostName(hostName);
        redisConnectionFactory.setPort(port);
        if(StringUtils.isNotBlank(password)) {
            redisConnectionFactory.setPassword(password);
        }
        redisConnectionFactory.setPoolConfig(config);
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

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }

    public static class RedisCacheErrorHandler implements CacheErrorHandler {

        private static final Logger log = LoggerFactory.getLogger(RedisCacheErrorHandler.class);

        @Override
        public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("handleCacheGetError key = {}, value = {}", key, cache);
            log.error("cache get error", exception);
        }

        @Override
        public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("handleCachePutError key = {}, value = {}", key, cache);
            log.error("cache put error", exception);
        }

        @Override
        public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("handleCacheEvictError key = {}, value = {}", key, cache);
            log.error("cache evict error", exception);
        }

        @Override
        public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("handleCacheClearError value = {}", cache);
            log.error("cache clear error", exception);
        }
    }
}