package io.zeta.metaspace.model.schemacrawler;

import lombok.Data;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexType;
import schemacrawler.schema.NamedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SchemaCrawlerIndex {
    private String name;
    private IndexType indexType;
    private boolean isUnique;
    private String comment;
    private List<String> columns;

    public SchemaCrawlerIndex() {
    }

    public SchemaCrawlerIndex(Index index) {
        this.name = index.getName();
        this.indexType = index.getIndexType();
        this.isUnique = index.isUnique();
        this.comment = index.getRemarks();
        this.columns = index.getColumns().stream().map(NamedObject::getName).collect(Collectors.toList());
    }

    public void addColumns(String columnName) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(columnName);
    }
}
