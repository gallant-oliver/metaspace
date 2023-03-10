package io.zeta.metaspace.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dataassets.*;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.datastandard.DataStandard;
import io.zeta.metaspace.model.enums.CategoryPrivateStatus;
import io.zeta.metaspace.model.metadata.GuidCount;
import io.zeta.metaspace.model.metadata.RuleParameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.AddRelationTable;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelation;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.dataquality.RuleTemplateDAO;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    private MetaDataService metadataService;

    @Autowired
    private SearchService searchService;

    // 租户资产-业务目录模块id
    private int TENANT_ASSETS_BUSINESS_MODULE = ModuleEnum.BUSINESSALL.getId();

    @Autowired
    private UserGroupDAO userGroupDAO;

    @Autowired
    private BusinessDAO businessDAO;

    @Autowired
    private TableDAO tableDAO;

    @Autowired
    private ColumnDAO columnDAO;

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    RelationDAO relationDAO;

    @Autowired
    private DataStandardDAO dataStandardDAO;

    @Autowired
    private RuleTemplateDAO ruleTemplateDAO;

    @Autowired
    private TaskManageDAO taskManageDAO;

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
    private boolean isPublicTenant(String tenantId) {
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

    public PageResult<DataAssets> search(int type, int offset, int limit, String tenantId, String query) {
        PageResult<DataAssets> pageResult = new PageResult<>();

        String userId = AdminUtils.getUserData().getUserId();

        // 是否公共租户
        boolean isPublic = isPublicTenant(tenantId);

        // 当前用户是否有全局权限
        boolean isGlobal = isGlobalUser();

        List<DataAssets> list;

        if (!StringUtils.isEmpty(query)) {
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        }

        // 搜索类型：0全部；1业务对象；2数据表;4任务；5质量
        switch (type) {
            case CommonConstant.BUSINESS:
                list = businessDAO.searchBusinesses(tenantId, userId, isPublic, isGlobal, offset, limit, query);
                break;
            case CommonConstant.TABLES:
                list = businessDAO.searchTables(tenantId, userId, isPublic, isGlobal, offset, limit, query);
                break;
            case CommonConstant.TASKS:
                list = taskManageDAO.tasksSearch(tenantId, userId, isGlobal, offset, limit, query);
                break;
            case CommonConstant.STANDARD:
                list = dataStandardDAO.dataSearch(tenantId, userId, isGlobal, offset, limit, query);
                break;
            default:
                list = businessDAO.searchAll(tenantId, userId, isPublic, isGlobal, offset, limit, query);
        }

        long totalSize = 0L;
        if (!CollectionUtils.isEmpty(list)) {
            totalSize =  list.get(0).getTotal();

            Map<String, List<GroupDeriveTableRelation>> m = new HashMap<>();

            // 数据表需要判断是否有重要表的查看权限
            if (type == CommonConstant.TABLES || type == CommonConstant.ALL) {
                List<GroupDeriveTableRelation> privileges = null;
                if (!isPublic || !isGlobal) {
                    List<String> tableIds = list.stream().filter(t -> t.getType() == 2 && t.getImportant()).map(DataAssets::getId).collect(Collectors.toList());
                    // 获取数据表用户组权限（重要表或保密表）
                    if (!CollectionUtils.isEmpty(tableIds)) {
                        privileges = tableDAO.getTablePrivileges(tenantId, tableIds, isPublic, isGlobal, userId);
                    }
                }

                if (!CollectionUtils.isEmpty(privileges)) {
                    m = privileges.stream().filter(p -> p.getImportancePrivilege()).collect(Collectors.groupingBy(GroupDeriveTableRelation::getDeriveTableId));
                }
            }

            for (DataAssets dataAssets: list) {
                String businessPath = dataAssets.getBusinessPath();
                String technicalPath = dataAssets.getTechnicalPath();
                dataAssets.setBusinessPath(formatPath(businessPath, dataAssets.getTenantName(), isPublic));
                dataAssets.setTechnicalPath(formatPath(technicalPath, null, isPublic));

                if (dataAssets.getType() == CommonConstant.TABLES) {
                    // 是否有当前表查看权限（为重要表时）
                    // 公共租户，且当前用户有全局权限
                    if ((isPublic && isGlobal)) {
                        dataAssets.setImportantPrivilege(true);
                    } else {
                        if (!CollectionUtils.isEmpty(m.get(dataAssets.getId()))) {
                            dataAssets.setImportantPrivilege(true);
                        } else {
                            dataAssets.setImportantPrivilege(!dataAssets.getImportant());
                        }
                    }
                }
            }
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);

        return pageResult;
    }

    /**
     * 规范路径（目录路径、技术路径等）
     */
    private String formatPath(String path, String tenantName, boolean isPublic) {
        if (!StringUtils.isEmpty(path)) {
            path = path.substring(1, path.length() - 1);
            path = path.replace(",", "/").replace("\"", "");
            if (!StringUtils.isEmpty(tenantName) && isPublic) {
                path = tenantName + "/" + path;
            }
        }

        return path;
    }

    public Result getDataAssetsById(String id, int type, String belongTenantId, String tenantId, String businessId) {
        Result success = ReturnUtil.success();

        // 是否公共租户
        boolean isPublic = isPublicTenant(tenantId);

        // 当前用户是否有全局权限
        boolean isGlobal = isGlobalUser();

        DataAssets result = null;
        // 搜索类型：1业务对象；2数据表；3主题
        switch (type) {
            case 1:
                result = businessDAO.searchBusinessById(id, belongTenantId);
                success.setData(result);
                break;
            case 2:
                // 表需要判断是否有保密表和重要表权限
                result = tableDAO.searchTableById(id, belongTenantId, businessId);

                List<GroupDeriveTableRelation> privileges = null;
                if (result != null && result.getSecret()) {
                    if (!isPublic || !isGlobal) {
                        privileges = tableDAO.getTablePrivileges(belongTenantId, Lists.newArrayList(result.getId()), isPublic, isGlobal, AdminUtils.getUserData().getUserId());
                    }
                }

                Map<String, List<GroupDeriveTableRelation>> m = new HashMap<>();
                if (!CollectionUtils.isEmpty(privileges)) {
                    m = privileges.stream().filter(p -> p.getSecurityPrivilege()).collect(Collectors.groupingBy(GroupDeriveTableRelation :: getDeriveTableId));
                }

                // 是否有当前表查看权限（为保密表时）
                // 公共租户，且当前用户有全局权限
                if (result != null) {
                    if (isPublic && isGlobal) {
                        result.setSecretPrivilege(true);
                    }
                    else {
                        if (!CollectionUtils.isEmpty(m.get(result.getId()))) {
                            result.setSecretPrivilege(true);
                        } else {
                            result.setSecretPrivilege(!result.getSecret());
                        }
                    }
                }
                success.setData(result);
                break;
            case 3:
                result = getThemeDetail(id, isPublic);
                success.setData(result);
                break;
            case 4:
                TaskExecutionReport taskExecutionInfo = taskManageDAO.getTaskExecutionInfo(id);
                success.setData(taskExecutionInfo);
                break;
            case 5:
                DataStandard dataStandard = dataStandardDAO.getStandardById(id);
                if (dataStandard != null) {
                    dataStandard.setPath(CategoryRelationUtils.getPath(dataStandard.getCategoryId(), dataStandard.getTenantId()));
                }
                success.setData(dataStandard);
                break;
            default:
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据资产类别错误: " + type);
        }

        if (result != null && type != 3) {
            String businessPath = result.getBusinessPath();
            String technicalPath = result.getTechnicalPath();
            result.setBusinessPath(formatPath(businessPath, result.getTenantName(), isPublic));
            result.setTechnicalPath(formatPath(technicalPath, null, isPublic));
        }

        return success;
    }

    public TableShow dataPreview(String tableId, int count) throws Exception {
        GuidCount gc = new GuidCount();
        gc.setGuid(tableId);
        gc.setCount(count);

        return searchService.getTableShow(gc, false);
    }

    public PageResult<TableInfo> getTableInfoByBusinessId(String businessId, String belongTenantId, String tenantId, int limit, int offset) {
        PageResult<TableInfo> pageResult = new PageResult<>();

        List<TableInfo> list = businessDAO.getTableInfos(businessId, belongTenantId, offset, limit);

        Long totalSize = 0L;
        if (!CollectionUtils.isEmpty(list)) {
            totalSize = Long.valueOf(list.get(0).getTotal());

            // 是否公共租户
            boolean isPublic = isPublicTenant(tenantId);

            // 当前用户是否有全局权限
            boolean isGlobal = isGlobalUser();

            List<GroupDeriveTableRelation> privileges = null;
            if (!isPublic || !isGlobal) {
                List<String> tableIds = list.stream().filter(t -> t.getImportant()).map(TableInfo::getTableId).collect(Collectors.toList());
                // 获取数据表用户组权限（重要表或保密表）
                if (!CollectionUtils.isEmpty(tableIds)) {
                    privileges = tableDAO.getTablePrivileges(belongTenantId, tableIds, isPublic, isGlobal, AdminUtils.getUserData().getUserId());
                }
            }

            Map<String, List<GroupDeriveTableRelation>> m = new HashMap<>();
            if (!CollectionUtils.isEmpty(privileges)) {
                m = privileges.stream().filter(p -> p.getImportancePrivilege()).collect(Collectors.groupingBy(GroupDeriveTableRelation :: getDeriveTableId));
            }

            for (TableInfo tableInfo : list) {
                // 是否有当前表查看权限（为重要表时）
                // 公共租户，且当前用户有全局权限
                if (isPublic && isGlobal) {
                    tableInfo.setImportantPrivilege(true);
                }
                else {
                    if (!CollectionUtils.isEmpty(m.get(tableInfo.getTableId()))) {
                        tableInfo.setImportantPrivilege(true);
                    }
                    else {
                        tableInfo.setImportantPrivilege(!tableInfo.getImportant());
                    }
                }

                tableInfo.setCategory(formatPath(tableInfo.getCategory(), null, isPublic));
                tableInfo.setTenantId(belongTenantId);
            }
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);

        return pageResult;
    }

    public List<ColumnInfo> getColumnInfoByTableId(String tableId, String belongTenantId, String tenantId) {
        List<ColumnInfo> columns;
        //获取entity
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = metadataService.getEntityInfoByGuid(tableId, false);

            columns = extractColumnInfo(info);

            if (!CollectionUtils.isEmpty(columns)) {
                List<String> columnIds = columns.stream().map(c -> c.getColumnId()).collect(Collectors.toList());

                // 查询字段关联的衍生表信息及字段标签等
                List<ColumnInfo> deriveColumnInfos = columnDAO.getDeriveColumnInfo(columnIds, belongTenantId, tableId);
                if (!CollectionUtils.isEmpty(deriveColumnInfos)) {
                    Map<String, ColumnInfo> m = deriveColumnInfos.stream().collect(Collectors.toMap(ColumnInfo::getColumnId, Function.identity(), (key1, key2) -> key2));
                    for (ColumnInfo column1 : columns) {
                        ColumnInfo column2 = m.get(column1.getColumnId());
                        if (column2 != null) {
                            column1.setColumnNameZh(column2.getColumnNameZh());
                            column1.setSecret(column2.getSecret());
                            column1.setPeriod(column2.getPeriod());
                            column1.setImportant(column2.getImportant());
                            column1.setTags(column2.getTags());
                            column1.setRemark(column2.getRemark());
                        }
                    }
                }

            }

        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }

        return columns;
    }

    private List<ColumnInfo> extractColumnInfo(AtlasEntity.AtlasEntityWithExtInfo info) throws AtlasBaseException {
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        List<ColumnInfo> columns = new ArrayList<>();

        List<AtlasObjectId> columnsObjectIdList = (List<AtlasObjectId>) info.getEntity().getAttribute("columns");

        if (columnsObjectIdList != null) {
            columnsObjectIdList.stream().map(AtlasObjectId::getGuid).forEach(key -> {
                AtlasEntity referredEntity = referredEntities.get(key);
                if (referredEntity.getTypeName().contains("column") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
                    ColumnInfo column = new ColumnInfo();

                    extractAttributeInfo(referredEntity, column);

                    column.setColumnId(referredEntity.getGuid());
                    columns.add(column);
                }
            });
        }
        return columns;
    }

    private void extractAttributeInfo(AtlasEntity referredEntity, ColumnInfo column) {
        Map<String, Object> attributes = referredEntity.getAttributes();

        String nameAttribute = "name";
        if (attributes.containsKey(nameAttribute) && Objects.nonNull(attributes.get(nameAttribute))) {
            column.setColumnNameEn(attributes.get(nameAttribute).toString());
        } else {
            column.setColumnNameEn("");
        }

        String dataTypeAttribute = "data_type";
        String hiveDataTypeAttribute = "type";
        if (attributes.containsKey(dataTypeAttribute) && Objects.nonNull(attributes.get(dataTypeAttribute))) {
            column.setType(attributes.get(dataTypeAttribute).toString());
        }
        else if (attributes.containsKey(hiveDataTypeAttribute) && Objects.nonNull(attributes.get(hiveDataTypeAttribute))) {
            column.setType(attributes.get(hiveDataTypeAttribute).toString());
        }
        else {
            column.setType("");
        }

        String isPrimaryKeyAttribute = "isPrimaryKey";
        if (attributes.containsKey(isPrimaryKeyAttribute) && Objects.nonNull(attributes.get(isPrimaryKeyAttribute))) {
            column.setPrimaryKey((boolean) attributes.get(isPrimaryKeyAttribute));
        } else {
            column.setPrimaryKey(false);
        }
    }

    public List<DomainInfo> getThemeDomains(String tenantId) throws AtlasException {
        List<DomainInfo> domainList;
        List<DomainInfo> resultList = new ArrayList<>();
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
            domainList = categoryDAO.getDomainCategory();
            for (DomainInfo domain : domainList) {
                //过滤没有二级目录的一级目录
                int num = categoryDAO.getThemeNumber(domain.getDomainId());
                if (0 == num) {
                    continue;
                }
                domain.setThemeNum(num);
                resultList.add(domain);
            }
        } else {
            if (isPublicTenant) {
                tenantId = null;
            }
            domainList = categoryDAO.getDomainCategoryByNotPublicUser(userGroupIds, userId, tenantId);
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
                List<DomainInfo> themeList = categoryDAO.getThemeByUserGroup(domain.getDomainId(), userGroupIds, userId, tenantId);
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
                //过滤没有二级目录的一级目录
                int num = themeList.size();
                if (0 == num) {
                    continue;
                }
                domain.setThemeNum(num);
                resultList.add(domain);
            }
        }

        return resultList;
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
            themeList = categoryDAO.getThemeCategory(guid);
            for (ThemeInfo theme : themeList) {
                List<String> businessList = relationDAO.getBusinessId(theme.getThemeId());
                if (CollectionUtils.isEmpty(businessList)) {
                    theme.setBussinessObjectNum(0);
                    theme.setTableNum(0);
                } else {
                    theme.setBussinessObjectNum(businessList.size());
                    theme.setTableNum(businessDAO.getTableNumber(businessList));
                }
            }
        } else {
            if (isPublicTenant) {
                tenantId = null;
            }
            domainList = categoryDAO.getThemeByUserGroup(guid, userGroupIds, userId, tenantId);
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
                List<String> businessList = businessDAO.queryBusinessIdByUserGroup(themeId, tenantId, userId);
                if (CollectionUtils.isEmpty(businessList)) {
                    theme.setBussinessObjectNum(0);
                    theme.setTableNum(0);
                } else {
                    theme.setBussinessObjectNum(businessList.size());
                    theme.setTableNum(businessDAO.getTableNumber(businessList));
                }
                themeList.add(theme);
            }
        }
        return themeList;
    }

    public PageResult getBusinesses(String guid, String tenantId, int limit, int offset) throws AtlasException {
        List<BussinessObject> objectList;
        PageResult<BussinessObject> pageResult = new PageResult<>();
        Boolean isPublicTenant = isPublicTenant(tenantId);
        Boolean isPublicUser = isGlobalUser();
        String userId = AdminUtils.getUserData().getUserId();
        int total = 0;
        if (isPublicTenant && isPublicUser) {
            objectList = businessDAO.queryBusiness(guid, limit, offset);
            if (!CollectionUtils.isEmpty(objectList)) {
                total = objectList.get(0).getTotal();
            }
        } else {
            if (isPublicTenant) {
                tenantId = null;
            }
            objectList = businessDAO.queryBusinessByUserGroup(tenantId, guid, userId, limit, offset);
            if (!CollectionUtils.isEmpty(objectList)) {
                total = objectList.get(0).getTotal();
            }

        }
        pageResult.setLists(objectList);
        pageResult.setCurrentSize(objectList.size());
        pageResult.setTotalSize(total);
        return pageResult;
    }

    public DataAssets getThemeDetail(String guid, boolean isPublic) {
        DataAssets theme = new DataAssets();
        CategoryEntityV2 categoryEntityV2 = categoryDAO.queryCategoryInfo(guid);

        String path = categoryEntityV2.getQualifiedName().replaceAll("\\.", "\\/");
        if (isPublic) {
            path = categoryEntityV2.getTenantName() + "/" + path;
        }
        theme.setId(guid);
        theme.setName(categoryEntityV2.getName());
        theme.setBusinessPath(path);
        theme.setDescription(categoryEntityV2.getDescription());
        theme.setType(3);
        return theme;
    }

    public PageResult<RuleTemplate> getStandardDetailListByDataStandardId(String dataStandardId, RuleParameters parameters) {
        if (parameters == null || parameters.getLimit() < 0 || parameters.getOffset() < 0) {
            //输入页数不符合规范自动填写默认值
            RuleParameters parame = new RuleParameters();
            parame.setLimit(10);
            parame.setOffset(0);
            parameters = parame;
        }
        PageResult<RuleTemplate> pageResult = new PageResult<>();
        if (!StringUtils.isEmpty(dataStandardId)) {

            List<RuleTemplate> ruleTemplates = ruleTemplateDAO.getRuleTemplateByDataStandardId(dataStandardId, parameters);
            if (!ruleTemplates.isEmpty()) {
                List<String> categoryIds = ruleTemplates.stream().map(RuleTemplate::getRuleType).collect(Collectors.toList());
                Set<CategoryEntityV2> categoryEntityV2s = categoryDAO.selectPathsByGuid(categoryIds);
                Map<String, String> map = categoryEntityV2s.stream().collect(Collectors.toMap(CategoryEntityV2::getGuid, CategoryEntityV2::getPath, (key1, key2) -> key1));
                for (RuleTemplate ruleTemplate : ruleTemplates) {
                    String path = map.get(ruleTemplate.getRuleType());
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(path)) {
                        String replacePath = path.replace(",", "/").replace("\"", "").replace("{", "").replace("}", "");
                        ruleTemplate.setRulesDirectory(replacePath.substring(replacePath.lastIndexOf("/") + 1));
                        ruleTemplate.setPath(replacePath);
                    }
                }
                pageResult.setLists(ruleTemplates);
                pageResult.setCurrentSize(
                        org.apache.commons.collections4.CollectionUtils.isNotEmpty(ruleTemplates)
                                ? ruleTemplates.size()
                                : 0);
                pageResult.setTotalSize(
                        org.apache.commons.collections4.CollectionUtils.isNotEmpty(ruleTemplates)
                                ? ruleTemplates.get(0).getTotal()
                                : 0
                );
                pageResult.setOffset(parameters.getOffset());
            }
        }
        return pageResult;
    }

    public PageResult<ExecutionRecordPage> getDataAssetsTaskExecutionReport(String taskId, RuleParameters parameters, String tenantId) {
        if (parameters == null || parameters.getLimit() < 0 || parameters.getOffset() < 0) {
            //输入页数不符合规范自动填写默认值
            RuleParameters parame = new RuleParameters();
            parame.setLimit(10);
            parame.setOffset(0);
            parameters = parame;
        }
        PageResult<ExecutionRecordPage> pageResult = new PageResult<>();
        if (!StringUtils.isEmpty(taskId)) {

            List<ExecutionRecordPage> taskExecutionRecords = taskManageDAO.getTaskExecutionRecordByPage(taskId, parameters);
            if (!CollectionUtils.isEmpty(taskExecutionRecords)) {
                //获取报告归档路径
                List<String> executionIds = taskExecutionRecords.stream().map(ExecutionRecordPage::getExecutionId).collect(Collectors.toList());
                List<ReportArchivedPath> archivedPathByExecuteId = ruleTemplateDAO.getArchivedPathByExecuteId(executionIds, tenantId);
                Map<String, String> archivedPathMap = archivedPathByExecuteId.stream().collect(Collectors.toMap(ReportArchivedPath::getExecuteId, ReportArchivedPath::getArchivedPath, (key1, key2) -> key1));

                taskExecutionRecords.stream().forEach(r -> {
                    if (r.getErrorCount() != null && r.getErrorCount() != 0) {
                        r.setCheckResult("异常");
                    } else if (r.getGeneralWarningCount() != null && r.getGeneralWarningCount() != 0) {
                        r.setCheckResult("不合格");
                    } else {
                        r.setCheckResult("合格");
                    }

                    if (archivedPathMap.get(r.getExecutionId()) != null) {
                        r.setArchivePath(archivedPathMap.get(r.getExecutionId()));
                    }
                });

                pageResult.setLists(taskExecutionRecords);
                pageResult.setCurrentSize(
                        org.apache.commons.collections4.CollectionUtils.isNotEmpty(taskExecutionRecords)
                                ? taskExecutionRecords.size()
                                : 0);
                pageResult.setTotalSize(
                        org.apache.commons.collections4.CollectionUtils.isNotEmpty(taskExecutionRecords)
                                ? taskExecutionRecords.get(0).getTotal()
                                : 0
                );
                pageResult.setOffset(parameters.getOffset());
            }
        }
        return pageResult;
    }
}
