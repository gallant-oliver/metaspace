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
 * @date 2018/11/19 20:10
 */
package io.zeta.metaspace.web.service;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 20:10
 */

import static io.zeta.metaspace.web.service.DataShareService.METASPACE_MOBIUS_ADDRESS;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.CategoryExport;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.DataOwner;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilegeV2;
import io.zeta.metaspace.model.result.GroupPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.share.APIDataOwner;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.CategoryUtil;
import io.zeta.metaspace.web.util.DateUtils;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.CategoryDeleteReturn;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.CategoryPath;
import org.apache.atlas.model.metadata.MoveCategory;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.model.metadata.SortCategory;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.zeta.metaspace.model.role.SystemRole;
import org.springframework.util.CollectionUtils;

@Service
public class DataManageService {
    private static final Logger LOG = LoggerFactory.getLogger(DataManageService.class);

    @Autowired
    CategoryDAO categoryDao;
    @Autowired
    RelationDAO relationDao;
    @Autowired
    RoleDAO roleDao;
    @Autowired
    RoleService roleService;
    @Autowired
    TableDAO tableDAO;
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    MetaspaceGremlinQueryService metaspaceEntityService;
    @Autowired
    UserDAO userDAO;
    @Autowired
    OrganizationDAO organizationDAO;
    @Autowired
    TableTagDAO tableTagDAO;
    @Autowired
    ColumnDAO columnDAO;
    @Autowired
    MetadataSubscribeDAO metadataSubscribeDAO;
    @Autowired
    UserGroupService userGroupService;
    @Autowired
    UserGroupDAO userGroupDAO;
    @Autowired
    TenantService tenantService;
    @Autowired
    BusinessDAO businessDAO;
    @Autowired
    RelationDAO relationDAO;

    int technicalType = 0;
    int dataStandType = 3;
    int technicalCount = 5;
    int dataStandCount = 14;

    private static final String ORGANIZATION_FIRST_PID = "sso.organization.first.pid";

    /**
     * 获取用户有权限的全部目录
     *
     * @param type
     * @return
     * @throws AtlasBaseException
     */
    //独立部署
    public List<CategoryPrivilege> getAll(int type) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
            List<CategoryPrivilege> valueList = null;
            if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                valueList = roleService.getUserCategory(SystemRole.ADMIN.getCode(), type);
            } else {
                Map<String, CategoryPrivilege> valueMap = new HashMap<>();
                if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                for (Role role : roles) {
                    if (role.getStatus() == 0) {
                        continue;
                    }
                    String roleId = role.getRoleId();
                    for (CategoryPrivilege categoryPrivilege : roleService.getUserCategory(roleId, type)) {
                        if (valueMap.containsKey(categoryPrivilege.getGuid()) && valueMap.get(categoryPrivilege.getGuid()) != null) {
                            valueMap.get(categoryPrivilege.getGuid()).getPrivilege().mergePrivilege(categoryPrivilege.getPrivilege());
                        } else {
                            valueMap.put(categoryPrivilege.getGuid(), categoryPrivilege);
                        }
                    }
                }
                valueList = new ArrayList<>(valueMap.values());
            }
            getCount(valueList, type, TenantService.defaultTenant);
            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    //多租户
    public List<CategoryPrivilege> getAllByUserGroup(int type, String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            List<CategoryPrivilege> valueList = null;
            List<Module> modules = tenantService.getModule(tenantId);
            if (type != 1 && type != 0) {
                valueList = userGroupService.getUserCategory(null, type, modules, tenantId);
            } else {
                //无用户组
                boolean isUserGroup = userGroups == null || userGroups.size() == 0;
                //目录管理权限
                boolean isAdmin = modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId() == module.getModuleId());
                if (isUserGroup && isAdmin) {
                    valueList = userGroupService.getAdminCategory(type, tenantId);
                } else {
                    valueList = userGroupService.getUserCategories(type, tenantId);
                }
            }
            if (modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId() == module.getModuleId())) {
                for (CategoryPrivilege categoryPrivilege : valueList) {
                    categoryPrivilege.getPrivilege().adminPrivilege(categoryPrivilege.getGuid());
                }
            }
            getCount(valueList, type, tenantId);
            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * item累加
     *
     * @param valueList
     * @param type
     */
    private void getCount(List<CategoryPrivilege> valueList, int type, String tenantId) {
        Map<String, Integer> guidAndCount = valueList.stream().collect(Collectors.toMap(CategoryPrivilege::getGuid, CategoryPrivilege::getCount, (key1, key2) -> key2));
        List<CategoryPath> paths = categoryDao.getPath(type, tenantId);
        Map<String, Integer> countMap = new HashMap<>();
        for (CategoryPath path : paths) {
            String guid = path.getGuid();
            Integer count = guidAndCount.get(guid);
            if (count == null || count == 0) {
                continue;
            }
            String[] parentCategory = path.getPath().replace("\"", "").replace("{", "").replace("}", "").split(",");
            for (int i = 0; i < parentCategory.length; i++) {
                if (countMap.containsKey(parentCategory[i])) {
                    countMap.put(parentCategory[i], countMap.get(parentCategory[i]) + count);
                } else {
                    countMap.put(parentCategory[i], count);
                }
            }
        }
        for (CategoryPrivilege categoryPrivilege : valueList) {
            Integer count = countMap.get(categoryPrivilege.getGuid());
            if (count == null) {
                categoryPrivilege.setCount(0);
            } else {
                categoryPrivilege.setCount(count);
            }

        }
    }

    /**
     * 创建业务目录
     *
     * @param info
     * @param type
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryPrivilege createCategory(CategoryInfoV2 info, Integer type, String tenantId) throws Exception {
        try {
            String currentCategoryGuid = info.getGuid();
            boolean authorized = info.isAuthorized();
            CategoryEntityV2 entity = new CategoryEntityV2();
            StringBuffer qualifiedName = new StringBuffer();
            String newCategoryGuid = UUID.randomUUID().toString();
            String name = info.getName();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            User user = AdminUtils.getUserData();
            //判断独立部署和多租户
            List<Module> moduleByUserId = TenantService.defaultTenant.equals(tenantId) ? userDAO.getModuleByUserId(user.getUserId()) : tenantService.getModule(tenantId);
            List<Integer> modules = new ArrayList<>();
            for (Module module : moduleByUserId) {
                modules.add(module.getModuleId());
            }
            //guid
            entity.setGuid(newCategoryGuid);
            //name
            entity.setName(name);
            //createtime
            entity.setCreateTime(io.zeta.metaspace.utils.DateUtils.currentTimestamp());
            //description
            entity.setDescription(info.getDescription());
            entity.setCategoryType(type);
            entity.setSafe(info.getSafe());
            if (StringUtils.isEmpty(entity.getSafe())) {
                entity.setSafe("1");
            }

            //创建一级目录
            if (StringUtils.isEmpty(currentCategoryGuid)) {
                if (TenantService.defaultTenant.equals(tenantId)) {
                    List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                    if (!roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                        throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有创建目录权限");
                    }

                } else {
                    boolean bool = type == 1 || type == 0;
                    if (!modules.contains(ModuleEnum.AUTHORIZATION.getId()) && bool) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有目录授权模块权限，无法创建一级目录");
                    }
                }
                CategoryPrivilege oneLevelCategory = null;
                if (categoryDao.ifExistCategory(type, tenantId) > 0) {
                    oneLevelCategory = createOneLevelCategory(entity, type, tenantId);
                } else {
                    oneLevelCategory = createFirstCategory(entity, type, tenantId);
                }
                if (authorized) {
                    CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, false, true, true, true, true, true, true, false);
                    oneLevelCategory.setPrivilege(privilege);
                }
                return oneLevelCategory;
            }
            if (Objects.isNull(categoryDao.queryByGuidV2(currentCategoryGuid, tenantId))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录已被删除，请刷新后重新操作");
            }
            CategoryPrivilege.Privilege privilege = createOtherCategory(entity, type, info, tenantId);
            categoryDao.add(entity, tenantId);
            CategoryPrivilege returnEntity = categoryDao.queryByGuidV2(newCategoryGuid, tenantId);
            //有目录权限管理模块权限，可以随意建目录
            if (authorized) {
                privilege = new CategoryPrivilege.Privilege(false, false, false, true, true, true, true, true, true, false);
            } else {
                boolean isAdmin = modules.contains(ModuleEnum.AUTHORIZATION.getId());
                //无当前目录权限
                boolean isPrivilege = !userGroupService.isPrivilegeCategory(user.getUserId(), newCategoryGuid, tenantId, type);
                boolean typeBoolean = type == 1 || type == 0;
                if (isAdmin) {
                    privilege.adminPrivilege(returnEntity.getGuid());
                }
                if (typeBoolean && isAdmin && isPrivilege) {
                    privilege.setAsh(true);
                }
            }
            returnEntity.setPrivilege(privilege);
            return returnEntity;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("创建业务目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "创建业务目录失败");
        }
    }

    private CategoryPrivilege createOneLevelCategory(CategoryEntityV2 entity, int type, String tenantId) throws AtlasBaseException {
        int count = categoryDao.querySameNameOne(entity.getName(), type, tenantId);
        if (count > 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
        StringBuffer qualifiedName = new StringBuffer();
        String lastCategoryId = categoryDao.queryLastCategory(type, tenantId);
        qualifiedName.append(entity.getName());
        entity.setQualifiedName(qualifiedName.toString());
        entity.setLevel(1);
        entity.setUpBrotherCategoryGuid(lastCategoryId);
        categoryDao.add(entity, tenantId);
        categoryDao.updateDownBrotherCategoryGuid(lastCategoryId, entity.getGuid(), tenantId);
        CategoryPrivilege returnEntity = new CategoryPrivilege();
        returnEntity.setGuid(entity.getGuid());
        returnEntity.setName(entity.getName());
        returnEntity.setDescription(entity.getDescription());
        returnEntity.setLevel(1);
        returnEntity.setParentCategoryGuid(null);
        returnEntity.setUpBrotherCategoryGuid(lastCategoryId);
        returnEntity.setDownBrotherCategoryGuid(null);
        CategoryPrivilege.Privilege privilege = null;
        if (type == 0 || type == 1) {
            privilege = new CategoryPrivilege.Privilege(false, true, true, true, true, true, true, true, true, false);
        } else {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        if (type == technicalType) {
            privilege.setDeleteRelation(false);
        }
        returnEntity.setPrivilege(privilege);
        return returnEntity;
    }

    private CategoryPrivilege createFirstCategory(CategoryEntityV2 entity, int type, String tenantId) throws AtlasBaseException {
        StringBuffer qualifiedName = new StringBuffer();
        //qualifiedName
        qualifiedName.append(entity.getName());
        entity.setQualifiedName(qualifiedName.toString());
        entity.setLevel(1);
        categoryDao.add(entity, tenantId);
        CategoryPrivilege returnEntity = new CategoryPrivilege();
        returnEntity.setGuid(entity.getGuid());
        returnEntity.setName(entity.getName());
        returnEntity.setDescription(entity.getDescription());
        returnEntity.setLevel(1);
        returnEntity.setParentCategoryGuid(null);
        returnEntity.setUpBrotherCategoryGuid(null);
        returnEntity.setDownBrotherCategoryGuid(null);
        CategoryPrivilege.Privilege privilege = null;
        if (type == 0 || type == 1) {
            privilege = new CategoryPrivilege.Privilege(false, true, true, true, true, true, true, true, true, false);
        } else {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        if (type == 1) {
            privilege.setAsh(true);
        }
        if (type == technicalType) {
            privilege.setDeleteRelation(false);
        }
        returnEntity.setPrivilege(privilege);
        return returnEntity;
    }

    private CategoryPrivilege.Privilege createOtherCategory(CategoryEntityV2 entity, int type, CategoryInfoV2 info, String tenantId) throws SQLException, AtlasBaseException {
        StringBuffer qualifiedName = new StringBuffer();
        String newCategoryGuid = entity.getGuid();
        String newCategoryParentGuid = info.getParentCategoryGuid();
        //获取当前catalog
        CategoryEntityV2 currentEntity = categoryDao.queryByGuid(info.getGuid(), tenantId);
        String parentQualifiedName = null;
        String parentGuid = null;
        int currentLevel = categoryDao.getCategoryLevel(info.getGuid(), tenantId);
        //创建子目录
        if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
            parentGuid = info.getGuid();
            entity.setParentCategoryGuid(info.getGuid());
            parentQualifiedName = currentEntity.getQualifiedName();
            entity.setLevel(currentLevel + 1);
        } else {
            //创建同级目录
            parentGuid = currentEntity.getParentCategoryGuid();
            entity.setLevel(currentLevel);
            if (StringUtils.isNotEmpty(parentGuid)) {
                entity.setParentCategoryGuid(parentGuid);
                CategoryEntityV2 currentCatalogParentEntity = categoryDao.queryByGuid(parentGuid, tenantId);
                parentQualifiedName = currentCatalogParentEntity.getQualifiedName();
            }
        }
        if (StringUtils.isNotEmpty(parentQualifiedName) && parentQualifiedName.length() > 0)
            qualifiedName.append(parentQualifiedName + ".");
        qualifiedName.append(entity.getName());
        int count = categoryDao.querySameNameNum(entity.getName(), parentGuid, type, tenantId);
        if (count > 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
        //qualifiedName
        entity.setQualifiedName(qualifiedName.toString());

        //子目录
        if (StringUtils.isNotEmpty(newCategoryParentGuid)) {
            String lastChildGuid = categoryDao.queryLastChildCategory(info.getGuid(), tenantId);
            if (StringUtils.isNotEmpty(lastChildGuid)) {
                entity.setUpBrotherCategoryGuid(lastChildGuid);
                categoryDao.updateDownBrotherCategoryGuid(lastChildGuid, newCategoryGuid, tenantId);
            }
        } else {
            //同级目录
            String up = "up";
            String down = "down";
            if (StringUtils.isNotEmpty(info.getGuid()) && Strings.equals(info.getDirection(), up)) {
                entity.setDownBrotherCategoryGuid(info.getGuid());
                String upBrotherGuid = currentEntity.getUpBrotherCategoryGuid();
                if (StringUtils.isNotEmpty(upBrotherGuid)) {
                    entity.setUpBrotherCategoryGuid(upBrotherGuid);
                    categoryDao.updateDownBrotherCategoryGuid(upBrotherGuid, newCategoryGuid, tenantId);
                }
                categoryDao.updateUpBrotherCategoryGuid(info.getGuid(), newCategoryGuid, tenantId);
            } else if (StringUtils.isNotEmpty(info.getGuid()) && Strings.equals(info.getDirection(), down)) {
                entity.setUpBrotherCategoryGuid(info.getGuid());
                String downBrotherGuid = currentEntity.getDownBrotherCategoryGuid();
                if (StringUtils.isNotEmpty(downBrotherGuid)) {
                    entity.setDownBrotherCategoryGuid(downBrotherGuid);
                    categoryDao.updateUpBrotherCategoryGuid(downBrotherGuid, newCategoryGuid, tenantId);
                }
                categoryDao.updateDownBrotherCategoryGuid(info.getGuid(), newCategoryGuid, tenantId);
            }
        }
        if (type == 3 || type == 4) {
            return new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, true);
        }
        if (parentGuid == null) {
            return new CategoryPrivilege.Privilege(false, true, true, true, false, true, false, false, true, false);
        }
        List<GroupPrivilege> parentPrivilege = userGroupDAO.getCategoryGroupPrivileges(entity.getParentCategoryGuid(), tenantId);
        parentPrivilege.forEach(privilege -> privilege.setCategoryId(entity.getGuid()));
        if (parentPrivilege.size() != 0) {
            userGroupDAO.addUserGroupPrivileges(parentPrivilege);
        }
        List<CategoryPrivilege> userCategories = userGroupService.getUserCategories(type, tenantId);
        for (CategoryPrivilege categoryPrivilege : userCategories) {
            if (categoryPrivilege.getGuid().equals(parentGuid)) {
                return categoryPrivilege.getPrivilege();
            }
        }
        return null;
    }

    /**
     * 获取目录信息
     *
     * @param guid
     * @return
     * @throws SQLException
     */
    public CategoryEntityV2 getCategory(String guid, String tenantId) throws SQLException {
        return categoryDao.queryByGuid(guid, tenantId);
    }

    /**
     * 删除目录
     *
     * @param guid
     * @return
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryDeleteReturn deleteCategory(String guid, String tenantId, int type) throws Exception {
        List<String> categoryIds = categoryDao.queryChildrenCategoryId(guid, tenantId);
        categoryIds.add(guid);
        int item = 0;
        if (type == 0) {
            item = relationDao.updateRelationByCategoryGuid(categoryIds, "1");
        } else if (type == 1) {
            List<String> businessIds = relationDao.getBusinessIdsByCategoryGuid(categoryIds);
            if (businessIds == null || businessIds.size() == 0) {
                item = 0;
            } else {
                item = businessDAO.deleteBusinessesByIds(businessIds);
                businessDAO.deleteRelationByBusinessIds(businessIds);
                businessDAO.deleteRelationByIds(businessIds);
            }
        }
        CategoryEntityV2 currentCatalog = categoryDao.queryByGuid(guid, tenantId);
        if (Objects.isNull(currentCatalog)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取当前目录信息失败");
        }
        String upBrotherCategoryGuid = currentCatalog.getUpBrotherCategoryGuid();
        String downBrotherCategoryGuid = currentCatalog.getDownBrotherCategoryGuid();
        if (StringUtils.isNotEmpty(upBrotherCategoryGuid)) {
            categoryDao.updateDownBrotherCategoryGuid(upBrotherCategoryGuid, downBrotherCategoryGuid, tenantId);
        }
        if (StringUtils.isNotEmpty(downBrotherCategoryGuid)) {
            categoryDao.updateUpBrotherCategoryGuid(downBrotherCategoryGuid, upBrotherCategoryGuid, tenantId);
        }
        if (TenantService.defaultTenant.equals(tenantId)) {
            User user = AdminUtils.getUserData();
            List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
            if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            }
            roleDao.deleteRole2categoryByUserId(guid);
        } else {
            userGroupDAO.deleteCategoryGroupRelationByCategory(guid);
        }
        int category = categoryDao.deleteCategoryByIds(categoryIds, tenantId);
        CategoryDeleteReturn deleteReturn = new CategoryDeleteReturn();
        deleteReturn.setCategory(category);
        deleteReturn.setItem(item);
        return deleteReturn;
    }

    /**
     * 更新目录
     *
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public String updateCategory(CategoryInfoV2 info, int type, String tenantId) throws AtlasBaseException {
        try {
            String guid = info.getGuid();
            String name = info.getName();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            CategoryEntityV2 currentEntity = categoryDao.queryByGuid(guid, tenantId);
            String parentQualifiedName = null;
            StringBuffer qualifiedName = new StringBuffer();
            if (StringUtils.isNotEmpty(currentEntity.getParentCategoryGuid()))
                parentQualifiedName = categoryDao.queryQualifiedName(currentEntity.getParentCategoryGuid(), tenantId);
            if (StringUtils.isNotEmpty(parentQualifiedName))
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);
            int count = categoryDao.querySameNameNum(name, currentEntity.getParentCategoryGuid(), type, tenantId);
            if (count > 0 && !currentEntity.getName().equals(info.getName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
            }
            CategoryEntity entity = new CategoryEntity();
            entity.setGuid(info.getGuid());
            entity.setName(info.getName());
            entity.setQualifiedName(qualifiedName.toString());
            entity.setDescription(info.getDescription());
            entity.setSafe(info.getSafe());
            categoryDao.updateCategoryInfo(entity, tenantId);
            return "success";
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("更新目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新目录失败");
        }
    }

    /**
     * 添加关联
     *
     * @param categoryGuid
     * @param ids
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignTablesToCategory(String categoryGuid, List<String> ids) throws AtlasBaseException {
        try {
            if (ids == null || ids.size() == 0) {
                return;
            }
            relationDao.updateByTableGuids(ids, categoryGuid, DateUtils.getNow());
        } catch (Exception e) {
            LOG.error("添加关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加关联失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int addRelation(RelationEntityV2 relationEntity) throws AtlasBaseException {
        try {
            String relationshiGuid = UUID.randomUUID().toString();
            relationEntity.setRelationshipGuid(relationshiGuid);
            return relationDao.add(relationEntity);
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }


    //1.4删除表关联，取消子目录表关联关系时数据表自动回到一级目录
    @Transactional(rollbackFor = Exception.class)
    public void removeRelationAssignmentFromTablesV2(List<RelationEntityV2> relationshipList, String tenantId) throws AtlasBaseException {
        try {
            if (Objects.nonNull(relationshipList)) {
                for (RelationEntityV2 relationship : relationshipList) {
                    String relationshipGuid = relationship.getRelationshipGuid();
                    RelationEntityV2 relationInfo = relationDao.getRelationInfoByGuid(relationshipGuid);
                    relationDao.delete(relationInfo.getRelationshipGuid());
                    String topGuid = relationDao.getTopGuidByGuid(relationInfo.getCategoryGuid(), tenantId);
                    //当贴源层没有关联该表时
                    if (relationDao.ifRelationExists(topGuid, relationInfo.getTableGuid()) == 0) {
                        TableRelation tableRelation = new TableRelation();
                        tableRelation.setRelationshipGuid(UUID.randomUUID().toString());
                        tableRelation.setCategoryGuid(topGuid);
                        tableRelation.setTableGuid(relationInfo.getTableGuid());
                        tableRelation.setGenerateTime(DateUtils.getNow());
                        relationDao.addRelation(tableRelation);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("取消关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消关联失败");
        }
    }

    /**
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuid(String categoryGuid, RelationQuery query, String tenantId) throws AtlasBaseException {
        try {
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = new ArrayList<>();
            int totalNum = 0;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                relations = relationDao.queryRelationByCategoryGuid(categoryGuid, limit, offset);
            } else {
                User user = AdminUtils.getUserData();
                List<String> databases = tenantService.getDatabase(tenantId);
                if (databases != null && databases.size() != 0)
                    relations = relationDao.queryRelationByCategoryGuidV2(categoryGuid, limit, offset, databases,tenantId);
            }
            for (RelationEntityV2 entity : relations) {
                String tableGuid = entity.getTableGuid();
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(tableGuid,tenantId);
                List<String> tableTagNameList = tableTageList.stream().map(tag -> tag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
            }
            if (relations.size() != 0) {
                totalNum = relations.get(0).getTotal();
            }
            getPath(relations, tenantId);
            pageResult.setCurrentSize(relations.size());
            pageResult.setLists(relations);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        }
    }

    /**
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuidFilter(String categoryGuid, RelationQuery query, String tenantId) throws AtlasBaseException {
        try {

            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = new ArrayList<>();
            int totalNum = 0;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                relations = relationDao.queryRelationByCategoryGuidFilter(categoryGuid, limit, offset);
            } else {
                User user = AdminUtils.getUserData();
                List<String> databases = tenantService.getDatabase(tenantId);
                if (databases != null && databases.size() != 0)
                    relations = relationDao.queryRelationByCategoryGuidFilterV2(categoryGuid,tenantId, limit, offset, databases);
            }
            if (relations.size() != 0) {
                totalNum = relations.get(0).getTotal();
            }
            getPath(relations, tenantId);
            pageResult.setCurrentSize(relations.size());
            pageResult.setLists(relations);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        }
    }

    public PageResult<RelationEntityV2> getRelationsByTableName(RelationQuery query, int type, String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<String> categoryIds = new ArrayList<>();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            int limit = query.getLimit();
            int offset = query.getOffset();
            List<RelationEntityV2> list = new ArrayList<>();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if (StringUtils.isNotEmpty(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            if (StringUtils.isNotEmpty(tag))
                tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                    categoryIds = CategoryRelationUtils.getPermissionCategoryList(SystemRole.ADMIN.getCode(), type);
                } else {
                    for (Role role : roles) {
                        if (role.getStatus() == 0) {
                            continue;
                        }
                        String roleId = role.getRoleId();
                        List<String> category = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
                        for (String categoryId : category) {
                            if (!categoryIds.contains(categoryId)) {
                                categoryIds.add(categoryId);
                            }
                        }
                    }
                }
                list = categoryIds.size() != 0 ? new ArrayList<>() : relationDao.queryByTableName(tableName, tag, categoryIds, limit, offset);
            } else {
                List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
                for (UserGroup userGroup : userGroups) {
                    String userGroupId = userGroup.getId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryListV2(userGroupId, type, tenantId);
                    for (String categoryId : category) {
                        if (!categoryIds.contains(categoryId)) {
                            categoryIds.add(categoryId);
                        }
                    }
                }
                List<String> databases = tenantService.getDatabase(tenantId);

                if (databases != null && databases.size() != 0 && categoryIds.size() != 0)
                    list = relationDao.queryByTableNameV2(tableName, tag, categoryIds, limit, offset, databases,tenantId);
            }
            //tag
            list.forEach(entity -> {
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(entity.getTableGuid(),tenantId);
                List<String> tableTagNameList = tableTageList.stream().map(tableTag -> tableTag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
            });

            //path
            getPath(list, tenantId);
            //dataOwner
            for (RelationEntityV2 entity : list) {
                String tableGuid = entity.getTableGuid();
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
            }
            long totalNum = 0;
            if (list.size() != 0) {
                totalNum = list.get(0).getTotal();
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("搜索关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "搜索关联表失败");
        }
    }

    public PageResult<RelationEntityV2> getRelationsByTableNameFilter(RelationQuery query, int type, String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<String> categoryIds = new ArrayList<>();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if (StringUtils.isNotEmpty(tableName))
                tableName = tableName.replaceAll("%", "/%").replaceAll("_", "/_");
            if (StringUtils.isNotEmpty(tag))
                tag = tag.replaceAll("%", "/%").replaceAll("_", "/_");
            List<RelationEntityV2> list = new ArrayList<>();

            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                List<Role> roles = roleDao.getRoleByUsersId(user.getUserId());
                if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
                }
                if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                    categoryIds = CategoryRelationUtils.getPermissionCategoryList(SystemRole.ADMIN.getCode(), type);
                } else {
                    for (Role role : roles) {
                        if (role.getStatus() == 0) {
                            continue;
                        }
                        String roleId = role.getRoleId();
                        List<String> category = CategoryRelationUtils.getPermissionCategoryList(roleId, type);
                        for (String categoryId : category) {
                            if (!categoryIds.contains(categoryId)) {
                                categoryIds.add(categoryId);
                            }
                        }
                    }
                }
                list = categoryIds.size() != 0 ? new ArrayList<>() : relationDao.queryByTableNameFilter(tableName, tag, categoryIds, limit, offset);
            } else {
                List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
                for (UserGroup userGroup : userGroups) {
                    String userGroupId = userGroup.getId();
                    List<String> category = CategoryRelationUtils.getPermissionCategoryListV2(userGroupId, type, tenantId);
                    for (String categoryId : category) {
                        if (!categoryIds.contains(categoryId)) {
                            categoryIds.add(categoryId);
                        }
                    }
                }
                List<String> databases = tenantService.getDatabase(tenantId);
                if (databases != null && databases.size() != 0 && categoryIds.size() != 0)
                    list = relationDao.queryByTableNameFilterV2(tableName, tag, categoryIds, limit, offset, databases);
            }

            getPath(list, tenantId);
            long totalNum = 0;
            if (list.size() != 0) {
                totalNum = list.get(0).getTotal();
            }
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            pageResult.setTotalSize(totalNum);
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联表失败");
        }
    }

    public void getPath(List<RelationEntityV2> list, String tenantId) throws AtlasBaseException {
        for (RelationEntityV2 entity : list) {
            String path = CategoryRelationUtils.getPath(entity.getCategoryGuid(), tenantId);
            entity.setPath(path);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(List<AtlasEntity> entities) {
        List<String> tableStatus=new ArrayList<>();
        List<String> databaseStatus=new ArrayList<>();
        List<String> columnStatus=new ArrayList<>();
        for (AtlasEntity entity : entities) {
            String guid = entity.getGuid();
            String typeName = entity.getTypeName();
            if (typeName.contains("table")) {
                tableStatus.add(guid);
            }
            if (typeName.contains("hive_db") || typeName.contains("rdbms_table")) {
                databaseStatus.add(guid);
            }
            if (typeName.contains("hive_column") || typeName.contains("rdbms_column")) {
                columnStatus.add(guid);
            }
        }

        if(!CollectionUtils.isEmpty(tableStatus)){
            String tableStatusStr=StringUtils.join(tableStatus,",");
            relationDao.updateTableStatusBatch(tableStatusStr, "DELETED");
        }
        if(!CollectionUtils.isEmpty(databaseStatus)){
            String databaseStatusStr=StringUtils.join(databaseStatus,",");
            relationDao.updateDatabaseStatusBatch(databaseStatusStr, "DELETED");
        }
        if(!CollectionUtils.isEmpty(columnStatus)){
            String columnStatusStr=StringUtils.join(columnStatus,",");
            columnDAO.updateColumnStatusBatch(columnStatusStr, "DELETED");
        }
    }

    public Set<CategoryEntityV2> getAllDepartments(int type, String tenantId) throws AtlasBaseException {
        try {
            return categoryDao.getAllDepartments(type, tenantId);
        } catch (SQLException e) {
            LOG.error("获取数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int addTableOwner(TableOwner tableOwner, String tenantId) throws AtlasBaseException {
        try {
            List<String> tableList = tableOwner.getTables();
            //api
            List<APIInfoHeader> apiList = shareDAO.getAPIByRelatedTable(tableList, tenantId);
            List<TableOwner.Owner> tableOwners = tableOwner.getOwners();
            if (Objects.nonNull(apiList) && apiList.size() > 0) {
                sendToMobius(apiList, tableOwners);
            }
            //删除旧关系
            categoryDao.deleteDataOwner(tableOwner.getTables());
            //修改人
            String keeper = AdminUtils.getUserData().getUserId();
            //修改时间
            String generateTIme = DateUtils.getNow();
            //列表
            List<DataOwner> table2OwnerList = new ArrayList<>();
            for (String tableGuid : tableList) {
                for (TableOwner.Owner owner : tableOwners) {
                    DataOwner dOnwer = new DataOwner();
                    dOnwer.setTableGuid(tableGuid);
                    dOnwer.setOwnerId(owner.getId());
                    dOnwer.setPkId(owner.getPkid());
                    dOnwer.setKeeper(keeper);
                    dOnwer.setGenerateTime(generateTIme);
                    table2OwnerList.add(dOnwer);
                }
            }
            return categoryDao.addDataOwner(table2OwnerList);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("添加组织架构失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加组织架构失败");
        }
    }

    public void sendToMobius(List<APIInfoHeader> apiList, List<TableOwner.Owner> tableOwners) throws AtlasException, AtlasBaseException {
        Configuration configuration = ApplicationProperties.get();
        String mobiusURL = configuration.getString(METASPACE_MOBIUS_ADDRESS) + "/reviews/user";
        //organization
        List<APIDataOwner.Organization> organizations = new ArrayList<>();
        for (TableOwner.Owner owner : tableOwners) {
            APIDataOwner.Organization organization = new APIDataOwner.Organization();
            organization.setOrganization(owner.getPkid());
            organization.setOrganization_type(owner.getType());
            organizations.add(organization);
        }

        Gson gson = new Gson();
        //向云平台发请求
        for (int i = 0, len = apiList.size(); i < len; i++) {
            String apiGuid = apiList.get(i).getGuid();
            String manager = apiList.get(i).getManager();
            List<String> apiGuidList = new ArrayList<>();
            List<String> ownerList = new ArrayList<>();
            ownerList.add(manager);
            apiGuidList.add(apiGuid);
            APIDataOwner dataOwner = new APIDataOwner();
            dataOwner.setApi_id_list(apiGuidList);
            dataOwner.setOrganization_list(organizations);
            dataOwner.setApi_owner(ownerList);
            String jsonStr = gson.toJson(dataOwner, APIDataOwner.class);

            int retryCount = 0;
            String errorId = null;
            String errorReason = null;
            int retries = 3;
            while (retryCount < retries) {
                String res = OKHttpClient.doPut(mobiusURL, jsonStr);
                LOG.info(res);
                if (StringUtils.isNotEmpty(res)) {
                    Map response = gson.fromJson(res, Map.class);
                    errorId = String.valueOf(response.get("error-id"));
                    errorReason = String.valueOf(response.get("reason"));
                    if ("0.0".equals(errorId)) {
                        break;
                    } else {
                        retryCount++;
                    }
                } else {
                    retryCount++;
                }
            }
            if (!"0.0".equals(errorId)) {
                StringBuffer detail = new StringBuffer();
                detail.append("云平台返回错误码:");
                detail.append(errorId);
                detail.append("错误信息:");
                detail.append(errorReason);
                throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "云平台修改表owner失败");
            }
        }


    }

    public PageResult<Organization> getOrganizationByPid(String pId, Parameters parameters) throws AtlasBaseException {
        try {
            String zero = "0";
            if (zero.equals(pId)) {
                pId = ApplicationProperties.get().getString("sso.organization.first.pid");
            }
            String query = parameters.getQuery();
            if (StringUtils.isNotEmpty(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<Organization> list = organizationDAO.getOrganizationByPid(pId, query, limit, offset);
            Configuration configuration = ApplicationProperties.get();
            String firstPid = configuration.getString(ORGANIZATION_FIRST_PID);
            for (Organization organization : list) {
                String pathStr = organizationDAO.getPathById(firstPid, organization.getId());
                String path = pathStr.replace(",", ".").replace("\"", "").replace("{", "").replace("}", "");
                organization.setPath(path);
            }
            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = list.get(0).getTotal();
            }
            long count = list.size();
            PageResult pageResult = new PageResult();
            pageResult.setLists(list);
            pageResult.setCurrentSize(count);
            pageResult.setTotalSize(totalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public PageResult<Organization> getOrganizationByName(Parameters parameters) throws AtlasBaseException {
        try {
            String query = parameters.getQuery();
            if (StringUtils.isNotEmpty(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            Integer limit = parameters.getLimit();
            Integer offset = parameters.getOffset();
            List<Organization> list = organizationDAO.getOrganizationByName(query, limit, offset);
            Configuration configuration = ApplicationProperties.get();
            String firstPid = configuration.getString(ORGANIZATION_FIRST_PID);
            for (Organization organization : list) {
                String pathStr = organizationDAO.getPathById(firstPid, organization.getId());
                if (pathStr == null) {
                    pathStr = "";
                }
                String path = pathStr.replace(",", ".").replace("\"", "").replace("{", "").replace("}", "");
                organization.setPath(path);
            }
            long totalSize = 0;
            if (list.size() != 0) {
                totalSize = list.get(0).getTotal();
            }
            long count = list.size();
            PageResult pageResult = new PageResult();

            pageResult.setLists(list);
            pageResult.setCurrentSize(count);
            pageResult.setTotalSize(totalSize);
            return pageResult;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public List getOrganization() throws AtlasBaseException {
        String organizationURL = SSOConfig.getOrganizationURL();
        String organizationCountURL = SSOConfig.getOrganizationCountURL();
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String endTime = sdf.format(currentTime);

        HashMap<String, String> header = new HashMap<>();
        Gson gson = new Gson();
        Map<String, String> queryCountParamMap = new HashMap<>();
        queryCountParamMap.put("endTime", endTime);
        List data = new ArrayList();
        int retryCount = 0;
        int retries = 3;
        String proper = "0.0";
        String errorCode = null;
        String message = null;
        putHeader(header);
        while (retryCount < retries) {
            String countSession = OKHttpClient.doGet(organizationCountURL, queryCountParamMap, header);
            if (Objects.isNull(countSession)) {
                retryCount++;
                continue;
            }
            Map countBody = gson.fromJson(countSession, Map.class);
            Map countData = (Map) countBody.get("data");
            errorCode = Objects.toString(countBody.get("errorCode"));
            if (!proper.equals(errorCode)) {
                message = Objects.toString(countBody.get("message"));
                retryCount++;
                continue;
            }
            double count = (Double) countData.get("count");

            Map<String, String> queryDataParamMap = new HashMap<>();
            queryDataParamMap.put("currentPage", "0");
            queryDataParamMap.put("pageSize", String.valueOf((int) count));
            queryDataParamMap.put("endTime", endTime);
            String session = OKHttpClient.doGet(organizationURL, queryDataParamMap, header);
            if (Objects.isNull(session)) {
                retryCount++;
                continue;
            }
            Map body = gson.fromJson(session, Map.class);
            errorCode = Objects.toString(body.get("errorCode"));
            if (!proper.equals(errorCode)) {
                message = Objects.toString(body.get("message"));
                retryCount++;
                continue;
            }
            data = (List) body.get("data");
            return data;
        }
        StringBuffer detail = new StringBuffer();
        detail.append("sso返回错误码:");
        detail.append(errorCode);
        detail.append("错误信息:");
        detail.append(message);
        throw new AtlasBaseException(detail.toString(), AtlasErrorCode.BAD_REQUEST, "sso获取组织架构失败");
    }

    public void putHeader(Map<String, String> header) throws AtlasBaseException {
        Configuration configuration;
        try {
            configuration = ApplicationProperties.get();
        } catch (AtlasException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取配置失败");
        }

        boolean isEncryption = configuration.getBoolean("sso.encryption", false);
        if (isEncryption) {
            String puk = configuration.getString("sso.encryption.public.key");
            String prk = configuration.getString("sso.encryption.private.key");
            StringBuffer buffer = new StringBuffer();
            //私钥
            buffer.append("&key=");
            buffer.append(prk);

            //时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String date = sdf.format(System.currentTimeMillis());
            buffer.append("&time=");
            buffer.append(date);

            //随机字符串
            buffer.append("&nonce_str=");
            String randomString = RandomStringUtils.randomAlphanumeric(10);
            buffer.append(randomString);

            //md5加密并转换成大写
            String str = DigestUtils.md5Hex(buffer.toString()).toUpperCase();
            String authentication = date + "_" + puk + "_" + str;
            header.put("authentication", authentication);
            header.put("nonce_str", randomString);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrganization() throws AtlasBaseException {
        try {
            organizationDAO.deleteOrganization();
            List<Map> data = getOrganization();
            List<Organization> list = toOrganization(data);
            int size = list.size();
            int startIndex = 0;
            int endIndex = startIndex + 200;
            List insertList = null;
            while (endIndex < size) {
                insertList = list.subList(startIndex, endIndex);
                if (Objects.nonNull(insertList) && insertList.size() > 0) {
                    organizationDAO.addOrganizations(insertList);
                }
                startIndex = endIndex;
                endIndex += 200;
            }
            insertList = list.subList(startIndex, size);
            if (Objects.nonNull(insertList) && insertList.size() > 0) {
                organizationDAO.addOrganizations(insertList);
            }
        } catch (AtlasBaseException e) {
            LOG.error("更新组织架构失败", e);
            throw e;
        } catch (Exception e) {
            LOG.error("更新组织架构失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新组织架构失败");
        }
    }

    public List<Organization> toOrganization(List<Map> dataList) {
        List<Organization> list = new ArrayList<>();
        for (Map data : dataList) {
            Organization organization = parseMap2Object(data, Organization.class);
            list.add(organization);
        }
        return list;
    }

    public static <T> T parseMap2Object(Map paramMap, Class<T> cls) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJson(paramMap), cls);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addEntity(List<AtlasEntity> entities) {
        List<Column> columnList = new ArrayList<>();
        try {
            //添加到tableinfo
            for (AtlasEntity entity : entities) {
                String typeName = entity.getTypeName();
                if (("hive_table").equals(typeName)) {
                    if (entity.getAttribute("temporary") == null || entity.getAttribute("temporary").toString().equals("false")) {
                        String guid = entity.getGuid();
                        String name = getEntityAttribute(entity, "name");
                        if (tableDAO.ifTableExists(guid) == 0) {
                            TableInfo tableInfo = new TableInfo();
                            tableInfo.setTableGuid(guid);
                            tableInfo.setTableName(name);
                            Object createTime = entity.getAttribute("createTime");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formatDateStr = sdf.format(createTime);
                            tableInfo.setCreateTime(formatDateStr);
                            tableInfo.setStatus(entity.getStatus().name());
                            AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                            tableInfo.setDatabaseGuid(relatedDB.getGuid());
                            tableInfo.setDbName(relatedDB.getDisplayText());
                            tableInfo.setDatabaseStatus(relatedDB.getEntityStatus().name());
                            tableInfo.setDescription(getEntityAttribute(entity, "comment"));
                            tableInfo.setSourceId("hive");
                            tableDAO.addTable(tableInfo);
                        }
                    }
                } else if (("rdbms_table").equals(typeName)) {
                    String guid = entity.getGuid();
                    String name = getEntityAttribute(entity, "name");
                    if (tableDAO.ifTableExists(guid) == 0) {
                        TableInfo tableInfo = new TableInfo();
                        tableInfo.setTableGuid(guid);
                        tableInfo.setTableName(name);
                        Object createTime = entity.getAttribute("createTime");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formatDateStr = sdf.format(createTime);
                        tableInfo.setCreateTime(formatDateStr);
                        tableInfo.setStatus(entity.getStatus().name());
                        AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                        tableInfo.setDatabaseGuid(relatedDB.getGuid());
                        tableInfo.setDbName(relatedDB.getDisplayText());
                        tableInfo.setDatabaseStatus(relatedDB.getEntityStatus().name());
                        tableInfo.setDescription(getEntityAttribute(entity, "comment"));

                        String qualifiedName = String.valueOf(entity.getAttribute("qualifiedName"));
                        tableInfo.setSourceId(qualifiedName.split("\\.")[0]);

                        tableDAO.addTable(tableInfo);
                    }
                } else if (("hive_column").equals(typeName)) {
                    AtlasRelatedObjectId table = (AtlasRelatedObjectId) entity.getRelationshipAttribute("table");
                    String tableGuid = table.getGuid();
                    String guid = entity.getGuid();
                    String name = entity.getAttribute("name").toString();
                    String type = entity.getAttribute("type").toString();
                    Object comment = entity.getAttribute("comment");
                    String status = entity.getStatus().name();
                    String updateTime = DateUtils.date2String(entity.getUpdateTime());
                    Column column = new Column();
                    column.setTableId(tableGuid);
                    column.setColumnId(guid);
                    column.setColumnName(name);
                    column.setType(type);
                    column.setStatus(status);
                    column.setDisplayNameUpdateTime(updateTime);
                    if (comment != null) {
                        column.setDescription(column.toString());
                    }

                    columnList.add(column);

                } else if (("rdbms_column").equals(typeName)) {
                    AtlasRelatedObjectId table = (AtlasRelatedObjectId) entity.getRelationshipAttribute("table");
                    String tableGuid = table.getGuid();
                    String guid = entity.getGuid();
                    String name = entity.getAttribute("name").toString();
                    String type = entity.getAttribute("data_type").toString();
                    Object comment = entity.getAttribute("comment");
                    String status = entity.getStatus().name();
                    String updateTime = DateUtils.date2String(entity.getUpdateTime());
                    Column column = new Column();
                    column.setTableId(tableGuid);
                    column.setColumnId(guid);
                    column.setColumnName(name);
                    column.setType(type);
                    column.setStatus(status);
                    column.setDisplayNameUpdateTime(updateTime);
                    if (comment != null) {
                        column.setDescription(column.toString());
                    }

                    columnList.add(column);

                }
            }
            if (columnList.size() > 0) {
                columnDAO.addColumnDisplayInfo(columnList);
            }
            addFullRelation();
        } catch (Exception e) {
            LOG.error("添加entity失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFullRelation() {
        //添加关联
        List<String> newTable = tableDAO.getNewTable();
        List<TableRelation> tableRelations = new ArrayList<>();
        for (String s : newTable) {
            TableRelation tableRelation = new TableRelation();
            tableRelation.setCategoryGuid("1");
            tableRelation.setGenerateTime(DateUtils.getNow());
            tableRelation.setRelationshipGuid(UUID.randomUUID().toString());
            tableRelation.setTableGuid(s);
            tableRelations.add(tableRelation);
        }
        if (tableRelations.size() > 0)
            tableDAO.addRelations(tableRelations);
    }

    @Transactional(rollbackFor = Exception.class)
    public void supplementTable(String tenantId) throws AtlasBaseException {
        try {
            PageResult<Table> tableNameAndDbNameByQuery;
            List<String> tableByDB;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", false, 0, -1);
                tableByDB = tableDAO.getTables();
            } else {
                List<String> dbs = tenantService.getDatabase(tenantId);
                String dbsToString = dbsToString(dbs);
                tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", false, 0, -1, dbsToString);
                tableByDB = tableDAO.getTableByDB(dbs);
            }
            List<Table> lists = tableNameAndDbNameByQuery.getLists();
            List<String> tableIds = lists.stream().map(table -> table.getTableId()).collect(Collectors.toList());
            List<String> deleteIds = new ArrayList<>();
            for (String table : tableByDB) {
                if (!tableIds.contains(table)) {
                    deleteIds.add(table);
                }
            }
            for (Table list : lists) {
                String tableId = list.getTableId();
                if (tableDAO.ifTableExists(tableId) == 0) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableGuid(tableId);
                    tableInfo.setTableName(list.getTableName());
                    tableInfo.setCreateTime(list.getCreateTime());
                    tableInfo.setStatus(list.getStatus());
                    tableInfo.setDatabaseGuid(list.getDatabaseId());
                    tableInfo.setDbName(list.getDatabaseName());
                    tableInfo.setDatabaseStatus(list.getDatabaseStatus());
                    tableInfo.setDescription(list.getDescription());
                    tableDAO.addTable(tableInfo);
                }
            }
            for (String tableGuid : deleteIds) {
                //表详情
                tableDAO.deleteTableInfo(tableGuid);
                //owner
                tableDAO.deleteTableRelatedOwner(tableGuid);
                //关联关系
                relationDAO.deleteByTableGuid(tableGuid);
                //business2table
                businessDAO.deleteBusinessRelationByTableGuid(tableGuid);
                //表标签
                tableTagDAO.delAllTable2Tag(tableGuid);
                //唯一信任数据
                businessDAO.removeBusinessTrustTableByTableId(tableGuid);
            }
            addFullRelation();
        } catch (Exception e) {
            LOG.error("补充元数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "补充元数据失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateEntityInfo(List<AtlasEntity> entities) {
        try {
            Configuration configuration = ApplicationProperties.get();
            Boolean enableEmail = configuration.getBoolean("metaspace.mail.enable", false);
            for (AtlasEntity entity : entities) {
                String typeName = entity.getTypeName();
                if (typeName.equals("hive_table")) {
                    if (entity.getAttribute("temporary") == null || entity.getAttribute("temporary").toString().equals("false")) {
                        TableInfo tableInfo = new TableInfo();
                        tableInfo.setTableGuid(entity.getGuid());
                        tableInfo.setTableName(getEntityAttribute(entity, "name"));
                        tableInfo.setDescription(getEntityAttribute(entity, "comment"));
                        AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                        tableInfo.setDbName(relatedDB.getDisplayText());
                        tableDAO.updateTable(tableInfo);
                        if (enableEmail) {
                            sendMetadataChangedMail(entity.getGuid());
                        }
                    }
                } else if (("rdbms_table").equals(typeName)) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableGuid(entity.getGuid());
                    tableInfo.setTableName(getEntityAttribute(entity, "name"));
                    tableInfo.setDescription(getEntityAttribute(entity, "comment"));
                    AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                    tableInfo.setDbName(relatedDB.getDisplayText());
                    tableDAO.updateTable(tableInfo);
                    if (enableEmail) {
                        sendMetadataChangedMail(entity.getGuid());
                    }
                } else if (typeName.equals("hive_column")) {
                    String guid = entity.getGuid();
                    String name = entity.getAttribute("name").toString();
                    String type = entity.getAttribute("type").toString();
                    String status = entity.getStatus().name();
                    String description = entity.getAttribute("comment") == null ? null : entity.getAttribute("comment").toString();
                    columnDAO.updateColumnBasicInfo(guid, name, type, status, description);
                } else if (("rdbms_column").equals(typeName)) {
                    String guid = entity.getGuid();
                    String name = entity.getAttribute("name").toString();
                    String type = entity.getAttribute("data_type").toString();
                    String status = entity.getStatus().name();
                    String description = entity.getAttribute("comment") == null ? null : entity.getAttribute("comment").toString();
                    columnDAO.updateColumnBasicInfo(guid, name, type, status, description);
                }
            }
        } catch (Exception e) {
            LOG.error("更新tableinfo表失败", e);
        }
    }

    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String store = "db";
        String hbaseTable = "hbase_table";
        if (entity.getTypeName().equals(hbaseTable)) {
            store = "namespace";
        }
        if (entity.hasRelationshipAttribute(store) && Objects.nonNull(entity.getRelationshipAttribute(store))) {
            Object obj = entity.getRelationshipAttribute(store);
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if (entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public String getCategoryNameById(String guid, String tenantId) {
        return categoryDao.getCategoryNameById(guid, tenantId);
    }

    public String getCategoryNameByRelationId(String guid, String tenantId) {
        return categoryDao.getCategoryNameByRelationId(guid, tenantId);
    }

    public void sendMetadataChangedMail(String tableGuid) throws AtlasBaseException {
        try {
            Table info = tableDAO.getDbAndTableName(tableGuid);
            String sendMessage = "数据库[" + info.getDatabaseName() + "]下的表[" + info.getTableName() + "]元数据发生变更";
            String subject = "元数据变更提醒";
            List<String> emails = userDAO.getUsersEmail(tableGuid);
            sendMail(emails, subject, sendMessage);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("发送邮件失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "发送邮件失败");
        }
    }

    public void sendMail(List<String> toList, String subject, String content) throws AtlasBaseException {
        try {
            StringJoiner mailJoiner = new StringJoiner(",");
            toList.forEach(to -> mailJoiner.add(to));

            Configuration configuration = ApplicationProperties.get();

            String user = configuration.getString("metaspace.mail.user");
            String passwd = configuration.getString("metaspace.mail.password");

            if (user == null || passwd == null) {
                LOG.warn("发件邮箱用户名和密码不能为空");
            }

            Properties props = new Properties();
            Iterator<String> mailKeys = configuration.getKeys("metaspace.mail.service");
            while (mailKeys.hasNext()) {
                String mailKey = mailKeys.next();
                String key = mailKey.replace("metaspace.mail.service.", "");
                String value = configuration.getString(mailKey);
                props.setProperty(key, value);
            }

            Session session = Session.getInstance(props);
            session.setDebug(true);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, mailJoiner.toString());
            message.setSubject(subject);
            message.setText(content);
            message.saveChanges();

            Transport ts = session.getTransport();
            ts.connect(user, passwd);
            ts.sendMessage(message, message.getAllRecipients());
            ts.close();
        } catch (Exception e) {
            LOG.error("获取邮件发送方失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public String dbsToString(List<String> dbs) {
        if (dbs == null || dbs.size() == 0) {
            return "";
        }
        StringBuffer str = new StringBuffer();
        for (String db : dbs) {
            str.append("'");
            str.append(db.replaceAll("'", "\\\\'"));
            str.append("'");
            str.append(",");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }


    /**
     * 目录转化为文件
     *
     * @param ids
     * @param categoryType
     * @return
     * @throws IOException
     * @throws AtlasBaseException
     */
    public File exportExcel(List<String> ids, int categoryType, String tenantId) throws IOException, AtlasBaseException {
        List<CategoryExport> data = queryByIds(ids, categoryType, tenantId);
        Workbook workbook = data2workbook(data);
        return workbook2file(workbook);
    }

    /**
     * 全局导出
     *
     * @param categoryType
     * @return
     * @throws IOException
     * @throws AtlasBaseException
     */
    public File exportExcelAll(int categoryType, String tenantId) throws IOException, SQLException {
        Set<CategoryEntityV2> data = categoryDao.getAll(categoryType, tenantId);
        Workbook workbook = allData2workbook(data);
        return workbook2file(workbook);
    }

    private List<CategoryExport> queryByIds(List<String> ids, int categoryType, String tenantId) throws AtlasBaseException {
        List<CategoryExport> data = userGroupDAO.getCategoryByIds(ids, categoryType, tenantId);
        int i = 0;
        List<CategoryExport> sortData = new ArrayList<>();
        sortData.addAll(data);
        Map<String, Integer> idMap = new HashMap<>();
        for (String id : ids) {
            idMap.put(id, i);
            i++;
        }
        for (CategoryExport categoryExport : data) {
            Integer integer = idMap.get(categoryExport.getGuid());
            if (integer != null) {
                sortData.set(integer, categoryExport);
            }
        }
        return sortData;
    }

    private Workbook data2workbook(List<CategoryExport> list) {
        List<List<String>> dataList = list.stream().map(categoryExport -> {
            List<String> data = Lists.newArrayList(categoryExport.getName(), categoryExport.getDescription());
            return data;
        }).collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        PoiExcelUtils.createSheet(workbook, "目录", Lists.newArrayList("目录名字", "目录描述"), dataList);
        return workbook;
    }

    //全局导出
    private Workbook allData2workbook(Set<CategoryEntityV2> list) {
        List<List<String>> dataList = list.stream().map(categoryEntityV2 -> {
            List<String> data = Lists.newArrayList(categoryEntityV2.getGuid(), categoryEntityV2.getName(), categoryEntityV2.getDescription(), categoryEntityV2.getUpBrotherCategoryGuid(), categoryEntityV2.getDownBrotherCategoryGuid(), categoryEntityV2.getParentCategoryGuid(), categoryEntityV2.getQualifiedName(), new Integer(categoryEntityV2.getLevel()).toString());
            return data;
        }).collect(Collectors.toList());

        Workbook workbook = new XSSFWorkbook();
        PoiExcelUtils.createSheet(workbook, "目录", Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别"), dataList);
        return workbook;
    }

    private File workbook2file(Workbook workbook) throws IOException {
        File tmpFile = File.createTempFile("CategoryExport", ".xlsx");
        try (FileOutputStream output = new FileOutputStream(tmpFile)) {
            workbook.write(output);
            output.flush();
            output.close();
        }
        return tmpFile;
    }

    /**
     * 同名校验
     *
     * @param categoryId
     * @param categoryExports
     */
    public void checkSameName(String categoryId, String direction, List<CategoryExport> categoryExports, int type, String tenantId) throws SQLException {
        List<String> childCategoryName;
        if (categoryId != null && categoryId.length() != 0) {
            String up = "up";
            String down = "down";
            if (up.equals(direction) || down.equals(direction)) {
                String parentCategoryGuid = categoryDao.queryByGuid(categoryId, tenantId).getParentCategoryGuid();
                childCategoryName = categoryDao.getChildCategoryName(parentCategoryGuid, tenantId);
            } else {
                childCategoryName = categoryDao.getChildCategoryName(categoryId, tenantId);
            }

        } else {
            childCategoryName = categoryDao.getChildCategoryNameByType(type, tenantId);
        }

        List<CategoryExport> categoryExportList = new ArrayList<>(categoryExports);
        for (CategoryExport categoryExport : categoryExportList) {
            if (!childCategoryName.contains(categoryExport.getName())) {
                continue;
            }
            categoryExports.remove(categoryExport);
        }

    }

    /**
     * 导入目录
     *
     * @param categoryId
     * @param fileInputStream
     * @param type
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public List<CategoryPrivilege> importCategory(String categoryId, String direction, File fileInputStream, boolean authorized, int type, String tenantId) throws Exception {
        if (!fileInputStream.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<CategoryExport> categoryExports;
        try {
            categoryExports = file2Data(fileInputStream);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件异常：" + e.getMessage());
        }
        checkSameName(categoryId, direction, categoryExports, type, tenantId);
        if (categoryExports.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空或全部重名");
        }
        Map<String, CategoryEntityV2> newCategorys = new HashMap<>();

        //是否是导入一级目录
        String upGuid;
        String downGuid;
        String parentQualifiedName = null;
        String parentCategoryGuid = null;
        int level;
        String down = "down";
        String up = "up";
        if (categoryId != null && categoryId.length() != 0) {
            if (up.equals(direction)) {
                CategoryEntityV2 currentEntity = categoryDao.queryByGuid(categoryId, tenantId);
                downGuid = categoryId;
                upGuid = currentEntity.getUpBrotherCategoryGuid();
                level = currentEntity.getLevel() - 1;
                if (currentEntity.getParentCategoryGuid() != null && currentEntity.getParentCategoryGuid().length() > 0) {
                    CategoryEntityV2 categoryEntityV2 = categoryDao.queryByGuid(currentEntity.getParentCategoryGuid(), tenantId);
                    parentQualifiedName = categoryEntityV2.getQualifiedName();
                    parentCategoryGuid = currentEntity.getParentCategoryGuid();
                }
            } else if (down.equals(direction)) {
                CategoryEntityV2 currentEntity = categoryDao.queryByGuid(categoryId, tenantId);
                upGuid = categoryId;
                downGuid = currentEntity.getDownBrotherCategoryGuid();
                level = currentEntity.getLevel() - 1;
                if (currentEntity.getParentCategoryGuid() != null && currentEntity.getParentCategoryGuid().length() > 0) {
                    CategoryEntityV2 categoryEntityV2 = categoryDao.queryByGuid(currentEntity.getParentCategoryGuid(), tenantId);
                    parentQualifiedName = categoryEntityV2.getQualifiedName();
                    parentCategoryGuid = currentEntity.getParentCategoryGuid();
                }
            } else {
                parentCategoryGuid = categoryId;
                upGuid = categoryDao.queryLastChildCategory(categoryId, tenantId);
                downGuid = null;
                CategoryEntityV2 currentEntity = categoryDao.queryByGuid(categoryId, tenantId);
                parentQualifiedName = currentEntity.getQualifiedName();
                level = currentEntity.getLevel();
            }
        } else {
            upGuid = categoryDao.queryLastCategory(type, tenantId);
            downGuid = null;
            parentQualifiedName = null;
            level = 0;
        }

        CategoryEntityV2 upChild = categoryDao.queryByGuid(upGuid, tenantId);
        if (upChild == null) {
            upChild = new CategoryEntityV2();
        }
        newCategorys.put(upGuid, upChild);
        Timestamp createTime = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
        String upId = upGuid;
        for (CategoryExport categoryExport : categoryExports) {
            StringBuffer qualifiedName = new StringBuffer();
            String name = categoryExport.getName();
            CategoryEntityV2 categoryEntityV2 = new CategoryEntityV2();
            categoryEntityV2.setName(name);
            categoryEntityV2.setSafe("1");
            categoryEntityV2.setCategoryType(type);
            categoryEntityV2.setDescription(categoryExport.getDescription());
            categoryEntityV2.setGuid(categoryExport.getGuid());
            categoryEntityV2.setParentCategoryGuid(parentCategoryGuid);
            categoryEntityV2.setLevel(level + 1);
            categoryEntityV2.setCreateTime(createTime);

            if (StringUtils.isNotEmpty(parentQualifiedName) && parentQualifiedName.length() > 0)
                qualifiedName.append(parentQualifiedName + ".");
            qualifiedName.append(name);

            categoryEntityV2.setUpBrotherCategoryGuid(upId);
            newCategorys.get(upId).setDownBrotherCategoryGuid(categoryExport.getGuid());
            upId = categoryEntityV2.getGuid();

            newCategorys.put(categoryEntityV2.getGuid(), categoryEntityV2);
        }
        newCategorys.remove(upGuid);
        if (newCategorys.get(upId) != null) {
            newCategorys.get(upId).setDownBrotherCategoryGuid(downGuid);
        }
        if (upGuid != null) {
            categoryDao.updateDownBrotherCategoryGuid(upGuid, upChild.getDownBrotherCategoryGuid(), tenantId);
        }
        if (downGuid != null) {
            categoryDao.updateUpBrotherCategoryGuid(downGuid, upId, tenantId);
        }

        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
        ArrayList<CategoryEntityV2> categoryEntityV2s = new ArrayList<>(newCategorys.values());
        List<GroupPrivilege> parentPrivilege = userGroupDAO.getCategoryGroupPrivileges(parentCategoryGuid, tenantId);
        if (parentPrivilege.size() != 0 && categoryEntityV2s != null) {
            userGroupDAO.addUserGroupCategoryPrivileges(parentPrivilege, categoryEntityV2s);
        }
        categoryDao.addAll(categoryEntityV2s, tenantId);
        if (type == 3 || type == 4) {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        } else if (level != 0 || !authorized) {
            fileInputStream.delete();
            GroupPrivilege groupPrivilege = new GroupPrivilege();
            groupPrivilege.setRead(false);
            groupPrivilege.setEditCategory(false);
            groupPrivilege.setEditItem(false);
            parentPrivilege.forEach(category -> {
                groupPrivilege.setRead(category.getRead() || groupPrivilege.getRead());
                groupPrivilege.setEditCategory(category.getEditCategory() || groupPrivilege.getEditCategory());
                groupPrivilege.setEditItem(category.getEditItem() || groupPrivilege.getEditItem());
            });
            privilege = getCategoryPrivilege(groupPrivilege.getRead(), groupPrivilege.getEditItem(), groupPrivilege.getEditCategory());
        } else if (authorized) {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        } else {
            privilege = new CategoryPrivilege.Privilege(false, true, true, true, true, true, true, true, true, false);
        }
        List<CategoryPrivilege> categoryPrivileges = new ArrayList<>();
        for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2s) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(categoryEntityV2);
            categoryPrivilege.setPrivilege(privilege);
            categoryPrivileges.add(categoryPrivilege);
        }
        return categoryPrivileges;


        //技术目录一级目录不允许删关联
//        if (type == 0 && category.getLevel() == 1) {
//            privilege.setDeleteRelation(false);
//        }
    }

    public CategoryPrivilege.Privilege getCategoryPrivilege(boolean read, boolean editItem, boolean editCategory) {
        CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
        if (read) {
            privilege.setHide(false);
            privilege.setAsh(false);
        } else {
            privilege.setHide(false);
            privilege.setAsh(true);
        }
        if (editCategory) {
            privilege.setAddSibling(true);
            privilege.setDelete(true);
            privilege.setMove(true);
        } else {
            privilege.setAddSibling(false);
            privilege.setDelete(false);
            privilege.setMove(false);
        }
        if (editCategory) {
            privilege.setAddChildren(true);
            privilege.setEdit(true);
        } else {
            privilege.setAddChildren(false);
            privilege.setEdit(false);
        }
        if (editItem) {
            privilege.setCreateRelation(true);
            privilege.setDeleteRelation(true);
            privilege.setAddOwner(true);
        } else {
            privilege.setCreateRelation(false);
            privilege.setDeleteRelation(false);
            privilege.setAddOwner(false);
        }
        return privilege;
    }

    /**
     * 文件转化为目录
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<CategoryExport> file2Data(File file) throws Exception {
        List<String> names = new ArrayList<>();
        List<CategoryExport> categoryExports = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        //文件格式校验
        Row first = sheet.getRow(0);
        ArrayList<String> strings = Lists.newArrayList("目录名字", "目录描述");
        ArrayList<String> allStrings = Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别");

        for (int i = 0; i < strings.size(); i++) {
            Cell cell = first.getCell(i);
            if (Objects.isNull(cell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
            } else {
                if (!strings.get(i).equals(cell.getStringCellValue())) {
                    if (allStrings.get(i).equals(cell.getStringCellValue())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "全局导出文件只能用于全局导入，请导入正确的文件");
                    }
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
                }
            }
        }

        int rowNum = sheet.getLastRowNum() + 1;
        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            CategoryExport category = new CategoryExport();
            Cell nameCell = row.getCell(0);
            if (Objects.isNull(nameCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名称不能为空");
            }
            if (names.contains(nameCell.getStringCellValue())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中存在相同目录名");
            }
            category.setName(nameCell.getStringCellValue());

            Cell discriptionCell = row.getCell(1);
            if (Objects.isNull(discriptionCell)) {
                category.setDescription("");
            } else {
                category.setDescription(discriptionCell.getStringCellValue());
            }

            String guid = UUID.randomUUID().toString();
            category.setGuid(guid);
            categoryExports.add(category);
            names.add(nameCell.getStringCellValue());
        }
        return categoryExports;
    }


    /**
     * 变更目录结构
     *
     * @param categories
     * @param type
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveCategories(MoveCategory categories, int type, String tenantId) throws AtlasBaseException {
        if (categories.getInit() != null) {
            categories.getInit().setGuid(categories.getGuid());
            sortAndAlterCategory(categories.getInit(), type, tenantId);
        }
        for (CategoryEntityV2 categoryEntityV2 : categories.getMove()) {
            moveCategory(categoryEntityV2, tenantId);
        }
    }

    /**
     * 移动目录
     *
     * @param category
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveCategory(CategoryEntityV2 category, String tenantId) throws AtlasBaseException {
        try {
            CategoryEntityV2 categoryEntityV2 = categoryDao.queryByGuid(category.getGuid(), tenantId);
            String newUpBrotherCategoryGuid = category.getUpBrotherCategoryGuid();
            String newDownBrotherCategoryGuid = category.getDownBrotherCategoryGuid();
            String oldParentCategoryGuid = categoryEntityV2.getParentCategoryGuid();
            if (oldParentCategoryGuid == null || oldParentCategoryGuid.length() == 0) {
                oldParentCategoryGuid = null;
            }

            if (newDownBrotherCategoryGuid != null) {
                newUpBrotherCategoryGuid = categoryDao.queryByGuid(newDownBrotherCategoryGuid, tenantId).getUpBrotherCategoryGuid();
            } else if (newUpBrotherCategoryGuid != null) {
                newDownBrotherCategoryGuid = categoryDao.queryByGuid(newUpBrotherCategoryGuid, tenantId).getDownBrotherCategoryGuid();
            }

            String newParentCategoryGuid = category.getParentCategoryGuid();
            if (newParentCategoryGuid == null || newParentCategoryGuid.length() == 0) {
                newParentCategoryGuid = null;
            }
            if (!Objects.equals(oldParentCategoryGuid, newParentCategoryGuid)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "只能移动同级目录");
            }

            String oldUpBrotherCategoryGuid = categoryEntityV2.getUpBrotherCategoryGuid();
            String oldDownBrotherCategoryGuid = categoryEntityV2.getDownBrotherCategoryGuid();

            //修改原所在位置目录结构
            if (oldUpBrotherCategoryGuid != null && oldUpBrotherCategoryGuid.length() != 0) {
                categoryDao.updateDownBrotherCategoryGuid(oldUpBrotherCategoryGuid, oldDownBrotherCategoryGuid, tenantId);
            }
            if (oldDownBrotherCategoryGuid != null && oldDownBrotherCategoryGuid.length() != 0) {
                categoryDao.updateUpBrotherCategoryGuid(oldDownBrotherCategoryGuid, oldUpBrotherCategoryGuid, tenantId);
            }
            //修改移动后的结构
            if (newUpBrotherCategoryGuid != null && newUpBrotherCategoryGuid.length() != 0) {
                categoryDao.updateDownBrotherCategoryGuid(newUpBrotherCategoryGuid, category.getGuid(), tenantId);
            }
            if (newDownBrotherCategoryGuid != null && newDownBrotherCategoryGuid.length() != 0) {
                categoryDao.updateUpBrotherCategoryGuid(newDownBrotherCategoryGuid, category.getGuid(), tenantId);
            }
            //修改自己的目录结构
            categoryDao.updateUpBrotherCategoryGuid(category.getGuid(), newUpBrotherCategoryGuid, tenantId);
            categoryDao.updateDownBrotherCategoryGuid(category.getGuid(), newDownBrotherCategoryGuid, tenantId);
        } catch (SQLException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "移动目录失败");
        }

    }

    /**
     * 排序并变更目录
     *
     * @param sortCategory
     * @param type
     */
    public void sortAndAlterCategory(SortCategory sortCategory, int type, String tenantId) throws AtlasBaseException {
        List<RoleModulesCategories.Category> childCategorys;
        if (sortCategory.getGuid() == null || sortCategory.getGuid().length() == 0) {
            List<Module> modules = tenantService.getModule(tenantId);
            if (!modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId() == module.getModuleId()) && type == 1) {
                throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有变更所有目录的权限");
            }
            childCategorys = userGroupDAO.getAllCategorysAndSort(type, sortCategory.getSort(), sortCategory.getOrder(), tenantId);
        } else {
            childCategorys = userGroupDAO.getChildCategorysAndSort(Arrays.asList(sortCategory.getGuid()), type, sortCategory.getSort(), sortCategory.getOrder(), tenantId);
        }
        if (childCategorys != null && childCategorys.size() != 0) {
            categoryDao.updateCategoryTree(childCategorys, tenantId);
        }
    }

    /**
     * 变更目录
     *
     * @param sortCategory
     * @param type
     * @return
     */
    public List<RoleModulesCategories.Category> sortCategory(SortCategory sortCategory, int type, String tenantId) throws AtlasBaseException {
        List<RoleModulesCategories.Category> childCategorys;
        if (sortCategory.getGuid() == null || sortCategory.getGuid().length() == 0) {
            List<Module> modules = tenantService.getModule(tenantId);
            if (!modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId() == module.getModuleId()) && type == 1) {
                throw new AtlasBaseException(AtlasErrorCode.PERMISSION_DENIED, "当前用户没有变更所有目录的权限");
            }
            childCategorys = userGroupDAO.getAllCategorysAndSort(type, sortCategory.getSort(), sortCategory.getOrder(), tenantId);
        } else {
            childCategorys = userGroupDAO.getChildCategorysAndSort(Arrays.asList(sortCategory.getGuid()), type, sortCategory.getSort(), sortCategory.getOrder(), tenantId);
        }
        if (sortCategory.getGuid() != null && sortCategory.getGuid().length() != 0) {
            RoleModulesCategories.Category category = categoryDao.getCategoryByGuid(sortCategory.getGuid(), tenantId);
            childCategorys.add(category);
        }
        return childCategorys;
    }

    /**
     * 导入目录
     *
     * @param fileInputStream
     * @param type
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void importAllCategory(File fileInputStream, int type, String tenantId) throws Exception {
        if (!fileInputStream.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        Set<CategoryEntityV2> all = categoryDao.getAll(type, tenantId);
        if (all.size() != 0 && type != 0 && type != dataStandType) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在目录，无法全局导入");
        } else if (type == technicalType && all.size() != technicalCount) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在初始目录之外的目录，无法全局导入");
        } else if (type == dataStandType && all.size() > dataStandCount) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在初始目录之外的目录，无法全局导入");
        }
        List<CategoryEntityV2> categories;
        List<CategoryEntityV2> systemCategory = new ArrayList<>();
        try {
            categories = file2AllData(fileInputStream, type, systemCategory);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "文件异常：" + e.getMessage());
        }
        if (systemCategory.size() != 0) {
            categoryDao.updateCategoryEntityV2Tree(systemCategory, tenantId);
        }
        try {
            if (categories.size() != 0) {
                categoryDao.addAll(categories, tenantId);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "禁止不同类型的目录互相导入，请选择正确的文件导入");
        }

        fileInputStream.delete();
    }

    /**
     * 文件转化为目录
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<CategoryEntityV2> file2AllData(File file, int type, List<CategoryEntityV2> systemCategory) throws Exception {
        List<CategoryEntityV2> categories = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum() + 1;

        List<String> systemCategoryGuids;
        if (type == technicalType) {
            systemCategoryGuids = new ArrayList<>(CategoryUtil.initTechnicalCategoryId);
        } else if (type == dataStandType) {
            systemCategoryGuids = new ArrayList<>(CategoryUtil.initDataStandardCategoryId);
        } else {
            systemCategoryGuids = new ArrayList<>();
        }
        Timestamp createTime = io.zeta.metaspace.utils.DateUtils.currentTimestamp();

        //文件格式校验
        Row first = sheet.getRow(0);
        ArrayList<String> strings = Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别");
        for (int i = 0; i < strings.size(); i++) {
            Cell cell = first.getCell(i);
            if (Objects.isNull(cell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
            } else {
                if (!strings.get(i).equals(cell.getStringCellValue())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内部格式错误，请导入正确的文件");
                }
            }
        }

        for (int i = 1; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            CategoryEntityV2 category = new CategoryEntityV2();

            Cell guidCell = row.getCell(0);
            if (Objects.isNull(guidCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "guid不能为空");
            }
            category.setGuid(guidCell.getStringCellValue());

            Cell nameCell = row.getCell(1);
            if (Objects.isNull(nameCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名称不能为空");
            }
            category.setName(nameCell.getStringCellValue());

            Cell discriptionCell = row.getCell(2);
            if (Objects.isNull(discriptionCell)) {
                category.setDescription("");
            } else {
                category.setDescription(discriptionCell.getStringCellValue());
            }

            Cell upCell = row.getCell(3);
            if (Objects.isNull(upCell) || upCell.getStringCellValue().length() == 0) {
                category.setUpBrotherCategoryGuid(null);
            } else {
                category.setUpBrotherCategoryGuid(upCell.getStringCellValue());
            }

            Cell downCell = row.getCell(4);
            if (Objects.isNull(downCell) || downCell.getStringCellValue().length() == 0) {
                category.setDownBrotherCategoryGuid(null);
            } else {
                category.setDownBrotherCategoryGuid(downCell.getStringCellValue());
            }

            Cell parentCell = row.getCell(5);
            if (Objects.isNull(parentCell) || parentCell.getStringCellValue().length() == 0) {
                category.setParentCategoryGuid(null);
            } else {
                category.setParentCategoryGuid(parentCell.getStringCellValue());
            }

            Cell qualifiedNameCell = row.getCell(6);
            if (Objects.isNull(qualifiedNameCell)) {
                category.setQualifiedName(null);
            } else {
                category.setQualifiedName(qualifiedNameCell.getStringCellValue());
            }

            Cell levelNameCell = row.getCell(7);
            if (Objects.isNull(levelNameCell)) {
                category.setLevel(0);
            } else {
                category.setLevel(Integer.parseInt(levelNameCell.getStringCellValue()));
            }

            category.setCategoryType(type);
            category.setCreateTime(createTime);
            if (systemCategoryGuids.contains(category.getGuid())) {
                systemCategoryGuids.remove(category.getGuid());
                systemCategory.add(category);
            } else {
                categories.add(category);
            }
        }
        if (systemCategoryGuids.size() != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件内容不合规范，不包含初始目录，请使用全局导出的文件");
        }
        return categories;
    }

    public String uploadCategory(String categoryId, String direction, File fileInputStream, int type, String tenantId) throws Exception {
        List<CategoryExport> categoryExports;
        try {
            categoryExports = file2Data(fileInputStream);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件异常：" + e.getMessage());
        }
        checkSameName(categoryId, direction, categoryExports, type, tenantId);
        if (categoryExports.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空或全部重名");
        }
        return ExportDataPathUtils.transferTo(fileInputStream);
    }

    public String uploadAllCategory(File fileInputStream, int type, String tenantId) throws Exception {
        Set<CategoryEntityV2> all = categoryDao.getAll(type, tenantId);
        if (all.size() != 0 && type != 0 && type != dataStandType) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在目录，无法全局导入");
        } else if (type == technicalType && all.size() != technicalCount) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在初始目录之外的目录，无法全局导入");
        } else if (type == dataStandType && all.size() > dataStandCount) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在初始目录之外的目录，无法全局导入");
        }
        List<CategoryEntityV2> categories;
        try {
            categories = file2AllData(fileInputStream, type, new ArrayList<>());
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "文件异常：" + e.getMessage());
        }
        return ExportDataPathUtils.transferTo(fileInputStream);
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateTable() throws AtlasBaseException {
        PageResult<Table> tableNameAndDbNameByQuery;
        //判断独立部署和多租户
        tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", false, 0, -1);
        List<Table> lists = tableNameAndDbNameByQuery.getLists();
        for (Table list : lists) {
            String tableId = list.getTableId();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableGuid(tableId);
            tableInfo.setTableName(list.getTableName());
            tableInfo.setDbName(list.getDatabaseName());
            String description = list.getDescription();
            if (!description.equals("")) {
                tableInfo.setDescription(list.getDescription());
            }
            if (tableDAO.ifTableInfo(tableInfo) == 0) {
                tableDAO.updateTable(tableInfo);
            }
        }

    }

    public void migrateCategory(String categoryId, String parentId, int type, String tenantId) throws Exception {
        CategoryEntityV2 category = categoryDao.queryByGuid(categoryId, tenantId);
        if (category == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录不存在");
        }
        if (categoryDao.querySameNameNum(category.getName(), parentId, type, tenantId) > 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在同名目录，请迁移到其他目录或修改目录名字");
        }
        List<GroupPrivilege> parentPrivilege = userGroupDAO.getCategoryGroupPrivileges(parentId, tenantId);
        List<GroupPrivilege> oldPrivilege = userGroupDAO.getCategoryGroupPrivileges(categoryId, tenantId);
        Map<String, GroupPrivilege> privilegeMap = new HashMap<>();
        List<GroupPrivilege> insertPrivilege = new ArrayList<>();
        List<GroupPrivilege> updatePrivilege = new ArrayList<>();
        oldPrivilege.forEach(privilege -> {
            privilegeMap.put(privilege.getId(), privilege);
        });
        parentPrivilege.forEach(privilege -> {
            GroupPrivilege groupPrivilege = privilegeMap.get(privilege.getId());
            if (groupPrivilege == null || groupPrivilege.getRead() == null) {
                GroupPrivilege childPrivilege = new GroupPrivilege(privilege);
                childPrivilege.setCategoryId(categoryId);
                insertPrivilege.add(privilege);
            } else if (privilege.getRead()) {
                groupPrivilege.setRead(true);
                groupPrivilege.setCategoryId(categoryId);
                if (privilege.getEditItem()) {
                    groupPrivilege.setEditItem(true);
                }
                if (privilege.getEditCategory()) {
                    groupPrivilege.setEditCategory(true);
                }
                updatePrivilege.add(groupPrivilege);
            }
        });

        //子目录权限
        List<RoleModulesCategories.Category> childCategorys = userGroupDAO.getChildCategorys(Lists.newArrayList(categoryId), category.getCategoryType(), tenantId);
        for (RoleModulesCategories.Category childCategory : childCategorys) {
            privilegeMap.clear();
            List<GroupPrivilege> childOldPrivilege = userGroupDAO.getCategoryGroupPrivileges(childCategory.getGuid(), tenantId);
            childOldPrivilege.forEach(privilege -> {
                privilegeMap.put(privilege.getId(), privilege);
            });
            parentPrivilege.forEach(privilege -> {
                GroupPrivilege groupPrivilege = privilegeMap.get(privilege.getId());
                if (groupPrivilege == null || groupPrivilege.getRead() == null) {
                    GroupPrivilege childPrivilege = new GroupPrivilege(privilege);
                    childPrivilege.setCategoryId(childCategory.getGuid());
                    insertPrivilege.add(privilege);
                } else if (privilege.getRead()) {
                    groupPrivilege.setRead(true);
                    groupPrivilege.setCategoryId(childCategory.getGuid());
                    if (privilege.getEditItem()) {
                        groupPrivilege.setEditItem(true);
                    }
                    if (privilege.getEditCategory()) {
                        groupPrivilege.setEditCategory(true);
                    }
                    updatePrivilege.add(groupPrivilege);
                }
            });

        }

        if (insertPrivilege.size() != 0) {
            userGroupDAO.addUserGroupPrivileges(insertPrivilege);
        }
        if (updatePrivilege.size() != 0) {
            userGroupDAO.updateUserGroupPrivileges(updatePrivilege);
        }
        String upId = category.getUpBrotherCategoryGuid();
        String downId = category.getDownBrotherCategoryGuid();
        String lastChildGuid = categoryDao.queryLastChildCategory(parentId, tenantId);
        categoryDao.updateDownBrotherCategoryGuid(upId, downId, tenantId);
        categoryDao.updateUpBrotherCategoryGuid(downId, upId, tenantId);
        categoryDao.updateDownBrotherCategoryGuid(lastChildGuid, categoryId, tenantId);
        categoryDao.updateParentCategoryGuid(categoryId, parentId, lastChildGuid, null, tenantId);
    }

    /**
     * 获取目录迁移可迁移到的目录
     *
     * @param categoryId
     * @param type
     * @param tenantId
     * @return
     * @throws SQLException
     * @throws AtlasBaseException
     */
    public List<CategoryPrivilege> getMigrateCategory(String categoryId, int type, String tenantId) throws SQLException, AtlasBaseException {
        CategoryEntityV2 category = getCategory(categoryId, tenantId);
        if (category == null) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录不存在");
        }
        if (category.getLevel() == 1) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "一级目录无法迁移");
        }
        List<CategoryPrivilege> allCategory = getAllByUserGroup(type, tenantId);
        List<CategoryPrivilege> categories = allCategory.stream()
                .filter(cate -> cate.getLevel() == category.getLevel() - 1 && cate.getPrivilege().isAddChildren() && !cate.getGuid().equals(category.getParentCategoryGuid()))
                .collect(Collectors.toList());
        if (categories.size() == 0) {
            return categories;
        }
        List<String> categoryIds = categories.stream().map(cate -> cate.getGuid()).collect(Collectors.toList());
        List<RoleModulesCategories.Category> parentCategories = userGroupDAO.getParentCategorys(categoryIds, type, tenantId);
        List<CategoryPrivilege> categoryPrivileges = parentCategories.stream().map(cate -> {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(cate);
            return categoryPrivilege;
        }).collect(Collectors.toList());
        categories.addAll(categoryPrivileges);
        return categories;

    }
}
