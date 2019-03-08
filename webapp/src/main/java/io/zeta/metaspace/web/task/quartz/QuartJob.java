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

import io.zeta.metaspace.model.dataquality.*;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:17
 */

public class QuartJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(QuartJob.class);
    private static final Integer STATUS_START = 1;
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
        String templateId = template.getTemplateId();
        try {
            //设置模板状态为【报表生成中】
            qualityDao.updateTemplateStatus(TemplateStatus.GENERATING_REPORT.code, templateId);

            rules = qualityDao.queryTemplateUserRuleById(templateId);
        } catch (SQLException e) {

        }
        int totalStep = rules.size() + 1;
        for (int i=0; i<rules.size(); i++) {
            //根据模板状态判断是否继续运行
            Integer status = qualityDao.getTemplateStatus(templateId);
            if(Objects.nonNull(status) && status.equals(TemplateStatus.SUSPENDING.code)) {
                qualityDao.updateFinishedPercent(template.getTemplateId(), 0F);
                return;
            }
            UserRule rule = rules.get(i);
            int retryCount = 0;
            try {
                List<Double> thresholds = qualityDao.queryTemplateThresholdByRuleId(rule.getRuleId());
                rule.setRuleCheckThreshold(thresholds);
                retryCount++;
                runJob(rule);
                float ratio = (float)(i+1)/totalStep;
                qualityDao.updateFinishedPercent(template.getTemplateId(), ratio);
            } catch (SQLException e) {

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
        try {
            updateReportResult(template, resultMap);
            qualityDao.updateFinishedPercent(template.getTemplateId(), (float) 1);
            String cron = qualityDao.getCronByTemplateId(templateId);
            if (Objects.isNull(cron)) {
                //设置模板状态为【已完成】
                qualityDao.updateTemplateStatus(TemplateStatus.FINISHED.code, templateId);
            } else {
                //设置模板状态为【已启用】
                qualityDao.updateTemplateStatus(TemplateStatus.RUNNING.code, templateId);
            }
        } catch (Exception e) {

        }
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
                    break;

                case UNIQUE_VALUE_NUM_RATIO:
                case EMPTY_VALUE_NUM_RATIO:
                case DUP_VALUE_NUM_RATIO:
                    getProportion(rule);
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
            //throw new RuntimeException();
        }
    }

    //规则值计算
    public double ruleResultValue(UserRule rule, boolean record, boolean columnRule) throws SQLException {
        try {
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            String[] sourceInfo = source.split(SEPARATOR);
            String dbName = sourceInfo[0];
            String tableName = sourceInfo[1];
            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            if (columnRule) {
                String columnName = rule.getRuleColumnName();

                switch (jobType) {
                    case UNIQUE_VALUE_NUM:
                    case UNIQUE_VALUE_NUM_CHANGE:
                    case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                    case UNIQUE_VALUE_NUM_RATIO:
                        sql = String.format(query, tableName, columnName, columnName, tableName, columnName);
                        break;
                    case DUP_VALUE_NUM:
                    case DUP_VALUE_NUM_CHANGE:
                    case DUP_VALUE_NUM_CHANGE_RATIO:
                    case DUP_VALUE_NUM_RATIO:
                        sql = String.format(query, columnName, tableName, columnName, columnName, tableName, columnName);
                        break;
                    case EMPTY_VALUE_NUM:
                    case EMPTY_VALUE_NUM_CHANGE:
                    case EMPTY_VALUE_NUM_CHANGE_RATIO:
                    case EMPTY_VALUE_NUM_RATIO:
                        sql = String.format(query, tableName, columnName);
                        break;
                    default:
                        sql = String.format(query, columnName, tableName);
                        break;
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
            if (record) {
                List<Double> values = new ArrayList<>();
                values.add(resultValue);
                values.add(resultValue);
                resultMap.put(rule, values);
            }
            return resultValue;
        } catch (SQLException e) {
            throw e;
        }catch (Exception e) {
            LOG.info(e.getMessage());
            throw new RuntimeException();
        }
    }

    //规则值变化
    public double ruleResultValueChange(UserRule rule, boolean record, boolean columnRule) throws AtlasBaseException {
        try {
            double nowValue = ruleResultValue(rule, false, columnRule);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();

            Double lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            double valueChange = Math.abs(nowValue - lastValue);
            if (record) {
                recordDataMap(rule, nowValue, valueChange);
            }
            return valueChange;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "计算出现异常");
        }
    }

    //规则值变化率
    public Double ruleResultChangeRatio(UserRule rule, boolean record, boolean columnRule) throws AtlasBaseException {
        try {
            Double ruleValueChange = ruleResultValueChange(rule, false, columnRule);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            Double lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            double ratio = 0;
            if (lastValue != 0) {
                ratio = ruleValueChange / lastValue;
            }
            if (record) {
                recordDataMap(rule, ruleValueChange + lastValue, ratio);
            }
            return ratio;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "计算出现异常");
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
            if (record) {
                recordDataMap(rule, (double) totalSize, (double) totalSize);
            }
            return totalSize;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表大小失败");
        }
    }

    //表大小变化
    public Double tableSizeChange(UserRule rule, boolean record) throws AtlasBaseException {
        try {
            long tableSize = tableSize(rule, false);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            Double lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            double sizeChange = Math.abs(tableSize - lastValue);
            if (record) {
                recordDataMap(rule, (double) tableSize, sizeChange);
            }
            return sizeChange;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表大小失败");
        }
    }

    //表大小变化率
    public double tableSizeChangeRatio(UserRule rule, boolean record) throws AtlasBaseException {
        Double tableSizeChange = tableSizeChange(rule, false);
        String templateId = rule.getTemplateId();
        String templateRuleId = rule.getRuleId();
        Double lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
        lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
        double ratio = 0;
        if (lastValue != 0) {
            ratio = tableSizeChange / lastValue;
        }
        if (record) {
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
            if (totalNum != 0) {
                ratio = nowNum / totalNum;
            }
            List<Double> values = new ArrayList<>();
            values.add(ratio);
            values.add(ratio);
            resultMap.put(rule, values);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "计算出现异常");
        }
    }

    /**
     * 记录规则结果
     *
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
     *
     * @param template
     * @return
     */
    public Report insertReport(Template template) {
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

        return report;
    }

    /**
     * 填充规则结果model
     *
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
     *
     * @param resultValue
     * @return
     */
    public RuleStatus getReportRuleStatus(double resultValue, UserRule rule) throws RuntimeException {
        RuleStatus ruleStatus = null;
        try {

            int ruleCheckType = rule.getRuleCheckType();
            RuleCheckType ruleCheckTypeByCode = RuleCheckType.getRuleCheckTypeByCode(ruleCheckType);
            List<Double> ruleCheckThreshold = rule.getRuleCheckThreshold();
            switch (ruleCheckTypeByCode) {
                case FIX: {
                    CheckExpression expressionByCode = CheckExpression.getExpressionByCode(rule.getRuleCheckExpression());
                    switch (expressionByCode) {
                        case EQU: {
                            if (resultValue == ruleCheckThreshold.get(0))
                                ruleStatus = RuleStatus.NORMAL;
                            else
                                ruleStatus = RuleStatus.RED;
                            break;
                        }
                        case NEQ: {
                            if (resultValue != ruleCheckThreshold.get(0))
                                ruleStatus = RuleStatus.NORMAL;
                            else
                                ruleStatus = RuleStatus.RED;
                            break;
                        }
                        case GTR: {
                            if (resultValue > ruleCheckThreshold.get(0))
                                ruleStatus = RuleStatus.NORMAL;
                            else
                                ruleStatus = RuleStatus.RED;
                            break;
                        }
                        case GER: {
                            if (resultValue >= ruleCheckThreshold.get(0))
                                ruleStatus = RuleStatus.NORMAL;
                            else
                                ruleStatus = RuleStatus.RED;
                            break;
                        }
                        case LSS: {
                            if (resultValue < ruleCheckThreshold.get(0))
                                ruleStatus = RuleStatus.NORMAL;
                            else
                                ruleStatus = RuleStatus.RED;
                            break;
                        }
                        case LEQ: {
                            if (resultValue <= ruleCheckThreshold.get(0))
                                ruleStatus = RuleStatus.NORMAL;
                            else
                                ruleStatus = RuleStatus.RED;
                            break;
                        }
                    }
                    break;
                }
                case FLU: {
                    Double o = ruleCheckThreshold.get(0);
                    Double r = ruleCheckThreshold.get(1);
                    if (resultValue <= o) {
                        ruleStatus = RuleStatus.NORMAL;
                    } else if (resultValue > o && resultValue <= r) {
                        ruleStatus = RuleStatus.ORANGE;
                    } else {
                        ruleStatus = RuleStatus.RED;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LOG.info(e.getMessage(),e);
        }
        if (ruleStatus == null) throw new RuntimeException();
        return ruleStatus;
    }

    /**
     * 报告规则结果入库并更新报告告警
     *
     * @param template
     * @param resultMap
     */

    public void updateReportResult(Template template, Map<UserRule, List<Double>> resultMap) throws RuntimeException {
        try {
            List<Report.ReportRule> list=new ArrayList<>();Report report = insertReport(template);
            Long interval = 0L;
            for (UserRule rule : resultMap.keySet()) {
                List<Double> values = resultMap.get(rule);
                double refValue = values.get(0);
                double resultValue = values.get(1);

                Report.ReportRule reportRule = getReportRule(rule, refValue, resultValue);
                RuleStatus status = getReportRuleStatus(resultValue, rule);
                reportRule.setReportRuleStatus(status.getCode());
                Long generateTime = System.currentTimeMillis();
                generateTime += (interval++);
                reportRule.setGenerateTime(generateTime);
                List<Double> ruleCheckThreshold = rule.getRuleCheckThreshold();
                reportRule.setRuleCheckThreshold(ruleCheckThreshold);
                list.add(reportRule);
            }
            addReportByDao(report,list);
        } catch (Exception e) {
            LOG.info(e.getMessage());
            throw new RuntimeException();
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void addReportByDao(Report report, List<Report.ReportRule> list ) throws SQLException {
        if(list.size() == 0)
            return;

        qualityDao.insertReport(report);
        //设置report开启告警
        qualityDao.updateAlertStatus(report.getReportId(), STATUS_START);
        for (Report.ReportRule reportRule : list) {
            qualityDao.insertRuleReport(report.getReportId(), reportRule);
            for (Double threshold : reportRule.getRuleCheckThreshold()) {
                qualityDao.insertReportThreshold(threshold, reportRule.getRuleId());
            }
        }
        qualityDao.updateAlerts(report.getReportId());
    }

}
