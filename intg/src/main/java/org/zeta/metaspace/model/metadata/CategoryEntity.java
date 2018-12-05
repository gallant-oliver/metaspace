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
 * @date 2018/10/24 11:03
 */
package org.zeta.metaspace.model.metadata;

import org.apache.atlas.model.glossary.relations.AtlasGlossaryHeader;
import org.apache.atlas.model.glossary.relations.AtlasRelatedCategoryHeader;

import java.io.Serializable;
import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/24 11:03
 */
public class CategoryEntity implements Serializable {

    private String guid;
    private String qualifiedName;
    private String name;
    private String description;
    private AtlasGlossaryHeader anchor;
    private String upBrothCategoryGuid;
    private String downBrothCategoryGuid;

    private String parentCategoryGuid;
    private Set<String> childrenCategoriesGuid;

    // Category hierarchy links
    private AtlasRelatedCategoryHeader parentCategory;
    private Set<AtlasRelatedCategoryHeader> childrenCategories;

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

    public AtlasGlossaryHeader getAnchor() {
        return anchor;
    }

    public void setAnchor(AtlasGlossaryHeader anchor) {
        this.anchor = anchor;
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

    public AtlasRelatedCategoryHeader getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(AtlasRelatedCategoryHeader parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Set<AtlasRelatedCategoryHeader> getChildrenCategories() {
        return childrenCategories;
    }

    public void setChildrenCategories(Set<AtlasRelatedCategoryHeader> childrenCategories) {
        this.childrenCategories = childrenCategories;
    }

    public String getParentCategoryGuid() {
        return parentCategoryGuid;
    }

    public void setParentCategoryGuid(String parentCategoryGuid) {
        this.parentCategoryGuid = parentCategoryGuid;
    }

    public Set<String> getChildrenCategoriesGuid() {
        return childrenCategoriesGuid;
    }

    public void setChildrenCategoriesGuid(Set<String> childrenCategoriesGuid) {
        this.childrenCategoriesGuid = childrenCategoriesGuid;
    }
}
