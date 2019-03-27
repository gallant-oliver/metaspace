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
 * @date 2019/2/26 17:19
 */
package io.zeta.metaspace.model.business;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/26 17:19
 */
public class TechnologyInfo {
    private String businessId;
    private String technicalLastUpdate;
    private String technicalOperator;
    private Boolean editTechnical;
    private List<Table> tables;

    public static class Table {
        private String tableGuid;
        private String tableName;
        private String dbName;
        private String status;
        private String createTime;
        private boolean trust;

        public String getTableGuid() {
            return tableGuid;
        }

        public void setTableGuid(String tableGuid) {
            this.tableGuid = tableGuid;
        }

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isTrust() {
            return trust;
        }

        public void setTrust(boolean trust) {
            this.trust = trust;
        }
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getTechnicalLastUpdate() {
        return technicalLastUpdate;
    }

    public void setTechnicalLastUpdate(String technicalLastUpdate) {
        this.technicalLastUpdate = technicalLastUpdate;
    }

    public String getTechnicalOperator() {
        return technicalOperator;
    }

    public void setTechnicalOperator(String technicalOperator) {
        this.technicalOperator = technicalOperator;
    }

    public Boolean getEditTechnical() {
        return editTechnical;
    }

    public void setEditTechnical(Boolean editTechnical) {
        this.editTechnical = editTechnical;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }


}
