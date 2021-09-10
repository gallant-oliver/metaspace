package io.zeta.metaspace.model.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public class RoleModulesCategories {
    private List<Category> technicalCategories;
    private List<Category> businessCategories;
    private PrivilegeInfo privilege;
    private int edit;

    public int getEdit() {
        return edit;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }

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

    public static class Category {
        private String guid;
        private String name;
        private String parentCategoryGuid;
        private String upBrotherCategoryGuid;
        private String downBrotherCategoryGuid;
        private int status;
        private String description;
        private int level;
        private String safe;
        private String creator;
        @JsonIgnore
        private Timestamp createTime;
        private String qualifiedName;
        private int count;
        private String code;
        private Integer sort;

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }

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

        public String getQualifiedName() {
            return qualifiedName;
        }

        public void setQualifiedName(String qualifiedName) {
            this.qualifiedName = qualifiedName;
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

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public Category() {
        }

        public Category(Category category) {
            this.guid = category.guid;
            this.name = category.name;
            this.parentCategoryGuid = category.parentCategoryGuid;
            this.upBrotherCategoryGuid = category.upBrotherCategoryGuid;
            this.downBrotherCategoryGuid = category.downBrotherCategoryGuid;
            this.description = category.description;
            this.safe = category.safe;
        }

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

        private boolean show;
        private boolean hide;

        public boolean isHide() {
            return hide;
        }

        public void setHide(boolean hide) {
            this.hide = hide;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
