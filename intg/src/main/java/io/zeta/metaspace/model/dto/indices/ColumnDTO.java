package io.zeta.metaspace.model.dto.indices;

import java.util.List;

public class ColumnDTO {
    private String columnId;
    private String columnName;
    private List<String> columnValues;

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public List<String> getColumnValues() {
        return columnValues;
    }
    public void setColumnValues(List<String> columnValues) {
        this.columnValues = columnValues;
    }
}
