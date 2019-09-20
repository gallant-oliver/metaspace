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

import io.zeta.metaspace.web.dao.MetadataHistoryDAO;
import io.zeta.metaspace.model.metadata.ColumnMetadata;
import io.zeta.metaspace.model.metadata.TableMetadata;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStoreV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 11:18
 */

@Service
public class MetadataHistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataHistoryService.class);

    @Autowired
    private MetadataHistoryDAO metadataDAO;
    @Autowired
    private AtlasEntityStoreV2 entityStore;

    public Set<String> getTableGuid(List<AtlasEntity> entities) {
        Set<String> tableSet = new HashSet<>();
        for (AtlasEntity entity : entities) {
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

        return tableSet;
    }

    public void storeHistoryMetadata(List<AtlasEntity> entities) throws AtlasBaseException {
        try {
            Set<String> tableSet = getTableGuid(entities);

            for(String tableGuid : tableSet) {
                AtlasEntity.AtlasEntityWithExtInfo info = entityStore.getById(tableGuid);
                if (null != info) {
                    AtlasEntity entity = info.getEntity();
                    TableMetadata tableMetadata = generateTableMetadata(entity);
                    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
                    Map<String, AtlasEntity> referrencedEntities = info.getReferredEntities();
                    for (String guid : referrencedEntities.keySet()) {
                        AtlasEntity referrencedEntity = referrencedEntities.get(guid);
                        String typeName = referrencedEntity.getTypeName();
                        if ("hive_column".equals(typeName)) {
                            ColumnMetadata columnMetadata = generateColumnMetadata(tableGuid, referrencedEntity);
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

                    metadataDAO.addTableMetadata(tableMetadata);
                    for (ColumnMetadata columnMetadata : columnMetadataList) {
                        metadataDAO.addColumnMetadata(columnMetadata);
                    }
                }
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public TableMetadata generateTableMetadata(AtlasEntity entity) {
        String guid = entity.getGuid();
        String name = getEntityAttribute(entity, "name");
        AtlasRelatedObjectId relatedObject = getRelatedDB(entity);
        String dbName = relatedObject.getDisplayText();
        String creator = entity.getCreatedBy();
        Timestamp updateTime = new Timestamp(entity.getUpdateTime().getTime());
        String tableType = getEntityAttribute(entity, "tableType");
        tableType = tableType.contains("EXTERNAL")?"EXTERNAL_TABLE":"INTERNAL_TABLE";
        Boolean isPartitionTable = extractPartitionInfo(entity);
        String tableFormat = "";
        String storeLocation = "";
        String description = getEntityAttribute(entity, "description");
        String status = entity.getStatus().name();

        TableMetadata metadata = new TableMetadata(guid,dbName,name,creator,updateTime,tableType,isPartitionTable,tableFormat,storeLocation,description,status);



        return metadata;
    }

    public ColumnMetadata generateColumnMetadata(String tableGuid, AtlasEntity entity) {
        String guid = entity.getGuid();
        String name = getEntityAttribute(entity, "name");
        String type = getEntityAttribute(entity, "type");
        String description = getEntityAttribute(entity, "commnet");
        String status = entity.getStatus().name();
        ColumnMetadata metadata = new ColumnMetadata(guid, name, type, tableGuid, description, status);
        return metadata;
    }

    public String getEntityAttribute(AtlasEntity entity, String attributeName) {
        if (entity.hasAttribute(attributeName) && Objects.nonNull(entity.getAttribute(attributeName))) {
            return entity.getAttribute(attributeName).toString();
        } else {
            return null;
        }
    }

    public boolean extractPartitionInfo(AtlasEntity entity) {
        if (entity.hasAttribute("partitionKeys") && Objects.nonNull(entity.getAttribute("partitionKeys"))) {
            return true;
        } else {
            return false;
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
}
