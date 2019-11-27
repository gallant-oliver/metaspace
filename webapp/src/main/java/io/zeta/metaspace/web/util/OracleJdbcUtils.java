package io.zeta.metaspace.web.util;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleJdbcUtils {
    private static final Logger LOG = LoggerFactory.getLogger(OracleJdbcUtils.class);
    private static final String SCHEMA_NAME = "SELECT USERNAME AS \"schemaName\" FROM DBA_USERS WHERE 1=1";
    private static final String SCHEMA_COUNT = "SELECT COUNT(*) FROM DBA_USERS";
    private static final String TABLE_NAME = "SELECT TABLE_NAME AS \"tableName\" FROM ALL_TABLES WHERE OWNER='%s'";
    private static final String TABLE_COUNT = "SELECT COUNT(*) FROM ALL_TABLES WHERE OWNER='%s'";
    private static final String COLUMN_NAME = "SELECT COLUMN_NAME AS \"columnName\",DATA_TYPE AS \"type\" FROM ALL_TAB_COLS WHERE OWNER='%s' AND TABLE_NAME='%s'";
    private static final String COLUMN_COUNT = "SELECT COUNT(*) FROM ALL_TAB_COLS WHERE OWNER='%s' AND TABLE_NAME='%s'";
    private static final String QUERY = "SELECT %s FROM %s";
    private static final String COUNT = " COUNT(*) ";
    private static final String WHERE = " WHERE ";
    private static final String ROWNUM_LIMIT = " AND ROWNUM<=%d ";
    private static final String MINUS = " MINUS ";


    public static ResultSet getSchemaList(Connection conn, long limit, long offset) throws AtlasBaseException {
        try {
            String sql = buildQuerySql(SCHEMA_NAME, limit, offset);
            return query(conn, sql);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }
    public static ResultSet getSchemaCount(Connection conn) throws AtlasBaseException {
        try {
            return query(conn, SCHEMA_COUNT);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public static ResultSet getTableList(Connection conn, String ownerName, long limit, long offset) throws AtlasBaseException {
        try {
            String sql  = buildQuerySql(TABLE_NAME, limit, offset, ownerName);
            return query(conn, sql);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }
    public static ResultSet getTableCount(Connection conn, String ownerName) throws AtlasBaseException {
        try {
            String sql = String.format(TABLE_COUNT, ownerName);
            return query(conn, sql);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public static ResultSet getColumnList(Connection conn, String ownerName, String tableName, long limit, long offset) throws AtlasBaseException {
        try {
            tableName = tableName.replace("'","''");
            String sql = buildQuerySql(COLUMN_NAME, limit, offset, ownerName, tableName);
            return query(conn, sql);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }
    public static ResultSet getColumnCount(Connection conn, String ownerName, String tableName) throws AtlasBaseException {
        try {
            tableName = tableName.replace("'","''");
            String sql = String.format(COLUMN_COUNT,  ownerName, tableName);
            return query(conn, sql);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public static String getQuerySql(String dbName, String tableName, String queryFields, String filterFields, long limit, long offset) throws AtlasBaseException {
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            String dbAndTableName = "\"" + dbName + "\"" + "." + "\"" + tableName + "\"";
            String queryStr = String.format(QUERY, queryFields, dbAndTableName);
            sqlBuilder.append(queryStr);
            if (filterFields != null) {
                sqlBuilder.append(WHERE);
                sqlBuilder.append(filterFields);
            }
            String sql = buildQuerySql(sqlBuilder.toString(), limit, offset);
            return sql;
        } catch (Exception e) {
            throw e;
        }
    }

    public static String getCountSql(String dbName, String tableName, String filterFields) {
        StringBuilder sqlBuilder = new StringBuilder();
        String dbAndTableName = "\"" + dbName + "\"" + "." + "\"" + tableName + "\"";
        String querySql = String.format(QUERY, COUNT, dbAndTableName);
        sqlBuilder.append(querySql);
        if (filterFields != null) {
            sqlBuilder.append(WHERE);
            sqlBuilder.append(filterFields);
        }
        return sqlBuilder.toString();
    }

    public static String buildQuerySql(String sqlTemplate, long limit, long offset, String... param) {
        StringBuilder sqlBuilder = new StringBuilder();
        String querySql = String.format(sqlTemplate, param);
        sqlBuilder.append(querySql);
        if(limit == -1) {
            return sqlBuilder.toString();
        }
        sqlBuilder.append(String.format(ROWNUM_LIMIT, offset + limit));
        sqlBuilder.append(MINUS);
        sqlBuilder.append(querySql);
        sqlBuilder.append(String.format(ROWNUM_LIMIT, offset));
        return sqlBuilder.toString();
    }



    public static ResultSet query(Connection conn, String sql) throws AtlasBaseException {
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = query(statement, sql);
            return resultSet;
        } catch (SQLException e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public static ResultSet query(Statement statement, String sql) throws AtlasBaseException {
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
}
