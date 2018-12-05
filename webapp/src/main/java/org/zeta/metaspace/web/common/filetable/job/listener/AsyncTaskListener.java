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

package org.zeta.metaspace.web.common.filetable.job.listener;

import com.google.common.util.concurrent.Service.State;
import org.zeta.metaspace.web.service.filetable.TaskService;

public class AsyncTaskListener extends StatusChangeListener {

    private TaskService taskService;

    public AsyncTaskListener(TaskService taskService, String taskId) {
        super(taskId);
        this.taskService = taskService;
    }

    @Override
    public void starting() {
        super.starting();
        taskService.updateState(getTaskId(), State.STARTING.name());
    }

    @Override
    public void running() {
        super.running();

        taskService.updateState(getTaskId(), State.RUNNING.name());
    }

    @Override
    public void stopping(State from) {
        super.stopping(from);

        taskService.updateState(getTaskId(), State.STOPPING.name());
    }

    @Override
    public void terminated(State from) {
        super.terminated(from);
        taskService.updateState(getTaskId(), State.TERMINATED.name());
    }

    @Override
    public void failed(State from, Throwable failure) {
        super.failed(from, failure);
        taskService.updateState(getTaskId(), State.FAILED.name());
    }
}
