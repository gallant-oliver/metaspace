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
 * @date 2018/10/20 13:58
 */
package org.apache.atlas.model.metadata;

import java.io.Serializable;
import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/20 13:58
 */
public class RelationEntity implements Serializable  {
    private String categoryName;
    private String categoryGuid;
    private Set<RelationInfo> relations;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryGuid() {
        return categoryGuid;
    }

    public void setCategoryGuid(String categoryGuid) {
        this.categoryGuid = categoryGuid;
    }

    public Set<RelationInfo> getRelations() {
        return relations;
    }

    public void setRelations(Set<RelationInfo> relatios) {
        this.relations = relatios;
    }

    public static class ChildCatetory {
        String guid;
        String name;

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class RelationInfo implements Serializable {
        private String guid;
        private String tableName;
        private String dbName;
        private String path;
        private String relationshipGuid;

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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getRelationshipGuid() {
            return relationshipGuid;
        }

        public void setRelationshipGuid(String relationshipGuid) {
            this.relationshipGuid = relationshipGuid;
        }
    }

}
