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


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.healthmarketscience.sqlbuilder.*;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.dataquality.CheckExpression;
import io.zeta.metaspace.model.dataquality.RuleCheckType;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.measure.*;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.GsonUtils;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.task.util.LivyTaskSubmitHelper;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.DateUtils;
import io.zeta.metaspace.web.util.HdfsUtils;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

import static io.zeta.metaspace.model.dataquality.RuleCheckType.FIX;
import static io.zeta.metaspace.model.dataquality.RuleCheckType.FLU;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/25 17:28
 */

@Transactional(rollbackFor = Exception.class)
public class QuartzJob implements Job {

    private static final Logger LOG = LoggerFactory.getLogger(QuartzJob.class);
    @Autowired
    QuartzManager quartzManager;
    @Autowired
    TaskManageDAO taskManageDAO;
    @Autowired
    DataSourceDAO dataSourceDAO;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    LivyTaskSubmitHelper livyTaskSubmitHelper;

    private final int RETRY = 3;

    Map<String, Float> columnType2Result = new HashMap<>();

    private static Configuration conf;
    private static String engine;

    static {
        try {
            conf = ApplicationProperties.get();
        } catch (Exception e) {

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
            taskExecute.setCounter(Objects.isNull(counter) ? 1 : ++counter);
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
            EditionTaskInfo taskInfo = taskManageDAO.getTaskInfo(taskId);
            String tenantId = taskInfo.getTenantId();
            String taskExecuteId = initExecuteInfo(taskId);
            //获取原子任务列表，包含子任务关联对象，子任务使用规则，子任务使用规则模板
            List<AtomicTaskExecution> taskList = taskManageDAO.getObjectWithRuleRelation(taskId, tenantId);
            if (Objects.isNull(taskList)) {
                quartzManager.handleNullErrorTask(key);
                LOG.warn("任务名为" + key.getName() + "所属任务已被删除,无法继续执行任务");
                return;
            }
            //补全数据
            completeTaskInformation(taskId, taskExecuteId, taskList);

            executeAtomicTaskList(taskId, taskExecuteId, taskList, tenantId);
        } catch (Exception e) {
            LOG.error(e.toString(), e);
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
                    if (2 == taskExecution.getScope()) {
                        TaskType taskType = TaskType.getTaskByCode(taskExecution.getTaskType());
                        if (TaskType.CONSISTENCY.equals(taskType)) {
                            taskExecution.setConsistencyParams(GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                            }.getType()));
                            return;
                        }
                    }
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                }
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }


    public void executeAtomicTaskList(String taskId, String taskExecuteId, List<AtomicTaskExecution> taskList, String tenantId) throws Exception {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
        LOG.info("query engine:" + engine);
        int totalStep = taskList.size();
        long startTime = System.currentTimeMillis();
        String errorMsg = null;
        for (int i = 0; i < totalStep; i++) {
            //根据模板状态判断是否继续运行
            int retryCount = 0;
            AtomicTaskExecution task = taskList.get(i);
            long currentTime = System.currentTimeMillis();
            task.setTimeStamp(currentTime);
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
                    if (RETRY == retryCount) {
                        taskManageDAO.updateTaskExecuteErrorMsg(taskExecuteId, e.toString());
                        taskManageDAO.updateTaskExecuteRuleErrorNum(task.getTaskExecuteId());
                        taskManageDAO.updateTaskErrorCount(taskId);
                        taskManageDAO.updateTaskExecuteErrorStatus(task.getTaskExecuteId(), WarningStatus.WARNING.code);
                    }
                } finally {
                    recordExecutionInfo(task, errorMsg, tenantId);
                }
            } while (retryCount < RETRY);
        }
        long endTime = System.currentTimeMillis();
        if (null != errorMsg) {
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 3);
            taskManageDAO.updateTaskStatus(taskId, 3);
        } else {
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 2);
            taskManageDAO.updateTaskStatus(taskId, 2);
        }
        taskManageDAO.updateDataTaskCostTime(taskExecuteId, endTime - startTime);
    }

    public void recordExecutionInfo(AtomicTaskExecution task, String errorMsg, String tenantId) {
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        String objectName = task.getObjectName();
        String source = dbName + "." + tableName;
        if (Objects.nonNull(objectName) && !objectName.equals(tableName)) {
            source += "." + objectName;
        }
        String checkMsg = taskManageDAO.getRuleCheckName(task.getSubTaskRuleId(), tenantId);
        String currentTime = DateUtils.getNow();

        String logInfoStatus = null;
        if (Objects.nonNull(errorMsg)) {
            logInfoStatus = "ERROR";
        } else {
            logInfoStatus = "INFO";
        }

        StringJoiner logJoiner = new StringJoiner(" ");
        logJoiner.add(currentTime);
        logJoiner.add(logInfoStatus);
        logJoiner.add(source);
        logJoiner.add(checkMsg);
        logJoiner.add(Objects.isNull(errorMsg) ? "SUCCESS" : errorMsg);

        taskManageDAO.updateTaskRuleExecutionErrorMsg(task.getId(), logJoiner.toString());
    }

    public void runJob(AtomicTaskExecution task) throws Exception {
        try {
            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            String pool = taskManageDAO.getPool(task.getTaskId());
            switch (jobType) {
                case TABLE_ROW_NUM:
                    ruleResultValue(task, true, false, pool);
                    break;
                case TABLE_ROW_NUM_CHANGE:
                    ruleResultValueChange(task, true, false, pool);
                    break;
                case TABLE_ROW_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(task, true, false, pool);
                    break;
                case TABLE_SIZE:
                    tableSize(task, true, pool);
                    break;
                case TABLE_SIZE_CHANGE:
                    tableSizeChange(task, true, pool);
                    break;
                case TABLE_SIZE_CHANGE_RATIO:
                    tableSizeChangeRatio(task, true, pool);
                    break;

                case AVG_VALUE:
                case TOTAL_VALUE:
                case MIN_VALUE:
                case MAX_VALUE:
                case UNIQUE_VALUE_NUM:
                case EMPTY_VALUE_NUM:
                case DUP_VALUE_NUM:
                    ruleResultValue(task, true, true, pool);
                    break;

                case AVG_VALUE_CHANGE:
                case TOTAL_VALUE_CHANGE:
                case MIN_VALUE_CHANGE:
                case MAX_VALUE_CHANGE:
                case UNIQUE_VALUE_NUM_CHANGE:
                case EMPTY_VALUE_NUM_CHANGE:
                case DUP_VALUE_NUM_CHANGE:
                    ruleResultValueChange(task, true, true, pool);
                    break;

                case AVG_VALUE_CHANGE_RATIO:
                case TOTAL_VALUE_CHANGE_RATIO:
                case MIN_VALUE_CHANGE_RATIO:
                case MAX_VALUE_CHANGE_RATIO:
                case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                case EMPTY_VALUE_NUM_CHANGE_RATIO:
                case DUP_VALUE_NUM_CHANGE_RATIO:
                    ruleResultChangeRatio(task, true, true, pool);
                    break;

                case UNIQUE_VALUE_NUM_RATIO:
                case EMPTY_VALUE_NUM_RATIO:
                case DUP_VALUE_NUM_RATIO:
                    getProportion(task, pool);
                    break;
                case CONSISTENCY:
                    consistencyRuleCheck(task, pool);
                default:
                    break;
            }
        } catch (Exception e) {
            LOG.info(e.getMessage());
            throw e;
        }
    }


    /**
     * 一致性校验
     * <p>
     * 1、构建参数，将任务转换为 Measure
     * 2、livy提交任务
     * 3、监控 spark 任务状态
     * 4、从 Hdfs 上读取结果返回
     *
     * @param task
     * @param pool
     * @return
     */
    public long consistencyRuleCheck(AtomicTaskExecution task, String pool) throws Exception {
        Long errorCount = 0L;
        try {
            Measure measure = buildMeasure(task, task.getTimeStamp());
            MeasureLivyResult result = null;
            try {

                result = livyTaskSubmitHelper.post2LivyWithRetry(measure, pool);
                if (result == null) {
                    throw new AtlasBaseException("提交任务失败 : " + measure.getName());
                }

                while (!isDone(result.getState())) {
                    Thread.sleep(10000);
                    result = livyTaskSubmitHelper.getResultByLivyId(result.getId());
                }
            } catch (InterruptedException e) {
                throw new AtlasBaseException(e);
            } finally {
                if (result != null) {
                    livyTaskSubmitHelper.deleteByLivy(result.getId());
                }
            }

            String _MetricFile = LivyTaskSubmitHelper.getHdfsOutPath(task.getId(), task.getTimeStamp(), "_METRICS");
            HdfsUtils hdfsUtils = new HdfsUtils();
            String metricJson = String.join("\n", hdfsUtils.catFile(_MetricFile, -1));
            MeasureMetrics metrics = GsonUtils.getInstance().fromJson(metricJson, MeasureMetrics.class);
            errorCount = metrics.getValue().getData().stream().findFirst().map(jsonObject -> jsonObject.get("value").getAsLong()).orElse(0L);
            return errorCount;
        } finally {
            checkResult(task, errorCount.floatValue(), 0f);
        }
    }


    private boolean isDone(String status) {
        return "SUCCESS".equalsIgnoreCase(status) || "DEAD".equalsIgnoreCase(status);
    }

    public Measure buildMeasure(AtomicTaskExecution task, Long timestamp) {
        List<ConsistencyParam> consistencyParams = task.getConsistencyParams();
        if (consistencyParams == null || consistencyParams.isEmpty() || consistencyParams.size() < 2) {
            throw new AtlasBaseException("一致性校验的数据源必须大于一个");
        }


        ConsistencyParam standard = null;
        List<ConsistencyParam> contrasts = new ArrayList<>();
        Map<String, MeasureDataSource> dataSourceMap = new HashMap<>();
        for (ConsistencyParam consistencyParam : consistencyParams) {

            DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(consistencyParam.getDataSourceId());

            MeasureConnector connector = null;
            if (DataSourceType.getType(dataSourceInfo.getSourceType()).isBuildIn()) {
                connector = new MeasureConnector(new MeasureConnector.Config(consistencyParam.getSchema(), consistencyParam.getTable()));
                connector.setType("HIVE");
            } else {
                AdapterSource adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
                String[] fields = Stream.of(consistencyParam.getJoinFields(), consistencyParam.getCompareFields()).flatMap(Collection::stream).distinct().toArray(String[]::new);
                connector = new MeasureConnector(new MeasureConnector.Config(
                        adapterSource.getDriverClass(),
                        adapterSource.getJdbcUrl(),
                        dataSourceInfo.getUserName(),
                        dataSourceInfo.getPassword(),
                        consistencyParam.getSchema(),
                        consistencyParam.getTable(),
                        fields,
                        consistencyParam.getJoinFields().toArray(new String[0])
                ));
            }
            MeasureDataSource dataSource = new MeasureDataSource();
            dataSource.setName(consistencyParam.getId());
            dataSource.setConnector(connector);

            dataSourceMap.put(consistencyParam.getId(), dataSource);

            if (consistencyParam.isStandard()) {
                if (standard != null) {
                    throw new AtlasBaseException("基准数据源仅允许一个");
                }
                standard = consistencyParam;
            } else {
                contrasts.add(consistencyParam);
            }
        }

        if (standard == null) {
            throw new AtlasBaseException("基准数据源不存在");
        }


        List<MeasureRule> rules = new ArrayList<>();
        for (ConsistencyParam contrast : contrasts) {
            String outName = LivyTaskSubmitHelper.getOutName(contrast.getId());
            MeasureRuleOut recordOut = new MeasureRuleOut(MeasureRuleOut.Type.RECORD, outName);
            MeasureRule rule = new MeasureRule(buildConsistencyRuleDataSql(standard, contrast), outName, true, Collections.singletonList(recordOut));
            rules.add(rule);
        }

        MeasureRuleOut metricOut = new MeasureRuleOut(MeasureRuleOut.Type.METRIC, "data");
        MeasureRule rule = new MeasureRule(buildConsistencyRuleValueSql(standard, contrasts), LivyTaskSubmitHelper.getOutName(task.getId()), false, Collections.singletonList(metricOut));
        rules.add(rule);

        Measure measure = new Measure();
        measure.setName(task.getId());
        measure.setTimestamp(timestamp);
        measure.setDataSources(new ArrayList<>(dataSourceMap.values()));
        measure.setRule(new MeasureEvaluateRule(rules));

        return measure;
    }

    public static FunctionCall coalesce() {
        return new FunctionCall(new CustomSql("COALESCE"));
    }

    public static String fullFieldName(String table, String field) {
        return table + "." + field;
    }

    public String buildConsistencyRuleValueSql(ConsistencyParam standard, List<ConsistencyParam> contrasts) {
        List<String> dataSqls = new ArrayList<>();
        for (ConsistencyParam contrast : contrasts) {
            SelectQuery itemSql = new SelectQuery().addCustomFromTable(new CustomSql(LivyTaskSubmitHelper.getOutName(contrast.getId())));
            for (int i = 0; i < contrast.getJoinFields().size(); i++) {
                itemSql.addAliasedColumn(new CustomSql(contrast.getJoinFields().get(i)), standard.getJoinFields().get(i));
            }
            dataSqls.add(itemSql.toString());
        }

        SelectQuery valueSql = new SelectQuery()
                .addAliasedColumn(new CustomSql("COUNT(DISTINCT   " + String.join(",", standard.getJoinFields()) + " )"), "value")
                .addCustomFromTable(new CustomSql("(" + String.join(" UNION ", dataSqls) + ") out_data"));

        return valueSql.toString();
    }

    /**
     * 拼接一致性校验 sql
     * 通过 full join 实现，需要基准字段是唯一数据才有效
     */
    public String buildConsistencyRuleDataSql(ConsistencyParam standard, ConsistencyParam contrast) {

        if (standard.getJoinFields().size() != contrast.getJoinFields().size()) {
            throw new AtlasBaseException("一致性校验连接字段数目不对");
        }

        if (standard.getCompareFields().size() != contrast.getJoinFields().size()) {
            throw new AtlasBaseException("一致性校验比较字段数目不对");
        }


        SelectQuery allQuery = new SelectQuery();

        ComboCondition newCondition = ComboCondition.and();
        ComboCondition delCondition = ComboCondition.and();
        ComboCondition changeCondition = ComboCondition.and();
        ComboCondition joinCondition = ComboCondition.and();

        for (int i = 0; i < contrast.getJoinFields().size(); i++) {
            CustomSql contrastField = new CustomSql(fullFieldName(contrast.getId(), contrast.getJoinFields().get(i)));
            CustomSql standardField = new CustomSql(fullFieldName(standard.getId(), standard.getJoinFields().get(i)));
            allQuery.addAliasedColumn(
                    coalesce()
                            .addCustomParams(contrastField)
                            .addCustomParams(standardField)
                    , contrast.getJoinFields().get(i));

            joinCondition.addCondition(BinaryCondition.equalTo(standardField, contrastField));
            newCondition.addCondition(UnaryCondition.isNull(contrastField)).addCondition(UnaryCondition.isNotNull(standardField));
            delCondition.addCondition(UnaryCondition.isNotNull(contrastField)).addCondition(UnaryCondition.isNull(standardField));
        }


        for (int i = 0; i < contrast.getCompareFields().size(); i++) {
            CustomSql contrastField = new CustomSql(fullFieldName(contrast.getId(), contrast.getCompareFields().get(i)));
            CustomSql standardField = new CustomSql(fullFieldName(standard.getId(), standard.getCompareFields().get(i)));
            allQuery.addAliasedColumn(
                    coalesce()
                            .addCustomParams(contrastField)
                            .addCustomParams(standardField)
                    , contrast.getCompareFields().get(i));

            changeCondition.addCondition(BinaryCondition.equalTo(contrastField, standardField));
        }


        allQuery
                .addAliasedColumn(
                        new CaseStatement()
                                .addWhen(newCondition, "new")
                                .addWhen(delCondition, "deleted")
                                .addWhen(changeCondition, "identical")
                                .addElse("changed")
                        , "flag")
                .addCustomFromTable(new CustomSql(contrast.getId()))
                .addCustomJoin(SelectQuery.JoinType.FULL_OUTER, new CustomSql(contrast.getId()), new CustomSql(standard.getId()), joinCondition);


        SelectQuery filterQuery = new SelectQuery()
                .addAllColumns()
                .addCustomFromTable("(" + allQuery + ") joinTable")
                .addCondition(BinaryCondition.notEqualTo(new CustomSql("flag"), "identical"));

        return filterQuery.toString();
    }

    //规则值计算
    public Float ruleResultValue(AtomicTaskExecution task, boolean record, boolean columnRule, String pool) throws Exception {
        Float resultValue = null;
        try {
            engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
            String dbName = task.getDbName();
            String tableName = task.getTableName();
            String columnName = null;
            String user = MetaspaceConfig.getHiveAdmin();
            AdapterSource adapterSource;
            if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                adapterSource = AdapterUtils.getImpalaAdapterSource();
            } else {
                adapterSource = AdapterUtils.getHiveAdapterSource();
            }

            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            String superType = String.valueOf(jobType.code);
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
            if (Objects.nonNull(columnName)) {
                columnTypeKey = joiner.add(dbName).add(tableName).add(columnName).add(superType).toString();
            } else {
                columnTypeKey = joiner.add(dbName).add(tableName).add(superType).toString();
            }
            if (columnType2Result.containsKey(columnTypeKey)) {
                return columnType2Result.get(columnTypeKey);
            }

            LOG.info("query Sql: " + sql);

            Connection connection = adapterSource.getConnection(user, dbName, pool);
            resultValue = adapterSource.getNewAdapterExecutor().queryResult(connection, sql, resultSet -> {
                try {
                    Float value = null;
                    if (Objects.nonNull(resultSet)) {
                        while (resultSet.next()) {
                            Object object = resultSet.getObject(1);
                            if (Objects.nonNull(object)) {
                                value = Float.valueOf(object.toString());
                            }
                        }
                    }
                    return value;
                } catch (Exception e) {
                    throw new AtlasBaseException(e);
                }
            });

            if (Objects.nonNull(resultValue)) {
                columnType2Result.put(columnTypeKey, resultValue);
            }
            return resultValue;
        } catch (Exception e) {
            LOG.info(e.toString());
            throw e;
        } finally {
            if (record) {
                checkResult(task, resultValue, resultValue);
            }
        }
    }

    //规则值变化
    public Float ruleResultValueChange(AtomicTaskExecution task, boolean record, boolean columnRule, String pool) throws Exception {
        Float nowValue = null;
        Float valueChange = null;
        try {
            nowValue = ruleResultValue(task, false, columnRule, pool);
            String subTaskRuleId = task.getSubTaskRuleId();
            Float lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            valueChange = Math.abs(nowValue - lastValue);
            return valueChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task, valueChange, nowValue);
            }
        }
    }

    //规则值变化率
    public Float ruleResultChangeRatio(AtomicTaskExecution task, boolean record, boolean columnRule, String pool) throws Exception {
        Float ratio = null;
        Float ruleValueChange = 0F;
        Float lastValue = 0F;
        try {
            ruleValueChange = ruleResultValueChange(task, false, columnRule, pool);
            ruleValueChange = Objects.isNull(ruleValueChange) ? 0 : ruleValueChange;
            String subTaskRuleId = task.getSubTaskRuleId();
            lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = Objects.isNull(lastValue) ? 0 : lastValue;
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
                checkResult(task, ratio, ruleValueChange + lastValue);
            }
        }
    }

    //表大小
    public Float tableSize(AtomicTaskExecution task, boolean record, String pool) throws Exception {
        Float totalSize = null;
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        try {
            //表数据量
            engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
            if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                totalSize = AdapterUtils.getImpalaAdapterSource().getNewAdapterExecutor().getTableSize(dbName, tableName, pool);
            } else {
                totalSize = AdapterUtils.getHiveAdapterSource().getNewAdapterExecutor().getTableSize(dbName, tableName, pool);
            }
            return totalSize;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task, totalSize, totalSize);
            }
        }
    }

    //表大小变化
    public Float tableSizeChange(AtomicTaskExecution task, boolean record, String pool) throws Exception {
        Float tableSize = null;
        Float sizeChange = null;
        try {
            tableSize = tableSize(task, false, pool);
            String subTaskRuleId = task.getSubTaskRuleId();
            Float lastValue = taskManageDAO.getLastValue(subTaskRuleId);
            lastValue = (Objects.isNull(lastValue)) ? 0 : lastValue;
            sizeChange = Math.abs(tableSize - lastValue);
            return sizeChange;
        } catch (Exception e) {
            throw e;
        } finally {
            if (record) {
                checkResult(task, sizeChange, tableSize);
            }
        }
    }

    //表大小变化率
    public Float tableSizeChangeRatio(AtomicTaskExecution task, boolean record, String pool) throws Exception {
        Float tableSizeChange = 0F;
        Float lastValue = 0F;
        Float ratio = null;
        try {
            tableSizeChange = tableSizeChange(task, false, pool);
            tableSizeChange = (Objects.isNull(lastValue)) ? 0 : lastValue;
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
                checkResult(task, ratio, tableSizeChange + lastValue);
            }
        }
    }

    public void getProportion(AtomicTaskExecution task, String pool) throws Exception {
        Float ratio = null;
        String dbName = task.getDbName();
        String tableName = task.getTableName();

        AdapterSource adapterSource;
        String user = MetaspaceConfig.getHiveAdmin();
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
        if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            adapterSource = AdapterUtils.getImpalaAdapterSource();
        } else {
            adapterSource = AdapterUtils.getHiveAdapterSource();
        }
        try {
            Float nowNum = ruleResultValue(task, false, true, pool);
            String query = "select count(*) from %s";
            String sql = String.format(query, tableName);

            Connection connection = adapterSource.getConnection(user, dbName, pool);

            Float totalNum = adapterSource.getNewAdapterExecutor().queryResult(connection, sql, resultSet -> {
                try {
                    Float num = 0F;
                    if (Objects.nonNull(resultSet)) {
                        while (resultSet.next()) {
                            Object object = resultSet.getObject(1);
                            if (Objects.nonNull(object)) {
                                num = Float.valueOf(object.toString());
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
            checkResult(task, ratio, ratio);
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
            Integer ruleCheckTypeCode = subTaskRule.getCheckType();
            Integer checkExpressionCode = subTaskRule.getCheckExpression();
            RuleCheckType ruleCheckType = RuleCheckType.getRuleCheckTypeByCode(ruleCheckTypeCode);

            CheckExpression checkExpression = null;
            checkExpression = Objects.nonNull(checkExpressionCode) ? CheckExpression.getExpressionByCode(checkExpressionCode) : checkExpression;
            Float checkThresholdMinValue = null;
            Float checkThresholdMaxValue = null;


            RuleExecuteStatus orangeWarningcheckStatus = null;
            RuleExecuteStatus redWarningcheckStatus = null;
            if (Objects.nonNull(resultValue)) {
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
                    if (orangeCheckRuleCheckType == RuleCheckType.FIX) {
                        orangeCheckRuleCheckExpression = CheckExpression.getExpressionByCode(subTaskRule.getOrangeCheckExpression());
                    }
                    orangeWarningcheckStatus = checkResultStatus(orangeCheckRuleCheckType, orangeCheckRuleCheckExpression, resultValue, subTaskRule.getOrangeThresholdMinValue(), subTaskRule.getOrangeThresholdMaxValue());
                    if (RuleExecuteStatus.WARNING == orangeWarningcheckStatus) {
                        orangeWarningcheckStatus = RuleExecuteStatus.NORMAL;
                    } else if (RuleExecuteStatus.NORMAL == orangeWarningcheckStatus) {
                        orangeWarningcheckStatus = RuleExecuteStatus.WARNING;
                    }
                }

                if (Objects.nonNull(subTaskRule.getRedCheckExpression())) {
                    RuleCheckType redCheckRuleCheckType = RuleCheckType.getRuleCheckTypeByCode(subTaskRule.getRedCheckType());
                    CheckExpression redCheckRuleCheckExpression = null;
                    if (redCheckRuleCheckType == RuleCheckType.FIX) {
                        redCheckRuleCheckExpression = CheckExpression.getExpressionByCode(subTaskRule.getRedCheckExpression());
                    }
                    redWarningcheckStatus = checkResultStatus(redCheckRuleCheckType, redCheckRuleCheckExpression, resultValue, subTaskRule.getRedThresholdMinValue(), subTaskRule.getRedThresholdMaxValue());
                    if (RuleExecuteStatus.WARNING == redWarningcheckStatus) {
                        redWarningcheckStatus = RuleExecuteStatus.NORMAL;
                    } else if (RuleExecuteStatus.NORMAL == redWarningcheckStatus) {
                        redWarningcheckStatus = RuleExecuteStatus.WARNING;
                    }
                }
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            DataQualityTaskRuleExecute taskRuleExecute = new DataQualityTaskRuleExecute(task.getId(), task.getTaskExecuteId(), task.getTaskId(), task.getSubTaskId(), task.getObjectId(), task.getSubTaskRuleId(),
                    resultValue, referenceValue, Objects.isNull(checkStatus) ? 2 : checkStatus.getCode(), Objects.isNull(orangeWarningcheckStatus) ? null : orangeWarningcheckStatus.getCode(),
                    Objects.isNull(redWarningcheckStatus) ? null : redWarningcheckStatus.getCode(), WarningMessageStatus.WAITING.getCode(), currentTime, currentTime);

            taskManageDAO.updateRuleExecutionWarningInfo(taskRuleExecute);
            //橙色告警数量
            if (Objects.nonNull(orangeWarningcheckStatus) && orangeWarningcheckStatus == RuleExecuteStatus.WARNING) {
                taskManageDAO.updateTaskExecuteOrangeWarningNum(task.getTaskExecuteId());
                taskManageDAO.updateTaskOrangeWarningCount(task.getTaskId());
                taskManageDAO.updateTaskExecuteWarningStatus(task.getId(), WarningStatus.WARNING.code);
            }
            //红色告警数量
            if (Objects.nonNull(redWarningcheckStatus) && redWarningcheckStatus == RuleExecuteStatus.WARNING) {
                taskManageDAO.updateTaskExecuteRedWarningNum(task.getTaskExecuteId());
                taskManageDAO.updateTaskRedWarningCount(task.getTaskId());
                taskManageDAO.updateTaskExecuteWarningStatus(task.getId(), WarningStatus.WARNING.code);
            }
            //计算异常数量

        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
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
                    default:
                        break;
                }
            } else if (FLU == ruleCheckType) {
                if (resultValue >= checkThresholdMinValue && resultValue <= checkThresholdMaxValue) {
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
