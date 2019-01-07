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

public class Database implements Serializable {
    private String databaseId;
    private String databaseName;
    private String databaseDescription;
    private String status;
    private List tableList;
    public String getDatabaseDescription() {
        return databaseDescription;
    }

    public void setDatabaseDescription(String databaseDescription) {
        this.databaseDescription = databaseDescription;
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

    public List getTableList() {
        return tableList;
    }

    public void setTableList(List tableList) {
        this.tableList = tableList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}