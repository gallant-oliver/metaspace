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
 * @date 2019/7/25 17:28
 */
package io.zeta.metaspace.web.task.quartz;


import static io.zeta.metaspace.model.dataquality.RuleCheckType.FIX;
import static io.zeta.metaspace.model.dataquality.RuleCheckType.FLU;

import io.zeta.metaspace.model.dataquality.CheckExpression;
import io.zeta.metaspace.model.dataquality.RuleCheckType;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality2.AtomicTaskExecution;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskExecute;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskRuleExecute;
import io.zeta.metaspace.model.dataquality2.RuleExecuteStatus;
import io.zeta.metaspace.model.dataquality2.WarningMessageStatus;
import io.zeta.metaspace.model.dataquality2.WarningStatus;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.DateUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import io.zeta.metaspace.web.util.ImpalaJdbcUtils;
import io.zeta.metaspace.web.util.QualityEngine;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/25 17:28
 */

@Transactional(rollbackFor=Exception.class)
public class QuartzJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(QuartzJob.class);
    @Autowired
    QuartzManager quartzManager;
    @Autowired
    TaskManageDAO taskManageDAO;

    private final int RETRY = 3;

    Map<String, Float> columnType2Result = new HashMap<>();

    private static Configuration conf;
    private static String engine;

    static {
        try {
            conf = ApplicationProperties.get();
        }  catch (Exception e) {

        }
    }

    public String initExecuteInfo(String taskId) {
        try {
            String userId = taskManageDAO.getTaskUpdater(taskId);
            DataQualityTaskExecute taskExecute = new DataQualityTaskExecute();
            String id = UUID.randomUUID().toString();
            taskExecute.setId(id);
            taskExecute.setTaskId(taskId);
            taskExecute.setPercent(0F);
            taskExecute.setExecuteStatus(1);
            taskExecute.setExecutor(userId);
            taskExecute.setExecuteTime(new Timestamp(System.currentTimeMillis()));
            taskExecute.setOrangeWarningCount(0);
            taskExecute.setRedWarningCount(0);
            taskExecute.setRuleErrorCount(0);
            taskExecute.setWarningStatus(0);
            taskExecute.setErrorStatus(0);
            taskExecute.setNumber(String.valueOf(System.currentTimeMillis()));
            Integer counter = taskManageDAO.getMaxCounter(taskId);
            taskExecute.setCounter(Objects.isNull(counter)?1:++counter);
            taskManageDAO.initTaskExecuteInfo(taskExecute);
            taskManageDAO.updateTaskExecutionCount(taskId);
            taskManageDAO.updateTaskExecuteStatus(taskId, 1);
            return id;
        } catch (Exception e) {
            LOG.error(e.toString());
        }
        return null;
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            JobKey key = jobExecutionContext.getTrigger().getJobKey();
            String taskId = taskManageDAO.getTaskIdByQrtzName(key.getName());
            String taskExecuteId = initExecuteInfo(taskId);
            //获取原子任务列表
            List<AtomicTaskExecution> taskList = taskManageDAO.getObjectWithRuleRelation(taskId);
            if (Objects.isNull(taskList)) {
                quartzManager.handleNullErrorTask(key);
                LOG.warn("任务名为" + key.getName() + "所属任务已被删除,无法继续执行任务");
                return;
            }
            //补全数据
            completeTaskInformation(taskId, taskExecuteId, taskList);

            executeAtomicTaskList(taskId, taskExecuteId, taskList);
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public void completeTaskInformation(String taskId, String taskExecuteId, List<AtomicTaskExecution> taskList) throws AtlasBaseException {
        try {
            for (AtomicTaskExecution taskExecution : taskList) {
                taskExecution.setTaskId(taskId);
                taskExecution.setTaskExecuteId(taskExecuteId);
                String id = UUID.randomUUID().toString();
                taskExecution.setId(id);
                String objectId = taskExecution.getObjectId();
                if (0 == taskExecution.getScope()) {
                    Table tableInfo = taskManageDAO.getDbAndTableName(objectId);
                    taskExecution.setDbName(tableInfo.getDatabaseName());
                    taskExecution.setTableName(tableInfo.getTableName());
                    taskExecution.setObjectName(tableInfo.getTableName());
                } else if (1 == taskExecution.getScope()) {
                    Column column = taskManageDAO.getDbAndTableAndColumnName(objectId);
                    taskExecution.setDbName(column.getDatabaseName());
                    taskExecution.setTableName(column.getTableName());
                    taskExecution.setObjectName(column.getColumnName());
                    taskExecution.setObjectType(column.getType());
                } else {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                }
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }


    public void executeAtomicTaskList(String taskId, String taskExecuteId, List<AtomicTaskExecution> taskList) throws Exception {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
        LOG.info("query engine:" + engine);
        int totalStep = taskList.size();
        long startTime = System.currentTimeMillis();
        String errorMsg = null;
        for (int i = 0; i < totalStep; i++) {
            //根据模板状态判断是否继续运行
            int retryCount = 0;
            AtomicTaskExecution task = taskList.get(i);
            long currentTime = System.currentTimeMillis();
            Timestamp currentTimeStamp = new Timestamp(currentTime);
            taskManageDAO.initRuleExecuteInfo(task.getId(), taskExecuteId, taskId, task.getSubTaskId(), task.getObjectId(), task.getSubTaskRuleId(), currentTimeStamp, currentTimeStamp, 0, 0, task.getRuleId());
            do {
                try {
                    //运行中途停止模板
                    if (!taskManageDAO.isRuning(taskId)) {
                        taskManageDAO.updateTaskFinishedPercent(taskId, 0F);
                        taskManageDAO.updateTaskExecutionFinishedPercent(taskExecuteId, 0F);
                        return;
                    }
                    runJob(task);
                    float ratio = (float) (i + 1) / totalStep;
                    LOG.info("raion=" + ratio);
                    taskManageDAO.updateTaskFinishedPercent(taskId, ratio);
                    taskManageDAO.updateTaskExecutionFinishedPercent(taskExecuteId, 0F);
                    errorMsg = null;
                    break;
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    LOG.error(e.toString());
                    try {
                        retryCount++;
                        LOG.info("retryCount=" + retryCount);

                        Thread.sleep(RETRY * 5000);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage());
                    }
                    if(RETRY == retryCount) {
                        taskManageDAO.updateTaskExecuteErrorMsg(taskExecuteId, e.toString());
                        taskManageDAO.updateTaskExecuteRuleErrorNum(task.getTaskExecuteId());
                        taskManageDAO.updateTaskErrorCount(taskId);
                        taskManageDAO.updateTaskExecuteErrorStatus(task.getTaskExecuteId(), WarningStatus.WARNING.code);
                    }
                } finally {
                    recordExecutionInfo(task, errorMsg);
                }
            } while (retryCount < RETRY);
        }
        long endTime = System.currentTimeMillis();
        if(null != errorMsg) {
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 3);
            taskManageDAO.updateTaskStatus(taskId, 3);
        } else {
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 2);
            taskManageDAO.updateTaskStatus(taskId, 2);
        }
        taskManageDAO.updateDataTaskCostTime(taskExecuteId, endTime-startTime);
    }

    public void recordExecutionInfo(AtomicTaskExecution task, String errorMsg) {
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        String objectName = task.getObjectName();
        String source = dbName + "." + tableName;
        if(Objects.nonNull(objectName) && !objectName.equals(tableName)) {
            source += "." + objectName;
        }
        String checkMsg = taskManageDAO.getRuleCheckName(task.getSubTaskRuleId());
        String currentTime = DateUtils.getNow();

        String logInfoStatus = null;
        if(Objects.nonNull(errorMsg)) {
            logInfoStatus = "ERROR";
        } else {
            logInfoStatus = "INFO";
        }

        StringJoiner logJoiner = new StringJoiner(" ");
        logJoiner.add(currentTime);
        logJoiner.add(logInfoStatus);
        logJoiner.add(source);
        logJoiner.add(checkMsg);
        logJoiner.add(Objects.isNull(errorMsg)?"SUCCESS":errorMsg);

        taskManageDAO.updateTaskRuleExecutionErrorMsg(task.getId(), logJoiner.toString());
    }

    public void runJob(AtomicTaskExecution task) throws Exception {
        try {
             TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            String pool = taskManageDAO.getPool(task.getTaskId());
            switch (jobType) {
                case TABLE_ROW_NUM:
                    ruleResultValue(task, true, false,pool);
                    break;
                case TABLE_ROW_NUM_CHANGE:
                    ruleResultValueChange(task, true, false,pool);
                    break;
                case TABLE_ROW_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(task, true, false,pool);
                    break;
                case TABLE_SIZE:
                    tableSize(task, true,pool);
                    break;
                case TABLE_SIZE_CHANGE:
                    tableSizeChange(task, true,pool);
                    break;
                case TABLE_SIZE_CHANGE_RATIO:
                    tableSizeChangeRatio(task, true,pool);
                    break;

                case AVG_VALUE:
                case TOTAL_VALUE:
                case MIN_VALUE:
                case MAX_VALUE:
                case UNIQUE_VALUE_NUM:
                case EMPTY_VALUE_NUM:
                case DUP_VALUE_NUM:
                    ruleResultValue(task, true, true,pool);
                    break;

                case AVG_VALUE_CHANGE:
                case TOTAL_VALUE_CHANGE:
                case MIN_VALUE_CHANGE:
                case MAX_VALUE_CHANGE:
                case UNIQUE_VALUE_NUM_CHANGE:
                case EMPTY_VALUE_NUM_CHANGE:
                case DUP_VALUE_NUM_CHANGE:
                    ruleResultValueChange(task, true, true,pool);
                    break;

                case AVG_VALUE_CHANGE_RATIO:
                case TOTAL_VALUE_CHANGE_RATIO:
                case MIN_VALUE_CHANGE_RATIO:
                case MAX_VALUE_CHANGE_RATIO:
                case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                case EMPTY_VALUE_NUM_CHANGE_RATIO:
                case DUP_VALUE_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(task, true, true,pool);
                    break;

                case UNIQUE_VALUE_NUM_RATIO:
                case EMPTY_VALUE_NUM_RATIO:
                case DUP_VALUE_NUM_RATIO:
                    getProportion(task,pool);
                    break;
                default:break;
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }


    //规则值计算
    public Float ruleResultValue(AtomicTaskExecution task, boolean record, boolean columnRule,String pool) throws Exception {
        Float resultValue = null;
        Connection conn = null;
        try {
            engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
            String dbName = task.getDbName();
            String tableName = task.getTableName();
            String columnName = null;
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                conn = ImpalaJdbcUtils.getSystemConnection(dbName,pool);
            } else {
                conn = HiveJdbcUtils.getSystemConnection(dbName,pool);
            }

            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            String superType  = String.valueOf(jobType.code);
            if (columnRule) {
                columnName = task.getObjectName();
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

            String columnTypeKey = null;
            StringJoiner joiner = new StringJoiner(".");
            if(Objects.nonNull(columnName)) {
                columnTypeKey = joiner.add(dbName).add(tableName).add(columnName).add(superType).toString();
            } else {
                columnTypeKey = joiner.add(dbName).add(tableName).add(superType).toString();
            }
            if(columnType2Result.containsKey(columnTypeKey)) {
                return columnType2Result.get(columnTypeKey);
            }

            LOG.info("query Sql: " + sql);
            ResultSet resultSet = null;
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                resultSet = ImpalaJdbcUtils.selectBySQLWithSystemCon(conn, sql);
            } else {
                resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(conn, sql);
            }
            if(Objects.nonNull(resultSet)) {
                while (resultSet.next()) {
                    Object object = resultSet.getObject(1);
                    if(Objects.nonNull(object)) {
                        resultValue = Float.valueOf(object.toString());
                    }
                }
            }
            if(Objects.nonNull(resultValue)) {
                columnType2Result.put(columnTypeKey, resultValue);
            }
            return resultValue;
        } catch (Exception e) {
            LOG.info(e.toString());
            throw e;
        } finally {
            if (record) {
                checkResult(task,resultValue,resultValue);
            }
            if(Objects.nonNull(conn)) {
                conn.close();
            }
        }
    }

    //规则值变化
    public Float ruleResultValueChange(AtomicTaskExecution task, boolean record, boolean columnRule,String pool) throws Exception {
        Float nowValue = null;
        Float valueChange = null;
        try {
            nowValue = ruleResultValue(task, false, columnRule,pool);
            String subTaskRuleId = task.getSubTaskRuleId();
            Float lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            valueChange = Math.abs(nowValue - lastValue);
            return valueChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task,valueChange,nowValue);
            }
        }
    }

    //规则值变化率
    public Float ruleResultChangeRatio(AtomicTaskExecution task, boolean record, boolean columnRule,String pool) throws Exception {
        Float ratio = null;
        Float ruleValueChange = 0F;
        Float lastValue = 0F;
        try {
            ruleValueChange = ruleResultValueChange(task, false, columnRule,pool);
            ruleValueChange= Objects.isNull(ruleValueChange)?0:ruleValueChange;
            String subTaskRuleId = task.getSubTaskRuleId();
            lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue= Objects.isNull(lastValue)?0:lastValue;
            if (lastValue != 0) {
                ratio = ruleValueChange / lastValue;
            } else {
                ratio = 0F;
            }
            return ratio;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task,ratio,ruleValueChange + lastValue);
            }
        }
    }

    //表大小
    public Float tableSize(AtomicTaskExecution task, boolean record,String pool) throws Exception {
        Float totalSize = null;
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        try {
            //表数据量
            engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                totalSize = ImpalaJdbcUtils.getTableSize(dbName, tableName,pool);
            } else {
                totalSize = HiveJdbcUtils.getTableSize(dbName, tableName,pool);
            }
            return totalSize;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task,totalSize,totalSize);
            }
        }
    }

    //表大小变化
    public Float tableSizeChange(AtomicTaskExecution task, boolean record,String pool) throws Exception {
        Float tableSize = null;
        Float sizeChange = null;
        try {
            tableSize = tableSize(task, false,pool);
            String subTaskRuleId = task.getSubTaskRuleId();
            Float lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            sizeChange = Math.abs(tableSize - lastValue);
            return sizeChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task,sizeChange,tableSize);
            }
        }
    }

    //表大小变化率
    public Float tableSizeChangeRatio(AtomicTaskExecution task, boolean record,String pool) throws Exception {
        Float tableSizeChange = 0F;
        Float lastValue = 0F;
        Float ratio = null;
        try {
            tableSizeChange = tableSizeChange(task, false,pool);
            tableSizeChange = (Objects.isNull(lastValue))? 0:lastValue;
            String subTaskRuleId = task.getSubTaskRuleId();
            lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            if (lastValue != 0) {
                ratio = tableSizeChange / lastValue;
            }
            return ratio;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task,ratio,tableSizeChange + lastValue);
            }
        }
    }

    public void getProportion(AtomicTaskExecution task,String pool) throws Exception {
        Float ratio = null;
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        Connection conn = null;
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
        if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            conn = ImpalaJdbcUtils.getSystemConnection(dbName,pool);
        } else {
            conn = HiveJdbcUtils.getSystemConnection(dbName,pool);
        }
        try {
            Float nowNum = ruleResultValue(task, false, true,pool);
            Float totalNum = 0F;
            String query = "select count(*) from %s";
            String sql = String.format(query, tableName);

            ResultSet resultSet = null;
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                resultSet = ImpalaJdbcUtils.selectBySQLWithSystemCon(conn, sql);
            } else {
                resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(conn, sql);
            }

            if(Objects.nonNull(resultSet)) {
                while (resultSet.next()) {
                    Object object = resultSet.getObject(1);
                    if(Objects.nonNull(object)) {
                        totalNum = Float.valueOf(object.toString());
                    }
                }
            }
            if (totalNum != 0) {
                ratio = nowNum / totalNum;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            checkResult(task, ratio, ratio);
            if(Objects.nonNull(conn)) {
                conn.close();
            }
        }
    }


    /**
     * 通过规则计算规则所处状态
     *
     * @param resultValue
     * @return
     */
    public RuleExecuteStatus checkResult(AtomicTaskExecution task, Float resultValue, Float referenceValue) throws Exception {
        RuleExecuteStatus checkStatus = null;
        try {
            DataQualitySubTaskRule subTaskRule = taskManageDAO.getSubTaskRuleInfo(task.getSubTaskRuleId());
            Integer ruleCheckTypeCode = taskManageDAO.getRuleCheckType(task.getSubTaskRuleId());
            Integer checkExpressionCode = taskManageDAO.getRuleCheckExpression(task.getSubTaskRuleId());
            RuleCheckType ruleCheckType = RuleCheckType.getRuleCheckTypeByCode(ruleCheckTypeCode);

            CheckExpression checkExpression = null;
            checkExpression=Objects.nonNull(checkExpressionCode)?CheckExpression.getExpressionByCode(checkExpressionCode):checkExpression;
            Float checkThresholdMinValue = null;
            Float checkThresholdMaxValue = null;


            RuleExecuteStatus orangeWarningcheckStatus = null;
            RuleExecuteStatus redWarningcheckStatus = null;
            if(Objects.nonNull(resultValue)) {
                if (FIX == ruleCheckType) {
                    checkThresholdMaxValue = subTaskRule.getCheckThresholdMaxValue();
                } else if (FLU == ruleCheckType) {
                    checkThresholdMinValue = subTaskRule.getCheckThresholdMinValue();
                    checkThresholdMaxValue = subTaskRule.getCheckThresholdMaxValue();
                }
                checkStatus = checkResultStatus(ruleCheckType, checkExpression, resultValue, checkThresholdMinValue, checkThresholdMaxValue);

                if (Objects.nonNull(subTaskRule.getOrangeCheckExpression())) {
                    RuleCheckType orangeCheckRuleCheckType = RuleCheckType.getRuleCheckTypeByCode(subTaskRule.getOrangeCheckType());
                    CheckExpression orangeCheckRuleCheckExpression = null;
                    if(orangeCheckRuleCheckType == RuleCheckType.FIX) {
                        orangeCheckRuleCheckExpression = CheckExpression.getExpressionByCode(subTaskRule.getOrangeCheckExpression());
                    }
                    orangeWarningcheckStatus = checkResultStatus(orangeCheckRuleCheckType, orangeCheckRuleCheckExpression, resultValue, subTaskRule.getOrangeThresholdMinValue(), subTaskRule.getOrangeThresholdMaxValue());
                    if(RuleExecuteStatus.WARNING == orangeWarningcheckStatus) {
                        orangeWarningcheckStatus = RuleExecuteStatus.NORMAL;
                    } else if(RuleExecuteStatus.NORMAL == orangeWarningcheckStatus){
                        orangeWarningcheckStatus = RuleExecuteStatus.WARNING;
                    }
                }

                if (Objects.nonNull(subTaskRule.getRedCheckExpression())) {
                    RuleCheckType redCheckRuleCheckType = RuleCheckType.getRuleCheckTypeByCode(subTaskRule.getRedCheckType());
                    CheckExpression redCheckRuleCheckExpression = null;
                    if(redCheckRuleCheckType == RuleCheckType.FIX) {
                        redCheckRuleCheckExpression = CheckExpression.getExpressionByCode(subTaskRule.getRedCheckExpression());
                    }
                    redWarningcheckStatus = checkResultStatus(redCheckRuleCheckType, redCheckRuleCheckExpression, resultValue, subTaskRule.getRedThresholdMinValue(), subTaskRule.getRedThresholdMaxValue());
                    if(RuleExecuteStatus.WARNING == redWarningcheckStatus) {
                        redWarningcheckStatus = RuleExecuteStatus.NORMAL;
                    } else if(RuleExecuteStatus.NORMAL == redWarningcheckStatus) {
                        redWarningcheckStatus = RuleExecuteStatus.WARNING;
                    }
                }
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            DataQualityTaskRuleExecute taskRuleExecute = new DataQualityTaskRuleExecute(task.getId(),task.getTaskExecuteId(),task.getTaskId(),task.getSubTaskId(), task.getObjectId(), task.getSubTaskRuleId(),
                                                                                        resultValue, referenceValue, Objects.isNull(checkStatus)?2:checkStatus.getCode(), Objects.isNull(orangeWarningcheckStatus)?null:orangeWarningcheckStatus.getCode(),
                                                                                        Objects.isNull(redWarningcheckStatus)?null:redWarningcheckStatus.getCode(), WarningMessageStatus.WAITING.getCode(),currentTime, currentTime);

            taskManageDAO.updateRuleExecutionWarningInfo(taskRuleExecute);
            //橙色告警数量
            if(Objects.nonNull(orangeWarningcheckStatus) && orangeWarningcheckStatus == RuleExecuteStatus.WARNING) {
                taskManageDAO.updateTaskExecuteOrangeWarningNum(task.getTaskExecuteId());
                taskManageDAO.updateTaskOrangeWarningCount(task.getTaskId());
                taskManageDAO.updateTaskExecuteWarningStatus(task.getId(), WarningStatus.WARNING.code);
            }
            //红色告警数量
            if(Objects.nonNull(redWarningcheckStatus) && redWarningcheckStatus == RuleExecuteStatus.WARNING) {
                taskManageDAO.updateTaskExecuteRedWarningNum(task.getTaskExecuteId());
                taskManageDAO.updateTaskRedWarningCount(task.getTaskId());
                taskManageDAO.updateTaskExecuteWarningStatus(task.getId(), WarningStatus.WARNING.code);
            }
            //计算异常数量

        } catch (Exception e) {
            LOG.info(e.getMessage(),e);
            throw e;
        }
        //if (checkStatus == null) throw new RuntimeException();
        return checkStatus;
    }

    public RuleExecuteStatus checkResultStatus(RuleCheckType ruleCheckType, CheckExpression checkExpression, Float resultValue, Float checkThresholdMinValue, Float checkThresholdMaxValue) {
        RuleExecuteStatus ruleStatus = null;
        try {
            if (FIX == ruleCheckType) {
                switch (checkExpression) {
                    case EQU: {
                        if (resultValue.equals(checkThresholdMaxValue)) {
                            ruleStatus = RuleExecuteStatus.NORMAL;
                        } else {
                            ruleStatus = RuleExecuteStatus.WARNING;
                        }
                        break;
                    }
                    case NEQ: {
                        if (!resultValue.equals(checkThresholdMaxValue)) {
                            ruleStatus = RuleExecuteStatus.NORMAL;
                        } else {
                            ruleStatus = RuleExecuteStatus.WARNING;
                        }
                        break;
                    }
                    case GTR: {
                        if (resultValue > checkThresholdMaxValue) {
                            ruleStatus = RuleExecuteStatus.NORMAL;
                        } else {
                            ruleStatus = RuleExecuteStatus.WARNING;
                        }
                        break;
                    }
                    case GER: {
                        if (resultValue >= checkThresholdMaxValue) {
                            ruleStatus = RuleExecuteStatus.NORMAL;
                        } else {
                            ruleStatus = RuleExecuteStatus.WARNING;
                        }
                        break;
                    }
                    case LSS: {
                        if (resultValue < checkThresholdMaxValue) {
                            ruleStatus = RuleExecuteStatus.NORMAL;
                        } else {
                            ruleStatus = RuleExecuteStatus.WARNING;
                        }
                        break;
                    }
                    case LEQ: {
                        if (resultValue <= checkThresholdMaxValue) {
                            ruleStatus = RuleExecuteStatus.NORMAL;
                        } else {
                            ruleStatus = RuleExecuteStatus.WARNING;
                        }
                        break;
                    }
                    default:break;
                }
            } else if (FLU == ruleCheckType) {
                if(resultValue>= checkThresholdMinValue && resultValue<=checkThresholdMaxValue) {
                    ruleStatus = RuleExecuteStatus.NORMAL;
                } else {
                    ruleStatus = RuleExecuteStatus.WARNING;
                }
            }
        } catch (Exception e) {
            LOG.info(e.toString());
        }
        return ruleStatus;
    }
}
