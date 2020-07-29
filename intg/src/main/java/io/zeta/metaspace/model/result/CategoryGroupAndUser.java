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
 * @Data 2020/7/27 16:06
 */
public class CategoryGroupAndUser {
    private String guid;
    private String name;
    private String parentCategoryGuid;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String description;
    private int level;

    private boolean group;
    private boolean user;
    private CategoryPrivilegeV2 groupPrivilege;
    private CategoryPrivilegeV2 userPrivilege;

    public CategoryGroupAndUser(){}
    public CategoryGroupAndUser(CategoryPrivilegeV2 category){
        this.guid = category.getGuid();
        this.name = category.getName();
        this.parentCategoryGuid = category.getParentCategoryGuid();
        this.upBrotherCategoryGuid = category.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid = category.getDownBrotherCategoryGuid();
        this.description = category.getDescription();
        this.level = category.getLevel();
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

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public boolean isUser() {
        return user;
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    public CategoryPrivilegeV2 getGroupPrivilege() {
        return groupPrivilege;
    }

    public void setGroupPrivilege(CategoryPrivilegeV2 groupPrivilege) {
        this.groupPrivilege = groupPrivilege;
    }

    public CategoryPrivilegeV2 getUserPrivilege() {
        return userPrivilege;
    }

    public void setUserPrivilege(CategoryPrivilegeV2 userPrivilege) {
        this.userPrivilege = userPrivilege;
    }
}
