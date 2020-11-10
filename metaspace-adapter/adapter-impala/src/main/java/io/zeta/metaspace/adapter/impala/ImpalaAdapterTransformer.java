package io.zeta.metaspace.adapter.impala;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import io.zeta.metaspace.adapter.AbstractAdapterTransformer;
import io.zeta.metaspace.adapter.Adapter;
import org.apache.commons.lang.StringUtils;

public class ImpalaAdapterTransformer extends AbstractAdapterTransformer {
    public ImpalaAdapterTransformer(Adapter adapter) {
        super(adapter);
    }

    @Override
    public String caseSensitive(String originElement) {
        return String.format("`%s`", originElement);
    }

    @Override
    public SelectQuery addOrderBy(SelectQuery originSQL, String sortSql) {
        originSQL.addCustomOrderings(new CustomSql(StringUtils.isNotEmpty(sortSql) ? sortSql : "1"));
        return originSQL;
    }
}
