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
 * @date 2019/3/27 19:33
 */
package io.zeta.metaspace.model.share;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/27 19:33
 */
public class QueryParameter {
    private long maxRowNumber;
    private List<Field> queryFields;
    private Boolean desensitize;
    private Long limit;
    private Long offset;
    private String pool;

    public String getPool() {
        return pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public long getMaxRowNumber() {
        return maxRowNumber;
    }

    public void setMaxRowNumber(long maxRowNumber) {
        this.maxRowNumber = maxRowNumber;
    }

    public Boolean getDesensitize() {
        return desensitize;
    }

    public void setDesensitize(Boolean desensitize) {
        this.desensitize = desensitize;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public List<Field> getQueryFields() {
        return queryFields;
    }

    public void setQueryFields(List<Field> queryFields) {
        this.queryFields = queryFields;
    }

    public static class Field {
        private String columnName;
        private String type;
        private Boolean filter;
        private List<Object> valueList;
        private Boolean sensitive;

        public  Field() { }

        public Field(String columnName, String type, Boolean filter, List<Object> valueList, Boolean sensitive) {
            this.columnName = columnName;
            this.type = type;
            this.filter = filter;
            this.valueList = valueList;
            this.sensitive = sensitive;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Boolean getFilter() {
            return filter;
        }

        public void setFilter(Boolean filter) {
            this.filter = filter;
        }

        public List<Object> getValueList() {
            return valueList;
        }

        public void setValueList(List<Object> valueList) {
            this.valueList = valueList;
        }

        public Boolean getSensitive() {
            return sensitive;
        }

        public void setSensitive(Boolean sensitive) {
            this.sensitive = sensitive;
        }
    }
}
