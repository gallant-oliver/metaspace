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

package org.apache.atlas.web.common.filetable;

import com.gridsum.gdp.library.commons.data.generic.GenericData;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Objects;
import com.google.common.util.concurrent.Service;

import java.sql.Timestamp;
import java.util.Comparator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
@JsonPropertyOrder({"taskId", "uid", "taskType", "state", "progress", "error", "initialized", "lastExecuteTime", "createTime"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({"jobId", "ignoreCase"})
public class TaskInfo extends GenericData implements Comparator<TaskInfo>, Comparable<TaskInfo> {
    @XmlElement
    private String taskId;
    private String jobId;
    @XmlElement
    private String uid;
    @XmlElement
    private Service.State state;
    @XmlElement
    private Float progress;
    @XmlElement(nillable = true)
    private String error;
    @XmlElement
    private boolean initialized = false;
    @XmlElement
    private Timestamp lastExecuteTime;
    @XmlElement(nillable = true)
    private Timestamp createTime;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
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

    public Service.State getState() {
        return state;
    }

    public void setState(Service.State state) {
        this.state = state;
    }

    public Float getProgress() {
        return progress;
    }

    public void setProgress(Float progress) {
        this.progress = progress;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Timestamp getLastExecuteTime() {
        return lastExecuteTime;
    }

    public void setLastExecuteTime(Timestamp lastExecuteTime) {
        this.lastExecuteTime = lastExecuteTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
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
        return Objects.equal(initialized, taskInfo.initialized) &&
               Objects.equal(taskId, taskInfo.taskId) &&
               Objects.equal(jobId, taskInfo.jobId) &&
               Objects.equal(uid, taskInfo.uid) &&
               Objects.equal(state, taskInfo.state) &&
               Objects.equal(error, taskInfo.error) &&
               Objects.equal(lastExecuteTime, taskInfo.lastExecuteTime) &&
               Objects.equal(createTime, taskInfo.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(taskId, jobId, uid, state, error, initialized, lastExecuteTime, createTime);
    }

    @Override
    public int compareTo(TaskInfo o) {
        if (createTime == null) {
            return -1;
        }
        return createTime.compareTo(o.createTime);
    }

    @Override
    public int compare(TaskInfo o1, TaskInfo o2) {
        if (o1 == null) {
            return -1;
        }
        return o1.createTime.compareTo(o2.createTime);
    }
}
