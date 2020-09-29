package io.zeta.metaspace.adapter.postgresql;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import com.healthmarketscience.sqlbuilder.custom.mysql.MysLimitClause;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import io.zeta.metaspace.adapter.AbstractAdapterTransformer;
import io.zeta.metaspace.adapter.Adapter;

public class PostgresqlAdapterTransformer extends AbstractAdapterTransformer {
    public PostgresqlAdapterTransformer(Adapter adapter) {
        super(adapter);
    }

    @Override
    public String caseSensitive(String originElement) {
        return String.format("\"%s\"", originElement);
    }
}
