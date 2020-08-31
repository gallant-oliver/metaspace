package io.zeta.metaspace.web.task.init;

import io.zeta.metaspace.web.task.quartz.QuartzManager;
import org.apache.atlas.annotation.ConditionalOnAtlasMethodProperty;
import org.quartz.*;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import java.io.IOException;
import java.util.Properties;

@Configuration
public class SchedulerConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SchedulerConfig.class);
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired @Qualifier("Scheduler")
    private Scheduler scheduler;
    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean(name = "SchedulerFactory")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setQuartzProperties(quartzProperties());
        factory.setJobFactory(springBeanJobFactory());
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new FileSystemResource(System.getProperty("atlas.conf") + "/quartz.properties"));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /*
     * quartz初始化监听器
     * 监听到工程的启动，在工程停止再启动时可以让已有的定时任务继续进行
     */
    @Bean
    @ConditionalOnAtlasMethodProperty(property = "metaspace.quartz.task.enable",isDefault = true)
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }

    /*
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean(name = "Scheduler")
    public Scheduler scheduler() throws IOException {
        SchedulerFactoryBean schedulerFactoryBean = schedulerFactoryBean();
        return schedulerFactoryBean.getScheduler();
    }

    @Bean
    public QuartzManager quartzManager() {
        return new QuartzManager();
    }

    /*
     *每晚十二点自动执行统计信息任务
     */
    @Bean
    @ConditionalOnAtlasMethodProperty(property = "metaspace.quartz.task.enable",isDefault = true)
    public String autoStatistics() throws IOException, SchedulerException {
        JobKey jobKey = new JobKey("统计信息任务", "元数据分析");
        if (scheduler.getJobDetail(jobKey) == null) {
            LOG.info("添加统计信息任务");
            JobDetail jobDetail = JobBuilder.newJob(StatisticsJob.class).withIdentity("统计信息任务", "元数据分析").build();
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity("统计信息调度器", "元数据分析");
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"));
            Trigger trigger = triggerBuilder.build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            LOG.info("统计信息任务已添加");
        }
        return "start";
    }
    /*
     *每晚十二点自动执行统计信息任务
     */
    @Bean
    @ConditionalOnAtlasMethodProperty(property = "metaspace.quartz.task.enable",isDefault = true)
    public String autoHomeStatistics() throws IOException, SchedulerException {
        JobKey jobKey = new JobKey("首页统计信息任务", "系统数据分析");
        if (scheduler.getJobDetail(jobKey) == null) {
            LOG.info("添加首页统计信息任务");
            JobDetail jobDetail = JobBuilder.newJob(HomeStatisticsJob.class).withIdentity("首页统计信息任务", "系统数据分析").build();
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity("首页统计信息调度器", "首页分析");
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"));
            Trigger trigger = triggerBuilder.build();
            scheduler.scheduleJob(jobDetail, trigger);
        } else {
            LOG.info("首页统计信息任务已添加");
        }
        return "start";
    }
}
