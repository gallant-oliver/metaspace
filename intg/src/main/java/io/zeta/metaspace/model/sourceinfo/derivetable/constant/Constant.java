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

    private static final String HIVE = "HIVE";
    private static final String ORACLE = "ORACLE";
    private static final String MYSQL = "MYSQL";


    // 表英文名和字段英文名校验规则：包含英文字母、下划线和数字，不能以下划线开头
    public static final String PATTERN = "^(?!_)[a-zA-Z0-9_]+$";

    // hive的数据类型
    private static final List<String> HIVE_DATA_TYPE = Arrays.asList("boolean", "tinyint", "smallint", "int", "bigint", "float", "double", "decimal",
            "string", "varchar", "char", "binary", "timestamp", "date", "array", "map", "struct");

    // oracle数据类型
    private static final List<String> ORACLE_DATA_TYPE = Arrays.asList("char", "nchar", "varchar", "varchar2", "nvarchar2", "number", "integer",
            "binary_float", "binary_double", "float", "date", "timestamp", "timestamp with time zone", "timestamp with local time zone",
            "interval year to month", "interval day to second", "blob", "clob", "nclob", "bfile", "long", "raw", "long raw");

    // MYSQL数据类型
    private static final List<String> MYSQL_DATA_TYPE = Arrays.asList("bigint", "binary", "bit", "blob", "char", "date", "datetime", "decimal", "double",
            "enum", "float", "geometry", "int", "integer", "json", "linestring", "longblob", "longtext", "mediumblob", "mediumint",
            "mediumtext", "multilinestring", "multipoint", "multipolygon", "numeric", "point", "polygon", "real", "set", "smallint", "time", "timestamp",
            "tinyblob", "tinyint", "tinytext", "varbinary", "varchar", "year");


    public static final Map<String, List<String>> DATA_TYPE_MAP = new HashMap<String, List<String>>() {
        {
            put(HIVE, HIVE_DATA_TYPE);
            put(ORACLE, ORACLE_DATA_TYPE);
            put(MYSQL, MYSQL_DATA_TYPE);
        }
    };

    // hive必须要加长度的数据类型
    private static final List<String> HIVE_DATA_LENGTH_TYPE = Arrays.asList("varchar", "char");

    // oracle必须要加长度的数据类型
    private static final List<String> ORACLE_DATA_LENGTH_TYPE = Arrays.asList("varchar", "varchar2", "nvarchar2", "raw");

    // MYSQL必须要加长度的数据类型
    private static final List<String> MYSQL_DATA_LENGTH_TYPE = Arrays.asList("varbinary", "varchar");

    public static final Map<String, List<String>> DATA_LENGTH_TYPE_MAP = new HashMap<String, List<String>>() {
        {
            put(HIVE, HIVE_DATA_LENGTH_TYPE);
            put(ORACLE, ORACLE_DATA_LENGTH_TYPE);
            put(MYSQL, MYSQL_DATA_LENGTH_TYPE);
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
            put("char", "'c'");
            put("nchar", "'c'");
            put("varchar", "'varchar_value'");
            put("varchar2", "'varchar2_value'");
            put("nvarchar2", "'nvarchar2_value'");
            put("number", "10");
            put("integer", "10");
            put("binary_float", "3.14");
            put("binary_double", "3.14");
            put("float", "3.14");
            put("date", "to_date('2014-02-14','yyyy-mm-dd')");
            put("timestamp", "to_timestamp('2021-07-25 12:00:00','yyyy-mm-dd hh24:mi:ss')");
            put("timestamp with time zone", "TO_TIMESTAMP_TZ('2006-12-14 19:45:09.9003 -10:00', 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM')");
            put("timestamp with local time zone", "TO_TIMESTAMP_TZ('2006-12-01 23:12:56.788 -12:44', 'YYYY-MM-DD HH24:MI:SS.FF TZH:TZM')");
            put("interval year to month", "interval '1' year");
            put("interval day to second", "INTERVAL '3' DAY");
            put("blob", "rawtohex('blobvalue')");
            put("clob", "'clobvalue'");
            put("nclob", "'nclobvalue'");
            put("long", "'longvalue'");
            put("bfile", "BFILENAME ('tmpdir', 'tmptest')");
            put("raw", "rawtohex('rawvalue')");
            put("long raw", "rawtohex('longrowvalue')");
        }
    };

    // 生成DML时候根据类型替换的数据
    private static final Map<String, Object> MYSQL_DATA_TYPE_TO_DATA = new HashMap<String, Object>() {
        {
            put("bigint", "256");
            put("binary", "UNHEX('A')");
            put("bit", "0");
            put("blob", "'c'");
            put("char", "'s'");
            put("date", "'2021-05-12'");
            put("datetime", "'2021-05-12 12:00:00'");
            put("decimal", "125");
            put("double", "125.1314");
            put("enum", "'str1'");
            put("float", "125.1314");
            put("geometry", "ST_GeometryFromText('point(108.9498710632 34.2588125935)')");
            put("int", "5");
            put("integer", "10");
            put("json", "'{\"key1\":\"value1\",\"key2\":2}'");
            put("linestring", "ST_GeometryFromText('LINESTRING(121.342423 31.542423,121.345664 31.246790,121.453178 31.456862)')");
            put("longblob", "'longblob'");
            put("longtext", "'longtext'");
            put("mediumblob", "'mediumblob'");
            put("mediumint", "156");
            put("mediumtext", "'mediumtext'");
            put("multilinestring", "ST_GeometryFromText('MULTILINESTRING((10 10, 20 20), (15 15, 30 15))')");
            put("multipoint", "ST_GeometryFromText('MULTIPOINT(0 0, 20 20, 60 60)')");
            put("multipolygon", "ST_GeometryFromText('MULTIPOLYGON(((0 0,10 0,10 10,0 10,0 0)),((5 5,7 5,7 7,5 7, 5 5)))')");
            put("numeric", "10");
            put("point", "ST_GeometryFromText('POINT(121.213342 31.234532)')");
            put("polygon", "ST_GeometryFromText('POLYGON((0 0,10 0,10 10,0 10,0 0),(5 5,7 5,7 7,5 7, 5 5))')");
            put("real", "21.123");
            put("set", "'str1'");
            put("smallint", "10");
            put("time", "'10:15:00'");
            put("timestamp", "'2021-05-12 12:00:00'");
            put("tinyblob", "'tinyblob'");
            put("tinyint", "10");
            put("tinytext", "'tinytext'");
            put("varbinary", "UNHEX('A')");
            put("varchar", "'varchar'");
            put("year", "2021");
        }
    };


    public static final Map<String, Map<String, Object>> REPLACE_DATE_MAP = new HashMap<String, Map<String, Object>>() {
        {
            put(HIVE, HIVE_DATA_TYPE_TO_DATA);
            put(ORACLE, ORACLE_DATA_TYPE_TO_DATA);
            put(MYSQL, MYSQL_DATA_TYPE_TO_DATA);
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

    // MYSQL的部分关键字需要替换
    public static final Map<String, String> MYSQL_TYPE_REPLACE = new HashMap<String, String>() {
        {
            put("set", "set('str1','str2')");
            put("enum", "enum('str1','str2')");
        }
    };

    public static final Map<String, Map<String, String>> REPLACE_TYPE_MAP = new HashMap<String, Map<String, String>>() {
        {
            put(HIVE, HIVE_TYPE_REPLACE);
            put(MYSQL, MYSQL_TYPE_REPLACE);
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

}
