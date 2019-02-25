package io.zeta.metaspace.model.result;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;

import java.util.List;

public class RoleModulesCategories {
    private List<Category> technicalCategories;
    private List<Category> businessCategories;
    private PrivilegeInfo privilege;

    public List<Category> getTechnicalCategories() {
        return technicalCategories;
    }

    public void setTechnicalCategories(List<Category> technicalCategories) {
        this.technicalCategories = technicalCategories;
    }

    public List<Category> getBusinessCategories() {
        return businessCategories;
    }

    public void setBusinessCategories(List<Category> businessCategories) {
        this.businessCategories = businessCategories;
    }

    public PrivilegeInfo getPrivilege() {
        return privilege;
    }

    public void setPrivilege(PrivilegeInfo privilege) {
        this.privilege = privilege;
    }
    public static class Category{
        private String guid;
        private String name;
        private String parentCategoryGuid;
        private String upBrothCategoryGuid;
        private String downBrothCategoryGuid;
        private int status;
        private boolean show	;
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

        public String getUpBrothCategoryGuid() {
            return upBrothCategoryGuid;
        }

        public void setUpBrothCategoryGuid(String upBrothCategoryGuid) {
            this.upBrothCategoryGuid = upBrothCategoryGuid;
        }

        public String getDownBrothCategoryGuid() {
            return downBrothCategoryGuid;
        }

        public void setDownBrothCategoryGuid(String downBrothCategoryGuid) {
            this.downBrothCategoryGuid = downBrothCategoryGuid;
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


    }
}
