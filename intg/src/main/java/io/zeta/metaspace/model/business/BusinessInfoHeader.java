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
 * @date 2019/2/21 11:43
 */
package io.zeta.metaspace.model.business;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/21 11:43
 */
public class BusinessInfoHeader {
    private String businessId;
    private String name;
    private String level2Category;
    private String path;
    private String businessStatus;
    private String technicalStatus;
    private String submitter;
    private String submitterName;


    private String departmentId;
    private String submissionTime;
    private String ticketNumber;
    private String categoryGuid;
    private String trustTable;
    private String tenantId;
    private List<TechnologyInfo.Table> tables;

    /**
     * 状态：0待发布，1待审批，2审核不通过，3审核通过'
     */
    private String status;

    /**
     * 是否发布
     */
    private Boolean publish;

    @JsonIgnore
    private int total;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public List<TechnologyInfo.Table> getTables() {
        return tables;
    }

    public void setTables(List<TechnologyInfo.Table> tables) {
        this.tables = tables;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
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

    public String getLevel2Category() {
        return level2Category;
    }

    public void setLevel2Category(String level2Category) {
        this.level2Category = level2Category;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public String getTechnicalStatus() {
        return technicalStatus;
    }

    public void setTechnicalStatus(String technicalStatus) {
        this.technicalStatus = technicalStatus;
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

    public String getCategoryGuid() {
        return categoryGuid;
    }

    public void setCategoryGuid(String categoryGuid) {
        this.categoryGuid = categoryGuid;
    }

    public String getTrustTable() {
        return trustTable;
    }

    public void setTrustTable(String trustTable) {
        this.trustTable = trustTable;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
