package io.zeta.metaspace.sqlbuilder;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;

import java.io.IOException;


/**
 * Appends a Sqlserver offset clause like {@code " OFFSET <offset> row"} for use in
 * {@link SelectQuery}s.
 *
 * @author James Ahlborn
 * @see SelectQuery#addCustomization(CustomSyntax)
 */
public class MssqlOffsetClause extends CustomSyntax
        implements Verifiable<MssqlOffsetClause> {
    private SqlObject _value;

    public MssqlOffsetClause(Object value) {
        _value = Converter.toValueSqlObject(value);
    }

    @Override
    public void apply(SelectQuery query) {
        query.addCustomization(SelectQuery.Hook.FOR_UPDATE, HookType.BEFORE, this);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
        app.append(" OFFSET ");
        app.append(_value);
        app.append(" ROW ");
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
        vContext.addVerifiable(this);
        collectSchemaObjects(_value, vContext);
    }

    public final MssqlOffsetClause validate() throws ValidationException {
        doValidate();
        return this;
    }

    public void validate(ValidationContext vContext)
            throws ValidationException {
        if (_value == null) {
            throw new ValidationException("Offset clause is missing row count");
        }
        MssqlFetchClause.validateValue(_value, "Offset");
    }
}
