package io.zeta.metaspace.web.task.init;

import io.zeta.metaspace.web.scheduler.MetaspaceScheduler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class StatisticsJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsJob.class);
    @Autowired
    private MetaspaceScheduler metaspaceScheduler;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            metaspaceScheduler.insertTableMetadataStat();
        } catch (Exception e) {
            LOG.error("统计信息出错", e);
        }
    }

}
