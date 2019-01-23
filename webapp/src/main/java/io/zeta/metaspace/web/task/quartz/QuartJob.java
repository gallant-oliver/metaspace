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
import io.zeta.metaspace.model.dataquality.Template;
import io.zeta.metaspace.model.dataquality.UserRule;
import io.zeta.metaspace.model.table.TableMetadata;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:17
 */

public class QuartJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(QuartJob.class);
    @Autowired
    private DataQualityDAO qualityDao;

    Map<UserRule, List<Double>> resultMap = new HashMap();

    private final int RETRY = 3;
    private final String SEPARATOR = "\\.";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        List<UserRule> rules = null;
        JobKey key = jobExecutionContext.getTrigger().getJobKey();
        Template template = qualityDao.getTemplateByJob(key.getName());
        try {


            String templateId = template.getTemplateId();

            rules = qualityDao.queryTemplateUserRuleById(templateId);
        } catch (SQLException e) {

        }

        for(UserRule rule: rules) {
            int retryCount = 0;
            try {
                retryCount++;
                runJob(rule);
            } catch (Exception e) {
                JobExecutionException execError = new JobExecutionException(e);
                if (retryCount <= RETRY) {
                    execError.setRefireImmediately(true);
                } else {
                    execError.setUnscheduleAllTriggers(true);
                }
                throw e;
            }
        }
        //更新报表结果
        updateReportResult(template, resultMap);
    }

    public void runJob(UserRule rule) {
        try {
            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            switch (jobType) {
                case TABLE_ROW_NUM:
                    ruleResultValue(rule, true, false);
                    break;
                case TABLE_ROW_NUM_CHANGE:
                    ruleResultValueChange(rule, true, false);
                    break;
                case TABLE_ROW_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(rule, true, false);
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
                case TOTAL_VALUE:
                case MIN_VALUE:
                case MAX_VALUE:
                case UNIQUE_VALUE_NUM:
                case EMPTY_VALUE_NUM:
                case DUP_VALUE_NUM:
                    ruleResultValue(rule, true, true);
                    break;

                case AVG_VALUE_CHANGE:
                case TOTAL_VALUE_CHANGE:
                case MIN_VALUE_CHANGE:
                case MAX_VALUE_CHANGE:
                case UNIQUE_VALUE_NUM_CHANGE:
                case EMPTY_VALUE_NUM_CHANGE:
                case DUP_VALUE_NUM_CHANGE:
                    ruleResultValueChange(rule, true, true);
                    break;

                case AVG_VALUE_CHANGE_RATIO:
                case TOTAL_VALUE_CHANGE_RATIO:
                case MIN_VALUE_CHANGE_RATIO:
                case MAX_VALUE_CHANGE_RATIO:
                case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                case EMPTY_VALUE_NUM_CHANGE_RATIO:
                case DUP_VALUE_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(rule, true, true);


                case UNIQUE_VALUE_NUM_RATIO:
                case EMPTY_VALUE_NUM_RATIO:
                case DUP_VALUE_NUM_RATIO:
                    getProportion(rule);



            }
        } catch (Exception e) {

        }
    }

    //规则值计算
    public double ruleResultValue(UserRule rule, boolean record, boolean columnRule) throws AtlasBaseException {
        try {
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            String[] sourceInfo = source.split(SEPARATOR);
            String dbName = sourceInfo[0];
            String tableName = sourceInfo[1];
            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            if(columnRule) {
                String columnName = rule.getRuleColumnName();
                if(jobType.equals(TaskType.UNIQUE_VALUE_NUM) || jobType.equals(TaskType.UNIQUE_VALUE_NUM_CHANGE)
            || jobType.equals(TaskType.UNIQUE_VALUE_NUM_CHANGE) || jobType.equals(TaskType.UNIQUE_VALUE_NUM_RATIO)) {
                    sql = String.format(query, tableName, columnName, columnName, tableName, columnName);
                } else if(jobType.equals(TaskType.DUP_VALUE_NUM) || jobType.equals(TaskType.DUP_VALUE_NUM_CHANGE)
                          || jobType.equals(TaskType.DUP_VALUE_NUM_CHANGE_RATIO) || jobType.equals(TaskType.DUP_VALUE_NUM_RATIO)) {
                    sql = String.format(query, columnName, tableName, columnName, columnName, tableName, columnName);
                }else if(jobType.equals(TaskType.EMPTY_VALUE_NUM) || jobType.equals(TaskType.EMPTY_VALUE_NUM_CHANGE) ||
                jobType.equals(TaskType.EMPTY_VALUE_NUM_CHANGE_RATIO) || jobType.equals(TaskType.EMPTY_VALUE_NUM_RATIO)) {
                    sql = String.format(query, tableName, columnName);
                } else {
                    sql = String.format(query, columnName, tableName);
                }
            } else {
                sql = String.format(query, tableName);
            }
            ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);
            double resultValue = 0;
            while (resultSet.next()) {
                Object object = resultSet.getObject(1);
                resultValue = Double.valueOf(object.toString());
            }
            if(record) {
                List<Double> values = new ArrayList<>();
                values.add(resultValue);
                values.add(resultValue);
                resultMap.put(rule, values);
            }
            return resultValue;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
    //规则值变化
    public double ruleResultValueChange(UserRule rule, boolean record, boolean columnRule) throws AtlasBaseException {
        try {
            double nowValue =  ruleResultValue(rule, false, columnRule);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();

            Double lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue))?0:lastValue;
            Double valueChange  = nowValue - lastValue;
            if(record) {
                recordDataMap(rule, nowValue, valueChange);
            }
            return valueChange;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
    //表行数变化率
    public Double ruleResultChangeRatio(UserRule rule, boolean record, boolean columnRule) throws AtlasBaseException {
        try {
            Double ruleValueChange = ruleResultValueChange(rule, false, columnRule);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            Double lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue))?0:lastValue;
            Double ratio = 0.0;
            if(lastValue != 0) {
                ratio = ruleValueChange/lastValue;
            }
            if(record) {
                recordDataMap(rule, ruleValueChange + lastValue, ratio);
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
                recordDataMap(rule, (double) totalSize, (double)totalSize);
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
                recordDataMap(rule, (double)tableSize, (double)sizeChange);
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
        double lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
        double ratio = 0;
        if(lastValue != 0) {
            ratio = tableSizeChange/lastValue;
        }
        if(record) {
            recordDataMap(rule, tableSizeChange + lastValue, ratio);
        }
        return ratio;
    }

    public void getProportion(UserRule rule) throws AtlasBaseException {
        try {
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            String[] sourceInfo = source.split(SEPARATOR);
            String dbName = sourceInfo[0];
            String tableName = sourceInfo[1];

            double nowNum = ruleResultValue(rule, false, true);
            Double totalNum = 0.0;
            String query = "select count(*) from %s";
            String sql = String.format(query, tableName);
            ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);
            while (resultSet.next()) {
                Object object = resultSet.getObject(1);
                totalNum = Double.valueOf(object.toString());
            }
            double ratio = 0;
            if(totalNum != 0) {
                ratio = nowNum/totalNum;
            }
            List<Double> values = new ArrayList<>();
            values.add(ratio);
            values.add(ratio);
            resultMap.put(rule, values);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    /**
     * 记录规则结果
     * @param rule
     * @param refValue
     * @param resultValue
     */
    public void recordDataMap(UserRule rule, Double refValue, Double resultValue) {
        List<Double> values = new ArrayList<>();
        values.add(refValue);
        values.add(resultValue);
        resultMap.put(rule, values);
    }

    /**
     * 生成报告
     * @param template
     * @return
     */
    public String insertReport(Template template)  {
        String reportId = UUID.randomUUID().toString();
        String templateName = template.getTemplateName();
        long currentTime = System.currentTimeMillis();
        String reportName = templateName + currentTime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String reportProduceDate = sdf.format(currentTime);
        Report report = new Report();
        report.setReportId(reportId);
        report.setReportName(reportName);
        report.setTemplateId(template.getTemplateId());
        report.setTemplateName(templateName);
        report.setPeriodCron(template.getPeriodCron());
        report.setBuildType(template.getBuildType());
        report.setSource(template.getSource());
        report.setReportProduceDate(reportProduceDate);
        qualityDao.insertReport(report);
        return reportId;
    }
    /**
     * 填充规则结果model
     * @param rule
     * @param refValue
     * @param resultValue
     * @return
     */
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

    /**
     * 通过规则计算规则所处状态
     * @param resultValue
     * @param reportRule
     * @return
     */
    public int getReportRuleStatus(double resultValue, Report.ReportRule reportRule) {
        int status = 0;

        return status;
    }

    /**
     * 报告规则结果入库并更新报告告警
     * @param template
     * @param resultMap
     */
    @Transactional
    public void updateReportResult(Template template,Map<UserRule, List<Double>> resultMap) {
        try {
            String reportId = insertReport(template);
            for(UserRule rule : resultMap.keySet()) {
                List<Double> values = resultMap.get(rule);
                double refValue = values.get(0);
                double resultValue = values.get(1);
                Report.ReportRule reportRule = getReportRule(rule, refValue, resultValue);
                //规则报告状态
                int status = getReportRuleStatus(resultValue, reportRule);
                reportRule.setReportRuleStatus(status);
                qualityDao.insertRuleReport(reportId, reportRule);
            }
            qualityDao.updateAlerts(reportId);
        } catch (Exception e) {

        }
    }
}
