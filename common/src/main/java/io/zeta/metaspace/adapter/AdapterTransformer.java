package io.zeta.metaspace.adapter;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgOffsetClause;
import io.zeta.metaspace.utils.SqlBuilderUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public interface AdapterTransformer {


    String TOTAL_FUNCTION = "count(*) over()";
    String TOTAL_COLUMN_ALIAS = "total_rows__";
    String TEMP_COLUMN_RNUM = "TEMP_COLUMN_RNUM";

    Adapter getAdapter();

    /**
     * 默认使用 LIMIT OFFSET 方式分页
     */
    default SelectQuery addLimit(SelectQuery originSQL, long limit, long offset) {
        if (limit>-1){
            originSQL = originSQL.addCustomization(new PgLimitClause(limit));
        }
        if (offset>0){
            originSQL = originSQL.addCustomization(new PgOffsetClause(offset));
        }
        return originSQL;
    }

    default SelectQuery addOrderBy(SelectQuery originSQL, String sortSql) {
        if (StringUtils.isNotEmpty(sortSql)) {
            originSQL.addCustomOrderings(new CustomSql(sortSql));
        }
        return originSQL;
    }

    /**
     * 除了 Mysql 外都使用 count(*) over() 方式支持分页
     */
    default SelectQuery addTotalCount(SelectQuery originSQL) {
        originSQL.addAliasedColumn(new CustomSql(TOTAL_FUNCTION), TOTAL_COLUMN_ALIAS);
        return originSQL;
    }


    default String caseSensitive(String originElement) {
        return originElement;
    }

    default String getFilterConditionStr(String column, List<String> values) {
        return SqlBuilderUtils.getFilterConditionStr(column, values);
    }

    /**
     * 解析 ResultSet 按类型解析值
     */
    default Object convertColumnValue(Object value) {
        return value;
    }
}
