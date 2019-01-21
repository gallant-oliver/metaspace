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

import io.zeta.metaspace.model.dataquality.Report;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.table.TableMetadata;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:17
 */

public class QuartJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(QuartJob.class);

    DataQualityDAO qualityDao;

    private final int RETRY = 3;
    private final String SEPARATOR = "\\.";
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        int retryCount = 0;
        try {
            retryCount++;
            JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            String reportId = (String)dataMap.get("reportId");
            List<UserRule> rules = (List<UserRule>) dataMap.get("ruleList");
            qualityDao = (DataQualityDAO) dataMap.get("dao");
            for(UserRule rule: rules) {
                runExactJob(rule);
            }
            //更新报表结果
            updateReportResult(reportId);
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

    //更新report表告警数量
    public void updateReportResult(String reportId) {
        try {
            qualityDao.updateAlerts(reportId);
        } catch (Exception e) {

        }
    }

    public void runExactJob(UserRule rule) {
        try {
            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            switch (jobType) {
                case TABLE_ROW_NUM:
                    ruleResultValue(rule, true);
                    break;
                case TABLE_ROW_NUM_CHANGE:
                    tableRowNumChange(rule, true);
                    break;
                case TABLE_ROW_NUM_CHANGE_RATIO:
                    tableRowNumChangeRatio(rule, true);
                    break;

                case TABLE_SIZE:
                    tableSize(rule, true);
                    break;
                case TABLE_SIZE_CHANGE:
                    tableSizeChange(rule, true);
                    break;
                case TABLE_SIZE_CHANGE_RATIO:
                    tableSizeChangeRatio(rule, true);
                    break;

                case AVG_VALUE:
                    ruleResultValue(rule, true);
                    break;

                case TOTAL_VALUE:

                    break;

                case MIN_VALUE:

                    break;

                case MAX_VALUE:

                    break;

                case UNIQUE_VALUE_NUM:

                    break;

                case EMPTY_VALUE_NUM:

                    break;

                case DUP_VALUE_NUM:

                    break;

            }
        } catch (Exception e) {

        }
    }

    //规则计算
    public int ruleResultValue(UserRule rule, boolean record) throws AtlasBaseException {
        try {
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            String[] sourceInfo = source.split(SEPARATOR);
            String dbName = sourceInfo[0];
            String tableName = sourceInfo[1];
            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = String.format(query, tableName);
            ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);
            int rowNum = 0;
            while (resultSet.next()) {
                Object object = resultSet.getObject(1);
                rowNum = Integer.parseInt(object.toString());
            }
            if(record) {
                generateReport(rule, rowNum, rowNum);
            }

            return rowNum;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
    //表行数变化
    public int tableRowNumChange(UserRule rule, boolean record) throws AtlasBaseException {
        try {
            int rowNum = ruleResultValue(rule, false);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            int lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
            int numChange = rowNum - lastValue;
            if(record) {
                generateReport(rule, rowNum, numChange);
            }
            return numChange;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
    //表行数变化率
    public double tableRowNumChangeRatio(UserRule rule, boolean record) throws AtlasBaseException {
        try {
            int numChange = tableRowNumChange(rule, false);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            int lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
            double ratio = 0;
            if(lastValue != 0) {
                ratio = numChange/lastValue;
            }
            if(record) {
                generateReport(rule,numChange + lastValue, ratio);
            }
            return ratio;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    //表大小
    public long tableSize(UserRule rule, boolean record) throws AtlasBaseException {
        try {
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            TableMetadata metadata = HiveJdbcUtils.systemMetadata(source);
            //表数据量
            long totalSize = metadata.getTotalSize();
            if(record) {
                generateReport(rule, totalSize, totalSize);
            }
            return totalSize;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
    //表大小变化
    public long tableSizeChange(UserRule rule, boolean record) throws AtlasBaseException {
        try {
            long tableSize = tableSize(rule, false);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            long lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            long sizeChange = tableSize - lastValue;
            if(record) {
                generateReport(rule, tableSize, sizeChange);
            }
            return sizeChange;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
    //表大小变化率
    public double tableSizeChangeRatio(UserRule rule, boolean record) throws AtlasBaseException {
        long tableSizeChange = tableSizeChange(rule, record);
        String templateId = rule.getTemplateId();
        String templateRuleId = rule.getRuleId();
        int lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
        double ratio = 0;
        if(lastValue != 0) {
            ratio = tableSizeChange/lastValue;
        }
        if(record) {
            generateReport(rule,tableSizeChange + lastValue, ratio);
        }
        return ratio;
    }




    public Report.ReportRule getReportRule(UserRule rule, double refValue, double resultValue) {
        Report.ReportRule reportRule = new Report.ReportRule();
        //ruleId
        String ruleId = UUID.randomUUID().toString();
        reportRule.setRuleId(ruleId);
        //ruleType
        reportRule.setRuleType(rule.getRuleType());
        //ruleName
        reportRule.setRuleName(rule.getRuleName());
        //ruleInfo
        reportRule.setRuleInfo(rule.getRuleInfo());
        //ruleColumnName
        reportRule.setRuleColumnName(rule.getRuleColumnName());
        //ruleColumnType
        reportRule.setRuleColumnType(rule.getRuleColumnType());
        //ruleCheckType
        reportRule.setRuleCheckType(rule.getRuleCheckType());
        //ruleCheckExpression
        reportRule.setRuleCheckExpression(rule.getRuleCheckExpression());
        //ruleCheckThresholdUnit
        reportRule.setRuleCheckThresholdUnit(rule.getRuleCheckThresholdUnit());
        //ruleColumnId
        reportRule.setRuleColumnId(rule.getRuleColumnId());
        //templateRuleId
        reportRule.setTemplateRuleId(rule.getRuleId());
        //refValue
        reportRule.setRefValue(refValue);
        //reportRuleValue
        reportRule.setReportRuleValue(resultValue);
        return reportRule;
    }

    public void generateReport(UserRule rule, double refValue, double resultValue) {
        try {
            Report.ReportRule reportRule = getReportRule(rule, refValue, resultValue);
            //规则报告状态
            int status = getReportRuleStatus(resultValue, reportRule);
            reportRule.setReportRuleStatus(status);
            qualityDao.insertRuleReport(rule.getReportId(), reportRule);
        } catch (Exception e) {

        }
    }

    public int getReportRuleStatus(double resultValue, Report.ReportRule reportRule) {
        int status = 0;

        return status;
    }

}
