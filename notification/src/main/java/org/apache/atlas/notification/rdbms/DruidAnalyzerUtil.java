package org.apache.atlas.notification.rdbms;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DruidAnalyzerUtil {
    private static final Logger log = LoggerFactory.getLogger(DruidAnalyzerUtil.class);
    /**
     *  获取sql的表血缘
     * @param sql
     * @param dbType ：mysql、oracle
     * @return
     * @throws ParserException
     */
    public static Map<String, TreeSet<String>> getFromTo (String sql,String dbType) {
        String db = "";
        if (JdbcConstants.MYSQL.equalsIgnoreCase(dbType) || JdbcConstants.MARIADB.equals(dbType)) {
            db = JdbcConstants.MYSQL;
        }
        if (JdbcConstants.ORACLE.equals(dbType) || JdbcConstants.ALI_ORACLE.equals(dbType)) {
            db = JdbcConstants.ORACLE;
        }
        List<SQLStatement> stmts = null;
        try{
            stmts = SQLUtils.parseStatements(sql, db);
        }catch (ParserException ex){
            log.error("sql parse error:{}",ex);
        }
        if (stmts == null) {
            return null;
        }
        
        TreeSet<String> fromSet = new TreeSet<>();
        TreeSet<String> toSet = new TreeSet<>();
        Map<String, TreeSet<String>> fromTo = new HashMap<>(4);
        for (SQLStatement stmt : stmts) {
            SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(db);
			/*if (stmt instanceof SQLUseStatement) {
			    database = ((SQLUseStatement) stmt).getDatabase().getSimpleName().toUpperCase();
			}*/
            stmt.accept(statVisitor);
            Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
            Collection<TableStat.Column> columns = statVisitor.getColumns();
            columns.forEach(column -> {
                System.out.println(column.getTable() + " "+column.getName());
            });
            if (tables != null) {
                tables.forEach((tableName, stat) -> {
                    if (stat.getCreateCount() > 0 || stat.getInsertCount() > 0) {
                        String to = tableName.getName().toUpperCase();
                        toSet.add(to);
                    } else if (stat.getSelectCount() > 0) {
                        String from = tableName.getName().toUpperCase();
                        fromSet.add(from);
                    }
                });
            }
        }

        fromTo.put("from", fromSet);
        fromTo.put("to", toSet);
        return fromTo;
    }

    public static void main(String[] args) {
        String sql = "-- step 1 step1\n" +
                "DROP TABLE IF EXISTS tb1;\n" +
                "CREATE TABLE tb1  AS\n" +
                "SELECT tb2.filed1,\n" +
                "       filed2,\n" +
                "       filed3\n" +
                "FROM tb2,tb3\n" +
                "WHERE tb2.filed1 = tb3.filed1 \n" +
                "  AND filed3 = 0\n" ;

        sql= "SELECT a.filed4 AS filed4,\n" +
                "       a.filed5\n" +
                "FROM tb2 a\n" +
                "LEFT JOIN tb3 b ON (a.filed4 = b.filed4)\n" +
                "WHERE a.filed4 = 1\n" +
                "  AND a.filed5 = 0;\n";

        Map<String, TreeSet<String>> result = DruidAnalyzerUtil.getFromTo(sql,"oracle");
        result.forEach((key, set) -> {
            System.out.print(key+":");
            set.forEach(item -> System.out.print(item +" "));
            System.out.println();
        });
    }
}
