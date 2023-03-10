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
 * @date 2018/11/20 10:41
 */
package org.apache.atlas.model.metadata;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.enums.CategoryPrivateStatus;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/20 10:41
 */
public class CategoryEntityV2 {

    private String guid;
    private String qualifiedName;
    private String name;
    private String description;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;

    private String parentCategoryGuid;
    private Integer categoryType;
    private int level;
    private int status;
    private CategoryPrivateStatus privateStatus;
    private boolean show;
    private String path;
    @JsonIgnore
    private String safe;

    @JsonIgnore
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonIgnore
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    private String creator;
    private String updater;
    //编码
    private String code;

    private Integer sort;

    private Boolean publish;

    private String information;

    private String approvalId;

    /**
     * 租户id
     */
    private String tenantid;

    private String tenantName;

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public CategoryEntityV2() { }
    public CategoryEntityV2(String guid, String qualifiedName, String name, String description, String upBrotherCategoryGuid, String downBrotherCategoryGuid, String parentCategoryGuid, Integer categoryType) {
        this.guid = guid;
        this.qualifiedName = qualifiedName;
        this.name = name;
        this.description = description;
        this.upBrotherCategoryGuid = upBrotherCategoryGuid;
        this.downBrotherCategoryGuid = downBrotherCategoryGuid;
        this.parentCategoryGuid = parentCategoryGuid;
        this.categoryType = categoryType;
    }
    public CategoryEntityV2(String guid, String name, String description, String parentCategoryGuid, String upBrotherCategoryGuid, String downBrotherCategoryGuid, Integer categoryType, Integer level,String safe,Timestamp createTime,
                            String code) {
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.upBrotherCategoryGuid = upBrotherCategoryGuid;
        this.downBrotherCategoryGuid = downBrotherCategoryGuid;
        this.parentCategoryGuid = parentCategoryGuid;
        this.categoryType = categoryType;
        this.level=level;
        this.safe=safe;
        this.createTime=createTime;
        this.code=code;
    }

    public CategoryEntityV2(String guid, String name, String description, String parentCategoryGuid, String upBrotherCategoryGuid, String downBrotherCategoryGuid, Integer categoryType, Integer level,String safe,Timestamp createTime,
                            String code, Integer sort, CategoryPrivateStatus privateStatus) {
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.upBrotherCategoryGuid = upBrotherCategoryGuid;
        this.downBrotherCategoryGuid = downBrotherCategoryGuid;
        this.parentCategoryGuid = parentCategoryGuid;
        this.categoryType = categoryType;
        this.level=level;
        this.safe=safe;
        this.createTime=createTime;
        this.code=code;
        this.sort=sort;
        this.privateStatus=privateStatus;
    }

    public CategoryEntityV2(String guid, String name, String description, String parentCategoryGuid, String upBrotherCategoryGuid, String downBrotherCategoryGuid, Integer categoryType,
                            Integer level,String safe,Timestamp createTime,String qualifiedName,Timestamp updateTime,String creator,String updater,String code) {
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.upBrotherCategoryGuid = upBrotherCategoryGuid;
        this.downBrotherCategoryGuid = downBrotherCategoryGuid;
        this.parentCategoryGuid = parentCategoryGuid;
        this.categoryType = categoryType;
        this.level=level;
        this.safe=safe;
        this.createTime=createTime;
        this.qualifiedName=qualifiedName;
        this.updateTime=updateTime;
        this.creator=creator;
        this.updater=updater;
        this.code=code;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryPrivateStatus getPrivateStatus() {
        return privateStatus;
    }

    public void setPrivateStatus(CategoryPrivateStatus privateStatus) {
        this.privateStatus = privateStatus;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUpBrotherCategoryGuid() {
        return upBrotherCategoryGuid;
    }

    public void setUpBrotherCategoryGuid(String upBrotherCategoryGuid) {
        this.upBrotherCategoryGuid = upBrotherCategoryGuid;
    }

    public String getDownBrotherCategoryGuid() {
        return downBrotherCategoryGuid;
    }

    public void setDownBrotherCategoryGuid(String downBrotherCategoryGuid) {
        this.downBrotherCategoryGuid = downBrotherCategoryGuid;
    }

    public String getParentCategoryGuid() {
        return parentCategoryGuid;
    }

    public void setParentCategoryGuid(String parentCategoryGuid) {
        this.parentCategoryGuid = parentCategoryGuid;
    }

    public Integer getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(Integer categoryType) {
        this.categoryType = categoryType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }
}
