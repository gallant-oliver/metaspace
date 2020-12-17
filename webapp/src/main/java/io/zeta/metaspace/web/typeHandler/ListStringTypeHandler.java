// ======================================================================
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
// ======================================================================
//

package io.zeta.metaspace.web.typeHandler;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ListStringTypeHandler extends BaseTypeHandler<List<String>> {
    private static final Type stringListType = new TypeToken<List<String>>() {
    }.getType();

    private static Gson gson = new Gson();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        String json = gson.toJson(parameter);
        ps.setString(i, json);
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        if (Strings.isNullOrEmpty(json))
            return Lists.newArrayList();
        List<String> list = gson.fromJson(json, stringListType);
        return list;
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        if (Strings.isNullOrEmpty(json))
            return Lists.newArrayList();
        List<String> list = gson.fromJson(json, stringListType);
        return list;
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        if (Strings.isNullOrEmpty(json))
            return Lists.newArrayList();
        List<String> list = gson.fromJson(json, stringListType);
        return list;
    }
}
