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
 * @date 2018/10/18 9:44
 */
package org.apache.atlas.model.metadata;

import java.io.Serializable;
import java.util.List;
import java.util.Set;


/*
 * @description
 * @author sunhaoning
 * @date 2018/10/18 9:44
 */
public class LineageInfo implements Serializable {

    private String guid;
    private Integer lineageDepth;
    private List<LineageEntity> guidEntityMap;
    private Set<LineageRelation> relations;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Integer getLineageDepth() {
        return lineageDepth;
    }

    public void setLineageDepth(Integer lineageDepth) {
        this.lineageDepth = lineageDepth;
    }

    public List<LineageEntity> getEntities() {
        return guidEntityMap;
    }

    public void setEntities(List<LineageEntity> guidEntityMap) {
        this.guidEntityMap = guidEntityMap;
    }

    public Set<LineageRelation> getRelations() {
        return relations;
    }

    public void setRelations(Set<LineageRelation> relations) {
        this.relations = relations;
    }

    public static class LineageEntity implements Serializable {

        private String typeName;
        private String guid;
        private String tableName;
        private String dbName;
        private long directUpStreamNum;
        private long directDownStreamNum;
        private long upStreamLevelNum;
        private long downStreamLevelNum;
        private String tableUpdateTime;
        private String displayText;
        private Boolean process;


        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public long getDirectUpStreamNum() {
            return directUpStreamNum;
        }

        public void setDirectUpStreamNum(long directUpStreamNum) {
            this.directUpStreamNum = directUpStreamNum;
        }

        public long getDirectDownStreamNum() {
            return directDownStreamNum;
        }

        public void setDirectDownStreamNum(long directDownStreamNum) {
            this.directDownStreamNum = directDownStreamNum;
        }

        public long getUpStreamLevelNum() {
            return upStreamLevelNum;
        }

        public void setUpStreamLevelNum(long upStreamLevelNum) {
            this.upStreamLevelNum = upStreamLevelNum;
        }

        public long getDownStreamLevelNum() {
            return downStreamLevelNum;
        }

        public void setDownStreamLevelNum(long downStreamLevelNum) {
            this.downStreamLevelNum = downStreamLevelNum;
        }

        public String getTableUpdateTime() {
            return tableUpdateTime;
        }

        public void setTableUpdateTime(String tableUpdateTime) {
            this.tableUpdateTime = tableUpdateTime;
        }

        public String getDisplayText() {
            return displayText;
        }

        public void setDisplayText(String displayText) {
            this.displayText = displayText;
        }

        public Boolean getProcess() {
            return process;
        }

        public void setProcess(Boolean process) {
            this.process = process;
        }
    }


    public static class LineageRelation implements Serializable {
        private String fromEntityId;
        private String toEntityId;
        private String relationshipId;

        public LineageRelation() { }

        public LineageRelation(String fromEntityId, String toEntityId, final String relationshipId) {
            this.fromEntityId = fromEntityId;
            this.toEntityId   = toEntityId;
            this.relationshipId = relationshipId;
        }

        public String getFromEntityId() {
            return fromEntityId;
        }

        public void setFromEntityId(String fromEntityId) {
            this.fromEntityId = fromEntityId;
        }

        public String getToEntityId() {
            return toEntityId;
        }

        public void setToEntityId(String toEntityId) {
            this.toEntityId = toEntityId;
        }

        public String getRelationshipId() {
            return relationshipId;
        }

        public void setRelationshipId(final String relationshipId) {
            this.relationshipId = relationshipId;
        }
    }



}
