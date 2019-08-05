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

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/8/5 18:24
 */
public class TaskWarningHeader {
    private String taskId;
    private String taskName;
    private Integer warningStatus;
    private Integer warningCount;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp lastWarningTime;
    private List<String> warningGroupObjectList;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }

    public Timestamp getLastWarningTime() {
        return lastWarningTime;
    }

    public void setLastWarningTime(Timestamp lastWarningTime) {
        this.lastWarningTime = lastWarningTime;
    }

    public List<String> getWarningGroupObjectList() {
        return warningGroupObjectList;
    }

    public void setWarningGroupObjectList(List<String> warningGroupObjectList) {
        this.warningGroupObjectList = warningGroupObjectList;
    }
}
