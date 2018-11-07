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

package org.apache.atlas.web.model.filetable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class AbstractJobInfo {
    private String jobId;
    private String uid;
    private TaskInfo taskInfo;
    private String error;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractJobInfo)) {
            return false;
        }
        AbstractJobInfo that = (AbstractJobInfo) o;
        return Objects.equal(jobId, that.jobId) &&
               Objects.equal(uid, that.uid) &&
               Objects.equal(taskInfo, that.taskInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(jobId, uid, taskInfo);
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public String getUid() {
        if (Strings.isNullOrEmpty(uid) && taskInfo != null) {
            return taskInfo.getUid();
        } else {
            return uid;
        }
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
