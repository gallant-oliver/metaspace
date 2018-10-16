package org.apache.atlas.model.metadata;

public class TablePermission {
    //更改表结构，创建分区
    private boolean ALTER ;
    //删除表，或分区
    private boolean DROP ;
    //创建和删除索引
    private boolean INDEX ;
    //锁定表，保证并发
    private boolean LOCK  ;
    //查询表权限
    private boolean SELECT ;
    //为表加载本地数据的权限
    private boolean UPDATE  ;
}
