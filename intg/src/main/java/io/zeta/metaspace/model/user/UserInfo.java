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

import io.zeta.metaspace.model.role.Role;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/18 16:14
 */
public class UserInfo {
    private User user;
    private Role role;
    private List<Module> module;
    private List<TechnicalCategory> technicalCategory;
    private List<BusinessCategory> businessCategory;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<Module> getModule() {
        return module;
    }

    public void setModule(List<Module> module) {
        this.module = module;
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

    public class Module {
        private String moduleId;
        private String moduleName;
        private String type;

        public String getModuleId() {
            return moduleId;
        }

        public void setModuleId(String moduleId) {
            this.moduleId = moduleId;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public class TechnicalCategory {
        private String categoryName;
        private String level;
        private String level2Category;

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getLevel2Category() {
            return level2Category;
        }

        public void setLevel2Category(String level2Category) {
            this.level2Category = level2Category;
        }
    }

    public class BusinessCategory {
        private String categoryName;
        private String level;
        private String level2Category;

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
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
