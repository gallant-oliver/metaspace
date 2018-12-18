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


package io.zeta.metaspace.web.common.filetable.job.listener;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusChangeListener extends Listener {

    protected static final Logger LOGGER = LoggerFactory.getLogger(StatusChangeListener.class);

    private String taskId;

    public StatusChangeListener(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }


    protected String getName() {
        return "taskId[" + taskId + "]";
    }

    @Override
    public void starting() {
        super.starting();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getName() + " state " + Service.State.STARTING);
        }
    }

    @Override
    public void running() {
        super.running();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getName() + " state " + Service.State.RUNNING);
        }
    }

    @Override
    public void stopping(Service.State state) {
        super.stopping(state);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getName() + " state " + Service.State.STOPPING + " state " + state.name());
        }
    }

    @Override
    public void terminated(Service.State state) {
        super.terminated(state);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getName() + " state " + Service.State.TERMINATED + " state " + state.name());
        }
    }

    @Override
    public void failed(Service.State state, Throwable e) {
        super.failed(state, e);
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(getName() + " state " + Service.State.FAILED + " state " + state.name() + " with error[" + e.getMessage() + "]", e);
        }
    }
}
