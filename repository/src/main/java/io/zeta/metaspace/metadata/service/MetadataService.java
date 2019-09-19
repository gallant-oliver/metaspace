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
package io.zeta.metaspace.metadata.service;

import io.zeta.metaspace.metadata.dao.MetadataDAO;
import io.zeta.metaspace.metadata.model.TableMetadata;
import io.zeta.metaspace.model.metadata.Table;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Objects;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 11:18
 */
public class MetadataService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataService.class);

    @Autowired
    private MetadataDAO metadataDAO;

    public void addTableMetadataHistory(AtlasEntity entity) {
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

        metadataDAO.addTableMetadata(metadata);
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
