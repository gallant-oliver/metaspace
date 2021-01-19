package io.zeta.metaspace.adapter.sqlserver;

import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.utils.ByteFormat;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;
import schemacrawler.schema.Schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class SqlServerAdapterExecutor extends AbstractAdapterExecutor {
    public SqlServerAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "select * from sys.tables t join sys.schemas s on t.schema_id=s.schema_id where t.name=? and s.name=?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("create_time");
                return DateUtils.parseDateTime(time);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return null;
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "SET NOCOUNT ON;exec sp_spaceused '%s.[%s]', true; ";
        db=db.replaceAll("'","''");
        tableName=tableName.replaceAll("'","''");
        querySQL=String.format(querySQL,db,tableName);
        Connection connection = getAdapterSource().getConnection();
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    String str = resultSet.getString("data");
                    if (str != null && str.length() != 0) {
                        totalSize = ByteFormat.parse(str);
                        break;
                    }
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询表大小失败", e);
            }
        });
    }

    /**
     * sqlserver不需要添加，获取到的库会带上"
     */
    @Override
    public String addSchemaEscapeChar(String string) {
        return string;
    }

    @Override
    public String getCreateTableSql(String schema, String table) {
        if(StringUtils.isEmpty(schema) || StringUtils.isEmpty(table)){
            throw new AtlasBaseException("schema or table is null !");
        }
        String schemaName=schema.replaceAll("\"","");
        String[] strs=schemaName.split("\\.");
        String tableName=table.replaceAll("\"","");
        String querySql="SELECT TABLE_CATALOG ,TABLE_SCHEMA , TABLE_NAME ,COLUMN_NAME ,IS_NULLABLE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH ,NUMERIC_PRECISION ,NUMERIC_SCALE,COLUMN_DEFAULT " +
                " FROM INFORMATION_SCHEMA.COLUMNS    " +
                " WHERE TABLE_CATALOG = '" +strs[0]+"'"+
                " and TABLE_SCHEMA = '" +strs[1]+"'"+
                " and TABLE_NAME ='" +tableName+"';";

        String create_sql=queryResult(querySql, resultSet -> {
            try {
                StringBuffer sql =new StringBuffer();
                sql.append("CREATE TABLE ");
                sql.append(schemaName).append(".").append(tableName).append(" ( \n");

                while (resultSet.next()) {
                    StringBuilder sb=new StringBuilder();
                    String column_name=resultSet.getString("COLUMN_NAME");
                    if(Character.isUpperCase(column_name.charAt(0))){
                        sb.append("\"").append(column_name).append("\" ");
                    }else{
                        sb.append(column_name).append(" ");
                    }
                    String data_type=resultSet.getString("DATA_TYPE");
                    sb.append(data_type);
                    int character_maximum_length = resultSet.getInt("CHARACTER_MAXIMUM_LENGTH");
                    int numeric_precision = resultSet.getInt("NUMERIC_PRECISION");
                    int numeric_scale = resultSet.getInt("NUMERIC_SCALE");
                    if(character_maximum_length>0){
                        sb.append("(").append(character_maximum_length).append(") ");
                    }else{
                        if(numeric_precision>0){
                            sb.append("(").append(numeric_precision);
                            if(numeric_scale>0){
                                sb.append(",").append(numeric_scale).append(")");
                            }else {
                                sb.append(")");
                            }
                        }
                    }
                    boolean is_nullable = resultSet.getBoolean("IS_NULLABLE");
                    String nullStr= is_nullable ? " NULL " : " NOT NULL ";
                    sb.append(nullStr);
                    String column_default = resultSet.getString("COLUMN_DEFAULT");
                    if(column_default==null||"null".equalsIgnoreCase(column_default.trim())){
                        sb.append("#");
                    }else{
                        sb.append("DEFAULT ").append(column_default).append("##");
                    }
                    sql.append(sb.toString());
                }
                sql.deleteCharAt(sql.lastIndexOf("#")).append("\n);");
                return sql.toString().replaceAll("#",",\n");
            } catch (SQLException e) {
                throw new AtlasBaseException("查询建表语句失败", e);
            }
        });
        return create_sql;
    }
}
