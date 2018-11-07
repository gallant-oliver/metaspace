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

package org.apache.atlas.web.common.filetable.job;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;
import org.apache.atlas.web.service.filetable.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public abstract class AnalysisJob extends AbstractExecutionThreadService implements Callable<Service.State> {

    protected static final Logger LOGGER = LoggerFactory.getLogger("AnalysisJob");

    private String jobId;
    protected TaskService taskService;


    protected void verify() {
        if (this.taskService == null) {
            throw new IllegalStateException("taskService is null");
        }
    }

    protected void init(TaskService taskService, String jobId) {
        this.taskService = taskService;
        this.jobId = jobId;
    }

    public abstract String description();

    public abstract void tryClose() throws Exception;

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return "Job[" + jobId + "]";
    }

    public String getJobId() {
        return jobId;
    }


    @Override
    protected String serviceName() {
        return getName();
    }

    @Override
    public State call() throws Exception {
        run();
        State state = state();
        return state;
    }
}
