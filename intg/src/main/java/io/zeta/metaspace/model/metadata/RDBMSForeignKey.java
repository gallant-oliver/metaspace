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

package io.zeta.metaspace.model.metadata;

import java.io.Serializable;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2019/10/17 15:01
 */
public class RDBMSForeignKey implements Serializable {
    private String name;
    private List<RDBMSColumn> columns;
    private String status;
    private String foreignKeyId;

    private String tableId;
    private String tableName;
    private String databaseId;
    private String databaseName;
    private String sourceId;
    private String sourceName;

    public String getForeignKeyId() {
        return foreignKeyId;
    }

    public void setForeignKeyId(String foreignKeyId) {
        this.foreignKeyId = foreignKeyId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RDBMSColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<RDBMSColumn> columns) {
        this.columns = columns;
    }
}
