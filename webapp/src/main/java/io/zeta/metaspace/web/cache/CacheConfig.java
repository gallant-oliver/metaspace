package io.zeta.metaspace.web.cache;

import org.apache.atlas.ApplicationProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
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
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/16 18:11
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    private static String[] hostName;
    private static int port;
    private static String engine;
    private static int expiration;
    private final static String CACHE_ON_REDIS = "redis";
    private static String password;
    private static int maxClient;
    private static int database;
    /**
     * redis部署模式0：单节点 1：哨兵 2：集群
     */
    private static Integer mode;

    static {
        try {
            org.apache.commons.configuration.Configuration configuration = ApplicationProperties.get();
            engine = configuration.getString("metaspace.cache.type", CACHE_ON_REDIS);
            hostName = configuration.getStringArray("metaspace.cache.redis.host");
            port = configuration.getInt("metaspace.cache.redis.port", 6379);
            expiration = configuration.getInt("metaspace.cache.redis.expiration", 300);
            password = configuration.getString("metaspace.cache.redis.password");
            maxClient = configuration.getInt("metaspace.cache.redis.client.max", 128);
            database = configuration.getInt("metaspace.cache.redis.database", 0);
            mode = configuration.getInt("metaspace.cache.redis.mode", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxClient);
        //单节点和哨兵
        if (mode.equals(0) || mode.equals(1)) {
            JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory();
            // Defaults
            List<String> hostNameList = Arrays.stream(hostName).filter(Strings::isNotBlank).collect(Collectors.toList());
            redisConnectionFactory.setHostName(String.join(",", hostNameList));
            redisConnectionFactory.setPort(port);
            if (StringUtils.isNotBlank(password)) {
                redisConnectionFactory.setPassword(password);
            }
            redisConnectionFactory.setDatabase(database);
            redisConnectionFactory.setPoolConfig(config);
            return redisConnectionFactory;
        }
        //集群
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        Set<RedisNode> hostAndPorts = new HashSet<>();
        for (String value : hostName) {
            if (StringUtils.isBlank(value)) {
                continue;
            }
            hostAndPorts.add(new RedisNode(value, port));
        }
        redisClusterConfiguration.setClusterNodes(hostAndPorts);
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisClusterConfiguration, config);
        if (StringUtils.isNotBlank(password)) {
            jedisConnectionFactory.setPassword(password);
        }
        jedisConnectionFactory.setDatabase(database);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate redisTemplate(JedisConnectionFactory cf) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(cf);
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory cf) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(cf);
        return stringRedisTemplate;
    }

    @Bean("customKeyGenerator")
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        if (CACHE_ON_REDIS.equals(engine)) {
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
