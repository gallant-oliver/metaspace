package org.apache.atlas.notification.rdbms;


import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * druid 解析sql 工具类
 * sql 解析生成表、列信息
 */
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
     * 获取创建表、视图 的列别名信息
     * @param stmt
     * @return 返回 原始表.列  --> 别名字段
     */
    private static Map<String,String> getColumnAliasMap(SQLStatement stmt){
        Map<String,String> aliasMap = new HashMap<>();
        SQLSelectQueryBlock selectBlock = null;
        if(stmt instanceof SQLCreateViewStatement) {
            SQLCreateViewStatement view = (SQLCreateViewStatement) stmt;
            selectBlock = view.getSubQuery().getQueryBlock();
        }else if(stmt instanceof SQLCreateMaterializedViewStatement){
            SQLCreateMaterializedViewStatement view = (SQLCreateMaterializedViewStatement)stmt;
            selectBlock = view.getQuery().getQueryBlock();
        }else if(stmt instanceof SQLCreateTableStatement) {
            SQLCreateTableStatement createStmt = (SQLCreateTableStatement) stmt;
            selectBlock =  createStmt.getSelect().getQueryBlock();
        }

        if(selectBlock != null){
            List<SQLSelectItem> list = selectBlock.getSelectList();
            for(SQLSelectItem item : list){
                String origin = "";
                String clomn = item.getAlias();
                if(clomn == null){
                    log.info("no alias..");
                    continue;
                }
                clomn = clomn.replaceAll("\"","").replaceAll("\'","");
                String oriColumn = item.getExpr().toString();
                int location = oriColumn.indexOf('.');
                if(location > -1 ) {
                    SQLTableSource tbSource = selectBlock.findTableSource(oriColumn.split("\\.")[0]);
                    origin = tbSource != null ? tbSource.toString()+":"+ oriColumn.split("\\.")[1] :oriColumn;
                }
                aliasMap.put(origin, clomn);
            }
        }
        return aliasMap;
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
        Map<String, TreeSet<String>> fromTo = new HashMap<>(5);
        fromTo.put("type",new TreeSet<>(Arrays.asList("table")));
        for (SQLStatement stmt : stmts) {
            Map<String,String> aliasMap = getColumnAliasMap(stmt);
            log.info("处理字段别名success.");
            //创建视图view  materialized view 需单独处理(否则获取不到目标视图名称)
            if(stmt instanceof SQLCreateViewStatement) {
                SQLCreateViewStatement view = (SQLCreateViewStatement)stmt;
                toSet.add(view.getTableSource().toString());
                fromTo.put("type",new TreeSet<>(Arrays.asList("view")));
            }
            if(stmt instanceof SQLCreateMaterializedViewStatement) {
                SQLCreateMaterializedViewStatement view = (SQLCreateMaterializedViewStatement)stmt;
                toSet.add(view.getName().toString());
                fromTo.put("type",new TreeSet<>(Arrays.asList("view")));
            }

            SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(db);
            stmt.accept(statVisitor);
            Map<TableStat.Name, TableStat> tables = statVisitor.getTables();
            Collection<TableStat.Column> columns = statVisitor.getColumns();

            if (tables != null) {
                tables.forEach((tableName, stat) -> {
                    if (stat.getCreateCount() > 0 || stat.getInsertCount() > 0
                            || stat.getDropCount() > 0 || stat.getAlterCount() > 0) {
                        String to = tableName.getName().toUpperCase();
                        //to.indexOf(".") != -1 ? to.split("\\.")[1]:
                        toSet.add(to);
                        toColumnSet.addAll( columns.stream().filter(v->to.equalsIgnoreCase(v.getTable()) )
                                .map(p->p.getTable() + ":"+p.getName()).collect(Collectors.toSet())
                        );
                    } else if (stat.getSelectCount() > 0) {
                        String from = tableName.getName().toUpperCase();
                        //from.indexOf(".") != -1 ? from.split("\\.")[1] :
                        fromSet.add(from);
                        //只筛选select后的字段，去除where  且 列存在别名的话，使用（别名=>原名）的格式
                        fromColumnSet.addAll( columns.stream().filter(v->v.isSelect() &&
                                (StringUtils.equalsIgnoreCase(v.getTable(), from) || StringUtils.equalsIgnoreCase(v.getTable(), "UNKNOWN") ))
                                .map(p-> MapUtils.getString(aliasMap,p.getTable() + ":"+p.getName(),"")+"=>"+p.getTable() + ":"+p.getName())
                                .collect(Collectors.toSet())
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

}
