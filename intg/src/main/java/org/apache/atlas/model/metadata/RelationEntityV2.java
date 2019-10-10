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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.metadata.DataOwnerHeader;

import java.util.List;

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
    private String createTime;
    private String generateTime;
    private List<DataOwnerHeader> dataOwner;
    private List<String> tableTagList;
    @JsonIgnore
    private int total;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(String generateTime) {
        this.generateTime = generateTime;
    }

    public List<DataOwnerHeader> getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(List<DataOwnerHeader> dataOwner) {
        this.dataOwner = dataOwner;
    }

    public List<String> getTableTagList() {
        return tableTagList;
    }

    public void setTableTagList(List<String> tableTagList) {
        this.tableTagList = tableTagList;
    }
}
