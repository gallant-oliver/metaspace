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
 * @date 2019/1/18 11:31
 */
package io.zeta.metaspace.web.task.util;

import io.zeta.metaspace.model.dataquality.TaskType;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/18 11:31
 */
public class QuartQueryProvider {

    public static String getQuery(final TaskType taskType) {
        switch (taskType) {
            case TABLE_ROW_NUM:
            case TABLE_ROW_NUM_CHANGE:
            case TABLE_ROW_NUM_CHANGE_RATIO:
                return "select count(*) from %s";
            case AVG_VALUE:
            case AVG_VALUE_CHANGE:
            case AVG_VALUE_CHANGE_RATIO:
                return "select avg(%s) from %s";
            case TOTAL_VALUE:
            case TOTAL_VALUE_CHANGE:
            case TOTAL_VALUE_CHANGE_RATIO:
                return "select sum(%s) from %s";
            case MIN_VALUE:
            case MIN_VALUE_CHANGE:
            case MIN_VALUE_CHANGE_RATIO:
                return "select min(%s) from %s";
            case MAX_VALUE:
            case MAX_VALUE_CHANGE:
            case MAX_VALUE_CHANGE_RATIO:
                return "select max(%s) from %s";
            case UNIQUE_VALUE_NUM:
            case UNIQUE_VALUE_NUM_CHANGE:
            case UNIQUE_VALUE_NUM_CHANGE_RATIO:
            case UNIQUE_VALUE_NUM_RATIO:
                return "SELECT count(*) from %s where %s in (SELECT %s from %s GROUP BY %s HAVING count(*)=1)";
            case EMPTY_VALUE_NUM:
            case EMPTY_VALUE_NUM_CHANGE:
            case EMPTY_VALUE_NUM_CHANGE_RATIO:
            case EMPTY_VALUE_NUM_RATIO:
                return "SELECT count(*) from %s WHERE %s is NULL";
            case DUP_VALUE_NUM:
            case DUP_VALUE_NUM_CHANGE:
            case DUP_VALUE_NUM_CHANGE_RATIO:
            case DUP_VALUE_NUM_RATIO:
                return "SELECT count(distinct(%s)) from %s where %s in (SELECT %s from %s GROUP BY %s HAVING count(*)>1)";
        }
        return null;
    }
}
