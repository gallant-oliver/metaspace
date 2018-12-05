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
 * @date 2018/10/31 20:00
 */
package org.zeta.metaspace.model.metadata;

import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/31 20:00
 */
public class CategoryChildren {

    private String categoryName;
    private String categoryGuid;
    private Set<RelationEntity.ChildCatetory> childCategory;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryGuid() {
        return categoryGuid;
    }

    public void setCategoryGuid(String categoryGuid) {
        this.categoryGuid = categoryGuid;
    }

    public Set<RelationEntity.ChildCatetory> getChildCategory() {
        return childCategory;
    }

    public void setChildCategory(Set<RelationEntity.ChildCatetory> childCategory) {
        this.childCategory = childCategory;
    }
}
