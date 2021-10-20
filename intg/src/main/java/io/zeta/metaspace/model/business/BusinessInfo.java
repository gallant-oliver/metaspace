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

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private String submitter;
    private String submissionTime;
    private String ticketNumber;
    private Boolean editBusiness;
    private String level2CategoryId;
    private String trustTable;
    @JsonIgnore
    private String categoryGuid;
    @JsonIgnore
    private String categoryName;

    /**
     * 是否发布
    */
    private Boolean publish;

    /**
     * 审批组id
     */
    private String approveGroupId;

    /**
     * 审批id
     */
    private String approveId;

    /**
     * 说明信息
     */
    private String publishDesc;

    /**
     * 状态：0待发布，1待审批，2审核不通过，3审核通过'
     */
    private String status;

    /**
     * 创建方式：0（手动添加），1（上传文件）
     */
    private int createMode;

    /**
     * 私密状态：PUBLIC公开；PRIVATE私密
     */
    private String privateStatus;

    /**
     * 是否创建人可见
     */
    private Boolean submitterRead;

    public Boolean getSubmitterRead() {
        return submitterRead;
    }

    public void setSubmitterRead(Boolean submitterRead) {
        this.submitterRead = submitterRead;
    }

    public String getPrivateStatus() {
        return privateStatus;
    }

    public void setPrivateStatus(String privateStatus) {
        this.privateStatus = privateStatus;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public String getApproveGroupId() {
        return approveGroupId;
    }

    public void setApproveGroupId(String approveGroupId) {
        this.approveGroupId = approveGroupId;
    }

    public String getApproveId() {
        return approveId;
    }

    public void setApproveId(String approveId) {
        this.approveId = approveId;
    }

    public String getPublishDesc() {
        return publishDesc;
    }

    public void setPublishDesc(String publishDesc) {
        this.publishDesc = publishDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreateMode() {
        return createMode;
    }

    public void setCreateMode(int createMode) {
        this.createMode = createMode;
    }

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

    public String getLevel2CategoryId() {
        return level2CategoryId;
    }

    public void setLevel2CategoryId(String level2CategoryId) {
        this.level2CategoryId = level2CategoryId;
    }

    public String getTrustTable() {
        return trustTable;
    }

    public void setTrustTable(String trustTable) {
        this.trustTable = trustTable;
    }
}
