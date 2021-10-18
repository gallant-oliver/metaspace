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
 * @date 2018/10/25 15:11
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.discovery.MetaspaceGremlinService;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.global.UserPermissionPO;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.security.Tenant;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelation;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.ThreadPoolUtil;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDatabaseService;
import io.zeta.metaspace.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.glossary.enums.AtlasTermRelationshipStatus;
import org.apache.atlas.model.instance.*;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.repository.graph.GraphHelper;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.store.AtlasTypeDefStore;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasStructType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.type.BaseAtlasType;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.metadata.BaseFields.*;
import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;
import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;


/*
 * @description
 * @author sunhaoning
 * @date 2018/10/25 15:11
 */
@Slf4j
@Service
public class MetaDataService {
    private static final Logger LOG = LoggerFactory.getLogger(MetaDataService.class);
    private static final String XLS = "xls";
    private static final String ATT_TABLES = "tables";

    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    private AtlasLineageService atlasLineageService;

    @Autowired
    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDAO;
    @Autowired
    AtlasTypeDefStore typeDefStore;

    @Autowired
    GroupDeriveTableRelationDAO groupDeriveTableRelationDAO;
    @Autowired
    MetaspaceGremlinService metaspaceLineageService;
    @Autowired
    TableTagDAO tableTagDAO;
    @Autowired
    RelationDAO relationDAO;
    @Autowired
    RoleDAO roleDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    TableDAO tableDAO;
    @Autowired
    BusinessDAO businessDAO;
    @Autowired
    MetadataSubscribeDAO metadataSubscribeDAO;
    @Autowired
    DataManageService dataManageService;
    @Autowired
    DataSourceDAO dataSourceDAO;
    @Autowired
    DatabaseInfoDAO databaseInfoDAO;
    @Autowired
    private SourceInfoDatabaseService sourceInfoDatabaseService;

    @Autowired
    private SourceInfoDeriveTableInfoDAO sourceInfoDeriveTableInfoDao;
    @Autowired
    private UserPermissionDAO userPermissionDAO;


    private String errorMessage = "";

    private Map<String, IMetaDataProvider> metaDataProviderMap = new HashMap<>();
    private Map<String, String> errorMap = new HashMap<>();

    private String tableAttribute = "table";
    private String temporaryAttribute = "temporary";
    private String nameAttribute = "name";
    private String partitionKeysAttribute = "partitionKeys";

    @Autowired
    private ColumnDAO columnDAO;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private MetadataHistoryDAO metadataHistoryDAO;
    @Autowired
    private SearchService searchService;
    @Autowired
    private UserGroupDAO userGroupDAO;

    private final EntityGraphRetriever entityRetriever;
    private final AtlasTypeRegistry atlasTypeRegistry;
    private String temporary = "temporary";

    @Inject
    MetaDataService(AtlasTypeRegistry typeRegistry) {
        this.entityRetriever = new EntityGraphRetriever(typeRegistry);
        this.atlasTypeRegistry = typeRegistry;
    }


    public Map<String, Object> getTableType(String guid) {
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        AtlasEntity entity = getEntityById(guid);
        return getTableType(entity);
    }

    public Map<String, Object> getTableType(AtlasEntity entity) {
        if (Objects.isNull(entity)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        try {
            Map<String, Object> result = new HashMap<>();
            if (Objects.isNull(entity)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到表");
            }
            AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
            result.put("schemaId", relatedObject.getGuid());
            result.put("schemaName", relatedObject.getDisplayText());

            String tableType;
            String type = entity.getTypeName();
            if ("hive_table".equalsIgnoreCase(type)) {
                tableType = getEntityAttribute(entity, "tableType");
                result.put("isHiveTable", true);
//                result.put("sourceId", "hive");
            } else if ("rdbms_table".equalsIgnoreCase(type)) {
                tableType = getEntityAttribute(entity, "type");
                result.put("isHiveTable", false);
//                String qualifiedName = String.valueOf(entity.getAttribute("qualifiedName"));
//                result.put("sourceId", StringUtils.isNotEmpty(qualifiedName) ? qualifiedName.split("\\.")[0] : "");
            } else {
                throw new AtlasBaseException("查找节点类型不是表");
            }
            if (tableType != null) {
                result.put("isView", tableType.toLowerCase().contains("view"));
            }
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, "查询表树状信息异常");
        }
    }

    public boolean isHiveTable(String guid) {
        String type = tableDAO.selectTypeByGuid(guid);
        if("hive".equalsIgnoreCase(type)){
            return true;
        }
        return false;
    }

    /**
     * guid请求点
     * 点转换为AtlasEntity
     *
     * @return
     */
    public AtlasEntity.AtlasEntityWithExtInfo getEntityInfoByGuid(String guid) {
        return getEntityInfoByGuid(guid, null, null);
    }

    /**
     * guid请求点
     * 点转换为AtlasEntity
     *
     * @return
     */
    public AtlasEntity.AtlasEntityWithExtInfo getEntityInfoByGuid(String guid, boolean isMinExtInfo) {
        return getEntityInfoByGuid(guid, null, null, isMinExtInfo);
    }

    /**
     * guid请求点
     * 点转换为AtlasEntity
     *
     * @param guid                      实体id
     * @param excludeAttributes         排除的属性，没有填null
     * @param excludeRelationAttributes 排除的依赖属性，没有填null
     * @return
     */
    public AtlasEntity.AtlasEntityWithExtInfo getEntityInfoByGuid(String guid, List<String> excludeAttributes, List<String> excludeRelationAttributes) {
        AtlasVertex entityVertex = entityRetriever.getEntityVertex(guid);
        return vertexToEntityInfo(entityVertex, excludeAttributes, excludeRelationAttributes);
    }

    /**
     * guid请求点
     * 点转换为AtlasEntity
     *
     * @param guid                      实体id
     * @param excludeAttributes         排除的属性，没有填null
     * @param excludeRelationAttributes 排除的依赖属性，没有填null
     * @return
     */
    public AtlasEntity.AtlasEntityWithExtInfo getEntityInfoByGuid(String guid, List<String> excludeAttributes, List<String> excludeRelationAttributes, boolean isMinExtInfo) {
        AtlasVertex entityVertex = entityRetriever.getEntityVertex(guid);
        return vertexToEntityInfo(entityVertex, excludeAttributes, excludeRelationAttributes, isMinExtInfo);
    }

    /**
     * 点转换为AtlasEntity
     *
     * @param atlasVertex               点对象
     * @param excludeAttributes         排除的属性，没有填null
     * @param excludeRelationAttributes 排除的依赖属性，没有填null
     * @return
     */
    public AtlasEntity.AtlasEntityWithExtInfo vertexToEntityInfo(AtlasVertex atlasVertex, List<String> excludeAttributes, List<String> excludeRelationAttributes) {
        return vertexToEntityInfo(atlasVertex, excludeAttributes, excludeRelationAttributes, true);
    }


    /**
     * 点转换为AtlasEntity
     *
     * @param atlasVertex               点对象
     * @param excludeAttributes         排除的属性，没有填null
     * @param excludeRelationAttributes 排除的依赖属性，没有填null
     * @return
     */
    public AtlasEntity.AtlasEntityWithExtInfo vertexToEntityInfo(AtlasVertex atlasVertex, List<String> excludeAttributes, List<String> excludeRelationAttributes, boolean isMinExtInfo) {
        String typeName = GraphHelper.getTypeName(atlasVertex);
        BaseAtlasType objType = atlasTypeRegistry.getType(typeName);
        AtlasStructType structType = (AtlasStructType) objType;
        AtlasEntityType entityType = atlasTypeRegistry.getEntityTypeByName(typeName);

        // 所有attribute
        ArrayList<String> attributes = new ArrayList<>(structType.getAllAttributes().keySet());
        // 排除att
        if (!CollectionUtils.isEmpty(excludeAttributes)) {
            attributes.removeAll(excludeAttributes);
        }
        // 所有relationAttribute
        ArrayList<String> relationshipAttributes = new ArrayList<>(entityType.getRelationshipAttributes().keySet());
        // 排除relationAtt
        if (!CollectionUtils.isEmpty(excludeRelationAttributes)) {
            relationshipAttributes.removeAll(excludeRelationAttributes);
        }

        return entityRetriever.toAtlasEntityWithAttribute(atlasVertex, attributes, relationshipAttributes, isMinExtInfo);
    }

    public Database getDatabase(String guid,String tenantId, String sourceId) throws AtlasBaseException {
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        Database database = new Database();
        List<DatabaseInfoBO> currentSourceInfoList = databaseInfoDAO.getLastDatabaseInfoByDatabaseId(guid,tenantId,sourceId);
        database.setHasDatabase(CollectionUtils.isNotEmpty(currentSourceInfoList));
        database.setSourceTreeId(EntityUtil.generateBusinessId(tenantId,sourceId,"",""));
        try {
            //获取对象
            // 转换成AtlasEntity
            // tables属性需要遍历所有的表，极其耗费性能，此接口没有用到，排除掉tables属性
            AtlasEntity entity = getEntityInfoByGuid(guid, Collections.singletonList(ATT_TABLES), Collections.singletonList(ATT_TABLES)).getEntity();
            database.setDatabaseId(guid);
            database.setDatabaseName(getEntityAttribute(entity, "name"));
            database.setDatabaseDescription(entity.getAttribute("comment") == null ? "-" : entity.getAttribute("comment").toString());
            database.setStatus(entity.getStatus().name());
            database.setOwner(getEntityAttribute(entity, "owner"));
            if (StringUtils.isNotBlank(sourceId)) {
                if ("hive".equalsIgnoreCase(sourceId)) {
                    database.setSourceId("hive");
                    database.setSourceName("hive");
                } else {
                    DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
                    if (null != dataSourceInfo) {
                        database.setSourceId(dataSourceInfo.getSourceId());
                        database.setSourceName(dataSourceInfo.getSourceName());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("getDatabase exception is {}", e);
        }
        return database;
    }


    public Table getTableInfoById(String guid, String tenantId, String sourceId) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableInfoById({})", guid);
        }
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }

        try {
            //获取entity
            AtlasEntity.AtlasEntityWithExtInfo entityInfo = getEntityInfoByGuid(guid, false);
            AtlasEntity entity = entityInfo.getEntity();
            if (Objects.isNull(entity)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到数据表信息");
            }
            //查询用户组
            User user = AdminUtils.getUserData();
            List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());
            //table
            Table table = extractTableInfo(entityInfo, guid, tenantId);
            SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDAO.getByNameAndDbGuid(table.getTableName(),table.getDatabaseId(),tenantId);
            if (Boolean.FALSE.equals(ParamUtil.isNull(sourceInfoDeriveTableInfo))) {
                if (Boolean.FALSE.equals(ParamUtil.isNull(userGroupIds))) {
                    Boolean importancePrivilege = Boolean.TRUE;
                    Boolean securityPrivilege = Boolean.TRUE;
                    GroupDeriveTableRelation relation = groupDeriveTableRelationDAO.getByTableIdAndGroups(guid, userGroupIds, tenantId);
                    if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getImportance()) &&
                            (Boolean.TRUE.equals(ParamUtil.isNull(relation)) || Boolean.FALSE.equals(relation.getImportancePrivilege()))) {
                        importancePrivilege = Boolean.FALSE;

                    }
                    if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getSecurity()) &&
                            (Boolean.TRUE.equals(ParamUtil.isNull(relation)) || Boolean.FALSE.equals(relation.getSecurityPrivilege()))) {
                        securityPrivilege = Boolean.FALSE;
                    }
                    table.setImportancePrivilege(importancePrivilege);
                    table.setSecurityPrivilege(securityPrivilege);
                } else {
                    table.setImportancePrivilege(!sourceInfoDeriveTableInfo.getImportance());
                    table.setSecurityPrivilege(!sourceInfoDeriveTableInfo.getSecurity());
                }
            }
            if(StringUtils.isBlank(sourceId)){
                sourceId="hive";
            }
            Table tableAttr = tableDAO.getDbAndTableName(guid);
            if(tableAttr != null){
                List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId,sourceId,tableAttr.getDatabaseId(),tableAttr.getTableName());
                table.setHasDerivetable(CollectionUtils.isNotEmpty(deriveTableInfoList));
            }


            String tableName = table.getTableName();
            String tableDisplayName = table.getDisplayName();
            if (Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                table.setDisplayName(tableName);
            }
            List<Column> columnList = table.getColumns();
            columnList.forEach(column -> {
                String columnName = column.getColumnName();
                String displayName = column.getDisplayName();
                if (Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    column.setDisplayName(columnName);
                }
            });
            DataSourceInfo dataSourceInfo = null;
            if(StringUtils.isNotBlank(sourceId)&& !"hive".equalsIgnoreCase(sourceId)){
                dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
                if(null != dataSourceInfo){
                    table.setSourceId(dataSourceInfo.getSourceId());
                    table.setSourceName(dataSourceInfo.getSourceName());
                }
            }
            if(null == dataSourceInfo){
                table.setSourceId("hive");
                table.setSourceName("hive");
            }

            table.setSourceTreeId(EntityUtil.generateBusinessId(tenantId,table.getSourceId(),"",""));
            table.setDbTreeId(EntityUtil.generateBusinessId(tenantId,table.getSourceId(),table.getDatabaseId(),""));
            TableExtInfo tableExtInfo = getTableExtAttributes(tenantId,table.getTableId());
            table.setImportance(tableExtInfo.isImportance());
            table.setSecurity(tableExtInfo.isSecurity());
            return table;
        } catch (AtlasBaseException e) {
            String message = "无效的实体ID";
            if (e.getMessage().contains(message)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不存在该表信息，请确定该表是否为脏数据");
            }
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        }
    }


    public Table getTableInfoById(String guid, String tenantId) throws AtlasBaseException {
        return getTableInfoById(guid, tenantId, null);
    }

    public Table extractTableInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid, String tenantId) throws AtlasBaseException {
        Table table = new Table();
        table.setTableId(guid);
        AtlasEntity entity = info.getEntity();
        if (entity.getTypeName().contains(tableAttribute)) {
            //表名称
            table.setTableName(getEntityAttribute(entity, "name"));
            //中文别名
            String displayName = columnDAO.getTableDisplayInfoByGuid(guid);
            if (Objects.nonNull(displayName)) {
                table.setDisplayName(displayName);
            } else {
                table.setDisplayName(table.getTableName());
            }
            //判断是否为虚拟表
            Object attribute = entity.getAttribute(temporaryAttribute);
            if (attribute != null && Boolean.parseBoolean(attribute.toString()) == true) {
                table.setVirtualTable(true);
            } else {
                table.setVirtualTable(false);
            }
            //extractVirtualTable(entity, table);
            //状态
            table.setStatus(entity.getStatus().name());
            //创建人
            table.setOwner(getEntityAttribute(entity, "owner"));

            //描述
            table.setDescription(getEntityAttribute(entity, "comment"));
            //sd
            extractSdInfo(entity, table);
            //类型
            extractTypeInfo(entity, table);
            //是否为分区表
            extractPartitionInfo(entity, table);
            //数据库名
            AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
            table.setDatabaseId(relatedObject.getGuid());
            table.setDatabaseName(relatedObject.getDisplayText());
            ColumnQuery columnQuery = new ColumnQuery();
            columnQuery.setGuid(guid);
            List<Column> columns = getColumnInfoByHasCondition(info, columnQuery, true);
//            List<Column> columns = getColumnInfoById(columnQuery, true);
            table.setColumns(columns);
            //权限,可能从secureplus获取，获取不到就不展示
            try {
                TablePermission permission = HivePermissionUtil.getHivePermission(table.getDatabaseName(), table.getTableName(), table.getColumns());
                table.setTablePermission(permission);
            } catch (Exception e) {
                LOG.error("获取权限失败,错误信息:" + e.getMessage(), e);
            }
            //tag，从postgresql获取，获取不到不展示
            try {
                List<Tag> tags = tableTagDAO.getTable2Tag(table.getTableId(), tenantId);
                table.setTags(tags);
            } catch (Exception e) {
                LOG.error("获取标签失败,错误信息:" + e.getMessage(), e);
            }
            //获取权限判断是否能编辑,默认不能
            table.setEdit(false);
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                try {
                    List<Role> roles = userDAO.getRoleByUserId(AdminUtils.getUserData().getUserId());
                    if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                        table.setEdit(true);
                    } else {
                        List<Module> modules = userDAO.getModuleByUserId(AdminUtils.getUserData().getUserId());
                        for (Module module : modules) {
                            if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                                if (table.getTablePermission().isWrite()) {
                                    table.setEdit(true);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error("获取系统权限失败,错误信息:" + e.getMessage(), e);
                }
            } else {
                try {
                    List<String> categoryIds = categoryDAO.getCategoryGuidByTableGuid(guid, tenantId);
                    boolean edit = false;
                    if (categoryIds.size() > 0) {
                        int count = userGroupDAO.useCategoryPrivilege(AdminUtils.getUserData().getUserId(), categoryIds, tenantId);
                        if (count > 0) {
                            edit = true;
                        }
                    }
                    table.setEdit(edit);
                } catch (Exception e) {
                    LOG.error("获取系统权限失败,错误信息:" + e.getMessage(), e);
                }
            }

            //1.4新增
            try {
                //owner.name
                List<DataOwnerHeader> owners = getDataOwner(guid);
                table.setDataOwner(owners);
                //更新时间
                table.setUpdateTime(DateUtils.date2String(entity.getUpdateTime()));
            } catch (Exception e) {
                LOG.error("获取数据基础信息失败,错误信息:" + e.getMessage(), e);
            }
            try {
                TableInfo tableInfo = tableDAO.getTableInfoByTableguid(guid);
                //所属系统
                table.setSubordinateSystem(tableInfo.getSubordinateSystem());
                //所属数据库
                table.setSubordinateDatabase(tableInfo.getSubordinateDatabase());
                //源系统管理员
                table.setSystemAdmin(tableInfo.getSystemAdmin());
                //数仓管理员
                table.setDataWarehouseAdmin(tableInfo.getDataWarehouseAdmin());
                //数仓描述
                table.setDataWarehouseDescription(tableInfo.getDataWarehouseDescription());
                //目录管理员
                table.setCatalogAdmin(tableInfo.getCatalogAdmin());
                //创建时间
                Object createTime = entity.getAttribute("createTime");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatDateStr = sdf.format(createTime);
                table.setCreateTime(formatDateStr);
            } catch (Exception e) {
                LOG.error("获取源系统维度失败,错误信息:" + e.getMessage(), e);
            }
            try {

            } catch (Exception e) {
                LOG.error("获取数仓维度失败,错误信息:" + e.getMessage(), e);
            }
            try {
                //表关联信息
                List<TableRelation> relations = getRelationList(guid, tenantId);
                table.setRelations(relations.stream().map(r -> r.getCategoryName()).collect(Collectors.toList()));
                //关联时间
                String createDates = relations.stream().map(r -> DateUtils.date2String(r.getCreateDate())).collect(Collectors.joining(","));
                table.setRelationTime(createDates);

            } catch (Exception e) {
                LOG.error("获取数据目录维度失败,错误信息:" + e.getMessage(), e);
            }
            try {
                List<Table.BusinessObject> businessObjectByTableguid = tableDAO.getBusinessObjectByTableguid(guid, tenantId);
                table.setBusinessObjects(businessObjectByTableguid);
            } catch (Exception e) {
                LOG.error("获取业务维度失败,错误信息:" + e.getMessage(), e);
            }
        }
        return table;
    }

    public RDBMSTable getRDBMSTableInfoById(String guid, String tenantId,String sourceId) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getRDBMSTableInfoById({})", guid);
        }
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        try {
            //获取entity
            //获取对象
            // 转换成AtlasEntity
            AtlasEntity.AtlasEntityWithExtInfo info = getEntityInfoByGuid(guid, false);
            AtlasEntity entity = info.getEntity();
            if (Objects.isNull(entity)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到数据表信息");
            }
            //获取当前用户的用户组
            User user = AdminUtils.getUserData();
            List<String> userGroupIds = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId).stream().map(UserGroup::getId).collect(Collectors.toList());
            //table
            RDBMSTable table = extractRDBMSTableInfo(entity, guid, info, tenantId);
            SourceInfoDeriveTableInfo sourceInfoDeriveTableInfo = sourceInfoDeriveTableInfoDAO.getByNameAndDbGuid(table.getTableName(),table.getDatabaseId(),tenantId);
             if (Boolean.FALSE.equals(ParamUtil.isNull(sourceInfoDeriveTableInfo))){
                 if (Boolean.FALSE.equals(ParamUtil.isNull(userGroupIds))) {
                     Boolean importancePrivilege = Boolean.TRUE;
                     Boolean securityPrivilege = Boolean.TRUE;
                     GroupDeriveTableRelation relation = groupDeriveTableRelationDAO.getByTableIdAndGroups(guid, userGroupIds, tenantId);
                     if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getImportance()) &&
                             (Boolean.TRUE.equals(ParamUtil.isNull(relation)) || Boolean.FALSE.equals(relation.getImportancePrivilege()))) {
                         importancePrivilege = Boolean.FALSE;

                     }
                     if (Boolean.TRUE.equals(sourceInfoDeriveTableInfo.getSecurity()) &&
                             (Boolean.TRUE.equals(ParamUtil.isNull(relation)) || Boolean.FALSE.equals(relation.getSecurityPrivilege()))) {
                         securityPrivilege = Boolean.FALSE;
                     }
                     table.setImportancePrivilege(importancePrivilege);
                     table.setSecurityPrivilege(securityPrivilege);
                 }else{
                     table.setImportancePrivilege(!sourceInfoDeriveTableInfo.getImportance());
                     table.setSecurityPrivilege(!sourceInfoDeriveTableInfo.getSecurity());
                 }
            }
            Table tableAttr = tableDAO.getDbAndTableName(guid);
            if(tableAttr != null){
                List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceId == null ? null : sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId,sourceId,tableAttr.getDatabaseId(),tableAttr.getTableName());
                table.setHasDerivetable(CollectionUtils.isNotEmpty(deriveTableInfoList));
            }

            String tableName = table.getTableName();
            String tableDisplayName = table.getDisplayName();
            if (Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                table.setDisplayName(tableName);
            }
            List<RDBMSColumn> columnList = table.getColumns();
            columnList.forEach(column -> {
                String columnName = column.getColumnName();
                String displayName = column.getDisplayName();
                if (Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    column.setDisplayName(columnName);
                }
            });

            TableExtInfo tableExtInfo = getTableExtAttributes(tenantId,table.getTableId());
            table.setImportance(tableExtInfo.isImportance());
            table.setSecurity(tableExtInfo.isSecurity());
            return table;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        }
    }

    private TableExtInfo getTableExtAttributes(String tenantId,String tableGuid){
        TableExtInfo info = new TableExtInfo();
        if(isConfigGloble()){
            LOG.info("当前用户已配置全局权限，忽略重要保密权限");
            info.setImportance(false);
            info.setSecurity(false);
            return info;
        }
        //查看该表在衍生表的重要保密性
        List<TableExtInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDAO.getImportanceInfo(tableGuid,tenantId);
        if(CollectionUtils.isEmpty(deriveTableInfoList)){
            LOG.info("该衍生表没有配置重要保密信息");
            info.setImportance(false);
            info.setSecurity(false);
            return info;
        }
        boolean deriveImportance = deriveTableInfoList.stream().anyMatch(v->v.isImportance());
        boolean deriveSecurity = deriveTableInfoList.stream().anyMatch(v->v.isSecurity());

        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(),tenantId);
        if(CollectionUtils.isEmpty(groups)){
            LOG.info("当前用户没有配置用户组，忽略权限");
            info.setImportance(deriveImportance);
            info.setSecurity(deriveSecurity);
            return info;
        }
        List<String> groupList = groups.stream().map(UserGroup::getId).collect(Collectors.toList());
        List<TableExtInfo> list = tableDAO.selectTableInfoByGroups(tableGuid,tenantId,groupList);
        if(CollectionUtils.isEmpty(list)){
            LOG.info("当前用户组没有配置表的权限，忽略权限");
            info.setImportance(deriveImportance);
            info.setSecurity(deriveSecurity);
            return info;
        }

        info.setImportance(deriveImportance && list.stream().noneMatch(p->p.isImportance()));
        info.setSecurity(deriveSecurity && list.stream().noneMatch(p->p.isSecurity()));
        return info;
    }

    /**
     * 当前账户是否配置全局权限
     * @return true：已配置全局权限
     */
    public boolean isConfigGloble(){
        User user = AdminUtils.getUserData();
        UserPermissionPO userPermissionPO = userPermissionDAO.selectListByUsersId(user.getUserId());
        boolean flag = userPermissionPO != null;
        LOG.info("当前用户配置全局权限:{}",flag);
        return flag;
    }
    public RDBMSTable extractRDBMSTableInfo(AtlasEntity entity, String guid, AtlasEntity.AtlasEntityWithExtInfo info, String tenantId) throws AtlasBaseException {
        RDBMSTable table = new RDBMSTable();
        table.setTableId(guid);
        if (entity.getTypeName().contains(tableAttribute)) {
            //表名称
            table.setTableName(getEntityAttribute(entity, "name"));
            //状态
            table.setStatus(entity.getStatus().name());
//            String qualifiedName = String.valueOf(entity.getAttribute("qualifiedName"));
//            String sourceId = StringUtils.isNotEmpty(qualifiedName) ? qualifiedName.split("\\.")[0] : "";

            //创建时间
            Object createTime = entity.getAttribute("createTime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDateStr = sdf.format(createTime);
            table.setCreateTime(formatDateStr);
            if ("1970-01-01 08:00:00".equals(formatDateStr)) {
                table.setCreateTime(sdf.format(entity.getCreateTime()));
            }

            //描述
            table.setTableDescription(getEntityAttribute(entity, "comment"));
            //数据库名
            AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
            //获取对象
            // 转换成AtlasEntity
            // 不需要tables属性，遍历耗费性能
            AtlasEntity.AtlasEntityWithExtInfo dbInfo = getEntityInfoByGuid(relatedObject.getGuid(), Collections.singletonList(ATT_TABLES), Collections.singletonList(ATT_TABLES));
            AtlasEntity tableEntity = dbInfo.getEntity();
            AtlasRelatedObjectId relatedInstance = this.getRelatedInstance(tableEntity);
//            AtlasEntity.AtlasEntityWithExtInfo dbInfo = entitiesStore.getById(relatedObject.getGuid());
//            AtlasRelatedObjectId relatedInstance = this.getRelatedInstance(dbInfo.getEntity());
            table.setDatabaseId(relatedObject.getGuid());
            table.setDatabaseName(relatedObject.getDisplayText());
            table.setDatabaseStatus(relatedObject.getEntityStatus().name());

//            table.setSourceId(sourceId);
            if(relatedInstance != null){
                table.setSourceName(relatedInstance.getDisplayText());
                table.setSourceStatus(relatedInstance.getEntityStatus().name());
            }
            ColumnQuery columnQuery = new ColumnQuery();
            columnQuery.setGuid(guid);

            RDBMSColumnAndIndexAndForeignKey cik = getRDBMSColumnInfoByHasCondition(info, dbInfo, columnQuery, true);

            table.setForeignKeys(cik.getForeignKeys());
            table.setIndexes(cik.getIndexes());
            table.setColumns(cik.getColumns());
            table.setOwner(getEntityAttribute(entity, "owner"));

            //获取权限判断是否能编辑,默认不能
            table.setEdit(false);
            try {
                List<TableRelation> relationList = getRelationList(guid, tenantId);
                boolean edit = false;
                if (relationList.size() > 0) {
                    List<String> categoryIds = relationList.stream().map(r -> r.getCategoryGuid()).collect(Collectors.toList());
                    int count = userGroupDAO.useCategoryPrivilege(AdminUtils.getUserData().getUserId(), categoryIds, tenantId);
                    if (count > 0) {
                        edit = true;
                    }
                }
                table.setEdit(edit);
            } catch (Exception e) {
                LOG.error("获取系统权限失败,错误信息:" + e.getMessage(), e);
            }

            //tag，从postgresql获取，获取不到不展示
            try {
                List<Tag> tags = tableTagDAO.getTable2Tag(table.getTableId(), tenantId);
                table.setTags(tags);
            } catch (Exception e) {
                LOG.error("获取标签失败,错误信息:" + e.getMessage(), e);
            }

            //1.4新增
            try {
                //owner.name
                List<DataOwnerHeader> owners = getDataOwner(guid);
                table.setDataOwner(owners);
                //更新时间
                table.setUpdateTime(DateUtils.date2String(entity.getUpdateTime()));
            } catch (Exception e) {
                LOG.error("获取数据基础信息失败,错误信息:" + e.getMessage(), e);
            }

            try {
                TableInfo tableInfo = tableDAO.getTableInfoByTableguid(guid);
                //所属系统
                table.setSubordinateSystem(tableInfo.getSubordinateSystem());
                //所属数据库
                table.setSubordinateDatabase(tableInfo.getSubordinateDatabase());
                //源系统管理员
                table.setSystemAdmin(tableInfo.getSystemAdmin());
                //数仓管理员
                table.setDataWarehouseAdmin(tableInfo.getDataWarehouseAdmin());
                //数仓描述
                table.setDataWarehouseDescription(tableInfo.getDataWarehouseDescription());
                //目录管理员
                table.setCatalogAdmin(tableInfo.getCatalogAdmin());


            } catch (Exception e) {
                LOG.error("获取源系统维度失败,错误信息:" + e.getMessage(), e);
            }

            try {
                //表关联信息
                List<TableRelation> relations = getRelationList(guid, tenantId);
                table.setRelations(relations.stream().map(r -> r.getCategoryName()).collect(Collectors.toList()));
                //关联时间
                String createDates = relations.stream().map(r -> DateUtils.date2String(r.getCreateDate())).collect(Collectors.joining(","));
                table.setRelationTime(createDates);

            } catch (Exception e) {
                LOG.error("获取数据目录维度失败,错误信息:" + e.getMessage(), e);
            }

            try {
                List<Table.BusinessObject> businessObjectByTableguid = tableDAO.getBusinessObjectByTableguid(guid, tenantId);
                table.setBusinessObjects(businessObjectByTableguid);
            } catch (Exception e) {
                LOG.error("获取业务维度失败,错误信息:" + e.getMessage(), e);
            }
        }
        return table;
    }


    @Cacheable(value = "RDBMSColumnCache", key = "#query.guid + #query.columnFilter.columnName + #query.columnFilter.type + #query.columnFilter.description", condition = "#refreshCache==false")
    public RDBMSColumnAndIndexAndForeignKey getRDBMSColumnInfoById(ColumnQuery query, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getRDBMSColumnInfoById({})", query);
        }
        RDBMSColumnAndIndexAndForeignKey cik = new RDBMSColumnAndIndexAndForeignKey();
        String guid = query.getGuid();
        List<RDBMSColumn> columns = null;
        List<RDBMSForeignKey> foreignKeys = null;
        List<RDBMSIndex> indexes = null;
        //获取entity
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = getEntityInfoByGuid(guid, false);
            AtlasRelatedObjectId relatedDB = getRelatedDB(info.getEntity());

            // 不需要tables属性，遍历耗费性能
            AtlasEntity.AtlasEntityWithExtInfo dbInfo = getEntityInfoByGuid(relatedDB.getGuid(), Collections.singletonList(ATT_TABLES), Collections.singletonList(ATT_TABLES));
            AtlasRelatedObjectId relatedInstance = getRelatedInstance(dbInfo.getEntity());
            //columns
            columns = extractRDBMSColumnInfo(info, guid, relatedDB, relatedInstance);
            //filter
            columns = filterRDBMSColumn(query, columns);
            foreignKeys = extractRDBMSForeignKeyInfo(info, guid, relatedDB, relatedInstance, columns, refreshCache);
            indexes = extractRDBMSIndexInfo(info, guid, relatedDB, relatedInstance, columns, refreshCache);
            cik.setColumns(columns);
            cik.setForeignKeys(foreignKeys);
            cik.setIndexes(indexes);
            return cik;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }
    }

    public RDBMSColumnAndIndexAndForeignKey getRDBMSColumnInfoByHasCondition(AtlasEntity.AtlasEntityWithExtInfo info, AtlasEntity.AtlasEntityWithExtInfo dbInfo, ColumnQuery query, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getRDBMSColumnInfoById({})", query);
        }
        RDBMSColumnAndIndexAndForeignKey cik = new RDBMSColumnAndIndexAndForeignKey();
        String guid = query.getGuid();
        List<RDBMSColumn> columns = null;
        List<RDBMSForeignKey> foreignKeys = null;
        List<RDBMSIndex> indexes = null;
        //获取entity
        try {
            AtlasRelatedObjectId relatedDB = getRelatedDB(info.getEntity());
            AtlasRelatedObjectId relatedInstance = getRelatedInstance(dbInfo.getEntity());
            //columns
            columns = extractRDBMSColumnInfo(info, guid, relatedDB, relatedInstance);
            //filter
            columns = filterRDBMSColumn(query, columns);
            foreignKeys = extractRDBMSForeignKeyInfo(info, guid, relatedDB, relatedInstance, columns, refreshCache);
            indexes = extractRDBMSIndexInfo(info, guid, relatedDB, relatedInstance, columns, refreshCache);
            cik.setColumns(columns);
            cik.setForeignKeys(foreignKeys);
            cik.setIndexes(indexes);
            return cik;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }
    }


    public List<RDBMSColumn> extractRDBMSColumnInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid, AtlasRelatedObjectId relatedDB, AtlasRelatedObjectId relatedInstance) throws AtlasBaseException {
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        AtlasEntity entity = info.getEntity();
        List<RDBMSColumn> columns = new ArrayList<>();

        List<AtlasObjectId> columnsObjectIdList = (List<AtlasObjectId>) info.getEntity().getAttribute("columns");

        if (columnsObjectIdList != null) {
            columnsObjectIdList.stream().map(AtlasObjectId::getGuid).forEach(key -> {
                AtlasEntity referredEntity = referredEntities.get(key);
                if (referredEntity.getTypeName().contains("column") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
                    RDBMSColumn column = new RDBMSColumn();
                    //tableId
                    column.setTableId(guid);
                    //tableName
                    column.setTableName(getEntityAttribute(entity, "name"));
                    column.setTableStatus(entity.getStatus().toString());
                    //status
                    column.setStatus(referredEntity.getStatus().name());
                    //databaseId && dataBaseName
                    column.setDatabaseId(relatedDB.getGuid());
                    column.setDatabaseName(relatedDB.getDisplayText());
                    column.setDatabaseStatus(relatedDB.getEntityStatus().name());

//                    column.setSourceId(relatedInstance.getGuid());
                    if(relatedInstance != null){
                        column.setSourceName(relatedInstance.getDisplayText());
                        column.setSourceStatus(relatedInstance.getEntityStatus().name());
                    }
                    column.setColumnId(referredEntity.getGuid());
                    //attribute
                    extractAttributeInfo(referredEntity, column);
                    columns.add(column);
                }
            });
        }
        return columns;
    }

    public List<RDBMSIndex> extractRDBMSIndexInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid, AtlasRelatedObjectId relatedDB, AtlasRelatedObjectId relatedInstance, List<RDBMSColumn> columns, Boolean refreshCache) throws AtlasBaseException {
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        AtlasEntity entity = info.getEntity();
        List<RDBMSIndex> indexes = new ArrayList<>();
        RDBMSIndex index = null;

        for (String key : referredEntities.keySet()) {
            AtlasEntity referredEntity = referredEntities.get(key);
            if (referredEntity.getTypeName().contains("rdbms_index") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
                index = new RDBMSIndex();
                //tableId
                index.setTableId(guid);
                //tableName
                index.setTableName(getEntityAttribute(entity, "name"));
                //status
                index.setStatus(referredEntity.getStatus().name());
                //databaseId && dataBaseName
                index.setDatabaseId(relatedDB.getGuid());
                index.setDatabaseName(relatedDB.getDisplayText());

                index.setSourceId(relatedInstance.getGuid());
                index.setSourceName(relatedInstance.getDisplayText());

                index.setIndexId(referredEntity.getGuid());
                //attribute
                Map<String, Object> attributes = referredEntity.getAttributes();
                if (attributes.containsKey(nameAttribute) && Objects.nonNull(attributes.get(nameAttribute))) {
                    index.setName(attributes.get(nameAttribute).toString());
                } else {
                    index.setName("");
                }
                if (attributes.containsKey("index_type") && Objects.nonNull(attributes.get("index_type"))) {
                    index.setType(attributes.get("index_type").toString());
                } else {
                    index.setName("");
                }
                if (attributes.containsKey("isUnique") && Objects.nonNull(attributes.get("isUnique"))) {
                    index.setUnique((boolean) attributes.get("isUnique"));
                } else {
                    index.setUnique(true);
                }
                if (attributes.containsKey("comment") && Objects.nonNull(attributes.get("comment"))) {
                    index.setDescription(attributes.get("comment").toString());
                } else {
                    index.setDescription("");
                }

                if (attributes.containsKey("columns") && Objects.nonNull(attributes.get("columns"))) {
                    List<AtlasObjectId> indexColumns = (List<AtlasObjectId>) attributes.get("columns");
                    List<RDBMSColumn> indexColumnsV2 = columns.stream().filter(column -> indexColumns.stream().anyMatch(indexColumn -> indexColumn.getGuid().equals(column.getColumnId()))).collect(Collectors.toList());
                    index.setColumns(indexColumnsV2);
                } else {
                    index.setColumns(new ArrayList<>());
                }

                indexes.add(index);
            }
        }
        return indexes;
    }

    public List<RDBMSForeignKey> extractRDBMSForeignKeyInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid, AtlasRelatedObjectId relatedDB, AtlasRelatedObjectId relatedInstance, List<RDBMSColumn> columns, Boolean refreshCache) throws AtlasBaseException {
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        AtlasEntity entity = info.getEntity();
        List<RDBMSForeignKey> foreignKeys = new ArrayList<>();
        RDBMSForeignKey foreignKey = null;

        for (String key : referredEntities.keySet()) {
            AtlasEntity referredEntity = referredEntities.get(key);
            if (referredEntity.getTypeName().contains(RDBMS_FOREIGN_KEY) && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE) && referredEntity.getAttribute(ATTRIBUTE_TABLE) != null && entity.getGuid().equals(((AtlasObjectId) referredEntity.getAttribute(ATTRIBUTE_TABLE)).getGuid())) {
                foreignKey = new RDBMSForeignKey();
                //tableId
                foreignKey.setTableId(guid);
                //tableName
                foreignKey.setTableName(getEntityAttribute(entity, "name"));
                //status
                foreignKey.setStatus(referredEntity.getStatus().name());
                //databaseId && dataBaseName
                foreignKey.setDatabaseId(relatedDB.getGuid());
                foreignKey.setDatabaseName(relatedDB.getDisplayText());

                foreignKey.setSourceId(relatedInstance.getGuid());
                foreignKey.setSourceName(relatedInstance.getDisplayText());

                foreignKey.setForeignKeyId(referredEntity.getGuid());
                //attribute
                Map<String, Object> attributes = referredEntity.getAttributes();
                if (attributes.containsKey("name") && Objects.nonNull(attributes.get("name"))) {
                    foreignKey.setName(attributes.get("name").toString());
                } else {
                    foreignKey.setName("");
                }
                if (attributes.containsKey("key_columns") && Objects.nonNull(attributes.get("key_columns"))) {
                    List<AtlasObjectId> keyColumns = (List<AtlasObjectId>) attributes.get("key_columns");
                    List<RDBMSColumn> keyColumnsV2 = columns.stream().filter(column -> keyColumns.stream().anyMatch(keyColumn -> keyColumn.getGuid().equals(column.getColumnId()))).collect(Collectors.toList());
                    foreignKey.setColumns(keyColumnsV2);
                } else {
                    foreignKey.setColumns(new ArrayList<>());
                }

                foreignKeys.add(foreignKey);
            }
        }
        return foreignKeys;
    }

    public void extractAttributeInfo(AtlasEntity referredEntity, RDBMSColumn column) {
        Map<String, Object> attributes = referredEntity.getAttributes();
        if (attributes.containsKey(nameAttribute) && Objects.nonNull(attributes.get(nameAttribute))) {
            column.setColumnName(attributes.get("name").toString());
        } else {
            column.setColumnName("");
        }
        String dataTypeAttribute = "data_type";
        if (attributes.containsKey(dataTypeAttribute) && Objects.nonNull(attributes.get(dataTypeAttribute))) {
            column.setType(attributes.get(dataTypeAttribute).toString());
        } else {
            column.setType("");
        }
        String lengthAttribute = "length";
        if (attributes.containsKey(lengthAttribute) && Objects.nonNull(attributes.get(lengthAttribute))) {
            column.setLength((int) attributes.get(lengthAttribute));
        } else {
            column.setLength(-1);
        }
        String defaultValueAttribute = "defaultValue";
        if (attributes.containsKey(defaultValueAttribute) && Objects.nonNull(attributes.get(defaultValueAttribute))) {
            column.setDefaultValue(attributes.get(defaultValueAttribute).toString());
        } else {
            column.setDefaultValue("");
        }
        String isNullableAttribute = "isNullable";
        if (attributes.containsKey(isNullableAttribute) && Objects.nonNull(attributes.get(isNullableAttribute))) {
            column.setNullable((boolean) attributes.get(isNullableAttribute));
        } else {
            column.setNullable(true);
        }
        String isPrimaryKeyAttribute = "isPrimaryKey";
        if (attributes.containsKey(isPrimaryKeyAttribute) && Objects.nonNull(attributes.get(isPrimaryKeyAttribute))) {
            column.setPrimaryKey((boolean) attributes.get(isPrimaryKeyAttribute));
        } else {
            column.setPrimaryKey(false);
        }
        String commentAttribute = "comment";
        if (attributes.containsKey(commentAttribute) && Objects.nonNull(attributes.get(commentAttribute))) {
            column.setColumnDescription(attributes.get(commentAttribute).toString());
        } else {
            column.setColumnDescription("");
        }
        column.setDescription(column.getColumnDescription());
        column.setDisplayName(column.getColumnName());

    }

    public List<RDBMSColumn> filterRDBMSColumn(ColumnQuery query, List<RDBMSColumn> columns) {
        if (query.getColumnFilter() != null) {
            ColumnQuery.ColumnFilter filter = query.getColumnFilter();
            String columnName = filter.getColumnName();
            String type = filter.getType();
            String description = filter.getDescription();
            if (Objects.nonNull(columnName) && !columnName.equals("")) {
                columns = columns.stream().filter(col -> col.getColumnName().contains(filter.getColumnName())).collect(Collectors.toList());
            }
            if (Objects.nonNull(type) && !type.equals("")) {
                columns = columns.stream().filter(col -> {
                    String start = "(";
                    String end = ")";
                    if (col.getType().contains(start) && col.getType().contains(end)) {
                        int lastIndex = col.getType().lastIndexOf(start);
                        String typeStr = col.getType().substring(0, lastIndex);
                        return typeStr.equals(type);
                    }
                    return col.getType().equals(type);
                }).collect(Collectors.toList());
            }
            if (Objects.nonNull(description) && !description.equals("")) {
                columns = columns.stream().filter(col -> col.getColumnDescription().contains(description)).collect(Collectors.toList());
            }
        }
        return columns;
    }

    public AtlasRelatedObjectId getRelatedInstance(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String instanceAttribute = "instance";
        if (entity.hasRelationshipAttribute(instanceAttribute) && Objects.nonNull(entity.getRelationshipAttribute(instanceAttribute))) {
            Object obj = entity.getRelationshipAttribute(instanceAttribute);
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }

    public void extractPartitionInfo(AtlasEntity entity, Table table) {
        if (entity.hasAttribute(partitionKeysAttribute) && entity.getAttribute(partitionKeysAttribute) != null) {
            List<AtlasObjectId> partitionKeys = toAtlasObjectIdList(entity.getAttribute(partitionKeysAttribute));
            if (partitionKeys.isEmpty())
                table.setPartitionTable(false);
            else
                table.setPartitionTable(true);
        } else {
            table.setPartitionTable(false);
        }
    }

    protected List<AtlasObjectId> toAtlasObjectIdList(Object obj) {
        final List<AtlasObjectId> ret;

        if (obj instanceof Collection) {
            Collection coll = (Collection) obj;

            ret = new ArrayList<>(coll.size());

            for (Object item : coll) {
                AtlasObjectId objId = toAtlasObjectId(item);

                if (objId != null) {
                    ret.add(objId);
                }
            }
        } else {
            AtlasObjectId objId = toAtlasObjectId(obj);

            if (objId != null) {
                ret = new ArrayList<>(1);

                ret.add(objId);
            } else {
                ret = null;
            }
        }

        return ret;
    }

    protected AtlasObjectId toAtlasObjectId(Object obj) {
        final AtlasObjectId ret;

        if (obj instanceof AtlasObjectId) {
            ret = (AtlasObjectId) obj;
        } else if (obj instanceof Map) {
            ret = new AtlasObjectId((Map) obj);
        } else if (obj != null) {
            // guid
            ret = new AtlasObjectId(obj.toString());
        } else {
            ret = null;
        }

        return ret;
    }

    public void extractVirtualTable(AtlasEntity entity, Table table) {
        String tableName = getEntityAttribute(entity, "name");
        String tmpTable = "values__tmp__table";
        if (tableName.contains(tmpTable))
            table.setVirtualTable(true);
        else
            table.setVirtualTable(false);
    }

    public void extractSdInfo(AtlasEntity entity, Table table) throws AtlasBaseException {
        String sd = "sd";
        if (entity.hasAttribute(sd) && Objects.nonNull(entity.getAttribute(sd))) {
            Object obj = entity.getAttribute(sd);
            if (obj instanceof AtlasObjectId) {
                AtlasObjectId atlasObject = (AtlasObjectId) obj;
                String sdGuid = atlasObject.getGuid();
                AtlasEntity sdEntity = getEntityById(sdGuid);
                //位置
                table.setLocation(getEntityAttribute(sdEntity, "location"));
                //格式
                String inputFormat = getEntityAttribute(sdEntity, "inputFormat");
                if (Objects.nonNull(inputFormat)) {
                    String[] fullFormat = inputFormat.split("\\.");
                    table.setFormat(fullFormat[fullFormat.length - 1]);
                }
            }
        }
    }

    public void extractTypeInfo(AtlasEntity entity, Table table) {
        String tableType = getEntityAttribute(entity, "tableType");
        if(null == tableType){
            tableType = getEntityAttribute(entity, "type");
            table.setType(tableType);
            return;
        }
        String external = "EXTERNAL";
        if (tableType.contains(external)) {
            table.setType("EXTERNAL_TABLE");
        } else {
            table.setType("INTERNAL_TABLE");
        }
    }

    public AtlasEntity getEntityById(String guid) throws AtlasBaseException {
        AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
        return info.getEntity();
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if (entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String dbAttribute = "db";
        if (entity.hasRelationshipAttribute(dbAttribute) && Objects.nonNull(entity.getRelationshipAttribute(dbAttribute))) {
            Object obj = entity.getRelationshipAttribute(dbAttribute);
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }

    public List<TableRelation> getRelationList(String tableGuid, String tenantId) throws AtlasBaseException {
        List<TableRelation> categoryRelations = new ArrayList<>();
        List<TableRelation> categoryRelationsFromRelation = relationDAO.queryTableCategoryRelations(tableGuid, tenantId);
        if(CollectionUtils.isNotEmpty(categoryRelationsFromRelation)){
            categoryRelations.addAll(categoryRelationsFromRelation);
        }
        List<TableRelation> categoryRelationsFromDb = relationDAO.queryTableCategoryRelationsFromDb(tableGuid, tenantId);
        if(CollectionUtils.isNotEmpty(categoryRelationsFromDb)){
            for(int i = categoryRelationsFromDb.size() -1; i>=0; i--){
                String categoryGuid = categoryRelationsFromDb.get(i).getCategoryGuid();
                for(TableRelation categoryRelation : categoryRelations){
                    if(categoryRelation.getCategoryGuid().equalsIgnoreCase(categoryGuid)){
                        categoryRelationsFromDb.remove(i);
                        break;
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(categoryRelationsFromDb)){
                categoryRelations.addAll(categoryRelationsFromDb);
            }

        }
        return categoryRelations;
    }

    @Cacheable(value = "columnCache", key = "#query.guid + #query.columnFilter.columnName + #query.columnFilter.type + #query.columnFilter.description", condition = "#refreshCache==false")
    public List<Column> getColumnInfoById(ColumnQuery query, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getColumnInfoById({})", query);
        }
        try {
            if (query.getColumnFilter() == null) {
                query.setColumnFilter(new ColumnQuery.ColumnFilter());
            } else {
                String columnName = query.getColumnFilter().getColumnName();
                if (StringUtils.isNotBlank(columnName)) {
                    query.getColumnFilter().setColumnName(columnName.replaceAll("%", "/%").replaceAll("_", "/_"));
                }
                String description = query.getColumnFilter().getDescription();
                if(StringUtils.isNotBlank(description)){
                    query.getColumnFilter().setDescription(description.replaceAll("%", "/%").replaceAll("_", "/_"));
                }
            }
            List<Column> columns = columnDAO.selectListByGuidOrLike(query.getGuid(), query.getColumnFilter().getColumnName(), query.getColumnFilter().getType(), query.getColumnFilter().getDescription());
//          TODO: 2021/8/24 缺少分区查询功能

//            AtlasEntity.AtlasEntityWithExtInfo info = getEntityInfoByGuid(guid, false);
//            //columns
//            columns = extractColumnInfo(info, guid);
//            //filter
//            columns = filterColumn(query, columns);
            return columns;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }
    }

    public List<Column> getColumnInfoByHasCondition(AtlasEntity.AtlasEntityWithExtInfo info, ColumnQuery query, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getColumnInfoById({})", query);
        }
        String guid = query.getGuid();
        List<Column> columns = null;
        //获取entity
        try {
            //columns
            columns = extractColumnInfo(info, guid);
            //filter
            columns = filterColumn(query, columns);
            return columns;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }
    }

    public List<Column> extractColumnInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid) {
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        AtlasEntity entity = info.getEntity();
        List<Column> columns = new ArrayList<>();


        List<AtlasObjectId> partitionKeys = extractPartitionKeyInfo(entity);
        List<AtlasObjectId> columnsObjectIdList = (List<AtlasObjectId>) info.getEntity().getAttribute("columns");

        if (columnsObjectIdList != null) {
            columnsObjectIdList.stream().map(AtlasObjectId::getGuid).forEach(key -> {
                AtlasEntity referredEntity = referredEntities.get(key);
                if (referredEntity.getTypeName().contains("column") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
                    Column column = new Column();
                    //tableId
                    column.setTableId(guid);
                    //tableName
                    column.setTableName(getEntityAttribute(entity, "name"));
                    //status
                    column.setStatus(referredEntity.getStatus().name());
                    //databaseId && dataBaseName
                    AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                    column.setDatabaseId(relatedDB.getGuid());
                    column.setDatabaseName(relatedDB.getDisplayText());

                    column.setColumnId(referredEntity.getGuid());
                    if (partitionKeys != null) {
                        for (int i = 0; i < partitionKeys.size(); i++) {
                            if (partitionKeys.get(i).getGuid().equals(column.getColumnId())) {
                                column.setPartitionKey(true);
                            }
                        }
                    }
                    //attribute
                    extractAttributeInfo(referredEntity, column);
                    columns.add(column);
                }
            });
        }
        return columns;
    }

    public List<AtlasObjectId> extractPartitionKeyInfo(AtlasEntity entity) {
        List<AtlasObjectId> partitionKeys = null;
        String partitionKeysAttribute = "partitionKeys";
        if (Objects.nonNull(entity.getAttribute(partitionKeysAttribute))) {
            Object partitionObjects = entity.getAttribute("partitionKeys");
            if (partitionObjects instanceof ArrayList<?>) {
                partitionKeys = (ArrayList<AtlasObjectId>) partitionObjects;
            }
        }
        return partitionKeys;
    }

    public void extractAttributeInfo(AtlasEntity referredEntity, Column column) {
        Map<String, Object> attributes = referredEntity.getAttributes();
        if (attributes.containsKey(nameAttribute) && Objects.nonNull(attributes.get(nameAttribute))) {
            column.setColumnName(attributes.get(nameAttribute).toString());
        } else {
            column.setColumnName("");
        }
        String typeAttribute = "type";
        if (attributes.containsKey(typeAttribute) && Objects.nonNull(attributes.get(typeAttribute))) {
            column.setType(attributes.get(typeAttribute).toString());
        } else {
            column.setType("");
        }

        String commentAttribute = "comment";
        if (attributes.containsKey(commentAttribute) && Objects.nonNull(attributes.get(commentAttribute))) {
            column.setDescription(attributes.get(commentAttribute).toString());
        } else {
            column.setDescription("");
        }
        Column pgColumnInfo = columnDAO.getColumnInfoByGuid(column.getColumnId());
        if (Objects.nonNull(pgColumnInfo)) {
            String displayName = pgColumnInfo.getDisplayName();
            if (Objects.nonNull(displayName) && !"".equals(displayName.trim())) {
                column.setDisplayName(displayName);
            } else {
                column.setDisplayName(column.getColumnName());
            }
            String displayUpdateTime = pgColumnInfo.getDisplayNameUpdateTime();
            if (Objects.nonNull(displayUpdateTime) && !"".equals(displayUpdateTime.trim())) {
                column.setDisplayNameUpdateTime(displayUpdateTime);
            }
        } else {
            column.setDisplayName(column.getColumnName());
        }

    }

    public List<Column> filterColumn(ColumnQuery query, List<Column> columns) {
        if (query.getColumnFilter() != null) {
            ColumnQuery.ColumnFilter filter = query.getColumnFilter();
            String columnName = filter.getColumnName();
            String type = filter.getType();
            String description = filter.getDescription();
            if (Objects.nonNull(columnName) && !columnName.equals("")) {
                columns = columns.stream().filter(col -> col.getColumnName().contains(filter.getColumnName())).collect(Collectors.toList());
            }
            String start = "(";
            String end = ")";
            if (Objects.nonNull(type) && !type.equals("")) {
                columns = columns.stream().filter(col -> {
                    if (col.getType().contains(start) && col.getType().contains(end)) {
                        int lastIndex = col.getType().lastIndexOf(start);
                        String typeStr = col.getType().substring(0, lastIndex);
                        return typeStr.equals(type);
                    }
                    return col.getType().equals(type);
                }).collect(Collectors.toList());
            }
            if (Objects.nonNull(description) && !description.equals("")) {
                columns = columns.stream().filter(col -> col.getDescription().contains(description)).collect(Collectors.toList());
            }
        }
        return columns;
    }

    public TableLineageInfo getTableLineage(String guid, AtlasLineageInfo.LineageDirection direction,
                                            int depth) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableLineage({}, {}, {})", guid, direction, depth);
        }
        try {
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, direction, depth);
            if (Objects.isNull(lineageInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取表血缘关系失败");
            }
            TableLineageInfo info = new TableLineageInfo();
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            String lineageGuid = lineageInfo.getBaseEntityGuid();
            //guid
            info.setGuid(lineageGuid);
            //relations
            Set<LineageTrace> lineageRelations = getRelations(lineageInfo);
            //entities
            List<TableLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();
            List<CompletableFuture> completableFutureList = new ArrayList<>(entities.size());
            for (String key : entities.keySet()) {
                completableFutureList.add(CompletableFuture.runAsync(() -> {
                    TableLineageInfo.LineageEntity lineageEntity = new TableLineageInfo.LineageEntity();
                    lineageEntities.add(lineageEntity);
                    AtlasEntityHeader atlasEntity = entities.get(key);
                    getTableEntityInfo(key, lineageEntity, entities, atlasEntity);
                }, threadPoolExecutor));
            }
            CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[]{})).get(30, TimeUnit.SECONDS);
            info.setEntities(lineageEntities);
            info.setRelations(lineageRelations);
            return info;
        } catch (AtlasBaseException | InterruptedException | TimeoutException | ExecutionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘关系失败");
        }
    }

    public LineageDepthInfo getTableLineageDepthInfo(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getLineageInfo({})", guid);
        }
        try {
            LineageDepthInfo lineageDepthEntity = new LineageDepthInfo();
            AtlasEntity entity = getEntityInfoByGuid(guid).getEntity();
            if (Objects.nonNull(entity)) {
                String hdfs = "hdfs";
                if (entity.getTypeName().contains(tableAttribute) || entity.getTypeName().contains(hdfs)) {
                    //guid
                    lineageDepthEntity.setGuid(guid);
                    //tableName
                    lineageDepthEntity.setTableName(getEntityAttribute(entity, "name"));
                    //displayText
                    //updateTime
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(entity.getUpdateTime());
                    lineageDepthEntity.setUpdateTime(formatDateStr);
                    //dbName
                    AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
                    if (Objects.nonNull(relatedObject))
                        lineageDepthEntity.setDbName(relatedObject.getDisplayText());
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }

            return lineageDepthEntity;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘深度详情失败");
        }
    }

    public LineageDepthInfo getLineageDepthV2(LineageDepthInfo lineageDepthEntity) throws AtlasBaseException {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();
        String guid = lineageDepthEntity.getGuid();
        //直接上游表数量
        CompletableFuture<Long> directUpStreamNumCompletableFuture = CompletableFuture.supplyAsync(() ->
                (long) metaspaceLineageService.getEntityDirectNum(guid, AtlasLineageInfo.LineageDirection.INPUT), threadPoolExecutor);
        //直接下游表数量
        CompletableFuture<Long> directDownStreamNumCompletableFuture = CompletableFuture.supplyAsync(() ->
                (long) metaspaceLineageService.getEntityDirectNum(guid, AtlasLineageInfo.LineageDirection.OUTPUT), threadPoolExecutor);
        //上游表层数
        CompletableFuture<Long> upStreamLevelNumCompletableFuture = CompletableFuture.supplyAsync(() ->
                (long) metaspaceLineageService.getLineageDepth(guid, AtlasLineageInfo.LineageDirection.INPUT), threadPoolExecutor);
        //下游表层数
        CompletableFuture<Long> downStreamLevelNumCompletableFuture = CompletableFuture.supplyAsync(() ->
                (long) metaspaceLineageService.getLineageDepth(guid, AtlasLineageInfo.LineageDirection.OUTPUT), threadPoolExecutor);
        try {
            lineageDepthEntity.setDirectUpStreamNum(directUpStreamNumCompletableFuture.get());
            lineageDepthEntity.setDirectDownStreamNum(directDownStreamNumCompletableFuture.get());
            lineageDepthEntity.setUpStreamLevelNum(upStreamLevelNumCompletableFuture.get());
            lineageDepthEntity.setDownStreamLevelNum(downStreamLevelNumCompletableFuture.get());
        } catch (AtlasBaseException | InterruptedException | ExecutionException e) {
            LOG.error("获取上下游表数量和层级失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取上下游表数量和层级失败");
        }
        return lineageDepthEntity;
    }

    /**
     * 字段血缘
     *
     * @param guid
     * @param direction
     * @param depth
     * @return
     * @throws AtlasBaseException
     */
    public ColumnLineageInfo getColumnLineageV2(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        List<String> tables = new ArrayList<>();
        tables.add(guid);
        List<String> relatedTables = metaspaceLineageService.getColumnRelatedTable(guid, direction, depth);
        tables.addAll(relatedTables);
        ColumnLineageInfo.LineageEntity entity = null;
        List<ColumnLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
        for (String tableGuid : tables) {
            AtlasEntity tableEntity = entitiesStore.getById(tableGuid).getEntity();
            String tableName = (String) tableEntity.getAttribute("name");
            AtlasRelatedObjectId db = (AtlasRelatedObjectId) tableEntity.getRelationshipAttribute("db");
            String dbName = db.getDisplayText();
            String dbGuid = db.getGuid();
            String dbStatus = db.getEntityStatus().name();
            String tableStatus = tableEntity.getStatus().name();
            List<AtlasRelatedObjectId> columns = (List<AtlasRelatedObjectId>) tableEntity.getRelationshipAttribute("columns");
            for (int i = 0, size = columns.size(); i < size; i++) {
                AtlasRelatedObjectId column = columns.get(i);
                entity = new ColumnLineageInfo.LineageEntity();
                entity.setColumnName(column.getDisplayText());
                entity.setGuid(column.getGuid());
                entity.setDbGuid(dbGuid);
                entity.setDbName(dbName);
                entity.setTableGuid(tableGuid);
                entity.setTableName(tableName);
                entity.setDbStatus(dbStatus);
                entity.setTableStatus(tableStatus);
                entity.setColumnStatus(column.getEntityStatus().name());
                lineageEntities.add(entity);
            }
        }
        AtlasLineageInfo lineageInfo = metaspaceLineageService.getColumnLineageInfo(guid, direction, depth);
        Set<LineageTrace> lineageRelations = getRelations(lineageInfo);
        ColumnLineageInfo info = new ColumnLineageInfo();
        //guid
        info.setGuid(guid);
        Set<LineageTrace> resultRelations = reOrderRelation(lineageEntities, lineageRelations);
        removeTableEntityAndRelation(lineageEntities, lineageRelations);
        info.setEntities(lineageEntities);
        info.setRelations(resultRelations);
        return info;
    }


    public ColumnLineageInfo getColumnLineages(String tableGuid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {

        Map<String, Object> cache = new HashMap<>();

        AtlasLineageInfo lineageInfo = metaspaceLineageService.getColumnLineageInfo(tableGuid, direction, depth);
        Set<LineageTrace> lineageRelations = getRelations(lineageInfo);

//        Set<LineageTrace> lineageRelations = getLineageRelations(tableGuid, direction, depth, cache);
        List<ColumnLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();

        for (LineageTrace resultRelation : lineageRelations) {
            addLineageEntity(resultRelation.getFromEntityId(), lineageEntities, cache);
            addLineageEntity(resultRelation.getToEntityId(), lineageEntities, cache);
        }

        Set<LineageTrace> resultRelations = reOrderRelation(lineageEntities, lineageRelations);
        ColumnLineageInfo info = new ColumnLineageInfo();
        //guid
        info.setGuid(tableGuid);
        info.setEntities(lineageEntities);
        info.setRelations(resultRelations);
        return info;
    }

    private Set<LineageTrace> getLineageRelations(String tableGuid, AtlasLineageInfo.LineageDirection direction, int depth, Map<String, Object> cache) {
        AtlasEntity tableEntity = getAtlasEntity(tableGuid, cache);
        List<AtlasRelatedObjectId> columns = (List<AtlasRelatedObjectId>) tableEntity.getRelationshipAttribute("columns");
        Set<LineageTrace> lineageRelations = new HashSet<>();

        for (int i = 0, size = columns.size(); i < size; i++) {
            AtlasRelatedObjectId column = columns.get(i);
            String columnGuid = column.getGuid();
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(columnGuid, direction, depth);
            if(null == lineageInfo){
                continue;
            }
            Set<LineageTrace> relations = getRelations(lineageInfo);
            if(null != relations && !relations.isEmpty()){
                lineageRelations.addAll(relations);
            }
        }
        return lineageRelations;
    }

    private ColumnLineageInfo.LineageEntity addLineageEntity(String columnGuid, List<ColumnLineageInfo.LineageEntity> lineageEntities, Map<String, Object> cache) {
        String columnLineageKey = "columnLineage_" + columnGuid;
        if(cache.containsKey(columnLineageKey)){
            return (ColumnLineageInfo.LineageEntity)cache.get(columnLineageKey);
        }
        ColumnLineageInfo.LineageEntity entity;
        AtlasEntity columnEntity = getAtlasEntity(columnGuid, cache);
        String typeName = columnEntity.getTypeName();
        if(!"rdbms_column".equalsIgnoreCase(typeName)&&!"hive_column".equalsIgnoreCase(typeName)&&!"column".equalsIgnoreCase(typeName)){
            return null;
        }
        entity = new ColumnLineageInfo.LineageEntity();
        entity.setGuid(columnGuid);
        entity.setColumnName(getAttName(columnEntity));
        entity.setColumnStatus(getAttStatus(columnEntity));
        AtlasRelatedObjectId table = (AtlasRelatedObjectId) columnEntity.getRelationshipAttribute("table");
        String tableGuid = table.getGuid();
        entity.setTableGuid(tableGuid);
        entity.setTableName(table.getDisplayText());
        entity.setTableStatus(table.getRelationshipStatus().name());
        AtlasEntity tableEntity = getAtlasEntity(tableGuid, cache);
        AtlasRelatedObjectId db = (AtlasRelatedObjectId) tableEntity.getRelationshipAttribute("db");
        String dbGuid = db.getGuid();
        entity.setDbGuid(dbGuid);
        entity.setDbName(db.getDisplayText());
        entity.setDbStatus(db.getRelationshipStatus().name());
        lineageEntities.add(entity);
        cache.put(columnLineageKey, entity);
        return entity;
    }

    private AtlasEntity getAtlasEntity(String guid, Map<String, Object> cache){
        if(cache.containsKey(guid)){
            return (AtlasEntity)cache.get(guid);
        }
        return entitiesStore.getById(guid).getEntity();
    }

    public String getAttName(AtlasEntity fromEntity){
        return (String)fromEntity.getAttribute("name");
    }
    public String getAttStatus(AtlasEntity fromEntity){
        String status = null;
        Object object = fromEntity.getStatus();
        if (object instanceof AtlasTermRelationshipStatus) {
            status = ((AtlasTermRelationshipStatus) object).name();
        }else if(object instanceof AtlasEntity.Status){
            status = ((AtlasEntity.Status)object).name();
        }else{
            status = (String)object;
        }
        return status;

    }


    public void removeTableEntityAndRelation(List<ColumnLineageInfo.LineageEntity> lineageEntities, Set<LineageTrace> lineageRelations) throws AtlasBaseException {
        Set<LineageTrace> removeNode = new HashSet<>();
        Set<ColumnLineageInfo.LineageEntity> removeEntity = new HashSet<>();
        for (ColumnLineageInfo.LineageEntity entity : lineageEntities) {
            String guid = entity.getGuid();
            AtlasEntityHeader header = entitiesStore.getHeaderById(guid);
            String typeName = header.getTypeName();
            if (typeName.contains(tableAttribute)) {
                removeEntity.add(entity);
                Iterator<LineageTrace> iterator = lineageRelations.iterator();
                while (iterator.hasNext()) {
                    LineageTrace node = iterator.next();
                    if (node.getFromEntityId().equals(guid))
                        removeNode.add(node);
                }
            }
        }
        removeNode.stream().forEach(node -> lineageRelations.remove(node));
        removeEntity.stream().forEach(node -> lineageEntities.remove(node));
    }

    /**
     * 去除Process节点
     *
     * @param lineageEntities
     * @param lineageRelations
     */
    public Set<LineageTrace> reOrderRelation(List<ColumnLineageInfo.LineageEntity> lineageEntities, Set<LineageTrace> lineageRelations) throws AtlasBaseException {
        Set<LineageTrace> resultRelation = new HashSet<>();
        LineageTrace trace = null;
        for (ColumnLineageInfo.LineageEntity entity : lineageEntities) {
            String fromGuid = entity.getGuid();
            Iterator<LineageTrace> fromIterator = lineageRelations.iterator();
            while (fromIterator.hasNext()) {
                LineageTrace fromNode = fromIterator.next();
                if (fromNode.getFromEntityId().equals(fromGuid)) {
                    String toGuid = fromNode.getToEntityId();
                    Iterator<LineageTrace> toIterator = lineageRelations.iterator();
                    while (toIterator.hasNext()) {
                        LineageTrace toNode = toIterator.next();
                        if (toNode.getFromEntityId().equals(toGuid)) {
                            trace = new LineageTrace();
                            trace.setFromEntityId(fromGuid);
                            trace.setToEntityId(toNode.getToEntityId());
                            resultRelation.add(trace);
                        }
                    }
                }
            }
        }
        return resultRelation;
    }

    public LineageDepthInfo getColumnLineageDepthInfo(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getLineageInfo({})", guid);
        }
        try {
            LineageDepthInfo lineageDepthEntity = new LineageDepthInfo();
            AtlasEntity entity = entitiesStore.getById(guid).getEntity();
            if (Objects.nonNull(entity)) {
                String columnType = "column";
                if (entity.getTypeName().contains(columnType)) {
                    //guid
                    lineageDepthEntity.setGuid(guid);
                    AtlasEntity atlasColumnEntity = getEntityById(guid);
                    //columnName && displayText
                    lineageDepthEntity.setDisplayText(getEntityAttribute(atlasColumnEntity, "name"));
                    //updateTime
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(atlasColumnEntity.getUpdateTime());
                    lineageDepthEntity.setUpdateTime(formatDateStr);

                    AtlasRelatedObjectId relatedTable = (AtlasRelatedObjectId) atlasColumnEntity.getRelationshipAttribute("table");
                    if (Objects.nonNull(relatedTable)) {
                        AtlasEntity atlasTableEntity = entitiesStore.getById(relatedTable.getGuid()).getEntity();
                        //tableName
                        if (atlasTableEntity.hasAttribute(nameAttribute) && Objects.nonNull(atlasTableEntity.getAttribute(nameAttribute)))
                            lineageDepthEntity.setTableName(atlasTableEntity.getAttribute(nameAttribute).toString());
                        AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                        if (Objects.nonNull(relatedObject)) {
                            //dbName
                            lineageDepthEntity.setDbName(relatedObject.getDisplayText());
                        }
                    }
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }
            return lineageDepthEntity;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘深度详情失败");
        }
    }

    public Set<LineageTrace> getRelations(AtlasLineageInfo lineageInfo) {
        Set<AtlasLineageInfo.LineageRelation> relations = lineageInfo.getRelations();
        //relations
        Iterator<AtlasLineageInfo.LineageRelation> it = relations.iterator();
        Set<LineageTrace> lineageRelations = new HashSet<>();
        LineageTrace relation = null;
        while (it.hasNext()) {
            AtlasLineageInfo.LineageRelation atlasRelation = it.next();
            relation = new LineageTrace();
            relation.setFromEntityId(atlasRelation.getFromEntityId());
            relation.setToEntityId(atlasRelation.getToEntityId());
            lineageRelations.add(relation);
        }
        return lineageRelations;
    }

    public TableLineageInfo.LineageEntity getTableEntityInfo(String guid, TableLineageInfo.LineageEntity lineageEntity, Map<String, AtlasEntityHeader> entities, AtlasEntityHeader atlasEntity) throws AtlasBaseException {
        //guid
        if (Objects.nonNull(atlasEntity.getGuid()))
            lineageEntity.setGuid(atlasEntity.getGuid());
        lineageEntity.setProcess(false);
        //status
        lineageEntity.setStatus(atlasEntity.getStatus().name());
        //typeName
        lineageEntity.setTypeName(atlasEntity.getTypeName());
        AtlasEntityDef entityDef = typeDefStore.getEntityDefByName(atlasEntity.getTypeName());
        Set<String> types = entityDef.getSuperTypes();
        Iterator<String> typeIterator = types.iterator();
        if (Objects.nonNull(typeIterator) && typeIterator.hasNext()) {
            String type = typeIterator.next();
            String processType = "Process";
            if (type.contains(processType))
                lineageEntity.setProcess(true);
        }
        lineageEntity.setStatus(atlasEntity.getStatus().name());
        //displayText
        if (Objects.nonNull(atlasEntity.getDisplayText())) {
            lineageEntity.setDisplayText(atlasEntity.getDisplayText());
        }
        AtlasEntity atlasTableEntity = getEntityInfoByGuid(guid, null, Collections.singletonList("columnLineages")).getEntity();
//        AtlasEntity atlasTableEntity = entitiesStore.getById(guid).getEntity();
        //tableName
        if (atlasEntity.hasAttribute(nameAttribute) && Objects.nonNull(atlasEntity.getAttribute(nameAttribute)))
            lineageEntity.setTableName(atlasEntity.getAttribute(nameAttribute).toString());
        //dbName
        AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
        if (Objects.nonNull(relatedObject))
            lineageEntity.setDbName(relatedObject.getDisplayText());
        return lineageEntity;
    }

    public Set<AtlasLineageInfo.LineageRelation> getOutDirectRelationNode(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for (Iterator it = relations.iterator(); it.hasNext(); ) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation) it.next();
            if (relation.getFromEntityId().equals(guid)) {
                directRelations.add(relation);
            }
        }
        return directRelations;
    }

    public Set<AtlasLineageInfo.LineageRelation> getInDirectRelationNode(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<String> processGuids = new HashSet<>();
        for (Iterator it = relations.iterator(); it.hasNext(); ) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation) it.next();
            if (relation.getToEntityId().equals(guid)) {
                processGuids.add(relation.getFromEntityId());
            }
        }

        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for (Iterator proIter = processGuids.iterator(); proIter.hasNext(); ) {
            String processGuid = (String) proIter.next();
            for (Iterator it = relations.iterator(); it.hasNext(); ) {
                AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation) it.next();
                if (relation.getToEntityId().equals(processGuid)) {
                    directRelations.add(relation);
                }
            }
        }
        return directRelations;
    }

    public Long getOutMaxDepth(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = getOutDirectRelationNode(guid, relations);
        long max = 0;
        for (Iterator it = directRelations.iterator(); it.hasNext(); ) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation) it.next();
            max = Math.max(max, getOutMaxDepth(relation.getToEntityId(), relations));
        }
        return max + 1;
    }

    public Long getInMaxDepth(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = getInDirectRelationNode(guid, relations);
        long max = 0;
        for (Iterator it = directRelations.iterator(); it.hasNext(); ) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation) it.next();
            max = Math.max(max, getInMaxDepth(relation.getFromEntityId(), relations));
        }
        return max + 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTable(TableEdit tableEdit) throws AtlasBaseException {
        String guid = tableEdit.getGuid();
        String description = tableEdit.getDescription();
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "提交修改信息有误");
        }
        //修改描述
        try {
            AtlasEntity entity = getEntityById(guid);
            String tableName = getEntityAttribute(entity, "name");
            AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
            String dbName = relatedObject.getDisplayText();
            String sql = String.format("alter table %s set tblproperties('comment'='%s')", tableName, description);
            AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
            adapterSource.getNewAdapterExecutor().execute(adapterSource.getConnection(AdminUtils.getUserName(), dbName, MetaspaceConfig.getHiveJobQueueName()), sql);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改表描述失败");
        }
    }

    @CacheEvict(value = "columnCache", allEntries = true)
    public void updateColumnDescription(List<ColumnEdit> columnEdits) throws AtlasBaseException {
        if (Objects.isNull(columnEdits))
            throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "提交修改信息有误");
        try {
            for (int i = 0; i < columnEdits.size(); i++) {
                ColumnEdit columnEdit = columnEdits.get(i);
                AtlasEntity entity = getEntityById(columnEdit.getTableId());
                String tableName = getEntityAttribute(entity, "name");
                AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
                String dbName = relatedObject.getDisplayText();
                String columnName = columnEdit.getColumnName();
                String type = columnEdit.getType();
                String description = columnEdit.getDescription();
                String sql = String.format("alter table %s change column %s %s %s comment '%s'", tableName, columnName, columnName, type, description);
                AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
                adapterSource.getNewAdapterExecutor().execute(adapterSource.getConnection(AdminUtils.getUserName(), dbName, MetaspaceConfig.getHiveJobQueueName()), sql);
            }
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改字段信息失败");
        }
    }

    @CacheEvict(value = {"columnCache", "tablePageCache", "columnPageCache", "databaseSearchCache", "TableByDBCache", "TimeAndDbCache", "DbTotalCache", "TbTotalCache"}, allEntries = true)
    public void refreshCache() {
        LOG.info("元数据管理缓存已被清除");
    }

    @CacheEvict(value = {"RDBMSDataSourceSearchCache", "RDBMSDBBySourceCache", "RDBMSTableByDBCache", "RDBMSDBPageCache", "RDBMSTablePageCache", "RDBMSColumnPageCache"}, allEntries = true)
    public void refreshRDBMSCache() {
        LOG.info("关系型元数据管理缓存已被清除");
    }

    @CacheEvict(value = {"RDBMSTableByDBCache", "RDBMSTablePageCache"}, allEntries = true)
    public void refreshRDBMSTableCache() {
        LOG.info("关系型表元数据管理缓存已被清除");
    }

    public List<DataOwnerHeader> getDataOwner(String guid) throws AtlasBaseException {
        try {
            List<DataOwnerHeader> owners = tableDAO.getDataOwnerList(guid);
            return owners;
        } catch (Exception e) {
            LOG.error("获取数据失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    public List<String> getDataOwnerId(String guid) throws AtlasBaseException {
        try {
            List<String> ownerIdList = tableDAO.getDataOwnerIdList(guid);
            return ownerIdList;
        } catch (Exception e) {
            LOG.error("获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public int deleteTableOwner(String tableGuid, List<String> ownerList) {
        try {
            return tableDAO.deleteTableOwner(tableGuid, ownerList);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public EntityMutationResponse hardDeleteByGuid(String guid) throws AtlasBaseException {
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
            AtlasEntity entity = info.getEntity();
            AtlasEntity.Status status = entity.getStatus();
            if (AtlasEntity.Status.DELETED != status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前实体未被删除，禁止使用硬删除");
            }
            List<String> deleteAllGuids = new ArrayList<>();
            List<String> deleteTableGuids = new ArrayList<>();
            deleteAllGuids.add(guid);
            //如果是数据库，则查找数据库下的表
            String hiveDb = "hive_db";
            if (entity.getTypeName().equals(hiveDb)) {
                Map<String, Object> relationshipAttributes = entity.getRelationshipAttributes();
                if (null != relationshipAttributes) {
                    for (Object collection : relationshipAttributes.values()) {
                        if (Objects.nonNull(collection) && collection instanceof ArrayList) {
                            List<Object> list = (List<Object>) collection;
                            for (Object object : list) {
                                if (Objects.nonNull(object) && object instanceof AtlasRelatedObjectId) {
                                    AtlasRelatedObjectId relatedObjectId = (AtlasRelatedObjectId) object;
                                    deleteTableGuids.add(relatedObjectId.getGuid());
                                }
                            }
                        }
                    }
                    deleteAllGuids.addAll(deleteTableGuids);
                }
            }

            EntityMutationResponse response = entitiesStore.hardDeleteById(deleteAllGuids);
            for (String tableGuid : deleteAllGuids) {
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
            return response;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "硬删除失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public EntityMutationResponse hardDeleteRDBMSByGuid(String guid) throws AtlasBaseException {
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid, true);
            AtlasEntity entity = info.getEntity();
            AtlasEntity.Status status = entity.getStatus();
            if (AtlasEntity.Status.DELETED != status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前实体未被删除，禁止使用硬删除");
            }
            List<String> deleteAllGuids = new ArrayList<>();
            deleteAllGuids.add(guid);
            EntityMutationResponse response = entitiesStore.hardDeleteById(deleteAllGuids);
            return response;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "硬删除失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public EntityMutationResponse hardDeleteRDBMSInstanceByGuid(String guid) throws AtlasBaseException {
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid, true);
            AtlasEntity entity = info.getEntity();
            AtlasEntity.Status status = entity.getStatus();
            if (!RMDB_INSTANCE.equalsIgnoreCase(entity.getTypeName())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前实体并非数据源，不能删除");
            }
            if (AtlasEntity.Status.ACTIVE == status) {
                entitiesStore.deleteById(guid);
            }
            List<String> deleteAllGuids = new ArrayList<>();
            deleteAllGuids.add(guid);
            EntityMutationResponse response = entitiesStore.hardDeleteById(deleteAllGuids);
            return response;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "硬删除失败");
        }
    }

    public int updateTableEditInfo(String tableGuid, Table info) throws AtlasBaseException {
        try {
            return tableDAO.updateTableEditInfo(tableGuid, info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新失败");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateTableInfo(String tableGuid, Table tableInfo) throws AtlasBaseException {
        try {
            tableDAO.updateTableInfo(tableGuid, tableInfo);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }


    public File exportExcel(List<String> dbGuidList, String tenantId) throws AtlasBaseException {
        try {
            List<Table> tableList = new ArrayList<>();
            List<String> tableGuidList = new ArrayList<>();
            if (null != dbGuidList) {
                for (String dbGuid : dbGuidList) {
                    PageResult<Table> tablePageResult = searchService.getTableByDB(dbGuid, true, 0, -1);
                    tablePageResult.getLists().stream().forEach(table -> tableGuidList.add(table.getTableId()));
                }
            }
            for (String tableGuid : tableGuidList) {
                Table table = getTableInfoById(tableGuid, tenantId);
                tableList.add(table);
            }
            Workbook workbook = createMetaDataExcelFile(tableList, "xlsx");

            File file = new File("metadata.xlsx");
            FileOutputStream output = new FileOutputStream(file);
            workbook.write(output);
            output.flush();
            output.close();
            return file;
        } catch (Exception e) {
            LOG.error("导出Excel失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "导出Excel失败");
        }
    }


    public Workbook createMetaDataExcelFile(List<Table> tableList, String extension) {
        Workbook workbook = null;
        if (StringUtils.isBlank(extension)) {
            return null;
        }

        if (extension.equalsIgnoreCase(XLS)) {
            // 2003版本
            workbook = new HSSFWorkbook();
        } else if (extension.equalsIgnoreCase(XLSX)) {
            // 2007版本
            workbook = new XSSFWorkbook();
        }

        if (workbook != null) {
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

            CellStyle cellStyle = workbook.createCellStyle();
            //下边框
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            //左边框
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            //上边框
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);
            //右边框
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            for (int i = 0; i < tableList.size(); i++) {
                Table table = tableList.get(i);
                createMetadataTableSheet(workbook, i + 1, table, headerStyle, cellStyle);
                createMetadataColumnSheet(workbook, i + 1, table, headerStyle, cellStyle);
            }
        }
        return workbook;
    }


    public void createMetadataTableSheet(Workbook workbook, int index, Table table, CellStyle headerStyle, CellStyle cellStyle) {
        String tableName = table.getTableName();
        String dbName = table.getDatabaseName();
        int rowNumber = 0;
        String sheetName = "表" + index + "-表信息";
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));

        CellRangeAddress tableAndDbNameRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(tableAndDbNameRangeAddress);
        Row tableAndDbNameRow = sheet.createRow(rowNumber++);
        Cell tableAndDbNameRowCell = tableAndDbNameRow.createCell(0);
        tableAndDbNameRowCell.setCellValue(dbName + "." + tableName + "表信息");
        tableAndDbNameRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);

        CellRangeAddress basicInfoRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(basicInfoRangeAddress);
        Row basicInfoRow = sheet.createRow(rowNumber++);
        Cell basicInfoRowCell = basicInfoRow.createCell(0);
        basicInfoRowCell.setCellValue("基础信息");
        basicInfoRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), basicInfoRangeAddress, sheet);

        Row tableNameRow = sheet.createRow(rowNumber++);
        Cell tableNameKeyCell = tableNameRow.createCell(0);
        tableNameKeyCell.setCellValue("表名称");
        tableNameKeyCell.setCellStyle(cellStyle);
        Cell tableNameValueCell = tableNameRow.createCell(1);
        tableNameValueCell.setCellValue(tableName);
        tableNameValueCell.setCellStyle(cellStyle);

        StringJoiner tagJoiner = new StringJoiner(",");
        table.getTags().forEach(tag -> tagJoiner.add(tag.getTagName()));
        Row tagRow = sheet.createRow(rowNumber++);
        Cell tagKeyCell = tagRow.createCell(0);
        tagKeyCell.setCellValue("标签");
        tableNameKeyCell.setCellStyle(cellStyle);
        Cell tagValueCell = tagRow.createCell(1);
        tagValueCell.setCellValue(tagJoiner.toString());
        tagValueCell.setCellStyle(cellStyle);

        String creator = table.getOwner();
        Row creatorRow = sheet.createRow(rowNumber++);
        Cell creatorKeyCell = creatorRow.createCell(0);
        creatorKeyCell.setCellStyle(cellStyle);
        creatorKeyCell.setCellValue("创建人");
        Cell creatorValueCell = creatorRow.createCell(1);
        creatorValueCell.setCellValue(creator);
        creatorValueCell.setCellStyle(cellStyle);

        StringJoiner dataOwnerJoiner = new StringJoiner(",");
        table.getDataOwner().forEach(dataOwnerHeader -> dataOwnerJoiner.add(dataOwnerHeader.getName()));
        Row dataOwnerRow = sheet.createRow(rowNumber++);
        Cell dataOwnerKeyCell = dataOwnerRow.createCell(0);
        dataOwnerKeyCell.setCellValue("数据Owner");
        dataOwnerKeyCell.setCellStyle(cellStyle);
        Cell dataOwnerValueCell = dataOwnerRow.createCell(1);
        dataOwnerValueCell.setCellValue(dataOwnerJoiner.toString());
        dataOwnerValueCell.setCellStyle(cellStyle);

        String updateTime = table.getUpdateTime();
        Row updateTimeRow = sheet.createRow(rowNumber++);
        Cell updateTimeKeyCell = updateTimeRow.createCell(0);
        updateTimeKeyCell.setCellValue("更新时间");
        updateTimeKeyCell.setCellStyle(cellStyle);
        Cell updateTimeValueCell = updateTimeRow.createCell(1);
        updateTimeValueCell.setCellValue(updateTime);
        updateTimeValueCell.setCellStyle(cellStyle);

        Row dbNameRow = sheet.createRow(rowNumber++);
        Cell dbNameKeyCell = dbNameRow.createCell(0);
        dbNameKeyCell.setCellValue("所属数据库");
        dbNameKeyCell.setCellStyle(cellStyle);
        Cell dbNameValueCell = dbNameRow.createCell(1);
        dbNameValueCell.setCellValue(dbName);
        dbNameValueCell.setCellStyle(cellStyle);

        String type = table.getType();
        Row typeRow = sheet.createRow(rowNumber++);
        Cell typeKeyCell = typeRow.createCell(0);
        typeKeyCell.setCellValue("类型");
        typeKeyCell.setCellStyle(cellStyle);
        Cell typeValueCell = typeRow.createCell(1);
        typeValueCell.setCellValue("INTERNAL_TABLE".equals(type) ? "内部表" : "外部表");
        typeValueCell.setCellStyle(cellStyle);

        Boolean isPartitionTable = table.getPartitionTable();
        Row isPartitionTableRow = sheet.createRow(rowNumber++);
        Cell isPartitionTableKeyCell = isPartitionTableRow.createCell(0);
        isPartitionTableKeyCell.setCellValue("分区表");
        isPartitionTableKeyCell.setCellStyle(cellStyle);
        Cell isPartitionTableValueCell = isPartitionTableRow.createCell(1);
        isPartitionTableValueCell.setCellValue((true == isPartitionTable) ? "是" : "否");
        isPartitionTableValueCell.setCellStyle(cellStyle);

        String format = table.getFormat();
        Row formatRow = sheet.createRow(rowNumber++);
        Cell formatKeyCell = formatRow.createCell(0);
        formatKeyCell.setCellValue("格式");
        formatKeyCell.setCellStyle(cellStyle);
        Cell formatValueCell = formatRow.createCell(1);
        formatValueCell.setCellValue(format);
        formatValueCell.setCellStyle(cellStyle);

        String location = table.getLocation();
        Row locationRow = sheet.createRow(rowNumber++);
        Cell locationKeyCell = locationRow.createCell(0);
        locationKeyCell.setCellValue("位置");
        locationKeyCell.setCellStyle(cellStyle);
        Cell locationValueCell = locationRow.createCell(1);
        locationValueCell.setCellValue(location);
        locationValueCell.setCellStyle(cellStyle);

        String description = table.getDescription();
        Row descriptionRow = sheet.createRow(rowNumber++);
        Cell descriptionKeyCell = descriptionRow.createCell(0);
        descriptionKeyCell.setCellValue("描述");
        descriptionKeyCell.setCellStyle(cellStyle);
        Cell descriptionValueCell = descriptionRow.createCell(1);
        descriptionValueCell.setCellValue(description);
        descriptionValueCell.setCellStyle(cellStyle);

        CellRangeAddress sourceSystemRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(sourceSystemRangeAddress);
        Row sourceSystemRow = sheet.createRow(rowNumber++);
        Cell sourceSystemRowCell = sourceSystemRow.createCell(0);
        sourceSystemRowCell.setCellValue("源系统维度");
        sourceSystemRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), sourceSystemRangeAddress, sheet);

        String subordinateSystem = table.getSubordinateSystem();
        Row subordinateSystemRow = sheet.createRow(rowNumber++);
        Cell subordinateSystemKeyCell = subordinateSystemRow.createCell(0);
        subordinateSystemKeyCell.setCellValue("所属系统");
        subordinateSystemKeyCell.setCellStyle(cellStyle);
        Cell subordinateSystemValueCell = subordinateSystemRow.createCell(1);
        subordinateSystemValueCell.setCellValue(subordinateSystem);
        subordinateSystemValueCell.setCellStyle(cellStyle);

        String subordinateDatabase = table.getSubordinateDatabase();
        Row subordinateDatabaseRow = sheet.createRow(rowNumber++);
        Cell subordinateDatabaseKeyCell = subordinateDatabaseRow.createCell(0);
        subordinateDatabaseKeyCell.setCellValue("所属数据库");
        subordinateDatabaseKeyCell.setCellStyle(cellStyle);
        Cell subordinateDatabaseValueCell = subordinateDatabaseRow.createCell(1);
        subordinateDatabaseValueCell.setCellValue(subordinateDatabase);
        subordinateDatabaseValueCell.setCellStyle(cellStyle);

        String systemAdmin = table.getSystemAdmin();
        Row systemAdminRow = sheet.createRow(rowNumber++);
        Cell systemAdminKeyCell = systemAdminRow.createCell(0);
        systemAdminKeyCell.setCellValue("源系统管理员");
        systemAdminKeyCell.setCellStyle(cellStyle);
        Cell systemAdminValueCell = systemAdminRow.createCell(1);
        systemAdminValueCell.setCellValue(systemAdmin);
        systemAdminValueCell.setCellStyle(cellStyle);

        String createTime = table.getCreateTime();
        Row createTimeRow = sheet.createRow(rowNumber++);
        Cell createTimeKeyCell = createTimeRow.createCell(0);
        createTimeKeyCell.setCellValue("表创建时间");
        createTimeKeyCell.setCellStyle(cellStyle);
        Cell createTimeValueCell = createTimeRow.createCell(1);
        createTimeValueCell.setCellValue(createTime);
        createTimeValueCell.setCellStyle(cellStyle);

        CellRangeAddress dataWarehouseRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(dataWarehouseRangeAddress);
        Row dataWarehouseRow = sheet.createRow(rowNumber++);
        Cell dataWarehouseRowCell = dataWarehouseRow.createCell(0);
        dataWarehouseRowCell.setCellValue("数仓维度");
        dataWarehouseRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), dataWarehouseRangeAddress, sheet);

        String dataWarehouseAdmin = table.getDataWarehouseAdmin();
        Row dataWarehouseAdminRow = sheet.createRow(rowNumber++);
        Cell dataWarehouseAdminKeyCell = dataWarehouseAdminRow.createCell(0);
        dataWarehouseAdminKeyCell.setCellValue("数仓管理员");
        dataWarehouseAdminKeyCell.setCellStyle(cellStyle);
        Cell dataWarehouseAdminValueCell = dataWarehouseAdminRow.createCell(1);
        dataWarehouseAdminValueCell.setCellValue(dataWarehouseAdmin);
        dataWarehouseAdminValueCell.setCellStyle(cellStyle);

        String dataWarehouseDescription = table.getDataWarehouseDescription();
        Row dataWarehouseDescriptionRow = sheet.createRow(rowNumber++);
        Cell dataWarehouseDescriptionKeyCell = dataWarehouseDescriptionRow.createCell(0);
        dataWarehouseDescriptionKeyCell.setCellValue("描述");
        dataWarehouseAdminKeyCell.setCellStyle(cellStyle);
        Cell dataWarehouseDescriptionValueCell = dataWarehouseDescriptionRow.createCell(1);
        dataWarehouseDescriptionValueCell.setCellValue(dataWarehouseDescription);
        dataWarehouseDescriptionValueCell.setCellStyle(cellStyle);

        CellRangeAddress catalogRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
        sheet.addMergedRegion(catalogRangeAddress);
        Row catalogRangeAddressRow = sheet.createRow(rowNumber++);
        Cell catalogRangeAddressRowCell = catalogRangeAddressRow.createCell(0);
        catalogRangeAddressRowCell.setCellValue("目录维度");
        catalogRangeAddressRowCell.setCellStyle(headerStyle);

        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), catalogRangeAddress, sheet);

        StringJoiner catalogJoiner = new StringJoiner(",");
        table.getRelations().forEach(relation -> catalogJoiner.add(relation));
        Row catalogRow = sheet.createRow(rowNumber++);
        Cell catalogKeyCell = catalogRow.createCell(0);
        catalogKeyCell.setCellValue("所属目录");
        catalogKeyCell.setCellStyle(cellStyle);
        Cell catalogValueCell = catalogRow.createCell(1);
        catalogValueCell.setCellValue(catalogJoiner.toString());
        catalogValueCell.setCellStyle(cellStyle);

        String catalogAdmin = table.getCatalogAdmin();
        Row catalogAdminRow = sheet.createRow(rowNumber++);
        Cell catalogAdminKeyCell = catalogAdminRow.createCell(0);
        catalogAdminKeyCell.setCellValue("目录管理员");
        catalogAdminKeyCell.setCellStyle(cellStyle);
        Cell catalogAdminValueCell = catalogAdminRow.createCell(1);
        catalogAdminValueCell.setCellValue(catalogAdmin);
        catalogAdminValueCell.setCellStyle(cellStyle);

        String relationTime = table.getRelationTime();
        Row relationTimeRow = sheet.createRow(rowNumber++);
        Cell relationTimeKeyCell = relationTimeRow.createCell(0);
        relationTimeKeyCell.setCellValue("数据关联时间");
        relationTimeKeyCell.setCellStyle(cellStyle);
        Cell relationTimeValueCell = relationTimeRow.createCell(1);
        relationTimeValueCell.setCellValue(relationTime);
        relationTimeValueCell.setCellStyle(cellStyle);

        List<Table.BusinessObject> businessObjectList = table.getBusinessObjects();
        for (Table.BusinessObject businessObject : businessObjectList) {
            CellRangeAddress businessRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 1);
            sheet.addMergedRegion(businessRangeAddress);
            Row businessRangeAddressRow = sheet.createRow(rowNumber++);
            Cell businessRangeAddressRowCell = businessRangeAddressRow.createCell(0);
            businessRangeAddressRowCell.setCellValue("业务维度");
            businessRangeAddressRowCell.setCellStyle(headerStyle);

            RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);
            RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);
            RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);
            RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), businessRangeAddress, sheet);

            String object = businessObject.getBusinessObject();
            Row objectRow = sheet.createRow(rowNumber++);
            Cell objectKeyCell = objectRow.createCell(0);
            objectKeyCell.setCellValue("对应业务对象");
            objectKeyCell.setCellStyle(cellStyle);
            Cell objectValueCell = objectRow.createCell(1);
            objectValueCell.setCellValue(object);
            objectValueCell.setCellStyle(cellStyle);

            String department = businessObject.getDepartment();
            Row departmentRow = sheet.createRow(rowNumber++);
            Cell departmentKeyCell = departmentRow.createCell(0);
            departmentKeyCell.setCellValue("所属部门");
            departmentKeyCell.setCellStyle(cellStyle);
            Cell departmentValueCell = departmentRow.createCell(1);
            departmentValueCell.setCellValue(department);
            departmentValueCell.setCellStyle(cellStyle);

            String businessLeader = businessObject.getBusinessLeader();
            Row businessLeaderRow = sheet.createRow(rowNumber++);
            Cell businessLeaderKeyCell = businessLeaderRow.createCell(0);
            businessLeaderKeyCell.setCellValue("业务负责人");
            businessLeaderKeyCell.setCellStyle(cellStyle);
            Cell businessLeaderValueCell = businessLeaderRow.createCell(1);
            businessLeaderValueCell.setCellValue(businessLeader);
            businessLeaderValueCell.setCellStyle(cellStyle);
        }


        sheet.autoSizeColumn(0, true);
        sheet.autoSizeColumn(1, true);
    }

    public void createMetadataColumnSheet(Workbook workbook, int index, Table table, CellStyle headerStyle, CellStyle cellStyle) {

        String tableName = table.getTableName();
        String dbName = table.getDatabaseName();
        int rowNumber = 0;
        String sheetName = "表" + index + "-字段信息";
        Sheet sheet = workbook.createSheet(CustomStringUtils.handleExcelName(sheetName));

        List<Column> columnList = table.getColumns();
        List<Column> normalColumnList = columnList.stream().filter(column -> !column.getPartitionKey()).collect(Collectors.toList());
        List<Column> partitionColumnList = columnList.stream().filter(column -> column.getPartitionKey()).collect(Collectors.toList());


        CellRangeAddress tableAndDbNameRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 2);
        sheet.addMergedRegion(tableAndDbNameRangeAddress);
        Row tableAndDbNameRow = sheet.createRow(rowNumber++);
        Cell tableAndDbNameRowCell = tableAndDbNameRow.createCell(0);
        tableAndDbNameRowCell.setCellValue(dbName + "." + tableName + "字段信息");
        tableAndDbNameRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), tableAndDbNameRangeAddress, sheet);

        CellRangeAddress normalColumnRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 2);
        sheet.addMergedRegion(normalColumnRangeAddress);
        Row normalColumnRow = sheet.createRow(rowNumber++);
        Cell normalColumnRowCell = normalColumnRow.createCell(0);
        normalColumnRowCell.setCellValue("普通字段");
        normalColumnRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);

        String[] headers = new String[]{"名称", "类型", "描述"};
        Row normalColumnHeaderRow = sheet.createRow(rowNumber++);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = normalColumnHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(cellStyle);
        }

        createDataCell(normalColumnList, sheet, rowNumber, cellStyle);
        rowNumber += normalColumnList.size();
        CellRangeAddress partitionColumnRangeAddress = new CellRangeAddress(rowNumber, rowNumber, 0, 2);
        sheet.addMergedRegion(partitionColumnRangeAddress);
        Row partitionColumnRow = sheet.createRow(rowNumber++);
        Cell partitionColumnRowCell = partitionColumnRow.createCell(0);
        partitionColumnRowCell.setCellValue("分区字段");
        partitionColumnRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), partitionColumnRangeAddress, sheet);

        Row partitionColumnHeaderRow = sheet.createRow(rowNumber++);
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = partitionColumnHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(cellStyle);
        }

        createDataCell(partitionColumnList, sheet, rowNumber, cellStyle);
    }

    public void createDataCell(List<Column> columnList, Sheet sheet, Integer rowNumber, CellStyle cellStyle) {
        for (int i = 0; i < columnList.size(); i++) {
            Row dataRow = sheet.createRow(rowNumber++);
            Column column = columnList.get(i);
            String columnName = column.getColumnName();
            String type = column.getType();
            String description = column.getDescription();
            Cell columnCell = dataRow.createCell(0);
            columnCell.setCellValue(columnName);
            columnCell.setCellStyle(cellStyle);
            Cell typeCell = dataRow.createCell(1);
            typeCell.setCellValue(type);
            typeCell.setCellStyle(cellStyle);
            Cell descriptionCell = dataRow.createCell(2);
            descriptionCell.setCellValue(description);
            descriptionCell.setCellStyle(cellStyle);
        }
    }


    public PageResult getTableHistoryList(String tableGuid, Parameters parameters) {
        PageResult pageResult = new PageResult();
        List<TableMetadata> tableMetadataList = metadataHistoryDAO.getTableMetadataList(tableGuid, parameters.getLimit(), parameters.getOffset());
        if (null != tableMetadataList && tableMetadataList.size() > 0) {
            Integer totalSize = tableMetadataList.get(0).getTotal();
            pageResult.setLists(tableMetadataList);
            pageResult.setCurrentSize(tableMetadataList.size());
            pageResult.setTotalSize(totalSize);
        }
        return pageResult;
    }

    public TableMetadata getTableMetadata(String tableGuid, Integer version) {
        return metadataHistoryDAO.getTableMetadata(tableGuid, version);
    }

    public List<ColumnMetadata> getColumnHistoryInfo(String tableGuid, Integer version, ColumnQuery query) {
        List<ColumnMetadata> columnMetadataList = metadataHistoryDAO.getColumnMetadataList(tableGuid, version, query.getColumnFilter());
        return columnMetadataList;
    }

    public ComparisonMetadata getComparisionTableMetadata(String tableGuid, Integer version) throws AtlasBaseException {
        ComparisonMetadata comparisonMetadata = new ComparisonMetadata();
        Set<String> changedFiledSet = new HashSet<>();
        try {
            TableMetadata currentMetadata = metadataHistoryDAO.getLastTableMetadata(tableGuid);
            TableMetadata oldMetadata = metadataHistoryDAO.getTableMetadata(tableGuid, version);
            Map<String, String> currentMetadataMap = BeanUtils.describe(currentMetadata);
            Map<String, String> oldMetadataMap = BeanUtils.describe(oldMetadata);
            for (String key : currentMetadataMap.keySet()) {
                String currentValue = currentMetadataMap.get(key);
                String oldValue = oldMetadataMap.get(key);
                currentValue = Objects.isNull(currentValue) ? "" : currentValue;
                oldValue = Objects.isNull(oldValue) ? "" : oldValue;
                if (!currentValue.equals(oldValue)) {
                    changedFiledSet.add(key);
                }
            }
            comparisonMetadata.setCurrentMetadata(currentMetadata);
            comparisonMetadata.setOldMetadata(oldMetadata);
            comparisonMetadata.setChangedSet(changedFiledSet);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return comparisonMetadata;
    }

    public ComparisonColumnMetadata getComparisionColumnMetadata(String tableGuid, Integer version) throws AtlasBaseException {
        ComparisonColumnMetadata comparisonMetadata = new ComparisonColumnMetadata();
        Set<String> changedFiledSet = new HashSet<>();
        comparisonMetadata.setChangedSet(changedFiledSet);
        try {
            List<ColumnMetadata> currentMetadata = metadataHistoryDAO.getLastColumnMetadata(tableGuid);
            List<ColumnMetadata> oldMetadata = metadataHistoryDAO.getColumnMetadata(tableGuid, version);

            //历史版本字段 获取字段描述信息
            List<Column>  columns = columnDAO.getColumnInfoListByTableGuid(tableGuid);
            if(CollectionUtils.isNotEmpty(columns)){
                for (Column item : columns){
                    currentMetadata.stream().forEach(t->{
                        if(StringUtils.equals(t.getGuid(),item.getColumnId())){
                            t.setDescription(item.getDescription());
                        }
                    });
                    oldMetadata.stream().forEach(t->{
                        if(StringUtils.equals(t.getGuid(),item.getColumnId())){
                            t.setDescription(item.getDescription());
                        }
                    });
                }
            }

            List<ColumnMetadata> orderMetadata = new ArrayList<>(); //记录有顺序的旧版字段
            comparisonMetadata.setOldMetadata(orderMetadata);
            if(CollectionUtils.isNotEmpty(currentMetadata)){
                if(version.compareTo(currentMetadata.get(0).getVersion())==0){
                    LOG.info("历史版本对比，版本一样，不需继续执行");
                    for (ColumnMetadata item : currentMetadata) {
                        Optional<ColumnMetadata> filterOpt = oldMetadata.stream().filter(p -> StringUtils.equalsIgnoreCase(item.getName(), p.getName())).findFirst();
                        if (filterOpt.isPresent()) {//名称一样比较类型
                            ColumnMetadata filter = filterOpt.get();
                            orderMetadata.add(filter);
                        }
                    }
                }else{
                    for (ColumnMetadata item : currentMetadata){
                        Optional<ColumnMetadata> filterOpt = oldMetadata.stream().filter(p->StringUtils.equalsIgnoreCase(item.getName(),p.getName())).findFirst();
                        if(filterOpt.isPresent()){//名称一样比较类型
                            ColumnMetadata filter = filterOpt.get();
                            orderMetadata.add(filter);

                            if(!StringUtils.equalsIgnoreCase(filter.getType(),item.getType()) ) {
                                item.setHasChange(true);
                                item.setChangeCause(ComparisonMetadata.ComparisonResult.CHANGE_CAUSE_TYPE_CHANGE);
                            }
                        }else{//旧版本找不到字段，则为新增
                            orderMetadata.add(new ColumnMetadata());
                            item.setHasChange(true);
                            item.setChangeCause(ComparisonMetadata.ComparisonResult.CHANGE_CAUSE_ADD);
                        }
                    }

                    List<String> currentColNames =  currentMetadata.stream().map(ColumnMetadata::getName).collect(Collectors.toList());
                    List<ColumnMetadata> deleteColumnList = oldMetadata.stream().filter(p->!currentColNames.contains(p.getName())).collect(Collectors.toList());
                    if(CollectionUtils.isNotEmpty(deleteColumnList)){
                        for(ColumnMetadata vo : deleteColumnList){
                            orderMetadata.add(vo);

                            ColumnMetadata e = new ColumnMetadata();
                            e.setHasChange(true);
                            e.setChangeCause(ComparisonMetadata.ComparisonResult.CHANGE_CAUSE_DELETE);
                            currentMetadata.add(e);
                        }
                    }
                }
            }

            comparisonMetadata.setCurrentMetadata(currentMetadata);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return comparisonMetadata;
    }

    @CacheEvict(value = {"RDBMSTableByDBCache", "TableByDBCache"}, allEntries = true)
    public void addMetadataSubscription(String tableGuid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            Timestamp generateTime = new Timestamp(System.currentTimeMillis());
            SubscriptionInfo subscriptionInfo = new SubscriptionInfo(userId, tableGuid, generateTime);
            metadataSubscribeDAO.addMetadataSubscription(subscriptionInfo);
        } catch (Exception e) {
            LOG.error("添加订阅元数据变更失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }


    @CacheEvict(value = {"RDBMSTableByDBCache", "TableByDBCache"}, allEntries = true)
    public void removeMetadataSubscription(String tableGuid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            metadataSubscribeDAO.removeMetadataSubscription(userId, tableGuid);
        } catch (Exception e) {
            LOG.error("添加订阅元数据变更失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public PageResult<MetaDataRelatedAPI> getTableInfluenceWithAPI(String tableGuid, Parameters parameters, String tenantId) {
        PageResult pageResult = new PageResult();
        List<MetaDataRelatedAPI> influenceAPIList = tableDAO.getTableInfluenceWithAPI(tableGuid, parameters.getLimit(), parameters.getOffset(), tenantId);
        influenceAPIList.forEach(api -> {
            String version = api.getVersion();
            String path = api.getPath();
            api.setPath("/api/" + version + "/share/" + path);
        });
        if (null != influenceAPIList && influenceAPIList.size() > 0) {
            Integer totalSize = influenceAPIList.get(0).getTotal();
            pageResult.setLists(influenceAPIList);
            pageResult.setCurrentSize(influenceAPIList.size());
            pageResult.setTotalSize(totalSize);
        }
        pageResult.setOffset(parameters.getOffset());
        return pageResult;
    }

    public List<TableHeader> getTableInfluenceWithDbAndTable(String tableGuid) throws AtlasBaseException {
        try {
            List<TableHeader> result = new ArrayList<>();
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(tableGuid, AtlasLineageInfo.LineageDirection.BOTH, 1);
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();

            for (String key : entities.keySet()) {
                TableHeader tableHeader = new TableHeader();
                AtlasEntityHeader atlasEntity = entities.get(key);
                if ("hive_process".equals(atlasEntity.getTypeName())) {
                    continue;
                }
                String guid = atlasEntity.getGuid();
                tableHeader.setTableId(guid);
                //tableName
                if (atlasEntity.hasAttribute(nameAttribute) && Objects.nonNull(atlasEntity.getAttribute(nameAttribute)))
                    tableHeader.setTableName(atlasEntity.getAttribute(nameAttribute).toString());
                //dbName
                AtlasEntity atlasTableEntity = entitiesStore.getById(guid).getEntity();
                AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                if (Objects.nonNull(relatedObject)) {
                    tableHeader.setDatabaseName(relatedObject.getGuid());
                    tableHeader.setDatabaseName(relatedObject.getDisplayText());
                }
                result.add(tableHeader);
            }
            return result;
        } catch (Exception e) {
            LOG.error("获取库表影响失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public CheckingInfo getCheckingTableInfo(String tableGuid) throws AtlasBaseException {
        CheckingInfo checkingInfo = new CheckingInfo();
        checkingInfo.setTableGuid(tableGuid);
        try {
            Table tableInfo = getTableInfoById(tableGuid, null);
            String tableName = tableInfo.getTableName();
            String namingConvention = "";
            boolean containChinese = isContainChinese(tableName);
            boolean contailSpecialChar = isSpecialChar(tableName);
            if (containChinese) {
                namingConvention += "(包含中文)";
            }
            if (contailSpecialChar) {
                namingConvention += "(包含特殊字符)";
            }
            if (namingConvention.length() == 0) {
                namingConvention = "符合规范";
            } else {
                namingConvention = "不符合" + namingConvention;
            }
            checkingInfo.setNamingConvention(namingConvention);

            int ratio = checkBasicInfo(tableInfo);
            checkingInfo.setFillRate(ratio);
            int proper = 100;
            if (ratio != proper) {
                checkingInfo.setMessageIntegrity("不完整");
            } else {
                checkingInfo.setMessageIntegrity("完整");
            }
            return checkingInfo;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("元数据稽核失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "元数据稽核失败");
        }

    }

    public int checkBasicInfo(Table tableInfo) throws Exception {
        int filledCount = 0;
        Field[] fileds = tableInfo.getClass().getDeclaredFields();
        for (Field field : fileds) {
            field.setAccessible(true);
            Object obj = field.get(tableInfo);
            if (obj != null && !obj.toString().trim().equals("")) {
                filledCount++;
            }
        }
        if (fileds.length == 0) {
            return 0;
        } else {
            return filledCount * 100 / fileds.length;
        }
    }

    public static final String CHINES_REGEX = "[\u4e00-\u9fa5]";

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile(CHINES_REGEX);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static final String DEFAULT_QUERY_REGEX = "[!$^&*+=|{}';'\",<>/?~！#￥%……&*——|{}【】‘；：”“'。，、？]";

    public static boolean isSpecialChar(String str) {
        Pattern p = Pattern.compile(DEFAULT_QUERY_REGEX);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public List<String> getTableNames(List<String> tableIds) {
        if (tableIds == null || tableIds.size() == 0) {
            return new ArrayList<>();
        }
        return tableDAO.getTableNames(tableIds);
    }

    public List<String> getHiveSchemaList() {
        AdapterSource adapterSource = AdapterUtils.getHiveAdapterSource();
        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        Connection connection = adapterSource.getConnection(AdminUtils.getUserName(), "", MetaspaceConfig.getHiveJobQueueName());
        return adapterExecutor.queryResult(connection, "show databases", resultSet -> {
            try {
                List<String> ret = new ArrayList<>();
                while (resultSet.next()) {
                    ret.add(resultSet.getString(1));
                }
                return ret;
            } catch (Exception e) {
                throw new AtlasBaseException(e);
            }
        });
    }

    public DatabaseInfoBO querySourceInfo(String tenantId, String sourceId, String schemaId) {
        if(StringUtils.isBlank(tenantId) || StringUtils.isBlank(sourceId) || StringUtils.isBlank(schemaId)){
            LOG.info("查询源信息登记的请求参数存在空");
            return null;
        }
        List<DatabaseInfoBO> currentSourceInfoList = databaseInfoDAO.getLastDatabaseInfoByDatabaseId(schemaId,tenantId,sourceId);
        if(CollectionUtils.isEmpty(currentSourceInfoList)){
            return null;
        }
        DatabaseInfoBO bo = new DatabaseInfoBO();
        //筛选获取最新的记录
        Optional<DatabaseInfoBO> databaseInfoOpt =  currentSourceInfoList.stream().sorted(Comparator.comparing(DatabaseInfoBO::getVersion)).findFirst();
        if(databaseInfoOpt.isPresent()){
            DatabaseInfoBO databaseInfoBO = databaseInfoOpt.get();
            if (Boolean.FALSE.equals(ParamUtil.isNull(databaseInfoBO))&&Boolean.TRUE.equals(ParamUtil.isNull(databaseInfoBO.getCategoryId()))){
                databaseInfoBO.setCategoryId(databaseInfoDAO.getParentCategoryIdById(databaseInfoBO.getId()));
            }
            if ("hive".equals(databaseInfoBO.getDataSourceId())){
                databaseInfoBO.setDataSourceName("hive");
            }
            databaseInfoBO.setCategoryName(databaseInfoBO.getStatus().equals(Status.ACTIVE.getIntValue()+"")?
                    sourceInfoDatabaseService.getActiveInfoAllPath(databaseInfoBO.getCategoryId(),tenantId):sourceInfoDatabaseService.getAllPath(databaseInfoBO.getId(),tenantId));
            return databaseInfoOpt.get();
        }
        return null;
    }

}
