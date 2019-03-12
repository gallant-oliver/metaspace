package io.zeta.metaspace.web.task.init;

import io.zeta.metaspace.web.service.HomePageService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class HomeStatisticsJob implements Job {
    @Autowired
    HomePageService homePageService;
    private final int RETRY = 3;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        int retryCount=0;
        try {
            retryCount++;
            homePageService.statisticsJob();
        }catch (Exception e){
            JobExecutionException execError = new JobExecutionException(e);
            if (retryCount <= RETRY) {
                execError.setRefireImmediately(true);
            } else {
                execError.setUnscheduleAllTriggers(true);
            }
        }
    }
}
