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
 * @date 2019/1/17 10:17
 */
package io.zeta.metaspace.web.task.quartz;

import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:17
 */

public class QuartJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(QuartJob.class);

    DataQualityDAO qualityDao;

    private final int RETRY = 3;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        int retryCount = 0;
        try {
            retryCount++;
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            UserRule rule = (UserRule) dataMap.get("rule");
            qualityDao = (DataQualityDAO) dataMap.get("dao");
            runExactJob(rule);


        } catch (Exception e) {
            JobExecutionException execError = new JobExecutionException(e);
            if(retryCount <= RETRY) {
                execError.setRefireImmediately(true);
            } else {
                execError.setUnscheduleAllTriggers(true);
            }
            throw e;
        }
    }

    public void runExactJob(UserRule rule) {
        TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
        switch (jobType) {
            case TABLE_ROW_NUM_CHANGE_RATIO:
               tableRowChangeRatio(rule);
        }
    }

    public void tableRowChangeRatio(UserRule rule) {
        try {
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            String[] sourceInfo = source.split("\\.");
            String dbName = sourceInfo[0];
            String tableName = sourceInfo[1];
            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = String.format(query, tableName);
            ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);
            int currentNum = 0;
            while (resultSet.next()) {
                Object object = resultSet.getObject(1);
                currentNum = Integer.parseInt(object.toString());
            }
            System.out.println("current num:" + currentNum);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
