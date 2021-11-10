package io.zeta.metaspace.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author wuyongliang
 * @Date 2021/11/9 17:27
 * @Description 数据资产检索
 */

@Service
public class DataAssetsRetrievalService {
    /**
     * 租户标签缓存（是否公共租户）
    */
    private static Cache<String, Boolean> tenantTagCache;

    private final static String USER_CACHE_EXPIRE = "user.info.expire";

    @Autowired
    private PublicService publicService;

    @Autowired
    private TenantService tenantService;

    // 租户资产业务目录模块id
    private int TENANT_ASSETS_BUSINESS_MODULE = 138;

    //当前租户是否是公共租户
    private boolean isPublicTenant(String tenantId) throws AtlasException {
        initTenantTagCache();

        String cacheKey = AdminUtils.getSSOTicket() + tenantId;

        Boolean isPublic = tenantTagCache.getIfPresent(cacheKey);
        if (isPublic == null) {
            List<Module> modules = tenantService.getModule(tenantId);
            if (!CollectionUtils.isEmpty(modules)) {
                List<Integer> moduleIds = modules.stream().map(m -> m.getModuleId()).collect(Collectors.toList());
                if (moduleIds.contains(TENANT_ASSETS_BUSINESS_MODULE)) {
                    isPublic = true;
                }
                else {
                    isPublic = false;
                }
            }

            tenantTagCache.put(cacheKey, isPublic);
        }

        return isPublic;
    }

    //当前租户是否是公共租户
    private boolean isGlobalUser() {
        return publicService.isGlobal();
    }

    /**
     * 初始化租户标签缓存
    */
    private void initTenantTagCache() throws AtlasException {
        if (tenantTagCache == null) {
            // 缓存失效时间（分钟）
            int expireTime = ApplicationProperties.get().getInt(USER_CACHE_EXPIRE, 30);
            tenantTagCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(expireTime, TimeUnit.MINUTES).build();
        }
    }

}
