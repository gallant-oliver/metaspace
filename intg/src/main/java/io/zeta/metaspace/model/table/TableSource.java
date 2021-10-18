package io.zeta.metaspace.model.table;

import lombok.Data;

@Data
public class TableSource {
    private String tablename;
    private String id;
    private String dbname;
    private String ip;
    private Integer port;
}
