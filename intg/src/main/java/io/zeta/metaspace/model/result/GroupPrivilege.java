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

package io.zeta.metaspace.model.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.business.BusinessInfo;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/7/31 10:24
 */
public class GroupPrivilege {
    private String categoryId;
    private String id;
    private String name;
    private String description;
    private String member;
    private String creator;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    @JsonIgnore
    private int totalSize;
    private String authorize;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp authorizeTime;
    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;

    private List<BusinessInfo> businessInfos;

    public GroupPrivilege(GroupPrivilege privilege){
        this.read=privilege.read;
        this.editCategory=privilege.editCategory;
        this.editItem=privilege.editItem;
        this.id=privilege.id;
        this.categoryId=privilege.categoryId;
    }
    public GroupPrivilege(){}

    public List<BusinessInfo> getBusinessInfos() {
        return businessInfos;
    }

    public void setBusinessInfos(List<BusinessInfo> businessInfos) {
        this.businessInfos = businessInfos;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public String getAuthorize() {
        return authorize;
    }

    public void setAuthorize(String authorize) {
        this.authorize = authorize;
    }

    public Timestamp getAuthorizeTime() {
        return authorizeTime;
    }

    public void setAuthorizeTime(Timestamp authorizeTime) {
        this.authorizeTime = authorizeTime;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getEditCategory() {
        return editCategory;
    }

    public void setEditCategory(Boolean editCategory) {
        this.editCategory = editCategory;
    }

    public Boolean getEditItem() {
        return editItem;
    }

    public void setEditItem(Boolean editItem) {
        this.editItem = editItem;
    }
}
