package org.apache.atlas.notification.rdbms;

import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerColumn;
import io.zeta.metaspace.utils.AdapterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库元数据工具类   （根据连接获取所有表、表的列信息）
 */
public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String SQL = "SELECT * FROM ";// 数据库操作
    private Connection currentConn = null;
    //connection map key:jdbcurl string
    private Map<String,Connection> connectionMap = new HashMap<>();
    private static DatabaseUtil databaseUtil = null;

    private DatabaseUtil(){}

    /**
     * 定义新的获取列方式
     * @param tableName
     * @return
     */
    public static List<TableColumnInfo> getColumnNames(DataSourceInfo dataSourceInfo, String tableUser, String tableName){
        List<TableColumnInfo> columnNames = new ArrayList<>();
        AdapterExecutor adapterExecutor = AdapterUtils.getAdapterExecutor(dataSourceInfo);
        List<SchemaCrawlerColumn> list = null;
        try{
            list = adapterExecutor.getColumns(tableUser,tableName);
        }catch (Exception e){//查找不到列的时候error
            logger.error("获取表 {} 列信息出错,{}",tableName,e);
        }

        if(CollectionUtils.isEmpty(list)){
            return columnNames;
        }
        TableColumnInfo tableColumnInfo = null;
        for(SchemaCrawlerColumn item : list){
            tableColumnInfo = new TableColumnInfo();
            tableColumnInfo.setColumnName(item.getName());
            tableColumnInfo.setDataType(item.getDataType());
            tableColumnInfo.setNullable(item.isNullable());
            tableColumnInfo.setLength(item.getLength());
            columnNames.add(tableColumnInfo);
        }
        return columnNames;
    }

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
            logger.error("table {} getColumnNames failure,cause:{}",tableName, e.getMessage());
            return columnNames;
        } finally {
            if (pStemt != null) {
                try {
                    pStemt.close();
                    // closeConnection(conn);
                } catch (SQLException e) {
                    logger.error("table {} getColumnNames close pstem and connection failure:{}",tableName, e);
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
