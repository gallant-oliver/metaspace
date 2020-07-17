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
public abstract class AbstractMetaspaceGremlinQueryProvider {

    public static final AbstractMetaspaceGremlinQueryProvider INSTANCE = new MetaspaceGremlin3QueryProvider();

    abstract public String getQuery(final AbstractMetaspaceGremlinQueryProvider.MetaspaceGremlinQuery gremlinQuery);

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

        TENANT_DB_TABLE_BY_QUERY,
        TENANT_FULL_DB_TABLE,
        TENANT_DB_TOTAL_NUM_BY_QUERY,
        TENANT_DB_ACTIVE_TOTAL_NUM_BY_QUERY,

        TABLE_GUID_QUERY,

        TABLE_DB_BY_QUERY,
        ACTIVE_TABLE_DB_BY_QUERY,
        FULL_TABLE_DB,
        FULL_ACTIVE_TABLE_DB,
        TABLE_COUNT_BY_QUEERY,
        ACTIVE_TABLE_COUNT_BY_QUEERY,
        COLUMN_TABLE_DB_BY_QUERY,
        FULL_COLUMN_TABLE_DB,
        ACTIVE_COLUMN_TABLE_DB_BY_QUERY,
        FULL_ACTIVE_COLUMN_TABLE_DB,
        COLUMN_COUNT_BY_QUERY,
        ACTIVE_COLUMN_COUNT_BY_QUERY,
        DATABASE_BY_QUERY,
        ACTIVE_DATABASE_BY_QUERY,
        FULL_DATABASE,
        FULL_ACTIVE_DATABASE,
        TABLE_BY_DB,
        FULL_TABLE,
        ACTIVE_TABLE_BY_DB,
        FULL_ACTIVE_TABLE,
        TABLE_TOTAL_BY_DB,
        ACTIVE_TABLE_TOTAL_BY_DB,
        ALL_TABLE,
        DB_TABLE_BY_STATE,
        FULL_DB_BY_STATE,
        FULL_RDBMS_DB_BY_STATE,

        TENANT_ACTIVE_TABLE_DB_BY_QUERY,
        TENANT_TABLE_DB_BY_QUERY,
        TENANT_FULL_TABLE_DB,
        TENANT_FULL_ACTIVE_TABLE_DB,
        TENANT_TABLE_COUNT_BY_QUEERY,
        TENANT_ACTIVE_TABLE_COUNT_BY_QUEERY,
        TENANT_COLUMN_TABLE_DB_BY_QUERY,
        TENANT_FULL_COLUMN_TABLE_DB,
        TENANT_ACTIVE_COLUMN_TABLE_DB_BY_QUERY,
        TENANT_FULL_ACTIVE_COLUMN_TABLE_DB,
        TENANT_COLUMN_COUNT_BY_QUERY,
        TENANT_ACTIVE_COLUMN_COUNT_BY_QUERY,
        TENANT_DATABASE_BY_QUERY,
        TENANT_ACTIVE_DATABASE_BY_QUERY,
        TENANT_FULL_DATABASE,
        TENANT_FULL_ACTIVE_DATABASE,

        RDBMS_DB_TABLE_BY_STATE,
        RDBMS_DB_TABLE_COLUMN_BY_STATE,
        RDBMS_DB_TABLE_FOREIGNKEY_BY_STATE,
        RDBMS_DB_TABLE_INDEX_BY_STATE,

        COLUMN_INFO_MAP,
        COLUMN_NAME_LIST,

        COLUMN_INFO,

        TABLE_COLUMN_LIST,
        FULL_TABLE_COLUMN_LIST,
        FULL_TABLE_COLUMN_COUNT,

        FUll_RDBMS_SOURCE,
        RDBMS_SOURCE_BY_QUERY,
        RDBMS_SOURCE_TOTAL_NUM_BY_QUERY,
        ACTIVE_FUll_RDBMS_SOURCE,
        ACTIVE_RDBMS_SOURCE_BY_QUERY,
        ACTIVE_RDBMS_SOURCE_TOTAL_NUM_BY_QUERY,

        FULL_RDBMS_DATABASE,
        RDBMS_DATABASE_BY_SOURCE,
        RDBMS_DATABASE_TOTAL_BY_SOURCE,
        ACTIVE_FULL_RDBMS_DATABASE,
        ACTIVE_RDBMS_DATABASE_BY_SOURCE,
        ACTIVE_RDBMS_DATABASE_TOTAL_BY_SOURCE,

        FULL_RDBMS_TABLE,
        RDBMS_TABLE_BY_DB,
        RDBMS_TABLE_TOTAL_BY_DB,
        ACTIVE_FULL_RDBMS_TABLE,
        ACTIVE_RDBMS_TABLE_BY_DB,
        ACTIVE_RDBMS_TABLE_TOTAL_BY_DB,

        FULL_RDBMS_DB_SOURCE,
        RDBMS_DB_SOURCE_BY_QUERY,
        RDBMS_DB_COUNT_BY_QUERY,
        ACTIVE_FULL_RDBMS_DB_SOURCE,
        ACTIVE_RDBMS_DB_SOURCE_BY_QUERY,
        ACTIVE_RDBMS_DB_COUNT_BY_QUERY,

        FULL_RDBMS_TABLE_DB_SOURCE,
        RDBMS_TABLE_DB_SOURCE_BY_QUERY,
        RDBMS_TABLE_COUNT_BY_QUERY,
        ACTIVE_FULL_RDBMS_TABLE_DB_SOURCE,
        ACTIVE_RDBMS_TABLE_DB_SOURCE_BY_QUERY,
        ACTIVE_RDBMS_TABLE_COUNT_BY_QUERY,

        FULL_RDBMS_COLUMN_TABLE_DB_SOURCE,
        RDBMS_COLUMN_TABLE_DB_SOURCE_BY_QUERY,
        RDBMS_COLUMN_COUNT_BY_QUERY
    }
}