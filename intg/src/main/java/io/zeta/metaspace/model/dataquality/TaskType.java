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
 * @date 2019/1/17 14:39
 */
package io.zeta.metaspace.model.dataquality;

public enum TaskType {

    //表体积
    TABLE_ROW_NUM_CHANGE_RATIO(0, "表行数变化率"),
    TABLE_SIZE_CHANGE_RATIO(1, "表大小变化率"),
    TABLE_ROW_NUM_CHANGE(2, "表行数变化"),
    TABLE_SIZE_CHANGE(3, "表大小变化"),
    TABLE_ROW_NUM(4, "当前表行数"),
    TABLE_SIZE(5, "当前表大小"),

    //空值校验
    EMPTY_VALUE_NUM_CHANGE_RATIO(11, "字段空值个数变化率"),
    EMPTY_VALUE_NUM_CHANGE(18, "字段空值个数变化"),
    EMPTY_VALUE_NUM(25, "字段空值个数"),
    EMPTY_VALUE_NUM_RATIO(28, "字段空值个数/总行数"),

    //唯一值校验
    UNIQUE_VALUE_NUM_CHANGE_RATIO(10, "字段唯一值个数变化率"),
    UNIQUE_VALUE_NUM_CHANGE(17, "字段唯一值个数变化"),
    UNIQUE_VALUE_NUM(24, "字段唯一值个数"),
    UNIQUE_VALUE_NUM_RATIO(27, "字段唯一值个数/总行数"),

    //重复值校验
    DUP_VALUE_NUM_CHANGE_RATIO(12, "字段重复值个数变化率"),
    DUP_VALUE_NUM_CHANGE(19, "字段重复值个数变化"),
    DUP_VALUE_NUM(26, "字段重复值个数"),
    DUP_VALUE_NUM_RATIO(29, "字段重复值个数/总行数"),

    //数值型校验
    AVG_VALUE_CHANGE_RATIO(6, "字段平均值变化率"),
    TOTAL_VALUE_CHANGE_RATIO(7, "字段汇总值变化率"),
    MIN_VALUE_CHANGE_RATIO(8, "字段最小值变化率"),
    MAX_VALUE_CHANGE_RATIO(9, "字段最大值变化率"),
    AVG_VALUE_CHANGE(13, "字段平均值变化"),
    TOTAL_VALUE_CHANGE(14, "字段汇总值变化"),
    MIN_VALUE_CHANGE(15, "字段最小值变化"),
    MAX_VALUE_CHANGE(16, "字段最大值变化"),
    AVG_VALUE(20, "字段平均值"),
    TOTAL_VALUE(21, "字段汇总值"),
    MIN_VALUE(22, "字段最小值"),
    MAX_VALUE(23, "字段最大值");

    public int code;
    public String desc;

    TaskType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static TaskType getTaskByCode(Integer code) {
        TaskType defaultTask = TaskType.TABLE_ROW_NUM;
        for (TaskType task : TaskType.values()) {
            if (task.code == code) {
                return task;
            }
        }
        return defaultTask;
    }

    public static String getDescByCode(Integer code) {
        return getTaskByCode(code).desc;
    }
}
