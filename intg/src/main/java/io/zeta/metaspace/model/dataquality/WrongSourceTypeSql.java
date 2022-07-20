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
 * @date 2019/1/17 14:39
 */
package io.zeta.metaspace.model.dataquality;

/**
 * 数据质量-异常数据sql枚举
 */
public enum WrongSourceTypeSql {

    // ORACLE类型获取异常数据sql
    ORACEL_WRONG_SQL("ORACLE", "select table_name from all_tab_comments c right join all_tables t on c.table_name = t.table_name and c.owner = t.owner and c.table_type = 'TABLE' where (c.comments is null or c.comments = '') and t.owner = '%s'"),

    // MYSQL类型获取异常数据sql
    MYSQL_WRONG_SQL("MYSQL", "select table_name from information_schema.tables where (table_comment is null or table_comment = '') and table_schema = '%s'"),

    // PGSQL类型获取异常数据sql
    POSTGRESQL_WRONG_SQL("POSTGRESQL", "select t.relname as table_name from (select relname,cast(obj_description(relfilenode,'pg_class') as varchar) as comment from pg_class c where  relkind = 'r' and relname not like 'pg_%' and relname not like 'sql_%' order by relname) t where t.comment is null or t.comment = ''"),

    // DB2类型获取异常数据sql
    DB2_WRONG_SQL("DB2", "select varchar(tabname,50) as table_name from syscat.tables where (remarks is null or remarks = '') and tabschema = '%s' and type = 'T'"),

    // SQLSERVER类型获取异常数据sql
    SQLSERVER_WRONG_SQL("SQLSERVER", "select obj.name as table_name from sys.objects obj left join [sys].[extended_properties] se on obj.object_id = se.major_id and se.minor_id = 0 join sys.schemas s on obj.schema_id=s.schema_id where obj.type = 'u' and (se.value is null or se.value = '') and s.name = '%s'"),

    // OSCAR类型获取异常数据sql
    OSCAR_WRONG_SQL("OSCAR", "select distinct table_name from all_tab_comments where (comments is null or comments = '') and table_name in (select varchar(c.relname) from sys_class c where c.relkind = 'r' and c.relnamespace = (select oid from sys_namespace n where n.nspname = '%s')) and owner = (select schem_owner from schemata where schem_name = '%s')");



    // 数据源类型
    public String sourceType;

    // 查询异常数据sql
    public String wrongSql;

    WrongSourceTypeSql(String sourceType, String wrongSql) {
        this.sourceType = sourceType;
        this.wrongSql = wrongSql;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getWrongSql() {
        return wrongSql;
    }

    public void setWrongSql(String wrongSql) {
        this.wrongSql = wrongSql;
    }

    public static WrongSourceTypeSql getEnumBySourceType(String sourceType) {
        WrongSourceTypeSql defaultTask = WrongSourceTypeSql.ORACEL_WRONG_SQL;
        for (WrongSourceTypeSql item : WrongSourceTypeSql.values()) {
            if (item.sourceType.equals(sourceType)) {
                return item;
            }
        }
        return defaultTask;
    }

    public static String getWrongSqlBySourceType(String sourceType) {
        return getEnumBySourceType(sourceType).wrongSql;
    }
}
