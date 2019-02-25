package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.privilege.Privilege;

import java.util.List;
import java.util.Objects;

public class RoleModulesCategories {
    private List<Category> technicalCategories;
    private List<Category> businessCategories;
    private Privilege privilege;

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

    public Privilege getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Privilege privilege) {
        this.privilege = privilege;
    }
    public static class Category{
        private String guid;
        private String name;
        private String parentCategoryGuid;
        private String upBrothCategoryGuid;
        private String downBrothCategoryGuid;
        private int status;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Category category = (Category) o;
            return Objects.equals(guid, category.guid);
        }

        @Override
        public int hashCode() {

            return Objects.hash(guid);
        }

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
