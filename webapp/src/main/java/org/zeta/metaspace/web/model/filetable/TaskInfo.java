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

package org.zeta.metaspace.web.model.filetable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties({"jobId", "ignoreCase"})
public class TaskInfo {

    @XmlElement
    private String taskId;
    private String jobId;
    @XmlElement
    private String uid;
    @XmlElement
    private String state;
    @XmlElement
    private Float progress;


    public TaskInfo() {
    }

    public TaskInfo(String taskId, String jobId, String uid, String state, Float progress) {
        this.taskId = taskId;
        this.jobId = jobId;
        this.uid = uid;
        this.state = state;
        this.progress = progress;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Float getProgress() {
        return progress;
    }

    public void setProgress(Float progress) {
        this.progress = progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskInfo)) {
            return false;
        }
        TaskInfo taskInfo = (TaskInfo) o;
        return Objects.equal(taskId, taskInfo.taskId) &&
               Objects.equal(jobId, taskInfo.jobId) &&
               Objects.equal(uid, taskInfo.uid) &&
               Objects.equal(state, taskInfo.state);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(taskId, jobId, uid, state);
    }


}
