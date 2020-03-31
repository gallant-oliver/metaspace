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

package io.zeta.metaspace.model.usergroup;

import io.zeta.metaspace.model.result.RoleModulesCategories;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/25 14:32
 */
public class UserGroupCategories {
    private List<RoleModulesCategories.Category> technicalCategories;
    private List<RoleModulesCategories.Category> businessCategories;
    private int edit;

    public List<RoleModulesCategories.Category> getTechnicalCategories() {
        return technicalCategories;
    }

    public void setTechnicalCategories(List<RoleModulesCategories.Category> technicalCategories) {
        this.technicalCategories = technicalCategories;
    }

    public List<RoleModulesCategories.Category> getBusinessCategories() {
        return businessCategories;
    }

    public void setBusinessCategories(List<RoleModulesCategories.Category> businessCategories) {
        this.businessCategories = businessCategories;
    }

    public int getEdit() {
        return edit;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }
}
