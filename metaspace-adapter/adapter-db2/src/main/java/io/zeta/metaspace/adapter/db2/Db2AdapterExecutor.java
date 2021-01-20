package io.zeta.metaspace.adapter.db2;

import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Db2AdapterExecutor extends AbstractAdapterExecutor {
    public Db2AdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "select CREATE_TIME from syscat.tables where TABNAME = ? AND TABSCHEMA  = ?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("CREATE_TIME");
                return LocalDateTime.parse(time,  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS"));
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return null;
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "SELECT TABSCHEMA,TABNAME,sum(data_object_p_size + index_object_p_size + long_object_p_size + " +
                          " lob_object_p_size + xml_object_p_size)*1024 size FROM TABLE (SYSPROC.ADMIN_GET_TAB_INFO('%s', '%s'))  group by TABSCHEMA,TABNAME ";
        db=db.replaceAll("'","''");
        tableName=tableName.replaceAll("'","''");
        querySQL=String.format(querySQL,db,tableName);
        Connection connection = getAdapterSource().getConnection();
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    totalSize = resultSet.getLong("size");
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询表大小失败", e);
            }
        });
    }

    @Override
    public String getCreateTableSql(String schema, String table) {
        if(StringUtils.isEmpty(schema) || StringUtils.isEmpty(table)){
            throw new AtlasBaseException("schema or table is null !");
        }
        String schemaName=schema.replaceAll("\"","");
        String tableName=table.replaceAll("\"","");
        String querySql="SELECT  " +
                " COLNAME AS COLUMN_NAME,TYPENAME AS DATA_TYPE,\"LENGTH\" AS MAXIMUM_LENGTH ,\"SCALE\" AS NUMERIC_SCALE,\"NULLS\" AS IS_NULLABLE ,\"DEFAULT\" AS COLUMN_DEFAULT " +
                " FROM  SYSCAT.COLUMNS " +
                " WHERE TABSCHEMA = '" +schemaName+"' "+
                " AND TABNAME='" +tableName+"';";

        String createSql=queryResult(querySql, resultSet -> {
            try {
                StringBuffer sql =new StringBuffer();
                sql.append("CREATE TABLE ");
                sql.append(schema).append(".").append(table).append(" ( \n");

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

                    int maximum_length = resultSet.getInt("MAXIMUM_LENGTH");
                    int numeric_scale = resultSet.getInt("NUMERIC_SCALE");
                    if(maximum_length>0){
                        sb.append("(").append(maximum_length);
                        if(numeric_scale>0){
                            sb.append(",").append(numeric_scale).append(")");
                        }else {
                            sb.append(")");
                        }
                    }

                    boolean is_nullable = resultSet.getBoolean("IS_NULLABLE");
                    String nullStr= is_nullable ? " NULL " : " NOT NULL ";
                    sb.append(nullStr);
                    String column_default = resultSet.getString("COLUMN_DEFAULT");
                    if(column_default==null||"null".equalsIgnoreCase(column_default.trim())){
                        sb.append("#");
                    }else{
                        sb.append("DEFAULT ").append(column_default).append("#");
                    }
                    sql.append(sb.toString());
                }
                sql.deleteCharAt(sql.lastIndexOf("#")).append("\n);");
                return sql.toString().replaceAll("#",",\n");
            } catch (SQLException e) {
                throw new AtlasBaseException("查询建表语句失败", e);
            }
        });
        return createSql;
    }
}
