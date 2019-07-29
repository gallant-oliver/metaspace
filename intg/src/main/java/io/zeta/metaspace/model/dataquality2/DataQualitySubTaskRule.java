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
 * @date 2019/7/24 18:06
 */
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 18:06
 */
public class DataQualitySubTaskRule {
    private String id;
    private String subTaskId;
    private String ruleId;
    private String checkThreshold;
    private Integer orangeCheckType;
    private Integer orangeCheckExpression;
    private String orangeThreshold;
    private String orangeWarningGroupIds;
    private Integer redCheckType;
    private Integer redCheckExpression;
    private String redThreshold;
    private String redWarningGroupIds;
    private Integer sequence;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    private Boolean delete;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubTaskId() {
        return subTaskId;
    }

    public void setSubTaskId(String subTaskId) {
        this.subTaskId = subTaskId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getCheckThreshold() {
        return checkThreshold;
    }

    public void setCheckThreshold(String checkThreshold) {
        this.checkThreshold = checkThreshold;
    }

    public Integer getOrangeCheckType() {
        return orangeCheckType;
    }

    public void setOrangeCheckType(Integer orangeCheckType) {
        this.orangeCheckType = orangeCheckType;
    }

    public Integer getOrangeCheckExpression() {
        return orangeCheckExpression;
    }

    public void setOrangeCheckExpression(Integer orangeCheckExpression) {
        this.orangeCheckExpression = orangeCheckExpression;
    }

    public String getOrangeThreshold() {
        return orangeThreshold;
    }

    public void setOrangeThreshold(String orangeThreshold) {
        this.orangeThreshold = orangeThreshold;
    }

    public String getOrangeWarningGroupIds() {
        return orangeWarningGroupIds;
    }

    public void setOrangeWarningGroupIds(String orangeWarningGroupIds) {
        this.orangeWarningGroupIds = orangeWarningGroupIds;
    }

    public Integer getRedCheckType() {
        return redCheckType;
    }

    public void setRedCheckType(Integer redCheckType) {
        this.redCheckType = redCheckType;
    }

    public Integer getRedCheckExpression() {
        return redCheckExpression;
    }

    public void setRedCheckExpression(Integer redCheckExpression) {
        this.redCheckExpression = redCheckExpression;
    }

    public String getRedThreshold() {
        return redThreshold;
    }

    public void setRedThreshold(String redThreshold) {
        this.redThreshold = redThreshold;
    }

    public String getRedWarningGroupIds() {
        return redWarningGroupIds;
    }

    public void setRedWarningGroupIds(String redWarningGroupIds) {
        this.redWarningGroupIds = redWarningGroupIds;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
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

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
}
