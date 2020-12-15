package io.zeta.metaspace.web.typeHandler;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.zeta.metaspace.model.share.ApiPolyEntity;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ApiPolyEntityTypeHandler extends BaseTypeHandler<ApiPolyEntity> {
    private static final Gson gson = new Gson();

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, ApiPolyEntity apiPolyEntity, JdbcType jdbcType) throws SQLException {
        try {
            String json = gson.toJson(apiPolyEntity, ApiPolyEntity.class);
            PGobject jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(json);
            preparedStatement.setObject(i, jsonObject);
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    @Override
    public ApiPolyEntity getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String json = resultSet.getString(s);
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, ApiPolyEntity.class);
    }

    @Override
    public ApiPolyEntity getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String json = resultSet.getString(i);
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, ApiPolyEntity.class);
    }

    @Override
    public ApiPolyEntity getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String json = callableStatement.getString(i);
        if (Strings.isNullOrEmpty(json)) {
            return null;
        }
        return gson.fromJson(json, ApiPolyEntity.class);
    }
}
