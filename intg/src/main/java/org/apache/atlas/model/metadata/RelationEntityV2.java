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
 * @date 2018/11/21 13:48
 */
package org.apache.atlas.model.metadata;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/21 13:48
 */
public class RelationEntityV2 {

    private String relationshipGuid;
    private String categoryGuid;
    private String tableName;
    private String dbName;
    private String tableGuid;
    private String path;
    private String status;

    public RelationEntityV2() { }

    public RelationEntityV2(String relationshipGuid, String categoryGuid, String tableName, String dbName, String tableGuid, String path, String status) {
        this.relationshipGuid = relationshipGuid;
        this.categoryGuid = categoryGuid;
        this.tableName = tableName;
        this.dbName = dbName;
        this.tableGuid = tableGuid;
        this.path = path;
        this.status = status;
    }

    public String getRelationshipGuid() {
        return relationshipGuid;
    }

    public void setRelationshipGuid(String relationshipGuid) {
        this.relationshipGuid = relationshipGuid;
    }

    public String getCategoryGuid() {
        return categoryGuid;
    }

    public void setCategoryGuid(String categoryGuid) {
        this.categoryGuid = categoryGuid;
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

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
