package org.apache.atlas.notification.rdbms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String SQL = "SELECT * FROM ";// 数据库操作
    private Connection currentConn = null;
    //connection map key:jdbcurl string
    private Map<String,Connection> connectionMap = new HashMap<>();
    private static DatabaseUtil databaseUtil = null;

    private DatabaseUtil(){}

    public static DatabaseUtil getInstance(String driver){
        try{
            Class.forName(driver); //"oracle.jdbc.driver.OracleDriver"
        }catch (ClassNotFoundException e) {
            logger.error("can not load jdbc driver", e);
        }

        if(databaseUtil == null){
            databaseUtil = new DatabaseUtil();
        }
        return databaseUtil;
    }

    /**
     * 获取数据库连接
     *
     * @return
     */
    private Connection getConnection(String jdbcurl, String username, String password) {
        currentConn = connectionMap.get(jdbcurl);

        if(this.currentConn != null){
            return this.currentConn;
        }
        try {
            //"jdbc:oracle:thin:@10.200.64.102:1521:orcl", "test", "123456"
            this.currentConn = DriverManager.getConnection(jdbcurl,  username,  password);
            connectionMap.put(jdbcurl,currentConn);
        } catch (Exception e) {
            logger.error("get connection failure", e);
        }
        return this.currentConn;
    }

    /**
     * 获取表中所有字段名称
     * @param tableName 表名
     * @return
     */
    public List<TableColumnInfo> getColumnNames(String tableName,String jdbcUrl, String username, String password) {
        List<TableColumnInfo> columnNames = new ArrayList<>();
        //与数据库的连接
        Connection conn = getConnection(jdbcUrl,  username,  password);
        PreparedStatement pStemt = null;
        String tableSql = SQL + tableName;
        try {
            pStemt = conn.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            TableColumnInfo tableColumnInfo = null;
            for (int i = 0; i < size; i++) {
                tableColumnInfo = new TableColumnInfo();
                tableColumnInfo.setColumnName(rsmd.getColumnName(i + 1));
                tableColumnInfo.setDataType(rsmd.getColumnTypeName(i + 1));
                tableColumnInfo.setNullable(rsmd.isNullable(i + 1)==1);
                tableColumnInfo.setLength(rsmd.getColumnDisplaySize(i+1));
                columnNames.add(tableColumnInfo);
            }
        } catch (SQLException e) {
            logger.error("getColumnNames failure", e);
        } finally {
            if (pStemt != null) {
                try {
                    pStemt.close();
                    // closeConnection(conn);
                } catch (SQLException e) {
                    logger.error("getColumnNames close pstem and connection failure", e);
                }
            }
        }
        return columnNames;
    }

    static class TableColumnInfo{
        private String columnName;
        private boolean nullable;
        private String dataType;
        private Integer length;

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }
    }
    /**
     * 关闭数据库连接
     * @param conn
     */
   /* public static void closeConnection(Connection conn) {
        if(conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("close connection failure", e);
            }
        }
    }
    *//**
     * 获取数据库下的所有表名
     *//*
    public static List<String> getTableNames(String driver,String jdbcurl, String username, String password) {
        List<String> tableNames = new ArrayList<>();
        Connection conn = getConnection(  driver,jdbcurl,  username,  password);
        ResultSet rs = null;
        try {
            //获取数据库的元数据
            DatabaseMetaData db = conn.getMetaData();
            //从元数据中获取到所有的表名
            rs = db.getTables(null, null, null, new String[] { "TABLE" });
            while(rs.next()) {
                tableNames.add(rs.getString(3));
            }
        } catch (SQLException e) {
            logger.error("getTableNames failure", e);
        } finally {
            try {
                rs.close();
                closeConnection(conn);
            } catch (SQLException e) {
                logger.error("close ResultSet failure", e);
            }
        }
        return tableNames;
    }

    *//**
     * 获取表中所有字段类型
     * @param tableName
     * @return
     *//*
    public static List<String> getColumnTypes(String driver,String tableName,String jdbcurl, String username, String password) {
        List<String> columnTypes = new ArrayList<>();
        //与数据库的连接
        Connection conn = getConnection(driver, jdbcurl,  username,  password);
        PreparedStatement pStemt = null;
        String tableSql = SQL + tableName;
        try {
            pStemt = conn.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            for (int i = 0; i < size; i++) {
                columnTypes.add(rsmd.getColumnTypeName(i + 1));
            }
        } catch (SQLException e) {
            logger.error("getColumnTypes failure", e);
        } finally {
            if (pStemt != null) {
                try {
                    pStemt.close();
                    closeConnection(conn);
                } catch (SQLException e) {
                    logger.error("getColumnTypes close pstem and connection failure", e);
                }
            }
        }
        return columnTypes;
    }
    */
}
