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
    private long limit;
    private long offset;

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

    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

    public static class Parameter {
        private String columnName;
        private String value;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
