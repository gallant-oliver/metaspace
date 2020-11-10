package io.zeta.metaspace.utils;

import com.healthmarketscience.sqlbuilder.CustomCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.share.DataType;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * 用于数据分享拼接 API 的查询sql
 */
public class SqlBuilderUtils {

    public static String getFilterConditionStr(AdapterTransformer transformer, DataType dataType, String column, List<String> values) {
        column = transformer.caseSensitive(column);
        if (DataType.CLOB == dataType) {
            return transformer.getFilterConditionStr(column, values);
        } else {
            return getFilterConditionStr(column, values);
        }
    }

    public static String getFilterConditionStr(String column, List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        } else if (values.size() > 1) {
            return MessageFormat.format("{0} in ({1})", column, String.join(",", values));
        } else {
            return MessageFormat.format("{0} = {1}", column, values.get(0));
        }
    }

    public static String buildQuerySql(AdapterTransformer transformer, String schemaName, String tableName, String queryColumn, String filterCondition, String sortSql, long limit, long offset) {
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new CustomSql(queryColumn))
                .addCustomFromTable(new CustomSql(transformer.caseSensitive(schemaName) + "." + transformer.caseSensitive(tableName)));

        query = transformer.addTotalCount(query);

        if (StringUtils.isNotEmpty(filterCondition)) {
            query.addCondition(new CustomCondition(filterCondition));
        }

        query = transformer.addOrderBy(query, sortSql);

        return transformer.addLimit(query, limit, offset).toString();
    }
}
