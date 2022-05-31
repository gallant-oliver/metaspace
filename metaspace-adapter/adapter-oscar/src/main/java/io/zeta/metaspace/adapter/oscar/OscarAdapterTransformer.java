package io.zeta.metaspace.adapter.oscar;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.Subquery;
import io.zeta.metaspace.adapter.AbstractAdapterTransformer;
import io.zeta.metaspace.adapter.Adapter;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class OscarAdapterTransformer extends AbstractAdapterTransformer {
    public OscarAdapterTransformer(Adapter adapter) {
        super(adapter);
    }

    /**
     * 使用 ROWNUM 方式提供 oscar 分页
     */
    @Override
    public SelectQuery addLimit(SelectQuery originSQL, long limit, long offset) {
        if (limit == -1) {
            return originSQL;
        }
        return new SelectQuery()
                .addAllColumns()
                .addCustomFromTable(
                        new Subquery(new SelectQuery()
                                .addCustomColumns(new CustomSql("ROWNUM  "+ TEMP_COLUMN_RNUM))
                                .addCustomColumns(new CustomSql("t1.*"))
                                .addCustomFromTable(new Subquery(originSQL) + " t1")
                                .addCondition(BinaryCondition.lessThanOrEq(new CustomSql("ROWNUM"), limit + offset - (offset == 0 ? 0 : 1))))
                )
                .addCondition(BinaryCondition.greaterThan(new CustomSql(TEMP_COLUMN_RNUM), offset));
    }

    @Override
    public String caseSensitive(String originElement) {
        return String.format("\"%s\"", originElement);
    }

    /**
     * 处理 CLOB 类型的过滤条件
     * @param column 过滤的列名
     * @param values 过滤的值
     * @param expressionType 过滤条件
     * @return
     */
    @Override
    public String getFilterConditionStr(String column, List<String> values,String expressionType) {
        String template = "dbms_lob.instr(" + column + ",{0},1,1)<>0";
        return MessageFormat.format("( {0} )",values.stream().map(v->MessageFormat.format(template,v)).collect(Collectors.joining(" or ")));
    }
}
