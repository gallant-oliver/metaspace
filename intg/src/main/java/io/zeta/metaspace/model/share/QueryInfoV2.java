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
 * @date 2019/5/5 15:41
 */
package io.zeta.metaspace.model.share;

import java.util.HashMap;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/5/5 15:41
 */
public class QueryInfoV2 {
    private List<String> columns;
    private List<Condition> filters;
    private long limit;
    private long offset;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Condition> getFilters() {
        return filters;
    }

    public void setFilters(List<Condition> filters) {
        this.filters = filters;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public static class Condition {
        private String name;
        private OPERATOR operator;
        private Object value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public OPERATOR getOperator() {
            return operator;
        }

        public void setOperator(OPERATOR operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Condition(OPERATOR operator, String name, Object value) {
            this.operator = operator;
            this.name = name;
            this.value = value;
        }
    }

    public enum OPERATOR {
        EQUAL("="),
        UNEQUAL("<>"),
        GREATER(">"),
        LESS("<"),
        LIKE("like"),
        IS_NULL("is null"),
        IN("in");

        String desc;
        OPERATOR(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
