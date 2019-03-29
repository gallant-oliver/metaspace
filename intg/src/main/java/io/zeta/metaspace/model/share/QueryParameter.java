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

import java.util.HashMap;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/27 19:33
 */
public class QueryParameter {
    private String tableGuid;
    private String dbGuid;
    private long maxRowNumber;
    private List<Parameter> parameter;
    private List<String> queryFields;
    private Long limit;
    private Long offset;

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getDbGuid() {
        return dbGuid;
    }

    public void setDbGuid(String dbGuid) {
        this.dbGuid = dbGuid;
    }

    public long getMaxRowNumber() {
        return maxRowNumber;
    }

    public void setMaxRowNumber(long maxRowNumber) {
        this.maxRowNumber = maxRowNumber;
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

    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

    public List<String> getQueryFields() {
        return queryFields;
    }

    public void setQueryFields(List<String> queryFields) {
        this.queryFields = queryFields;
    }

    public static class Parameter {
        private String columnName;
        private List<String> value;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public List<String> getValue() {
            return value;
        }

        public void setValue(List<String> value) {
            this.value = value;
        }
    }
}
