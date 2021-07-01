package io.zeta.metaspace.web.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class IndexCounterUtils {

    /**
     * 元数据采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_SUCCESS_COUNT = "metaspace_metadata_task_success_count";

    /**
     * 元数据采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_FAIL_COUNT = "metaspace_metadata_task_fail_count";

    /**
     * sqlserver类型数据源采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_SQLSERVER_SUCCESS_COUNT = "metaspace_metadata_task_sqlserver_success_count";

    /**
     * db2类型数据源采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_DB2_SUCCESS_COUNT = "metaspace_metadata_task_db2_success_count";

    /**
     * oracle类型数据源采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_ORACLE_SUCCESS_COUNT = "metaspace_metadata_task_oracle_success_count";

    /**
     * postgresql类型数据源采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_POSTGRESQL_SUCCESS_COUNT = "metaspace_metadata_task_postgresql_success_count";

    /**
     * mysql类型数据源采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_MYSQL_SUCCESS_COUNT = "metaspace_metadata_task_mysql_success_count";

    /**
     * hive类型数据源采集任务成功数
     */
    public static final String METASPACE_METADATA_TASK_HIVE_SUCCESS_COUNT = "metaspace_metadata_task_hive_success_count";

    /**
     * sqlserver类型数据源采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_SQLSERVER_FAIL_COUNT = "metaspace_metadata_task_sqlserver_fail_count";

    /**
     * db2类型数据源采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_DB2_FAIL_COUNT = "metaspace_metadata_task_db2_fail_count";

    /**
     * oracle类型数据源采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_ORACLE_FAIL_COUNT = "metaspace_metadata_task_oracle_fail_count";

    /**
     * postgresql类型数据源采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_POSTGRESQL_FAIL_COUNT = "metaspace_metadata_task_postgresql_fail_count";

    /**
     * mysql类型数据源采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_MYSQL_FAIL_COUNT = "metaspace_metadata_task_mysql_fail_count";

    /**
     * hive类型数据源采集任务失败数
     */
    public static final String METASPACE_METADATA_TASK_HIVE_FAIL_COUNT = "metaspace_metadata_task_hive_fail_count";

    /**
     * 数据质量任务成功数
     */
    public static final String METASPACE_QUALITY_TASK_SUCCESS_COUNT = "metaspace_quality_task_success_count";

    /**
     * 数据质量任务失败数
     */
    public static final String METASPACE_QUALITY_TASK_FAIL_COUNT = "metaspace_quality_task_fail_count";

    /**
     * 元数据检索次数
     */
    public static final String METASPACE_METADATA_SEARCH_COUNT = "metaspace_metadata_search_count";

    /**
     * 元数据检索时长
     */
    public static final String METASPACE_METADATA_SEARCH_DURATION = "metaspace_metadata_search_duration";

    /**
     * 数据血缘关系检索次数
     */
    public static final String METASPACE_DATA_KINSHIP_SEARCH_COUNT = "metaspace_data_kinship_search_count";

    /**
     * 数据血缘关系检索时长
     */
    public static final String METASPACE_DATA_KINSHIP_SEARCH_DURATION = "metaspace_data_kinship_search_duration";

    public final static Map<String, AtomicLong> INDEX_MAP = new HashMap<String, AtomicLong>() {
        private static final long serialVersionUID = 1L;

        {
            put(METASPACE_METADATA_TASK_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_SQLSERVER_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_DB2_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_ORACLE_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_POSTGRESQL_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_MYSQL_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_HIVE_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_SQLSERVER_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_DB2_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_ORACLE_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_POSTGRESQL_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_MYSQL_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_TASK_HIVE_FAIL_COUNT, new AtomicLong());
            put(METASPACE_QUALITY_TASK_SUCCESS_COUNT, new AtomicLong());
            put(METASPACE_QUALITY_TASK_FAIL_COUNT, new AtomicLong());
            put(METASPACE_METADATA_SEARCH_COUNT, new AtomicLong());
            put(METASPACE_METADATA_SEARCH_DURATION, new AtomicLong());
            put(METASPACE_DATA_KINSHIP_SEARCH_COUNT, new AtomicLong());
            put(METASPACE_DATA_KINSHIP_SEARCH_DURATION, new AtomicLong());
        }
    };

    public final static Map<String, String> SOURCE_TYPE_SUCCESS_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("MYSQL", METASPACE_METADATA_TASK_MYSQL_SUCCESS_COUNT);
            put("POSTGRESQL", METASPACE_METADATA_TASK_POSTGRESQL_SUCCESS_COUNT);
            put("HIVE", METASPACE_METADATA_TASK_HIVE_SUCCESS_COUNT);
            put("ORACLE", METASPACE_METADATA_TASK_ORACLE_SUCCESS_COUNT);
            put("DB2", METASPACE_METADATA_TASK_DB2_SUCCESS_COUNT);
            put("SQLSERVER", METASPACE_METADATA_TASK_SQLSERVER_SUCCESS_COUNT);
        }
    };

    public final static Map<String, String> SOURCE_TYPE_FAIL_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;

        {
            put("MYSQL", METASPACE_METADATA_TASK_MYSQL_FAIL_COUNT);
            put("POSTGRESQL", METASPACE_METADATA_TASK_POSTGRESQL_FAIL_COUNT);
            put("HIVE", METASPACE_METADATA_TASK_HIVE_FAIL_COUNT);
            put("ORACLE", METASPACE_METADATA_TASK_ORACLE_FAIL_COUNT);
            put("DB2", METASPACE_METADATA_TASK_DB2_FAIL_COUNT);
            put("SQLSERVER", METASPACE_METADATA_TASK_SQLSERVER_FAIL_COUNT);
        }
    };
}
