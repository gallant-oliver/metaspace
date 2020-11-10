package io.zeta.metaspace.model.schemacrawler;

import lombok.Data;
import schemacrawler.schema.Table;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class SchemaCrawlerTable {
    List<SchemaCrawlerColumn> columns;
    List<SchemaCrawlerIndex> indexes;
    List<SchemaCrawlerForeignKey> foreignKeys;

    public SchemaCrawlerTable(Table table) {
        this.columns = table.getColumns().stream().map(SchemaCrawlerColumn::new).collect(Collectors.toList());
        this.indexes = table.getIndexes().stream().map(SchemaCrawlerIndex::new).collect(Collectors.toList());
        this.foreignKeys = table.getForeignKeys().stream().map(SchemaCrawlerForeignKey::new).collect(Collectors.toList());
    }

    public SchemaCrawlerTable() {
    }
}
