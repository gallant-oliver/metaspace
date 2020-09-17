package io.zeta.metaspace.sqlbuilder;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.custom.CustomSyntax;
import com.healthmarketscience.sqlbuilder.custom.HookType;

import java.io.IOException;


/**
 * Appends a Sqlserver limit clause like {@code " fetch next 10 row only"} for use in
 * {@link SelectQuery}s.
 *
 * @author James Ahlborn
 * @see SelectQuery#addCustomization(CustomSyntax)
 */
public class MssqlFetchClause extends CustomSyntax
        implements Verifiable<MssqlFetchClause> {
    private SqlObject _value;

    public MssqlFetchClause(Object value) {
        _value = Converter.toValueSqlObject(value);
    }

    @Override
    public void apply(SelectQuery query) {
        query.addCustomization(SelectQuery.Hook.FOR_UPDATE, HookType.BEFORE, this);
    }

    @Override
    public void appendTo(AppendableExt app) throws IOException {
        app.append(" FETCH NEXT ");
        app.append(_value);
        app.append(" ROW ONLY");
    }

    @Override
    protected void collectSchemaObjects(ValidationContext vContext) {
        vContext.addVerifiable(this);
        collectSchemaObjects(_value, vContext);
    }

    public final MssqlFetchClause validate() throws ValidationException {
        doValidate();
        return this;
    }

    public void validate(ValidationContext vContext)
            throws ValidationException {
        if (_value == null) {
            throw new ValidationException("fetch next clause is missing row count");
        }
        validateValue(_value, "FETCH NEXT");
    }

    static void validateValue(SqlObject valueObj, String type) {
        if (!(valueObj instanceof NumberValueObject)) {
            // nothing we can do, custom value
            return;
        }
        if (!((NumberValueObject) valueObj).isIntegralInRange(0, Long.MAX_VALUE)) {
            throw new ValidationException(
                    type + " value must be positive integer, given: " + valueObj);
        }
    }
}
