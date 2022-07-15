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


import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.healthmarketscience.sqlbuilder.*;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterBaseException;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.model.dataquality.CheckExpression;
import io.zeta.metaspace.model.dataquality.RuleCheckType;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourceType;
import io.zeta.metaspace.model.measure.*;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.GsonUtils;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.service.dataquality.TaskManageService;
import io.zeta.metaspace.web.service.indexmanager.IndexCounter;
import io.zeta.metaspace.web.task.util.LivyTaskSubmitHelper;
import io.zeta.metaspace.web.task.util.QuartQueryProvider;
import io.zeta.metaspace.web.util.*;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.zeta.metaspace.model.dataquality.RuleCheckType.FIX;
import static io.zeta.metaspace.model.dataquality.RuleCheckType.FLU;
import static io.zeta.metaspace.model.dataquality.TaskType.EMPTY_VALUE_NUM_TABLE_REMAKR;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/25 17:28
 */


public class QuartzJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(QuartzJob.class);

    @Autowired
    private QuartzManager quartzManager;
    @Autowired
    private TaskManageDAO taskManageDAO;
    @Autowired
    private TaskManageService taskManageService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private LivyTaskSubmitHelper livyTaskSubmitHelper;
    @Autowired
    private IndexCounter indexCounter;

    private final int RETRY = 3;
    private Map<String, Float> columnType2Result = new HashMap<>();
    private static Configuration conf;
    private static String engine;

    public static final Map<String, Boolean> STATE_MAP = new HashMap<>();
    public static final String hiveId = "hive";
    public static final String EXECUTE_ID = "executeId";

    static {
        try {
            conf = ApplicationProperties.get();
        } catch (Exception e) {

        }
    }

    private String initExecuteInfo(String taskId, String taskExecuteId) {
        String userId = taskManageDAO.getTaskUpdater(taskId);
        DataQualityTaskExecute taskExecute = new DataQualityTaskExecute();
        String id = StringUtils.isEmpty(taskExecuteId)
                ? UUID.randomUUID().toString()
                : taskExecuteId;
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
        taskExecute.setGeneralWarningCount(0);
        taskExecute.setErrorStatus(0);
        taskExecute.setNumber(String.valueOf(System.currentTimeMillis()));
        Integer counter = taskManageDAO.getMaxCounter(taskId);
        taskExecute.setCounter(Objects.isNull(counter) ? 1 : ++counter);
        taskManageDAO.initTaskExecuteInfo(taskExecute);
        taskManageDAO.updateTaskExecutionCount(taskId);
        // TODO update了个寂寞
        taskManageDAO.updateTaskExecuteStatus(taskId, 1);
        taskManageDAO.updateTaskStatus(taskId, 1);
        return id;

    }

    private boolean canceled(String taskId, String taskExecuteId) {
        if (STATE_MAP.get(taskId)) {
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 4);
            taskManageDAO.updateTaskStatus(taskId, 4);
            taskManageDAO.updateTaskFinishedPercent(taskId, 0F);
            taskManageDAO.updateTaskExecutionFinishedPercent(taskExecuteId, 0F);
            return true;
        }
        return false;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        String taskId = "";
        String taskExecuteId = "";
        try {
            JobKey key = jobExecutionContext.getTrigger().getJobKey();
            LOG.warn("任务名为" + key.getName() + "开始执行");
            taskId = taskManageDAO.getTaskIdByQrtzName(key.getName());
            if (STATE_MAP.containsKey(taskId)) {
                //任务正在运行中，跳过本次执行
                return;
            }

            taskExecuteId = (String) jobExecutionContext.getJobDetail()
                    .getJobDataMap()
                    .getOrDefault(EXECUTE_ID, StringUtils.EMPTY);
            taskExecuteId = initExecuteInfo(taskId, taskExecuteId);
            STATE_MAP.put(taskId, false);
            EditionTaskInfo taskInfo = taskManageDAO.getTaskInfo(taskId);
            String tenantId = taskInfo.getTenantId();

            if (canceled(taskId, taskExecuteId)) {
                return;
            }
            //获取原子任务列表，包含子任务关联对象，子任务使用规则，子任务使用规则模板
            List<AtomicTaskExecution> taskList = taskManageDAO.getObjectWithRuleRelation(taskId, tenantId);
            if (Objects.isNull(taskList)) {
                quartzManager.handleNullErrorTask(key);
                LOG.warn("任务名为" + key.getName() + "所属任务已被删除,无法继续执行任务");
                return;
            }
            //补全数据
            completeTaskInformation(taskId, taskExecuteId, taskList);
            if (canceled(taskId, taskExecuteId)) {
                return;
            }
            //执行任务
            executeAtomicTaskList(taskId, taskExecuteId, taskList, tenantId);
            //取消任务
            canceled(taskId, taskExecuteId);
        } catch (Exception e) {
            if (StringUtils.isNotBlank(taskId)) {
                taskManageDAO.updateTaskStatus(taskId, 3);
            }
            if (StringUtils.isNotBlank(taskExecuteId)) {
                taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 3);
            }
            LOG.error(e.toString(), e);
        } finally {
            STATE_MAP.remove(taskId);
        }
    }

    private void completeTaskInformation(String taskId, String taskExecuteId, List<AtomicTaskExecution> taskList) throws AtlasBaseException {
        try {
            for (AtomicTaskExecution taskExecution : taskList) {
                taskExecution.setTaskId(taskId);
                taskExecution.setTaskExecuteId(taskExecuteId);
                String id = UUID.randomUUID().toString();
                taskExecution.setId(id);
                String objectId = taskExecution.getObjectId();
                String pool = taskManageDAO.getPool(taskExecution.getSubTaskId());
                taskExecution.setPool(pool);
                if (0 == taskExecution.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    taskExecution.setDataSourceId(paramInfo.getDataSourceId());
                    taskExecution.setDbName(paramInfo.getSchema());
                    taskExecution.setTableName(paramInfo.getTable());
                    taskExecution.setObjectName(paramInfo.getTable());
                } else if (1 == taskExecution.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    taskExecution.setDataSourceId(paramInfo.getDataSourceId());
                    taskExecution.setDbName(paramInfo.getSchema());
                    taskExecution.setTableName(paramInfo.getTable());
                    taskExecution.setObjectName(paramInfo.getColumn());
                } else if (2 == taskExecution.getScope()) {
                    String sparkConfig = taskManageDAO.geSparkConfig(taskExecution.getSubTaskId());
                    if (sparkConfig != null && sparkConfig.length() != 0) {
                        Map<String, Object> configMap = GsonUtils.getInstance().fromJson(sparkConfig, new TypeToken<Map<String, Integer>>() {
                        }.getType());
                        taskExecution.setConfig(configMap);
                    }
                    TaskType taskType = TaskType.getTaskByCode(taskExecution.getTaskType());
                    if (TaskType.CONSISTENCY.equals(taskType)) {
                        taskExecution.setConsistencyParams(GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                        }.getType()));
                    } else if (TaskType.CUSTOMIZE.equals(taskType)) {
                        taskExecution.setCustomizeParam(GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                        }.getType()));
                    } else {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                    }
                } else if (3 == taskExecution.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    taskExecution.setDataSourceId(paramInfo.getDataSourceId());
                    taskExecution.setDbName(paramInfo.getSchema());
                    taskExecution.setObjectName(paramInfo.getSchema());
                } else {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                }

            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e);
        }
    }


    private void executeAtomicTaskList(String taskId, String
            taskExecuteId, List<AtomicTaskExecution> taskList, String tenantId) throws Exception {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
        LOG.info("query engine:" + engine);
        int totalStep = taskList.size();
        long startTime = System.currentTimeMillis();
        StringBuilder errorMsg = new StringBuilder();
        for (int i = 0; i < totalStep; i++) {
            if (STATE_MAP.get(taskId)) {
                taskManageDAO.updateDataTaskCostTime(taskExecuteId, System.currentTimeMillis() - startTime);
                return;
            }
            //根据模板状态判断是否继续运行
            int retryCount = 0;
            AtomicTaskExecution task = taskList.get(i);
            task.setTaskId(taskId);
            task.setTaskExecuteId(taskExecuteId);
            long currentTime = System.currentTimeMillis() / 1000 * 1000;
            task.setTimeStamp(currentTime);
            Timestamp currentTimeStamp = new Timestamp(currentTime);
            taskManageDAO.initRuleExecuteInfo(task.getId(), taskExecuteId, taskId, task.getSubTaskId(), task.getObjectId(), task.getSubTaskRuleId(), currentTimeStamp, currentTimeStamp, 0, 0, task.getRuleId());
            do {
                try {
                    //执行任务
                    runJob(task);
                    float ratio = (float) (i + 1) / totalStep;
                    LOG.info("raion=" + ratio);
                    taskManageDAO.updateTaskFinishedPercent(taskId, ratio);
                    taskManageDAO.updateTaskExecutionFinishedPercent(taskExecuteId, 0F);
                    break;
                } catch (Exception e) {
                    if (STATE_MAP.get(taskId)) {
                        error(taskId, task, e);
                        return;
                    }
                    if (RETRY == retryCount) {
                        error(taskId, task, e);
                        return;
                    }
                    retryCount++;
                    LOG.info("retryCount=" + retryCount);
                    Thread.sleep((retryCount + 1) * 5000);
                    if (STATE_MAP.get(taskId)) {
                        return;
                    }
                } finally {
                    errorMsg.append(StringUtils.isBlank(task.getErrorMsg()) ? "" : task.getErrorMsg());
                    recordExecutionInfo(task, task.getErrorMsg(), tenantId);
                }
            } while (retryCount < RETRY);
        }
        if (StringUtils.isNotBlank(errorMsg)) {
            indexCounter.plusOne(IndexCounterUtils.METASPACE_QUALITY_TASK_FAIL_COUNT);
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 3);
            taskManageDAO.updateTaskStatus(taskId, 3);
        } else {
            indexCounter.plusOne(IndexCounterUtils.METASPACE_QUALITY_TASK_SUCCESS_COUNT);
            taskManageDAO.updateTaskExecuteStatus(taskExecuteId, 2);
            taskManageDAO.updateTaskStatus(taskId, 2);
        }
        taskManageDAO.updateDataTaskCostTime(taskExecuteId, System.currentTimeMillis() - startTime);
    }

    private void error(String taskId, AtomicTaskExecution task, Exception e) {
        taskManageDAO.updateTaskExecutionErrorMsg(task.getTaskExecuteId(), e.toString());
        taskManageDAO.updateTaskExecuteRuleErrorNum(task.getTaskExecuteId());
        taskManageDAO.updateTaskErrorCount(taskId);
        taskManageDAO.updateTaskExecuteErrorStatus(task.getTaskExecuteId(), WarningStatus.WARNING.code);
        taskManageDAO.updateRuleExecuteErrorStatus(task.getId(), WarningStatus.WARNING.code);
    }

    private void recordExecutionInfo(AtomicTaskExecution task, String errorMsg, String tenantId) {
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        String objectName = task.getObjectName();
        String source = dbName + "." + tableName;
        if (Objects.nonNull(objectName) && !objectName.equals(tableName)) {
            source += "." + objectName;
        }
        if (task.getTaskType().equals(TaskType.CONSISTENCY.getCode())) {
            StringBuilder str = new StringBuilder();
            str.append(task.getConsistencyParams().get(0).getSchema()).append(".").append(task.getConsistencyParams().get(0).getTable());
            str.append("~").append(task.getConsistencyParams().get(1).getSchema()).append(".").append(task.getConsistencyParams().get(1).getTable());
            source = str.toString();
        } else if (task.getTaskType().equals(TaskType.CUSTOMIZE.getCode())) {
            StringBuilder str = new StringBuilder();
            for (CustomizeParam customizeParam : task.getCustomizeParam()) {
                if (StringUtils.isBlank(customizeParam.getColumn())) {
                    str.append(customizeParam.getSchema()).append(".").append(customizeParam.getTable()).append("~");
                }
            }
            source = StringUtils.substring(str.toString(), 0, str.length() - 1);
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

        taskManageDAO.updateRuleExecuteErrorMsg(task.getId(), logJoiner.toString());
    }

    private void runJob(AtomicTaskExecution task) throws Exception {
        try {
            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            Measure measure = null;
            switch (jobType) {
                case TABLE_ROW_NUM:
                case EMPTY_VALUE_NUM_TABLE_REMAKR:
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
                    break;
                case CONSISTENCY:
                    measure = buildMeasure(task, task.getTimeStamp());
                    otherRuleCheck(task, measure);
                    break;
                case CUSTOMIZE:
                    measure = builderCustomizeMeasure(task, task.getTimeStamp());
                    otherRuleCheck(task, measure);
                    /*if (Objects.nonNull(measure)) {
                        otherRuleCheck(task, measure);
                    }*/
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LOG.info(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 其他规则校验
     * <p>
     * 1、livy提交任务
     * 2、监控 spark 任务状态
     * 3、从 Hdfs 上读取结果返回
     *
     * @param task
     * @return
     */
    private long otherRuleCheck(AtomicTaskExecution task, Measure measure) throws Exception {
        Long errorCount = 0L;
        try {
            MeasureLivyResult result = null;
            try {
                String pool = task.getPool();
                checkSparkConfig(task.getConfig());
                result = livyTaskSubmitHelper.post2LivyWithRetry(measure, pool, task);
                if (result == null) {
                    task.setErrorMsg("提交任务失败");
                    throw new AtlasBaseException("提交任务失败 : " + measure.getName());
                }
            } catch (Exception e) {
                throw new AtlasBaseException(e);
            } finally {
                if (result != null) {
                    //清除livy记录
                    livyTaskSubmitHelper.deleteByLivy(result.getId());
                    if ("DEAD".equalsIgnoreCase(result.getState())) {
                        //任务执行失败，抛出异常，不做后续处理
                        task.setErrorMsg("任务规则执行失败");
                        throw new AtlasException("任务规则执行失败 task rule id=" + task.getRuleId());
                    }
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

    private void checkSparkConfig(Map<String, Object> config) {
        if (config == null) {
            return;
        }
        if (config.containsKey("driverMemory")) {
            config.put("driverMemory", config.get("driverMemory").toString() + "g");
        }
        if (config.containsKey("executorMemory")) {
            config.put("executorMemory", config.get("executorMemory").toString() + "g");
        }
    }


    private boolean isDone(String status) {
        return "SUCCESS".equalsIgnoreCase(status) || "DEAD".equalsIgnoreCase(status);
    }

    /**
     * 自定义规则生成Measure
     *
     * @param task
     * @param timestamp
     * @return
     */
    public Measure builderCustomizeMeasure(AtomicTaskExecution task, Long timestamp) throws Exception {
        List<CustomizeParam> customizeParam = task.getCustomizeParam();
        List<CustomizeParam> tables = customizeParam.stream().filter(param -> param.getId().toLowerCase().contains("table")).collect(Collectors.toList());
        List<CustomizeParam> columns = customizeParam.stream().filter(param -> param.getId().toLowerCase().contains("column")).collect(Collectors.toList());
        // 神通数据库不走livy提交规则，通过jdbc执行自定义规则sql语句
        /*if (isOscarType(task)) {
            oscarCustomHandle(task, tables, columns);
            return null;
        }*/
        Map<String, MeasureDataSource> dataSourceMap = new HashMap<>();
        for (CustomizeParam table : tables) {
            MeasureConnector connector = null;
            if (StringUtils.isEmpty(table.getDataSourceId()) || hiveId.equals(table.getDataSourceId())) {
                connector = new MeasureConnector(new MeasureConnector.Config(table.getSchema(), table.getTable()));
                connector.setType("HIVE");
            } else {
                DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(table.getDataSourceId());
                AdapterSource adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
                connector = new MeasureConnector(new MeasureConnector.Config(
                        adapterSource.getDriverClass(),
                        adapterSource.getJdbcUrl(),
                        dataSourceInfo.getUserName(),
                        dataSourceInfo.getPassword(),
                        table.getSchema(),
                        table.getTable(),
                        null,
                        null
                ));
            }
            MeasureDataSource dataSource = new MeasureDataSource();
            dataSource.setName(table.getId());
            dataSource.setConnector(connector);

            dataSourceMap.put(table.getId(), dataSource);
        }

        String sql = task.getSql();
        if (tables != null) {
            for (CustomizeParam table : tables) {
                sql = sql.replaceAll("\\$\\{" + table.getId() + "\\}", table.getId());
            }
        }
        if (columns != null) {
            for (CustomizeParam column : columns) {
                sql = sql.replaceAll("\\$\\{" + column.getId() + "\\}", "`" + column.getColumn() + "`");
            }
        }
        List<MeasureRule> rules = new ArrayList<>();
        String outName = LivyTaskSubmitHelper.getOutName("data");
        MeasureRuleOut recordOut = new MeasureRuleOut(MeasureRuleOut.Type.RECORD, outName);
        MeasureRule rule = new MeasureRule(sql, outName, false, Collections.singletonList(recordOut));
        rules.add(rule);

        String countSql = "select count(*) as value from " + outName;
        MeasureRuleOut metricOut = new MeasureRuleOut(MeasureRuleOut.Type.METRIC, "data");
        MeasureRule countRule = new MeasureRule(countSql, LivyTaskSubmitHelper.getOutName(task.getId()), false, Collections.singletonList(metricOut));
        rules.add(countRule);

        Measure measure = new Measure();
        measure.setName(task.getId());
        measure.setTimestamp(timestamp);
        measure.setDataSources(new ArrayList<>(dataSourceMap.values()));
        measure.setRule(new MeasureEvaluateRule(rules));

        return measure;
    }

    /**
     * 一致性生成Measure
     *
     * @param task
     * @param timestamp
     * @return
     */
    private Measure buildMeasure(AtomicTaskExecution task, Long timestamp) {
        List<ConsistencyParam> consistencyParams = task.getConsistencyParams();
        if (consistencyParams == null || consistencyParams.isEmpty() || consistencyParams.size() < 2) {
            throw new AtlasBaseException("一致性校验的数据源必须大于一个");
        }


        ConsistencyParam standard = null;
        List<ConsistencyParam> contrasts = new ArrayList<>();
        Map<String, MeasureDataSource> dataSourceMap = new HashMap<>();
        for (ConsistencyParam consistencyParam : consistencyParams) {

            MeasureConnector connector = null;
            if (StringUtils.isEmpty(consistencyParam.getDataSourceId()) || hiveId.equals(consistencyParam.getDataSourceId())) {
                connector = new MeasureConnector(new MeasureConnector.Config(consistencyParam.getSchema(), consistencyParam.getTable()));
                connector.setType("HIVE");
            } else {
                DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(consistencyParam.getDataSourceId());
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

        if (standard.getCompareFields().size() != contrast.getCompareFields().size()) {
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
    public Float ruleResultValue(AtomicTaskExecution task, boolean record, boolean columnRule) throws Exception {
        Float resultValue = 0.0f;
        String pool = task.getPool();
        try {
            engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
            String dbName = task.getDbName();
            String tableName = task.getTableName();
            String columnName = null;
            String user = MetaspaceConfig.getHiveAdmin();
            AdapterSource adapterSource;
            String sourceType;
            if (task.getDataSourceId() == null || hiveId.equals(task.getDataSourceId())) {
                if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                    adapterSource = AdapterUtils.getImpalaAdapterSource();
                    sourceType = "IMPALA";
                } else {
                    adapterSource = AdapterUtils.getHiveAdapterSource();
                    sourceType = "HIVE";
                }
            } else {
                DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(task.getDataSourceId());
                adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
                sourceType = dataSourceInfo.getSourceType();
            }

            //表名列名转义
            AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
            if (StringUtils.isNotBlank(tableName)) {
                tableName = adapterExecutor.addEscapeChar(tableName);
            }
            String sqlDbName = adapterExecutor.addSchemaEscapeChar(dbName);
            TaskType jobType = TaskType.getTaskByCode(task.getTaskType());
            String query = QuartQueryProvider.getQuery(jobType);
            String sql = null;
            String superType = String.valueOf(jobType.code);
            String fileName = LivyTaskSubmitHelper.getOutName("data");
            String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(task.getId(), task.getTimeStamp(), fileName);
            if (columnRule) {
                columnName = adapterExecutor.addEscapeChar(task.getObjectName());
                switch (jobType) {
                    case UNIQUE_VALUE_NUM:
                    case UNIQUE_VALUE_NUM_CHANGE:
                    case UNIQUE_VALUE_NUM_CHANGE_RATIO:
                    case UNIQUE_VALUE_NUM_RATIO:
                        writeErrorData(jobType, tableName, columnName, sqlDbName, adapterSource, adapterSource.getConnection(user, dbName, pool), hdfsOutPath, sourceType, null);
                        sql = String.format(query, sqlDbName, tableName, columnName, columnName, sqlDbName, tableName, columnName);
                        break;
                    case DUP_VALUE_NUM:
                    case DUP_VALUE_NUM_CHANGE:
                    case DUP_VALUE_NUM_CHANGE_RATIO:
                    case DUP_VALUE_NUM_RATIO:
                        writeErrorData(jobType, tableName, columnName, sqlDbName, adapterSource, adapterSource.getConnection(user, dbName, pool), hdfsOutPath, sourceType, null);

                        sql = String.format(query, columnName, sqlDbName, tableName, columnName, columnName, sqlDbName, tableName, columnName);
                        break;
                    case EMPTY_VALUE_NUM:
                    case EMPTY_VALUE_NUM_CHANGE:
                    case EMPTY_VALUE_NUM_CHANGE_RATIO:
                    case EMPTY_VALUE_NUM_RATIO:
                        writeErrorData(jobType, tableName, columnName, sqlDbName, adapterSource, adapterSource.getConnection(user, dbName, pool), hdfsOutPath, sourceType, null);
                        sql = String.format(query, sqlDbName, tableName, columnName, columnName);
                        break;

                    default:
                        sql = String.format(query, columnName, sqlDbName, tableName);
                        break;
                }
            } else {
                switch (jobType) {
                    case EMPTY_VALUE_NUM_TABLE_REMAKR:
                        HashMap<String, Object> map = new HashMap<>();
                        resultValue = adapterExecutor.getTblRemarkCountByDb(adapterSource, user, dbName, pool, map);
                        writeErrorData(jobType, tableName, columnName, dbName, adapterSource, adapterSource.getConnection(user, dbName, pool), hdfsOutPath, sourceType, map);
                        return resultValue;
                    default:
                        sql = String.format(query, sqlDbName, tableName);
                        break;
                }
            }

            String columnTypeKey = null;
            StringJoiner joiner = new StringJoiner(".");
            if (Objects.nonNull(columnName)) {
                columnTypeKey = joiner.add(sqlDbName).add(tableName).add(columnName).add(superType).toString();
            } else {
                columnTypeKey = joiner.add(sqlDbName).add(tableName).add(superType).toString();
            }
            if (columnType2Result.containsKey(columnTypeKey)) {
                return columnType2Result.get(columnTypeKey);
            }

            LOG.info("query Sql: " + sql);

            Connection connection = adapterSource.getConnection(user, dbName, pool);
            resultValue = adapterExecutor.queryResult(connection, sql, resultSet -> {
                try {
                    Float value = 0.0f;
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

    public void writeErrorData(TaskType jobType, String tableName, String columnName, String
            sqlDbName, AdapterSource adapterSource, Connection connection, String hdfsOutPath, String
                                       sourceType, Map<String, Object> mapVal) {
        String errDataSql = QuartQueryProvider.getErrData(jobType, sourceType);
        String sql;
        HdfsUtils hdfsUtils = new HdfsUtils();
        try (BufferedWriter fileBufferWriter = hdfsUtils.getFileBufferWriter(hdfsOutPath);) {
            if (jobType == EMPTY_VALUE_NUM_TABLE_REMAKR) {
                if (sourceType.equalsIgnoreCase("IMPALA") || sourceType.equalsIgnoreCase("HIVE")) {
                    List<String> emptyTblNameList = (List<String>) mapVal.get("emptyTblNameList");
                    if (!CollectionUtils.isEmpty(emptyTblNameList)) {
                        for (String item : emptyTblNameList) {
                            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                            map.put("TABLE_NAME", item);
                            fileBufferWriter.write(GsonUtils.getInstance().toJson(map) + "\n");
                        }
                        fileBufferWriter.flush();
                    }
                    return;
                }
            }
        } catch (Exception e) {
            throw new AdapterBaseException("解析查询结果失败", e);
        }


        switch (jobType) {
            case UNIQUE_VALUE_NUM:
            case UNIQUE_VALUE_NUM_CHANGE:
            case UNIQUE_VALUE_NUM_CHANGE_RATIO:
            case UNIQUE_VALUE_NUM_RATIO:
            case DUP_VALUE_NUM:
            case DUP_VALUE_NUM_CHANGE:
            case DUP_VALUE_NUM_CHANGE_RATIO:
            case DUP_VALUE_NUM_RATIO:

                sql = String.format(errDataSql, columnName, sqlDbName, tableName, columnName);
                break;
            case EMPTY_VALUE_NUM:
            case EMPTY_VALUE_NUM_CHANGE:
            case EMPTY_VALUE_NUM_CHANGE_RATIO:
            case EMPTY_VALUE_NUM_RATIO:
                sql = String.format(errDataSql, sqlDbName, tableName, columnName, columnName);
                break;
            case EMPTY_VALUE_NUM_TABLE_REMAKR:
                sql = getSql(errDataSql, sqlDbName, sourceType);
                break;
            default:
                sql = sql = String.format(errDataSql, sqlDbName, tableName, columnName, columnName);
                break;
        }

        AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
        LOG.info("query sql = " + sql);
        adapterExecutor.queryResultByFetchSize(connection, sql, resultSet -> {
            try (BufferedWriter fileBufferWriter = hdfsUtils.getFileBufferWriter(hdfsOutPath);) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (resultSet.next()) {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String column = metaData.getColumnName(i);
                        if (column.contains(".")) {
                            column = column.substring(column.lastIndexOf(".") + 1, column.length());
                        }
                        Object value = resultSet.getObject(column);
                        if (value instanceof Clob) {
                            Clob clob = (Clob) value;
                            StringBuilder buffer = new StringBuilder();
                            clob.getCharacterStream();
                            BufferedReader br = new BufferedReader(clob.getCharacterStream());
                            clob.getCharacterStream();
                            String line = br.readLine();
                            while (line != null) {
                                buffer.append(line);
                                line = br.readLine();
                            }
                            value = buffer.toString();
                        } else if (value instanceof Timestamp) {
                            Timestamp timValue = (Timestamp) value;
                            value = timValue.toString();
                        } else {
                            value = adapterSource.getAdapter().getAdapterTransformer().convertColumnValue(value);
                        }

                        map.put(column, value);
                    }
                    fileBufferWriter.write(GsonUtils.getInstance().toJson(map) + "\n");
                }
                fileBufferWriter.flush();
                return null;
            } catch (Exception e) {
                throw new AdapterBaseException("解析查询结果失败", e);
            }

        });

    }

    private String getSql(String errDataSql, String sqlDbName, String sourceType) {
        if (!sourceType.equalsIgnoreCase("POSTGRESQL")) {
            if (sourceType.equalsIgnoreCase("OSCAR")){
                errDataSql = String.format(errDataSql, sqlDbName, sqlDbName);
            } else {
                errDataSql = String.format(errDataSql, sqlDbName);
            }
        }
        return errDataSql;
    }

    //规则值变化
    public Float ruleResultValueChange(AtomicTaskExecution task, boolean record, boolean columnRule) throws
            Exception {
        Float nowValue = null;
        Float valueChange = null;
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
                checkResult(task, valueChange, nowValue);
            }
        }
    }

    //规则值变化率
    public Float ruleResultChangeRatio(AtomicTaskExecution task, boolean record, boolean columnRule) throws
            Exception {
        Float ratio = null;
        Float ruleValueChange = 0F;
        Float lastValue = 0F;
        try {
            ruleValueChange = ruleResultValueChange(task, false, columnRule);
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
    public Float tableSize(AtomicTaskExecution task, boolean record) throws Exception {
        Float totalSize = null;
        String dbName = task.getDbName();
        String tableName = task.getTableName();
        String pool = task.getPool();
        try {
            //表数据量
            AdapterSource adapterSource = null;
            if (task.getDataSourceId() == null || hiveId.equals(task.getDataSourceId())) {
                engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
                if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                    adapterSource = AdapterUtils.getImpalaAdapterSource();
                } else {
                    adapterSource = AdapterUtils.getHiveAdapterSource();
                }
            } else {
                DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(task.getDataSourceId());
                adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
            }

            totalSize = adapterSource.getNewAdapterExecutor().getTableSize(dbName, tableName, pool);
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
    public Float tableSizeChange(AtomicTaskExecution task, boolean record) throws Exception {
        Float tableSize = null;
        Float sizeChange = null;
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
                checkResult(task, sizeChange, tableSize);
            }
        }
    }

    //表大小变化率
    public Float tableSizeChangeRatio(AtomicTaskExecution task, boolean record) throws Exception {
        Float tableSizeChange = 0F;
        Float lastValue = 0F;
        Float ratio = null;
        try {
            tableSizeChange = tableSizeChange(task, false);
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

    public void getProportion(AtomicTaskExecution task) throws Exception {
        Float ratio = null;
        String dbName = task.getDbName();
        String tableName = task.getTableName();

        AdapterSource adapterSource;
        String user = MetaspaceConfig.getHiveAdmin();
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf, String::valueOf);
        if (task.getDataSourceId() == null || hiveId.equals(task.getDataSourceId())) {
            if (Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                adapterSource = AdapterUtils.getImpalaAdapterSource();
            } else {
                adapterSource = AdapterUtils.getHiveAdapterSource();
            }
        } else {
            DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(task.getDataSourceId());
            adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
        }
        try {
            AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
            tableName = adapterExecutor.addEscapeChar(tableName);
            String pool = task.getPool();
            Float nowNum = ruleResultValue(task, false, true);
            String query = "select count(*) from %s";
            String sql = String.format(query, tableName);

            Connection connection = adapterSource.getConnection(user, dbName, pool);

            Float totalNum = adapterExecutor.queryResult(connection, sql, resultSet -> {
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
    public RuleExecuteStatus checkResult(AtomicTaskExecution task, Float resultValue, Float referenceValue) throws
            Exception {
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

            // 数据库更新告警信息
            taskManageService.updateWarningInfo(task, checkStatus, orangeWarningcheckStatus, redWarningcheckStatus, taskRuleExecute);

            //计算异常数量

        } catch (Exception e) {
            task.setErrorMsg(e.getMessage());
            LOG.error("checkResult EXCEPTION IS {}", e);
            throw e;
        }
        //if (checkStatus == null) throw new RuntimeException();
        return checkStatus;
    }

    public RuleExecuteStatus checkResultStatus(RuleCheckType ruleCheckType, CheckExpression checkExpression, Float
            resultValue, Float checkThresholdMinValue, Float checkThresholdMaxValue) {
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

    public float oscarCustomHandle(AtomicTaskExecution
                                           task, List<CustomizeParam> tables, List<CustomizeParam> columns) throws Exception {
        Float resultValue = 0.0f;
        try {
            AdapterSource adapterSource = getOscarAdapterSource(task);
            String sql = buildExecuteSql(task, tables, columns);
            Connection connection = adapterSource.getConnection(MetaspaceConfig.getHiveAdmin(), task.getDbName(), task.getPool());
            AdapterExecutor adapterExecutor = adapterSource.getNewAdapterExecutor();
            resultValue = adapterExecutor.queryResult(connection, sql, resultSet -> {
                try {
                    Float value = 0.0f;
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
            return resultValue;
        } catch (Exception e) {
            LOG.error("oscarCustomHandle fail:{}", e);
            throw e;
        } finally {
            checkResult(task, resultValue, resultValue);
        }
    }

    private AdapterSource getOscarAdapterSource(AtomicTaskExecution task) throws Exception {
        String json = task.getObjectId();
        if (StringUtils.isEmpty(json)) {
            new AtlasBaseException("自定义规则动态参数字符串串不能为空！");
        }
        JsonArray jsonArray = JsonUtils.toJsonArray(json);
        AdapterSource adapterSource = null;
        Optional.ofNullable(jsonArray).orElseThrow(() -> new AtlasBaseException("自定义规则动态参数数组不能为空！"));
        if (jsonArray.size() > 0) {
            String item = jsonArray.get(0).toString();
            String datasourceId = String.valueOf(JsonUtils.toJsonObject(item).get("dataSourceId")).replaceAll("\"", "");
            DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(datasourceId);
            adapterSource = AdapterUtils.getAdapterSource(dataSourceInfo);
        }
        return adapterSource;
    }

    private String buildExecuteSql(AtomicTaskExecution
                                           task, List<CustomizeParam> tables, List<CustomizeParam> columns) throws Exception {
        String sql = task.getSql();
        if (tables != null) {
            for (CustomizeParam table : tables) {
                StringJoiner tableJoiner = new StringJoiner(".");
                sql = sql.replaceAll("\\$\\{" + table.getId() + "\\}", tableJoiner.add(table.getSchema()).add(table.getTable()).toString());
            }
        }
        if (columns != null) {
            for (CustomizeParam column : columns) {
                StringJoiner columnJoiner = new StringJoiner(".");
                sql = sql.replaceAll("\\$\\{" + column.getId() + "\\}", columnJoiner.add(column.getSchema()).add(column.getTable()).add(column.getColumn()).toString());
            }
        }
        return sql;
    }

    public boolean isOscarType(AtomicTaskExecution task) throws Exception {
        boolean isOscar = false;
        String json = task.getObjectId();
        if (StringUtils.isEmpty(json)) {
            new AtlasBaseException("自定义规则动态参数字符串串不能为空！");
        }
        JsonArray jsonArray = JsonUtils.toJsonArray(json);
        AdapterSource adapterSource = null;
        Optional.ofNullable(jsonArray).orElseThrow(() -> new AtlasBaseException("自定义规则动态参数数组不能为空！"));
        if (jsonArray.size() > 0) {
            String item = jsonArray.get(0).toString();
            String datasourceId = String.valueOf(JsonUtils.toJsonObject(item).get("dataSourceId")).replaceAll("\"", "");
            DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(datasourceId);
            String sourceType = dataSourceInfo.getSourceType();
            if (DataSourceType.OSCAR.getName().equals(sourceType)) {
                isOscar = true;
            }
        }
        return isOscar;
    }
}
