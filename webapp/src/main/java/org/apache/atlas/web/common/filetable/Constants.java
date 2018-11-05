package org.apache.atlas.web.common.filetable;

import org.apache.commons.lang.time.FastDateFormat;

public interface Constants {

    String UPLOAD_CVS = "csv";
    String UPLOAD_XLSX = "xlsx";
    String UPLOAD_XLS = "xls";
    String IMPALA_ERROR_START_TIME = "startTime";
    String IMPALA_ERROR_END_TIME = "endTime";
    String FILE_PREFIX_NAME = "query_result_";
    FastDateFormat FORMATTER = FastDateFormat.getInstance("yyyyMMdd_HHmmssSSS");
    String CLUSTER = "A";
    String MEM_LIMIT_SUFFIX = "mb";
    String MEM_LIMIT_SQL = "SET MEM_LIMIT=%s";
    String ERROR_MEM_OUT = "null";
    String ERROR_OOM = "Memory limit exceeded";
    String MEM_OUT_PATTERN = "SQL state:[\\s|\\S]*,";

    int UPLOAD_OUT_TIME = 1800000;// 上传超时时间，默认30分钟
    int UPLOAD_CLEAN_TIME = 5;// 线程运行间隔，分钟

     int PARQUET_BLOCK_SIZE = 268435456;
    int PARQUET_PAGE_SIZE = 65536;
    int PARQUET_DICTIONARY_PAGE_SIZE = 1048576;
    boolean PARQUET_ENABLE_DICTIONARY = true;
    boolean PARQUET_VALIDATING = false;
    int PREVIEW_SIZE = 100;

    String EVENT_TABLE_NAME = "event_job";
    String EXPORT_TABLE_NAME = "export_job";
    String QUERY_RESULT_PERSISTENCE_TABLE_NAME = "persistence_job";
    String MONITOR_TABLE_NAME = "monitor_table";
    String SCHEDULE_TABLE_NAME = "schedule_job";
    String UPLOAD_TABLE_NAME = "upload_job";
    String TASK_TABLE_NAME = "task";
    String OFFLINE_QUERY_TASK_TABLE_NAME = "offline_query_task";
    CsvFormatPredefined EXPORT_CSV_PREFERENCE = CsvFormatPredefined.COMMA;
    String UPDATE_TIME_COLUMN_NAME = "__update__time__";

    String HIVE_WAREHOUSE_BASE_DIR = "/user/hive/warehouse/";
}
