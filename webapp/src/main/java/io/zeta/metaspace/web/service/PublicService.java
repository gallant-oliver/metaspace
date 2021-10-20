package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.global.CategoryGlobal;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.TenantDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.dao.UserPermissionDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PublicService {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private UserGroupDAO userGroupDAO;

    @Autowired
    private DataManageService dataManageService;

    @Autowired
    private UserPermissionDAO userPermissionDAO;

    @Autowired
    private TenantDAO tenantDAO;

    @Autowired
    private BusinessService businessService;

    /**
     * 获取目录
     *
     * @param categoryType 目录类型
     * @return
     */
    public List<CategoryGlobal> getCategory(Integer categoryType) {
        Boolean global = isGlobal();
        List<CategoryGlobal> categoryGlobalList = new ArrayList<>();
        Set<CategoryEntityV2> categoryEntityV2s;
        try {
            //获取所有租户
            List<Tenant> tenants = tenantService.getTenants();
            if (global) {
                categoryEntityV2s = getCategoryGlobal(categoryType);
            } else {
                categoryEntityV2s = getCategoryGeneral(categoryType);
            }
            if (CollectionUtils.isEmpty(categoryEntityV2s)) {
                return categoryGlobalList;
            }
            removeNoParentCategory(categoryEntityV2s);
            Map<String, List<CategoryEntityV2>> map = categoryEntityV2s.stream().collect(Collectors.groupingBy(CategoryEntityV2::getTenantid));
            for (Tenant tenant : tenants) {
                CategoryGlobal categoryGlobal = new CategoryGlobal();
                categoryGlobal.setTenantId(tenant.getTenantId());
                categoryGlobal.setTenantName(tenant.getProjectName());
                categoryGlobal.setChildren(map.get(tenant.getTenantId()) == null ? new ArrayList<>() : map.get(tenant.getTenantId()));
                categoryGlobalList.add(categoryGlobal);
            }
        } catch (AtlasBaseException e) {
            log.error("getCategory exception {}", e);
        }
        return categoryGlobalList;
    }

    /**
     * 获取全局用户的目录
     *
     * @return
     */
    public Set<CategoryEntityV2> getCategoryGlobal(Integer categoryType) {
        return categoryDAO.selectGlobal(categoryType);
    }

    /**
     * 获取普通用户的目录
     *
     * @param categoryType
     * @return
     */
    public Set<CategoryEntityV2> getCategoryGeneral(Integer categoryType) {
        Set<CategoryEntityV2> categoryEntityV2s = new HashSet<>();
        try {
            User user = AdminUtils.getUserData();
            //获取用户组
            List<UserGroup> userGroups = userGroupDAO.selectListByUsersId(user.getUserId());
            List<String> userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            if (categoryType == 0) {
                categoryEntityV2s = categoryDAO.selectListByStatus(user.getUserId(), userGroupIds, categoryType);
            } else {
                //目录管理权限
                List<CategoryEntityV2> categories = userGroupDAO.getAllCategoryByCommonTenant(userGroupIds, categoryType, user.getUserId());
                categoryEntityV2s = new HashSet<>(categories);
            }
        } catch (AtlasBaseException e) {
            log.error("getCategoryGeneral exception {}", e);
        }
        return categoryEntityV2s;
    }

    /**
     * 删除有父目录，但是父目录不存在的目录
     *
     * @param categoryEntityV2s
     */
    public void removeNoParentCategory(Set<CategoryEntityV2> categoryEntityV2s) {
        Map<String, String> map = categoryEntityV2s.stream().collect(HashMap::new, (m, v) -> m.put(v.getGuid(), v.getParentCategoryGuid()), HashMap::putAll);
        categoryEntityV2s.removeIf(categoryPrivilege -> this.checkParentIfExist(map, categoryPrivilege.getParentCategoryGuid(), categoryEntityV2s));
    }

    private boolean checkParentIfExist(Map<String, String> map, String parentId, Set<CategoryEntityV2> categoryEntityV2s) {
        if (StringUtils.isEmpty(parentId)) {
            return false;
        }
        if (map.containsKey(parentId)) {
            Optional<CategoryEntityV2> result = categoryEntityV2s.stream().filter(c -> parentId.equals(c.getGuid())).findFirst();
            if (result.isPresent()) {
                return checkParentIfExist(map, result.get().getParentCategoryGuid(), categoryEntityV2s);
            }
        }
        return true;
    }

    public PageResult<RelationEntityV2> getCategoryRelations(String categoryGuid, RelationQuery query, String tenantId) throws AtlasBaseException {
        String name = tenantDAO.selectNameById(tenantId);
        Boolean global = isGlobal();
        PageResult<RelationEntityV2> relationsByCategoryGuid;
        if (global) {
            relationsByCategoryGuid = dataManageService.getRelationsByCategoryGuidGlobal(categoryGuid, query, tenantId);
        } else {
            relationsByCategoryGuid = dataManageService.getRelationsByCategoryGuid(categoryGuid, query, tenantId);
        }
        for (RelationEntityV2 list : relationsByCategoryGuid.getLists()) {
            list.setPath(name + "/" + list.getPath());
        }
        return relationsByCategoryGuid;
    }

    /**
     * 获取是否是全局用户
     *
     * @return
     */
    public Boolean isGlobal() {
        User user = AdminUtils.getUserData();
        return userPermissionDAO.selectListByUsersId(user.getUserId()) != null;
    }

    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        PageResult<RelationEntityV2> pageResult;
        if (isGlobal()) {
            pageResult = dataManageService.getRelationsByTableNameGlobal(relationQuery);
        } else {
            pageResult = dataManageService.getRelationsByTableNameGeneral(relationQuery);
        }
        if (CollectionUtils.isEmpty(pageResult.getLists())) {
            return pageResult;
        }
        //获取所有租户
        List<Tenant> tenants = tenantService.getTenants();
        Map<String, String> map = tenants.stream().collect(Collectors.toMap(Tenant::getTenantId, Tenant::getProjectName));
        for (RelationEntityV2 item : pageResult.getLists()) {
            item.setPath(map.get(item.getTenantId()) + "/" + item.getPath());
        }
        return pageResult;
    }

    public PageResult<BusinessInfoHeader> getBusinessObject(String categoryId, String tenantId, Parameters parameters) {
        if (isGlobal()) {
            return businessService.getBusinessListByCategoryIdGlobal(categoryId, parameters, tenantId);
        } else {
            return businessService.getBusinessListByCategoryId(categoryId, parameters, tenantId);
        }
    }

    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters) {
        if (isGlobal()) {
            return businessService.getBusinessListByNameGlobal(parameters);
        } else {
            return businessService.getBusinessListByName(parameters, null);
        }
    }
}
