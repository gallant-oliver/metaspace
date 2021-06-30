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
    public List<String> getColumnNames(String tableName,String jdbcurl, String username, String password) {
        List<String> columnNames = new ArrayList<>();
        //与数据库的连接
        Connection conn = getConnection(jdbcurl,  username,  password);
        PreparedStatement pStemt = null;
        String tableSql = SQL + tableName;
        try {
            pStemt = conn.prepareStatement(tableSql);
            //结果集元数据
            ResultSetMetaData rsmd = pStemt.getMetaData();
            //表列数
            int size = rsmd.getColumnCount();
            for (int i = 0; i < size; i++) {
                columnNames.add(rsmd.getColumnName(i + 1));
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
    *//**
     * 获取表中字段的所有注释
     * @param
     * @return
     *//*
    public static List<String> getColumnComments(String driver,String tableName,String jdbcurl, String username, String password) {
        List<String> columnTypes = new ArrayList<>();
        //与数据库的连接
        Connection conn = getConnection( driver, jdbcurl,  username,  password);
        PreparedStatement pStemt = null;
        String tableSql = SQL + tableName;
        List<String> columnComments = new ArrayList<>();//列名注释集合
        ResultSet rs = null;
        try {
            pStemt = conn.prepareStatement(tableSql);
            rs = pStemt.executeQuery("show full columns from " + tableName);
            while (rs.next()) {
                columnComments.add(rs.getString("Comment"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                    closeConnection(conn);
                } catch (SQLException e) {
                    logger.error("getColumnComments close ResultSet and connection failure", e);
                }
            }
        }
        return columnComments;
    }
    public static void main(String[] args) {
        System.out.println(DatabaseUtil.class.getName());
        String jdbcurl = "jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=utf8",username="root",  password="root";
        String driver = "com.mysql.jdbc.Driver";
        List<String> tableNames = getTableNames(driver, jdbcurl,  username,  password);
        System.out.println("tableNames:" + tableNames);
        for (String tableName : tableNames) {
            System.out.println("================start==========================");
            System.out.println("==============================================");
           // System.out.println("ColumnNames:" + getColumnNames(driver,tableName,jdbcurl,  username,  password));
            System.out.println("ColumnTypes:" + getColumnTypes(driver,tableName,jdbcurl,  username,  password));
            System.out.println("ColumnComments:" + getColumnComments(driver,tableName,jdbcurl,  username,  password));
            System.out.println("==============================================");
            System.out.println("=================end=======================");
        }
    }*/

}
