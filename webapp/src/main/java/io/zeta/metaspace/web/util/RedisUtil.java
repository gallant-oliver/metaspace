package io.zeta.metaspace.web.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huangrongwen
 * @Description: Redis的工具类
 * @date 2022/7/1217:53
 */
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, Long time) {
        stringRedisTemplate.opsForValue().set(key, value, time,TimeUnit.SECONDS);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * hash获取数据
     *
     * @return
     */
    public Object hmGet(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * hash添加数据
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void hmSet(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * hash添加map
     *
     * @param key
     * @param map
     */
    public void hmSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * hash删除某个key
     *
     * @param key
     * @param hashKey
     */
    public void hmDelete(String key, Object hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    /**
     * hash 判断key值是否存在
     *
     * @param key
     * @param hashKey
     * @return
     */
    public Boolean hmExistHashKey(String key, Object hashKey) {
        Map<String, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries == null) {
            return false;
        }
        for (String item : entries.keySet()) {
            if (item.equals(hashKey)) {
                return true;
            }
        }
        return false;
    }

}


