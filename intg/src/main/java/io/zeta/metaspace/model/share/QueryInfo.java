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
 * @date 2019/4/22 9:31
 */
package io.zeta.metaspace.model.share;

import java.util.HashMap;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/22 9:31
 */
public class QueryInfo {
    private List<String> columns;
    private HashMap filters;
    private long limit;
    private long offset;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public HashMap getFilters() {
        return filters;
    }

    public void setFilters(HashMap filters) {
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
}
