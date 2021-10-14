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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

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
    private Integer count;
    private String trustTable;

    @Data
    public static class Table {
        private String tableGuid;
        private String tableName;
        private String dbName;
        private String status;
        private String createTime;
        private boolean trust;
        private String databaseGuid;
        private String displayName;
        private String description;
        private String sourceId;
        private String sourceName;

        /**
        * 与业务对象关联关系来源：0通过业务对象挂载功能挂载到该业务对象的表；1通过衍生表登记模块登记关联到该业务对象上的表
        */
        private Integer relationType;

        /**
         * 是否重要
         */
        private boolean important;

        /**
         * 是否保密
         */
        private boolean secret;

        @JsonIgnore
        private int total;

        public boolean isImportant() {
            return important;
        }

        public void setImportant(boolean important) {
            this.important = important;
        }

        public boolean isSecret() {
            return secret;
        }

        public void setSecret(boolean secret) {
            this.secret = secret;
        }

        public Integer getRelationType() {
            return relationType;
        }

        public void setRelationType(Integer relationType) {
            this.relationType = relationType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

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

        public String getDatabaseGuid() {
            return databaseGuid;
        }

        public void setDatabaseGuid(String databaseGuid) {
            this.databaseGuid = databaseGuid;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
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


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getTrustTable() {
        return trustTable;
    }

    public void setTrustTable(String trustTable) {
        this.trustTable = trustTable;
    }
}
