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
    ORACEL_WRONG_SQL("ORACLE", "select table_name from all_tab_comments where (comments is null or comments = '') and owner = '%s'"),


    // MYSQL类型获取异常数据sql
    MYSQL_WRONG_SQL("MYSQL", "select table_name from information_schema.tables where (table_comment is null or table_comment = '') and table_schema = '%s'");

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
