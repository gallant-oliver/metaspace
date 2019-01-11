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
 * @date 2019/1/10 14:18
 */
package io.zeta.metaspace.model.dataquality;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/10 14:18
 */
public class ExcelReport {

    private String tableSheetName;
    private String columnSheetName;
    private List<String> tableAttributes;
    private List<List<String>> tableData;
    private List<String> columnAttributes;
    private List<List<String>> columnData;

    public String getTableSheetName() {
        return tableSheetName;
    }

    public void setTableSheetName(String tableSheetName) {
        this.tableSheetName = tableSheetName;
    }

    public String getColumnSheetName() {
        return columnSheetName;
    }

    public void setColumnSheetName(String columnSheetName) {
        this.columnSheetName = columnSheetName;
    }

    public List<String> getTableAttributes() {
        return tableAttributes;
    }

    public void setTableAttributes(List<String> tableAttributes) {
        this.tableAttributes = tableAttributes;
    }

    public List<List<String>> getTableData() {
        return tableData;
    }

    public void setTableData(List<List<String>> tableData) {
        this.tableData = tableData;
    }

    public List<String> getColumnAttributes() {
        return columnAttributes;
    }

    public void setColumnAttributes(List<String> columnAttributes) {
        this.columnAttributes = columnAttributes;
    }

    public List<List<String>> getColumnData() {
        return columnData;
    }

    public void setColumnData(List<List<String>> columnData) {
        this.columnData = columnData;
    }
}
