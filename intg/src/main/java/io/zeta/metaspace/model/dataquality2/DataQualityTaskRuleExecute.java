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
 * @date 2019/7/26 10:48
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/26 10:48
 */
public class DataQualityTaskRuleExecute {
    private String id;
    private String taskExecuteId;
    private String taskId;
    private String subTaskId;
    private String subTaskObjectId;
    private String subTaskRuleId;
    private Float result;
    private Float referenceValue;
    private Integer checkStatus;
    private Integer orangeWarningCheckStatus;
    private Integer redWarningCheckStatus;
    private Integer warningStatus;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    public DataQualityTaskRuleExecute(String id, String taskExecuteId, String taskId, String subTaskId, String subTaskObjectId, String subTaskRuleId, Float result, Float referenceValue, Integer checkStatus, Integer orangeWarningCheckStatus, Integer redWarningCheckStatus, Integer warningStatus, Timestamp createTime, Timestamp updateTime) {
        this.id = id;
        this.taskExecuteId = taskExecuteId;
        this.taskId = taskId;
        this.subTaskId = subTaskId;
        this.subTaskObjectId = subTaskObjectId;
        this.subTaskRuleId = subTaskRuleId;
        this.result = result;
        this.referenceValue = referenceValue;
        this.checkStatus = checkStatus;
        this.orangeWarningCheckStatus = orangeWarningCheckStatus;
        this.redWarningCheckStatus = redWarningCheckStatus;
        this.warningStatus = warningStatus;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskExecuteId() {
        return taskExecuteId;
    }

    public void setTaskExecuteId(String taskExecuteId) {
        this.taskExecuteId = taskExecuteId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSubTaskId() {
        return subTaskId;
    }

    public void setSubTaskId(String subTaskId) {
        this.subTaskId = subTaskId;
    }

    public String getSubTaskObjectId() {
        return subTaskObjectId;
    }

    public void setSubTaskObjectId(String subTaskObjectId) {
        this.subTaskObjectId = subTaskObjectId;
    }

    public String getSubTaskRuleId() {
        return subTaskRuleId;
    }

    public void setSubTaskRuleId(String subTaskRuleId) {
        this.subTaskRuleId = subTaskRuleId;
    }

    public Float getResult() {
        return result;
    }

    public void setResult(Float result) {
        this.result = result;
    }

    public Float getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(Float referenceValue) {
        this.referenceValue = referenceValue;
    }

    public Integer getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Integer checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Integer getOrangeWarningCheckStatus() {
        return orangeWarningCheckStatus;
    }

    public void setOrangeWarningCheckStatus(Integer orangeWarningCheckStatus) {
        this.orangeWarningCheckStatus = orangeWarningCheckStatus;
    }

    public Integer getRedWarningCheckStatus() {
        return redWarningCheckStatus;
    }

    public void setRedWarningCheckStatus(Integer redWarningCheckStatus) {
        this.redWarningCheckStatus = redWarningCheckStatus;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
}
