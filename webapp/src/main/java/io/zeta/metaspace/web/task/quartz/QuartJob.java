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

import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.dataquality.*;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.web.dao.DataQualityDAO;
import io.zeta.metaspace.web.service.DataQualityService;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.QualityEngine;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/17 10:17
 */

@Deprecated
public class QuartJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(QuartJob.class);
    private static final Integer STATUS_START = 1;
    @Autowired
    private DataQualityDAO qualityDao;
    @Autowired
    QuartzManager quartzManager;
    private static Configuration conf;
    private static String engine;

    @Autowired
    DataQualityService qualityService;
    Map<UserRule, List<Double>> resultMap = new LinkedHashMap<>();

    private final int RETRY = 3;
    private final String SEPARATOR = "\\.";

    Map<String, Double> columnType2Result = new HashMap<>();
    static {
        try {
            conf = ApplicationProperties.get();
        }  catch (Exception e) {

        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        JobKey key = jobExecutionContext.getTrigger().getJobKey();
        Template template = qualityDao.getTemplateByJob(key.getName());
        if(Objects.isNull(template)) {
            handleNullErrorTemplate(key);
            LOG.warn("任务名为" + key.getName() + "所属模板已被删除,无法继续执行任务");
            return;
        }
        String reportId = UUID.randomUUID().toString();
        String templateId = template.getTemplateId();
        try {
            //设置模板状态为【报表生成中】
            qualityDao.updateTemplateStatus(TemplateStatus.GENERATING_REPORT.code, templateId);
            List<UserRule> rules = qualityDao.queryTemplateUserRuleById(templateId);

            executeRuleList(templateId, reportId, rules);
            Integer status = qualityDao.getTemplateStatus(templateId);
            //运行中途停止模板
            if (Objects.nonNull(status) && status.equals(TemplateStatus.SUSPENDING.code)) {
                qualityDao.updateFinishedPercent(templateId, 0F);
                return;
            }
            //更新报表结果
            updateReportResult(template, reportId, resultMap);
            qualityDao.updateFinishedPercent(template.getTemplateId(), (float) 1);
            String cron = qualityDao.getCronByTemplateId(templateId);
            if (StringUtils.isEmpty(cron)) {
                //设置模板状态为【已完成】
                qualityDao.updateTemplateStatus(TemplateStatus.FINISHED.code, templateId);
                qualityDao.deleteTemplate2QrtzByTemplateId(templateId);
            } else {
                //设置模板状态为【已启用】
                qualityDao.updateTemplateStatus(TemplateStatus.RUNNING.code, templateId);
            }

        } catch (Exception e) {
            LOG.error("任务执行失败", e);
            qualityDao.updateFinishedPercent(templateId, 0F);
            return;
        }
    }

    public void handleNullErrorTemplate(JobKey jobKey) {
        String jobName = jobKey.getName();
        String jobGroupName = DataQualityService.JOB_GROUP_NAME + jobName;
        String triggerName  = DataQualityService.TRIGGER_NAME + jobName;
        String triggerGroupName = DataQualityService.TRIGGER_GROUP_NAME + jobName;
        quartzManager.removeJob(jobName, jobGroupName, triggerName, triggerGroupName);
    }

    public void executeRuleList(String templateId, String reportId, List<UserRule> rules) throws Exception {
        String source = qualityDao.querySourceByTemplateId(templateId);
        String[] sourceInfo = source.split(SEPARATOR);
        String dbName = sourceInfo[0];
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
        LOG.info("query engine:" + engine);

        int totalStep = rules.size();
        for (int i = 0; i < rules.size(); i++) {
            //根据模板状态判断是否继续运行
            int retryCount = 0;
            UserRule rule = rules.get(i);
            do {
                try {
                    Integer status = qualityDao.getTemplateStatus(templateId);
                    //运行中途停止模板
                    if (Objects.nonNull(status) && status.equals(TemplateStatus.SUSPENDING.code)) {
                        qualityDao.updateFinishedPercent(templateId, 0F);
                        return;
                    }
                    List<Double> thresholds = qualityDao.queryTemplateThresholdByRuleId(rule.getRuleId());
                    rule.setRuleCheckThreshold(thresholds);
                    runJob(rule);
                    float ratio = (float) (i + 1) / totalStep;
                    LOG.info("raion=" + ratio);
                    qualityDao.updateFinishedPercent(templateId, ratio);
                    break;
                } catch (Exception e) {
                    try {
                        retryCount++;
                        LOG.info("retryCount=" + retryCount);
                        ReportError error = new ReportError();
                        String errorId = UUID.randomUUID().toString();
                        error.setErrorId(errorId);
                        error.setTemplateId(templateId);
                        error.setReportId(reportId);
                        error.setRuleId(rule.getRuleId());
                        error.setContent(e.toString());
                        long currentTime = System.currentTimeMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String generateTime = sdf.format(currentTime);
                        error.setGenerateTime(generateTime);
                        error.setRetryCount(retryCount);
                        qualityDao.insertReportError(error);
                        qualityDao.updateFinishedPercent(templateId, 0F);
                        Thread.sleep(RETRY * 5000);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage());
                    }
                    if(RETRY == retryCount) {
                        LOG.error(e.getMessage());
                        throw e;
                    }
                } finally {

                }

            } while (retryCount < RETRY);
        }
    }

    public void runJob(UserRule rule) throws Exception {
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
                    break;
                default:break;
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }

    //规则值计算
    public double ruleResultValue(UserRule rule, boolean record, boolean columnRule) throws Exception {
        double resultValue = 0.0;
        try {
            engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
            String templateId = rule.getTemplateId();
            String source = qualityDao.querySourceByTemplateId(templateId);
            String[] sourceInfo = source.split(SEPARATOR);
            String dbName = sourceInfo[0];
            String tableName = sourceInfo[1];

            String columnName = null;

            AdapterSource adapterSource;
            String pool;
            String user = MetaspaceConfig.getHiveAdmin();
            if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                adapterSource = AdapterUtils.getImpalaAdapterSource();
                pool = MetaspaceConfig.getImpalaResourcePool();
            } else {
                adapterSource = AdapterUtils.getHiveAdapterSource();
                pool = MetaspaceConfig.getHiveJobQueueName();
            }

            TaskType jobType = TaskType.getTaskByCode(rule.getSystemRuleId());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            StringJoiner joiner = new StringJoiner(".");
            String superType = null;
            if (columnRule) {
                columnName = rule.getRuleColumnName();

                switch (jobType) {
                    case UNIQUE_VALUE_NUM:
                    case UNIQUE_VALUE_NUM_CHANGE:
                    case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                    case UNIQUE_VALUE_NUM_RATIO:
                        superType = String.valueOf(TaskType.UNIQUE_VALUE_NUM.code);
                        sql = String.format(query, tableName, columnName, columnName, tableName, columnName);
                        break;
                    case DUP_VALUE_NUM:
                    case DUP_VALUE_NUM_CHANGE:
                    case DUP_VALUE_NUM_CHANGE_RATIO:
                    case DUP_VALUE_NUM_RATIO:
                        superType = String.valueOf(TaskType.DUP_VALUE_NUM.code);
                        sql = String.format(query, columnName, tableName, columnName, columnName, tableName, columnName);
                        break;
                    case EMPTY_VALUE_NUM:
                    case EMPTY_VALUE_NUM_CHANGE:
                    case EMPTY_VALUE_NUM_CHANGE_RATIO:
                    case EMPTY_VALUE_NUM_RATIO:
                        superType = String.valueOf(TaskType.EMPTY_VALUE_NUM.code);
                        sql = String.format(query, tableName, columnName);
                        break;
                    default:
                        sql = String.format(query, columnName, tableName);
                        break;
                }
            } else {
                superType = String.valueOf(TaskType.TABLE_ROW_NUM.code);
                sql = String.format(query, tableName);
            }

            String columnTypeKey = null;
            if(Objects.nonNull(columnName)) {
                columnTypeKey = joiner.add(dbName).add(tableName).add(columnName).add(superType).toString();
            } else {
                columnTypeKey = joiner.add(dbName).add(tableName).add(superType).toString();
            }
            if(columnType2Result.containsKey(columnTypeKey)) {
                return columnType2Result.get(columnTypeKey);
            }


            LOG.info("query Sql: " + sql);

            Connection connection = adapterSource.getConnection(user, dbName, pool);
            resultValue = adapterSource.getNewAdapterExecutor().queryResult(connection, sql, resultSet -> {
                try {
                    double value = 0;
                    if (Objects.nonNull(resultSet)) {
                        while (resultSet.next()) {
                            Object object = resultSet.getObject(1);
                            if (Objects.nonNull(object))
                                value = Double.valueOf(object.toString());
                        }
                    }
                    return value;
                } catch (Exception e) {
                    throw new AtlasBaseException(e);
                }
            });
            columnType2Result.put(columnTypeKey, resultValue);
            return resultValue;
        } catch (Exception e) {
            LOG.info(e.toString());
            throw e;
        } finally {
            if (record) {
                recordDataMap(rule, resultValue, resultValue);
            }
        }
    }

    //规则值变化
    public double ruleResultValueChange(UserRule rule, boolean record, boolean columnRule) throws Exception {
        double nowValue = 0;
        double valueChange = 0;
        try {
            nowValue = ruleResultValue(rule, false, columnRule);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            Double lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            valueChange = Math.abs(nowValue - lastValue);
            return valueChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                recordDataMap(rule, nowValue, valueChange);
            }
        }
    }

    //规则值变化率
    public Double ruleResultChangeRatio(UserRule rule, boolean record, boolean columnRule) throws Exception {
        double ratio = 0;
        double ruleValueChange = 0;
        Double lastValue = 0D;
        try {
            ruleValueChange = ruleResultValueChange(rule, false, columnRule);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            if (lastValue != 0) {
                ratio = ruleValueChange / lastValue;
            }
            return ratio;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                recordDataMap(rule, ruleValueChange + lastValue, ratio);
            }
        }
    }

    //表大小
    public float tableSize(UserRule rule, boolean record) throws Exception {
        float totalSize = 0;
        String templateId = rule.getTemplateId();
        String source = qualityDao.querySourceByTemplateId(templateId);
        String dbName = source.split("\\.")[0];
        String tableName = source.split("\\.")[1];
        try {


            String superType = String.valueOf(TaskType.TABLE_SIZE.code);
            StringJoiner joiner = new StringJoiner(".");
            String columnTypeKey = joiner.add(dbName).add(tableName).add(superType).toString();
            if(columnType2Result.containsKey(columnTypeKey)) {
                Double result = columnType2Result.get(columnTypeKey);
                if(Objects.nonNull(result))
                    return result.longValue();
            }


            //表数据量
            totalSize = AdapterUtils.getHiveAdapterSource().getNewAdapterExecutor().getTableSize(dbName, tableName, "metaspace");
            return totalSize;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                recordDataMap(rule, (double) totalSize, (double) totalSize);
            }
        }
    }

    //表大小变化
    public Double tableSizeChange(UserRule rule, boolean record) throws Exception {
        float tableSize = 0;
        double sizeChange = 0;
        try {
            tableSize = tableSize(rule, false);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            Double lastValue = qualityDao.getLastValue(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            sizeChange = Math.abs(tableSize - lastValue);
            return sizeChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                recordDataMap(rule, (double) tableSize, sizeChange);
            }
        }
    }

    //表大小变化率
    public double tableSizeChangeRatio(UserRule rule, boolean record) throws Exception {
        double tableSizeChange = 0;
        Double lastValue = 0D;
        double ratio = 0;
        try {
            tableSizeChange = tableSizeChange(rule, false);
            String templateId = rule.getTemplateId();
            String templateRuleId = rule.getRuleId();
            lastValue = qualityDao.getLastTableRowNum(templateId, templateRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            if (lastValue != 0) {
                ratio = tableSizeChange / lastValue;
            }
            return ratio;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                recordDataMap(rule, tableSizeChange + lastValue, ratio);
            }
        }
    }

    public void getProportion(UserRule rule) throws Exception {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
        double ratio = 0;
        String templateId = rule.getTemplateId();
        String source = qualityDao.querySourceByTemplateId(templateId);
        String[] sourceInfo = source.split(SEPARATOR);
        String dbName = sourceInfo[0];
        String tableName = sourceInfo[1];
        AdapterSource adapterSource = null;
        String pool;
        if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            adapterSource = AdapterUtils.getImpalaAdapterSource();
            pool = MetaspaceConfig.getImpalaResourcePool();
        } else {
            adapterSource = AdapterUtils.getHiveAdapterSource();
            pool = MetaspaceConfig.getHiveJobQueueName();
        }
        try {
            double nowNum = ruleResultValue(rule, false, true);
            String query = "select count(*) from %s";
            String sql = String.format(query, tableName);

            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), dbName, pool);
            Double totalNum = adapterSource.getNewAdapterExecutor().queryResult(connection, sql, resultSet -> {
                try {
                    double num = 0.0;
                    if (Objects.nonNull(resultSet)) {
                        while (resultSet.next()) {
                            Object object = resultSet.getObject(1);
                            if (Objects.nonNull(object)) {
                                num = Double.parseDouble(object.toString());
                            }
                        }
                    }
                    return num;
                } catch (Exception e) {
                    throw new AtlasBaseException(e);
                }
            });
            if (totalNum != 0) {
                ratio = nowNum / totalNum;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            recordDataMap(rule, ratio, ratio);
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
    public Report insertReport(Template template, String reportId) {
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
    public RuleStatus getReportRuleStatus(double resultValue, UserRule rule) throws Exception {
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
                        default:break;
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
                default:break;
            }
        } catch (Exception e) {
            LOG.info(e.getMessage(),e);
            throw e;
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

    public void updateReportResult(Template template, String reportId, Map<UserRule, List<Double>> resultMap) throws Exception {
        try {
            List<Report.ReportRule> list=new ArrayList<>();
            Report report = insertReport(template, reportId);
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
            throw e;
        }
    }
    @Transactional(rollbackFor=Exception.class)
    public void addReportByDao(Report report, List<Report.ReportRule> list) throws SQLException {
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
