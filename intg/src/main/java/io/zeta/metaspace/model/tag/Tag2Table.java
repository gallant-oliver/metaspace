package io.zeta.metaspace.model.tag;

import io.zeta.metaspace.model.metadata.Table;

public class Tag2Table {
    private String tagId;
    private Table table;

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }
}
