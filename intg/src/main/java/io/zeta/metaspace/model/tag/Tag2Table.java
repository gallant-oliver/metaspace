package io.zeta.metaspace.model.tag;

import io.zeta.metaspace.model.metadata.Table;

import java.util.List;

public class Tag2Table {
    private List<String> tags;
    private Table table;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
