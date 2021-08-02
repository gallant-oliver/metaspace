package io.zeta.metaspace.model.sourceinfo.derivetable.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName Keyword
 * @Descroption TODO
 * @Author Lvmengliang
 * @Date 2021/7/16 11:49
 * @Version 1.0
 */
public class Constant {

    // 表英文名和字段英文名校验规则：包含英文字母、下划线和数字，不能以下划线开头
    public static final String pattern = "^(?!_)[a-zA-Z0-9_]+$";

    // hive的数据类型
    private static final List<String> HIVE_DATA_TYPE = Arrays.asList("boolean", "tinyint", "smallint", "int", "bigint", "float", "double", "decimal",
            "string", "varchar", "char", "binary", "timestamp", "date", "array", "map", "struct");

    // oracle数据类型
    private static final List<String> ORACLE_DATA_TYPE = Arrays.asList("char", "nchar", "varchar", "varchar2", "nvarchar2 ", "number", "integer",
            "binary_float", "binary_double", "float", "date", "timestamp", "timestamp with time zone", "timestamp with local time zone",
            "interval year to month", "interval day to second", "blob", "clob", "nclob", "bfile", "long", "raw", "long raw");


    public static final Map<String, List<String>> DATA_TYPE_MAP = new HashMap<String, List<String>>() {
        {
            put("HIVE", HIVE_DATA_TYPE);
            put("ORACLE", ORACLE_DATA_TYPE);
        }
    };

    // hive必须要加长度的数据类型
    private static final List<String> HIVE_DATA_LENGTH_TYPE = Arrays.asList("varchar", "char");

    // oracle必须要加长度的数据类型
    private static final List<String> ORACLE_DATA_LENGTH_TYPE = Arrays.asList("varchar", "varchar2", "nvarchar2 ", "raw");

    public static final Map<String, List<String>> DATA_LENGTH_TYPE_MAP = new HashMap<String, List<String>>() {
        {
            put("HIVE", HIVE_DATA_LENGTH_TYPE);
            put("ORACLE", ORACLE_DATA_LENGTH_TYPE);
        }
    };

    // 生成DML时候根据类型替换的数据
    private static final Map<String, Object> HIVE_DATA_TYPE_TO_DATA = new HashMap<String, Object>() {
        {
            put("boolean", "true");
            put("tinyint", "1");
            put("smallint", "2");
            put("int", "10");
            put("bigint", "100");
            put("float", "3.14159");
            put("double", "3.14159");
            put("decimal", "10.2");
            put("string", "'string_value'");
            put("varchar", "'varchar_value'");
            put("char", "'char_value'");
            put("binary", "'binary_value'");
            put("timestamp", "'2021-05-29 15:00:00'");
            put("date", "'2021-07-29'");
            put("array", "array('arr_str1','arr_str2')");
            put("map", "map('key1','value1','key2','value2')");
            put("struct", "named_struct('name','username','age',15)");
        }
    };

    // 生成DML时候根据类型替换的数据
    private static final Map<String, Object> ORACLE_DATA_TYPE_TO_DATA = new HashMap<String, Object>() {
        {
            put("char", "'c'" );
            put("nchar", "'c'" );
            put("varchar", "'varchar_value'" );
            put("varchar2", "'varchar2_value'" );
            put("nvarchar2 ", "'nvarchar2_value'" );
            put("number", "10" );
            put("integer", "10" );
            put("binary_float", "3.14" );
            put("binary_double", "3.14" );
            put("float", "3.14" );
            put("date", "to_date('2014-02-14','yyyy-mm-dd')" );
            put("timestamp", "to_timestamp('2021-07-25 12:00:00','yyyy-mm-dd hh24:mi:ss')" );
            put("timestamp with time zone", "TO_TIMESTAMP_TZ('2006-12-14 19:45:09.9003 -10:00', 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM')" );
            put("timestamp with local time zone", "TO_TIMESTAMP_TZ('2006-12-01 23:12:56.788 -12:44', 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM')" );
            put("interval year to month", "interval '1' year" );
            put("interval day to second", "INTERVAL '3' DAY" );
            put("blob", "rawtohex('blobvalue')" );
            put("clob", "'clobvalue'" );
            put("nclob", "'nclobvalue'" );
            put("long", "'longvalue'" );
            put("bfile", "BFILENAME ('tmpdir', 'tmptest')" );
            put("raw", "rawtohex('rawvalue')" );
            put("long raw", "rawtohex('longrowvalue')" );
        }
    };


    public static final Map<String, Map<String, Object>> REPLACE_DATE_MAP = new HashMap<String, Map<String, Object>>() {
        {
            put("HIVE", HIVE_DATA_TYPE_TO_DATA);
            put("ORACLE", ORACLE_DATA_TYPE_TO_DATA);
        }
    };

    // hive的部分关键字需要替换
    public static final Map<String, String> HIVE_TYPE_REPLACE = new HashMap<String, String>() {
        {
            put("array", "array<string>");
            put("map", "map<string,string>");
            put("struct", "struct<field:string>");
        }
    };


    // hive关键字
    public static final List<String> HIVE_KEYWORD = Arrays.asList("ABORT", "ADD", "ADMIN", "AFTER", "ALTER", "ANALYZE", "AND", "ARCHIVE", "ARRAY", "AS", "ASC",
            "AUTHORIZATION", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BOOLEAN", "BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", "CASCADE", "CASE", "CAST", "CHANGE",
            "CHAR", "CLUSTER", "CLUSTERED", "CLUSTERSTATUS", "COLLECTION", "COLUMN", "COLUMNS", "COMMENT", "COMMIT", "COMPACT", "COMPACTIONS", "COMPUTE",
            "CONCATENATE", "CONF", "CONSTRAINT", "CONTINUE", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_TIMESTAMP", "CURSOR", "DATA",
            "DATABASE", "DATABASES", "DATE", "DATETIME", "DAY", "DAYS", "DBPROPERTIES", "DECIMAL", "DEFERRED", "DEFINED", "DELETE", "DELIMITED", "DEPENDENCY",
            "DESC", "DESCRIBE", "DETAIL", "DIRECTORIES", "DIRECTORY", "DISABLE", "DISTINCT", "DISTRIBUTE", "DOUBLE", "DOW", "DROP", "ELEM_TYPE", "ELSE",
            "ENABLE", "END", "ESCAPED", "EXCHANGE", "EXCLUSIVE", "EXISTS", "EXPLAIN", "EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIELDS",
            "FILE", "FILEFORMAT", "FIRST", "FLOAT", "FLOOR", "FOLLOWING", "FOR", "FOREIGN", "FORMAT", "FORMATTED", "FROM", "FULL", "FUNCTION", "FUNCTIONS",
            "GRANT", "GROUP", "GROUPING", "HAVING", "HOLD_DDLTIME", "HOUR", "HOURS", "IDXPROPERTIES", "IF", "IGNORE", "IMPORT", "IN", "INDEX", "INDEXES",
            "INNER", "INPATH", "INPUTDRIVER", "INPUTFORMAT", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "ITEMS", "JAR",
            "JOIN", "KEY", "KEY_TYPE", "KEYS", "LAST", "LATERAL", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LINES", "LOAD", "LOCAL", "LOCATION", "LOCK", "LOCKS",
            "LOGICAL", "LONG", "MACRO", "MAP", "MAPJOIN", "MATERIALIZED", "METADATA", "MINUS", "MINUTE", "MINUTES", "MONTH", "MONTHS", "MORE", "MSCK", "NO_DROP",
            "NONE", "NORELY", "NOSCAN", "NOT", "NOVALIDATE", "NULL", "NULLS", "NUMERIC", "OF", "OFFLINE", "OFFSET", "ON", "ONLY", "OPERATOR", "OPTION", "OR", "ORDER",
            "OUT", "OUTER", "OUTPUTDRIVER", "OUTPUTFORMAT", "OVER", "OVERWRITE", "OWNER", "PARTIALSCAN", "PARTITION", "PARTITIONED", "PARTITIONS", "PERCENT",
            "PLUS", "PRECEDING", "PRECISION", "PRESERVE", "PRETTY", "PRIMARY", "PRINCIPALS", "PROCEDURE", "PROTECTION", "PURGE", "QUARTER", "RANGE", "READ",
            "READONLY", "READS", "REBUILD", "RECORDREADER", "RECORDWRITER", "REDUCE", "REGEXP", "REGEXP", "REGEXP", "RELOAD", "RELY", "RENAME", "REPAIR",
            "REPLACE", "REPLICATION", "RESTRICT", "REVOKE", "REWRITE", "RIGHT", "RLIKE", "RLIKE", "ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", "ROWS",
            "SCHEMA", "SCHEMAS", "SECOND", "SELECT", "SEMI", "SERDE", "SERDEPROPERTIES", "SERVER", "SET", "SETS", "SHARED", "SHOW", "SHOW_DATABASE", "SKEWED",
            "SMALLINT", "SNAPSHOT", "SORT", "SORTED", "SSL", "STATISTICS", "STORED", "STREAMTABLE", "STRING", "STRUCT", "SUMMARY", "SYNC", "TABLE", "TABLES",
            "TABLESAMPLE", "TBLPROPERTIES", "TEMPORARY", "TERMINATED", "THEN", "TIME", "TIMESTAMP", "TIMESTAMPTZ", "TINYINT", "TO", "TOUCH", "TRANSACTION",
            "TRANSACTIONS", "TRANSFORM", "TRIGGER", "TRUE", "TRUNCATE", "UNARCHIVE", "UNBOUNDED", "UNDO", "UNION", "UNIONTYPE", "UNIQUEJOIN", "UNLOCK",
            "UNSET", "UNSIGNED", "UPDATE", "URI", "USE", "USER", "USING", "UTC", "UTC_TMESTAMP", "UTCTIMESTAMP", "VALUE_TYPE", "VALUES", "VARCHAR",
            "VECTORIZATION", "VIEW", "WEEK", "WEEKS", "WHEN", "WHERE", "WHILE", "WINDOW", "WORK", "YEARALL", "YEARS");

    // oracle关键字
    public static final List<String> ORACLE_KEYWORD = Arrays.asList("access", "accessed", "account", "activate", "add", "admin", "administer", "administrator",
            "advise", "advisor", "after", "algorithm", "alias", "all", "allocate", "allow", "all_rows", "alter", "always", "analyze", "ancillary", "and", "and_equal",
            "antijoin", "any", "append", "apply", "archive", "archivelog", "array", "as", "asc", "associate", "at", "attribute", "attributes", "audit", "authenticated",
            "authentication", "authid", "authorization", "auto", "autoallocate", "autoextend", "automatic", "availability", "backup", "become", "before", "begin",
            "behalf", "between", "bfile", "bigfile", "binary_double", "binary_double_infinity", "binary_double_nan", "binary_float", "binary_float_infinity",
            "binary_float_nan", "binding", "bitmap", "bits", "blob", "block", "blocks", "blocksize", "block_range", "body", "both", "bound", "broadcast", "buffer",
            "buffer_cache", "buffer_pool", "build", "bulk", "by", "bypass_recursive_check", "bypass_ujvc", "byte", "cache", "cache_cb", "cache_instances",
            "cache_temp_table", "call", "cancel", "cardinality", "cascade", "case", "cast", "category", "certificate", "cfile", "chained", "change", "char",
            "character", "char_cs", "check", "checkpoint", "child", "choose", "chunk", "civ_gb", "class", "clear", "clob", "clone", "close", "close_cached_open_cursors",
            "cluster", "clustering_factor", "coalesce", "coarse", "collect", "collections_get_refs", "column", "columns", "column_stats", "column_value",
            "comment", "commit", "committed", "compact", "compatibility", "compile", "complete", "composite_limit", "compress", "compute", "conforming",
            "connect", "connect_by_iscycle", "connect_by_isleaf", "connect_by_root", "connect_time", "consider", "consistent", "constraint", "constraints",
            "container", "content", "contents", "context", "continue", "controlfile", "convert", "corruption", "cost", "cpu_costing", "cpu_per_call", "cpu_per_session",
            "create", "create_stored_outlines", "cross", "cube", "cube_gb", "current", "current_date", "current_schema", "current_time", "current_timestamp", "current_user",
            "cursor", "cursor_sharing_exact", "cursor_specific_segment", "cycle", "dangling", "data", "database", "datafile", "datafiles", "dataobjno", "date",
            "date_mode", "day", "dba", "dba_recyclebin", "dbtimezone", "ddl", "deallocate", "debug", "dec", "decimal", "declare", "decrement", "default", "deferrable",
            "deferred", "defined", "definer", "degree", "delay", "delete", "demand", "dense_rank", "deref", "deref_no_rewrite", "desc", "detached", "determines",
            "dictionary", "dimension", "directory", "disable", "disassociate", "disconnect", "disk", "diskgroup", "disks", "dismount", "distinct", "distinguished",
            "distributed", "dml", "dml_update", "document", "domain_index_no_sort", "domain_index_sort", "double", "downgrade", "driving_site", "drop", "dump",
            "dynamic", "dynamic_sampling", "dynamic_sampling_est_cdn", "each", "element", "else", "empty", "enable", "encrypted", "encryption", "end", "enforce",
            "enforced", "entry", "error", "error_on_overlap_time", "escape", "estimate", "events", "except", "exceptions", "exchange", "excluding", "exclusive",
            "execute", "exempt", "exists", "expand_gset_to_union", "expire", "explain", "explosion", "export", "expr_corr_check", "extend", "extends", "extent",
            "extents", "external", "externally", "extract", "fact", "failed", "failed_login_attempts", "failgroup", "false", "fast", "fbtscan", "fic_civ", "fic_piv",
            "file", "filter", "final", "fine", "finish", "first", "first_rows", "flagger", "flashback", "float", "flob", "flush", "following", "for", "force",
            "force_xml_query_rewrite", "foreign", "freelist", "freelists", "freepools", "fresh", "from", "full", "function", "functions", "gather_plan_statistics",
            "gby_conc_rollup", "generated", "global", "globally", "global_name", "global_topic_enabled", "grant", "group", "grouping", "groups", "group_by", "guarantee",
            "guaranteed", "guard", "hash", "hashkeys", "hash_aj", "hash_sj", "having", "header", "heap", "hierarchy", "high", "hintset_begin", "hintset_end", "hour",
            "hwm_brokered", "id", "identified", "identifier", "identity", "idgenerators", "idle_time", "if", "ignore", "ignore_on_clause", "ignore_optim_embedded_hints",
            "ignore_where_clause", "immediate", "import", "in", "include_version", "including", "increment", "incremental", "index", "indexed", "indexes", "indextype",
            "indextypes", "index_asc", "index_combine", "index_desc", "index_ffs", "index_filter", "index_join", "index_rows", "index_rrs", "index_scan", "index_skip_scan",
            "index_ss", "index_ss_asc", "index_ss_desc", "index_stats", "indicator", "infinite", "informational", "initial", "initialized", "initially", "initrans", "inline",
            "inner", "insert", "instance", "instances", "instantiable", "instantly", "instead", "int", "integer", "integrity", "intermediate", "internal_convert", "internal_use",
            "interpreted", "intersect", "interval", "into", "invalidate", "in_memory_metadata", "is", "isolation", "isolation_level", "iterate", "iteration_number", "java",
            "job", "join", "keep", "kerberos", "key", "keyfile", "keys", "keysize", "key_length", "kill", "last", "lateral", "layer", "ldap_registration", "ldap_registration_enabled",
            "ldap_reg_sync_interval", "leading", "left", "length", "less", "level", "levels", "library", "like", "like2", "like4", "likec", "like_expand", "limit", "link", "list", "lob",
            "local", "localtime", "localtimestamp", "local_indexes", "location", "locator", "lock", "locked", "log", "logfile", "logging", "logical", "logical_reads_per_call",
            "logical_reads_per_session", "logoff", "logon", "long", "main", "manage", "managed", "management", "manual", "mapping", "master", "matched", "materialize",
            "materialized", "max", "maxarchlogs", "maxdatafiles", "maxextents", "maximize", "maxinstances", "maxlogfiles", "maxloghistory", "maxlogmembers", "maxsize",
            "maxtrans", "maxvalue", "measures", "member", "memory", "merge", "merge_aj", "merge_const_on", "merge_sj", "method", "migrate", "min", "minextents", "minimize", "minimum",
            "minus", "minute", "minvalue", "mirror", "mlslabel", "mode", "model", "model_dontverify_uniqueness", "model_min_analysis", "model_no_analysis", "model_pby", "model_push_ref",
            "modify", "monitoring", "month", "mount", "move", "movement", "multiset", "mv_merge", "name", "named", "nan", "national", "native", "natural", "nav", "nchar", "nchar_cs", "nclob",
            "needed", "nested", "nested_table_fast_insert", "nested_table_get_refs", "nested_table_id", "nested_table_set_refs", "nested_table_set_setid", "network", "never", "new",
            "next", "nls_calendar", "nls_characterset", "nls_comp", "nls_currency", "nls_date_format", "nls_date_language", "nls_iso_currency", "nls_lang",
            "nls_language", "nls_length_semantics", "nls_nchar_conv_excp", "nls_numeric_characters", "nls_sort", "nls_special_chars", "nls_territory", "nl_aj", "nl_sj", "no", "noappend",
            "noarchivelog", "noaudit", "nocache", "nocompress", "nocpu_costing", "nocycle", "nodelay", "noforce", "noguarantee", "nologging", "nomapping", "nomaxvalue", "nominimize",
            "nominvalue", "nomonitoring", "none", "noorder", "nooverride", "noparallel", "noparallel_index", "norely", "norepair", "noresetlogs", "noreverse",
            "norewrite", "normal", "norowdependencies", "nosegment", "nosort", "nostrict", "noswitch", "not", "nothing", "novalidate", "nowait", "no_access",
            "no_basetable_multimv_rewrite", "no_buffer", "no_cpu_costing", "no_expand", "no_expand_gset_to_union", "no_fact", "no_filtering", "no_index", "no_index_ffs",
            "no_index_ss", "no_merge", "no_model_push_ref", "no_monitoring", "no_multimv_rewrite", "no_order_rollups", "no_parallel", "no_parallel_index", "no_partial_commit",
            "no_prune_gsets", "no_push_pred", "no_push_subq", "no_qkn_buff", "no_query_transformation", "no_ref_cascade", "no_rewrite", "no_semijoin", "no_set_to_join",
            "no_star_transformation", "no_stats_gsets", "no_swap_join_inputs", "no_trigger", "no_unnest", "no_use_hash", "no_use_merge",
            "no_use_nl", "no_xml_query_rewrite", "null", "nulls", "number", "numeric", "nvarchar2", "object", "objno", "objno_reuse", "of", "off", "offline", "oid",
            "oidindex", "old", "on", "online", "only", "opaque", "opaque_transform", "opaque_xcanonical", "opcode", "open", "operator", "optimal", "optimizer_features_enable",
            "optimizer_goal", "option", "opt_estimate", "or", "ora_rowscn", "order", "ordered", "ordered_predicates", "organization", "or_expand", "outer", "outline",
            "out_of_line", "over", "overflow", "overflow_nomove", "overlaps", "own", "package", "packages", "parallel", "parallel_index", "parameters", "parent", "parity",
            "partially", "partition", "partitions", "partition_hash", "partition_list", "partition_range", "password", "password_grace_time",
            "password_life_time", "password_lock_time", "password_reuse_max", "password_reuse_time", "password_verify_function", "pctfree", "pctincrease",
            "pctthreshold", "pctused", "pctversion", "percent", "performance", "permanent", "pfile", "physical", "piv_gb", "piv_ssf", "plan", "plsql_code_type",
            "plsql_debug", "plsql_optimize_level", "plsql_warnings", "policy", "post_transaction", "power", "pq_distribute", "pq_map", "pq_nomap", "prebuilt",
            "preceding", "precision", "prepare", "present", "preserve", "primary", "prior", "private", "private_sga", "privilege", "privileges", "procedure",
            "profile", "program", "project", "protected", "protection", "public", "purge", "push_pred", "push_subq", "px_granule", "qb_name", "query", "query_block",
            "queue", "queue_curr", "queue_rowp", "quiesce", "quota", "random", "range", "rapidly", "raw", "rba", "read", "reads", "real", "rebalance", "rebuild",
            "records_per_block", "recover", "recoverable", "recovery", "recycle", "recyclebin", "reduced", "redundancy", "ref", "reference",
            "referenced", "references", "referencing", "refresh", "ref_cascade_cursor", "regexp_like", "register", "reject", "rekey", "relational", "rely",
            "remote_mapped", "rename", "repair", "replace", "required", "reset", "resetlogs", "resize", "resolve", "resolver", "resource", "restore_as_intervals",
            "restrict", "restricted", "restrict_all_ref_cons", "resumable", "resume", "retention", "return", "returning", "reuse", "reverse", "revoke", "rewrite",
            "rewrite_or_error", "right", "role", "roles", "rollback", "rollup", "row", "rowdependencies", "rowid", "rownum", "rows", "row_length", "rule", "rules",
            "sample", "savepoint", "save_as_intervals", "sb4", "scale", "scale_rows", "scan", "scan_instances", "scheduler", "schema", "scn", "scn_ascending", "scope", "sd_all",
            "sd_inhibit", "sd_show", "second", "security", "seed", "segment", "seg_block", "seg_file", "select", "selectivity", "semijoin", "semijoin_driver", "sequence",
            "sequenced", "sequential", "serializable", "servererror", "session", "sessions_per_user", "sessiontimezone", "sessiontzname", "session_cached_cursors",
            "set", "sets", "settings", "set_to_join", "severe", "share", "shared", "shared_pool", "shrink", "shutdown", "siblings", "sid", "simple", "single", "singletask",
            "size", "skip", "skip_ext_optimizer", "skip_unq_unusable_idx", "skip_unusable_indexes", "smallfile", "smallint", "snapshot", "some", "sort", "source", "space",
            "specification", "spfile", "split", "spreadsheet", "sql", "sqlldr", "sql_trace", "standby", "star", "start", "startup", "star_transformation",
            "statement_id", "static", "statistics", "stop", "storage", "store", "streams", "strict", "strip", "structure", "submultiset", "subpartition", "subpartitions",
            "subpartition_rel", "substitutable", "successful", "summary", "supplemental", "suspend", "swap_join_inputs", "switch", "switchover", "synonym", "sysaux",
            "sysdate", "sysdba", "sysoper", "system", "systimestamp", "sys_dl_cursor", "sys_fbt_insdel", "sys_op_bitvec", "sys_op_cast", "sys_op_col_present",
            "sys_op_enforce_not_null$", "sys_op_mine_value", "sys_op_noexpand", "sys_op_ntcimg$", "sys_parallel_txn", "sys_rid_order", "table", "tables",
            "tablespace", "tablespace_no", "table_stats", "tabno", "tempfile", "template", "temporary", "test", "than", "the", "then", "thread", "through", "time",
            "timeout", "timestamp", "timezone_abbr", "timezone_hour", "timezone_minute", "timezone_region", "time_zone", "tiv_gb", "tiv_ssf", "to", "toplevel",
            "trace", "tracing", "tracking", "trailing", "transaction", "transitional", "treat", "trigger", "triggers", "true", "truncate", "trusted", "tuning", "tx", "type",
            "types", "tz_offset", "ub2", "uba", "uid", "unarchived", "unbound", "unbounded", "under", "undo", "undrop", "uniform", "union", "unique", "unlimited", "unlock",
            "unnest", "unpacked", "unprotected", "unquiesce", "unrecoverable", "until", "unusable", "unused", "updatable", "update", "updated", "upd_indexes",
            "upd_joinindex", "upgrade", "upsert", "urowid", "usage", "use", "user", "user_defined", "user_recyclebin", "use_anti", "use_concat", "use_hash",
            "use_merge", "use_nl", "use_nl_with_index", "use_private_outlines", "use_semi", "use_stored_outlines", "use_ttt_for_gsets", "use_weak_name_resl",
            "using", "validate", "validation", "value", "values", "varchar", "varchar2", "varray", "varying", "vector_read", "vector_read_trace", "version", "versions",
            "view", "wait", "wellformed", "when", "whenever", "where", "whitespace", "with", "within", "without", "work", "write", "xid", "xmlattributes", "xmlcolattval",
            "xmlelement", "xmlforest", "xmlparse", "xmlschema", "xmltype", "x_dyn_prune", "year", "zone");

}
