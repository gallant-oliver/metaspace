package io.zeta.metaspace.model.result;

import io.zeta.metaspace.model.dataquality.SystemRule;

import java.util.List;

public class TableColumnRules {
    private List<SystemRule> tableRules;
    private List<ColumnsRule> columnsRules;

    public List<SystemRule> getTableRules() {
        return tableRules;
    }

    public void setTableRules(List<SystemRule> tableRules) {
        this.tableRules = tableRules;
    }

    public List<ColumnsRule> getColumnsRules() {
        return columnsRules;
    }

    public void setColumnsRules(List<ColumnsRule> columnsRules) {
        this.columnsRules = columnsRules;
    }
    public static class ColumnsRule{
        private String columnName;
        private String columnType;
        private List<SystemRule> columnRules;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        public List<SystemRule> getColumnRules() {
            return columnRules;
        }

        public void setColumnRules(List<SystemRule> columnRules) {
            this.columnRules = columnRules;
        }
    }
}
