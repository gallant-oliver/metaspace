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
 * @date 2019/2/12 13:52
 */
package io.zeta.metaspace.model.business;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 13:52
 */
public class BusinessInfo {
    private String businessId;
    private String name;
    private String departmentId;
    private String departmentName;
    private String module;
    private String description;
    private String owner;
    private String manager;
    private String maintainer;
    private String dataAssets;
    private String businessLastUpdate;
    private String businessOperator;
    private String technicalLastUpdate;
    private String technicalOperator;
    private String submitter;
    private String submissionTime;
    private String ticketNumber;
    private Boolean editBusiness;
    private Boolean editTechnical;
    private List<Table> tables;

    public static class Table {
        private String tableGuid;
        private String tableName;
        private String dbName;
        private String status;
        private String createTime;

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
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getDataAssets() {
        return dataAssets;
    }

    public void setDataAssets(String dataAssets) {
        this.dataAssets = dataAssets;
    }

    public String getBusinessLastUpdate() {
        return businessLastUpdate;
    }

    public void setBusinessLastUpdate(String businessLastUpdate) {
        this.businessLastUpdate = businessLastUpdate;
    }

    public String getBusinessOperator() {
        return businessOperator;
    }

    public void setBusinessOperator(String businessOperator) {
        this.businessOperator = businessOperator;
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

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Boolean getEditBusiness() {
        return editBusiness;
    }

    public void setEditBusiness(Boolean editBusiness) {
        this.editBusiness = editBusiness;
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
