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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.dto.indices.IndexFieldExport;
import io.zeta.metaspace.model.dto.indices.IndexFieldNode;
import io.zeta.metaspace.model.enums.CategoryPrivateStatus;
import io.zeta.metaspace.model.enums.SourceInfoOperation;
import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.po.sourceinfo.SourceInfo;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.share.APIDataOwner;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForCategory;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.utils.ThreadPoolUtil;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseDAO;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.dao.sourceinfo.SourceInfoDAO;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDatabaseService;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.repository.Constants;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.web.service.DataShareService.METASPACE_MOBIUS_ADDRESS;

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
    TableDAO tableDAO;
    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DatabaseDAO databaseDAO;
    @Autowired
    DatabaseInfoDAO databaseInfoDAO;
    @Autowired
    SourceInfoDatabaseService sourceInfoDatabaseService;
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
    @Autowired
    DataSourceDAO dataSourceDAO;
    @Autowired
    KafkaConnectorDAO kafkaConnectorDAO;
    @Autowired
    DbDAO dbDAO;
    @Autowired
    TenantDAO tenantDAO;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private SourceInfoDAO sourceInfoDAO;
    @Autowired
    private AtlasEntityStore atlasEntityStore;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDAO;

    @Autowired
    private SourceInfoDeriveColumnInfoDAO sourceInfoDeriveColumnInfoDAO;

    int technicalType = 0;
    int dataStandType = 3;
    int technicalCount = 5;
    int dataStandCount = 14;

    private static final int CREATE_MAX_CATEGORY_LEVEL = 4;
    private static final int UPDATE_MAX_CATEGORY_LEVEL = 5;
    private static final String ORGANIZATION_FIRST_PID = "sso.organization.first.pid";


    public List<CategoryPrivilege> getTechnicalCategory(String tenantId) {
        List<CategoryPrivilege> categoryPrivilegeList = new ArrayList<>();
        List<CategoryPrivilege> categoryPrivilegeListNew = new ArrayList<>();
        try {
            User user = AdminUtils.getUserData();
            List<String> userGroupIds = new ArrayList<>();
            //获取用户组
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            if (!CollectionUtils.isEmpty(userGroups)) {
                userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            }
            categoryPrivilegeList = categoryDAO.selectListByTenantIdAndStatus(tenantId, user.getUserId(), userGroupIds, 5);
            List<SourceInfo> sourceInfoList = sourceInfoDAO.selectCategoryListAndCount(tenantId);
            Map<String, Integer> map = new HashMap<>();
            if (!CollectionUtils.isEmpty(sourceInfoList)) {
                map = sourceInfoList.stream().collect(Collectors.toMap(SourceInfo::getCategoryId, SourceInfo::getCount));
            }
            Set<String> guidSet = new HashSet<>();
            for (CategoryPrivilege categoryPrivilege : categoryPrivilegeList) {
                categoryPrivilege.setCount(0);
                CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
                privilege.setHide(false);
                privilege.setAsh(false);
                privilege.setAddSibling(true);
                privilege.setAddChildren(true);
                privilege.setCreateRelation(true);
                privilege.setEdit(categoryPrivilege.isEdit());
                privilege.setDelete(categoryPrivilege.isEdit());
                //源信息登记的，不能编辑，不能添加子目录，不能删除
                if (map.keySet().contains(categoryPrivilege.getGuid())) {
                    privilege.setEdit(false);
                    privilege.setAddChildren(false);
                    privilege.setDelete(false);
                    privilege.setCreateRelation(true);
                    categoryPrivilege.setCount(map.get(categoryPrivilege.getGuid()));
                } else if (MetaspaceConfig.systemCategory.contains(categoryPrivilege.getGuid())) {
                    privilege.setDelete(false);
                    if (privilege.isEdit()) {
                        privilege.setEditSafe(true);
                        privilege.setCreateRelation(true);
                    }
                    privilege.setEdit(false);
                }

                categoryPrivilege.setPrivilege(privilege);
                if (guidSet.contains(categoryPrivilege.getGuid())) {
                    if (categoryPrivilege.isEdit()) {
                        removeDuplicatedData(categoryPrivilegeListNew, categoryPrivilege.getGuid());
                        categoryPrivilegeListNew.add(categoryPrivilege);
                    }
                } else {
                    guidSet.add(categoryPrivilege.getGuid());
                    categoryPrivilegeListNew.add(categoryPrivilege);
                }
            }
            //源信息登记的父级目录不能删除
            updateParentCategory(categoryPrivilegeListNew);
            removeNoParentCategory(categoryPrivilegeListNew);
            if (CollectionUtils.isEmpty(userGroups)) {
                for (CategoryPrivilege categoryPrivilege : categoryPrivilegeListNew) {
                    categoryPrivilege.getPrivilege().setEdit(false);
                    categoryPrivilege.getPrivilege().setAddSibling(false);
                    categoryPrivilege.getPrivilege().setAddChildren(false);
                    categoryPrivilege.getPrivilege().setDelete(false);
                }
            }
        } catch (AtlasBaseException e) {
            LOG.error("getTechnicalCategory exception is {}", e);
        }
        return categoryPrivilegeListNew;
    }

    /**
     * 删除指定数据
     */
    private void removeDuplicatedData(List<CategoryPrivilege> categoryPrivilegeListNew, String guid) {
        Iterator<CategoryPrivilege> iterator = categoryPrivilegeListNew.iterator();
        while (iterator.hasNext()) {
            CategoryPrivilege categoryPrivilege = iterator.next();
            if (categoryPrivilege.getGuid().equals(guid)) {
                iterator.remove();
                return;
            }
        }
    }


    public List<CategoryPrivilege> getTechnicalCategoryByUserId(String tenantId, String userId) {
        List<CategoryPrivilege> categoryPrivilegeList = new ArrayList<>();
        try {
            List<String> userGroupIds = new ArrayList<>();
            //获取用户组
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(userId, tenantId);
            if (!CollectionUtils.isEmpty(userGroups)) {
                userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            }
            categoryPrivilegeList = categoryDAO.selectListByTenantIdAndGroupId(tenantId, userGroupIds);
            removeNoParentCategory(categoryPrivilegeList);
            categoryPrivilegeList.forEach(categoryPrivilege -> {
                        if (CollectionUtils.isEmpty(userGroups)) {
                            categoryPrivilege.setEditItem(false);
                            categoryPrivilege.setEditCategory(false);
                        }
                        if (MetaspaceConfig.systemCategory.contains(categoryPrivilege.getGuid())) {
                            categoryPrivilege.setEditItem(false);
                            categoryPrivilege.setEditCategory(false);
                        }
                    }
            );
        } catch (AtlasBaseException e) {
            LOG.error("getTechnicalCategory exception is {}", e);
        }
        return categoryPrivilegeList;
    }

    /**
     * 获取当前租户下全部的技术目录
     * @param tenantId
     * @return
     */
    public Set<CategoryEntityV2> getAllTechnicalCategory(String tenantId) {
        try {
            User user = AdminUtils.getUserData();
            List<String> userGroupIds = new ArrayList<>();
            //获取用户组
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            if (!CollectionUtils.isEmpty(userGroups)) {
                userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            }
            return categoryDAO.selectSetByTenantIdAndStatus(tenantId, userGroupIds);
        } catch (AtlasBaseException e) {
            LOG.error("getAllTechnicalCategory exception is {}", e);
        }
        return new HashSet<>();
    }

    /**
     * 删除有父目录，但是父目录不存在的目录
     *
     * @param categoryPrivilegeList
     */
    private void removeNoParentCategory(List<CategoryPrivilege> categoryPrivilegeList) {
        Map<String, String> map = categoryPrivilegeList.stream().collect(HashMap::new, (m, v) -> m.put(v.getGuid(), v.getParentCategoryGuid()), HashMap::putAll);
        categoryPrivilegeList.removeIf(categoryPrivilege -> this.checkParentIfExist(map, categoryPrivilege.getParentCategoryGuid(), categoryPrivilegeList));
    }

    private boolean checkParentIfExist(Map<String, String> map, String parentId, List<CategoryPrivilege> categoryPrivilegeList) {
        if (StringUtils.isEmpty(parentId)) {
            return false;
        }
        if (map.containsKey(parentId)) {
            Optional<CategoryPrivilege> result = categoryPrivilegeList.stream().filter(c -> parentId.equals(c.getGuid())).findFirst();
            if (result.isPresent()) {
                return checkParentIfExist(map, result.get().getParentCategoryGuid(), categoryPrivilegeList);
            }
        }
        return true;
    }

    private void updateParentCategory(List<CategoryPrivilege> categoryPrivilegeList) {
        for (CategoryPrivilege categoryPrivilege : categoryPrivilegeList) {
            if (!categoryPrivilege.getPrivilege().isDelete()) {
                if (StringUtils.isNotBlank(categoryPrivilege.getParentCategoryGuid())) {
                    updateParentCategoryAttribute(categoryPrivilege.getParentCategoryGuid(), categoryPrivilegeList);
                }
            }
        }
    }

    /**
     * 递归设置父目录的删除属性
     * @param parentGuid
     * @param categoryPrivilegeList
     */
    private void updateParentCategoryAttribute(String parentGuid, List<CategoryPrivilege> categoryPrivilegeList) {
        for (CategoryPrivilege privilege : categoryPrivilegeList) {
            if (parentGuid.equals(privilege.getGuid())) {
                privilege.getPrivilege().setDelete(false);
                if (StringUtils.isNotBlank(privilege.getParentCategoryGuid())) {
                    updateParentCategoryAttribute(privilege.getParentCategoryGuid(), categoryPrivilegeList);
                }
                break;
            }
        }
    }

    //多租户
    public List<CategoryPrivilege> getAllByUserGroup(int type, String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            List<CategoryPrivilege> valueList = null;
            List<Module> modules = tenantService.getModule(tenantId);
            if (type != 1 && type != 0 && type != 5) {
                valueList = userGroupService.getUserCategory(null, type, modules, tenantId);
            } else {
                //是否无用户组
                boolean isUserGroup = userGroups == null || userGroups.size() == 0;
                //目录管理权限
                boolean isAdmin = modules.stream().anyMatch(module -> ModuleEnum.AUTHORIZATION.getId() == module.getModuleId());
                //无用户组并且有目录管理权限
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
            if (type == 5 && !CollectionUtils.isEmpty(valueList)) {
                valueList.forEach(x -> {
                    if (CategoryUtil.indexFieldId.equals(x.getGuid())) {
                        x.setPrivilege(new CategoryPrivilege.Privilege(false, false, true, false, true, false, false, false, false, false));
                    }
                });
            }
            return valueList;
        } catch (MyBatisSystemException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 获取源信息登记时的技术目录
     * @param tenantId
     * @return
     */
    public List<CategoryPrivilege> getSourceInfoTechnicalCategory(String tenantId, SourceInfoOperation operation) {
        List<CategoryPrivilege> categoryPrivilegeList = new ArrayList<>();
        try {
            User user = AdminUtils.getUserData();
            List<String> userGroupIds = new ArrayList<>();
            //获取用户组
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            if (!CollectionUtils.isEmpty(userGroups)) {
                userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            }
            categoryPrivilegeList = categoryDAO.selectListByTenantIdAndStatus(tenantId, user.getUserId(), userGroupIds,SourceInfoOperation.UPDATE.equals(operation)?UPDATE_MAX_CATEGORY_LEVEL:CREATE_MAX_CATEGORY_LEVEL);
            for (CategoryPrivilege categoryPrivilege : categoryPrivilegeList) {
                CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege();
                privilege.setHide(false);
                privilege.setAsh(false);
                privilege.setEdit(false);
                privilege.setAddSibling(false);
                privilege.setAddChildren(false);
                privilege.setDelete(false);
                categoryPrivilege.setPrivilege(privilege);
            }
            if (SourceInfoOperation.CREATE.equals(operation)){
                List<String> strings = sourceInfoDAO.selectCategoryListByTenantId(tenantId);
                if (CollectionUtils.isEmpty(strings)) {
                    return categoryPrivilegeList;
                }
                Iterator<CategoryPrivilege> iter = categoryPrivilegeList.iterator();
                while (iter.hasNext()) {
                    CategoryPrivilege categoryPrivilege = iter.next();
                    if (strings.contains(categoryPrivilege.getGuid())) {
                        iter.remove();
                    }
                }
            }
            removeNoParentCategory(categoryPrivilegeList);
        } catch (AtlasBaseException e) {
            LOG.error("getTechnicalCategory exception is {}", e);
        }
        return categoryPrivilegeList;
    }

    public List<CategoryPrivilegeV2> getUserCategories(String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        List<CategoryPrivilegeV2> userCategories = userGroupDAO.getUserCategories(tenantId, user.getUserId());
        if (!CollectionUtils.isEmpty(userCategories)) {
            userCategories.stream().forEach(c -> c.setRead(true));
            List<String> guids = userCategories.stream().map(c -> c.getParentCategoryGuid()).collect(Collectors.toList());
            getParentGuids(userCategories, guids, tenantId);
        }
        return userCategories;
    }

    public void getParentGuids(List<CategoryPrivilegeV2> userCategories, List<String> guids, String tenantId) {
        guids = guids.stream().filter(g -> g != null).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(guids)) {
            return;
        }
        for (CategoryPrivilegeV2 userCategory : userCategories) {
            if (guids.contains(userCategory.getGuid())) {
                guids.remove(userCategory.getGuid());
            }
        }
        if (CollectionUtils.isEmpty(guids)) {
            return;
        }
        List<CategoryPrivilegeV2> parentCategories = userGroupDAO.getUserCategoriesByIds(guids, tenantId);
        parentCategories.stream().forEach(c -> c.setRead(false));
        userCategories.addAll(parentCategories);
        guids = parentCategories.stream().map(p -> p.getParentCategoryGuid()).collect(Collectors.toList());
        getParentGuids(userCategories, guids, tenantId);
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
            String creatorId = Boolean.TRUE.equals(ParamUtil.isNull(info.getCreator()))?AdminUtils.getUserData().getUserId():info.getCreator();
            if (Objects.isNull(name)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "目录名不能为空");
            }
            //判断独立部署和多租户
            List<Module> moduleByUserId = tenantService.getModule(tenantId);
            List<Integer> modules = new ArrayList<>();
            for (Module module : moduleByUserId) {
                modules.add(module.getModuleId());
            }
            //guid
            entity.setGuid(newCategoryGuid);
            //name
            entity.setName(name);
            entity.setPrivateStatus(info.getPrivateStatus()==null?CategoryPrivateStatus.PRIVATE:info.getPrivateStatus());
            //创建人
            entity.setCreator(creatorId);
            //createtime
            entity.setCreateTime(io.zeta.metaspace.utils.DateUtils.currentTimestamp());
            //description
            entity.setDescription(info.getDescription());
            entity.setCategoryType(type);
            entity.setSafe(info.getSafe());
            if (StringUtils.isEmpty(entity.getSafe())) {
                entity.setSafe("1");
            }
            entity.setCode(info.getCode());
            String parentCategoryGuid = categoryDAO.getParentIdByGuid(currentCategoryGuid,tenantId);
            if (type == technicalType) {
                int currentCategorySort = 0;
                if (currentCategoryGuid != null) {
                    currentCategorySort = categoryDAO.getCategorySortById(currentCategoryGuid, tenantId);
                }
                if ("up".equals(info.getDirection())) {
                    categoryDao.updateSort(currentCategorySort, parentCategoryGuid, tenantId);
                    entity.setSort(currentCategorySort);
                } else if ("down".equals(info.getDirection())) {
                    categoryDao.updateSort(currentCategorySort + 1, parentCategoryGuid, tenantId);
                    entity.setSort(currentCategorySort + 1);
                } else {
                    int maxSort = categoryDao.getMaxSortByParentGuid(info.getParentCategoryGuid(), tenantId);
                    entity.setSort(maxSort);
                }
            }
            //创建一级目录
            if (StringUtils.isEmpty(currentCategoryGuid)) {
                if (type == 5) {
                    //判断一级指标域是否已经存在
                    Set<CategoryEntityV2> ces = categoryDao.getCategoryByNameOrCode(tenantId, type, info.getName(), info.getCode(), 1);
                    if (ces != null && ces.size() > 0) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前指标域名称或编码已被使用");
                    }
                }

                boolean bool = type == 1 || type == 0 || type == 5;
                if (!modules.contains(ModuleEnum.AUTHORIZATION.getId()) && bool) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有目录授权模块权限，无法创建一级目录");
                }

                CategoryPrivilege oneLevelCategory = null;
                if (categoryDao.ifExistCategory(type, tenantId) > 0) {
                    oneLevelCategory = createOneLevelCategory(entity, type, tenantId);
                } else {
                    oneLevelCategory = createFirstCategory(entity, type, tenantId);
                }
                if (authorized) {
                    CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, false, false, true, false, false);
                    oneLevelCategory.setPrivilege(privilege);
                }
                if (!Objects.isNull(oneLevelCategory)) {
                    oneLevelCategory.setCode(entity.getCode());
                }
                if (CategoryPrivateStatus.PRIVATE.equals(entity.getPrivateStatus())){
                    List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(entity.getCreator(), tenantId).stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
                    if (userGroupIds!=null && !userGroupIds.isEmpty()) {
                        userGroupDAO.insertGroupRelations(userGroupIds, entity.getGuid(), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
                    }
                }
                return oneLevelCategory;
            }
            if (Objects.isNull(categoryDao.queryByGuidV2(currentCategoryGuid, tenantId))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录已被删除，请刷新后重新操作");
            }
            if (type == 5) {
                //判断二级指标域是否已经存在
                Set<CategoryEntityV2> ces = categoryDao.getCategoryByNameOrCode(tenantId, type, info.getName(), info.getCode(), 2);
                if (ces != null && ces.size() > 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录名称或编码已被使用");
                }
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
                boolean isPrivilege = !userGroupService.isPrivilegeCategory(creatorId, newCategoryGuid, tenantId, type);
                boolean typeBoolean = type == 1 ;
                if (isAdmin) {
                    privilege.adminPrivilege(returnEntity.getGuid());
                }
                if (typeBoolean && isAdmin && isPrivilege) {
                    privilege.setAsh(true);
                }
                if (type == technicalType) {
                    privilege.setDeleteRelation(false);
                    privilege.setAsh(false);
                    privilege.setAddChildren(true);
                    privilege.setCreateRelation(true);
                    privilege.setDelete(false);
                    privilege.setEdit(false);
                }
            }
            if (CategoryPrivateStatus.PRIVATE.equals(entity.getPrivateStatus())){
                List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(entity.getCreator(), tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());
                if (userGroupIds!=null && !userGroupIds.isEmpty()) {
                    userGroupDAO.insertGroupRelations(userGroupIds, entity.getGuid(), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
                }
            }
            if (type == 5) {
                privilege.setAddChildren(false);
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
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
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
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        } else {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        if (type == technicalType) {
            privilege.setDeleteRelation(false);
            privilege.setAsh(false);
            privilege.setAddChildren(true);
            privilege.setCreateRelation(true);
            privilege.setDelete(false);
            privilege.setEdit(false);
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
            privilege.setAsh(false);
            privilege.setAddChildren(true);
            privilege.setCreateRelation(true);
            privilege.setDelete(false);
            privilege.setEdit(false);
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

        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
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

        if(Boolean.FALSE.equals(this.removeSourceInfo(categoryIds,tenantId,guid))){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录已关联源信息登记，无法删除");
        }
        categoryIds.add(guid);
        int item = 0;
        if (type == 1) {
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
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(upBrotherCategoryGuid)) {
            categoryDao.updateDownBrotherCategoryGuid(upBrotherCategoryGuid, downBrotherCategoryGuid, tenantId);
        }
        if (StringUtils.isNotEmpty(downBrotherCategoryGuid)) {
            categoryDao.updateUpBrotherCategoryGuid(downBrotherCategoryGuid, upBrotherCategoryGuid, tenantId);
        }
        userGroupDAO.deleteCategoryGroupRelationByCategory(guid);
        int category = categoryDao.deleteCategoryByIds(categoryIds, tenantId);
        CategoryDeleteReturn deleteReturn = new CategoryDeleteReturn();
        deleteReturn.setCategory(category);
        deleteReturn.setItem(item);
        return deleteReturn;
    }

    /**
     * 删除源信息登记的数据
     * @param categoryIds
     * @param tenantId
     * @param guid
     */
    private Boolean removeSourceInfo(List<String> categoryIds, String tenantId, String guid) {
        if (categoryIds==null||categoryIds.size()==0){
            return Boolean.TRUE;
        }
        List<DatabaseInfoForCategory> dif = databaseInfoDAO.getDatabaseInfoByCategoryId(categoryIds, tenantId, guid);
        if (Boolean.FALSE.equals(ParamUtil.isNull(dif))&&!dif.get(0).getCategoryId().equals(guid)) {
                return Boolean.FALSE;
        }
        return Boolean.TRUE;
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

            if (type == 5) {
                Set<CategoryEntityV2> otherCategorys = categoryDao.getOtherCategoryByCodeOrName(tenantId, info.getGuid(), type, info.getName(), info.getCode(), currentEntity.getLevel());
                if (otherCategorys != null && otherCategorys.size() > 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的指标域编码或指标域名称");
                }
            } else {
                int count = categoryDao.querySameNameNum(name, currentEntity.getParentCategoryGuid(), type, tenantId);
                if (count > 0 && !currentEntity.getName().equals(info.getName())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在相同的目录名");
                }
            }

            CategoryEntity entity = new CategoryEntity();
            entity.setGuid(info.getGuid());
            entity.setName(info.getName());
            entity.setQualifiedName(qualifiedName.toString());
            entity.setDescription(info.getDescription());
            entity.setSafe(info.getSafe());
            entity.setCode(info.getCode());
            User user = AdminUtils.getUserData();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            categoryDao.updateCategoryInfo(entity, tenantId, user.getUserId(), timestamp);
            return "success";
        } catch (SQLException e) {
            LOG.error("数据库服务异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            LOG.error("更新目录失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
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
            User user = AdminUtils.getUserData();
            List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            List<String> groupIds = groups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            relations = relationDao.queryRelationByCategoryGuidV2(categoryGuid, limit, offset, tenantId);
            if (!CollectionUtils.isEmpty(relations)) {
                for (RelationEntityV2 entity : relations) {
                    String tableGuid = entity.getTableGuid();
                    List<Tag> tableTageList = tableTagDAO.getTable2Tag(tableGuid, tenantId);
                    List<String> tableTagNameList = tableTageList.stream().map(tag -> tag.getTagName()).collect(Collectors.toList());
                    entity.setTableTagList(tableTagNameList);
                    List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                    entity.setDataOwner(ownerHeaders);
                    String sourceId = entity.getSourceId();
                    entity.setJump(true);
                    //用户组新增数据库权限，技术目录跳转到元数据管理，判断如果当前表所在数据库未被赋权给用户组，不允许跳转
                    if (!"hive".equals(sourceId)) {
                        String dbId = entity.getDbId();
                        int cnt = userGroupDAO.getDatabaseIdNum(groupIds, sourceId, dbId);
                        if (cnt == 0) {
                            entity.setJump(false);
                        }
                    }
                }
                getPathByCategoryId(relations, tenantId, categoryGuid);
                pageResult.setCurrentSize(relations.size());
                pageResult.setLists(relations);
                pageResult.setTotalSize(relations.get(0).getTotal());
            } else {
                pageResult.setCurrentSize(0);
                pageResult.setLists(relations);
                pageResult.setTotalSize(0);
            }
            return pageResult;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取关联失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关联失败");
        }
    }


    /**
     * 获取关联关系-全局
     * @param categoryGuid
     * @param query
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<RelationEntityV2> getRelationsByCategoryGuidGlobal(String categoryGuid, RelationQuery query, String tenantId) throws AtlasBaseException {
        try {
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            List<RelationEntityV2> relations = relationDao.queryRelationByCategoryGuidV2(categoryGuid, limit, offset, tenantId);
            if (CollectionUtils.isEmpty(relations)) {
                pageResult.setCurrentSize(0);
                pageResult.setLists(relations);
                pageResult.setTotalSize(0);
                return pageResult;
            }
            for (RelationEntityV2 entity : relations) {
                String tableGuid = entity.getTableGuid();
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(tableGuid, tenantId);
                List<String> tableTagNameList = tableTageList.stream().map(tag -> tag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
                entity.setJump(true);
            }
            getPathByCategoryId(relations, tenantId, categoryGuid);
            pageResult.setCurrentSize(relations.size());
            pageResult.setLists(relations);
            pageResult.setTotalSize(relations.get(0).getTotal());
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
            int totalNum = 0;
            String tableName = query.getFilterTableName();
            if (StringUtils.isNotBlank(tableName)) {
                tableName = tableName.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }
            List<RelationEntityV2> relations = relationDao.queryRelationByCategoryGuidFilterV2(categoryGuid, tenantId, limit, offset, tableName);
            if (!CollectionUtils.isEmpty(relations)) {
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
     * 模糊查询获取库或者表
     *
     * @param tenantId
     * @param name     名称
     * @param offset
     * @param limit
     * @param type     db 数据库 table 数据表
     */
    public PageResult<Database> getDbListLikeName(String tenantId, String name, Long offset, Long limit, String type) {
        try {
            List<String> databases = tenantService.getDatabase(tenantId);
            List<RelationEntityV2> relations = new ArrayList<>();
            List<Database> databaseList = new ArrayList<>();
            if ("db".equals(type)) {
                relations = relationDao.selectListByDbName(name, tenantId, limit, offset, databases);
            } else {
                relations = relationDao.selectListByTableName(name, tenantId, limit, offset, databases);
            }
            PageResult<Database> databasePageResult = new PageResult<>();
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(relations)) {
                return databasePageResult;
            }
            for (RelationEntityV2 relation : relations) {
                Database database = new Database();
                database.setDatabaseId(relation.getTableGuid());
                database.setDatabaseName(relation.getTableName());
                database.setStatus(relation.getStatus());
                databaseList.add(database);
            }
            databasePageResult.setLists(databaseList);
            databasePageResult.setCurrentSize(databaseList.size());
            databasePageResult.setTotalSize(relations.get(0).getTotal());
            databasePageResult.setOffset(offset);
            return databasePageResult;
        } catch (Exception e) {
            LOG.error("getDbListLikeName exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, "获取数据列表失败");
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
            if (StringUtils.isNotEmpty(tableName)) {
                tableName = tableName.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }
            if (StringUtils.isNotEmpty(tag)) {
                tag = tag.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }

            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            List<String> groupIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(userGroups)) {
                groupIds = userGroups.stream().map(x -> x.getId()).distinct().collect(Collectors.toList());
            }
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

            if (databases != null && databases.size() != 0 && categoryIds.size() != 0) {
                list = relationDao.queryByTableNameV2(tableName, tag, categoryIds, limit, offset, databases, tenantId);
            }
            //tag
            list.forEach(entity -> {
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(entity.getTableGuid(), tenantId);
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
                String sourceId = entity.getSourceId();
                entity.setJump(true);
                //用户组新增数据库权限，技术目录跳转到元数据管理，判断如果当前表所在数据库未被赋权给用户组，不允许跳转
                if (StringUtils.isNotBlank(sourceId)) {
                    String dbId = entity.getDbId();
                    int cnt = userGroupDAO.getDatabaseIdNum(groupIds, sourceId, dbId);
                    if (cnt == 0) {
                        entity.setJump(false);
                    }
                }
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

    public PageResult<RelationEntityV2> getRelationsByTableNameGeneral(RelationQuery query) throws AtlasBaseException {
        try {
            List<String> categoryIds = new ArrayList<>();
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            int limit = query.getLimit();
            int offset = query.getOffset();
            List<RelationEntityV2> list = new ArrayList<>();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if (StringUtils.isNotEmpty(tableName)) {
                tableName = tableName.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }
            if (StringUtils.isNotEmpty(tag)) {
                tag = tag.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }
            User user = AdminUtils.getUserData();
            //获取用户组
            List<UserGroup> userGroups = userGroupDAO.selectListByUsersId(user.getUserId());
            List<String> userGroupIds = userGroups.stream().map(userGroup -> userGroup.getId()).collect(Collectors.toList());
            Set<CategoryEntityV2> categoryEntityV2s = categoryDAO.selectListByStatus(user.getUserId(), userGroupIds, 0);
            categoryEntityV2s.forEach(categoryEntityV2 -> categoryIds.add(categoryEntityV2.getGuid()));
            if (!CollectionUtils.isEmpty(categoryIds)) {
                list = relationDao.queryByTableNameV2General(tableName, tag, categoryIds, limit, offset);
            }
            //tag
            list.forEach(entity -> {
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(entity.getTableGuid(), entity.getTenantId());
                List<String> tableTagNameList = tableTageList.stream().map(tableTag -> tableTag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
            });
            //path
            getPathGlobal(list);
            //dataOwner
            for (RelationEntityV2 entity : list) {
                String tableGuid = entity.getTableGuid();
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
                String sourceId = entity.getSourceId();
                entity.setJump(true);
                //用户组新增数据库权限，技术目录跳转到元数据管理，判断如果当前表所在数据库未被赋权给用户组，不允许跳转
                if (StringUtils.isNotBlank(sourceId)) {
                    String dbId = entity.getDbId();
                    int cnt = userGroupDAO.getDatabaseIdNum(userGroupIds, sourceId, dbId);
                    if (cnt == 0) {
                        entity.setJump(false);
                    }
                }
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


    public PageResult<RelationEntityV2> getRelationsByTableNameGlobal(RelationQuery query) throws AtlasBaseException {
        try {
            String tableName = query.getFilterTableName();
            String tag = query.getTag();
            int limit = query.getLimit();
            int offset = query.getOffset();
            PageResult<RelationEntityV2> pageResult = new PageResult<>();
            if (StringUtils.isNotEmpty(tableName)) {
                tableName = tableName.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }
            if (StringUtils.isNotEmpty(tag)) {
                tag = tag.replaceAll("%", "\\\\%").replaceAll("_", "\\\\_");
            }

            List<RelationEntityV2> list = relationDao.queryByTableNameV2Global(tableName, tag, limit, offset);
            if(CollectionUtils.isEmpty(list)){
                pageResult.setCurrentSize(0);
                pageResult.setLists(new ArrayList<>());
                pageResult.setTotalSize(0);
                return pageResult;
            }
            //tag
            list.forEach(entity -> {
                List<Tag> tableTageList = tableTagDAO.getTable2Tag(entity.getTableGuid(), entity.getTenantId());
                List<String> tableTagNameList = tableTageList.stream().map(tableTag -> tableTag.getTagName()).collect(Collectors.toList());
                entity.setTableTagList(tableTagNameList);
            });

            //path
            getPathGlobal(list);
            //dataOwner
            for (RelationEntityV2 entity : list) {
                String tableGuid = entity.getTableGuid();
                List<DataOwnerHeader> ownerHeaders = tableDAO.getDataOwnerList(tableGuid);
                entity.setDataOwner(ownerHeaders);
                entity.setJump(true);
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
                list = relationDao.queryByTableNameFilterV2(tenantId, tableName, tag, categoryIds, limit, offset, databases);
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

    public void getPathGlobal(List<RelationEntityV2> list) throws AtlasBaseException {
        for (RelationEntityV2 entity : list) {
            String path = CategoryRelationUtils.getPath(entity.getCategoryGuid(), entity.getTenantId());
            entity.setPath(path);
        }
    }

    /**
     * 根据指定目录ID获取路径
     * @param list
     * @param tenantId
     * @param categoryId
     * @throws AtlasBaseException
     */
    public void getPathByCategoryId(List<RelationEntityV2> list, String tenantId, String categoryId) throws AtlasBaseException {
        String path = CategoryRelationUtils.getPath(categoryId, tenantId);
        for (RelationEntityV2 entity : list) {
            entity.setPath(path);
        }
    }

    public void updateStatus(List<AtlasEntity> entities) {

        List<String> tableStatus = new ArrayList<>();
        List<String> databaseStatus = new ArrayList<>();
        List<String> columnStatus = new ArrayList<>();
        for (AtlasEntity entity : entities) {
            String guid = entity.getGuid();
            String typeName = entity.getTypeName();
            if (typeName.contains("_table")) {
                tableStatus.add(guid);
            }
            if (typeName.contains("_db")) {
                databaseStatus.add(guid);
            }
            if (typeName.contains("_column")) {
                columnStatus.add(guid);
            }
        }

        if (!CollectionUtils.isEmpty(databaseStatus)) {
            String databaseStatusStr = StringUtils.join(databaseStatus, "','");
            dbDAO.updateDatabaseStatusBatch(databaseStatusStr, "DELETED");
            dbDAO.deleteSourceDbRelationId(databaseStatusStr);
            List<String> tableGuids = tableDAO.getTableGuidByDataBaseGuids(databaseStatusStr);
            if(!CollectionUtils.isEmpty(tableGuids)){
                tableDAO.updateTableDatabaseStatusBatch(databaseStatusStr,"DELETED");
                tableStatus.retainAll(tableGuids);
                tableStatus.addAll(tableGuids);
            }
        }
        if (!CollectionUtils.isEmpty(tableStatus)) {
            String tableStatusStr = StringUtils.join(tableStatus, "','");
            relationDao.updateTableStatusBatch(tableStatusStr, "DELETED");
            columnDAO.updateColumnStatusByTableGuids(tableStatusStr, "DELETED");
        }

        if (!CollectionUtils.isEmpty(columnStatus)) {
            String columnStatusStr = StringUtils.join(columnStatus, "','");
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
            Map<String, Object> headerMap = Maps.newHashMap();
            headerMap.put(Constants.TICKET_KEY, AdminUtils.getSSOTicket());
            while (retryCount < retries) {
                String res = OKHttpClient.doPut(mobiusURL, jsonStr, headerMap);
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
            if (!CollectionUtils.isEmpty(insertList)) {
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

    /**
     * sql语句分为输入和输出型，只有输出型的表和字段才会视为修改 比如：create table a as SELECT * from b ，那么只有a表才会修改，b表不变
     * @param entity
     * @return
     */
    public Boolean getOutputFromProcesses(AtlasEntity entity) {
        try {
            if(entity.getRelationshipAttributes() == null){
                return false;
            }
//            LOG.info("inputToProcesses is {}", entity.getRelationshipAttributes().get("inputToProcesses"));
//            LOG.info("outputFromProcesses is {}", entity.getRelationshipAttributes().get("outputFromProcesses"));
            List<Object> input = (List<Object>) entity.getRelationshipAttributes().get("inputToProcesses");
            List<Object> output = (List<Object>) entity.getRelationshipAttributes().get("outputFromProcesses");
            if (CollectionUtils.isEmpty(input) && !CollectionUtils.isEmpty(output)) {
                return false;
            }
        } catch (Exception e) {
            LOG.error("getOutputFromProcesses exception is {}", e);
        }
        return true;
    }

    /**
     * HIVE数据-检查AtlasEntity中输入和输出是否全是空
     * @param entities
     * @return
     */
    public Boolean getHiveAtlasEntityAll(List<AtlasEntity> entities){
        int i = 0;
        for (AtlasEntity entity : entities) {
            //当执行删表语句时，关联关系为空
            if(entity.getRelationshipAttributes() == null){
                return false;
            }
            List<Object> input = (List<Object>) entity.getRelationshipAttributes().get("inputToProcesses");
            List<Object> output = (List<Object>) entity.getRelationshipAttributes().get("outputFromProcesses");
            if (CollectionUtils.isEmpty(input) && CollectionUtils.isEmpty(output)) {
                i++;
            }
        }
        LOG.debug("getHiveAtlasEntityAll i = {},entities is {}", i, entities.size());
        if(entities.size() == i){
            return false;
        }
        return true;
    }


    public void addEntity(List<AtlasEntity> entities, SyncTaskDefinition definition, KafkaConnector.Config config) {
        try {
            createOrUpdateEntities(entities, definition, config, false);
        } catch (Exception e) {
            LOG.error("添加entity失败", e);
        }
    }


    private synchronized void addOrUpdateColumn(Column column){
        Column c = columnDAO.getColumnInfoByGuid(column.getColumnId());
        if(null == c){
            columnDAO.addColumn(column);
        }else{
            columnDAO.updateColumnInfo(column);
        }
    }


    private Column getAndRemoveColumn(AtlasEntity entity, String typeKey) {
        AtlasRelatedObjectId table = (AtlasRelatedObjectId) entity.getRelationshipAttribute("table");
        String tableGuid = table.getGuid();
        AtlasEntity.AtlasEntityWithExtInfo info = atlasEntityStore.getById(table.getGuid());
        String guid = entity.getGuid();
        String name = entity.getAttribute("name").toString();
        String columnGuid = columnDAO.getColumnGuid(tableGuid, name);
        if (null != columnGuid) {
            columnDAO.deleteColumn(columnGuid);
        }
        String type = entity.getAttribute(typeKey).toString();
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
            column.setDescription(comment.toString());
        }
        List<String> partitionKeys = EntityUtil.extractPartitionKeyInfo(info.getEntity());
        if (!CollectionUtils.isEmpty(partitionKeys) && partitionKeys.contains(guid)) {
            column.setPartitionKey(true);
        }else{
            column.setPartitionKey(false);
        }
        return column;
    }

    private TableInfo getTableInfo(AtlasEntity entity) {
        String guid = entity.getGuid();
        String qualifiedName = getEntityAttribute(entity, "qualifiedName");
        LOG.info("组装表信息，guid:{},qualifiedName:{} ", guid,qualifiedName);
        String name = getEntityAttribute(entity, "name");
        String owner = getEntityAttribute(entity, "owner");
        TableInfo tableInfo = new TableInfo();
        tableInfo.setType(getTableType(entity));
        tableInfo.setOwner(owner);
        tableInfo.setTableGuid(guid);
        tableInfo.setTableName(name);
        tableInfo.setCreateTime(formatDate(entity.getAttribute("createTime")));
        tableInfo.setStatus(entity.getStatus().name());
        AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
        tableInfo.setDatabaseGuid(relatedDB.getGuid());
        tableInfo.setDbName(relatedDB.getDisplayText());
        if(null != relatedDB.getEntityStatus()){
            tableInfo.setDatabaseStatus(relatedDB.getEntityStatus().name());
        }
        tableInfo.setDescription(getEntityAttribute(entity, "comment"));
        return tableInfo;
    }

    private String getTableType(AtlasEntity entity){
        String type = getEntityAttribute(entity, "tableType");
        if(StringUtils.isBlank(type)){
            return "table";
        }
        if("VIRTUAL_VIEW".equalsIgnoreCase(type)){
            return "view";
        }
        return "table";
    }

    private String formatDate(Object createTime){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(createTime);
        } catch (Exception e) {
            LOG.error("createTime is {},exception is {}", createTime, e);
        }
        return "";
    }

    public void addOrUpdateDb(Database dbInfo, SyncTaskDefinition definition) {
        if (StringUtils.isBlank(dbInfo.getInstanceId()) || "UNKOWN".equalsIgnoreCase(dbInfo.getInstanceId())) {
            return;
        }
        dbDAO.deleteIfExitDbByName(dbInfo.getDatabaseId(), dbInfo.getInstanceId(), dbInfo.getDatabaseName());
        Database db = dbDAO.getDb(dbInfo.getDatabaseId());
        if (null != db) {
            dbDAO.updateDb(dbInfo);
        } else {
            dbDAO.insertDb(dbInfo);
        }
        if (null != definition && "ACTIVE".equalsIgnoreCase(dbInfo.getStatus())) {
            String dataSourceId = definition.getDataSourceId();
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(dataSourceId);
            if (dataSourceInfo == null || dbInfo == null) {
                return;
            }
            //不是采集所有的数据库，并且当前数据库名不在任务中，则不往source_db表添加数据
            if (!definition.isSyncAll() && !definition.getSchemas().contains(dbInfo.getDatabaseName())) {
                return;
            }
            String sourceDbRelationId = dbDAO.getSourceDbRelationId(dbInfo.getDatabaseId(), dataSourceInfo.getSourceId());
            if (null == sourceDbRelationId) {
                dbDAO.insertSourceDbRelation(UUID.randomUUID().toString(), dbInfo.getDatabaseId(), dataSourceInfo.getSourceId());
            }
        }
    }

    public void addOrUpdateTable(TableInfo tableInfo, SyncTaskDefinition definition) throws Exception {
        String tableGuid = tableInfo.getTableGuid();
        if ("ACTIVE".equalsIgnoreCase(tableInfo.getStatus())) {
            tableDAO.deleteIfExist(tableGuid, tableInfo.getDatabaseGuid(), tableInfo.getTableName());
        }
        TableInfo table = tableDAO.getTableInfoByTableguid(tableGuid);
        if (null != table) {
            ProxyUtil.getProxy(DataManageService.class).updateTable(tableInfo);
        } else {
            ProxyUtil.getProxy(DataManageService.class).addTable(tableInfo);
        }
    }

    /**
     * 创建表，同时更新衍生表id
     *
     * @param tableInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public void addTable(TableInfo tableInfo) throws Exception {
        tableDAO.addTable(tableInfo);
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDAO.selectByDbAndTableName(tableInfo.getDatabaseGuid(), tableInfo.getTableName());
        if (sourceInfoDeriveTableInfo == null) {
            return;
        }

        businessDAO.updateBusiness2TableByTableGuid(tableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getTableGuid());

        sourceInfoDeriveColumnInfoDAO.updateColumnInfoByTableGuid(tableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getTableGuid());
        sourceInfoDeriveColumnInfoDAO.updateColumnRelationByTableGuid(tableInfo.getTableGuid(), sourceInfoDeriveTableInfo.getTableGuid());

        sourceInfoDeriveTableInfoDAO.updateByDbAndTableName(tableInfo.getDatabaseGuid(), tableInfo.getTableName(), tableInfo.getTableGuid());
    }

    /**
     * 更新表，同时更新衍生表名称
     *
     * @param tableInfo
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateTable(TableInfo tableInfo) throws Exception {
        tableDAO.updateTable(tableInfo);
        SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDAO.selectByTableGuid(tableInfo.getTableGuid());
        if (sourceInfoDeriveTableInfo == null) {
            return;
        }

        // 更新表名、ddl、dml
        String ddl = sourceInfoDeriveTableInfo.getDdl();
        String dml = sourceInfoDeriveTableInfo.getDml();

        ddl = ddl.replace("." + sourceInfoDeriveTableInfo.getTableNameEn(), "." + tableInfo.getTableName());
        dml = dml.replace("insert into " + sourceInfoDeriveTableInfo.getTableNameEn(), "insert into " + tableInfo.getTableName());

        sourceInfoDeriveTableInfoDAO.updateByTableGuid(tableInfo.getTableGuid(), tableInfo.getTableName(), ddl, dml);
    }

    private Database getDbInfo(AtlasEntity entity) {
        String guid = entity.getGuid();
        String qualifiedName = getEntityAttribute(entity, "qualifiedName");
        LOG.info("组装数据库信息，guid:{},qualifiedName:{} ", guid,qualifiedName);
        Database database = new Database();
        String name = getEntityAttribute(entity, "name");
        String owner = getEntityAttribute(entity, "owner");
        database.setOwner(owner);
        database.setDatabaseName(name);
        database.setDatabaseId(guid);
        database.setStatus(entity.getStatus().name());
        database.setDatabaseDescription(getEntityAttribute(entity, "comment"));
        return database;
    }

    private String getInstanceGuid(AtlasEntity entity) {
        String guid = "UNKOWN";
        try{
            AtlasRelatedObjectId relatedDB = getRelatedInstance(entity);
            guid = relatedDB.getGuid();
        }catch (Exception e){
            LOG.warn("查询实体失败：{}" , e.getMessage());
        }
        return guid;
    }

    private String getDbType(SyncTaskDefinition definition, KafkaConnector.Config config) {
        String dbType = null;
        if(definition != null){
            String dataSourceId = definition.getDataSourceId();
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(dataSourceId);
            dbType = dataSourceInfo.getSourceType().toUpperCase();
        }else if(config != null){
            KafkaConnector kafkaConnector = kafkaConnectorDAO.selectConnector(config.getDbIp(), config.getDbPort(), config.getDbName());
            dbType = kafkaConnector.getType();
            config.setDbType(dbType);
        }else {
            dbType = "UN_KNOW";
        }
        return dbType;
    }

    @Transactional(rollbackFor = Exception.class)
    public void supplementTable(String tenantId) throws AtlasBaseException {
        try {
            PageResult<Table> tableNameAndDbNameByQuery;
            List<TableHeader> tables = new ArrayList<>();
            List<String> tableByDB;
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                tableNameAndDbNameByQuery = metaspaceEntityService.getTableNameAndDbNameByQuery("", false, 0, -1);
                tableByDB = tableDAO.getTables();
            } else {
                List<String> dbs = tenantService.getDatabase(tenantId);
                String dbsToString = dbsToString(dbs);
                List<Database> databases = metaspaceEntityService.getAllDBAndTable(dbsToString, 0, -1).getLists();
                if(!CollectionUtils.isEmpty(databases)){
                    databases.forEach(dbInfo -> {
                        dbInfo.setDbType("HIVE");
                        addOrUpdateDb(dbInfo, null);
                        List<TableHeader> tableList = dbInfo.getTableList();
                        if(!CollectionUtils.isEmpty(tableList)){
                            tables.addAll(tableList);
                        }
                    });
                }
                tableByDB = tableDAO.getTableByDB(dbs);
            }
            List<String> tableIds = tables.stream().map(table -> table.getTableId()).collect(Collectors.toList());
            List<String> deleteIds = new ArrayList<>();
            for (String table : tableByDB) {
                if (!tableIds.contains(table)) {
                    deleteIds.add(table);
                }
            }
            for (TableHeader table : tables) {
                String tableId = table.getTableId();
                if (tableDAO.ifTableExists(tableId) == 0) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableGuid(tableId);
                    tableInfo.setTableName(table.getTableName());
                    tableInfo.setCreateTime(table.getCreateTime());
                    tableInfo.setStatus(table.getStatus());
                    tableInfo.setDatabaseGuid(table.getDatabaseId());
                    tableInfo.setDbName(table.getDatabaseName());
                    tableInfo.setDatabaseStatus(table.getDatabaseStatus());
                    tableInfo.setDescription(table.getComment());
                    addOrUpdateTable(tableInfo, null);
                }
            }
            for (String tableGuid : deleteIds) {
                deleteTable(tableGuid);
            }
        } catch (Exception e) {
            LOG.error("补充元数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "补充元数据失败");
        }
    }

    private void deleteTable(String tableGuid) {
        //表详情
        tableDAO.deleteTableInfo(tableGuid);
        //owner
        tableDAO.deleteTableRelatedOwner(tableGuid);
        //business2table
        businessDAO.deleteBusinessRelationByTableGuid(tableGuid);
        //表标签
        tableTagDAO.delAllTable2Tag(tableGuid);
        //唯一信任数据
        businessDAO.removeBusinessTrustTableByTableId(tableGuid);
    }

    public void updateEntityInfo(List<AtlasEntity> entities, SyncTaskDefinition definition, KafkaConnector.Config config) {
        try {
            Configuration configuration = ApplicationProperties.get();
            Boolean enableEmail = configuration.getBoolean("metaspace.mail.enable", false);
            createOrUpdateEntities(entities, definition, config, enableEmail);
        } catch (Exception e) {
            LOG.error("更新tableinfo表失败", e);
        }
    }

    private void createOrUpdateEntities(List<AtlasEntity> entities, SyncTaskDefinition definition, KafkaConnector.Config config, Boolean enableEmail) throws Exception {
        Boolean hiveAtlasEntityAll = getHiveAtlasEntityAll(entities);
        for (AtlasEntity entity : entities) {
            String typeName = entity.getTypeName();
            String dbType = null;
            String instanceGuid = null;
            Database dbInfo;

            switch (typeName){
                case "hive_db":
                    dbType = "HIVE";
                    instanceGuid = "hive";
                case "rdbms_db":
                    if(null == dbType){
                        dbType = getDbType(definition, config);
                    }
                    if(null == instanceGuid){
                        dbType = getDbType(definition, config);
                        instanceGuid = getInstanceGuid(entity);
                    }
                    dbInfo = getDbInfo(entity);
                    dbInfo.setDbType(dbType);
                    dbInfo.setInstanceId(instanceGuid);
                    addOrUpdateDb(dbInfo, definition);
                    break;
                case "rdbms_instance":
                    if(null == dbType){
                        dbType = getDbType(definition, config);
                    }
                    if(null == instanceGuid){
                        dbType = getDbType(definition, config);
                        instanceGuid = getInstanceGuid(entity);
                    }
                    insertOrUpdateDb(entity, dbType, instanceGuid, definition);
                    break;
                case "hive_table":
                    if(getOutputFromProcesses(entity) && hiveAtlasEntityAll){
                        continue;
                    }
                    if (entity.getAttribute("temporary") != null && entity.getAttribute("temporary").toString().equals("true")) {
                        continue;
                    }
                case "rdbms_table":
                    TableInfo tableInfo = getTableInfo(entity);
                    addOrUpdateTable(tableInfo,definition);
                    if (enableEmail) {
                        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();
                        threadPoolExecutor.execute(()->{
                            sendMetadataChangedMail(entity.getGuid());
                        });
                    }
                    break;
                case "hive_column":
                    if(this.getOutputFromProcesses(entity) && hiveAtlasEntityAll){
                        continue;
                    }
                    Column column = getAndRemoveColumn(entity, "type");
                    addOrUpdateColumn(column);
                    break;
                case "rdbms_column":
                    column = getAndRemoveColumn(entity, "data_type");
                    addOrUpdateColumn(column);
                    break;
            }
        }
    }

    /**
     * rdbms元数据采集，添加或更新数据库
     * @param entity
     * @param dbType
     * @param instanceGuid
     * @param definition
     */
    public void insertOrUpdateDb(AtlasEntity entity, String dbType, String instanceGuid, SyncTaskDefinition definition) {
        List<AtlasRelatedObjectId> atlasRelatedObjectIdList = (List<AtlasRelatedObjectId>)  entity.getRelationshipAttributes().get("databases");
        if (CollectionUtils.isEmpty(atlasRelatedObjectIdList)) {
            LOG.warn("数据源下获取不到数据库信息");
            return;
        }
        for (AtlasRelatedObjectId atlasRelatedObjectId : atlasRelatedObjectIdList) {
            String typeName = atlasRelatedObjectId.getTypeName();
            if (!"rdbms_db".equals(typeName)) {
                continue;
            }
            String dbName = atlasRelatedObjectId.getDisplayText();
            String status = atlasRelatedObjectId.getEntityStatus().name();
            String guid = atlasRelatedObjectId.getGuid();
            Database database = new Database();
            database.setDatabaseName(dbName);
            database.setDatabaseId(guid);
            database.setStatus(status);
            database.setDbType(dbType);
            database.setInstanceId(instanceGuid);
            addOrUpdateDb(database, definition);
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

    public AtlasRelatedObjectId getRelatedInstance(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String store = "instance";
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

    /**
     * 异步调用
     * @param tableGuid
     * @throws AtlasBaseException
     */
    public void sendMetadataChangedMail(String tableGuid) throws AtlasBaseException {
        try {
            Table info = tableDAO.getDbAndTableName(tableGuid);
            String sendMessage = "数据库[" + info.getDatabaseName() + "]下的表[" + info.getTableName() + "]元数据发生变更";
            String subject = "元数据变更提醒";
            List<String> emails = userDAO.getUsersEmail(tableGuid);
            sendMail(emails, subject, sendMessage);
        }catch (Exception e) {
            LOG.error("发送邮件失败", e);
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
        Set<CategoryEntityV2> data;
        List<CategoryEntityV2> valueList=new ArrayList<>();
        Map<String,List<CategoryEntityV2>> parent;
        if(categoryType == 0){
            data = dataManageService.getAllTechnicalCategory(tenantId);
            parent=data.stream().collect(Collectors.groupingBy(c -> c.getParentCategoryGuid() +"str"));
            parent.forEach((k,v) -> {
            List<CategoryEntityV2> list=v.stream().sorted(Comparator.comparing(s -> s.getSort())).collect(Collectors.toList());
                parent.put(k,list);
            });
           List<CategoryEntityV2> cateList= parent.get("nullstr");
           for(CategoryEntityV2 c:cateList){
               getCate(valueList,c,parent);
           }
        }else{
            data = categoryDao.getAll(categoryType, tenantId);
            //将目录排序
            parent=data.stream().collect(Collectors.groupingBy(c -> c.getParentCategoryGuid() +"str"));
            parent.forEach((k,v) -> {
            List<CategoryEntityV2> list=new ArrayList<>();
            for(CategoryEntityV2 cv:v){
                if(StringUtils.isBlank(cv.getUpBrotherCategoryGuid())){
                    list.add(cv);
                }
            }
            if(list.size()>0) {
                String id = list.get(0).getGuid();
                String guid=id;
                for (int i = 0; i < v.size(); i++) {
                    for (CategoryEntityV2 cv3 : v) {
                        if (StringUtils.isNotBlank(cv3.getUpBrotherCategoryGuid())) {
                            if (cv3.getUpBrotherCategoryGuid().equals(guid)) {
                                list.add(cv3);
                                guid = cv3.getGuid();
                                break;
                            }
                        }
                    }
                }
            }
             parent.put(k,list);
            });
            List<CategoryEntityV2> cateList= parent.get("nullstr");
            for(CategoryEntityV2 c:cateList){
                getCate(valueList,c,parent);
            }
        }
        Workbook workbook = allData2workbook(userDAO, categoryType, valueList);
        return workbook2file(workbook);
    }

    private List getCate( List<CategoryEntityV2> valueList,CategoryEntityV2 cate,Map<String,List<CategoryEntityV2>> parent){
        valueList.add(cate);
        String guid=cate.getGuid();
        List<CategoryEntityV2> sonCate=parent.get(guid+"str");
        if(!CollectionUtils.isEmpty(sonCate)){
           for( CategoryEntityV2 c:sonCate){
               getCate(valueList,c,parent);
           }
        }
       return valueList;
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
    private Workbook allData2workbook(UserDAO userDAO, int categoryType, List<CategoryEntityV2> list) {

        Workbook workbook = new XSSFWorkbook();
        if (categoryType == 5) {
            List<String> creatorAndUpdaters = list.stream().map(categoryEntityV2 -> {
                List<String> caus = Lists.newArrayList(categoryEntityV2.getCreator(), categoryEntityV2.getUpdater());
                return caus;
            }).flatMap(pList -> pList.stream()).distinct().collect(Collectors.toList());
            Map<String, String> idNameMap = new HashMap<String, String>();
            if (!CollectionUtils.isEmpty(creatorAndUpdaters)) {
                List<User> users = userDAO.getUsersByIds(creatorAndUpdaters);
                if (!CollectionUtils.isEmpty(users)) {
                    idNameMap = users.stream().collect(Collectors.toMap(User::getUserId, User::getUsername));
                }
            }
            Map<String, String> finalIdNameMap = idNameMap;
            List<List<String>> dataList = list.stream().map(categoryEntityV2 -> {
                List<String> data = Lists.newArrayList(categoryEntityV2.getCode(), categoryEntityV2.getName(), categoryEntityV2.getQualifiedName(), categoryEntityV2.getDescription(),
                        finalIdNameMap.get(categoryEntityV2.getCreator()), DateUtils.timestampToString(categoryEntityV2.getCreateTime()), finalIdNameMap.get(categoryEntityV2.getUpdater()),
                        DateUtils.timestampToString(categoryEntityV2.getUpdateTime()));
                return data;
            }).collect(Collectors.toList());
            PoiExcelUtils.createSheet(workbook, "指标域", Lists.newArrayList("编码", "名称", "路径", "描述", "创建人", "创建时间", "更新人", "更新时间"), dataList);
        } else {
            List<List<String>> dataList = list.stream().map(categoryEntityV2 -> {
                List<String> data = Lists.newArrayList(categoryEntityV2.getGuid(), categoryEntityV2.getName(), categoryEntityV2.getDescription(), categoryEntityV2.getUpBrotherCategoryGuid(), categoryEntityV2.getDownBrotherCategoryGuid(), categoryEntityV2.getParentCategoryGuid(), categoryEntityV2.getQualifiedName(), new Integer(categoryEntityV2.getLevel()).toString());
                return data;
            }).collect(Collectors.toList());
            PoiExcelUtils.createSheet(workbook, "目录", Lists.newArrayList("目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别"), dataList);
        }
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
        Timestamp timestamp = io.zeta.metaspace.utils.DateUtils.currentTimestamp();
        User user = AdminUtils.getUserData();
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
            categoryEntityV2.setCreateTime(timestamp);
            categoryEntityV2.setCreator(user.getUserId());

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
        if (type == technicalType) {
            AtomicInteger maxSort = new AtomicInteger(categoryDao.getMaxSortByParentGuid(parentCategoryGuid, tenantId));
            categoryEntityV2s.forEach(c -> {
                c.setPrivateStatus(CategoryPrivateStatus.PRIVATE);
                c.setSort(maxSort.get());
                maxSort.getAndIncrement();
            });
        }
        categoryDao.addAll(categoryEntityV2s, tenantId);
        if (type == 3 || type == 4) {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        } else if (level != 0 || !authorized) {
            fileInputStream.delete();
            GroupPrivilege groupPrivilege = new GroupPrivilege();
            groupPrivilege.setRead(true);
            groupPrivilege.setEditCategory(true);
            groupPrivilege.setEditItem(true);
            parentPrivilege.forEach(category -> {
                groupPrivilege.setRead(category.getRead() || groupPrivilege.getRead());
                groupPrivilege.setEditCategory(category.getEditCategory() || groupPrivilege.getEditCategory());
                groupPrivilege.setEditItem(category.getEditItem() || groupPrivilege.getEditItem());
            });
            privilege = getCategoryPrivilege(groupPrivilege.getRead(), groupPrivilege.getEditItem(), groupPrivilege.getEditCategory());
        } else if (authorized) {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        } else {
            privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true, false);
        }
        List<CategoryPrivilege> categoryPrivileges = new ArrayList<>();
        for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2s) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(categoryEntityV2);
            categoryPrivilege.setPrivilege(privilege);
            categoryPrivileges.add(categoryPrivilege);
        }
            List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(AdminUtils.getUserData().getUserId(), tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());
            if (userGroupIds!=null && !userGroupIds.isEmpty()) {
                for (CategoryEntityV2 categoryEntityV2 : categoryEntityV2s) {
                    userGroupDAO.insertGroupRelations(userGroupIds, categoryEntityV2.getGuid(), Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
                }
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
        } else {
            privilege.setAddSibling(false);
            privilege.setDelete(false);
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
     * 文件转化为指标域
     *
     * @param file
     * @return
     * @throws Exception
     */
    private List<IndexFieldExport> file2IndexField(File file) throws Exception {
        Map<String, List<String>> names = new HashMap<>();
        names.put(null, new ArrayList<>());
        List<String> codes = new ArrayList<>();
        List<IndexFieldExport> indexFieldExports = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        //文件格式校验
        Row first = sheet.getRow(0);
        ArrayList<String> strings = Lists.newArrayList("注释", "编码", "名称", "父指标域编码", "描述");

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

        int rowNum = sheet.getLastRowNum() + 1;
        for (int i = 3; i < rowNum; i++) {
            Row row = sheet.getRow(i);
            IndexFieldExport indexFieldExport = new IndexFieldExport();
            Cell codeCell = row.getCell(1);
            if (Objects.isNull(codeCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域编码不能为空");
            } else {
                codeCell.setCellType(CellType.STRING);
                if (!codeCell.getStringCellValue().matches("^[0-9A-Za-z]+$")) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域编码仅支持英文、数字");
                }
                if (codeCell.getStringCellValue().length() > 128) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域编码长度不能超过128个字符");
                }
            }
            if (codes.contains(codeCell.getStringCellValue())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中存在相同指标域编码");
            }
            Cell nameCell = row.getCell(2);
            if (Objects.isNull(nameCell)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域名称不能为空");
            } else {
                nameCell.setCellType(CellType.STRING);
                if (nameCell.getStringCellValue().length() > 128) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域名称长度不能超过128个字符");
                }
            }
            Cell parentCode = row.getCell(3);
            if (Objects.isNull(parentCode)) {
                //一级指标域
                List<String> nameList = names.get(null);
                if (nameList.contains(nameCell.getStringCellValue())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中的一级指标域中存在重名");
                } else {
                    nameList.add(nameCell.getStringCellValue());
                }
            } else {
                parentCode.setCellType(CellType.STRING);
                //二级指标域
                String pc = parentCode.getStringCellValue();
                if (!pc.matches("^[0-9A-Za-z]+$")) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域编码仅支持英文、数字");
                }
                if (pc.length() > 128) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域编码长度不能超过128个字符");
                }
                List<String> nameList = names.get(pc);
                if (nameList != null) {
                    if (nameList.contains(nameCell.getStringCellValue())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件中父指标域编码为" + pc + "的指标域中存在重名");
                    } else {
                        nameList.add(nameCell.getStringCellValue());
                    }
                } else {
                    nameList = new ArrayList<>();
                    nameList.add(nameCell.getStringCellValue());
                    names.put(pc, nameList);
                }
                indexFieldExport.setParentCode(pc);
            }
            indexFieldExport.setCode(codeCell.getStringCellValue());
            indexFieldExport.setName(nameCell.getStringCellValue());
            Cell descriptionCell = row.getCell(4);
            if (!Objects.isNull(descriptionCell)) {
                descriptionCell.setCellType(CellType.STRING);
                String description = descriptionCell.getStringCellValue();
                if (description.length() > 200) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "指标域描述长度不能超过200个字符");
                }
                indexFieldExport.setDescription(description);
            }
            indexFieldExports.add(indexFieldExport);
            codes.add(codeCell.getStringCellValue());
        }
        return indexFieldExports;
    }

    /**
     * 编码与名称校验
     *
     * @param indexFieldExports
     */
    public List<IndexFieldExport> checkSameNameCode(List<IndexFieldExport> indexFieldExports, int type, String tenantId) throws SQLException {

        //1.过滤指标域编码已存在的指标域
        List<String> codes = indexFieldExports.stream().map(x -> x.getCode()).distinct().collect(Collectors.toList());
        List<CategoryEntityV2> indexFields = categoryDao.getCategoryByCodes(codes, tenantId, type);
        if (!CollectionUtils.isEmpty(indexFields)) {
            List<IndexFieldExport> removeList = new ArrayList<>();
            List<String> allExistCodes = indexFields.stream().map(x -> x.getCode()).distinct().collect(Collectors.toList());
            for (IndexFieldExport indexFieldExport : indexFieldExports) {
                if (allExistCodes.contains(indexFieldExport.getCode())) {
                    removeList.add(indexFieldExport);
                }
            }
            indexFieldExports.removeAll(removeList);
        }
        //获取数据库中已有的一级指标域
        List<CategoryEntityV2> dbOneLevelCategorys = categoryDao.getAllCategoryByLevel(type, tenantId, 1);
        //获取文件中的一级指标域
        List<String> fileOneLevelCodes = indexFieldExports.stream().filter(x -> Objects.isNull(x.getParentCode())).map(x -> x.getCode()).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(fileOneLevelCodes)) {
            fileOneLevelCodes = new ArrayList<>();
        }
        if (!CollectionUtils.isEmpty(dbOneLevelCategorys) && !CollectionUtils.isEmpty(fileOneLevelCodes)) {
            List<String> dbOneLevelNames = dbOneLevelCategorys.stream().map(x -> x.getName()).distinct().collect(Collectors.toList());
            List<String> dbOneLevelCodes = dbOneLevelCategorys.stream().map(x -> x.getCode()).distinct().collect(Collectors.toList());
            List<IndexFieldExport> removeList = new ArrayList<>();
            for (IndexFieldExport indexFieldExport : indexFieldExports) {
                String parentCode = indexFieldExport.getParentCode();
                if (Objects.isNull(parentCode)) {
                    if (dbOneLevelNames.contains(indexFieldExport.getName())) {
                        //2. 过滤文件中名称已被使用的一级指标域
                        removeList.add(indexFieldExport);
                    }
                } else {
                    //3. 过滤文件中父指标域编码不存在的二级指标域
                    if (!dbOneLevelCodes.contains(parentCode) && !fileOneLevelCodes.contains(parentCode)) {
                        removeList.add(indexFieldExport);
                    }
                }
            }
            indexFieldExports.removeAll(removeList);
        }
        Map<String, List<String>> names = new HashMap<>();
        for (IndexFieldExport indexFieldExport : indexFieldExports) {
            List<String> nameLists = names.get(indexFieldExport.getParentCode());
            if (CollectionUtils.isEmpty(nameLists)) {
                nameLists = new ArrayList<>();
                nameLists.add(indexFieldExport.getName());
                names.put(indexFieldExport.getParentCode(), nameLists);
            } else {
                nameLists.add(indexFieldExport.getName());
            }
        }
        //4. 过滤同一个一级指标域下名称已存在的二级指标域
        Set<String> keys = names.keySet();
        if (!CollectionUtils.isEmpty(keys)) {
            List<IndexFieldExport> removeList = new ArrayList<>();
            for (String key : keys) {
                if (Objects.isNull(key)) {
                    continue;
                } else {
                    List<String> nameList = names.get(key);
                    if (!CollectionUtils.isEmpty(nameList)) {
                        //获取一级指标域
                        CategoryEntityV2 categoryByCode = categoryDao.getCategoryByCode(key, tenantId, type);
                        if (!Objects.isNull(categoryByCode)) {
                            //获取已使用的名称
                            List<String> categoryNames = categoryDao.getChildCategoryName(categoryByCode.getGuid(), tenantId);
                            if (!CollectionUtils.isEmpty(categoryNames)) {
                                //取交集
                                List<String> joinNames = nameList.stream().filter(x -> categoryNames.contains(x)).distinct().collect(Collectors.toList());
                                if (!CollectionUtils.isEmpty(joinNames)) {
                                    for (String joinName : joinNames) {
                                        IndexFieldExport indexFieldExport = indexFieldExports.stream().filter(x -> key.equals(x.getParentCode()) && joinName.equals(x.getName())).findFirst().orElse(null);
                                        if (!Objects.isNull(indexFieldExport)) {
                                            removeList.add(indexFieldExport);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            indexFieldExports.removeAll(removeList);
        }
        return indexFieldExports;
    }

    /**
     * 指标域批量导入
     *
     * @param fileInputStream
     * @param type
     * @throws Exception
     */
    @OperateType(INSERT)
    @Transactional(rollbackFor = Exception.class)
    public void importBatchIndexField(File fileInputStream, int type, String tenantId) throws Exception {
        if (!fileInputStream.exists()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件丢失，请重新上传");
        }
        List<IndexFieldExport> indexFieldExports;
        try {
            indexFieldExports = file2IndexField(fileInputStream);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件异常：" + e.getMessage());
        }
        //根据指标域编码和名称进行过滤
        indexFieldExports = checkSameNameCode(indexFieldExports, type, tenantId);
        //构建指标域树
        List<IndexFieldNode> roots = getIndexFieldTree(indexFieldExports, type, tenantId);
        if (!CollectionUtils.isEmpty(roots)) {
            //完善指标域信息
            for (IndexFieldNode node : roots) {
                optimizeIndexFieldInfo(node, type);
            }
            //获取新增的指标域
            List<CategoryEntityV2> addIndexFields = new ArrayList<>();
            getIndexFields(roots, true, addIndexFields);
            //获取需更新的指标域
            List<CategoryEntityV2> updateIndexFields = new ArrayList<>();
            getIndexFields(roots, false, updateIndexFields);
            importIndexFields(addIndexFields, updateIndexFields, tenantId);
        }
    }

    @OperateType(INSERT)
    @Transactional(rollbackFor = Exception.class)
    public void importIndexFields(List<CategoryEntityV2> addIndexFields, List<CategoryEntityV2> updateIndexFields, String tenantId) {
        if (!CollectionUtils.isEmpty(addIndexFields)) {
            categoryDao.addAll(addIndexFields, tenantId);
            StringBuilder sb = new StringBuilder();
            sb.append("指标域：");
            addIndexFields.forEach(x -> sb.append(x.getName()).append(","));
            sb.deleteCharAt(sb.lastIndexOf(","));
            HttpRequestContext.get().auditLog(ModuleEnum.NORMDESIGN.getAlias(), sb.toString());
        }
        if (!CollectionUtils.isEmpty(updateIndexFields)) {
            categoryDao.updateCategoryEntityV2(updateIndexFields, tenantId);
        }
    }

    private void getIndexFields(List<IndexFieldNode> roots, boolean add, List<CategoryEntityV2> indexFields) {
        for (IndexFieldNode node : roots) {
            if (node.isAdd() == add) {
                indexFields.add(node.getCurrent());
            }
            IndexFieldNode preNode = node.getPreNode();
            if (!add && !Objects.isNull(preNode) && !roots.contains(preNode)) {
                indexFields.add(preNode.getCurrent());
            }
            List<IndexFieldNode> childNodes = node.getChildNodes();
            if (!CollectionUtils.isEmpty(childNodes)) {
                getIndexFields(childNodes, add, indexFields);
            }
        }
    }

    private void optimizeIndexFieldInfo(IndexFieldNode node, int type) {
        CategoryEntityV2 current = node.getCurrent();
        if (!Objects.isNull(node.getPreNode())) {
            current.setUpBrotherCategoryGuid(node.getPreNode().getCurrent().getGuid());
            node.getPreNode().getCurrent().setDownBrotherCategoryGuid(current.getGuid());
        }
        if (!Objects.isNull(node.getNextNode())) {
            current.setDownBrotherCategoryGuid(node.getNextNode().getCurrent().getGuid());
        }
        if (!Objects.isNull(node.getParentNode())) {
            current.setParentCategoryGuid(node.getParentNode().getCurrent().getGuid());
        }
        current.setSafe("1");
        current.setCategoryType(type);
        List<IndexFieldNode> childNodes = node.getChildNodes();
        User user = AdminUtils.getUserData();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (node.isAdd()) {
            current.setCreator(user.getUserId());
            current.setCreateTime(timestamp);
        }
        if (!CollectionUtils.isEmpty(childNodes)) {
            for (IndexFieldNode childNode : childNodes) {
                optimizeIndexFieldInfo(childNode, type);
            }
        }
    }

    private List<IndexFieldNode> getIndexFieldTree(List<IndexFieldExport> indexFieldExports, int type, String tenantId) {
        if (!CollectionUtils.isEmpty(indexFieldExports)) {
            //新增一级指标域
            List<IndexFieldExport> oneLevelsInFile = indexFieldExports.stream().filter(x -> Objects.isNull(x.getParentCode())).collect(Collectors.toList());
            //构建指标域树
            List<IndexFieldNode> roots = new ArrayList<>();
            if (!CollectionUtils.isEmpty(oneLevelsInFile)) {
                CategoryEntityV2 lastCategory = categoryDao.getLastCategory(null, type, tenantId);
                IndexFieldNode preNode = new IndexFieldNode(null, null, lastCategory, null, new ArrayList<>(), false, lastCategory.getCode());
                roots.add(preNode);
                //将一级指标域映射为roots的节点
                for (IndexFieldExport indexFieldExport : oneLevelsInFile) {
                    CategoryEntityV2 current = BeanMapper.map(indexFieldExport, CategoryEntityV2.class);
                    current.setGuid(UUID.randomUUID().toString());
                    current.setLevel(1);
                    current.setQualifiedName(current.getName());
                    IndexFieldNode currentNode = new IndexFieldNode(null, preNode, current, null, new ArrayList<>(), true, current.getCode());
                    preNode.setNextNode(currentNode);
                    roots.add(currentNode);
                    preNode = currentNode;
                }
            }
            Map<String, IndexFieldNode> oneLevel = roots.stream().collect(Collectors.toMap(IndexFieldNode::getCode, Function.identity(), (key1, key2) -> key2));
            //二级指标域
            Map<String, List<IndexFieldExport>> imports = new HashMap<>();
            for (IndexFieldExport indexFieldExport : indexFieldExports) {
                if (StringUtils.isNotEmpty(indexFieldExport.getParentCode())) {
                    List<IndexFieldExport> ifes = imports.get(indexFieldExport.getParentCode());
                    if (CollectionUtils.isEmpty(ifes)) {
                        ifes = new ArrayList<>();
                        ifes.add(indexFieldExport);
                        imports.put(indexFieldExport.getParentCode(), ifes);
                    } else {
                        ifes.add(indexFieldExport);
                    }
                }
            }
            //将二级指标域映射为roots节点
            if (!CollectionUtils.isEmpty(imports)) {
                for (Map.Entry<String, List<IndexFieldExport>> entry : imports.entrySet()) {
                    String k = entry.getKey();
                    List<IndexFieldExport> v = entry.getValue();
                    IndexFieldNode parentNode = oneLevel.get(k);
                    CategoryEntityV2 parent = categoryDao.getCategoryByCode(k, tenantId, type);
                    if (Objects.isNull(parentNode)) {
                        if (Objects.isNull(parent)) {
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "父指标域编码不存在");
                        } else {
                            parentNode = new IndexFieldNode(null, null, parent, null, new ArrayList<>(), false, parent.getCode());
                            roots.add(parentNode);
                        }
                    }
                    IndexFieldNode preNode = null;
                    if (!Objects.isNull(parent)) {
                        CategoryEntityV2 lastCategory = categoryDao.getLastCategory(parent.getGuid(), type, tenantId);
                        if (!Objects.isNull(lastCategory)) {
                            preNode = new IndexFieldNode(null, null, lastCategory, null, null, false, lastCategory.getCode());
                        }
                    }
                    for (IndexFieldExport ife : v) {
                        CategoryEntityV2 current = BeanMapper.map(ife, CategoryEntityV2.class);
                        current.setGuid(UUID.randomUUID().toString());
                        current.setLevel(2);
                        current.setQualifiedName(parentNode.getCurrent().getName() + "." + current.getName());
                        IndexFieldNode currentNode = new IndexFieldNode(parentNode, preNode, current, null, new ArrayList<>(), true, current.getCode());
                        if (!Objects.isNull(preNode)) {
                            preNode.setNextNode(currentNode);
                        }
                        parentNode.getChildNodes().add(currentNode);
                        preNode = currentNode;
                    }
                }
            }
            return roots;
        }
        return null;
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
            User user = AdminUtils.getUserData();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
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
        if (all != null) {
            int size = all.size();
            if (size != 0 && type != technicalType && type != dataStandType) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在目录，无法全局导入");
            } else if (type == technicalType && size != technicalCount) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在初始目录之外的目录，无法全局导入");
            } else if (type == dataStandType && size > dataStandCount) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "存在初始目录之外的目录，无法全局导入");
            }
        }

        List<CategoryEntityV2> categories;
        List<CategoryEntityV2> systemCategory = new ArrayList<>();
        try {
            categories = file2AllData(fileInputStream, type, systemCategory);
            if (type ==technicalType){
                Map<String,List<CategoryEntityV2>> map=categories.stream().collect(Collectors.groupingBy(CategoryEntityV2::getParentCategoryGuid));
                map.forEach((guid,categoryList)->{
                    AtomicInteger maxSort = new AtomicInteger(categoryDao.getMaxSortByParentGuid(guid, tenantId));
                    categoryList.forEach(category->{
                        category.setSort(maxSort.get());
                        maxSort.getAndIncrement();
                    });
                });
            }

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
                System.out.println("strings.get(i)="+strings.get(i));
                System.out.println("cell.getStringCellValue()="+cell.getStringCellValue());
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
            User user = AdminUtils.getUserData();
            category.setCreator(user.getUserId());
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

    //指标域
    public String uploadIndexField(File fileInputStream, int type, String tenantId) throws Exception {
        List<IndexFieldExport> indexFieldExports;
        try {
            indexFieldExports = file2IndexField(fileInputStream);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("数据转换失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件异常：" + e.getMessage());
        }
        if (CollectionUtils.isEmpty(indexFieldExports)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "上传数据为空或均为重复数据");
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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        User user = AdminUtils.getUserData();
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

    public List<String> getChildIndexFields(String parentGuid, String tenantId) {
        List<String> indexFields = categoryDao.queryChildrenCategoryId(parentGuid, tenantId);
        if (!CollectionUtils.isEmpty(indexFields)) {
            return indexFields;
        }
        return new ArrayList<>();
    }

    public List<CategoryEntityV2> queryCategoryEntitysByGuids(List<String> indexFields, String tenantId) throws SQLException {
        List<CategoryEntityV2> categoryEntityV2s = categoryDao.queryCategoryEntitysByGuids(indexFields, tenantId);
        return categoryEntityV2s;
    }
}
