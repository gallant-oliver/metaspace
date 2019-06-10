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

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.gridsum.gdp.library.commons.exception.VerifyException;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;
import io.zeta.metaspace.discovery.MetaspaceGremlinService;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.common.filetable.*;
import io.zeta.metaspace.web.config.FiletableConfig;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.model.Progress;
import io.zeta.metaspace.web.model.TableSchema;
import io.zeta.metaspace.web.model.filetable.UploadJobInfo;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.*;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.store.AtlasTypeDefStore;
import org.apache.avro.Schema;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/25 15:11
 */
@Service
public class MetaDataService {
    private static final Logger LOG = LoggerFactory.getLogger(MetaDataService.class);

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
    private String errorMessage;
    @Autowired
    private HiveMetaStoreBridgeUtils hiveMetaStoreBridgeUtils;


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
            return table;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        }
    }

    public Table extractTableInfo(AtlasEntity entity, String guid) throws AtlasBaseException {
        Table table = new Table();
        table.setTableId(guid);
        if (entity.getTypeName().contains("table")) {
            //表名称
            table.setTableName(getEntityAttribute(entity, "name"));
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
            table.setEditTag(false);
            try {
                List<Module> modules = userDAO.getModuleByUserId(AdminUtils.getUserData().getUserId());
                for (Module module : modules) {
                    if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                        table.setEditTag(true);
                        if (table.getTablePermission().isWRITE()) {
                            table.setEdit(true);
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
                //所属系统
                table.setSubordinateSystem("");
                //所属数据库
                table.setSubordinateDatabase("");
                //源系统管理员
                table.setSystemAdmin(new ArrayList<String>());
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
                if (atlasEntity.getTypeName().contains("table") || atlasEntity.getTypeName().contains("hdfs")) {
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
                if (atlasEntity.getTypeName().contains("column")) {
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
    public void refreshCache() throws AtlasBaseException {

    }

    /**
     * 同步元数据
     *
     * @return
     */
    public void synchronizeMetaData(String databaseType, TableSchema tableSchema){
        DatabaseType databaseTypeEntity = getDatabaseType(databaseType);
        if (null == databaseTypeEntity) {
            errorMessage = String.format("not support database type %s", databaseType);
            LOG.error(errorMessage);
            return;
        }
        if (hiveMetaStoreBridgeUtils == null) {
            errorMessage = String.format("get hiveMetaStoreBridgeUtils instance error: init hive metastore bridge error");
            LOG.error(errorMessage);
            return;
        }
        errorMessage = "";
        try {
            switch (databaseTypeEntity) {
                case HIVE:
                    hiveMetaStoreBridgeUtils.importDatabases(tableSchema);
                    break;
                case MYSQL:
                case ORACLE:
                case POSTGRESQL:
                    errorMessage = String.format("not support database type %s", databaseType);
                    LOG.error(errorMessage);
                    break;
            }
        } catch (HiveException e) {
            errorMessage = "同步元数据出错，无法连接到hive";
            LOG.error("import metadata error,", e);
        } catch (Exception e) {
            errorMessage = String.format("同步元数据出错，%s", e.getMessage());
            LOG.error("import metadata error", e);
        }
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

    public Progress importProgress(String databaseType) throws Exception {
        DatabaseType databaseTypeEntity = getDatabaseType(databaseType);
        Progress progress = new Progress(0, 0, "");
        if (null == databaseTypeEntity) {
            errorMessage = String.format("not support database type %s", databaseType);
            LOG.error(errorMessage);
            progress.setError(errorMessage);
            return progress;
        }
        if (hiveMetaStoreBridgeUtils == null) {
            errorMessage = String.format("get hiveMetaStoreBridgeUtils instance error: init hive metastore bridge error");
            LOG.error(errorMessage);
            progress.setError(errorMessage);
            return progress;
        }
        switch (databaseTypeEntity) {
            case HIVE:
                AtomicInteger totalTables = hiveMetaStoreBridgeUtils.getTotalTables();
                AtomicInteger updatedTables = hiveMetaStoreBridgeUtils.getUpdatedTables();
                AtomicLong startTime = hiveMetaStoreBridgeUtils.getStartTime();
                AtomicLong endTime = hiveMetaStoreBridgeUtils.getEndTime();
                progress = new Progress(totalTables.get(), updatedTables.get());
                progress.setError(errorMessage);
                progress.setStartTime(startTime.get());
                progress.setEndTime(endTime.get());
                break;
            case MYSQL:
            case ORACLE:
            case POSTGRESQL:
                progress.setError(String.format("not support database type %s, hive is support", databaseType));
                break;
        }
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

    @AtlasService
    public static class UploadJobService {

        private static final Logger LOGGER = LoggerFactory.getLogger(UploadJobService.class);

        public String getPath(String jobId) {
            return FiletableConfig.getUploadPath() + jobId + ".upload";
        }

        /**
         * 将文件从临时文件写入本地（返回jobId和filePath）
         *
         * @param tempFile
         * @return
         */
        public UploadFileInfo uploadFile(File tempFile) {
            String jobId = UUIDUtils.alphaUUID();
            String filePath = StringUtils.obtainFilePath(jobId);
            try {
                org.apache.commons.io.FileUtils.forceMkdir(new File(FiletableConfig.getUploadPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            File uploadFile = new File(filePath);
            //确保文件名不重名
            while (uploadFile.exists()) {
                jobId = UUIDUtils.alphaUUID();
                filePath = StringUtils.obtainFilePath(jobId);
                uploadFile = new File(filePath);
            }
            try {
                org.apache.commons.io.FileUtils.copyFile(tempFile, uploadFile);
                org.apache.commons.io.FileUtils.forceDelete(tempFile);
            } catch (IOException e) {
                uploadFile.delete();
                throw new RuntimeException(e);
            }
            UploadFileInfo twoTuple = new UploadFileInfo();
            twoTuple.setFilePath(filePath);
            twoTuple.setJobId(jobId);

            return twoTuple;
        }


        /**
         * 预览Excel xls数据</p>
         *
         * @param workbook
         * @param headers
         * @param size
         * @param sheetName     if null，sheet = workbook.getSheetAt(0)
         * @param includeHeader
         * @return
         */
        private UploadPreview previewExcel(Workbook workbook, CsvHeader headers, int size, String sheetName, boolean includeHeader) {
            Sheet sheet;
            if (sheetName == null) {
                sheet = workbook.getSheetAt(0);
            } else {
                sheet = workbook.getSheet(sheetName);
            }
            if (headers == null) {
                headers = ExcelUtils.readExcelHerder(sheet, includeHeader);
            }
            List<List<String>> previewValues = ExcelUtils.readExcelDatas(sheet, size, headers.size(), includeHeader);
            //读取表头信息
            List<String> tableHeads = ExcelUtils.readTableHeads(sheet, headers.size());
            UploadPreview preview = new UploadPreview();
            preview.setIncludeHeader(includeHeader);
            preview.setHeaders(headers.getColumnExtList());
            preview.setRows(previewValues);
            preview.setSize(ExcelUtils.getDatasSize(sheet, includeHeader));
            preview.setTableHeads(tableHeads);
            if (workbook instanceof HSSFWorkbook) {
                preview.setFileType(FileType.XLS);
            } else if (workbook instanceof XSSFWorkbook) {
                preview.setFileType(FileType.XLSX);
            }
            preview.setSheets(ExcelUtils.getAllSheetNames(workbook));
            return preview;
        }


        public UploadPreview previewUpload(String jobId, String fileCode, String delimiter, boolean includeHeader, CsvHeader csvHeader, int size) {
            String filePath = getPath(jobId);
            try {
                CsvEncode csvEncode = CsvEncode.of(fileCode);
                return CsvUtils.getHeadersWithPreview(filePath, csvEncode.name(), delimiter, includeHeader, csvHeader, size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public UploadPreview previewUpload(String jobId, UploadConfig uploadConfig, int size) {
            FileType fileType = uploadConfig.getFileType();
            if (fileType != null && FileType.XLS.equals(fileType)) {
                return previewExcel(jobId, uploadConfig, size);
            } else if (fileType != null && FileType.XLSX.equals(fileType)) {
                try {
                    return previewExcelForXLSX(jobId, uploadConfig, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            CsvHeader csvHeader;
            if (uploadConfig.getColumns() != null && uploadConfig.getColumns().size() > 0) {
                csvHeader = new CsvHeader(uploadConfig.getColumns());
            } else {
                try {
                    csvHeader = CsvUtils.detectCsvHeader(getPath(jobId), uploadConfig.getFileEncode(), uploadConfig.getFieldDelimiter(), uploadConfig.isIncludeHeaders());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return previewUpload(jobId, uploadConfig.getFileEncode(), uploadConfig.getFieldDelimiter(), uploadConfig.isIncludeHeaders(), csvHeader, size);
        }

        private UploadPreview previewExcel(String jobId, UploadConfig uploadConfig, int size) {
            String filePath = getPath(jobId);
            CsvHeader csvHeader = null;
            if (uploadConfig.getColumns() != null && uploadConfig.getColumns().size() > 0) {
                csvHeader = new CsvHeader(uploadConfig.getColumns());
            }
            Workbook workbook = ExcelUtils.isExcelFile(filePath);
            if (workbook != null) {
                return previewExcel(workbook, csvHeader, size, uploadConfig.getSheetName(), uploadConfig.isIncludeHeaders());
            } else {
                throw new VerifyException("不支持的文件类型！");
            }
        }

        /**
         * 获取xlsx 的数据用于大数据传输
         *
         * @param jobId
         * @param uploadConfig
         * @param size
         * @return
         * @throws Exception
         */
        private UploadPreview previewExcelForXLSX(String jobId, UploadConfig uploadConfig, final int size) throws Exception {
            String filePath = getPath(jobId);
            ExcelReader reader = new ExcelReader() {
                @Override
                public void getRows(int sheetIndex, int curRow, List<String> rowList) {
                    if (rowList != null && !rowList.isEmpty() && this.getPreviewRows().size() < size) {
                        List<String> tempList = new ArrayList<>(rowList);
                        this.add(tempList);
                    }
                    if (rowList != null && !rowList.isEmpty()) {
                        this.totalNum++;
                    }
                }
            };
            reader.processTableName(filePath);
            String sheetName = null;
            if (uploadConfig != null) {
                sheetName = uploadConfig.getSheetName();
            }
            int index = 0;
            for (String name : reader.getTableNames()) {
                index++;
                if (sheetName == null || sheetName.equals(name)) {
                    break;
                }
            }
            reader.process(filePath, index);
            List<List<String>> previewValues = reader.getPreviewRows();
            UploadPreview preview = new UploadPreview();
            preview.setIncludeHeader(false);
            preview.setHeaders(preview.getHeaders());
            preview.setRows(previewValues);
            preview.setSize(reader.getAllSize(false));
            preview.setTableHeads(previewValues.get(0));
            preview.setFileType(FileType.XLSX);
            preview.setSheets(reader.getTableNames());
            return preview;
        }

        public UploadJobInfo createUploadJob(UploadJobInfo uploadJobInfo) {
            Preconditions.checkNotNull(uploadJobInfo, "uploadJobInfo should not be null");

            return null;
        }

        public String getAvroSchemaJson(UploadConfig uploadConfig) {
            // 判断是否有重复的列名
            CsvHeader.valid(uploadConfig.getColumns().toArray(new ColumnExt[uploadConfig.getColumns().size()]));
            JSONObject schemaJson = new JSONObject();
            try {
                schemaJson.put("namespace", "com.gridsum.metaspace." + uploadConfig.getDatabase());
                schemaJson.put("name", uploadConfig.getTableName());
                schemaJson.put("type", "record");
                JSONArray fields = new JSONArray();
                for (ColumnExt column : uploadConfig.getColumns()) {
                    JSONObject field = new JSONObject();
                    field.put("name", Ascii.toLowerCase(column.getName()));
                    field.put("type", new String[]{column.getType().avroType, Schema.Type.NULL.getName()});
                    fields.put(field);
                }
                schemaJson.put("fields", fields);
            } catch (JSONException e) {
                LOGGER.error("create avro schema failed", e);
            }
            return schemaJson.toString();
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
            User user = AdminUtils.getUserData();
            String id = user.getRoleId();
            if(!"1".equals(id)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户权限使用该接口");
            }
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
            AtlasEntity entity = info.getEntity();
            AtlasEntity.Status status = entity.getStatus();
            if(AtlasEntity.Status.DELETED != status) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前实体未被删除，禁止使用硬删除");
            }
            EntityMutationResponse response = entitiesStore.hardDeleteById(guid);
            //表详情
            tableDAO.deleteTableInfo(guid);
            //owner
            tableDAO.deleteTableRelatedOwner(guid);
            //关联关系
            relationDAO.deleteByTableGuid(guid);
            //business2table
            businessDAO.deleteBusinessRelationByTableGuid(guid);
            //表标签
            tableTagDAO.delAllTable2Tag(guid);

            return response;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "硬删除失败");
        }
    }
}
