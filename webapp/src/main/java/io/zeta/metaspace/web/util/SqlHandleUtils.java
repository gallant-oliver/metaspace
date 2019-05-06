// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/4/28 15:24
 */
package io.zeta.metaspace.web.util;

import io.zeta.metaspace.model.share.QueryInfoV2;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/28 15:24
 */
public class SqlHandleUtils {
    private static String SEPERATOR = ",";
    private static String KEYWORD_SELECT = "select";
    private static String KEYWORD_WHERE = "where";
    private static String KEYWORD_FROM = "from";
    private static String BLANK = " ";
    private static String COUNT = " count(1) ";

    private static StringBuffer sql;
    private static List<String> queryFields;
    private static List<QueryInfoV2.Condition> conditions;
    private static String tableName;
    private static int limit = 10;
    private static int offset = 0;

    static {
        sql = new StringBuffer(KEYWORD_SELECT);
        queryFields = new ArrayList<>();
        conditions = new ArrayList<>();
    }

    public static List<String> getQueryFields() {
        return queryFields;
    }

    public static void setQueryFields(List<String> queryFields) {
        SqlHandleUtils.queryFields = queryFields;
    }

    public static List<QueryInfoV2.Condition> getConditions() {
        return conditions;
    }

    public static void setConditions(List<QueryInfoV2.Condition> conditions) {
        SqlHandleUtils.conditions = conditions;
    }

    public void addQueryField(String field) {
        queryFields.add(field);
    }

    public void addQueryFields(List<String> fields) {
        queryFields.addAll(fields);
    }

    public static String getTableName() {
        return tableName;
    }

    public static void setTableName(String tableName) {
        SqlHandleUtils.tableName = tableName;
    }



    public String getTableStr() {
        StringBuffer sb = new StringBuffer();
        sb.append(KEYWORD_FROM).append(" ").append(tableName);
        return sb.toString();
    }

    public String getQueryFieldsStr() {
        StringJoiner joiner = new StringJoiner(SEPERATOR);
        queryFields.stream().forEach(field -> joiner.add(field));
        return joiner.toString();
    }

    public String getFilterStr() {
        StringJoiner joiner = new StringJoiner(SEPERATOR);
        for(QueryInfoV2.Condition condition : conditions) {
            StringBuffer sb = new StringBuffer();
            /*switch (condition.operator) {
                case EQUAL:
                    sb.append(" = ");
                    break;
                case UNEQUAL:
                    sb.append(" <> ");
                    break;
                case GREATER:
                    sb.append(" > ");
                    break;
                case LESS:
                    sb.append(" < ");
                    break;
                case LIKE:
                    sb.append(" like ");
                    break;
            }*/
            sb.append(condition.getName());
            sb.append(condition.getOperator().getDesc());
            //in
            if(QueryInfoV2.OPERATOR.IN == condition.getOperator()) {
                sb.append("(");
                if(condition.getValue() instanceof List) {
                    StringJoiner valueJoiner = new StringJoiner(SEPERATOR);
                    List<Object> values = (List)condition.getValue();
                    values.stream().forEach(v -> valueJoiner.add(v.toString()));
                    sb.append(valueJoiner.toString());
                } else {
                    sb.append(condition.getValue());
                }
                sb.append(")");
            }

            sb.append(condition.getValue());
            joiner.add(sb.toString());
        }
        StringBuffer sb = new StringBuffer();
        sb.append(KEYWORD_WHERE).append(BLANK).append(joiner.toString()).append(BLANK);
        return sb.toString();
    }

    private String getLimitAndOffsetStr() {
        StringBuffer sb = new StringBuffer();
        sb.append("limit=").append(limit).append(" and ").append("offset=").append(offset);
        return sb.toString();
    }

    public String getQuerySql() {
        String queryFieldStr = getQueryFieldsStr();
        String filterStr = getFilterStr();
        String limitAndOffsetStr = getLimitAndOffsetStr();
        this.sql.append(queryFieldStr).append(filterStr).append(limitAndOffsetStr);
        return sql.toString();
    }

    public String getCountSql() {
        String filterStr = getFilterStr();
        this.sql.append(COUNT).append(filterStr);
        return sql.toString();
    }
}
