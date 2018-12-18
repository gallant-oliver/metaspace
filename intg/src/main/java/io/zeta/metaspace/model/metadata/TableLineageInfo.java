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
package io.zeta.metaspace.model.metadata;

import java.io.Serializable;
import java.util.List;
import java.util.Set;


/*
 * @description
 * @author sunhaoning
 * @date 2018/10/18 9:44
 */
public class TableLineageInfo implements Serializable {

    private String guid;
    private List<LineageEntity> guidEntityMap;
    private Set<LineageTrace> relations;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public List<LineageEntity> getEntities() {
        return guidEntityMap;
    }

    public void setEntities(List<LineageEntity> guidEntityMap) {
        this.guidEntityMap = guidEntityMap;
    }

    public Set<LineageTrace> getRelations() {
        return relations;
    }

    public void setRelations(Set<LineageTrace> relations) {
        this.relations = relations;
    }

    public static class LineageEntity implements Serializable {

        private String typeName;
        private String guid;
        private String tableName;
        private String dbName;
        private String displayText;
        private Boolean process;
        private String status;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
