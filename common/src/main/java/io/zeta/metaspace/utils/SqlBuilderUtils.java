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

    /**
     * 获取filter语句
     * @param transformer 对应数据源的AdapterTransformer
     * @param dataType 列类型
     * @param column 列名
     * @param expressionType 过滤条件
     * @param values 校验值
     * @return
     */
    public static String getFilterConditionStr(AdapterTransformer transformer, DataType dataType, String column,String expressionType, List<String> values) {
        column = transformer.caseSensitive(column);
        if (DataType.CLOB == dataType) {
            return transformer.getFilterConditionStr(column, values,expressionType);
        } else {
            return getFilterConditionStr(column, values,expressionType);
        }
    }

    /**
     * 获取filter语句
     * @param column 过滤的列名
     * @param values 过滤的值
     * @param expressionType 过滤条件
     * @return
     */
    public static String getFilterConditionStr(String column, List<String> values,String expressionType) {
        if (values == null || values.isEmpty()) {
            return null;
        } else if (values.size() > 1) {
            return MessageFormat.format("{0} in ({1})", column, String.join(",", values));
        } else {
            return MessageFormat.format("{0} {1} {2}", column,expressionType, values.get(0));
        }
    }

    public static String buildQuerySql(AdapterTransformer transformer, String schemaName, String tableName, String queryColumn, String filterCondition, String sortSql, long limit, long offset) {
        SelectQuery query = new SelectQuery()
                .addCustomColumns(new CustomSql(queryColumn))
                .addCustomFromTable(new CustomSql(transformer.caseSensitive(schemaName) + "." + transformer.caseSensitive(tableName)));

        // 先添加filters语句在进行totalCount统计，确保正确的统计totalCount
        if (StringUtils.isNotEmpty(filterCondition)) {
            query.addCondition(new CustomCondition(filterCondition));
        }
        query = transformer.addTotalCount(query);

        query = transformer.addOrderBy(query, sortSql);

        return transformer.addLimit(query, limit, offset).toString();
    }
}
