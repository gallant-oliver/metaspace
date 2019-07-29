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
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;


public class Warning {

    /**
     * data_quality_task_execute
     */
    private String taskExecuteId;
    private Integer warningStatus;
    private String taskName;
    private String yellowWarningCount;
    private String redWarningCount;
    /**
     * 任务级别
     */
    private String taskLevel;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastWarningTime;
    /**
     * 告警通知对象
     */
    private String warningReceivers;


    public String getTaskExecuteId() {
        return taskExecuteId;
    }

    public void setTaskExecuteId(String taskExecuteId) {
        this.taskExecuteId = taskExecuteId;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getYellowWarningCount() {
        return yellowWarningCount;
    }

    public void setYellowWarningCount(String yellowWarningCount) {
        this.yellowWarningCount = yellowWarningCount;
    }

    public String getRedWarningCount() {
        return redWarningCount;
    }

    public void setRedWarningCount(String redWarningCount) {
        this.redWarningCount = redWarningCount;
    }

    public String getTaskLevel() {
        return taskLevel;
    }

    public void setTaskLevel(String taskLevel) {
        this.taskLevel = taskLevel;
    }

    public Timestamp getLastWarningTime() {
        return lastWarningTime;
    }

    public void setLastWarningTime(Timestamp lastWarningTime) {
        this.lastWarningTime = lastWarningTime;
    }

    public String getWarningReceivers() {
        return warningReceivers;
    }

    public void setWarningReceivers(String warningReceivers) {
        this.warningReceivers = warningReceivers;
    }
}
