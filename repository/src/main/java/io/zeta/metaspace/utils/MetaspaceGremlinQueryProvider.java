// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2018/12/4 20:28
 */
package io.zeta.metaspace.utils;

/*
 * @description
 * @author sunhaoning
 * @date 2018/12/4 20:28
 */
public abstract class MetaspaceGremlinQueryProvider {

    public static final MetaspaceGremlinQueryProvider INSTANCE = new MetaspaceGremlin3QueryProvider();

    abstract public String getQuery(final MetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery);

    public enum MetaspaceGremlinQuery {

        // Lineage Queries
        FULL_LINEAGE,
        PARTIAL_LINEAGE,

        FULL_COLUMN_LINEAGE,
        PARTIAL_COLUMN_LINEAGE,
        DIRECT_ENTITY_NUM,
        LINEAGE_DEPTH,
        LINEAGE_DEPTH_V2,

        FULL_COLUMN_RELATED_TABLE,
        PARTIAL_COLUMN_RELATED_TABLE,

        DB_TABLE_BY_QUERY,
        FULL_DB_TABLE,
        DB_TOTAL_NUM_BY_QUERY,
        DB_ACTIVE_TOTAL_NUM_BY_QUERY,

        TABLE_GUID_QUERY,

        TABLE_DB_BY_QUERY,
        FULL_TABLE_DB,
        TABLE_COUNT_BY_QUEERY,
        COLUMN_TABLE_DB_BY_QUERY,
        FULL_COLUMN_TABLE_DB,
        COLUMN_COUNT_BY_QUERY,
        DATABASE_BY_QUERY,
        ACTIVE_DATABASE_BY_QUERY,
        FULL_DATABASE,
        FULL_ACTIVE_DATABASE,
        TABLE_BY_DB,
        FULL_TABLE,
        TABLE_TOTAL_BY_DB,
        ALL_TABLE,
        DB_TABLE_BY_STATE,
        FULL_DB_BY_STATE,
        
        COLUMN_INFO_MAP,
        COLUMN_NAME_LIST,

        COLUMN_INFO,

        TABLE_COLUMN_LIST,
        FULL_TABLE_COLUMN_LIST,
        FULL_TABLE_COLUMN_COUNT
    }
}
