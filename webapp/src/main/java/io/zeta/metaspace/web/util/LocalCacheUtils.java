package io.zeta.metaspace.web.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存工具类
 */
public class LocalCacheUtils {

    /**
     * 元数据采集开关-手动关闭按钮
     */
    public final static Cache<String, String> RDBMS_METADATA_GATHER_ENABLE_CACHE = CacheBuilder.newBuilder().maximumSize(10000).
            expireAfterWrite(10, TimeUnit.HOURS).build();

}
