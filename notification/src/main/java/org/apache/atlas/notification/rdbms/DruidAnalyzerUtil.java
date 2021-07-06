package org.apache.atlas.notification.rdbms;


import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DruidAnalyzerUtil {
    private static final Logger log = LoggerFactory.getLogger(DruidAnalyzerUtil.class);

    /**
     * druid 处理 sql 的解析，增加了sql加工的逻辑
     * @param db
     * @param sql
     * @return
     */
    private static List<SQLStatement> parseStatements(String db,String sql){
        List<SQLStatement> stmts = null;
        while(true){
            try {
                stmts = SQLUtils.parseStatements(sql, db);
                break;
            }catch (ParserException e) {
                String errMsg = e.getMessage();
                log.error("parse sql error :{}",errMsg);
                int index = errMsg.indexOf("token");
                int index2 = errMsg.indexOf("EOF");
                if(index2 != -1){
                    String[] arr = sql.split(" ");
                    sql = sql.substring(0, sql.indexOf(arr[arr.length-1]));
                    log.info("EOF deal after sql: {}",sql);
                }else if(index != -1) {
                    String[] arr = errMsg.split(" ");
                    sql = sql.substring(0, sql.indexOf(arr[arr.length-1]));
                    log.info("invalid token deal after sql: {}",sql);
                }else {
                    break;
                }
            }
        }
        return stmts;
    }
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
        List<SQLStatement> stmts = parseStatements(db, sql);
        if (stmts == null) {
            log.error("sql {} => parse error",sql);
            return null;
        }

        TreeSet<String> fromSet = new TreeSet<>();
        TreeSet<String> toSet = new TreeSet<>();
        TreeSet<String> fromColumnSet = new TreeSet<>();
        TreeSet<String> toColumnSet = new TreeSet<>();
        Map<String, TreeSet<String>> fromTo = new HashMap<>(4);
        for (SQLStatement stmt : stmts) {
            SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(db);

            stmt.accept(statVisitor);
            Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
            Collection<TableStat.Column> columns = statVisitor.getColumns();
            /*columns.forEach(column -> {
                System.out.println(column.getTable() + " "+column.getName());
            });*/
            if (tables != null) {
                tables.forEach((tableName, stat) -> {
                    if (stat.getCreateCount() > 0 || stat.getInsertCount() > 0) {
                        String to = tableName.getName().toUpperCase();
                        toSet.add(to);
                        toColumnSet.addAll( columns.stream().filter(v->to.equalsIgnoreCase(v.getTable()) )
                                .map(p->p.getTable() + ":"+p.getName()).collect(Collectors.toSet())
                        );
                    } else if (stat.getSelectCount() > 0) {
                        String from = tableName.getName().toUpperCase();
                        fromSet.add(from);
                        //只筛选select后的字段，去除where
                        fromColumnSet.addAll( columns.stream().filter(v->v.isSelect() &&
                                (StringUtils.equalsIgnoreCase(v.getTable(), from) || StringUtils.equalsIgnoreCase(v.getTable(), "UNKNOWN") ))
                                .map(p->p.getTable() + ":"+p.getName()).collect(Collectors.toSet())
                        );
                    }
                });
            }
        }

        fromTo.put("from", fromSet);
        fromTo.put("to", toSet);
        fromTo.put("fromColumn", fromColumnSet);
        fromTo.put("toColumn", toColumnSet);
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
