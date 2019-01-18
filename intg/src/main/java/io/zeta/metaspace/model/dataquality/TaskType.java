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
    TABLE_ROW_NUM_CHANGE_RATIO(0,"表行数变化率"),
    TABLE_SIZE_CHANGE_RATIO(1,"表大小变化率"),
    TABLE_SIZE_CHANGE(2,"表大小变化"),
    TABLE_ROW_NUM_CHANGE(3,"表行数变化"),
    TABLE_ROW_NORMAL(4,"当前表行数"),
    TABLE_SIZE_NORMAL(5,"当前表大小")
    ;

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
        TaskType defaultTask = TaskType.TABLE_ROW_NORMAL;
        for(TaskType task : TaskType.values()) {
            if(task.code == code)
                return task;
        }
        return defaultTask;
    }

    public static String getDescByCode(Integer code) {
        return getTaskByCode(code).desc;
    }
}
