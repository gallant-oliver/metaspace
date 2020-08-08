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

package io.zeta.metaspace.model.result;

/**
 * @author lixiang03
 * @Data 2020/7/31 10:36
 */
public class CategoryGroupPrivilege {
    private String guid;
    private boolean child;
    private Boolean read;
    private Boolean editCategory;
    private Boolean editItem;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public boolean isChild() {
        return child;
    }

    public void setChild(boolean child) {
        this.child = child;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getEditCategory() {
        return editCategory;
    }

    public void setEditCategory(Boolean editCategory) {
        this.editCategory = editCategory;
    }

    public Boolean getEditItem() {
        return editItem;
    }

    public void setEditItem(Boolean editItem) {
        this.editItem = editItem;
    }
}
