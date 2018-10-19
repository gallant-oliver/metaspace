package org.apache.atlas.model.metadata;

import java.util.List;
import java.util.Map;

public class TableShow {
    private String tableId;
    private List<String> columnNames;

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<Map<String, String>> getLines() {
        return lines;
    }

    public void setLines(List<Map<String, String>> lines) {
        this.lines = lines;
    }

    private List<Map<String,String>> lines;
}
