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

public class TaskExecute {

    private String id;
    private String taskId;
    private String percent;
    private String executeStatus;
    private String executor;
    private String errorMsg;
    private String executeTime;
    private String closer;
    private String closeTime;
    private String costTime;
    private String yellowWarningCount;
    private String redWarningCount;
    private String ruleErrorCount;
    private String warningStatus;

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

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public String getExecuteStatus() {
        return executeStatus;
    }

    public void setExecuteStatus(String executeStatus) {
        this.executeStatus = executeStatus;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
    }

    public String getCloser() {
        return closer;
    }

    public void setCloser(String closer) {
        this.closer = closer;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getCostTime() {
        return costTime;
    }

    public void setCostTime(String costTime) {
        this.costTime = costTime;
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

    public String getRuleErrorCount() {
        return ruleErrorCount;
    }

    public void setRuleErrorCount(String ruleErrorCount) {
        this.ruleErrorCount = ruleErrorCount;
    }

    public String getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(String warningStatus) {
        this.warningStatus = warningStatus;
    }
}
