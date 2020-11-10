package io.zeta.metaspace.adapter.mysql;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.mysql.MysLimitClause;
import io.zeta.metaspace.adapter.AbstractAdapterTransformer;
import io.zeta.metaspace.adapter.Adapter;

public class MysqlAdapterTransformer extends AbstractAdapterTransformer {
    public MysqlAdapterTransformer(Adapter adapter) {
        super(adapter);
    }


    /**
     * 使用 LIMIT 方式分页
     */
    @Override
    public SelectQuery addLimit(SelectQuery originSQL, long limit, long offset) {
        originSQL.addCustomization(new MysLimitClause(offset, limit));
        return originSQL;
    }


    /**
     * 因为 Mysql 在低版本不支持窗口函数，无法使用 count(*) over() 方式获取总数，所以通过子查询的方式
     */
    @Override
    public SelectQuery addTotalCount(SelectQuery originSQL) {
        SelectQuery totalSelectQuery = new SelectQuery();
        totalSelectQuery.addCustomColumns(new CustomSql("COUNT(*)"));
        totalSelectQuery.addCustomFromTable(new CustomSql("(" + originSQL.toString() + ") TOTAL_COLUMN_SQL"));
        originSQL.addAliasedColumn(new CustomSql("(" + totalSelectQuery.toString() + ")"), TOTAL_COLUMN_ALIAS);
        return originSQL;
    }
}
