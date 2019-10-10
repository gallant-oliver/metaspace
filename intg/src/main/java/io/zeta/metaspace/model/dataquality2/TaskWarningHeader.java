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
 * @date 2019/8/5 18:24
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/5 18:24
 */
public class TaskWarningHeader {
    private String taskId;
    private String executionId;
    private String taskName;
    private String number;
    private Integer redWarningCount;
    private Integer orangeWarningCount;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executionTime;
    private Integer warningStatus;
    private List<WarningGroupHeader> warningGroupList;
    @JsonIgnore
    private int total;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getRedWarningCount() {
        return redWarningCount;
    }

    public void setRedWarningCount(Integer redWarningCount) {
        this.redWarningCount = redWarningCount;
    }

    public Integer getOrangeWarningCount() {
        return orangeWarningCount;
    }

    public void setOrangeWarningCount(Integer orangeWarningCount) {
        this.orangeWarningCount = orangeWarningCount;
    }

    public Timestamp getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Timestamp executionTime) {
        this.executionTime = executionTime;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    public List<WarningGroupHeader> getWarningGroupList() {
        return warningGroupList;
    }

    public void setWarningGroupList(List<WarningGroupHeader> warningGroupList) {
        this.warningGroupList = warningGroupList;
    }

    public static class WarningGroupHeader {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
