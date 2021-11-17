package io.zeta.metaspace.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.model.dataassets.BussinessObject;
import io.zeta.metaspace.model.dataassets.BussinessObjectList;
import io.zeta.metaspace.model.dataassets.DomainInfo;
import io.zeta.metaspace.model.dataassets.ThemeInfo;
import io.zeta.metaspace.model.enums.CategoryPrivateStatus;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.dao.BusinessDAO;
import io.zeta.metaspace.web.dao.DataAssetsRetrievalDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
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

    @Autowired
    DataAssetsRetrievalDAO dataAssetsRetrievalDAO;

    @Autowired
    UserGroupDAO userGroupDAO;

    @Autowired
    BusinessDAO businessDAO;

    // 租户资产业务目录模块id
    private int TENANT_ASSETS_BUSINESS_MODULE = 50;

    /**
     * 初始化租户标签缓存
     */
    static {
        try {
            if (tenantTagCache == null) {
                // 缓存失效时间（分钟）
                int expireTime = ApplicationProperties.get().getInt(USER_CACHE_EXPIRE, 30);
                tenantTagCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(expireTime, TimeUnit.MINUTES).build();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //当前租户是否是公共租户
    private boolean isPublicTenant(String tenantId) throws AtlasException {

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


    public List<DomainInfo> getThemeDomains(String tenantId) throws AtlasException {
        List<DomainInfo> domainList;
        Boolean isPublicTenant = isPublicTenant(tenantId);
        Boolean isPublicUser = isGlobalUser();
        String userId = AdminUtils.getUserData().getUserId();
        List<String> userGroupIds = null;
        if (isPublicTenant) {
            userGroupIds = userGroupDAO.getAlluserGroupByUsersId(userId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        } else {
            userGroupIds = userGroupDAO.getuserGroupByUsersId(userId, tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        }
        if (isPublicTenant && isPublicUser) {
            domainList = dataAssetsRetrievalDAO.getDemainCategory();
            for (DomainInfo domain : domainList) {
                domain.setThemeNum(dataAssetsRetrievalDAO.getThemeNumber(domain.getDomainId()));
            }
        } else {
            if (isPublicTenant) {
                tenantId = null;
            }
            domainList = dataAssetsRetrievalDAO.getDemainCategoryByNotPublicUser(userGroupIds, userId, tenantId);
            Iterator<DomainInfo> iterator = domainList.iterator();
            while (iterator.hasNext()) {
                DomainInfo domainInfo = iterator.next();
                String privateStatus = domainInfo.getPrivateStatus();
                String guid = domainInfo.getDomainId();
                if (CategoryPrivateStatus.PRIVATE.name().equals(privateStatus)) {
                    if (CollectionUtils.isEmpty(userGroupDAO.getCataUserGroupPrivilege(guid, userGroupIds))) {
                        if (userGroupDAO.getCateUserGroupRelationNum(guid) > 0) {
                            iterator.remove();
                        }
                    }
                }
            }
            for (DomainInfo domain : domainList) {
                List<DomainInfo> themeList = dataAssetsRetrievalDAO.getThemeByUserGroup(domain.getDomainId(), userGroupIds, userId, tenantId);
                Iterator<DomainInfo> iterator2 = themeList.iterator();
                while (iterator2.hasNext()) {
                    DomainInfo domainInfo = iterator2.next();
                    String privateStatus = domainInfo.getPrivateStatus();
                    String guid = domainInfo.getDomainId();
                    if (CategoryPrivateStatus.PRIVATE.name().equals(privateStatus)) {
                        if (CollectionUtils.isEmpty(userGroupDAO.getCataUserGroupPrivilege(guid, userGroupIds))) {
                            if (userGroupDAO.getCateUserGroupRelationNum(guid) > 0) {
                                iterator2.remove();
                            }
                        }
                    }
                }
                domain.setThemeNum(themeList.size());
            }
        }


        return domainList;
    }


    public List<ThemeInfo> getThemes(String guid, String tenantId) throws AtlasException {
        List<ThemeInfo> themeList = new ArrayList<>();
        List<DomainInfo> domainList;
        Boolean isPublicTenant = isPublicTenant(tenantId);
        Boolean isPublicUser = isGlobalUser();
        String userId = AdminUtils.getUserData().getUserId();
        List<String> userGroupIds = null;
        if (isPublicTenant) {
            userGroupIds = userGroupDAO.getAlluserGroupByUsersId(userId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        } else {
            userGroupIds = userGroupDAO.getuserGroupByUsersId(userId, tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
        }
        if (isPublicTenant && isPublicUser) {
            themeList = dataAssetsRetrievalDAO.getThemeCategory(guid);
            for (ThemeInfo theme : themeList) {
                List<String> businessList = dataAssetsRetrievalDAO.getBusinessId(theme.getThemeId());
                if (CollectionUtils.isEmpty(businessList)) {
                    theme.setBussinessObjectNum(0);
                    theme.setTableNum(0);
                } else {
                    theme.setBussinessObjectNum(businessList.size());
                    theme.setTableNum(dataAssetsRetrievalDAO.getTableNumber(businessList));
                }
            }
        } else {
            if (isPublicTenant) {
                tenantId = null;
            }
            domainList = dataAssetsRetrievalDAO.getThemeByUserGroup(guid, userGroupIds, userId, tenantId);
            Iterator<DomainInfo> iterator = domainList.iterator();
            while (iterator.hasNext()) {
                DomainInfo domainInfo = iterator.next();
                String privateStatus = domainInfo.getPrivateStatus();
                String id = domainInfo.getDomainId();
                if (CategoryPrivateStatus.PRIVATE.name().equals(privateStatus)) {
                    if (CollectionUtils.isEmpty(userGroupDAO.getCataUserGroupPrivilege(id, userGroupIds))) {
                        if (userGroupDAO.getCateUserGroupRelationNum(id) > 0) {
                            iterator.remove();
                        }
                    }
                }
            }
            for (DomainInfo domain : domainList) {
                ThemeInfo theme = new ThemeInfo();
                String themeId = domain.getDomainId();
                theme.setThemeId(themeId);
                theme.setThemeName(domain.getDomainName());

                List<String> businessList = dataAssetsRetrievalDAO.queryBusinessIdByUsergroup(themeId, tenantId, userId);
                if (CollectionUtils.isEmpty(businessList)) {
                    theme.setBussinessObjectNum(0);
                    theme.setTableNum(0);
                } else {
                    theme.setBussinessObjectNum(businessList.size());
                    theme.setTableNum(dataAssetsRetrievalDAO.getTableNumber(businessList));
                }
                themeList.add(theme);
            }
        }
        return themeList;
    }


    public BussinessObjectList getBusinesses(String guid, String tenantId, int limit, int offset) throws AtlasException {
        BussinessObjectList bussinessObjectList = new BussinessObjectList();
        List<BussinessObject> objectList;
        PageResult<BussinessObject> pageResult = new PageResult<>();
        Boolean isPublicTenant = isPublicTenant(tenantId);
        Boolean isPublicUser = isGlobalUser();
        String userId = AdminUtils.getUserData().getUserId();
        int total = 0;
        if (isPublicTenant && isPublicUser) {
            objectList = dataAssetsRetrievalDAO.queryBusiness(guid, limit, offset);
            if (!CollectionUtils.isEmpty(objectList)) {
                total = objectList.get(0).getTotal();
            }
        } else {
            if (isPublicTenant) {
                tenantId = null;
            }
            objectList = dataAssetsRetrievalDAO.queryBusinessByUsergroup(tenantId, guid, userId, limit, offset);
            if (!CollectionUtils.isEmpty(objectList)) {
                total = objectList.get(0).getTotal();
            }

        }
        CategoryEntityV2 categoryEntityV2 = dataAssetsRetrievalDAO.queryCategoryInfo(guid);
        bussinessObjectList.setThemeId(guid);
        bussinessObjectList.setThemeName(categoryEntityV2.getName());
        bussinessObjectList.setDescription(categoryEntityV2.getDescription());
        bussinessObjectList.setPath(categoryEntityV2.getQualifiedName().replaceAll("\\.", "\\/"));

        pageResult.setLists(objectList);
        pageResult.setCurrentSize(objectList.size());
        pageResult.setTotalSize(total);
        bussinessObjectList.setObjectPageResult(pageResult);

        return bussinessObjectList;
    }
}
