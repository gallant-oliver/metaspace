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
 * @date 2019/7/26 10:43
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/26 10:43
 */
public class DataQualityTaskExecute {
    private String id;
    private String taskId;
    private Float percent;
    private Integer executeStatus;
    private String executor;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executeTime;
    private String closer;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp closeTime;
    private Integer costTime;
    private Integer orangeWarningCount;
    private Integer redWarningCount;
    private Integer ruleErrorCount;
    private Integer warningStatus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Float getPercent() {
        return percent;
    }

    public void setPercent(Float percent) {
        this.percent = percent;
    }

    public Integer getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(Integer executeStatus) {
        this.executeStatus = executeStatus;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Timestamp getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Timestamp executeTime) {
        this.executeTime = executeTime;
    }

    public String getCloser() {
        return closer;
    }

    public void setCloser(String closer) {
        this.closer = closer;
    }

    public Timestamp getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Timestamp closeTime) {
        this.closeTime = closeTime;
    }

    public Integer getCostTime() {
        return costTime;
    }

    public void setCostTime(Integer costTime) {
        this.costTime = costTime;
    }

    public Integer getOrangeWarningCount() {
        return orangeWarningCount;
    }

    public void setOrangeWarningCount(Integer orangeWarningCount) {
        this.orangeWarningCount = orangeWarningCount;
    }

    public Integer getRedWarningCount() {
        return redWarningCount;
    }

    public void setRedWarningCount(Integer redWarningCount) {
        this.redWarningCount = redWarningCount;
    }

    public Integer getRuleErrorCount() {
        return ruleErrorCount;
    }

    public void setRuleErrorCount(Integer ruleErrorCount) {
        this.ruleErrorCount = ruleErrorCount;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }
}
