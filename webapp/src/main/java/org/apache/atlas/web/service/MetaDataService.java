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
package org.apache.atlas.web.service;

import static org.apache.cassandra.utils.concurrent.Ref.DEBUG_ENABLED;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.SortOrder;
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
import org.apache.atlas.model.metadata.CategoryChildren;
import org.apache.atlas.model.metadata.CategoryEntity;
import org.apache.atlas.model.metadata.CategoryHeader;
import org.apache.atlas.model.metadata.Column;
import org.apache.atlas.model.metadata.ColumnEdit;
import org.apache.atlas.model.metadata.ColumnQuery;
import org.apache.atlas.model.metadata.LineageInfo;
import org.apache.atlas.model.metadata.RelationEntity;
import org.apache.atlas.model.metadata.Table;
import org.apache.atlas.model.metadata.TableEdit;
import org.apache.atlas.model.metadata.TablePermission;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
                        if(inputFormat.contains("TextInputFormat")) {
                            table.setFormat("TextFile");
                        } else if(inputFormat.contains("SequenceFileInputFormat")) {
                            table.setFormat("SequenceFile");
                        } else if(inputFormat.contains("RCFileInputFormat")) {
                            table.setFormat("RCFile");
                        } else if(inputFormat.contains("OrcInputFormat")) {
                            table.setFormat("ORCFile");
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
                if(referredEntity.getTypeName().contains("column")) {
                    column = new Column();
                    //tableId
                    column.setTableId(guid);
                    //tableName
                    column.setTableName(getEntityAttribute(entity, "name"));
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
                    columns = columns.stream().filter(col -> col.getColumnName().equals(filter.getColumnName())).collect(Collectors.toList());
                }
                if(Objects.nonNull(type) && !type.equals("")) {
                    columns = columns.stream().filter(col -> col.getType().equals(type)).collect(Collectors.toList());
                }
                if(Objects.nonNull(description) && !description.equals("")) {
                    columns = columns.stream().filter(col -> col.getDescription().equals(description)).collect(Collectors.toList());
                }
            }
            return columns;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }
    }

    @Cacheable(value = "lineageCache", key = "#guid", condition = "#refreshCache==false")
    public LineageInfo getTableLineage(String guid, AtlasLineageInfo.LineageDirection direction,
                                       int depth, Boolean refreshCache) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableLineage({}, {}, {})", guid, direction, depth);
        }
        try {
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, direction, depth);
            if(Objects.isNull(lineageInfo)) {
                throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "请求参数异常，获取表血缘关系失败");
            }
            LineageInfo info = new LineageInfo();
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            String lineageGuid = lineageInfo.getBaseEntityGuid();
            Set<AtlasLineageInfo.LineageRelation> relations = lineageInfo.getRelations();
            //guid
            info.setGuid(lineageGuid);
            //depth
            info.setLineageDepth(depth);
            //relations
            Iterator<AtlasLineageInfo.LineageRelation> it = relations.iterator();
            Set<LineageInfo.LineageRelation> lineageRelations = new HashSet<>();
            LineageInfo.LineageRelation  relation = null;
            while(it.hasNext()) {
                AtlasLineageInfo.LineageRelation atlasRelation = it.next();
                relation = new LineageInfo.LineageRelation();
                relation.setFromEntityId(atlasRelation.getFromEntityId());
                relation.setToEntityId(atlasRelation.getToEntityId());
                relation.setRelationshipId(atlasRelation.getRelationshipId());
                lineageRelations.add(relation);
            }
            //entities
            List<LineageInfo.LineageEntity> lineageEntities = new ArrayList<>();
            LineageInfo.LineageEntity lineageEntity = null;
            for(String key: entities.keySet()) {
                lineageEntity = new LineageInfo.LineageEntity();
                AtlasEntityHeader atlasEntity = entities.get(key);
                getEntityInfo(key, lineageEntity, entities, atlasEntity);
                lineageEntity.setDirectUpStreamNum(0);
                lineageEntity.setDirectDownStreamNum(0);
                lineageEntity.setUpStreamLevelNum(0);
                lineageEntity.setDownStreamLevelNum(0);
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

    //@Cacheable(value = "lineageDepthCache", key = "#guid")
    public LineageInfo.LineageEntity getLineageInfo(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getLineageInfo({})", guid);
        }
        try {
            LineageInfo.LineageEntity lineageEntity = new LineageInfo.LineageEntity();
            AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, 1);
            Map<String, AtlasEntityHeader> entities = lineageInfo.getGuidEntityMap();
            if(Objects.nonNull(entities) && entities.size()!=0) {
                AtlasEntityHeader atlasEntity = entities.get(guid);
                getEntityInfo(guid, lineageEntity, entities, atlasEntity);
                if (atlasEntity.getTypeName().contains("table")) {
                    AtlasLineageInfo fullLineageInfo = atlasLineageService.getAtlasLineageInfo(guid, AtlasLineageInfo.LineageDirection.BOTH, -1);
                    Set<AtlasLineageInfo.LineageRelation> fullRelations = fullLineageInfo.getRelations();
                    //直接上游表数量
                    long directUpStreamNum = getInDirectRelationNode(guid, fullRelations).size();
                    lineageEntity.setDirectUpStreamNum(directUpStreamNum);
                    //直接下游表数量
                    long directDownStreamNum = getOutDirectRelationNode(lineageEntity.getGuid(), fullRelations).size();
                    lineageEntity.setDirectDownStreamNum(directDownStreamNum);
                    //上游表层数
                    long upStreamLevelNum = getMaxDepth("in", lineageEntity.getGuid(), fullRelations);
                    lineageEntity.setUpStreamLevelNum((upStreamLevelNum - 1) / 2);
                    //下游表层数
                    long downStreamLevelNum = getMaxDepth("out", lineageEntity.getGuid(), fullRelations);
                    lineageEntity.setDownStreamLevelNum((downStreamLevelNum - 1) / 2);
                }
            } else {
                lineageEntity.setGuid(guid);
                AtlasEntity atlasTableEntity = getEntityById(guid);
                lineageEntity.setTableName(getEntityAttribute(atlasTableEntity, "name"));

                lineageEntity.setTypeName(atlasTableEntity.getTypeName());
                //updateTime
                SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatDateStr = sdf.format(atlasTableEntity.getUpdateTime());
                lineageEntity.setTableUpdateTime(formatDateStr);
                //dbName
                AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
                lineageEntity.setDbName(relatedObject.getDisplayText());
            }
            return lineageEntity;
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表血缘深度详情失败");
        }
    }

    public LineageInfo.LineageEntity getEntityInfo(String guid, LineageInfo.LineageEntity lineageEntity, Map<String, AtlasEntityHeader> entities, AtlasEntityHeader atlasEntity) throws AtlasBaseException{
        //guid
        if(Objects.nonNull(atlasEntity.getGuid()))
            lineageEntity.setGuid(atlasEntity.getGuid());
        lineageEntity.setProcess(false);
        //typeName
        if(Objects.nonNull(atlasEntity.getTypeName())) {
            lineageEntity.setTypeName(atlasEntity.getTypeName());
            if(atlasEntity.getTypeName().contains("process")) {
                lineageEntity.setProcess(true);
            }
        }
        //tableName
        if(atlasEntity.hasAttribute("name") && Objects.nonNull(atlasEntity.getAttribute("name")))
            lineageEntity.setTableName(atlasEntity.getAttribute("name").toString());
        //displayName
        if(Objects.nonNull(atlasEntity.getDisplayText())) {
            lineageEntity.setDisplayText(atlasEntity.getDisplayText());
        }
        AtlasEntity atlasTableEntity = entitiesStore.getById(guid).getEntity();
        //updateTime
        SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatDateStr = sdf.format(atlasTableEntity.getUpdateTime());
        lineageEntity.setTableUpdateTime(formatDateStr);
        //dbName
        AtlasRelatedObjectId relatedObject = getRelatedDB(atlasTableEntity);
        if(Objects.nonNull(relatedObject))
            lineageEntity.setDbName(relatedObject.getDisplayText());
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
        Set<AtlasLineageInfo.LineageRelation> directRelations = new HashSet<>();
        for(Iterator it = relations.iterator(); it.hasNext();) {
            AtlasLineageInfo.LineageRelation relation = (AtlasLineageInfo.LineageRelation)it.next();
            if(relation.getToEntityId().equals(guid)) {
                directRelations.add(relation);
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
        try {
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
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改目录失败");
        }
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
        AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);
        Set<AtlasRelatedCategoryHeader> childrenCategories = category.getChildrenCategories();
        if(Objects.nonNull(childrenCategories)) {
            Iterator<AtlasRelatedCategoryHeader> iterator = childrenCategories.iterator();
            while(iterator.hasNext()) {
                String chidGuid = iterator.next().getCategoryGuid();
                getCategoryChildrenTerms(chidGuid, deleteChildrenRelatedTerms);
            }
        }
    }

    @CacheEvict(value = {"relationCache", "tableRelationCache"}, allEntries = true)
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

    @CacheEvict(value = "relationCache", key = "#categoryGuid")
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除关联失败");
        }
    }

    @Cacheable(value = "relationCache", key = "#categoryGuid", condition = "#refreshCache==false")
    public RelationEntity getCategoryRelations(String categoryGuid,Boolean refreshCache) throws AtlasBaseException {
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
                        String relatedObjectGuid = relatedObject.getGuid();
                        //获取entity
                        List<String> attributes = new ArrayList<>();
                        attributes.add("name");
                        List<String> relationshipAttributes = new ArrayList<>();
                        relationshipAttributes.add("db");
                        AtlasEntity entity = entitiesStore.getByIdWithAttributes(relatedObjectGuid, attributes, relationshipAttributes).getEntity();

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
        Set<RelationEntity.ChildCatetory> childs = new HashSet<>();
        if(Objects.nonNull(childrenCategories)) {
            Iterator<AtlasRelatedCategoryHeader> it = childrenCategories.iterator();
            while(it.hasNext()) {
                AtlasRelatedCategoryHeader atlasRelatedCategory = it.next();
                RelationEntity.ChildCatetory childCatetory = new RelationEntity.ChildCatetory();
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

    @Cacheable(value = "tableRelationCache", key = "#tableName")
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
                            if(tableName.equals(name) && status.equals(AtlasEntity.Status.ACTIVE)) {
                                RelationEntity.RelationInfo info = new RelationEntity.RelationInfo();
                                info.setGuid(object.getGuid());
                                info.setRelationshipGuid(object.getRelationshipGuid());

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
                                    pathStr += tableName;
                                    info.setPath(pathStr);
                                }
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

    @CacheEvict(value = {"tableCache", "columnCache", "relationCache", "lineageCache", "categoryCache", "tableRelationCache",
                         "databaseCache", "tablePageCache", "columnPageCache"}, allEntries = true)
    public void refreshCache() throws AtlasBaseException {

    }
}
