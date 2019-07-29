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
import io.zeta.metaspace.model.dataquality.RuleStatus;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality2.AtomicTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskExecute;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import io.zeta.metaspace.web.util.ImpalaJdbcUtils;
import io.zeta.metaspace.web.util.QualityEngine;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
public class QuartzJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(QuartzJob.class);
    @Autowired
    QuartzManager quartzManager;
    @Autowired
    TaskManageDAO taskManageDAO;

    private final int RETRY = 3;

    Map<String, Float> columnType2Result = new HashMap<>();

    private static String engine;

    static {
        try {
            org.apache.commons.configuration.Configuration conf = ApplicationProperties.get();
            engine = conf.getString("metaspace.quality.engine");
        }  catch (Exception e) {

        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            JobKey key = jobExecutionContext.getTrigger().getJobKey();
            //获取原子任务列表
            List<AtomicTask> taskList = taskManageDAO.getObjectWithRuleRelation(key.getName());
            if (Objects.isNull(taskList)) {
                quartzManager.handleNullErrorTask(key);
                LOG.warn("任务名为" + key.getName() + "所属任务已被删除,无法继续执行任务");
                return;
            }
            //补全数据
            completeTaskInformation(taskList);

            executeAtomicTaskList(null, taskList);
        } catch (Exception e) {
            LOG.error(e.toString());
        }
    }

    public void completeTaskInformation(List<AtomicTask> taskList) throws AtlasBaseException {
        try {
            for (AtomicTask task : taskList) {
                String id = UUID.randomUUID().toString();
                task.setId(id);
                String objectId = task.getObjectId();
                if (1 == task.getScope()) {
                    Table tableInfo = taskManageDAO.getDbAndTableName(objectId);
                    task.setDbName(tableInfo.getDatabaseName());
                    task.setTableName(tableInfo.getTableName());
                    task.setObjectName(tableInfo.getTableName());
                } else if (2 == task.getScope()) {
                    Column column = taskManageDAO.getDbAndTableAndColumnName(objectId);
                    task.setDbName(column.getDatabaseName());
                    task.setTableName(column.getTableName());
                    task.setObjectName(column.getColumnName());
                    task.setObjectType(column.getType());
                } else {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                }
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }


    public void executeAtomicTaskList(String taskId, List<AtomicTask> taskList) throws Exception {
        String executeId = initExecuteInfo(taskId);
        LOG.info("query engine:" + engine);
        int totalStep = taskList.size();
        for (int i = 0; i < totalStep; i++) {
            //根据模板状态判断是否继续运行
            int retryCount = 0;
            AtomicTask task = taskList.get(i);
            taskManageDAO.initRuleExecuteInfo(task.getId(), taskId, executeId, task.getSubTaskId(), task.getObjectId(), task.getSubTaskRuleId());
            do {
                try {
                    //运行中途停止模板
                    if (!taskManageDAO.isRuning(taskId)) {
                        taskManageDAO.updateTaskFinishedPercent(executeId, 0F);
                        return;
                    }
                    runJob(task);
                    float ratio = (float) (i + 1) / totalStep;
                    LOG.info("raion=" + ratio);
                    taskManageDAO.updateTaskFinishedPercent(executeId, ratio);
                    break;
                } catch (Exception e) {
                    LOG.error(e.toString());
                    try {
                        retryCount++;
                        LOG.info("retryCount=" + retryCount);
                        Thread.sleep(RETRY * 5000);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage());
                    }
                    if(RETRY == retryCount) {
                        LOG.error(e.getMessage());
                        throw e;
                    }
                } finally {
                    taskManageDAO.updateTaskExecuteWarningInfo();
                }
            } while (retryCount < RETRY);
        }
    }

    public String initExecuteInfo(String taskId) {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            DataQualityTaskExecute taskExecute = new DataQualityTaskExecute();
            String id = UUID.randomUUID().toString();
            taskExecute.setId(id);
            taskExecute.setTaskId(taskId);
            taskExecute.setPercent(0F);
            taskExecute.setExecuteStatus(1);
            taskExecute.setExecutor(userId);
            taskExecute.setExecuteTime(new Timestamp(System.currentTimeMillis()));
            taskManageDAO.initTaskExecuteInfo(taskExecute);
            return id;
        } catch (Exception e) {
            LOG.error(e.toString());
        }
        return null;
    }
    public void runJob(AtomicTask task) throws Exception {
        try {
            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            switch (jobType) {
                case TABLE_ROW_NUM:
                    ruleResultValue(task, true, false);
                    break;
                case TABLE_ROW_NUM_CHANGE:
                    ruleResultValueChange(task, true, false);
                    break;
                case TABLE_ROW_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(task, true, false);
                    break;
                case TABLE_SIZE:
                    tableSize(task, true);
                    break;
                case TABLE_SIZE_CHANGE:
                    tableSizeChange(task, true);
                    break;
                case TABLE_SIZE_CHANGE_RATIO:
                    tableSizeChangeRatio(task, true);
                    break;

                case AVG_VALUE:
                case TOTAL_VALUE:
                case MIN_VALUE:
                case MAX_VALUE:
                case UNIQUE_VALUE_NUM:
                case EMPTY_VALUE_NUM:
                case DUP_VALUE_NUM:
                    ruleResultValue(task, true, true);
                    break;

                case AVG_VALUE_CHANGE:
                case TOTAL_VALUE_CHANGE:
                case MIN_VALUE_CHANGE:
                case MAX_VALUE_CHANGE:
                case UNIQUE_VALUE_NUM_CHANGE:
                case EMPTY_VALUE_NUM_CHANGE:
                case DUP_VALUE_NUM_CHANGE:
                    ruleResultValueChange(task, true, true);
                    break;

                case AVG_VALUE_CHANGE_RATIO:
                case TOTAL_VALUE_CHANGE_RATIO:
                case MIN_VALUE_CHANGE_RATIO:
                case MAX_VALUE_CHANGE_RATIO:
                case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                case EMPTY_VALUE_NUM_CHANGE_RATIO:
                case DUP_VALUE_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(task, true, true);
                    break;

                case UNIQUE_VALUE_NUM_RATIO:
                case EMPTY_VALUE_NUM_RATIO:
                case DUP_VALUE_NUM_RATIO:
                    getProportion(task);
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }


    //规则值计算
    public Float ruleResultValue(AtomicTask task, boolean record, boolean columnRule) throws Exception {
        Float resultValue = 0F;
        Connection conn = null;
        try {
            String dbName = task.getDbName();
            String tableName = task.getTableName();
            String columnName = null;
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                conn = ImpalaJdbcUtils.getSystemConnection(dbName);
            } else {
                conn = HiveJdbcUtils.getSystemConnection(dbName);
            }

            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            String superType = null;
            if (columnRule) {
                columnName = task.getObjectName();
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
            columnType2Result.put(columnTypeKey, resultValue);
            return resultValue;
        } catch (Exception e) {
            LOG.info(e.toString());
            throw e;
        } finally {
            if (record) {
                taskManageDAO.completeCalculationResult(task.getId(), resultValue, resultValue);
            }
            if(Objects.nonNull(conn)) {
                conn.close();
            }
        }
    }

    //规则值变化
    public Float ruleResultValueChange(AtomicTask task, boolean record, boolean columnRule) throws Exception {
        Float nowValue = 0F;
        Float valueChange = 0F;
        try {
            nowValue = ruleResultValue(task, false, columnRule);
            String subTaskRuleId = task.getSubTaskRuleId();
            Float lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            valueChange = Math.abs(nowValue - lastValue);
            return valueChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                taskManageDAO.completeCalculationResult(task.getId(), valueChange, nowValue);
            }
        }
    }

    //规则值变化率
    public Float ruleResultChangeRatio(AtomicTask task, boolean record, boolean columnRule) throws Exception {
        Float ratio = 0F;
        Float ruleValueChange = 0F;
        Float lastValue = 0F;
        try {
            ruleValueChange = ruleResultValueChange(task, false, columnRule);
            String subTaskRuleId = task.getSubTaskRuleId();
            lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            if (lastValue != 0) {
                ratio = ruleValueChange / lastValue;
            }
            return ratio;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                taskManageDAO.completeCalculationResult(task.getId(), ratio, ruleValueChange + lastValue);
            }
        }
    }

    //表大小
    public Float tableSize(AtomicTask task, boolean record) throws Exception {
        Float totalSize = 0F;
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        try {
            //表数据量
            totalSize = Float.valueOf(HiveJdbcUtils.getTableSize(dbName, tableName));
            return totalSize;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                taskManageDAO.completeCalculationResult(task.getId(), totalSize, totalSize);
            }
        }
    }

    //表大小变化
    public Float tableSizeChange(AtomicTask task, boolean record) throws Exception {
        Float tableSize = 0F;
        Float sizeChange = 0F;
        try {
            tableSize = tableSize(task, false);
            String subTaskRuleId = task.getSubTaskRuleId();
            Float lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            sizeChange = Math.abs(tableSize - lastValue);
            return sizeChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                taskManageDAO.completeCalculationResult(task.getId(), sizeChange, tableSize);
            }
        }
    }

    //表大小变化率
    public Float tableSizeChangeRatio(AtomicTask task, boolean record) throws Exception {
        Float tableSizeChange = 0F;
        Float lastValue = 0F;
        Float ratio = 0F;
        try {
            tableSizeChange = tableSizeChange(task, false);
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
                taskManageDAO.completeCalculationResult(task.getId(), ratio, tableSizeChange + lastValue);
            }
        }
    }

    public void getProportion(AtomicTask task) throws Exception {
        Float ratio = 0F;
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        Connection conn = null;
        if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            conn = ImpalaJdbcUtils.getSystemConnection(dbName);
        } else {
            conn = HiveJdbcUtils.getSystemConnection(dbName);
        }
        try {
            Float nowNum = ruleResultValue(task, false, true);
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
            taskManageDAO.completeCalculationResult(task.getId(), ratio, ratio);
            if(Objects.nonNull(conn))
                conn.close();
        }
    }


    /**
     * 通过规则计算规则所处状态
     *
     * @param resultValue
     * @return
     */
    public RuleStatus checkResultStatus(float resultValue, AtomicTask task) throws Exception {
        RuleStatus ruleStatus = null;
        try {
            DataQualitySubTaskRule subTaskRule = taskManageDAO.getSubTaskRuleInfo(task.getSubTaskRuleId());
            int ruleCheckType = taskManageDAO.getRuleCheckType(task.getSubTaskRuleId());
            RuleCheckType ruleCheckTypeByCode = RuleCheckType.getRuleCheckTypeByCode(ruleCheckType);

        } catch (Exception e) {
            LOG.info(e.getMessage(),e);
            throw e;
        }
        if (ruleStatus == null) throw new RuntimeException();
        return ruleStatus;
    }

    public RuleStatus getCalculateStatus(RuleCheckType ruleCheckTypeByCode, CheckExpression expressionByCode, float resultValue, String checkThreshold) {
        RuleStatus ruleStatus = null;
        try {
            if (FIX == ruleCheckTypeByCode) {
                float checkThresholdValue = Float.parseFloat(checkThreshold);
                switch (expressionByCode) {
                    case EQU: {
                        if (checkThresholdValue == 0) {
                            ruleStatus = RuleStatus.NORMAL;
                        } else {
                            ruleStatus = RuleStatus.RED;
                        }
                        break;
                    }
                    case NEQ: {
                        if (resultValue != checkThresholdValue) {
                            ruleStatus = RuleStatus.NORMAL;
                        } else {
                            ruleStatus = RuleStatus.RED;
                        }
                        break;
                    }
                    case GTR: {
                        if (resultValue > checkThresholdValue) {
                            ruleStatus = RuleStatus.NORMAL;
                        } else {
                            ruleStatus = RuleStatus.RED;
                        }
                        break;
                    }
                    case GER: {
                        if (resultValue >= checkThresholdValue) {
                            ruleStatus = RuleStatus.NORMAL;
                        } else {
                            ruleStatus = RuleStatus.RED;
                        }
                        break;
                    }
                    case LSS: {
                        if (resultValue < checkThresholdValue) {
                            ruleStatus = RuleStatus.NORMAL;
                        } else {
                            ruleStatus = RuleStatus.RED;
                        }
                        break;
                    }
                    case LEQ: {
                        if (resultValue <= checkThresholdValue) {
                            ruleStatus = RuleStatus.NORMAL;
                        } else {
                            ruleStatus = RuleStatus.RED;
                        }
                        break;
                    }
                }
            } else if (FLU == ruleCheckTypeByCode) {


            }
        } catch (Exception e) {

        }
        return ruleStatus;
    }
}
