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
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.AtlasLineageService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.glossary.GlossaryService;
import org.apache.atlas.model.glossary.AtlasGlossary;
import org.apache.atlas.model.glossary.AtlasGlossaryCategory;
import org.apache.atlas.model.glossary.AtlasGlossaryTerm;
import org.apache.atlas.model.glossary.enums.AtlasTermRelationshipStatus;
import org.apache.atlas.model.glossary.relations.AtlasGlossaryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedCategoryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedTermHeader;
import org.apache.atlas.model.glossary.relations.AtlasTermCategorizationHeader;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.lineage.AtlasLineageInfo;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.PathParam;


/*
 * @description
 * @author sunhaoning
 * @date 2018/10/25 15:11
 */
@AtlasService
public class MetaDataService {
    private static final Logger LOG  = LoggerFactory.getLogger(MetaDataService.class);

    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    private AtlasLineageService atlasLineageService;
    @Autowired
    private  GlossaryService glossaryService;

    public Table getTableInfoById(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableInfoById({})", guid);
        }

        if (Objects.isNull(guid)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "TableGuid is null/empty");
        }

        Table table  = new Table();
        table.setTableId(guid);
        //获取entity
        AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
        AtlasEntity entity = info.getEntity();
        if(entity.getTypeName().contains("table")) {
            //表名称
            if(entity.hasAttribute("name") && Objects.nonNull(entity.getAttribute("name"))) {
                table.setTableName(entity.getAttribute("name").toString());
            } else {
                table.setTableName("");
            }
            //创建人
            if(entity.hasAttribute("owner") && Objects.nonNull(entity.getAttribute("owner"))) {
                table.setOwner(entity.getAttribute("owner").toString());
            } else {
                table.setOwner("");
            }
            //创建时间
            if(entity.hasAttribute("createTime") && Objects.nonNull(entity.getAttribute("createTime"))) {
                SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formatDateStr = sdf.format(entity.getAttribute("createTime"));
                table.setCreateTime(formatDateStr);
            }
            //描述
            if(entity.hasAttribute("comment") && Objects.nonNull(entity.getAttribute("comment"))) {
                table.setDescription(entity.getAttribute("comment").toString());
            }

            if(entity.hasAttribute("sd") && Objects.nonNull(entity.getAttribute("sd"))) {
                Object obj = entity.getAttribute("sd");
                if(obj instanceof AtlasObjectId) {
                    AtlasObjectId atlasObject = (AtlasObjectId)obj;
                    String sdGuid = atlasObject.getGuid();
                    AtlasEntity sdEntity = entitiesStore.getById(sdGuid).getEntity();
                    //位置
                    if(sdEntity.hasAttribute("location") && Objects.nonNull(sdEntity.getAttribute("location"))) {
                        table.setLocation(sdEntity.getAttribute("location").toString());
                    }
                    //格式
                    if(sdEntity.hasAttribute("inputFormat") && Objects.nonNull(sdEntity.getAttribute("inputFormat"))) {
                        String inputFormat = sdEntity.getAttribute("inputFormat").toString();
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
            }
            //类型
            if(entity.hasAttribute("tableType") && Objects.nonNull(entity.getAttribute("tableType"))) {
                if(entity.getAttribute("tableType").toString().contains("EXTERNAL")) {
                    table.setType("EXTERNAL_TABLE");
                } else {
                    table.setType("INTERNAL_TABLE");
                }
            }
            //是否为分区表
            if(entity.hasAttribute("partitionKeys") && Objects.nonNull(entity.getAttribute("partitionKeys"))) {
                table.setPartitionTable(true);
            } else {
                table.setPartitionTable(false);
            }
            //数据库名
            if(entity.hasRelationshipAttribute("db") && Objects.nonNull(entity.getRelationshipAttribute("db"))) {
                Object obj = entity.getRelationshipAttribute("db");
                if(obj instanceof AtlasRelatedObjectId) {
                    AtlasRelatedObjectId relatedObject = (AtlasRelatedObjectId)obj;
                    table.setDatabaseId(relatedObject.getGuid());
                    table.setDatabaseName(relatedObject.getDisplayText());
                }
            }
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
        List<Column> columns = getColumnInfoById(columnQuery);

        table.setColumns(columns);
        return table;
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

    public List<Column> getColumnInfoById(ColumnQuery query) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getColumnInfoById({})", query);
        }
        String guid = query.getGuid();
        List<Column> columns = new ArrayList<>();
        Column column = null;
        //获取entity
        AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guid);
        AtlasEntity entity = info.getEntity();

        //获取PartitionKey的guid
        List<AtlasObjectId> partitionKeys = null;
        if(Objects.nonNull(entity.getAttribute("partitionKeys"))) {
            Object partitionObjects = entity.getAttribute("partitionKeys");
            if(partitionObjects instanceof ArrayList<?>) {
                partitionKeys = (ArrayList<AtlasObjectId>)partitionObjects;
            }
        }
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        for(String key: referredEntities.keySet()) {
            column = new Column();
            //tableId
            column.setTableId(guid);
            //tableName
            if(entity.hasAttribute("name") && Objects.nonNull(entity.getAttribute("name")))
                column.setTableName(entity.getAttribute("name").toString());

            //databaseId && dataBaseName
            if(entity.hasRelationshipAttribute("db") && Objects.nonNull(entity.getRelationshipAttribute("db"))) {
                Object relAttribute = entity.getRelationshipAttribute("db");
                if(relAttribute instanceof AtlasRelatedObjectId) {
                    AtlasRelatedObjectId relObject = (AtlasRelatedObjectId)relAttribute;
                    column.setDatabaseId(relObject.getGuid());
                    column.setDatabaseName(relObject.getDisplayText());
                }
            }
            AtlasEntity referredEntity = referredEntities.get(key);
            if(referredEntity.getTypeName().contains("column")) {
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
    }

    public LineageInfo getTableLineage(String guid, AtlasLineageInfo.LineageDirection direction,
                                       int depth) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getTableLineage({}, {}, {})", guid, direction, depth);
        }

        AtlasLineageInfo lineageInfo = atlasLineageService.getAtlasLineageInfo(guid, direction, depth);
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
    }

    public LineageInfo.LineageEntity getLineageInfo(String guid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getLineageInfo({})", guid);
        }

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
            AtlasEntity atlasTableEntity = entitiesStore.getById(guid).getEntity();
            if(atlasTableEntity.hasAttribute("name")) {
                lineageEntity.setTableName(atlasTableEntity.getAttribute("name").toString());
            }
            lineageEntity.setTypeName(atlasTableEntity.getTypeName());
            //updateTime
            SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatDateStr = sdf.format(atlasTableEntity.getUpdateTime());
            lineageEntity.setTableUpdateTime(formatDateStr);
            //dbName
            if(atlasTableEntity.hasRelationshipAttribute("db") && atlasTableEntity.getRelationshipAttribute("db") != null) {
                Object obj = atlasTableEntity.getRelationshipAttribute("db");
                if(obj instanceof AtlasRelatedObjectId) {
                    AtlasRelatedObjectId relatedObject = (AtlasRelatedObjectId)obj;
                    lineageEntity.setDbName(relatedObject.getDisplayText());
                }
            }
        }
        return lineageEntity;
    }

    public LineageInfo.LineageEntity getEntityInfo(String guid, LineageInfo.LineageEntity lineageEntity, Map<String, AtlasEntityHeader> entities, AtlasEntityHeader atlasEntity) throws AtlasBaseException{
        //guid
        if(Objects.nonNull(atlasEntity.getGuid()))
            lineageEntity.setGuid(atlasEntity.getGuid());
        //typeName
        if(Objects.nonNull(atlasEntity.getTypeName()))
            lineageEntity.setTypeName(atlasEntity.getTypeName());
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
        if(atlasTableEntity.hasRelationshipAttribute("db") && Objects.nonNull(atlasTableEntity.getRelationshipAttribute("db"))) {
            Object obj = atlasTableEntity.getRelationshipAttribute("db");
            if(obj instanceof AtlasRelatedObjectId) {
                AtlasRelatedObjectId relatedObject = (AtlasRelatedObjectId)obj;
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
        return category;
    }

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
        glossaryCategory = glossaryService.updateCategory(glossaryCategory);

        if(Objects.nonNull(glossaryCategory.getAnchor()))
            category.setAnchor(glossaryCategory.getAnchor());
        if(Objects.nonNull(glossaryCategory.getParentCategory()))
            category.setParentCategory(glossaryCategory.getParentCategory());
        if(Objects.nonNull(glossaryCategory.getChildrenCategories()))
            category.setChildrenCategories(glossaryCategory.getChildrenCategories());
        String qualfiiedName = glossaryCategory.getQualifiedName().replaceFirst(historyName, category.getName());
        glossaryCategory.setQualifiedName(qualfiiedName);
        category.setQualifiedName(qualfiiedName);
        category.setQualifiedName(glossaryCategory.getQualifiedName());
        return category;
    }

    public void deleteGlossaryCategory(String categoryGuid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.deleteGlossaryCategory({})", categoryGuid);
        }
        //删除Category之前需先删除与之关联的Term
        List<AtlasRelatedTermHeader> terms = glossaryService.getCategoryTerms(categoryGuid, -1, 0, SortOrder.ASCENDING);
        for (AtlasRelatedTermHeader term : terms) {
            glossaryService.deleteTerm(term.getTermGuid());
        }
        AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);
        Set<AtlasRelatedCategoryHeader> childrenCategories = category.getChildrenCategories();
        if(Objects.nonNull(childrenCategories)) {
            Iterator<AtlasRelatedCategoryHeader> iterator = childrenCategories.iterator();
            while(iterator.hasNext()) {
                String chidGuid = iterator.next().getCategoryGuid();
                deleteGlossaryCategory(chidGuid);
            }
        }

        glossaryService.deleteCategory(categoryGuid);
    }

    public Set<AtlasRelatedObjectId> assignTermToEntities(String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.assignTermToEntities({}, {})", categoryGuid, relatedObjectIds);
        }

        AtlasGlossaryCategory category = glossaryService.getCategory(categoryGuid);
        Set<AtlasRelatedTermHeader> terms = category.getTerms();

        AtlasGlossaryTerm glossaryTerm = null;
        if(Objects.isNull(terms) || terms.size()==0) {
            //根据categoryGuid获取Category
            AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
            String categoryName = glossaryCategory.getName();
            //根据Category获取GlossaryHeader
            AtlasGlossaryHeader glossaryHeader = glossaryCategory.getAnchor();

            //获取Glossary得到Name
            AtlasGlossary glossary = glossaryService.getGlossary(glossaryHeader.getGlossaryGuid());
            glossaryHeader.setDisplayText(glossary.getName());

            //创建Term
            glossaryTerm = new AtlasGlossaryTerm();
            glossaryTerm.setName(categoryName + "-Term");
            glossaryTerm.setAnchor(glossaryHeader);
            glossaryTerm = glossaryService.createTerm(glossaryTerm);

            AtlasRelatedTermHeader termHeader = new AtlasRelatedTermHeader();
            termHeader.setTermGuid(glossaryTerm.getGuid());
            Set<AtlasRelatedTermHeader> termHeaderSet = new HashSet<>();
            termHeaderSet.add(termHeader);
            glossaryCategory.setTerms(termHeaderSet);
            //将Term与Category关联
            glossaryService.updateCategory(glossaryCategory);
        } else {
            glossaryTerm = glossaryService.getTerm(terms.iterator().next().getTermGuid());
        }

        //创建关联关系
        glossaryService.assignTermToEntities(glossaryTerm.getGuid(), relatedObjectIds);
        AtlasGlossaryTerm ret = glossaryService.getTerm(glossaryTerm.getGuid());
        return ret.getAssignedEntities();
    }

    public void removeRelationAssignmentFromEntities(String categoryGuid, List<AtlasRelatedObjectId> relatedObjectIds) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.removeRelationAssignmentFromEntities({}, {})", categoryGuid, relatedObjectIds);
        }

        //获取Category信息
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
        Set<AtlasRelatedTermHeader> relatedTerms = glossaryCategory.getTerms();
        Iterator<AtlasRelatedTermHeader> iterator = relatedTerms.iterator();
        if(iterator.hasNext()) {
            String termGuid = iterator.next().getTermGuid();
            glossaryService.removeTermFromEntities(termGuid, relatedObjectIds);
        }

    }

    public RelationEntity getCategoryRelations(String categoryGuid) throws AtlasBaseException {
        if (DEBUG_ENABLED) {
            LOG.debug("==> MetaDataService.getCategoryRelations({})", categoryGuid);
        }
        RelationEntity relationEntity = new RelationEntity();
        //获取Category信息
        AtlasGlossaryCategory glossaryCategory = glossaryService.getCategory(categoryGuid);
        relationEntity.setCategoryGuid(glossaryCategory.getGuid());
        relationEntity.setCategoryName(glossaryCategory.getName());
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
        //添加子目录信息
        relationEntity.setChildCategory(childs);
        //获取与Category关联Term
        Set<AtlasRelatedTermHeader> relatedTerms = glossaryCategory.getTerms();
        //迭代获取父级目录信息
        AtlasRelatedCategoryHeader parent = glossaryCategory.getParentCategory();
        List<String> pathList = new ArrayList<>();
        pathList.add(glossaryCategory.getName());
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
                        Table table = getTableInfoById(relatedObjectGuid);
                        String tableName = table.getTableName();
                        String dbName = table.getDatabaseName();
                        relationInfo.setGuid(relatedObjectGuid);
                        relationInfo.setTableName(tableName);
                        relationInfo.setDbName(dbName);
                        relationInfo.setPath(pathStr + tableName);
                        relationInfo.setRealationGuid(relatedObject.getRelationshipGuid());
                        relationInfos.add(relationInfo);
                    }
                    relationEntity.setRelations(relationInfos);
                }
            }
        }
        return relationEntity;
    }

    public Set<CategoryHeader> getCategories(String sort) throws AtlasBaseException {
        Set<CategoryHeader> categoryHeaders = new HashSet<CategoryHeader>();
        List<AtlasGlossary> glossaries = glossaryService.getGlossaries(-1, 0, toSortOrder(sort));
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
    }

    public void updateTableDescription(TableEdit tableEdit) throws AtlasBaseException {
        String guid = tableEdit.getGuid();
        String description = tableEdit.getDescription();
        Table table = getTableInfoById(guid);
        String dbName = table.getDatabaseName();
        String tableName = table.getTableName();
        String sql = String.format("alter table %s set tblproperties('comment'='%s')", tableName, description);
        HiveJdbcUtils.execute(sql, dbName);
    }

    public void updateColumnDescription(List<ColumnEdit> columnEdits) throws AtlasBaseException {
        if(Objects.isNull(columnEdits))
            throw new AtlasBaseException(AtlasErrorCode.INVALID_PARAMETERS, "parameters is none");
        for(int i=0; i<columnEdits.size(); i++) {
            ColumnEdit columnEdit = columnEdits.get(i);
            Table table = getTableInfoById(columnEdit.getTableId());
            String tableName = table.getTableName();
            String dbName = table.getDatabaseName();
            String columnName = columnEdit.getColumnName();
            String type = columnEdit.getType();
            String description = columnEdit.getDescription();
            String sql = String.format("alter table %s change column %s %s %s comment '%s'", tableName, columnName, columnName, type, description);
            HiveJdbcUtils.execute(sql, dbName);
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
                            if(tableName.equals(name) && status.equals(AtlasEntity.Status.ACTIVE)) {
                                RelationEntity.RelationInfo info = new RelationEntity.RelationInfo();
                                info.setGuid(object.getGuid());
                                info.setRealationGuid(object.getRelationshipGuid());
                                AtlasEntity entity = entitiesStore.getById(object.getGuid()).getEntity();
                                //表名称
                                if(entity.hasAttribute("name") && Objects.nonNull(entity.getAttribute("name"))) {
                                    info.setTableName(entity.getAttribute("name").toString());
                                } else {
                                    info.setTableName("");
                                }
                                //库名称
                                if(entity.hasRelationshipAttribute("db") && Objects.nonNull(entity.getRelationshipAttribute("db"))) {
                                    Object relAttribute = entity.getRelationshipAttribute("db");
                                    if(relAttribute instanceof AtlasRelatedObjectId) {
                                        AtlasRelatedObjectId relObject = (AtlasRelatedObjectId)relAttribute;
                                        info.setDbName(relObject.getDisplayText());
                                    }
                                }

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
}
