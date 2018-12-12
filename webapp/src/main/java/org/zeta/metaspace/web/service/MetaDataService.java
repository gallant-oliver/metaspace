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
package org.zeta.metaspace.web.service;

import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;

import com.gridsum.gdp.library.commons.exception.VerifyException;
import com.gridsum.gdp.library.commons.utils.FileUtils;
import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.SortOrder;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.glossary.GlossaryService;
import org.apache.atlas.model.glossary.AtlasGlossary;
import org.apache.atlas.model.glossary.AtlasGlossaryCategory;
import org.apache.atlas.model.glossary.AtlasGlossaryTerm;
import org.apache.atlas.model.glossary.relations.AtlasGlossaryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedCategoryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedTermHeader;
import org.apache.atlas.model.glossary.relations.AtlasTermCategorizationHeader;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
import org.apache.atlas.model.typedef.AtlasEntityDef;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.store.AtlasTypeDefStore;
import org.zeta.metaspace.model.metadata.Database;
import org.zeta.metaspace.model.metadata.Parameters;
import org.zeta.metaspace.model.result.PageResult;
import org.zeta.metaspace.web.common.filetable.ColumnExt;
import org.zeta.metaspace.web.common.filetable.CsvEncode;
import org.zeta.metaspace.web.common.filetable.CsvHeader;
import org.zeta.metaspace.web.common.filetable.CsvUtils;
import org.zeta.metaspace.web.common.filetable.ExcelReader;
import org.zeta.metaspace.web.common.filetable.FileType;
import org.zeta.metaspace.web.common.filetable.UploadConfig;
import org.zeta.metaspace.web.common.filetable.UploadFileInfo;
import org.zeta.metaspace.web.common.filetable.UploadPreview;
import org.zeta.metaspace.web.config.FiletableConfig;
import org.zeta.metaspace.web.model.filetable.UploadJobInfo;
import org.zeta.metaspace.web.util.ExcelUtils;
import org.zeta.metaspace.web.util.HiveJdbcUtils;
import org.zeta.metaspace.web.util.StringUtils;
import org.apache.avro.Schema;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.zeta.metaspace.discovery.MetaspaceLineageService;
import org.zeta.metaspace.model.metadata.CategoryChildren;
import org.zeta.metaspace.model.metadata.CategoryEntity;
import org.zeta.metaspace.model.metadata.CategoryHeader;
import org.zeta.metaspace.model.metadata.Column;
import org.zeta.metaspace.model.metadata.ColumnEdit;
import org.zeta.metaspace.model.metadata.ColumnLineageInfo;
import org.zeta.metaspace.model.metadata.ColumnQuery;
import org.zeta.metaspace.model.metadata.LineageDepthInfo;
import org.zeta.metaspace.model.metadata.LineageTrace;
import org.zeta.metaspace.model.metadata.RelationEntity;
import org.zeta.metaspace.model.metadata.Table;
import org.zeta.metaspace.model.metadata.TableEdit;
import org.zeta.metaspace.model.metadata.TableLineageInfo;
import org.zeta.metaspace.model.metadata.TablePermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/25 15:11
 */
@Service
public class MetaDataService {
    private static final Logger LOG  = LoggerFactory.getLogger(MetaDataService.class);

    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    private AtlasLineageService atlasLineageService;
    @Autowired
    private  GlossaryService glossaryService;
    @Autowired
    AtlasTypeDefStore typeDefStore;
    @Autowired
    MetaspaceLineageService metaspaceLineageService;

    @Cacheable(value = "tableCache", key = "#guid", condition = "#refreshCache==false")
    public Table getTableInfoById(String guid, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableInfoById({})", guid);
        }
        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        Table table  = new Table();
        table.setTableId(guid);
        try {
            //获取entity
            AtlasEntity entity = getEntityById(guid);
            if(Objects.isNull(entity)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未找到数据表信息");
            }
            if(entity.getTypeName().contains("table")) {
                //表名称
                table.setTableName(getEntityAttribute(entity, "name"));
                //判断是否为虚拟表
                if(table.getTableName().contains("values__tmp__table"))
                    table.setVirtualTable(true);
                else
                    table.setVirtualTable(false);
                //状态
                table.setStatus(entity.getStatus().name());
                //创建人
                table.setOwner(getEntityAttribute(entity, "owner"));
                //创建时间
                Object createTime = entity.getAttribute("createTime");
                SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatDateStr = sdf.format(createTime);
                table.setCreateTime(formatDateStr);
                //描述
                table.setDescription(getEntityAttribute(entity, "comment"));

                if(entity.hasAttribute("sd") && Objects.nonNull(entity.getAttribute("sd"))) {
                    Object obj = entity.getAttribute("sd");
                    if(obj instanceof AtlasObjectId) {
                        AtlasObjectId atlasObject = (AtlasObjectId)obj;
                        String sdGuid = atlasObject.getGuid();
                        AtlasEntity sdEntity = getEntityById(sdGuid);
                        //位置
                        table.setLocation(getEntityAttribute(sdEntity, "location"));
                        //格式
                        String inputFormat = getEntityAttribute(sdEntity, "inputFormat");
                        if(Objects.nonNull(inputFormat)) {
                            String[] fullFormat = inputFormat.split("\\.");
                            table.setFormat(fullFormat[fullFormat.length-1]);
                        }
                    }
                }
                //类型
                String tableType = getEntityAttribute(entity, "tableType");
                if(tableType.contains("EXTERNAL")) {
                    table.setType("EXTERNAL_TABLE");
                } else {
                    table.setType("INTERNAL_TABLE");
                }
                //是否为分区表
                if(entity.hasAttribute("partitionKeys") && Objects.nonNull(entity.getAttribute("partitionKeys"))) {
                    table.setPartitionTable(true);
                } else {
                    table.setPartitionTable(false);
                }
                //数据库名
                AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
                table.setDatabaseId(relatedObject.getGuid());
                table.setDatabaseName(relatedObject.getDisplayText());
                //所属业务
                table.setBusiness("");
                //表关联信息
                List<String> relations = getRelationList(guid);
                table.setRelations(relations);
                //类别
                table.setCategory("");
                //表生命周期
                table.setTableLife("");
                //分区生命周期
                table.setPartitionLife("");
                //分类信息
                table.setTopic("");
                //权限
                TablePermission permission = new TablePermission();
                table.setTablePermission(permission);
            }
            ColumnQuery columnQuery = new ColumnQuery();
            columnQuery.setGuid(guid);
            List<Column> columns = getColumnInfoById(columnQuery, true);
            table.setColumns(columns);
            return table;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到数据库表信息");
        }
    }

    public AtlasEntity getEntityById(String guid) throws AtlasBaseException {
        AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
        return info.getEntity();
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if(entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        if(entity.hasRelationshipAttribute("db") && Objects.nonNull(entity.getRelationshipAttribute("db"))) {
            Object obj = entity.getRelationshipAttribute("db");
            if(obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId)obj;
            }
        }
        return objectId;
    }

    public List<String> getRelationList(String guid) throws AtlasBaseException {
        List<String> relations = new ArrayList<>();
        //获取当前entity
        AtlasEntity entityInfo = entitiesStore.getById(guid).getEntity();

        List<String> relationDirs = new ArrayList<>();
        String tableName = entityInfo.getAttribute("name").toString();

        //获取RelationShip中的"meanings"属性
        List<AtlasRelatedObjectId> relatedObjectIds = (ArrayList)entityInfo.getRelationshipAttribute("meanings");
        if(Objects.nonNull(relatedObjectIds) && relatedObjectIds.size()!=0) {

            //遍历，得到每一个关联关系
            for(AtlasRelatedObjectId objectId: relatedObjectIds) {
                if(objectId.getRelationshipStatus().name().equals("DELETED"))
                    continue;
                relationDirs.add(tableName);
                String termGuid = objectId.getGuid();
                //获取Term
                AtlasGlossaryTerm term = glossaryService.getTerm(termGuid);
                //获取与Term关联的CategoryHeather
                Set<AtlasTermCategorizationHeader> termCategoryHeaders = term.getCategories();
                if(Objects.nonNull(termCategoryHeaders)) {
                    Iterator<AtlasTermCategorizationHeader> categories = termCategoryHeaders.iterator();
                    if(categories.hasNext()) {
                        AtlasTermCategorizationHeader categoryHeader = categories.next();
                        //根据guid获取category
                        String categoryGuid = categoryHeader.getCategoryGuid();
                        AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);
                        //获取category的名字
                        String categoryName = category.getName();
                        relationDirs.add(categoryName);
                        while(Objects.nonNull(category.getParentCategory())) {
                            AtlasRelatedCategoryHeader parentCategoryHeader = category.getParentCategory();
                            category = glossaryService.getCategory(parentCategoryHeader.getCategoryGuid());
                            relationDirs.add(category.getName());
                        }
                    }
                }
                StringBuffer path = new StringBuffer();
                for(int i=relationDirs.size()-1; i>0; i--) {
                    path.append(relationDirs.get(i) + "/");
                }
                path.append(relationDirs.get(0));
                relations.add(path.toString());
                relationDirs.clear();
            }
        }
        return relations;
    }

    @Cacheable(value = "columnCache", key = "#query.guid + #query.columnFilter.columnName + #query.columnFilter.type + #query.columnFilter.description", condition = "#refreshCache==false")
    public List<Column> getColumnInfoById(ColumnQuery query, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getColumnInfoById({})", query);
        }
        String guid = query.getGuid();
        List<Column> columns = new ArrayList<>();
        Column column = null;
        //获取entity
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
            AtlasEntity entity = info.getEntity();

            //获取PartitionKey的guid
            List<AtlasObjectId> partitionKeys = null;
            if(Objects.nonNull(entity.getAttribute("partitionKeys"))) {
                Object partitionObjects = entity.getAttribute("partitionKeys");
                if(partitionObjects  instanceof ArrayList<?>) {
                    partitionKeys = (ArrayList<AtlasObjectId>)partitionObjects;
                }
            }
            Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
            for(String key: referredEntities.keySet()) {
                AtlasEntity referredEntity = referredEntities.get(key);
                if(referredEntity.getTypeName().contains("column") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
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
                    if(partitionKeys != null) {
                        for (int i = 0; i < partitionKeys.size(); i++) {
                            if (partitionKeys.get(i).getGuid().equals(column.getColumnId())) {
                                column.setPartitionKey(true);
                            }
                        }
                    }

                    Map<String,Object> attributes = referredEntity.getAttributes();
                    if(attributes.containsKey("name") && Objects.nonNull(attributes.get("name"))) {
                        column.setColumnName(attributes.get("name").toString());
                    } else {
                        column.setColumnName("");
                    }
                    if(attributes.containsKey("type") && Objects.nonNull(attributes.get("type"))) {
                        column.setType(attributes.get("type").toString());
                    } else {
                        column.setType("");
                    }
                    if(attributes.containsKey("comment") && Objects.nonNull(attributes.get("comment"))) {
                        column.setDescription(attributes.get("comment").toString());
                    } else {
                        column.setDescription("");
                    }
                    columns.add(column);
                }
            }
            //过滤
            if(query.getColumnFilter() != null) {
                ColumnQuery.ColumnFilter filter = query.getColumnFilter();
                String columnName = filter.getColumnName();
                String type = filter.getType();
                String description = filter.getDescription();
                if(Objects.nonNull(columnName) && !columnName.equals("")) {
                    columns = columns.stream().filter(col -> col.getColumnName().contains(filter.getColumnName())).collect(Collectors.toList());
                }
                if(Objects.nonNull(type) && !type.equals("")) {
                    columns = columns.stream().filter(col -> col.getType().contains(type)).collect(Collectors.toList());
                }
                if(Objects.nonNull(description) && !description.equals("")) {
                    columns = columns.stream().filter(col -> col.getDescription().contains(description)).collect(Collectors.toList());
                }
            }
            return columns;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }
    }

    public TableLineageInfo getTableLineage(String guid, AtlasLineageInfo.LineageDirection direction,
                                            int depth) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableLineage({}, {}, {})", guid, direction, depth);
        }
        try {
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, direction, depth);
            if(Objects.isNull(lineageInfo)) {
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
            for(String key: entities.keySet()) {
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
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            if(Objects.nonNull(entities) && entities.size()!=0) {
                AtlasEntityHeader atlasEntity = entities.get(guid);
                if (atlasEntity.getTypeName().contains("table")) {
                    //guid
                    lineageDepthEntity.setGuid(guid);
                    AtlasEntity atlasTableEntity = getEntityById(guid);
                    //tableName
                    lineageDepthEntity.setTableName(getEntityAttribute(atlasTableEntity, "name"));
                    //displayText
                    lineageDepthEntity.setDisplayText(atlasEntity.getDisplayText());
                    //updateTime
                    SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formatDateStr = sdf.format(atlasTableEntity.getUpdateTime());
                    lineageDepthEntity.setUpdateTime(formatDateStr);
                    //dbName
                    AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                    lineageDepthEntity.setDbName(relatedObject.getDisplayText());

                    //AtlasLineageInfo fullLineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
                    //lineageDepthEntity = getLineageDepth(lineageDepthEntity, fullLineageInfo);
                    lineageDepthEntity = getLineageDepthV2(lineageDepthEntity);
                }
            }
            return lineageDepthEntity;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘深度详情失败");
        }
    }

    public LineageDepthInfo getLineageDepth(LineageDepthInfo lineageDepthEntity,AtlasLineageInfo fullLineageInfo) {
        Set<AtlasLineageInfo.LineageRelation> fullRelations = fullLineageInfo.getRelations();
        String guid = lineageDepthEntity.getGuid();
        //直接上游表数量
        long directUpStreamNum = getInDirectRelationNode(guid, fullRelations).size();
        lineageDepthEntity.setDirectUpStreamNum(directUpStreamNum);
        //直接下游表数量
        long directDownStreamNum = getOutDirectRelationNode(guid, fullRelations).size();
        lineageDepthEntity.setDirectDownStreamNum(directDownStreamNum);
        //上游表层数
        long upStreamLevelNum = getMaxDepth("in", guid, fullRelations);
        lineageDepthEntity.setUpStreamLevelNum(upStreamLevelNum - 1);
        //下游表层数
        long downStreamLevelNum = getMaxDepth("out", guid, fullRelations);
        lineageDepthEntity.setDownStreamLevelNum((downStreamLevelNum - 1) / 2);
        return lineageDepthEntity;
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
     * @param guid
     * @param direction
     * @param depth
     * @param refreshCache
     * @return
     * @throws AtlasBaseException
     */
    public ColumnLineageInfo getColumnLineage(String guid, AtlasLineageInfo.LineageDirection direction,
                                              int depth, Boolean refreshCache) throws AtlasBaseException {
        try {
            ArrayList<AtlasObjectId> columns = null;
            ColumnLineageInfo info = new ColumnLineageInfo();
            AtlasEntity tableEntity = entitiesStore.getById(guid).getEntity();
            if(Objects.nonNull(tableEntity) && tableEntity.hasAttribute("columns")) {
                columns = (ArrayList<AtlasObjectId>)tableEntity.getAttribute("columns");
            }

            for(int i=0; i<columns.size(); i++) {
                String columnGuid = columns.get(i).getGuid();
                AtlasEntityHeader header = entitiesStore.getHeaderById(columnGuid);
                if(header.getStatus().equals(AtlasEntity.Status.DELETED))
                    continue;
                AtlasLineageInfo lineageInfo = metaspaceLineageService.getColumnLineageInfo(columnGuid, direction, depth);
                if(Objects.isNull(lineageInfo)) {
                    throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取字段血缘关系失败");
                }

                Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
                //guid
                info.setGuid(guid);
                //relations
                Set<LineageTrace> lineageRelations = getRelations(lineageInfo);
                //entities
                List<ColumnLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();

                for(String key: entities.keySet()) {

                    AtlasEntityHeader atlasEntity = entities.get(key);
                    //判断process类型
                    AtlasEntityDef entityDef = typeDefStore.getEntityDefByName(atlasEntity.getTypeName());
                    Set<String> types = entityDef.getSuperTypes();
                    Iterator<String> typeIterator = types.iterator();
                    if(Objects.nonNull(typeIterator) && typeIterator.hasNext()) {
                        String type = typeIterator.next();
                        if(type.contains("Process"))
                            continue;
                    }
                    ColumnLineageInfo.LineageEntity lineageEntity = getColumnEntityInfo(key, atlasEntity);
                    lineageEntities.add(lineageEntity);
                }
                reOrderRelation(lineageEntities, lineageRelations);
                removeTableEntityAndRelation(lineageEntities, lineageRelations);
                if(Objects.isNull(info.getRelations()) || info.getRelations().size()==0)
                    info.setRelations(lineageRelations);
                else
                    info.getRelations().addAll(lineageRelations);
                if(Objects.isNull(info.getEntities()) || info.getEntities().size()==0)
                    info.setEntities(lineageEntities);
                else
                    info.getEntities().addAll(lineageEntities);
            }
            //getAllTableLineageColumns(info.getEntities());
            return info;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }


    public ColumnLineageInfo getColumnLineageV2(String guid, AtlasLineageInfo.LineageDirection direction, int depth) throws AtlasBaseException {
        List<String> tables = metaspaceLineageService.getColumnRelatedTable(guid, direction, depth);
        tables.add(guid);
        ColumnLineageInfo.LineageEntity entity = null;
        List<ColumnLineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
        for(String tableGuid : tables) {
            AtlasEntity tableEntity = entitiesStore.getById(tableGuid).getEntity();
            String tableName = (String) tableEntity.getAttribute("name");
            AtlasRelatedObjectId db = (AtlasRelatedObjectId)tableEntity.getRelationshipAttribute("db");
            String dbName = db.getDisplayText();
            String dbGuid = db.getGuid();
            String dbStatus = db.getEntityStatus().name();
            String tableStatus = tableEntity.getStatus().name();
            List<AtlasRelatedObjectId> columns = (List<AtlasRelatedObjectId>)tableEntity.getRelationshipAttribute("columns");
            for(int i=0, size=columns.size(); i<size; i++) {
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

    public void removeTableEntityAndRelation(List<ColumnLineageInfo.LineageEntity> lineageEntities, Set<LineageTrace> lineageRelations) throws AtlasBaseException{
        Set<LineageTrace> removeNode = new HashSet<>();
        Set<ColumnLineageInfo.LineageEntity> removeEntity = new HashSet<>();
        for(ColumnLineageInfo.LineageEntity entity: lineageEntities) {
            String guid = entity.getGuid();
            AtlasEntityHeader header = entitiesStore.getHeaderById(guid);
            String typeName = header.getTypeName();
            if(typeName.contains("table")) {
                removeEntity.add(entity);
                Iterator<LineageTrace> iterator = lineageRelations.iterator();
                while(iterator.hasNext()) {
                    LineageTrace node = iterator.next();
                    if(node.getFromEntityId().equals(guid))
                        removeNode.add(node);
                }
            }
        }
        removeNode.stream().forEach(node -> lineageRelations.remove(node));
        removeEntity.stream().forEach(node -> lineageEntities.remove(node));
    }

    /**
     * 去除Process节点
     * @param lineageEntities
     * @param lineageRelations
     */
    public Set<LineageTrace> reOrderRelation(List<ColumnLineageInfo.LineageEntity> lineageEntities, Set<LineageTrace> lineageRelations) throws AtlasBaseException {
        Set<LineageTrace> resultRelation = new HashSet<>();
        LineageTrace trace = null;
        for(ColumnLineageInfo.LineageEntity entity: lineageEntities) {
            String fromGuid = entity.getGuid();
            Iterator<LineageTrace> fromIterator = lineageRelations.iterator();
            while(fromIterator.hasNext()) {
                LineageTrace fromNode = fromIterator.next();
                if(fromNode.getFromEntityId().equals(fromGuid)) {
                    String toGuid = fromNode.getToEntityId();
                    Iterator<LineageTrace> toIterator = lineageRelations.iterator();
                    while(toIterator.hasNext()) {
                        LineageTrace toNode = toIterator.next();
                        if(toNode.getFromEntityId().equals(toGuid)) {
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
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            if(Objects.nonNull(entities) && entities.size()!=0) {
                AtlasEntityHeader atlasEntity = entities.get(guid);
                if (atlasEntity.getTypeName().contains("column")) {
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
                    //AtlasLineageInfo fullLineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
                    //lineageDepthEntity = getLineageDepth(lineageDepthEntity, fullLineageInfo);
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
        LineageTrace  relation = null;
        while(it.hasNext()) {
            AtlasLineageInfo.LineageRelation atlasRelation = it.next();
            relation = new LineageTrace();
            relation.setFromEntityId(atlasRelation.getFromEntityId());
            relation.setToEntityId(atlasRelation.getToEntityId());
            lineageRelations.add(relation);
        }
        return lineageRelations;
    }

    public TableLineageInfo.LineageEntity getTableEntityInfo(String guid, TableLineageInfo.LineageEntity lineageEntity, Map<String, AtlasEntityHeader> entities, AtlasEntityHeader atlasEntity) throws AtlasBaseException{
        //guid
        if(Objects.nonNull(atlasEntity.getGuid()))
            lineageEntity.setGuid(atlasEntity.getGuid());
        lineageEntity.setProcess(false);
        //status
        lineageEntity.setStatus(atlasEntity.getStatus().name());
        //typeName
        lineageEntity.setTypeName(atlasEntity.getTypeName());
        AtlasEntityDef entityDef = typeDefStore.getEntityDefByName(atlasEntity.getTypeName());
        Set<String> types = entityDef.getSuperTypes();
        Iterator<String> typeIterator = types.iterator();
        if(Objects.nonNull(typeIterator) && typeIterator.hasNext()) {
            String type = typeIterator.next();
            if(type.contains("Process"))
                lineageEntity.setProcess(true);
        }
        lineageEntity.setStatus(atlasEntity.getStatus().name());
        //displayText
        if(Objects.nonNull(atlasEntity.getDisplayText())) {
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

    public ColumnLineageInfo.LineageEntity getColumnEntityInfo(String guid, AtlasEntityHeader atlasEntity) throws AtlasBaseException{
        ColumnLineageInfo.LineageEntity lineageEntity = new ColumnLineageInfo.LineageEntity();
        //guid
        if(Objects.nonNull(atlasEntity.getGuid()))
            lineageEntity.setGuid(atlasEntity.getGuid());
        lineageEntity.setColumnName(atlasEntity.getDisplayText());

        AtlasEntity atlasTableEntity = null;
        AtlasEntity atlasColumnEntity = entitiesStore.getById(guid).getEntity();
        AtlasRelatedObjectId relatedTable = (AtlasRelatedObjectId)atlasColumnEntity.getRelationshipAttribute("table");
        if(Objects.nonNull(relatedTable)) {
            atlasTableEntity = entitiesStore.getById(relatedTable.getGuid()).getEntity();
            //tableGuid
            lineageEntity.setTableGuid(relatedTable.getGuid());
            //tableName
            if (atlasTableEntity.hasAttribute("name") && Objects.nonNull(atlasTableEntity.getAttribute("name")))
                lineageEntity.setTableName(atlasTableEntity.getAttribute("name").toString());

            AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
            if (Objects.nonNull(relatedObject)) {
                //dbGuid
                lineageEntity.setDbGuid(relatedObject.getGuid());
                //dbName
                lineageEntity.setDbName(relatedObject.getDisplayText());
            }
        }
        return lineageEntity;
    }

    public Set<AtlasLineageInfo.LineageRelation> getOutDirectRelationNode(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for(Iterator it = relations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            if(relation.getFromEntityId().equals(guid)) {
                directRelations.add(relation);
            }
        }
        return directRelations;
    }

    public Set<AtlasLineageInfo.LineageRelation> getInDirectRelationNode(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<String> processGuids = new HashSet<>();
        for(Iterator it = relations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            if(relation.getToEntityId().equals(guid)) {
                processGuids.add(relation.getFromEntityId());
            }
        }

        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for(Iterator proIter = processGuids.iterator(); proIter.hasNext(); ) {
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
        for(Iterator it = directRelations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            max = Math.max(max, getOutMaxDepth(relation.getToEntityId(), relations));
        }
        return max + 1;
    }

    public Long getInMaxDepth(String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        Set<AtlasLineageInfo.LineageRelation> directRelations = getInDirectRelationNode(guid, relations);
        long max = 0;
        for(Iterator it = directRelations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            max = Math.max(max, getInMaxDepth(relation.getFromEntityId(), relations));
        }
        return max + 1;
    }

    public Long getMaxDepth(String direction, String guid, Set<AtlasLineageInfo.LineageRelation> relations) {
        long max = 0;
        if(direction.equals("out"))
            max = getOutMaxDepth(guid, relations);
        else if(direction.equals("in"))
            max = getInMaxDepth(guid, relations);
        return max;
    }

    @CacheEvict(value = "categoryCache", allEntries=true)
    public CategoryEntity createMetadataCategory(CategoryEntity category) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.createMetadataCategory({})", category);
        }
        List<AtlasGlossary> glossaries = glossaryService.getGlossaries(-1, 0, SortOrder.ASCENDING);
        AtlasGlossary baseGlosary = null;
        //如果Glossary为空，此时没有数据，则需要创建根Glossary
        if(Objects.isNull(glossaries) || glossaries.size() ==0) {
            baseGlosary = new AtlasGlossary();
            baseGlosary.setName("BaseGlosary");
            baseGlosary.setQualifiedName("BaseGlosary");
            baseGlosary = glossaryService.createGlossary(baseGlosary);
        } else {
            baseGlosary = glossaries.get(0);
        }
        AtlasGlossaryHeader baseGlossaryHeader = new AtlasGlossaryHeader();
        baseGlossaryHeader.setGlossaryGuid(baseGlosary.getGuid());
        baseGlossaryHeader.setDisplayText(baseGlosary.getName());

        AtlasGlossaryCategory tmpCategory = new AtlasGlossaryCategory();
        tmpCategory.setAnchor(baseGlossaryHeader);
        tmpCategory.setName(category.getName());
        tmpCategory.setShortDescription(category.getDescription());
        tmpCategory.setLongDescription(category.getDescription());
        AtlasRelatedCategoryHeader parentCategoryHeader = category.getParentCategory();
        if(Objects.nonNull(parentCategoryHeader))
            tmpCategory.setParentCategory(parentCategoryHeader);
        tmpCategory = glossaryService.createCategory(tmpCategory);
        category.setQualifiedName(tmpCategory.getQualifiedName());
        category.setGuid(tmpCategory.getGuid());
        category.setAnchor(tmpCategory.getAnchor());

        getCategories("ASC", true);
        return category;
    }

    @CacheEvict(value = "categoryCache", allEntries=true)
    public CategoryEntity updateMetadataCategory(CategoryEntity category) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.createMetadataCategory({})", category);
        }
        String guid = category.getGuid();
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(guid);
        String historyName = glossaryCategory.getName();
        glossaryCategory.setName(category.getName());
        glossaryCategory.setLongDescription(category.getDescription());
        glossaryCategory.setShortDescription(category.getDescription());
        String qualifiedName = glossaryCategory.getQualifiedName().replaceFirst(historyName, category.getName());
        glossaryCategory.setQualifiedName(qualifiedName);
        glossaryCategory = glossaryService.updateCategory_V2(glossaryCategory);

        if(Objects.nonNull(glossaryCategory.getAnchor()))
            category.setAnchor(glossaryCategory.getAnchor());
        if(Objects.nonNull(glossaryCategory.getParentCategory()))
            category.setParentCategory(glossaryCategory.getParentCategory());
        if(Objects.nonNull(glossaryCategory.getChildrenCategories()))
            category.setChildrenCategories(glossaryCategory.getChildrenCategories());
        category.setQualifiedName(glossaryCategory.getQualifiedName());
        return category;
    }

    @CacheEvict(value = "categoryCache", allEntries=true)
    public void deleteGlossaryCategory(String categoryGuid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.deleteGlossaryCategory({})", categoryGuid);
        }
        Set<String> deleteChildrenRelatedTerms = new HashSet<>();
        try {
            getCategoryChildrenTerms(categoryGuid, deleteChildrenRelatedTerms);
            Iterator<String> childrenRelatedTermsIterator = deleteChildrenRelatedTerms.iterator();
            while(childrenRelatedTermsIterator.hasNext()) {
                String termGuid = childrenRelatedTermsIterator.next();
                glossaryService.deleteTerm(termGuid);
            }
            glossaryService.deleteCategory(categoryGuid);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public void getCategoryChildrenTerms(String categoryGuid, Set<String> deleteChildrenRelatedTerms)throws AtlasBaseException {
        AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);
        Set<AtlasRelatedCategoryHeader> childrenCategories = category.getChildrenCategories();
        if(Objects.nonNull(childrenCategories) && childrenCategories.size()>0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录下仍存在子目录，请删除其子目录");

        //获取关联Term
        List<AtlasRelatedTermHeader> terms = glossaryService.getCategoryTerms(categoryGuid, 0, -1, SortOrder.ASCENDING);
        for (AtlasRelatedTermHeader term : terms) {
            String termGuid = term.getTermGuid();
            //获取Term关联Entity
            List<AtlasRelatedObjectId> relatedObjects = glossaryService.getAssignedEntities(termGuid,0,1, SortOrder.ASCENDING);
            if(Objects.nonNull(relatedObjects) && relatedObjects.size() > 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前目录或其子目录下仍存在关联关系，请取消全部关联后删除目录");
            }
            deleteChildrenRelatedTerms.add(termGuid);
        }
    }

    @CacheEvict(value = {"relationCache"}, allEntries = true)
    public Set<AtlasRelatedObjectId> assignTermToEntities(String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.assignTermToEntities({}, {})", categoryGuid, relatedObjectIds);
        }

        AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);
        Set<AtlasRelatedTermHeader> terms = category.getTerms();
        AtlasGlossaryTerm glossaryTerm = null;
        if(Objects.isNull(terms) || terms.size()==0) {
            //根据categoryGuid获取Category
            //AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
            String categoryName = category.getName();
            //根据Category获取GlossaryHeader
            AtlasGlossaryHeader glossaryHeader = category.getAnchor();
            //获取Glossary得到Name
            AtlasGlossary glossary = glossaryService.getGlossary(glossaryHeader.getGlossaryGuid());
            glossaryHeader.setDisplayText(glossary.getName());
            //创建Term
            glossaryTerm = new AtlasGlossaryTerm();
            String time = String.valueOf(System.currentTimeMillis());
            glossaryTerm.setName(categoryName + "-" + time + "-Term");
            glossaryTerm.setAnchor(glossaryHeader);
            glossaryTerm = glossaryService.createTerm(glossaryTerm);

            AtlasRelatedTermHeader termHeader = new AtlasRelatedTermHeader();
            termHeader.setTermGuid(glossaryTerm.getGuid());
            Set<AtlasRelatedTermHeader> termHeaderSet = new HashSet<>();
            termHeaderSet.add(termHeader);
            category.setTerms(termHeaderSet);
            //将Term与Category关联
            glossaryService.updateCategory(category);
        } else {
            glossaryTerm = glossaryService.getTerm(terms.iterator().next().getTermGuid());
        }

        //创建关联关系
        glossaryService.assignTermToEntities(glossaryTerm.getGuid(), relatedObjectIds);
        AtlasGlossaryTerm ret = glossaryService.getTerm(glossaryTerm.getGuid());

        return ret.getAssignedEntities();
    }

    @CacheEvict(value = {"relationCache"}, key = "#categoryGuid")
    public void removeRelationAssignmentFromEntities(String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.removeRelationAssignmentFromEntities({}, {})", categoryGuid, relatedObjectIds);
        }
        try {
            //获取Category信息
            AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
            Set<AtlasRelatedTermHeader> relatedTerms = glossaryCategory.getTerms();
            Iterator<AtlasRelatedTermHeader> iterator = relatedTerms.iterator();
            if (Objects.nonNull(iterator) && iterator.hasNext()) {
                String termGuid = iterator.next().getTermGuid();
                glossaryService.removeTermFromEntities(termGuid, relatedObjectIds);
            }
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "取消关联失败");
        }
    }

    @Cacheable(value = "relationCache", key = "#categoryGuid", condition = "#refreshCache==false")
    public RelationEntity getCategoryRelations(String categoryGuid, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getCategoryRelations({})", categoryGuid);
        }
        RelationEntity relationEntity = new RelationEntity();
        //获取Category信息
        //AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
        List<String> categoryAttributes = new ArrayList<>();
        List<String> categoryRelationshipAttributes = new ArrayList<>();
        categoryAttributes.add("name");
        categoryRelationshipAttributes.add("parentCategory");
        categoryRelationshipAttributes.add("terms");
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid, categoryAttributes, categoryRelationshipAttributes);
        relationEntity.setCategoryGuid(categoryGuid);
        relationEntity.setCategoryName(glossaryCategory.getName());
        //获取与Category关联Term
        Set<AtlasRelatedTermHeader> relatedTerms = glossaryCategory.getTerms();
        //迭代获取父级目录信息
        AtlasRelatedCategoryHeader parent = glossaryCategory.getParentCategory();
        List<String> pathList = new ArrayList<>();
        pathList.add(glossaryCategory.getName());
        while(Objects.nonNull(parent)) {

            AtlasGlossaryCategory parentCategory = glossaryService.getCategory(parent.getCategoryGuid(),categoryAttributes, categoryRelationshipAttributes);
            parent = parentCategory.getParentCategory();
            pathList.add(parentCategory.getName());
        }
        //拼接路径
        String pathStr = "";
        for(int i=pathList.size()-1; i>=0; i--) {
            pathStr += pathList.get(i) + "/";
        }

        if(Objects.nonNull(relatedTerms) && relatedTerms.size()!=0) {
            Iterator<AtlasRelatedTermHeader> iterator = relatedTerms.iterator();
            if (iterator.hasNext()) {
                AtlasRelatedTermHeader term = iterator.next();
                String termGuid = term.getTermGuid();
                AtlasGlossaryTerm ret = glossaryService.getTerm(termGuid);
                //获取Term下的关联
                Set<AtlasRelatedObjectId> relatedObjectIds = ret.getAssignedEntities();
                if (Objects.nonNull(relatedObjectIds) && relatedObjectIds.size() != 0) {
                    Iterator<AtlasRelatedObjectId> relatedIterator = relatedObjectIds.iterator();
                    Set<RelationEntity.RelationInfo> relationInfos = new HashSet<>();
                    while (relatedIterator.hasNext()) {
                        AtlasRelatedObjectId relatedObject = relatedIterator.next();
                        RelationEntity.RelationInfo relationInfo = new RelationEntity.RelationInfo();
                        relationInfo.setCategoryGuid(categoryGuid);
                        String relatedObjectGuid = relatedObject.getGuid();
                        //获取entity
                        List<String> attributes = new ArrayList<>();
                        attributes.add("name");
                        List<String> relationshipAttributes = new ArrayList<>();
                        relationshipAttributes.add("db");
                        AtlasEntity entity = entitiesStore.getByIdWithAttributes(relatedObjectGuid, attributes, relationshipAttributes).getEntity();
                        String status = entity.getStatus().name();
                        //AtlasEntity entity = getEntityById(relatedObjectGuid);
                        //表名称
                        String tableName = getEntityAttribute(entity, "name");

                        AtlasRelatedObjectId relatedDB = getRelatedDB(entity);
                        String dbName = relatedDB.getDisplayText();
                        //数据库名
                        relationInfo.setGuid(relatedObjectGuid);
                        relationInfo.setTableName(tableName);
                        relationInfo.setDbName(dbName);
                        relationInfo.setPath(pathStr + tableName);
                        relationInfo.setStatus(status);
                        relationInfo.setRelationshipGuid(relatedObject.getRelationshipGuid());
                        relationInfos.add(relationInfo);
                    }
                    relationEntity.setRelations(relationInfos);
                }
            }
        }
        return relationEntity;
    }

    public CategoryChildren getCategoryChildren(String categoryGuid) throws AtlasBaseException {
        CategoryChildren children = new CategoryChildren();
        //获取Category信息
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
        children.setCategoryGuid(glossaryCategory.getGuid());
        children.setCategoryName(glossaryCategory.getName());
        //获取当前Category的子Category
        Set<AtlasRelatedCategoryHeader> childrenCategories =glossaryCategory.getChildrenCategories();
        Set<CategoryChildren.ChildCatetory> childs = new HashSet<>();
        if(Objects.nonNull(childrenCategories)) {
            Iterator<AtlasRelatedCategoryHeader> it = childrenCategories.iterator();
            while(it.hasNext()) {
                AtlasRelatedCategoryHeader atlasRelatedCategory = it.next();
                CategoryChildren.ChildCatetory childCatetory = new CategoryChildren.ChildCatetory();
                childCatetory.setGuid(atlasRelatedCategory.getCategoryGuid());
                childCatetory.setName(atlasRelatedCategory.getDisplayText());
                childs.add(childCatetory);
            }
        }
        children.setChildCategory(childs);
        return children;
    }

    @Cacheable(value = "categoryCache", condition = "#refreshCache==false")
    public Set<CategoryHeader> getCategories(String sort, Boolean refreshCache) throws AtlasBaseException {
        try {
            Set<CategoryHeader> categoryHeaders = new HashSet<CategoryHeader>();
            List<AtlasGlossary> glossaries = glossaryService.getGlossaries(1, 0, toSortOrder(sort));
            if(Objects.nonNull(glossaries) && glossaries.size()!=0) {
                AtlasGlossary baseGlosary = glossaries.get(0);
                Set<AtlasRelatedCategoryHeader> categories = baseGlosary.getCategories();
                Iterator<AtlasRelatedCategoryHeader> iterator = categories.iterator();
                while(iterator.hasNext()) {
                    AtlasRelatedCategoryHeader header = iterator.next();
                    CategoryHeader categoryHeader = new CategoryHeader();
                    categoryHeader.setCategoryGuid(header.getCategoryGuid());
                    categoryHeader.setName(header.getDisplayText());
                    categoryHeader.setRelationGuid(header.getRelationGuid());
                    if(Objects.nonNull(header.getParentCategoryGuid()))
                        categoryHeader.setParentCategoryGuid(header.getParentCategoryGuid());
                    AtlasGlossaryCategory category = glossaryService.getCategory(categoryHeader.getCategoryGuid());
                    if(Objects.nonNull(category.getLongDescription()))
                        categoryHeader.setDescription(category.getLongDescription());
                    categoryHeaders.add(categoryHeader);
                }
            }
            return categoryHeaders;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取目录失败");
        }

    }

    @CacheEvict(value = "tableCache", key = "#tableEdit.guid")
    public void updateTableDescription(TableEdit tableEdit) throws AtlasBaseException {
        String guid = tableEdit.getGuid();
        String description = tableEdit.getDescription();
        if(Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "提交修改信息有误");
        }
        try {
            AtlasEntity entity = getEntityById(guid);
            String tableName = getEntityAttribute(entity, "name");
            AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
            String dbName = relatedObject.getDisplayText();
            String sql = String.format("alter table %s set tblproperties('comment'='%s')", tableName, description);
            HiveJdbcUtils.execute(sql, dbName);
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改表信息失败");
        }

    }

    @CacheEvict(value = "columnCache", allEntries = true)
    public void updateColumnDescription(List<ColumnEdit> columnEdits) throws AtlasBaseException {
        if(Objects.isNull(columnEdits))
            throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "提交修改信息有误");
        try {
            for(int i=0; i<columnEdits.size(); i++) {
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

    public List<RelationEntity.RelationInfo> getQueryTables(String tableName) throws AtlasBaseException {
        List<RelationEntity.RelationInfo> relationInfoList = new ArrayList<>();
        List<AtlasGlossary> glossaries = glossaryService.getGlossaries(-1,0, SortOrder.ASCENDING);
        if(Objects.nonNull(glossaries) && glossaries.size()!=0) {
            AtlasGlossary glossary = glossaries.get(0);
            Set<AtlasRelatedTermHeader> terms = glossary.getTerms();
            if(Objects.nonNull(terms) && terms.size()!=0) {
                Iterator<AtlasRelatedTermHeader> iterator = terms.iterator();
                while (iterator.hasNext()) {
                    AtlasRelatedTermHeader termHeader = iterator.next();
                    String termGuid = termHeader.getTermGuid();
                    AtlasGlossaryTerm term = glossaryService.getTerm(termGuid);
                    Set<AtlasRelatedObjectId> relatedObjectIds = term.getAssignedEntities();
                    if(Objects.nonNull(relatedObjectIds) && relatedObjectIds.size()!=0) {
                        Iterator<AtlasRelatedObjectId> relatedIterator = relatedObjectIds.iterator();
                        while(relatedIterator.hasNext()) {
                            AtlasRelatedObjectId object = relatedIterator.next();
                            String name  = object.getDisplayText();
                            AtlasEntity.Status status = object.getEntityStatus();
                            if(name.contains(tableName)) {
                                RelationEntity.RelationInfo info = new RelationEntity.RelationInfo();
                                info.setGuid(object.getGuid());
                                info.setRelationshipGuid(object.getRelationshipGuid());
                                info.setStatus(status.name());
                                AtlasEntity entity = getEntityById(object.getGuid());
                                //表名称
                                info.setTableName(getEntityAttribute(entity, "name"));
                                //库名称
                                AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
                                info.setDbName(relatedObject.getDisplayText());
                                Set<AtlasTermCategorizationHeader> categoriesHeader  = term.getCategories();
                                if(Objects.nonNull(categoriesHeader) && categoriesHeader.size()!=0) {
                                    AtlasTermCategorizationHeader categoryHeader = categoriesHeader.iterator().next();
                                    String  categoryGuid = categoryHeader.getCategoryGuid();
                                    info.setCategoryGuid(categoryGuid);
                                    AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);

                                    //迭代获取父级目录信息
                                    AtlasRelatedCategoryHeader parent = category.getParentCategory();
                                    List<String> pathList = new ArrayList<>();
                                    pathList.add(category.getName());
                                    while(Objects.nonNull(parent)) {
                                        AtlasGlossaryCategory parentCategory = glossaryService.getCategory(parent.getCategoryGuid());
                                        parent = parentCategory.getParentCategory();
                                        pathList.add(parentCategory.getName());
                                    }
                                    //拼接路径
                                    String pathStr = "";
                                    for(int i=pathList.size()-1; i>=0; i--) {
                                        pathStr += pathList.get(i) + "/";
                                    }
                                    pathStr += name;
                                    info.setPath(pathStr);
                                }
                                if(Objects.nonNull(info.getCategoryGuid()))
                                    relationInfoList.add(info);
                            }
                        }
                    }
                }
            }
        }
        return relationInfoList;
    }

    private SortOrder toSortOrder(final String sort) {
        SortOrder ret = SortOrder.ASCENDING;
        if (!"ASC".equals(sort)) {
            if ("DESC".equals(sort)) {
                ret = SortOrder.DESCENDING;
            }
        }
        return ret;
    }

    @CacheEvict(value = {"tableCache", "columnCache", "relationCache", "categoryCache", "tableRelationCache",
                         "databaseCache", "tablePageCache", "columnPageCache"}, allEntries = true)
    public void refreshCache() throws AtlasBaseException {

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

        public UploadPreview previewUpload(String jobId, int size) {
            String filePath = getPath(jobId);
            try {
                return previewExcelForXLSX(jobId, null, size);
            } catch (OLE2NotOfficeXmlFileException e) {

            } catch (NotOfficeXmlFileException e) {

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Workbook workbook = ExcelUtils.isExcelFile(filePath);
            if (workbook != null) {
                Iterator<Sheet> iterator = workbook.iterator();
                if (!iterator.hasNext()) {
                    throw new VerifyException("至少有一个sheet！");
                }
                List<String> emptySheet = new ArrayList<>();
                UploadPreview preview = null;
                while (iterator.hasNext()) {
                    Sheet sheet = iterator.next();
                    try {
                        UploadPreview tempResult = previewExcel(workbook, null, size, sheet.getSheetName(), true);
                        if (preview == null) {
                            preview = tempResult;
                        }
                    } catch (VerifyException e) {
                        LOGGER.warn("previewExcel sheet[{}]: empty", sheet.getSheetName(), e);
                        emptySheet.add(sheet.getSheetName());
                    }
                }
                if (preview == null) {
                    throw new VerifyException("至少有一行数据！");
                }
                if (!emptySheet.isEmpty()) {
                    preview.getSheets().removeAll(emptySheet);
                    StringBuffer stringBuffer = new StringBuffer("Excel文件中" + emptySheet.size() + "个sheet: ");
                    for (String s : emptySheet) {
                        stringBuffer.append(s + ",");
                    }
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                    stringBuffer.append(" 没有数据，忽略");
                    preview.setPreviewInfo(stringBuffer.toString());
                }
                return preview;
            }
            try {
                String fileCode = FileUtils.fileCode(filePath);
                if (fileCode == null) {
                    fileCode = "UTF8";
                }
                CsvEncode csvEncode = CsvEncode.of(fileCode);
                String delimiter = CsvUtils.detectDelimiter(filePath, csvEncode.name());
                final boolean includeHeader = true;
                final CsvHeader csvHeader = CsvUtils.detectCsvHeader(filePath, fileCode, delimiter, includeHeader);
                return previewUpload(jobId, fileCode, delimiter, includeHeader, csvHeader, size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    public PageResult<Database> getAllDBAndTable(Parameters parameters) throws AtlasBaseException {
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        return metaspaceLineageService.getAllDBAndTable(limit, offset);
    }
}
