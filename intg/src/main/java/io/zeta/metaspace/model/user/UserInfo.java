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
 * @date 2019/2/18 16:14
 */
package io.zeta.metaspace.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.security.RoleResource;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/18 16:14
 */
public class UserInfo {
    private User user;
    private List<Role> roles;
    private List<Module> modules;
    private List<TechnicalCategory> technicalCategory;
    private List<BusinessCategory> businessCategory;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public List<TechnicalCategory> getTechnicalCategory() {
        return technicalCategory;
    }

    public void setTechnicalCategory(List<TechnicalCategory> technicalCategory) {
        this.technicalCategory = technicalCategory;
    }

    public List<BusinessCategory> getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(List<BusinessCategory> businessCategory) {
        this.businessCategory = businessCategory;
    }

    public static class User {
        private String userId;
        private String username;
        private String account;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }
    }

    public static class Role {
        private String roleId;
        private String roleName;

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }

    public static class Module {
        private int moduleId;
        private String moduleName;
        private int type;
        @JsonIgnore
        private int groupId;
        @JsonIgnore
        private String groupName;
        private String show;

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getShow() {
            return show;
        }

        public void setShow(String show) {
            this.show = show;
        }

        public int getModuleId() {
            return moduleId;
        }

        public void setModuleId(int moduleId) {
            this.moduleId = moduleId;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public static class TechnicalCategory {
        private String guid;
        private String categoryName;
        private int level;
        private String level2Category;

        public TechnicalCategory() { }

        public TechnicalCategory(String guid, String categoryName, int level, String level2Category) {
            this.guid = guid;
            this.categoryName = categoryName;
            this.level = level;
            this.level2Category = level2Category;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getLevel2Category() {
            return level2Category;
        }

        public void setLevel2Category(String level2Category) {
            this.level2Category = level2Category;
        }
    }

    public static class BusinessCategory {
        private String guid;
        private String categoryName;
        private int level;
        private String level2Category;

        public BusinessCategory(String guid, String categoryName, int level, String level2Category) {
            this.guid = guid;
            this.categoryName = categoryName;
            this.level = level;
            this.level2Category = level2Category;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getLevel2Category() {
            return level2Category;
        }

        public void setLevel2Category(String level2Category) {
            this.level2Category = level2Category;
        }
    }

}
