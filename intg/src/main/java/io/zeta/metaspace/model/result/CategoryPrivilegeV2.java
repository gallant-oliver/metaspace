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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author lixiang03
 * @Data 2020/7/24 17:27
 */
public class CategoryPrivilegeV2 {
    private String guid;
    private String name;
    private String parentCategoryGuid;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String description;
    private int level;
    private String safe;
    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;
    private String code;
    private int count;

    @JsonIgnore
    private int total;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public CategoryPrivilegeV2(){}
    public CategoryPrivilegeV2(CategoryPrivilegeV2 category){
        this.guid = category.getGuid();
        this.name = category.getName();
        this.parentCategoryGuid = category.getParentCategoryGuid();
        this.upBrotherCategoryGuid = category.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid = category.getDownBrotherCategoryGuid();
        this.description = category.getDescription();
        this.level = category.getLevel();
        read=false;
        editCategory=false;
        editItem=false;
    }
    public CategoryPrivilegeV2(UpdateCategory category){
        read=category.getRead();
        editCategory=category.getEditCategory();
        editItem=category.getEditItem();
    }
    public CategoryPrivilegeV2(CategoryGroupPrivilege privilege){
        this.guid=privilege.getGuid();
        this.read=privilege.getRead();
        this.editCategory=privilege.getEditCategory();
        this.editItem=privilege.getEditItem();
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCategoryGuid() {
        return parentCategoryGuid;
    }

    public void setParentCategoryGuid(String parentCategoryGuid) {
        this.parentCategoryGuid = parentCategoryGuid;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
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
