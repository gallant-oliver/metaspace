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


import io.zeta.metaspace.web.service.DataQualityService;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.INTERNAL;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

import javax.print.attribute.standard.JobKOctets;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:22
 */
public class QuartzManager {

    @Autowired @Qualifier("Scheduler")
    private Scheduler scheduler;

    public void addJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName,
                       Class jobClass, String cron) {
        try {
            //任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            //触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            //触发器名，触发器组
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            Trigger trigger = null;
            //触发器时间设定
            if( Objects.isNull(cron) || StringUtils.isEmpty(cron)) {

                trigger = triggerBuilder.newTrigger()
                        //重复执行的次数，因为加入任务的时候马上执行了，所以不需要重复，否则会多一次。
                        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(3).withRepeatCount(0))
                        .startNow().build();
            } else {
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                trigger = triggerBuilder.build();
            }
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


    public void addCronJobWithTimeRange(String jobName, String jobGroupName, String triggerName, String triggerGroupName,
                                        Class jobClass, String cron, Integer level, Timestamp startTime, Timestamp endTime) {
        try {
            //任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            //触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            //触发器名，触发器组
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            Trigger trigger = triggerBuilder
                                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                                .withPriority(level)
                                .startAt(startTime)
                                .endAt(endTime)
                                .build();

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

    public void addSimpleJob(String jobName, String jobGroupName, Class jobClass) {
        try {
            //任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                    .startNow().build();
            scheduler.scheduleJob(jobDetail, trigger);
            //启动
            if(!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public Date getJobEndTime(String triggerName, String triggerGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if(Objects.nonNull(trigger)) {
                return trigger.getEndTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public Date getJobNextExecuteTime(String triggerName, String triggerGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if(Objects.nonNull(trigger)) {
                return trigger.getNextFireTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Date getJobLastExecuteTime(String triggerName, String triggerGroupName) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if(Objects.nonNull(trigger)) {
                return trigger.getPreviousFireTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public void handleNullErrorTask(JobKey jobKey) {
        String jobName = jobKey.getName();
        String jobGroupName = DataQualityService.JOB_GROUP_NAME + jobName;
        String triggerName  = DataQualityService.TRIGGER_NAME + jobName;
        String triggerGroupName = DataQualityService.TRIGGER_GROUP_NAME + jobName;
        removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
