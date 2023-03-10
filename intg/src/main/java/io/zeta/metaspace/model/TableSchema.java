package io.zeta.metaspace.model;

import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import lombok.Data;

import java.util.List;

@Data
public class TableSchema {
    //数据源id
    String instance;
    //指定数据库
    List<String> databases;
    //指定数据表
    String table;
    //只同步数据库
    boolean allDatabase;
    //同步所有数据库和表
    boolean all = false;

    private SyncTaskDefinition definition;
}
