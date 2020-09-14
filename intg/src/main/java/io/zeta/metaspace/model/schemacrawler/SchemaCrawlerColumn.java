package io.zeta.metaspace.model.schemacrawler;


import lombok.Data;
import schemacrawler.schema.Column;

@Data
public class SchemaCrawlerColumn {
    private String name;
    private String dataType;
    private int length;
    private String defaultValue;
    private String comment;
    private boolean isNullable;
    private boolean isPrimaryKey;

    private String schemaName;
    private String tableName;

    public SchemaCrawlerColumn() {
    }

    public SchemaCrawlerColumn(Column column) {
        this.name = column.getName();
        this.dataType = column.getColumnDataType().getFullName();
        this.length = column.getSize();
        this.defaultValue = column.getDefaultValue();
        this.comment = column.getRemarks();
        this.isNullable = column.isNullable();
        this.isPrimaryKey = column.isPartOfPrimaryKey();
        this.schemaName = column.getSchema().getFullName();
        this.tableName = column.getParent().getName();
    }

    public SchemaCrawlerColumn(String schemaName, String tableName, String name) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.name = name;
    }
}
