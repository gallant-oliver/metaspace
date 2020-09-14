package io.zeta.metaspace.model.schemacrawler;

import lombok.Data;
import schemacrawler.schema.ForeignKey;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SchemaCrawlerForeignKey {
    private String name;
    private List<SchemaCrawlerColumn> primaryKeyColumns = new ArrayList<>();
    private List<SchemaCrawlerColumn> foreignKeyColumns = new ArrayList<>();

    public SchemaCrawlerForeignKey() {
    }

    public SchemaCrawlerForeignKey(ForeignKey foreignKey) {
        this.name = foreignKey.getName();
        foreignKey.getColumnReferences().forEach(reference -> {
            primaryKeyColumns.add(new SchemaCrawlerColumn(reference.getPrimaryKeyColumn()));
            foreignKeyColumns.add(new SchemaCrawlerColumn(reference.getForeignKeyColumn()));
        });
    }

    public void addPrimaryKeyColumn(String schemaName, String tableName, String columnName) {
        if (primaryKeyColumns == null) {
            primaryKeyColumns = new ArrayList<>();
        }
        primaryKeyColumns.add(new SchemaCrawlerColumn(schemaName, tableName, columnName));
    }

    public void addForeignKeyColumn(String schemaName, String tableName, String columnName) {
        if (foreignKeyColumns == null) {
            foreignKeyColumns = new ArrayList<>();
        }
        foreignKeyColumns.add(new SchemaCrawlerColumn(schemaName, tableName, columnName));
    }

    public List<String> getPrimaryKeyColumnNames() {
        return primaryKeyColumns.stream().map(SchemaCrawlerColumn::getName).collect(Collectors.toList());
    }

    public List<String> getForeignKeyColumnNames() {
        return foreignKeyColumns.stream().map(SchemaCrawlerColumn::getName).collect(Collectors.toList());
    }
}
