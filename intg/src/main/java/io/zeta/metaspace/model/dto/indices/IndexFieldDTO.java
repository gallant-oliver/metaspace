package io.zeta.metaspace.model.dto.indices;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class IndexFieldDTO {

    private String guid;
    private String qualifiedName;
    private String name;
    private String description;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String parentCategoryGuid;
    private Integer categoryType;
    private int level;

    private String safe;

    private String createTime;

    private String updateTime;

    private String creator;
    private String updater;
    //编码
    private String code;

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

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
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
}
