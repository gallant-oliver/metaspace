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

package io.zeta.metaspace.model.datastandard;

import io.zeta.metaspace.model.result.CategoryPrivilege;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2019/10/30 16:52
 */
public class CategoryAndDataStandard {
    private String guid;
    private String name;
    private String parentCategoryGuid;
    private String upBrotherCategoryGuid;
    private String downBrotherCategoryGuid;
    private String description;
    private int level;
    private List<DataStandardHead> dataStandards;
    public CategoryAndDataStandard(){}
    public CategoryAndDataStandard(CategoryPrivilege categoryPrivilege){
        this.guid = categoryPrivilege.getGuid();
        this.name = categoryPrivilege.getName();
        this.parentCategoryGuid = categoryPrivilege.getParentCategoryGuid();
        this.upBrotherCategoryGuid = categoryPrivilege.getUpBrotherCategoryGuid();
        this.downBrotherCategoryGuid = categoryPrivilege.getDownBrotherCategoryGuid();
        this.description = categoryPrivilege.getDescription();
        this.level = categoryPrivilege.getLevel();
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

    public List<DataStandardHead> getDataStandards() {
        return dataStandards;
    }

    public void setDataStandards(List<DataStandardHead> dataStandards) {
        this.dataStandards = dataStandards;
    }
}
