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

import io.zeta.metaspace.discovery.MetaspaceGremlinService;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.metadata.MetaDataProvider;
import io.zeta.metaspace.web.metadata.Oracle.OracleMetaDataProvider;
import io.zeta.metaspace.web.metadata.mysql.MysqlMetaDataProvider;
import io.zeta.metaspace.web.model.Progress;
import io.zeta.metaspace.web.model.TableSchema;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.*;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.store.AtlasTypeDefStore;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.atlas.type.AtlasTypeRegistry;

import org.apache.commons.beanutils.BeanUtils;

import javax.ws.rs.PathParam;

import static io.zeta.metaspace.web.util.PoiExcelUtils.XLSX;
import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;


/*
 * @description
 * @author sunhaoning
 * @date 2018/10/25 15:11
 */
@Service
public class MetaDataService {
    private static final Logger LOG = LoggerFactory.getLogger(MetaDataService.class);
    private static final String XLS = "xls";

    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    private AtlasLineageService atlasLineageService;
    @Autowired
    AtlasTypeDefStore typeDefStore;
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
    DataManageService dataManageService;
    private String errorMessage = "";
    @Autowired
    private HiveMetaStoreBridgeUtils hiveMetaStoreBridgeUtils;
    @Autowired
    private MysqlMetaDataProvider mysqlMetaDataProvider;

    @Autowired
    private OracleMetaDataProvider oracleMetaDataProvider;
    private Map<String, IMetaDataProvider> metaDataProviderMap = new LRUMap(50);
    private Map<String, String> errorMap = new LRUMap(50);

    @Autowired
    private ColumnDAO columnDAO;

    @Autowired
    private MetadataHistoryDAO metadataHistoryDAO;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private AtlasTypeRegistry atlasTypeRegistry;
    @Autowired
    private AtlasGraph graph;

    @Autowired
    private SearchService searchService;

    public Table getTableInfoById(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableInfoById({})", guid);
        }
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        try {
            //获取entity
            AtlasEntity entity = getEntityById(guid);
            if (Objects.isNull(entity)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到数据表信息");
            }
            //table
            Table table = extractTableInfo(entity, guid);

            String tableName = table.getTableName();
            String tableDisplayName = table.getDisplayName();
            if(Objects.isNull(tableDisplayName) || "".equals(tableDisplayName.trim())) {
                table.setDisplayName(tableName);
            }
            List<Column> columnList = table.getColumns();
            columnList.forEach(column -> {
                String columnName = column.getColumnName();
                String displayName = column.getDisplayName();
                if(Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    column.setDisplayName(columnName);
                }
            });

            return table;
        } catch (AtlasBaseException e) {
            if (e.getMessage().contains("无效的实体ID")){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不存在该表信息，请确定该表是否为脏数据");
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息"+e.getMessage());
        }
    }

    public Table extractTableInfo(AtlasEntity entity, String guid) throws AtlasBaseException {
        Table table = new Table();
        table.setTableId(guid);
        if (entity.getTypeName().contains("table")) {
            //表名称
            table.setTableName(getEntityAttribute(entity, "name"));
            //中文别名
            String displayName = columnDAO.getTableDisplayInfoByGuid(guid);
            if(Objects.nonNull(displayName)) {
                table.setDisplayName(displayName);
            } else {
                table.setDisplayName(table.getTableName());
            }
            //判断是否为虚拟表
            if(Boolean.getBoolean(entity.getAttribute("temporary").toString()) == true) {
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
            List<Column> columns = getColumnInfoById(columnQuery, true);
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
                List<Tag> tags = tableTagDAO.getTable2Tag(table.getTableId());
                table.setTags(tags);
            } catch (Exception e) {
                LOG.error("获取标签失败,错误信息:" + e.getMessage(), e);
            }
            //获取权限判断是否能编辑,默认不能
            table.setEdit(false);
            try {
                Role role = userDAO.getRoleByUserId(AdminUtils.getUserData().getUserId());
                if("1".equals(role.getRoleId())) {
                    table.setEdit(true);
                } else {
                    List<Module> modules = userDAO.getModuleByUserId(AdminUtils.getUserData().getUserId());
                    for (Module module : modules) {
                        if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                            if (table.getTablePermission().isWRITE()) {
                                table.setEdit(true);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("获取系统权限失败,错误信息:" + e.getMessage(), e);
            }

            //1.4新增
            try {
                //owner.name
                List<DataOwnerHeader> owners = getDataOwner(guid);
                table.setDataOwner(owners);
                //更新时间
                //table.setUpdateTime((entity.hasAttribute("last_modified_time") && Objects.nonNull(entity.getAttribute("last_modified_time")))?DateUtils.date2String((Date)entity.getAttribute("last_modified_time")):null);
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
                List<String> relations = getRelationList(guid);
                table.setRelations(relations);
//                List<String> adminByTableguid = tableDAO.getAdminByTableguid(guid);
                //目录管理员
//                table.setCatalogAdmin(adminByTableguid);
                //关联时间
                if(relations.size()==1)
                        table.setRelationTime(tableDAO.getDateByTableguid(guid));
            } catch (Exception e) {
                LOG.error("获取数据目录维度失败,错误信息:" + e.getMessage(), e);
            }
            try {
                List<Table.BusinessObject> businessObjectByTableguid = tableDAO.getBusinessObjectByTableguid(guid);
                table.setBusinessObjects(businessObjectByTableguid);
            } catch (Exception e) {
                LOG.error("获取业务维度失败,错误信息:" + e.getMessage(), e);
            }
        }
        return table;
    }

    public void extractPartitionInfo(AtlasEntity entity, Table table) {
        if (entity.hasAttribute("partitionKeys") && Objects.nonNull(entity.getAttribute("partitionKeys"))) {
            table.setPartitionTable(true);
        } else {
            table.setPartitionTable(false);
        }
    }

    public void extractVirtualTable(AtlasEntity entity, Table table) {
        String tableName = getEntityAttribute(entity, "name");
        if (tableName.contains("values__tmp__table"))
            table.setVirtualTable(true);
        else
            table.setVirtualTable(false);
    }

    public void extractSdInfo(AtlasEntity entity, Table table) throws AtlasBaseException {
        if (entity.hasAttribute("sd") && Objects.nonNull(entity.getAttribute("sd"))) {
            Object obj = entity.getAttribute("sd");
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
        if (tableType.contains("EXTERNAL")) {
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
        if (entity.hasRelationshipAttribute("db") && Objects.nonNull(entity.getRelationshipAttribute("db"))) {
            Object obj = entity.getRelationshipAttribute("db");
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }

    public List<String> getRelationList(String guid) throws AtlasBaseException {
        try {
            List<RelationEntityV2> relationEntities = relationDAO.queryRelationByTableGuid(guid);
            dataManageService.getPath(relationEntities);
            List<String> relations = new ArrayList<>();
            if (Objects.nonNull(relationEntities)) {
                for (RelationEntityV2 entity : relationEntities)
                    relations.add(entity.getPath());
            }
            return relations;
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    @Cacheable(value = "columnCache", key = "#query.guid + #query.columnFilter.columnName + #query.columnFilter.type + #query.columnFilter.description", condition = "#refreshCache==false")
    public List<Column> getColumnInfoById(ColumnQuery query, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getColumnInfoById({})", query);
        }
        String guid = query.getGuid();
        List<Column> columns = null;
        //获取entity
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
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
        Column column = null;

        List<AtlasObjectId> partitionKeys = extractPartitionKeyInfo(entity);
        for (String key : referredEntities.keySet()) {
            AtlasEntity referredEntity = referredEntities.get(key);
            if (referredEntity.getTypeName().contains("column") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
                column = new Column();
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
                column.setPartitionKey(false);
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
        }
        return columns;
    }

    public List<AtlasObjectId> extractPartitionKeyInfo(AtlasEntity entity) {
        List<AtlasObjectId> partitionKeys = null;
        if (Objects.nonNull(entity.getAttribute("partitionKeys"))) {
            Object partitionObjects = entity.getAttribute("partitionKeys");
            if (partitionObjects instanceof ArrayList<?>) {
                partitionKeys = (ArrayList<AtlasObjectId>) partitionObjects;
            }
        }
        return partitionKeys;
    }

    public void extractAttributeInfo(AtlasEntity referredEntity, Column column) {
        Map<String, Object> attributes = referredEntity.getAttributes();
        if (attributes.containsKey("name") && Objects.nonNull(attributes.get("name"))) {
            column.setColumnName(attributes.get("name").toString());
        } else {
            column.setColumnName("");
        }
        if (attributes.containsKey("type") && Objects.nonNull(attributes.get("type"))) {
            column.setType(attributes.get("type").toString());
        } else {
            column.setType("");
        }
        if (attributes.containsKey("comment") && Objects.nonNull(attributes.get("comment"))) {
            column.setDescription(attributes.get("comment").toString());
        } else {
            column.setDescription("");
        }
        Column pgColumnInfo = columnDAO.getColumnInfoByGuid(column.getColumnId());
        if(Objects.nonNull(pgColumnInfo)) {
            String displayName = pgColumnInfo.getDisplayName();
            if (Objects.nonNull(displayName) && !"".equals(displayName.trim())) {
                column.setDisplayName(displayName);
            } else {
                column.setDisplayName(column.getColumnName());
            }
            String displayUpdateTime = pgColumnInfo.getDisplayNameUpdateTime();
            if(Objects.nonNull(displayUpdateTime) && !"".equals(displayUpdateTime.trim())) {
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
            if (Objects.nonNull(type) && !type.equals("")) {
                columns = columns.stream().filter(col -> {
                    if (col.getType().contains("(") && col.getType().contains(")")) {
                        int lastIndex = col.getType().lastIndexOf("(");
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
            TableLineageInfo.LineageEntity lineageEntity = null;
            for (String key : entities.keySet()) {
                lineageEntity = new TableLineageInfo.LineageEntity();
                AtlasEntityHeader atlasEntity = entities.get(key);
                getTableEntityInfo(key, lineageEntity, entities, atlasEntity);
                lineageEntities.add(lineageEntity);
            }
            info.setEntities(lineageEntities);
            info.setRelations(lineageRelations);
            System.out.println();
            return info;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘关系失败");
        }
    }

    public LineageDepthInfo getTableLineageDepthInfo(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getLineageInfo({})", guid);
        }
        try {
            LineageDepthInfo lineageDepthEntity = new LineageDepthInfo();
            AtlasEntity entity = entitiesStore.getById(guid).getEntity();
            if(Objects.nonNull(entity)) {
                if (entity.getTypeName().contains("table") || entity.getTypeName().contains("hdfs")) {
                    //guid
                    lineageDepthEntity.setGuid(guid);
                    //tableName
                    lineageDepthEntity.setTableName(getEntityAttribute(entity, "name"));
                    //displayText
                    //lineageDepthEntity.setDisplayText(entity);
                    //updateTime
                    SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(entity.getUpdateTime());
                    lineageDepthEntity.setUpdateTime(formatDateStr);
                    //dbName
                    AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
                    if(Objects.nonNull(relatedObject))
                        lineageDepthEntity.setDbName(relatedObject.getDisplayText());
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }
            /*AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            if (Objects.nonNull(entities) && entities.size() != 0) {
                AtlasEntityHeader atlasEntity = entities.get(guid);
                if (atlasEntity.getDatabaseTypeName().contains("table") || atlasEntity.getDatabaseTypeName().contains("hdfs")) {
                    //guid
                    lineageDepthEntity.setGuid(guid);
                    AtlasEntity atlasTableEntity = getEntityById(guid);
                    //tableName
                    lineageDepthEntity.setTableName(getEntityAttribute(atlasTableEntity, "name"));
                    //displayText
                    lineageDepthEntity.setDisplayText(atlasEntity.getDisplayText());
                    //updateTime
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(atlasTableEntity.getUpdateTime());
                    lineageDepthEntity.setUpdateTime(formatDateStr);
                    //dbName
                    AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                    if (Objects.nonNull(relatedObject))
                        lineageDepthEntity.setDbName(relatedObject.getDisplayText());
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }*/
            return lineageDepthEntity;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘深度详情失败");
        }
    }

    public LineageDepthInfo getLineageDepthV2(LineageDepthInfo lineageDepthEntity) throws AtlasBaseException {
        String guid = lineageDepthEntity.getGuid();
        //直接上游表数量
        long directUpStreamNum = metaspaceLineageService.getEntityDirectNum(guid, AtlasLineageInfo.LineageDirection.INPUT);
        lineageDepthEntity.setDirectUpStreamNum(directUpStreamNum);
        //直接下游表数量
        long directDownStreamNum = metaspaceLineageService.getEntityDirectNum(guid, AtlasLineageInfo.LineageDirection.OUTPUT);
        lineageDepthEntity.setDirectDownStreamNum(directDownStreamNum);
        //上游表层数
        long upStreamLevelNum = metaspaceLineageService.getLineageDepth(guid, AtlasLineageInfo.LineageDirection.INPUT);
        lineageDepthEntity.setUpStreamLevelNum(upStreamLevelNum);
        //下游表层数
        long downStreamLevelNum = metaspaceLineageService.getLineageDepth(guid, AtlasLineageInfo.LineageDirection.OUTPUT);
        lineageDepthEntity.setDownStreamLevelNum(downStreamLevelNum);
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

    public void removeTableEntityAndRelation(List<ColumnLineageInfo.LineageEntity> lineageEntities, Set<LineageTrace> lineageRelations) throws AtlasBaseException {
        Set<LineageTrace> removeNode = new HashSet<>();
        Set<ColumnLineageInfo.LineageEntity> removeEntity = new HashSet<>();
        for (ColumnLineageInfo.LineageEntity entity : lineageEntities) {
            String guid = entity.getGuid();
            AtlasEntityHeader header = entitiesStore.getHeaderById(guid);
            String typeName = header.getTypeName();
            if (typeName.contains("table")) {
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
            if(Objects.nonNull(entity)) {
                if (entity.getTypeName().contains("column")) {
                    //guid
                    lineageDepthEntity.setGuid(guid);
                    AtlasEntity atlasColumnEntity = getEntityById(guid);
                    //columnName && displayText
                    lineageDepthEntity.setDisplayText(getEntityAttribute(atlasColumnEntity, "name"));
                    //updateTime
                    SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(atlasColumnEntity.getUpdateTime());
                    lineageDepthEntity.setUpdateTime(formatDateStr);

                    AtlasRelatedObjectId relatedTable = (AtlasRelatedObjectId)atlasColumnEntity.getRelationshipAttribute("table");
                    if(Objects.nonNull(relatedTable)) {
                        AtlasEntity atlasTableEntity = entitiesStore.getById(relatedTable.getGuid()).getEntity();
                        //tableName
                        if (atlasTableEntity.hasAttribute("name") && Objects.nonNull(atlasTableEntity.getAttribute("name")))
                            lineageDepthEntity.setTableName(atlasTableEntity.getAttribute("name").toString());
                        AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                        if (Objects.nonNull(relatedObject)) {
                            //dbName
                            lineageDepthEntity.setDbName(relatedObject.getDisplayText());
                        }
                    }
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }
            /*AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            if (Objects.nonNull(entities) && entities.size() != 0) {
                AtlasEntityHeader atlasEntity = entities.get(guid);
                if (atlasEntity.getDatabaseTypeName().contains("column")) {
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
                        if (atlasTableEntity.hasAttribute("name") && Objects.nonNull(atlasTableEntity.getAttribute("name")))
                            lineageDepthEntity.setTableName(atlasTableEntity.getAttribute("name").toString());
                        AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                        if (Objects.nonNull(relatedObject)) {
                            //dbName
                            lineageDepthEntity.setDbName(relatedObject.getDisplayText());
                        }
                    }
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }*/
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
            if (type.contains("Process"))
                lineageEntity.setProcess(true);
        }
        lineageEntity.setStatus(atlasEntity.getStatus().name());
        //displayText
        if (Objects.nonNull(atlasEntity.getDisplayText())) {
            lineageEntity.setDisplayText(atlasEntity.getDisplayText());
        }
        AtlasEntity atlasTableEntity = entitiesStore.getById(guid).getEntity();
        //tableName
        if (atlasEntity.hasAttribute("name") && Objects.nonNull(atlasEntity.getAttribute("name")))
            lineageEntity.setTableName(atlasEntity.getAttribute("name").toString());
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

    @Transactional
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
            HiveJdbcUtils.execute(sql, dbName);
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
                HiveJdbcUtils.execute(sql, dbName);
            }
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改字段信息失败");
        }
    }

    @CacheEvict(value = {"columnCache", "tablePageCache", "columnPageCache", "databaseSearchCache", "TableByDBCache"}, allEntries = true)
    public void refreshCache(){
        LOG.info("元数据管理缓存已被清除");
    }

    /**
     * 同步元数据
     *
     * @return
     */
    public void synchronizeMetaData(String databaseType, TableSchema tableSchema){
        DatabaseType databaseTypeEntity = getDatabaseType(databaseType);
        if (databaseTypeEntity==DatabaseType.HIVE){
            tableSchema.setInstance("hive");
        }
        if (null == databaseTypeEntity) {
            errorMap.put(tableSchema.getInstance(),String.format("not support database type %s", databaseType));
            LOG.error(errorMap.get(tableSchema.getInstance()));
            return;
        }
        IMetaDataProvider metaDataProvider = null;
        errorMap.put(tableSchema.getInstance(),"");
        try {
            metaDataProvider = getMetaDataProviderFactory(databaseTypeEntity,tableSchema);
            metaDataProvider.importDatabases(tableSchema);
        } catch (HiveException e) {
            errorMap.put(tableSchema.getInstance(),"同步元数据出错，无法连接到hive");
            LOG.error("import metadata error,", e);
        } catch (Exception e) {
            errorMap.put(tableSchema.getInstance(),String.format("同步元数据出错，%s", e.getMessage()));
            LOG.error("import metadata error", e);
        }
        if (null != metaDataProvider) {
            metaDataProvider.getEndTime().set(System.currentTimeMillis());
        }
    }

    IMetaDataProvider getMetaDataProviderFactory(DatabaseType databaseTypeEntity,TableSchema tableSchema) throws Exception {
        IMetaDataProvider metaDataProvider;
        switch (databaseTypeEntity) {
            case HIVE:
                return hiveMetaStoreBridgeUtils;
            case MYSQL:
                if (!metaDataProviderMap.containsKey(tableSchema.getInstance())) {
                    MysqlMetaDataProvider mysqlMetaDataProvider = new MysqlMetaDataProvider();
                    mysqlMetaDataProvider.set(entitiesStore, dataSourceService,atlasTypeRegistry,graph);
                    metaDataProvider=mysqlMetaDataProvider;
                }else{
                    metaDataProvider=metaDataProviderMap.get(tableSchema.getInstance());
                }
                break;
            case ORACLE:
                if (!metaDataProviderMap.containsKey(tableSchema.getInstance())) {
                    OracleMetaDataProvider oracleMetaDataProvider = new OracleMetaDataProvider();
                    oracleMetaDataProvider.set(entitiesStore, dataSourceService,atlasTypeRegistry,graph);
                    metaDataProvider=oracleMetaDataProvider;
                }else{
                    metaDataProvider=metaDataProviderMap.get(tableSchema.getInstance());
                }
                break;
            default:
                throw new Exception("不支持的数据源类型" + databaseTypeEntity.getName());
        }
        metaDataProviderMap.put(tableSchema.getInstance(),metaDataProvider);
        return metaDataProvider;
    }

    private DatabaseType getDatabaseType(String databaseType) {
        DatabaseType databaseTypeEntity = null;
        for (DatabaseType databaseType2 : DatabaseType.values()) {
            if (org.apache.commons.lang.StringUtils.isNotEmpty(databaseType) && databaseType.toLowerCase().equals(databaseType2.getName())) {
                databaseTypeEntity = databaseType2;
                break;
            }
        }
        return databaseTypeEntity;
    }

    public Progress importProgress(String databaseType,String sourceId) throws Exception {
        DatabaseType databaseTypeEntity = getDatabaseType(databaseType);
        Progress progress = new Progress(0, 0, "");
        if (null == databaseTypeEntity) {
            errorMap.put(sourceId,String.format("not support database type %s", databaseType));
            LOG.error(errorMap.get(sourceId));
            progress.setError(errorMap.get(sourceId));
            return progress;
        }
        if (hiveMetaStoreBridgeUtils == null) {
            errorMap.put(sourceId,String.format("get hiveMetaStoreBridgeUtils instance error: init hive metastore bridge error"));
            LOG.error(errorMap.get(sourceId));
            progress.setError(errorMap.get(sourceId));
            return progress;
        }
        switch (databaseTypeEntity) {
            case HIVE:
                progress = getProgress(hiveMetaStoreBridgeUtils,sourceId);
                break;
            case MYSQL:
            case ORACLE:
                progress = getProgress(metaDataProviderMap.get(sourceId),sourceId);
                break;
            case POSTGRESQL:
                progress.setError(String.format("not support database type %s, hive is support", databaseType));
                break;
        }
        return progress;
    }

    private Progress getProgress(IMetaDataProvider metaDataProvider,String sourceId) throws AtlasBaseException {
        Progress progress;
        if (metaDataProvider==null){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该数据源未开始采集元数据或元数据已采集完毕");
        }
        AtomicInteger totalTables = metaDataProvider.getTotalTables();
        AtomicInteger updatedTables = metaDataProvider.getUpdatedTables();
        AtomicLong startTime = metaDataProvider.getStartTime();
        AtomicLong endTime = metaDataProvider.getEndTime();
        progress = new Progress(totalTables.get(), updatedTables.get());
        if (errorMap.containsKey(sourceId)){
            progress.setError(errorMap.get(sourceId));
        }else{
            progress.setError("");
        }
        progress.setStartTime(startTime.get());
        progress.setEndTime(endTime.get());
        return progress;
    }

    public enum DatabaseType {
        HIVE,
        MYSQL,
        ORACLE,
        POSTGRESQL;

        public String getName() {
            return name().toLowerCase();
        }
    }

    public List<DataOwnerHeader> getDataOwner(String guid) throws AtlasBaseException {
        try {
            List<DataOwnerHeader> owners = tableDAO.getDataOwnerList(guid);
            return owners;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    public List<String> getDataOwnerId(String guid) throws AtlasBaseException {
        try {
            List<String> ownerIdList = tableDAO.getDataOwnerIdList(guid);
            return ownerIdList;
        } catch (Exception e) {
            LOG.error(e.getMessage());
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

    @Transactional
    public EntityMutationResponse hardDeleteByGuid(String guid) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            String roleId = roleDAO.getRoleIdByUserId(userId);
            if(!"1".equals(roleId)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户无权限使用该接口");
            }
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
            AtlasEntity entity = info.getEntity();
            AtlasEntity.Status status = entity.getStatus();
            if(AtlasEntity.Status.DELETED != status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前实体未被删除，禁止使用硬删除");
            }
            List<String> deleteAllGuids = new ArrayList<>();
            List<String> deleteTableGuids = new ArrayList<>();
            deleteAllGuids.add(guid);
            //如果是数据库，则查找数据库下的表
            if (entity.getTypeName().equals("hive_db")) {
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
                //关联关系
                relationDAO.deleteByTableGuid(tableGuid);
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

    public int updateTableEditInfo(String tableGuid,Table info) throws AtlasBaseException {
        try {
            return tableDAO.updateTableEditInfo(tableGuid, info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新失败");
        }
    }


    @Transactional
    public void updateTableInfo(String tableGuid, Table tableInfo) throws AtlasBaseException {
        try {
            tableDAO.updateTableInfo(tableGuid, tableInfo);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }


    public File exportExcel(List<String> dbGuidList) throws AtlasBaseException {
        try {
            List<Table> tableList = new ArrayList<>();
            List<String> tableGuidList = new ArrayList<>();
            if(null != dbGuidList) {
                for (String dbGuid : dbGuidList) {
                    PageResult<Table> tablePageResult = searchService.getTableByDB(dbGuid, 0, -1);
                    tablePageResult.getLists().stream().forEach(table -> {
                        if("ACTIVE".equals(table.getStatus())) {
                            tableGuidList.add(table.getTableId());
                        }
                    });
                }
            }
            for (String tableGuid : tableGuidList) {
                Table table = getTableInfoById(tableGuid);
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
            LOG.error(e.getMessage());
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
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN); //下边框
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);//左边框
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);//上边框
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);//右边框*/
            for(Table table : tableList) {
                createMetadataTableSheet(workbook, table, headerStyle, cellStyle);
                createMetadataColumnSheet(workbook, table, headerStyle, cellStyle);
            }
        }
        return workbook;
    }

    public String processSpecialCharacter(String sheetName) {
        return sheetName.replace(":","_")
                 .replace("\\","_")
                 .replace("/","_")
                 .replace("?","_")
                 .replace("*","_")
                 .replace("[","_")
                 .replace("]","_");
    }

    public void createMetadataTableSheet(Workbook workbook, Table table, CellStyle headerStyle, CellStyle cellStyle) {
        String tableName = table.getTableName();
        String dbName = table.getDatabaseName();
        int rowNumber = 0;
        String sheetNamePrefix = dbName + "." + tableName;
        sheetNamePrefix = processSpecialCharacter(sheetNamePrefix);
        String sheetName = sheetNamePrefix + "-表信息";
        Sheet hasSheet = workbook.getSheet(sheetName);
        int sheetIndex = 1;
        while(null != hasSheet) {
            sheetNamePrefix = sheetNamePrefix + (++sheetIndex);
            sheetName = sheetNamePrefix + "-表信息";
            hasSheet = workbook.getSheet(sheetName);
        }
        Sheet sheet = workbook.createSheet(sheetName);

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
        typeValueCell.setCellValue("INTERNAL_TABLE".equals(type)?"内部表":"外部表");
        typeValueCell.setCellStyle(cellStyle);

        Boolean isPartitionTable = table.getPartitionTable();
        Row isPartitionTableRow = sheet.createRow(rowNumber++);
        Cell isPartitionTableKeyCell = isPartitionTableRow.createCell(0);
        isPartitionTableKeyCell.setCellValue("分区表");
        isPartitionTableKeyCell.setCellStyle(cellStyle);
        Cell isPartitionTableValueCell = isPartitionTableRow.createCell(1);
        isPartitionTableValueCell.setCellValue((true==isPartitionTable)?"是":"否");
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

        CellRangeAddress sourceSystemRangeAddress = new CellRangeAddress(rowNumber,rowNumber,0,1);
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

        CellRangeAddress dataWarehouseRangeAddress = new CellRangeAddress(rowNumber,rowNumber,0,1);
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

        CellRangeAddress catalogRangeAddress = new CellRangeAddress(rowNumber,rowNumber,0,1);
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
        for(Table.BusinessObject businessObject : businessObjectList) {
            CellRangeAddress businessRangeAddress = new CellRangeAddress(rowNumber,rowNumber,0,1);
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

    public void createMetadataColumnSheet(Workbook workbook, Table table, CellStyle headerStyle, CellStyle cellStyle) {

        String tableName = table.getTableName();
        String dbName = table.getDatabaseName();
        int rowNumber = 0;
        String sheetNamePrefix = dbName + "." + tableName;
        sheetNamePrefix = processSpecialCharacter(sheetNamePrefix);
        String sheetName = sheetNamePrefix + "-字段信息";
        Sheet hasSheet = workbook.getSheet(sheetName);
        int sheetIndex = 1;
        while(null != hasSheet) {
            sheetNamePrefix = sheetNamePrefix + (++sheetIndex);
            sheetName = sheetNamePrefix + "-字段信息";
            hasSheet = workbook.getSheet(sheetName);
        }

        Sheet sheet = workbook.createSheet(sheetName);

        List<Column> columnList = table.getColumns();
        List<Column> normalColumnList = columnList.stream().filter(column -> column.getPartitionKey()==false).collect(Collectors.toList());
        List<Column> partitionColumnList = columnList.stream().filter(column -> column.getPartitionKey()==true).collect(Collectors.toList());

        CellRangeAddress normalColumnRangeAddress = new CellRangeAddress(rowNumber,rowNumber,0,2);
        sheet.addMergedRegion(normalColumnRangeAddress);
        Row normalColumnRow = sheet.createRow(rowNumber++);
        Cell normalColumnRowCell = normalColumnRow.createCell(0);
        normalColumnRowCell.setCellValue("普通字段");
        normalColumnRowCell.setCellStyle(headerStyle);
        RegionUtil.setBorderLeft(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderRight(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);
        RegionUtil.setBorderTop(BorderStyle.THIN.getCode(), normalColumnRangeAddress, sheet);

        String[] headers = new String[]{"名称","类型","描述"};
        Row normalColumnHeaderRow = sheet.createRow(rowNumber++);
        for(int i=0; i<headers.length; i++) {
            Cell headerCell = normalColumnHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(cellStyle);
        }

        createDataCell(normalColumnList, sheet, rowNumber, cellStyle);
        rowNumber += normalColumnList.size();
        CellRangeAddress partitionColumnRangeAddress = new CellRangeAddress(rowNumber,rowNumber,0,2);
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
        for(int i=0; i<headers.length; i++) {
            Cell headerCell = partitionColumnHeaderRow.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(cellStyle);
        }

        createDataCell(partitionColumnList, sheet, rowNumber, cellStyle);
    }

    public void createDataCell(List<Column> columnList, Sheet sheet, Integer rowNumber, CellStyle cellStyle) {
        for(int i=0; i<columnList.size(); i++) {
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
        if(null != tableMetadataList && tableMetadataList.size()>0) {
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
            for(String key : currentMetadataMap.keySet()) {
                String currentValue = currentMetadataMap.get(key);
                String oldValue = oldMetadataMap.get(key);
                currentValue = Objects.isNull(currentValue)? "":currentValue;
                oldValue = Objects.isNull(oldValue)? "":oldValue;
                if(!currentValue.equals(oldValue)) {
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
        try {
            List<ColumnMetadata> currentMetadata = metadataHistoryDAO.getLastColumnMetadata(tableGuid);
            List<ColumnMetadata> oldMetadata = metadataHistoryDAO.getColumnMetadata(tableGuid, version);

            Map<String, Map> currentColumnMedataMap = new HashMap<>();
            for (ColumnMetadata metadata : currentMetadata) {
                String name = metadata.getName();
                Map<String, String> columnMetadataMap = BeanUtils.describe(metadata);
                currentColumnMedataMap.put(name, columnMetadataMap);
            }
            Map<String, Map> oldColumnMedataMap = new HashMap<>();
            for (ColumnMetadata metadata : oldMetadata) {
                String name = metadata.getName();
                Map<String, String> columnMetadataMap = BeanUtils.describe(metadata);
                oldColumnMedataMap.put(name, columnMetadataMap);
            }

            /*Set<String> keySet = new HashSet<>();
            keySet.addAll(currentColumnMedataMap.keySet());
            keySet.addAll(oldColumnMedataMap.keySet());
            for(String name : keySet) {
                Map<String, String> currentValueMap = currentColumnMedataMap.get(name);
                Map<String, String> oldValueMap = oldColumnMedataMap.get(name);

                if((currentColumnMedataMap.containsKey(name) && !oldColumnMedataMap.containsKey(name)) ||
                        (!currentColumnMedataMap.containsKey(name) && oldColumnMedataMap.containsKey(name))) {
                    if(currentColumnMedataMap.containsKey(name)) {

                    }
                }

                for(String key : currentValueMap.keySet()) {
                    String currentValue = currentValueMap.get(key);
                    String oldValue = oldValueMap.get(key);
                    currentValue = Objects.isNull(currentValue)? "":currentValue;
                    oldValue = Objects.isNull(oldValue)? "":oldValue;

                    if(!currentValue.equals(oldValue)) {
                        changedFiledSet.add(key);
                    }
                }
            }*/
            comparisonMetadata.setCurrentMetadata(currentMetadata);
            comparisonMetadata.setOldMetadata(oldMetadata);
            comparisonMetadata.setChangedSet(changedFiledSet);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
        return comparisonMetadata;
    }

    public PageResult<MetaDataRelatedAPI> getTableInfluenceWithAPI(String tableGuid, Parameters parameters) {
        PageResult pageResult = new PageResult();
        List<MetaDataRelatedAPI> influenceAPIList = tableDAO.getTableInfluenceWithAPI(tableGuid, parameters.getLimit(), parameters.getOffset());
        influenceAPIList.forEach(api -> {
            String version = api.getVersion();
            String path = api.getPath();
            api.setPath("/api/" + version + "/share/" + path);
        });
        if(null != influenceAPIList && influenceAPIList.size()>0) {
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
                if("hive_process".equals(atlasEntity.getTypeName())) {
                    continue;
                }
                String guid = atlasEntity.getGuid();
                tableHeader.setTableId(guid);
                //tableName
                if (atlasEntity.hasAttribute("name") && Objects.nonNull(atlasEntity.getAttribute("name")))
                    tableHeader.setTableName(atlasEntity.getAttribute("name").toString());
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
}
