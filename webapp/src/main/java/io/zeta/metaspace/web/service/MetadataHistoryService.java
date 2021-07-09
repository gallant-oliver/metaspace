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
 * @date 2019/9/19 11:18
 */
package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.ColumnMetadata;
import io.zeta.metaspace.model.metadata.TableMetadata;
import io.zeta.metaspace.web.dao.MetadataHistoryDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 11:18
 */

@Service
@Slf4j
public class MetadataHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataHistoryService.class);

    @Autowired
    private MetadataHistoryDAO metadataDAO;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private AtlasEntityStoreV2 entityStore;
    private String partitionAttribute = "partitionKeys";

    public Set<String> getTableGuid(List<AtlasEntity> entities) {
        Set<String> tableSet = new HashSet<>();
        Boolean hiveAtlasEntityAll = dataManageService.getHiveAtlasEntityAll(entities);
        for (AtlasEntity entity : entities) {
            if (dataManageService.getOutputFromProcesses(entity) && hiveAtlasEntityAll) {
                continue;
            }
            String typeName = entity.getTypeName();
            if("hive_table".equals(typeName)) {
                tableSet.add(entity.getGuid());
            } else if("hive_column".equals(typeName) || "hive_storagedesc".equals(typeName)) {
                AtlasRelatedObjectId table = (AtlasRelatedObjectId)entity.getRelationshipAttribute("table");
                if(null != table) {
                    tableSet.add(table.getGuid());
                }
            }
        }
        log.info("tableSet is {}", tableSet);
        return tableSet;
    }

    @Transactional(rollbackFor=Exception.class)
    public void storeHistoryMetadata(List<AtlasEntity> entities) throws AtlasBaseException {
        try {
            Set<String> tableSet = getTableGuid(entities);

            for(String tableGuid : tableSet) {
                AtlasEntity.AtlasEntityWithExtInfo info = entityStore.getById(tableGuid);
                if (null != info) {
                    AtlasEntity entity = info.getEntity();
                    List<String> partitionKeyList = extractPartitionKeyInfo(entity);
                    TableMetadata tableMetadata = generateTableMetadata(entity);
                    log.info("storeHistoryMetadata AtlasEntity is {},name is {}", entity, tableMetadata.getName());
                    int sameCount = metadataDAO.getSameUpdateEntityCount(tableMetadata);
                    if(sameCount > 0) {
                        return;
                    }
                    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                    Map<String, AtlasEntity> referrencedEntities = info.getReferredEntities();
                    for (String guid : referrencedEntities.keySet()) {
                        AtlasEntity referrencedEntity = referrencedEntities.get(guid);
                        String typeName = referrencedEntity.getTypeName();
                        if ("hive_column".equals(typeName) && AtlasEntity.Status.ACTIVE == referrencedEntity.getStatus()) {
                            ColumnMetadata columnMetadata = generateColumnMetadata(tableGuid, referrencedEntity, partitionKeyList);
                            columnMetadataList.add(columnMetadata);
                        } else if ("hive_storagedesc".equals(typeName)) {
                            String location = getEntityAttribute(referrencedEntity, "location");
                            tableMetadata.setStoreLocation(location);
                            //格式
                            String inputFormat = getEntityAttribute(referrencedEntity, "inputFormat");
                            if (Objects.nonNull(inputFormat)) {
                                String[] fullFormat = inputFormat.split("\\.");
                                tableMetadata.setTableFormat(fullFormat[fullFormat.length - 1]);
                            }
                        }
                    }
                    if (this.getTableCompareResult(tableGuid, tableMetadata) && this.getColumnCompareResult(tableGuid, columnMetadataList)) {
                        return;
                    }
                    metadataDAO.addTableMetadata(tableMetadata);
                    int version = metadataDAO.getTableVersion(tableGuid);
                    columnMetadataList.forEach(columnMetadata -> columnMetadata.setVersion(version));
                    if (columnMetadataList.size() > 0) {
                        metadataDAO.addColumnMetadata(columnMetadataList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("storeHistoryMetadata exception is {}", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 校验字段和上一版本是否完全一致
     * @param tableGuid
     * @param columnMetadataList
     * @return
     */
    private Boolean getColumnCompareResult(String tableGuid, List<ColumnMetadata> columnMetadataList) {
        List<ColumnMetadata> columnMetadataListHistory = metadataDAO.getLastColumnMetadata(tableGuid);
        if (CollectionUtils.isEmpty(columnMetadataListHistory)) {
            return false;
        }
        if (columnMetadataList.size() != columnMetadataListHistory.size()) {
            return false;
        }
        int i = 0;
        for (ColumnMetadata columnMetadata : columnMetadataListHistory) {
            for (ColumnMetadata metadata : columnMetadataList) {
                if (columnMetadata.compareColumn(metadata)) {
                    i++;
                }
            }
        }
        if (columnMetadataList.size() == i) {
            return true;
        }
        return false;
    }

    /**
     * 比较表内容是否一致
     * @param tableGuid
     * @param tableMetadata
     * @return
     */
    private Boolean getTableCompareResult(String tableGuid, TableMetadata tableMetadata) {
        TableMetadata tableMetadataListHistory = metadataDAO.getLastTableMetadata(tableGuid);
        if (tableMetadataListHistory == null) {
            return false;
        }
        return tableMetadataListHistory.compareTable(tableMetadata);
    }


    public TableMetadata generateTableMetadata(AtlasEntity entity) {
        String guid = entity.getGuid();
        String name = getEntityAttribute(entity, "name");
        AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
        String dbName = relatedObject.getDisplayText();
        String creator = entity.getCreatedBy();
        String updater = entity.getUpdatedBy();
        Timestamp createTime = new Timestamp(entity.getCreateTime().getTime());
        Timestamp updateTime = new Timestamp(entity.getUpdateTime().getTime());
        String tableType = getEntityAttribute(entity, "tableType");
        tableType = tableType.contains("EXTERNAL")?"EXTERNAL_TABLE":"INTERNAL_TABLE";
        Boolean isPartitionTable = extractPartitionInfo(entity);
        String tableFormat = "";
        String storeLocation = "";
        String description = getEntityAttribute(entity, "description");
        String status = entity.getStatus().name();

        TableMetadata metadata = new TableMetadata(guid,dbName,name,creator,updater, createTime, updateTime,tableType,isPartitionTable,tableFormat,storeLocation,description,status);



        return metadata;
    }

    public ColumnMetadata generateColumnMetadata(String tableGuid, AtlasEntity entity, List<String> partitionKeys) {
        String guid = entity.getGuid();
        String name = getEntityAttribute(entity, "name");
        String type = getEntityAttribute(entity, "type");
        String description = getEntityAttribute(entity, "commnet");
        Boolean isPartitionKey = partitionKeys.contains(guid)?true:false;
        String status = entity.getStatus().name();
        String creator = entity.getCreatedBy();
        String updater = entity.getUpdatedBy();
        Timestamp createTime = new Timestamp(entity.getCreateTime().getTime());
        Timestamp updateTime = new Timestamp(entity.getUpdateTime().getTime());

        ColumnMetadata metadata = new ColumnMetadata(guid, name, type, tableGuid, description, status, isPartitionKey, creator, updater, createTime, updateTime);
        return metadata;
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if (entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public List<String> extractPartitionKeyInfo(AtlasEntity entity) {
        List<AtlasObjectId> partitionKeys = null;
        if (Objects.nonNull(entity.getAttribute(partitionAttribute))) {
            Object partitionObjects = entity.getAttribute(partitionAttribute);
            if (partitionObjects instanceof ArrayList<?>) {
                partitionKeys = (ArrayList<AtlasObjectId>) partitionObjects;
            }
        }
        List<String> guidList = new ArrayList<>();
        for (AtlasObjectId partitionKey : partitionKeys) {
            guidList.add(partitionKey.getGuid());
        }
        return guidList;
    }

    public boolean extractPartitionInfo(AtlasEntity entity) {
        if (entity.hasAttribute(partitionAttribute) && Objects.nonNull(entity.getAttribute(partitionAttribute))) {
            return true;
        } else {
            return false;
        }
    }

    public AtlasRelatedObjectId getRelatedDB(AtlasEntity entity) {
        AtlasRelatedObjectId objectId = null;
        String db = "db";
        if (entity.hasRelationshipAttribute(db) && Objects.nonNull(entity.getRelationshipAttribute(db))) {
            Object obj = entity.getRelationshipAttribute(db);
            if (obj instanceof AtlasRelatedObjectId) {
                objectId = (AtlasRelatedObjectId) obj;
            }
        }
        return objectId;
    }
}
