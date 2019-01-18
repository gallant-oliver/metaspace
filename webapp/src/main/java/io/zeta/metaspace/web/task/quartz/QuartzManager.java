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
 * @date 2019/1/17 10:22
 */
package io.zeta.metaspace.web.task.quartz;

import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:22
 */
public class QuartzManager {
    private static String JOB_GROUP_NAME = "METASPACE_JOBGROUP";
    private static String TRIGGER_NAME = "METASPACE_TRIGGER";
    private static String TRIGGER_GROUP_NAME = "METASPACE_TRIGGERGROUP";
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private DataQualityDAO qualityDao;
    public void addJob(UserRule rule, String jobName, String jobGroupName, String triggerName, String triggerGroupName,
                       Class jobClass, String cron) {
        try {
            //任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();

            jobDetail.getJobDataMap().put("rule", rule);
            jobDetail.getJobDataMap().put("dao", qualityDao);
            //触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

            TriggerKey triggerKey = new TriggerKey(TRIGGER_NAME, TRIGGER_GROUP_NAME);
            Trigger triger = scheduler.getTrigger(triggerKey);
            //触发器名，触发器组
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            triggerBuilder.startNow();
            //触发器时间设定
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            //创建Trigger对象
            CronTrigger trigger = (CronTrigger)triggerBuilder.build();
            //调度器设置JobDetail和Trigger
            scheduler.scheduleJob(jobDetail, trigger);

            //启动
            if(!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addJob(UserRule rule, Class jobClass, String cron) {
        String ruleId = rule.getRuleId();
        String jobName = rule.getRuleName() + ruleId;
        String jobGroupName = JOB_GROUP_NAME + ruleId;
        String triggerName  = TRIGGER_NAME + ruleId;
        String triggerGroupName = TRIGGER_GROUP_NAME + ruleId;
        addJob(rule, jobName, jobGroupName, triggerName, triggerGroupName, jobClass, cron);
    }

    /**
     * 修改一个任务的触发时间
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     * @param cron
     */
    public void modifyJobTime(String jobName, String jobGroupName, String triggerName, String triggerGroupName, String cron) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger)scheduler.getTrigger(triggerKey);
            if(trigger == null) {
                return;
            }

            String oldTime = trigger.getCronExpression();
            if(!oldTime.equalsIgnoreCase(cron)) {
                //触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                //触发器名，触发器组
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                //触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                //创建Trigger对象
                trigger = (CronTrigger)triggerBuilder.build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停任务
     * @param jobName
     * @param jobGroupName
     */
    public void pauseJob(String jobName, String jobGroupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            scheduler.pauseJob(jobKey);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * 恢复任务
     * @param jobName
     * @param jobGroupName
     */
    public void resumeJob(String jobName, String jobGroupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            scheduler.resumeJob(jobKey);
        } catch (Exception e) {

        }
    }

    /**
     * 移除一个任务
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     */
    public void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            //停止触发器
            scheduler.pauseTrigger(triggerKey);
            //移除触发器
            scheduler.unscheduleJob(triggerKey);
            //删除任务
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (Exception e) {

        }
    }

    /**
     * 启动所有定时任务
     */
    public void startJobs() {
        try {
            scheduler.start();
        } catch (Exception e) {

        }
    }

    /**
     * 关闭所有定时任务
     */
    public void shutdownJobs() {
        try {
            if(!scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
