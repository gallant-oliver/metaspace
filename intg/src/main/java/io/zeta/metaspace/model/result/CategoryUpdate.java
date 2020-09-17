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

/**
 * @author lixiang03
 * @Data 2020/7/28 15:30
 */
public class CategoryUpdate {
    private String guid;
    private String name;
    private String parentCategoryGuid;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String description;
    private int level;
    private String path;
    private CategoryPrivilegeV2 oldPrivilege;
    private CategoryPrivilegeV2 newPrivilege;

    public CategoryUpdate(){}
    public CategoryUpdate(CategoryPrivilegeV2 category){
        this.guid = category.getGuid();
        this.name = category.getName();
        this.parentCategoryGuid = category.getParentCategoryGuid();
        this.upBrotherCategoryGuid = category.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid = category.getDownBrotherCategoryGuid();
        this.description = category.getDescription();
        this.level = category.getLevel();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public CategoryPrivilegeV2 getOldPrivilege() {
        return oldPrivilege;
    }

    public void setOldPrivilege(CategoryPrivilegeV2 oldPrivilege) {
        this.oldPrivilege = oldPrivilege;
    }

    public CategoryPrivilegeV2 getNewPrivilege() {
        return newPrivilege;
    }

    public void setNewPrivilege(CategoryPrivilegeV2 newPrivilege) {
        this.newPrivilege = newPrivilege;
    }
}
