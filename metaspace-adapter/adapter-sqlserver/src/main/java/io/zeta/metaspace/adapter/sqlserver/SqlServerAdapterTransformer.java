package io.zeta.metaspace.adapter.sqlserver;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import io.zeta.metaspace.adapter.AbstractAdapterTransformer;
import io.zeta.metaspace.adapter.Adapter;
import io.zeta.metaspace.sqlbuilder.MssqlFetchClause;
import io.zeta.metaspace.sqlbuilder.MssqlOffsetClause;
import org.apache.commons.lang.StringUtils;

public class SqlServerAdapterTransformer extends AbstractAdapterTransformer {
    public SqlServerAdapterTransformer(Adapter adapter) {
        super(adapter);
    }

    /**
     * 使用 OFFSET FETCH 方式分页，需要注意 sql server 版本应该是 SQL Server 2012（11.x）获取更高
     */
    @Override
    public SelectQuery addLimit(SelectQuery originSQL, long limit, long offset) {
        originSQL.addCustomization(new MssqlOffsetClause(offset)).addCustomization(new MssqlFetchClause(limit));
        return originSQL;
    }

    @Override
    public SelectQuery addOrderBy(SelectQuery originSQL, String sortSql) {
        originSQL.addCustomOrderings(new CustomSql(StringUtils.isNotEmpty(sortSql) ? sortSql : "(select 1)"));
        return originSQL;
    }
}
