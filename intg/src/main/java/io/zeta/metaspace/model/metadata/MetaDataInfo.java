package io.zeta.metaspace.model.metadata;

import lombok.Data;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Data
public class MetaDataInfo {
    /**
     * jdbc 连接 url
     */
    private String jdbcUrl;
    /**
     * schema 列表
     */
    private Collection<Schema> schemas;
    /**
     * table 列表，列信息等可能为空
     */
    private Collection<Table> tables;
    /**
     * 信息不完整的 table 列表
     */
    private Collection<Table> incompleteTables;

    public Collection<Schema> getSchemas() {
        if (schemas == null) {
            schemas = new ArrayList<>();
        }
        return schemas;
    }

    public Collection<Table> getTables() {
        if (tables == null) {
            tables = new ArrayList<>();
        }
        return tables;
    }

    public Collection<Table> getIncompleteTables() {
        if (incompleteTables == null) {
            incompleteTables = new ArrayList<>();
        }
        return incompleteTables;
    }

    public boolean isIncompleteTable(String tableName) {
        return getIncompleteTables().stream().anyMatch(table -> Objects.equals(table.getFullName(), tableName));
    }
}
