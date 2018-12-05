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
 * @date 2018/10/23 19:06
 */
package org.zeta.metaspace.model.metadata;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/23 19:06
 */
public class ColumnQuery {
    private String guid;
    private ColumnFilter columnFilter;
    public static class ColumnFilter {
        private String columnName;
        private String type;
        private String description;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public ColumnFilter getColumnFilter() {
        return columnFilter;
    }

    public void setColumnFilter(ColumnFilter columnFilter) {
        this.columnFilter = columnFilter;
    }
}
