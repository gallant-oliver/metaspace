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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.discovery.MetaspaceGremlinService;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.po.tableinfo.TableSourceDataBasePO;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.utils.ThreadPoolUtil;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.dao.sourceinfo.DatabaseInfoDAO;
import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.model.CommonConstant;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDatabaseService;
import io.zeta.metaspace.web.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
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
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
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
import java.util.function.Function;
import java.util.function.Predicate;
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
    private PublicService publicService;
    @Autowired
    private MetadataTableSheetService metadataTableSheetService;
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
        Map<String, Object> result = new HashMap<>();
        TableEntity tableEntity = tableDAO.selectById(guid);
        if(tableEntity == null){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到表");
        }
        result.put("schemaId", tableEntity.getDatabaseId());
        result.put("schemaName", tableEntity.getDbName());
        result.put("isHiveTable", "hive".equalsIgnoreCase(tableEntity.getDbType()));
        result.put("isView", "view".equalsIgnoreCase(tableEntity.getTableType()));
        return result;
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
        if (entityVertex == null) {
            return null;
        }
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
            //table
            Table table = extractTableInfo(entityInfo, guid, tenantId);
            if (StringUtils.isBlank(sourceId)) {
                sourceId = "hive";
                table.setSourceId("hive");
                table.setSourceName("hive");
            }
            Table tableAttr = tableDAO.getDbAndTableName(guid);
            if (tableAttr != null) {
                List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId, sourceId, tableAttr.getDatabaseId(), tableAttr.getTableName());
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
            if (StringUtils.isNotBlank(sourceId) && !"hive".equalsIgnoreCase(sourceId)) {
                dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
                if (null != dataSourceInfo) {
                    table.setSourceId(dataSourceInfo.getSourceId());
                    table.setSourceName(dataSourceInfo.getSourceName());
                }
            }
            getTableExtAttributesHive(tenantId, table);
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

    public Table getTableInfoByIdGlobal(String guid, String tenantId, String sourceId) throws AtlasBaseException {
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
            //table
            Table table = extractTableInfo(entityInfo, guid, tenantId);
            if (StringUtils.isBlank(sourceId)) {
                sourceId = "hive";
                table.setSourceId("hive");
                table.setSourceName("hive");
            }
            Table tableAttr = tableDAO.getDbAndTableName(guid);
            if (tableAttr != null) {
                List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId, sourceId, tableAttr.getDatabaseId(), tableAttr.getTableName());
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
            if (StringUtils.isNotBlank(sourceId) && !"hive".equalsIgnoreCase(sourceId)) {
                dataSourceInfo = dataSourceDAO.getDataSourceInfo(sourceId);
                if (null != dataSourceInfo) {
                    table.setSourceId(dataSourceInfo.getSourceId());
                    table.setSourceName(dataSourceInfo.getSourceName());
                }
            }
            table.setSourceTreeId(EntityUtil.generateBusinessId(tenantId, table.getSourceId(), "", ""));
            table.setDbTreeId(EntityUtil.generateBusinessId(tenantId, table.getSourceId(), table.getDatabaseId(), ""));
            table.setTenantId(tenantId);
            if (publicService.isGlobal()) {
                table.setImportancePrivilege(false);
                table.setSecurityPrivilege(false);
                table.setImportance(false);
                table.setSecurity(false);
                return table;
            }
            getTableExtAttributesHive(tenantId, table);
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

    public RDBMSTable getRDBMSTableInfoById(String guid, String tenantId, String sourceId) throws AtlasBaseException {
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
            //table
            RDBMSTable table = extractRDBMSTableInfo(entity, guid, info, tenantId);
            Table tableAttr = tableDAO.getDbAndTableName(guid);
            if (tableAttr != null) {
                List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceId == null ? null : sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId, sourceId, tableAttr.getDatabaseId(), tableAttr.getTableName());
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
            getTableExtAttributes(tenantId, table);
            return table;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        }
    }


    /**
     * 公共租户-表详情
     * @param guid
     * @param tenantId
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    public RDBMSTable getRDBMSTableInfoByIdGlobal(String guid, String tenantId, String sourceId) throws AtlasBaseException {
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

            //table
            RDBMSTable table = extractRDBMSTableInfo(entity, guid, info, tenantId);
            Table tableAttr = tableDAO.getDbAndTableName(guid);
            if (tableAttr != null) {
                List<SourceInfoDeriveTableInfo> deriveTableInfoList = sourceId == null ? null : sourceInfoDeriveTableInfoDao.getDeriveTableByIdAndTenantId(tenantId, sourceId, tableAttr.getDatabaseId(), tableAttr.getTableName());
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
            if (publicService.isGlobal()) {
                table.setImportancePrivilege(false);
                table.setSecurityPrivilege(false);
                table.setImportance(false);
                table.setSecurity(false);
                return table;
            }
            getTableExtAttributes(tenantId, table);
            return table;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        }
    }

    private void getTableExtAttributes(String tenantId, RDBMSTable table) {
        //查看该表在衍生表的重要保密性
        List<TableExtInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDAO.getImportanceInfo(table.getTableId(), tenantId);
        if (CollectionUtils.isEmpty(deriveTableInfoList)) {
            LOG.info("该衍生表没有配置重要保密信息");
            table.setImportance(false);
            table.setSecurity(false);
            table.setImportancePrivilege(true);
            table.setSecurityPrivilege(true);
            return;
        }
        boolean deriveImportance = deriveTableInfoList.stream().anyMatch(v -> v.isImportance());
        boolean deriveSecurity = deriveTableInfoList.stream().anyMatch(v -> v.isSecurity());
        table.setImportance(deriveImportance);
        table.setSecurity(deriveSecurity);
        table.setImportancePrivilege(!deriveImportance);
        table.setSecurityPrivilege(!deriveSecurity);
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        if (CollectionUtils.isEmpty(groups)) {
            LOG.info("当前用户没有配置用户组，忽略权限");
            return;
        }
        List<String> groupList = groups.stream().map(UserGroup::getId).collect(Collectors.toList());
        List<TableExtInfo> list = tableDAO.selectTableInfoByGroups(table.getTableId(), tenantId, groupList);
        if (CollectionUtils.isEmpty(list)) {
            LOG.info("当前用户组没有配置表的权限，忽略权限");
            return;
        }
        if (deriveImportance) {
            table.setImportancePrivilege(list.stream().anyMatch(p -> p.isImportance()));
        }
        if (deriveSecurity) {
            table.setSecurityPrivilege(list.stream().anyMatch(p -> p.isSecurity()));
        }
    }

    private void getTableExtAttributesHive(String tenantId, Table table) {
        //查看该表在衍生表的重要保密性
        List<TableExtInfo> deriveTableInfoList = sourceInfoDeriveTableInfoDAO.getImportanceInfo(table.getTableId(), tenantId);
        if (CollectionUtils.isEmpty(deriveTableInfoList)) {
            LOG.info("该衍生表没有配置重要保密信息");
            table.setImportance(false);
            table.setSecurity(false);
            table.setImportancePrivilege(true);
            table.setSecurityPrivilege(true);
            return;
        }
        boolean deriveImportance = deriveTableInfoList.stream().anyMatch(v -> v.isImportance());
        boolean deriveSecurity = deriveTableInfoList.stream().anyMatch(v -> v.isSecurity());
        table.setImportance(deriveImportance);
        table.setSecurity(deriveSecurity);
        table.setImportancePrivilege(!deriveImportance);
        table.setSecurityPrivilege(!deriveSecurity);
        User user = AdminUtils.getUserData();
        List<UserGroup> groups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
        if (CollectionUtils.isEmpty(groups)) {
            LOG.info("当前用户没有配置用户组，忽略权限");
            return;
        }
        List<String> groupList = groups.stream().map(UserGroup::getId).collect(Collectors.toList());
        List<TableExtInfo> list = tableDAO.selectTableInfoByGroups(table.getTableId(), tenantId, groupList);
        if (CollectionUtils.isEmpty(list)) {
            LOG.info("当前用户组没有配置表的权限，忽略权限");
            return;
        }
        if (deriveImportance) {
            table.setImportancePrivilege(list.stream().anyMatch(p -> p.isImportance()));
        }
        if (deriveSecurity) {
            table.setSecurityPrivilege(list.stream().anyMatch(p -> p.isSecurity()));
        }
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

    public RDBMSColumnAndIndexAndForeignKey getRDBMSColumnInfoById(ColumnQuery query) throws AtlasBaseException {
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
            foreignKeys = extractRDBMSForeignKeyInfo(info, guid, relatedDB, relatedInstance, columns);
            indexes = extractRDBMSIndexInfo(info, guid, relatedDB, relatedInstance, columns);
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
            foreignKeys = extractRDBMSForeignKeyInfo(info, guid, relatedDB, relatedInstance, columns);
            indexes = extractRDBMSIndexInfo(info, guid, relatedDB, relatedInstance, columns);
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

    public List<RDBMSIndex> extractRDBMSIndexInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid, AtlasRelatedObjectId relatedDB, AtlasRelatedObjectId relatedInstance, List<RDBMSColumn> columns) throws AtlasBaseException {
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

    public List<RDBMSForeignKey> extractRDBMSForeignKeyInfo(AtlasEntity.AtlasEntityWithExtInfo info, String guid, AtlasRelatedObjectId relatedDB, AtlasRelatedObjectId relatedInstance, List<RDBMSColumn> columns) throws AtlasBaseException {
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
        return relationDAO.queryTableCategoryRelationsFromDb(tableGuid, tenantId);
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
            return columnDAO.selectListByGuidOrLike(query.getGuid(), query.getColumnFilter().getColumnName(), query.getColumnFilter().getType(), query.getColumnFilter().getDescription());
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
                                            int depth, String token) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableLineage({}, {}, {})", guid, direction, depth);
        }
        if (StringUtils.isEmpty(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取表血缘关系失败");
        }
        TableSourceDataBasePO tableInfoData = tableDAO.selectDataTypeByGuid(guid);
        if (tableInfoData == null) {
            throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取表血缘关系失败");
        }
        TableLineageInfo info = new TableLineageInfo();
        if (tableInfoData.getType().equalsIgnoreCase("hive")) {
            try {
                AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, direction, depth);
                if (Objects.isNull(lineageInfo)) {
                    throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取表血缘关系失败");
                }
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
            } catch (AtlasBaseException | InterruptedException | TimeoutException | ExecutionException e) {
                log.error("获取表血缘关系失败: " + e.getMessage());
                Thread.currentThread().interrupt();
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘关系失败");
            }
        } else {
            TableSourceDataBasePO dataBasePO = DataSourceType.POSTGRESQL.getName().equalsIgnoreCase(tableInfoData.getType()) ?
                    tableDAO.selectSourceDatabaseByDatabaseGuid(tableInfoData.getDatabaseGuid())
                    : tableDAO.selectSourceInfoByDatabaseGuid(tableInfoData.getDatabaseGuid());
            TableInfoVo tableInfo = new TableInfoVo();
            org.springframework.beans.BeanUtils.copyProperties(tableInfoData, tableInfo);
            tableInfo.setPort(dataBasePO.getPort());
            tableInfo.setHost(dataBasePO.getHost());
            tableInfo.setDepth(depth);
            tableInfo.setDirection(direction.toString());
            TableInfoVo tableInfoVo = judgeSourceDB(tableInfo, dataBasePO);
            info = getRelationTableLineage(token, tableInfoVo);
        }
        return info;
    }

    /**
     * 对不同数据库类型进行元数据采集与任务调度数据库名统一
     *
     * @param tableInfoVo 元数据采集的配置信息
     * @return 任务调度的配置信息
     */
    public TableInfoVo judgeSourceDB(TableInfoVo tableInfoVo, TableSourceDataBasePO dataBasePO) {
        if (StringUtils.isNotBlank(tableInfoVo.getType())) {
            String database;
            switch (tableInfoVo.getType()) {
                case "POSTGRESQL":
                    database = dataBasePO.getSourceDatabase() + "." + tableInfoVo.getDatabase();
                    tableInfoVo.setDatabase(database);
                    break;
                case "SQLSERVER":
                    break;
            }
        }
        return tableInfoVo;
    }

    public TableLineageInfo getRelationTableLineage(String token, TableInfoVo tableInfoVo) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getRelationTableLineage({})", tableInfoVo);
        }
        try {
            TableLineageInfo tableLineageInfo = new TableLineageInfo();
            tableLineageInfo.setGuid(tableInfoVo.getGuid());
            tableLineageInfo.setRelations(new HashSet<>());
            tableLineageInfo.setEntities(new ArrayList<>());
            Gson gson = new Gson();
            Configuration conf = ApplicationProperties.get();
            String logoutURL = conf.getString("metaspace.matedate.table.lineage");
            if (logoutURL == null || logoutURL.equals("")) {
                throw new AtlasBaseException(AtlasErrorCode.CONF_LOAD_ERROE, "metaspace.matedate.table.lineage");
            }
            Map<String, Object> header = new HashMap<>();
            header.put("token", token);
            String strJson = gson.toJson(tableInfoVo);
            String res = OKHttpClient.doPost(logoutURL, strJson, header);
            if (Objects.isNull(res)) {
                throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取表血缘关系失败");
            }
            LOG.info(res);
            List<SimpleTaskNode> simpleTaskNodes;
            if (res.contains("[")) {
                simpleTaskNodes = gson.fromJson(res, new TypeToken<List<SimpleTaskNode>>() {
                }.getType());
            } else {
                return tableLineageInfo;
            }
            if (CollectionUtils.isEmpty(simpleTaskNodes)) {
                return tableLineageInfo;
            }
            // 限定血缘层级 小于1大于6 默认为3
            if (tableInfoVo.getDepth() < 1) {
                tableInfoVo.setDepth(3);
            }
            if (tableInfoVo.getDepth() > 6) {
                tableInfoVo.setDepth(3);
            }
            // 将请求任务调度数据库名转义为对应数据管理中的数据库名
            MetaDateRelationalDateService.unifyTableInfo(tableInfoVo);
            List<TableInfoVo> tableInfoVos = new ArrayList<>();
            tableInfoVos.add(tableInfoVo);
            Set<LineageTrace> lineageTraceSet = new HashSet<>();
            List<TableLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            List<TableInfoVo> tableInfoVosCache = new ArrayList<>();
            List<SimpleTaskNode> taskNodeCache = new ArrayList<>();
            HashMap<String, Integer> upDescMap = new HashMap<>();
            HashMap<String, Integer> downDescMap = new HashMap<>();
            upDescMap.put(tableInfoVo.getGuid(), 0);
            downDescMap.put(tableInfoVo.getGuid(), 0);
            if (!org.springframework.util.StringUtils.isEmpty(tableInfoVo.getDirection())) {
                // 将任务调度获取的数据库名数据处理为元数据采集的数据库名
                MetaDateRelationalDateService.unifyDataBase(simpleTaskNodes);
                // 映射元数据采集的数据
                List<TableInfoVo> allTableInfo = new ArrayList<>();
                allTableInfo.addAll(simpleTaskNodes.stream().map(SimpleTaskNode::getInputTable).collect(Collectors.toList()));
                allTableInfo.addAll(simpleTaskNodes.stream().map(SimpleTaskNode::getOutputTable).collect(Collectors.toList()));
                List<TableSourceDataBasePO> basePOS = tableDAO.selectSourceDbByListName(allTableInfo);
                MetaDateRelationalDateService.updateExistTableGuid(simpleTaskNodes, basePOS);
                // 选择遍历方式
                if (CommonConstant.INPUT_DIRECTION.equals(tableInfoVo.getDirection())) {
                    MetaDateRelationalDateService.upTaskNode(simpleTaskNodes, tableInfoVos, tableInfoVo.getDepth(),
                            lineageTraceSet, lineageEntities, tableInfoVosCache, taskNodeCache, upDescMap);
                } else if (CommonConstant.OUTPUT_DIRECTION.equals(tableInfoVo.getDirection())) {
                    MetaDateRelationalDateService.downTaskNode(simpleTaskNodes, tableInfoVos, tableInfoVo.getDepth(),
                            lineageTraceSet, lineageEntities, tableInfoVosCache, taskNodeCache, downDescMap);
                } else if (CommonConstant.BOTH_DIRECTION.equals(tableInfoVo.getDirection())) {
                    MetaDateRelationalDateService.upTaskNode(simpleTaskNodes, tableInfoVos, tableInfoVo.getDepth(),
                            lineageTraceSet, lineageEntities, tableInfoVosCache, taskNodeCache, upDescMap);
                    MetaDateRelationalDateService.downTaskNode(simpleTaskNodes, tableInfoVos, tableInfoVo.getDepth(),
                            lineageTraceSet, lineageEntities, tableInfoVosCache, taskNodeCache, downDescMap);
                } else {
                    throw new AtlasBaseException(AtlasErrorCode.INSTANCE_LINEAGE_INVALID_PARAMS, "direction", tableInfoVo.getDirection());
                }
            } else {
                throw new AtlasBaseException(AtlasErrorCode.INSTANCE_LINEAGE_INVALID_PARAMS, "direction", null);
            }
            if (CollectionUtils.isNotEmpty(tableInfoVos)) {
                //过滤重复数据
                tableLineageInfo.setEntities(lineageEntities.stream()
                        .filter(distinctByKey(TableLineageInfo.LineageEntity::getGuid)).collect(Collectors.toList()));
                tableLineageInfo.setRelations(lineageTraceSet.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(o -> o.getFromEntityId() + ";" + o.getToEntityId()))), HashSet::new)));
            }
            MetaDateRelationalDateService.removeExtraLayers(tableLineageInfo, upDescMap, tableInfoVo, CommonConstant.INPUT_DIRECTION);
            MetaDateRelationalDateService.removeExtraLayers(tableLineageInfo, downDescMap, tableInfoVo, CommonConstant.OUTPUT_DIRECTION);
            return tableLineageInfo;
        } catch (AtlasBaseException | AtlasException e) {
            log.error("获取关系表血缘失败: " + e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取关系表血缘失败数据出现死循环！");
        }
    }

    public TableLineageInfo setTableLineageInfo(List<SimpleTaskNode> simpleTaskNodes, int depth, TableInfoVo tableInfoVo, String tableGuid) {
        TableLineageInfo info = new TableLineageInfo();
        Set<LineageTrace> lineageTraceSet = new HashSet<>();
        List<TableLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
        int num = depth;

        List<SimpleTaskNode> initTable = getConditionTier(simpleTaskNodes, tableInfoVo);
        List<TableInfoVo> inoutTableInfoVos = new ArrayList<>();
        List<TableInfoVo> outputTableInfoVos = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(initTable)) {
            initTable = initTable.stream().map(r -> {
                r.getOutputTable().setGuid(tableGuid);
                return r;
            }).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(simpleTaskNodes)) {
            while (num > 0) {
                //下一次获取的目标节点
                if (num != depth) {
                    outputTableInfoVos.clear();
                    outputTableInfoVos.addAll(inoutTableInfoVos);
                }
                inoutTableInfoVos = new ArrayList<>();
                for (SimpleTaskNode simpleTaskNode : initTable) {
                    TableInfoVo inputTable = simpleTaskNode.getInputTable();
                    TableInfoVo outputTable = simpleTaskNode.getOutputTable();

                    TableLineageInfo.LineageEntity lineageEntity = new TableLineageInfo.LineageEntity();
                    lineageEntity.setGuid(outputTable.getGuid());
                    lineageEntity.setTableName(outputTable.getTable());
                    lineageEntity.setDbName(outputTable.getDatabase());
                    lineageEntity.setTypeName(outputTable.getType());
                    lineageEntities.add(lineageEntity);

                    LineageTrace lineageTrace = new LineageTrace();
                    lineageTrace.setToEntityId(outputTable.getGuid());
                    lineageTrace.setFromEntityId(inputTable.getGuid());
                    lineageTraceSet.add(lineageTrace);

                    inoutTableInfoVos.add(inputTable);
                }

                initTable = new ArrayList<>();
                for (TableInfoVo infoVo : inoutTableInfoVos) {
                    initTable.addAll(getConditionTier(simpleTaskNodes, infoVo));
                }

                if (num != depth) {
                    //到顶但是没有上一级的元素(取差集添加)
                    List<TableInfoVo> differenceSetList = inoutTableInfoVos.stream()
                            .filter(item -> !outputTableInfoVos.stream()
                                    .map(TableInfoVo::getGuid)
                                    .collect(Collectors.toList())
                                    .contains(item.getGuid()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(inoutTableInfoVos) && CollectionUtils.isNotEmpty(outputTableInfoVos)) {
                        differenceSetList = outputTableInfoVos;
                    }
                    if (CollectionUtils.isNotEmpty(differenceSetList)) {
                        for (TableInfoVo tableInfo : differenceSetList) {
                            TableLineageInfo.LineageEntity lineageEntity = new TableLineageInfo.LineageEntity();
                            lineageEntity.setGuid(tableInfo.getGuid());
                            lineageEntity.setTableName(tableInfo.getTable());
                            lineageEntity.setDbName(tableInfo.getDatabase());
                            lineageEntity.setTypeName(tableInfo.getType());
                            lineageEntities.add(lineageEntity);
                        }
                    }
                }
                --num;
            }
            lineageEntities = lineageEntities.stream().filter(distinctByKey(TableLineageInfo.LineageEntity::getGuid)).collect(Collectors.toList());
        }
        info.setGuid(tableGuid);
        info.setRelations(lineageTraceSet);
        info.setEntities(lineageEntities);
        return info;
    }

    public List<SimpleTaskNode> getConditionTier(List<SimpleTaskNode> simpleTaskNodes, TableInfoVo tableInfoVo) {

        String guid = tableInfoVo.getGuid();
        String host = tableInfoVo.getHost();
        String port = tableInfoVo.getPort();
        String database = tableInfoVo.getDatabase();
        String table = tableInfoVo.getTable();

        return simpleTaskNodes.stream().filter(r -> {
            TableInfoVo outputTable = r.getOutputTable();
            if (outputTable != null && outputTable.getHost().equals(host) && outputTable.getPort().equals(port)
                    && outputTable.getDatabase().equals(database) && outputTable.getTable().equals(table)) {
                return true;
            }
            return false;
        }).map(r -> {
            String tableGuid = UUID.randomUUID().toString();
            r.getOutputTable().setGuid(guid);
            r.getInputTable().setGuid(tableGuid);
            return r;
        }).collect(Collectors.toList());
    }

    /**
     * 自定义条件过滤器
     *
     * @param keyExtractor
     * @param <T>
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public LineageDepthInfo getTableLineageDepthInfo(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getLineageInfo({})", guid);
        }
        try {
            LineageDepthInfo lineageDepthEntity = new LineageDepthInfo();
            AtlasEntity.AtlasEntityWithExtInfo entityInfoByGuid = getEntityInfoByGuid(guid);
            if (Objects.isNull(entityInfoByGuid)) {
                return null;
            }
            AtlasEntity entity = entityInfoByGuid.getEntity();
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
            } else {
                return null;
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

    public List<DataOwnerHeader> getDataOwnerByGuids(List<String> guids) throws AtlasBaseException {
        try {
            List<DataOwnerHeader> owners = tableDAO.getDataOwnerLists(guids);
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
                metadataTableSheetService.createMetadataTableSheet(workbook, i + 1, table, headerStyle, cellStyle);
                metadataTableSheetService.createMetadataColumnSheet(workbook, i + 1, table, headerStyle, cellStyle);
            }
        }
        return workbook;
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
