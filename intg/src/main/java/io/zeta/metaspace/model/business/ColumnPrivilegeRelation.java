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
 * @date 2019/3/20 11:49
 */
package io.zeta.metaspace.model.business;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/20 11:49
 */
public class ColumnPrivilegeRelation {
    private String columnGuid;
    private Integer columnPrivilegeGuid;
    private String name;


    public String getColumnGuid() {
        return columnGuid;
    }

    public void setColumnGuid(String columnGuid) {
        this.columnGuid = columnGuid;
    }


    public Integer getColumnPrivilegeGuid() {
        return columnPrivilegeGuid;
    }

    public void setColumnPrivilegeGuid(Integer columnPrivilegeGuid) {
        this.columnPrivilegeGuid = columnPrivilegeGuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
