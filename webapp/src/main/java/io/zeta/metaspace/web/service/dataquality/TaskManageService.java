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
 * @date 2019/7/24 10:29
 */
package io.zeta.metaspace.web.service.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 10:29
 */

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.dataquality.Schedule;
import io.zeta.metaspace.model.dataquality.TaskType;
import io.zeta.metaspace.model.dataquality2.ConsistencyParam;
import io.zeta.metaspace.model.dataquality2.CustomizeParam;
import io.zeta.metaspace.model.dataquality2.DataQualityBasicInfo;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskObject;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTask;
import io.zeta.metaspace.model.dataquality2.EditionTaskInfo;
import io.zeta.metaspace.model.dataquality2.ErrorData;
import io.zeta.metaspace.model.dataquality2.ExecutionLog;
import io.zeta.metaspace.model.dataquality2.ExecutionLogHeader;
import io.zeta.metaspace.model.dataquality2.ExecutionReportData;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.dataquality2.ObjectType;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleHeader;
import io.zeta.metaspace.model.dataquality2.RuleTemplateType;
import io.zeta.metaspace.model.dataquality2.SubTaskRecord;
import io.zeta.metaspace.model.dataquality2.TaskExecutionReport;
import io.zeta.metaspace.model.dataquality2.TaskHeader;
import io.zeta.metaspace.model.dataquality2.TaskInfo;
import io.zeta.metaspace.model.dataquality2.TaskRuleExecutionRecord;
import io.zeta.metaspace.model.dataquality2.TaskRuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.dataquality2.WarningType;
import io.zeta.metaspace.model.datasource.DataSourceHead;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.ColumnParameters;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.security.Pool;
import io.zeta.metaspace.model.security.Queue;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.utils.GsonUtils;
import io.zeta.metaspace.web.dao.DataSourceDAO;
import io.zeta.metaspace.web.dao.dataquality.TaskManageDAO;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.task.quartz.QuartzJob;
import io.zeta.metaspace.web.task.quartz.QuartzManager;
import io.zeta.metaspace.web.task.util.LivyTaskSubmitHelper;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HdfsUtils;
import io.zeta.metaspace.web.util.PoiExcelUtils;
import io.zeta.metaspace.web.util.QualityEngine;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tinkerpop.shaded.minlog.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TaskManageService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManageService.class);
    public static String JOB_GROUP_NAME = "METASPACE_JOBGROUP";
    public static String TRIGGER_NAME = "METASPACE_TRIGGER";
    public static String TRIGGER_GROUP_NAME = "METASPACE_TRIGGERGROUP";
    private static String engine;
    private static Configuration conf;
    private static Cache<String, List<String>> errorDataCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(30, TimeUnit.MINUTES).build();
    static {
        try {
            conf = ApplicationProperties.get();
        }  catch (Exception e) {
            LOG.error(e.toString());
        }
    }
    private static int errorDataSize=2000;

    @Autowired
    TaskManageDAO taskManageDAO;
    @Autowired
    RuleService ruleService;
    @Autowired
    QuartzManager quartzManager;
    @Autowired
    UsersService usersService;
    @Autowired
    MetaspaceGremlinQueryService metaspaceEntityService;
    @Autowired
    BusinessService businessService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private DataShareService dataShareService;
    @Autowired
    private DataSourceDAO dataSourceDAO;

    public PageResult<TaskHeader> getTaskList(Integer my, Parameters parameters,String tenantId) throws AtlasBaseException {
        try {
            String userId = AdminUtils.getUserData().getUserId();
            List<TaskHeader> list = taskManageDAO.getTaskList(my, userId, parameters,tenantId);

            long totalSize = 0;
            if (list.size()!=0){
                totalSize = list.get(0).getTotal();
            }
            PageResult<TaskHeader> pageResult = new PageResult<>();
            pageResult.setTotalSize(totalSize);
            pageResult.setCurrentSize(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取任务列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务列表失败");
        }
    }

    public PageResult getTableList(String dbName, Parameters parameters) throws AtlasBaseException {
        try {
            String databaseId = taskManageDAO.getDbIdByDbName(dbName);
            PageResult<Table> pageResult = metaspaceEntityService.getTableByDB(databaseId, true, parameters.getOffset(), parameters.getLimit());

            List<Table> tableList = pageResult.getLists();
            if(Objects.nonNull(tableList) && tableList.size()>0) {
                Table tmpTable = taskManageDAO.getDbAndTableName(tableList.get(0).getTableId());
                dbName = tmpTable.getDatabaseName();
            }
            for (Table table : tableList) {
                table.setDatabaseName(dbName);
            }
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表失败");
        }
    }

    public PageResult getColumnList(String dbName,String tableName, ColumnParameters parameters, String tenantId) throws AtlasBaseException {
        try {
            String tableGuid = taskManageDAO.getTableId(dbName,tableName);
            List<String> ruleIds = parameters.getRuleIds();
            //判断规则是否还有数值型规则
            int count = 0;
            if (ruleIds!=null&&ruleIds.size()!=0){
                count = taskManageDAO.getNumericTypeTemplateRuleIdCount(ruleIds,tenantId);
            }
            boolean isNum = count!=0;
            PageResult<Column> pageResult = businessService.getTableColumnList(tableGuid, parameters, null, null,isNum);
            List<Column> columnList = pageResult.getLists();
            if(columnList!=null && columnList.size()>0) {  //JanusGraph中无column信息
                for (Column column : columnList) {
                    Column tmpColumn = taskManageDAO.getDbAndTableAndColumnName(column.getColumnId());
                    column.setDatabaseName(tmpColumn.getDatabaseName());
                    column.setTableName(tmpColumn.getTableName());
                }
            }
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取字段列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取字段列表失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void deleteTaskList(List<String> taskIdList) throws AtlasBaseException {
        try {
            if(Objects.nonNull(taskIdList)) {
                for (String taskId:taskIdList){
                    String jobName = taskManageDAO.getQrtzJobByTaskId(taskId);
                    if (jobName!=null&&jobName.length()!=0){
                        String jobGroupName = JOB_GROUP_NAME + jobName;
                        String triggerName = TRIGGER_NAME + jobName;
                        String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
                        quartzManager.removeJob(jobName, jobGroupName,triggerName,triggerGroupName);
                    }
                }
                taskManageDAO.deleteTaskList(taskIdList);
                taskManageDAO.deleteSubTaskList(taskIdList);
                taskManageDAO.deleteSubTaskObjectList(taskIdList);
                taskManageDAO.deleteSubTaskRuleList(taskIdList);
                taskManageDAO.deleteWarningGroupUsed(taskIdList);
            }
        } catch (Exception e) {
            LOG.error("删除任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除任务失败");
        }
    }

    public List<RuleHeader> getValidRuleList(String groupId, String scope,String tenantId) throws AtlasBaseException {
        try {
            Integer objType = null;
            if (!"all".equals(scope)){
                objType=Integer.valueOf(scope);
            }
            List<RuleHeader> ruleList = taskManageDAO.getRuleListByCategoryId(groupId, objType,tenantId);
            return ruleList;
        }  catch (Exception e) {
            LOG.error("获取规则列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取规则列表失败");
        }
    }

    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroupList(String groupId,String tenantId) throws AtlasBaseException {
        try {
            return taskManageDAO.getWarningGroupList(groupId,tenantId);
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }

    public List<TaskWarningHeader.WarningGroupHeader> getAllWarningGroup(String tenantId) throws AtlasBaseException {
        try {
            return taskManageDAO.getAllWarningGroup(tenantId);
        } catch (Exception e) {
            LOG.error("获取告警组列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取告警组列表失败");
        }
    }

    public void addTask(TaskInfo taskInfo,String tenantId) throws AtlasBaseException {
        Timestamp currentTime = DateUtils.currentTimestamp();
        addDataQualityTask(currentTime, taskInfo,tenantId);
    }

    @Transactional(rollbackFor=Exception.class)
    public void addDataQualityTask(Timestamp currentTime, TaskInfo taskInfo,String tenantId) throws AtlasBaseException {
        try {
            DataQualityTask dataQualityTask = new DataQualityTask();
            //id
            String guid = UUID.randomUUID().toString();
            dataQualityTask.setId(guid);
            //任务名称
            dataQualityTask.setName(taskInfo.getTaskName());
            //任务级别
            dataQualityTask.setLevel(taskInfo.getLevel());
            //描述
            dataQualityTask.setDescription(taskInfo.getDescription());
            //cron表达式
            dataQualityTask.setCronExpression(taskInfo.getCronExpression());
            //执行起始时间
            dataQualityTask.setStartTime(taskInfo.getStartTime());
            //执行截止时间
            dataQualityTask.setEndTime(taskInfo.getEndTime());

            //创建时间
            dataQualityTask.setCreateTime(currentTime);
            //更新时间
            dataQualityTask.setUpdateTime(currentTime);
            //创建者
            String userId = AdminUtils.getUserData().getUserId();
            dataQualityTask.setCreator(userId);
            dataQualityTask.setUpdater(userId);
            //是否已删除
            dataQualityTask.setDelete(false);
            //是否开启
            dataQualityTask.setEnable(false);
            //orangeWarningCount
            dataQualityTask.setOrangeWarningTotalCount(0);
            //redWarningCount
            dataQualityTask.setRedWarningTotalCount(0);
            //errorCount
            dataQualityTask.setErrorTotalCount(0);
            //executionCount
            dataQualityTask.setExecutionCount(0);
            //子任务
            List<TaskInfo.SubTask> subTaskList = taskInfo.getTaskList();
            addDataQualitySubTask(guid, currentTime, subTaskList,tenantId);

            if (taskInfo.getContentWarningNotificationIdList()!=null&&taskInfo.getContentWarningNotificationIdList().size()!=0){
                taskManageDAO.addTaskWarningGroup(guid, WarningType.WARNING.code, taskInfo.getContentWarningNotificationIdList());
            }
            if (taskInfo.getExecutionWarningNotificationIdList()!=null&&taskInfo.getExecutionWarningNotificationIdList().size()!=0){
                taskManageDAO.addTaskWarningGroup(guid, WarningType.ERROR.code, taskInfo.getExecutionWarningNotificationIdList());
            }


            taskManageDAO.addDataQualityTask(dataQualityTask,tenantId);

            taskManageDAO.updateTaskStatus(guid, 0);
            taskManageDAO.updateTaskFinishedPercent(guid, 0F);

        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public void addDataQualitySubTask(String taskId, Timestamp currentTime, List<TaskInfo.SubTask> subTaskList,String tenantId) throws AtlasBaseException {
        try {
            for(int i=0, size=subTaskList.size(); i<size; i++) {
                TaskInfo.SubTask subTask = subTaskList.get(i);
                Gson gson = new Gson();
                DataQualitySubTask dataQualitySubTask = new DataQualitySubTask();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTask.setId(guid);
                //taskId
                dataQualitySubTask.setTaskId(taskId);
                //数据源类型
                dataQualitySubTask.setDataSourceType(subTask.getDataSourceType());
                //序号
                dataQualitySubTask.setSequence(i+1);
                //创建时间
                dataQualitySubTask.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTask.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTask.setDelete(false);
                // 资源池
                dataQualitySubTask.setPool(subTask.getPool());
                // spark配置
                String config = null;
                if (subTask.getConfig()!=null){
                    config = GsonUtils.getInstance().toJson(subTask.getConfig());
                }
                dataQualitySubTask.setConfig(config);
                //subTaskRule
                List<TaskInfo.SubTaskRule> subTaskRuleList = subTask.getSubTaskRuleList();
                addDataQualitySubTaskRule(guid, currentTime, subTaskRuleList,tenantId);
                //object
                List<String> objectIdList=subTask.getObjectIdList().stream().map(obj->GsonUtils.getInstance().toJson(obj)).collect(Collectors.toList());
                addDataQualitySubTaskObject(taskId, guid, currentTime, objectIdList);
                taskManageDAO.addDataQualitySubTask(dataQualitySubTask);
            }
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "添加子任务失败");
        }
    }

    public void addDataQualitySubTaskObject(String taskId, String subTaskId, Timestamp currentTime, List<String> objectIdList) throws AtlasBaseException {
        try {
            for (int i = 0, size = objectIdList.size(); i < size; i++) {
                String objectId = objectIdList.get(i);
                DataQualitySubTaskObject dataQualitySubTaskObject = new DataQualitySubTaskObject();
                //id
                String guid = UUID.randomUUID().toString();
                dataQualitySubTaskObject.setId(guid);
                //taskId
                dataQualitySubTaskObject.setTaskId(taskId);
                //子任务Id
                dataQualitySubTaskObject.setSubTaskId(subTaskId);
                //对象Id
                dataQualitySubTaskObject.setObjectId(objectId);
                //序号
                dataQualitySubTaskObject.setSequence(i + 1);
                //创建时间
                dataQualitySubTaskObject.setCreateTime(currentTime);
                //更新时间
                dataQualitySubTaskObject.setUpdateTime(currentTime);
                //是否已删除
                dataQualitySubTaskObject.setDelete(false);
                taskManageDAO.addDataQualitySubTaskObject(dataQualitySubTaskObject);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "添加子任务对象失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void addDataQualitySubTaskRule(String subTaskId, Timestamp currentTime, List<TaskInfo.SubTaskRule> subTaskRuleList,String tenantId) throws AtlasBaseException {
        try {
            for (int i=0,size=subTaskRuleList.size(); i<size; i++) {
                TaskInfo.SubTaskRule rule = subTaskRuleList.get(i);
                DataQualitySubTaskRule subTaskRule = new DataQualitySubTaskRule();
                //id
                String guid = UUID.randomUUID().toString();
                subTaskRule.setId(guid);
                //子任务Id
                subTaskRule.setSubTaskId(subTaskId);
                //规则Id
                subTaskRule.setRuleId(rule.getRuleId());
                //校验类型
                subTaskRule.setCheckType(rule.getCheckType());
                //校验表达式
                subTaskRule.setCheckExpression(rule.getCheckExpression());
                //校验阈值
                subTaskRule.setCheckThresholdMinValue(rule.getCheckThresholdMinValue());
                subTaskRule.setCheckThresholdMaxValue(rule.getCheckThresholdMaxValue());
                //order
                subTaskRule.setSequence(i+1);

                List<TaskInfo.Warning> warningList = rule.getWarnings();
                if (Objects.nonNull(warningList)) {
                    for (TaskInfo.Warning warning : warningList) {
                        Integer warningType = warning.getWarningType();
                        if (1 == warningType) {
                            //橙色校验类型
                            Integer warningCheckType = warning.getWarningCheckType();
                            subTaskRule.setOrangeCheckType(warningCheckType);
                            //橙色校验表达式
                            Integer warningCheckExpression = warning.getWarningCheckExpression();
                            subTaskRule.setOrangeCheckExpression(warningCheckExpression);
                            //橙色阈值
                            subTaskRule.setOrangeThresholdMinValue(warning.getWarningCheckThresholdMinValue());
                            subTaskRule.setOrangeThresholdMaxValue(warning.getWarningCheckThresholdMaxValue());
                        } else if (2 == warningType) {
                            //红色校验类型
                            Integer warningCheckType = warning.getWarningCheckType();
                            subTaskRule.setRedCheckType(warningCheckType);
                            //红色校验表达式
                            Integer warningCheckExpression = warning.getWarningCheckExpression();
                            subTaskRule.setRedCheckExpression(warningCheckExpression);
                            //红色阈值
                            subTaskRule.setRedThresholdMinValue(warning.getWarningCheckThresholdMinValue());
                            subTaskRule.setRedThresholdMaxValue(warning.getWarningCheckThresholdMaxValue());
                        }
                    }
                }
                //创建时间
                subTaskRule.setCreateTime(currentTime);
                //更新时间
                subTaskRule.setUpdateTime(currentTime);
                //是否已删除
                subTaskRule.setDelete(false);
                String unit = taskManageDAO.getTaskRuleUnit(rule.getRuleId(),tenantId);
                subTaskRule.setCheckThresholdUnit(unit);
                taskManageDAO.addDataQualitySubTaskRule(subTaskRule);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e, "添加子任务规则失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void updateTask(DataQualityTask taskInfo) throws AtlasBaseException {
        Timestamp currentTime = DateUtils.currentTimestamp();
        try {
            String userId = AdminUtils.getUserData().getUserId();
            taskInfo.setUpdateTime(currentTime);
            taskInfo.setUpdater(userId);
            taskManageDAO.updateTaskInfo(taskInfo);
            taskManageDAO.deleteWarningGroupUsedByTaskId(taskInfo.getId());
            if (taskInfo.getContentWarningNotificationIdList()!=null&&taskInfo.getContentWarningNotificationIdList().size()!=0){
                taskManageDAO.addTaskWarningGroup(taskInfo.getId(), WarningType.WARNING.code, taskInfo.getContentWarningNotificationIdList());
            }
            if (taskInfo.getExecutionWarningNotificationIdList()!=null&&taskInfo.getExecutionWarningNotificationIdList().size()!=0){
                taskManageDAO.addTaskWarningGroup(taskInfo.getId(), WarningType.ERROR.code, taskInfo.getExecutionWarningNotificationIdList());
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新任务失败");
        }

    }

    public EditionTaskInfo getTaskInfo(String taskId,String tenantId) throws AtlasBaseException {
        try {
            EditionTaskInfo info = taskManageDAO.getTaskInfo(taskId);
            List<EditionTaskInfo.SubTask> subTaskList = taskManageDAO.getSubTaskInfo(taskId);
            for (EditionTaskInfo.SubTask subTask : subTaskList) {
                String subTaskId = subTask.getSubTaskId();
                List<EditionTaskInfo.ObjectInfo> objectInfoList = taskManageDAO.getSubTaskRelatedObject(subTaskId);
                List<EditionTaskInfo.SubTaskRule> subTaskRuleList = taskManageDAO.getSubTaskRule(subTaskId,tenantId);
                String sparkConfig = taskManageDAO.geSparkConfig(subTaskId);
                if (sparkConfig!=null&&sparkConfig.length()!=0){
                    Map<String,Integer> configMap = GsonUtils.getInstance().fromJson(sparkConfig, new TypeToken<Map<String, Integer>>() {
                    }.getType());
                    subTask.setConfig(configMap);
                }
                subTask.setSubTaskRuleList(subTaskRuleList);

                //获取规则类型
                EditionTaskInfo.SubTaskRule subTaskRule = subTask.getSubTaskRuleList().get(0);
                Rule rule = ruleService.getById(subTaskRule.getRuleId(), tenantId);
                subTask.setScope(rule.getScope());
                subTask.setType(rule.getType());
                int dataSourceType = rule.getScope();
                for (EditionTaskInfo.ObjectInfo objectInfo : objectInfoList) {
                    String objectId = objectInfo.getObjectId().toString();
                    if (0 == dataSourceType) {
                        //添加数据源名字
                        CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                        String dataSourceId = paramInfo.getDataSourceId();
                        String dataSourceName = getDataSourceName(dataSourceId);
                        paramInfo.setDataSourceName(dataSourceName);
                        objectInfo.setObjectId(paramInfo);
                    } else if (1 == dataSourceType) {
                        //添加数据源名字
                        CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                        String dataSourceId = paramInfo.getDataSourceId();
                        String dataSourceName = getDataSourceName(dataSourceId);
                        paramInfo.setDataSourceName(dataSourceName);
                        objectInfo.setObjectId(paramInfo);
                    } else if (2 == dataSourceType) {
                        TaskType taskType = TaskType.getTaskByCode(rule.getType());
                        if (TaskType.CONSISTENCY.equals(taskType)) {
                            List<ConsistencyParam> lists = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                            }.getType());
                            lists.forEach(param->param.setDataSourceName(getDataSourceName(param.getDataSourceId())));
                            objectInfo.setObjectId(lists);
                        }else if (TaskType.CUSTOMIZE.equals(taskType)) {
                            List<CustomizeParam> lists = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                            }.getType());
                            lists.forEach(param->param.setDataSourceName(getDataSourceName(param.getDataSourceId())));
                            objectInfo.setObjectId(lists);
                        }else{
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                        }
                    }else{
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "错误的任务类型");
                    }
                }
                subTask.setObjectIdList(objectInfoList);

            }
            info.setTaskList(subTaskList);
            List<EditionTaskInfo.WarningGroup> contentWarningGroup = taskManageDAO.getWarningGroup(taskId, 0);
            List<EditionTaskInfo.WarningGroup> errorWarningGroup = taskManageDAO.getWarningGroup(taskId, 1);
            info.setContentWarningNotificationIdList(contentWarningGroup);
            info.setExecutionWarningNotificationIdList(errorWarningGroup);


            return info;
        } catch (Exception e) {
            Log.error(e.getMessage(),e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务信息失败");
        }
    }

    public String getDataSourceName(String dataSourceId){
        if (dataSourceId==null||dataSourceId.length()==0||QuartzJob.hiveId.equals(dataSourceId)){
            return QuartzJob.hiveId;
        }else{
            DataSourceInfo dataSourceInfo = dataSourceDAO.getDataSourceInfo(dataSourceId);
            if (dataSourceInfo==null){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源不存在或已删除，请修改任务");
            }
            return dataSourceInfo.getSourceName();
        }
    }

    public DataQualityBasicInfo getTaskBasicInfo(String guid) throws AtlasBaseException {
        try {
            DataQualityBasicInfo basicInfo = taskManageDAO.getTaskBasicInfo(guid);
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(guid);
            if(Objects.nonNull(qrtzName)) {
                String triggerName = TRIGGER_NAME + qrtzName;
                String triggerGroupName = TRIGGER_GROUP_NAME + qrtzName;

                Date lastExecuteTime = quartzManager.getJobLastExecuteTime(triggerName, triggerGroupName);
                Date nextExecuteTime = quartzManager.getJobNextExecuteTime(triggerName, triggerGroupName);
                if(Objects.nonNull(lastExecuteTime)) {
                    basicInfo.setLastExecuteTime(new Timestamp(lastExecuteTime.getTime()));
                }
                if(Objects.nonNull(nextExecuteTime)) {
                    basicInfo.setNextExecuteTime(new Timestamp(nextExecuteTime.getTime()));
                }
            }
            return basicInfo;
        } catch (Exception e) {
            LOG.error("获取任务信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务信息失败");
        }

    }


    public void startTask(String taskId) throws AtlasBaseException {
        try {
            String qrtzName = taskManageDAO.getQrtzJobByTaskId(taskId);
            if (Objects.isNull(qrtzName) || qrtzName.trim().length() == 0) {
                addQuartzJob(taskId);
            } else {
                String jobGroupName = JOB_GROUP_NAME + qrtzName;
                String triggerName = TRIGGER_NAME + qrtzName;
                String triggerGroupName = TRIGGER_GROUP_NAME + qrtzName;
                DataQualityTask task = taskManageDAO.getQrtzInfoByTemplateId(taskId);
                String cron = task.getCronExpression();
                quartzManager.modifyJobTime(qrtzName,jobGroupName,triggerName,triggerGroupName,cron);  //任务重新开启后检查cron是否有变化，如果有需要重新刷新trigger
                quartzManager.resumeJob(qrtzName, jobGroupName);
            }
            //设置任务状态为【启用】
            taskManageDAO.updateTaskEnableStatus(taskId, true);
        } catch (Exception e) {
            LOG.error("开启任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "开启任务失败");
        }
    }

    public void startTaskNow(String taskId) throws AtlasBaseException {
        try {
            String jobName = taskManageDAO.getJobName(taskId);
            String jobGroupName = JOB_GROUP_NAME + System.currentTimeMillis();
            quartzManager.addSimpleJob(jobName, jobGroupName, QuartzJob.class);
        } catch (Exception e) {
            LOG.error("开启任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "开启任务失败");
        }
    }

    public void stopTask(String taskId) throws AtlasBaseException {
        try {
            String jobName = taskManageDAO.getQrtzJobByTaskId(taskId);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            quartzManager.pauseJob(jobName, jobGroupName);
            //设置模板状态为【暂停】
            taskManageDAO.updateTaskEnableStatus(taskId, false);
        } catch (Exception e) {
            LOG.error("关闭模板失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "关闭模板失败");
        }
    }



    public void addQuartzJob(String taskId) throws AtlasBaseException {
        try {
            long currentTime = System.currentTimeMillis();
            String jobName = String.valueOf(currentTime);
            String jobGroupName = JOB_GROUP_NAME + jobName;
            String triggerName = TRIGGER_NAME + jobName;
            String triggerGroupName = TRIGGER_GROUP_NAME + jobName;
            DataQualityTask task = taskManageDAO.getQrtzInfoByTemplateId(taskId);
            String cron = task.getCronExpression();
            Timestamp startTime = task.getStartTime();
            Timestamp endTime = task.getEndTime();
            Integer level = task.getLevel();
            if(Objects.nonNull(cron)) {
                quartzManager.addCronJobWithTimeRange(jobName, jobGroupName, triggerName, triggerGroupName, QuartzJob.class, cron, level, startTime, endTime);
            } else {
                quartzManager.addSimpleJob(jobName, jobGroupName, QuartzJob.class);
            }
            //添加qrtzName
            taskManageDAO.updateTaskQrtzName(taskId, jobName);

        } catch (Exception e) {
            LOG.error("添加任务失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加任务失败");
        }
    }

    public PageResult<TaskRuleHeader> getRuleList(String taskId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            EditionTaskInfo taskInfo = taskManageDAO.getTaskInfo(taskId);
            List<TaskRuleHeader> lists = taskManageDAO.getRuleList(taskId, parameters,taskInfo.getTenantId());
            Map<String, String> ruleTemplateCategoryMap = new HashMap();
            RuleTemplateType.all().stream().forEach(ruleTemplateType -> {
                ruleTemplateCategoryMap.put(ruleTemplateType.getRuleType(), ruleTemplateType.getName());
            });
            for (TaskRuleHeader rule : lists) {
                String categoryId = taskManageDAO.getRuleTypeCodeByRuleId(rule.getRuleId(),taskInfo.getTenantId());
                String typeName = ruleTemplateCategoryMap.get(categoryId);
                rule.setTypeName(typeName);
            }
            long totalSize = taskManageDAO.countRuleList(taskId,taskInfo.getTenantId());
            pageResult.setLists(lists);
            pageResult.setCurrentSize(lists.size());
            pageResult.setTotalSize(totalSize);

            return pageResult;
        } catch (Exception e) {
            LOG.error("获取任务规则列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务规则列表失败");
        }
    }

    public TaskExecutionReport getTaskExecutionReport(String taskId) throws AtlasBaseException {
        try {
            TaskExecutionReport taskExecutionInfo = taskManageDAO.getTaskExecutionInfo(taskId);
            List<TaskExecutionReport.ExecutionRecord> executionRecordList = taskManageDAO.getTaskExecutionRecord(taskId);
            taskExecutionInfo.setExecutionRecordList(executionRecordList);
            return taskExecutionInfo;
        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public ExecutionReportData getTaskReportData(String taskId, String taskExecuteId,String subtaskId,String tenantId) throws AtlasBaseException {
        try {
            ExecutionReportData resultData = new ExecutionReportData();
            //basicInfo
            ExecutionReportData.TaskBasicInfo basicInfo =  taskManageDAO.getTaskExecutionBasicInfo(taskId, taskExecuteId);
            resultData.setBasicInfo(basicInfo);
            //checkStatus
            ExecutionReportData.TaskCheckResultCount checkResultCount = new ExecutionReportData.TaskCheckResultCount();
            //查询一个子任务或者All
            long tableRulePassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 0, 0,tenantId,subtaskId);
            long tableRuleNoPassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 0, 1,tenantId,subtaskId);
            long columnRulePassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 1, 0,tenantId,subtaskId);
            long columnRuleNoPassedNumber =  taskManageDAO.countTaskRuleExecution(taskId, taskExecuteId, 1, 1,tenantId,subtaskId);
            checkResultCount.setTableRulePassedNumber(tableRulePassedNumber);
            checkResultCount.setTableRuleNoPassedNumber(tableRuleNoPassedNumber);
            checkResultCount.setColumnRulePassedNumber(columnRulePassedNumber);
            checkResultCount.setColumnRuleNoPassedNumber(columnRuleNoPassedNumber);
            //error
            List<Integer> errorList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 0,subtaskId);
            if(null != errorList && errorList.size()>0) {
                long errorNumber = errorList.stream().filter(status -> status==2).count();
                checkResultCount.setErrorRuleNumber(errorNumber);
                checkResultCount.setTotalRuleNumber(errorList.size());
            } else {
                checkResultCount.setErrorRuleNumber(0);
                checkResultCount.setTotalRuleNumber(0);
            }
            //orangeWarning
            List<Integer> checkOrangeWarningStatusList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 1,subtaskId);
            if(null != checkOrangeWarningStatusList && checkOrangeWarningStatusList.size()>0) {
                long warningNumber = checkOrangeWarningStatusList.stream().filter(status -> status==1).count();
                checkResultCount.setOrangeWarningNumber(warningNumber);
                checkResultCount.setTotalOrangeWarningRuleNumber(checkOrangeWarningStatusList.size());
            } else {
                checkResultCount.setOrangeWarningNumber(0);
                checkResultCount.setTotalOrangeWarningRuleNumber(0);
            }
            //redWarning
            List<Integer> checkRedWarningStatusList = taskManageDAO.getWarningValueList(taskId, taskExecuteId, 2,subtaskId);
            if(null != checkRedWarningStatusList && checkRedWarningStatusList.size()>0) {
                long warningNumber = checkRedWarningStatusList.stream().filter(status -> status==1).count();
                checkResultCount.setRedWarningNumber(warningNumber);
                checkResultCount.setTotalRedWarningRuleNumber(checkRedWarningStatusList.size());
            } else {
                checkResultCount.setRedWarningNumber(0);
                checkResultCount.setTotalRedWarningRuleNumber(0);
            }
            resultData.setCheckResultCount(checkResultCount);

            //result && suggestion
            ExecutionReportData.ImprovingSuggestion suggestion = new ExecutionReportData.ImprovingSuggestion();
            List<String> tableRuleSuggestion = new ArrayList<>();
            List<String> columnRuleSuggestion = new ArrayList<>();
            String suffix = "的结果不符合预期，请及时处理";
            String noEnd = "未执行完毕，请稍候再次进行下载";
            List<SubTaskRecord> executeResult = getTaskRuleExecutionRecordList(taskExecuteId,subtaskId,tenantId);
            for (SubTaskRecord subTaskRecord : executeResult) {
                for (TaskRuleExecutionRecord record : subTaskRecord.getTaskRuleExecutionRecords()) {
                    if (0 == record.getScope() && record.getCheckStatus() == null) {
                        tableRuleSuggestion.add(record.getObjectName() + noEnd);
                    } else if (1 == record.getScope() && record.getCheckStatus() == null) {
                        columnRuleSuggestion.add(record.getObjectName() + noEnd);
                    } else if (0 == record.getScope() && 1 == record.getCheckStatus()) {
                        tableRuleSuggestion.add(record.getObjectName() + suffix);
                    } else if (1 == record.getScope() && 1 == record.getCheckStatus()) {
                        columnRuleSuggestion.add(record.getObjectName() + suffix);
                    }
                    Integer sequence = taskManageDAO.getSubTaskSequence(record.getSubtaskId());
                    record.setSubTaskSequence(sequence);
                }
            }
            resultData.setRuleCheckResult(executeResult);
            //suggestion
            suggestion.setTableQuestion(tableRuleSuggestion);
            suggestion.setColumnQuestion(columnRuleSuggestion);
            resultData.setSuggestion(suggestion);
            return resultData;
        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public List<SubTaskRecord> getTaskRuleExecutionRecordList(String executionId,String subtaskId,String tenantId) throws AtlasBaseException {
        try {
            Map<String, SubTaskRecord> map = new HashMap<>();
            List<TaskRuleExecutionRecord> list = taskManageDAO.getTaskRuleExecutionRecordList(executionId,subtaskId,tenantId);
            for (TaskRuleExecutionRecord record : list) {
                if (map.containsKey(record.getSubtaskId())){
                    map.get(record.getSubtaskId()).getTaskRuleExecutionRecords().add(record);
                }else{
                    SubTaskRecord subTaskRecord = new SubTaskRecord(record);
                    subTaskRecord.setSequence(taskManageDAO.getSubTaskSequence(record.getSubtaskId()));
                    subTaskRecord.getTaskRuleExecutionRecords().add(record);
                    map.put(record.getSubtaskId(),subTaskRecord);
                }
                String objectId = record.getObjectId();
                if(0 == record.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
                    record.setDataSourceName(dataSourceName);
                    record.setDbName(paramInfo.getSchema());
                    record.setTableName(paramInfo.getTable());
                    record.setObjectName(paramInfo.getTable());
                    record.setTableId(paramInfo.getTable());
                } else if(1 == record.getScope()) {
                    CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                    String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
                    record.setDataSourceName(dataSourceName);
                    record.setDbName(paramInfo.getSchema());
                    record.setTableName(paramInfo.getTable());
                    record.setObjectName(paramInfo.getColumn());
                    record.setTableId(paramInfo.getTable());
                }else if(2 == record.getScope()) {
                    TaskType taskType = TaskType.getTaskByCode(record.getTaskType());
                    if (TaskType.CONSISTENCY.equals(taskType)) {
                        List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                        }.getType());
                        List<TaskRuleExecutionRecord> records = params.stream().map(param -> {
                            String dataSourceName = getDataSourceName(param.getDataSourceId());
                            String tableId = param.getId();
                            TaskRuleExecutionRecord taskRuleExecutionRecord = new TaskRuleExecutionRecord(record);
                            taskRuleExecutionRecord.setDbName(param.getSchema());
                            taskRuleExecutionRecord.setTableName(param.getTable());
                            taskRuleExecutionRecord.setDataSourceName(dataSourceName);
                            taskRuleExecutionRecord.setTableId(tableId);
                            String fileName = LivyTaskSubmitHelper.getOutName(tableId);
                            String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(taskRuleExecutionRecord.getRuleExecutionId(), taskRuleExecutionRecord.getCreateTime().getTime(), fileName);
                            HdfsUtils hdfsUtils = new HdfsUtils();
                            int fileLine = 0;
                            try {
                                fileLine = hdfsUtils.getFileLine(hdfsOutPath);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            taskRuleExecutionRecord.setResult(Float.valueOf(fileLine));
                            return taskRuleExecutionRecord;
                        }).collect(Collectors.toList());
                        map.get(record.getSubtaskId()).setTaskRuleExecutionRecords(records);
                    }else if (TaskType.CUSTOMIZE.equals(taskType)) {
                        List<CustomizeParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                        }.getType());
                        List<TaskRuleExecutionRecord> records = params.stream().map(param -> {
                            String dataSourceName = getDataSourceName(param.getDataSourceId());
                            String tableId = param.getId();
                            TaskRuleExecutionRecord taskRuleExecutionRecord = new TaskRuleExecutionRecord(record);
                            taskRuleExecutionRecord.setDbName(param.getSchema());
                            taskRuleExecutionRecord.setTableName(param.getTable());
                            taskRuleExecutionRecord.setDataSourceName(dataSourceName);
                            taskRuleExecutionRecord.setTableId(tableId);
                            return taskRuleExecutionRecord;
                        }).collect(Collectors.toList());
                        map.get(record.getSubtaskId()).setTaskRuleExecutionRecords(records);
                    }
                }

                Boolean filing = taskManageDAO.getFilingStatus(executionId)==0?false:true;
                record.setFiling(filing);
            }

            return new ArrayList<>(map.values());
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("获取报告规则详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告规则详情失败");
        }
    }

    public PageResult<ExecutionLogHeader> getExecutionLogList(String taskId, Parameters parameters) throws AtlasBaseException {
        try {
            PageResult pageResult = new PageResult();
            List<ExecutionLogHeader> lists = taskManageDAO.getExecutionLogList(taskId, parameters);
            Long count = taskManageDAO.countExecutionLogList(taskId, parameters);
            pageResult.setLists(lists);
            pageResult.setCurrentSize(lists.size());
            pageResult.setTotalSize(count);
            return pageResult;

        } catch (Exception e) {
            LOG.error("获取任务列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取任务列表失败");
        }
    }

    public ExecutionLog getExecutionLogList(String ruleExecutionId) throws AtlasBaseException {
        try {
            ExecutionLog logInfo = taskManageDAO.getExecutionInfo(ruleExecutionId);
            List<String> errorList = taskManageDAO.getRuleExecutionLog(ruleExecutionId);
            logInfo.setMsg(errorList);
            return logInfo;

        } catch (Exception e) {
            LOG.error("获取报告详情失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取报告详情失败");
        }
    }

    public List<Queue> getPools(String tenantId) throws AtlasBaseException {
        engine = AtlasConfiguration.METASPACE_QUALITY_ENGINE.get(conf,String::valueOf);
        Pool pools = tenantService.getPools(tenantId);
        if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
            return pools.getImpala();
        } else {
            return pools.getHive();
        }
    }

    public PageResult getDataSourceList(Parameters parameters,String tenantId) throws AtlasBaseException {
        PageResult dataSourceList = dataShareService.getDataSourceList(parameters, null, tenantId);
        DataSourceHead hive = new DataSourceHead();
        if (parameters.getOffset()==0){
            hive.setSourceType("hive");
            hive.setSourceName("hive");
            hive.setSourceId("hive");
            dataSourceList.getLists().add(hive);
        }
        return dataSourceList;
    }

    public List<RuleHeader> searchRules(Parameters parameters, String scope,String tenantId) throws AtlasBaseException {
        Integer objType = null;
        if (!"all".equals(scope)){
            objType=Integer.valueOf(scope);
        }
        String query = parameters.getQuery();
        if (query!=null){
            parameters.setQuery(query.replaceAll("%", "/%").replaceAll("_", "/_"));
        }
        List<RuleHeader> ruleList = taskManageDAO.searchRuleList(parameters, objType,tenantId);
        return ruleList;
    }

    public List<String> schedulePreview(Schedule schedule) throws AtlasBaseException {
        return quartzManager.schedulePreview(schedule);
    }

    public boolean checkPreview(Schedule schedule) throws AtlasBaseException {
        return quartzManager.checkSchedule(schedule);
    }

    public ErrorData getErrorData(Parameters parameters,String ruleExecutionId,String tableId,String tenantId) throws IOException {
        ErrorData errorData = new ErrorData();
        TaskRuleExecutionRecord record = taskManageDAO.getTaskRuleExecutionRecord(ruleExecutionId,tenantId);
        if(record==null){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"执行结果不存在");
        }
        String objectId = record.getObjectId();
        HdfsUtils hdfsUtils = new HdfsUtils();
        if(0 == record.getScope()) {
            CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
            String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
            errorData.setDataSourceName(dataSourceName);
            errorData.setDbName(paramInfo.getSchema());
            errorData.setTableName(paramInfo.getTable());
            errorData.setDataSourceId(paramInfo.getDataSourceId());
        } else if(1 == record.getScope()) {
            CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
            String dataSourceName = getDataSourceName(paramInfo.getDataSourceId());
            errorData.setDataSourceName(dataSourceName);
            errorData.setDbName(paramInfo.getSchema());
            errorData.setTableName(paramInfo.getTable());
            errorData.setDataSourceId(paramInfo.getDataSourceId());
            errorData.setColumnName(paramInfo.getColumn());
        }else if(2 == record.getScope()) {
            TaskType taskType = TaskType.getTaskByCode(record.getTaskType());
            if (TaskType.CONSISTENCY.equals(taskType)) {
                List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                }.getType());
                params.forEach(param->{
                    if (param.getId().equals(tableId)){
                        String dataSourceName = getDataSourceName(param.getDataSourceId());
                        errorData.setDataSourceName(dataSourceName);
                        errorData.setDbName(param.getSchema());
                        errorData.setTableName(param.getTable());
                        errorData.setDataSourceId(param.getDataSourceId());
                    }
                });
                String fileName = LivyTaskSubmitHelper.getOutName(tableId);
                String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(ruleExecutionId, record.getCreateTime().getTime(), fileName);
                List<String> list  = errorDataCache.getIfPresent(hdfsOutPath);
                if (list==null){
                    list = hdfsUtils.exists(hdfsOutPath)? hdfsUtils.catFile(hdfsOutPath, errorDataSize) : new ArrayList<>(); //无输出的规则或者执行异常的任务HDFS上是不会有输出结果的，返回空数据
                }
                errorDataCache.put(hdfsOutPath,list);
                getDataByList(list,parameters,errorData);
                return errorData;
            }
        }
        String fileName = LivyTaskSubmitHelper.getOutName("data");
        String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(ruleExecutionId, record.getCreateTime().getTime(), fileName);
        List<String> list  = errorDataCache.getIfPresent(hdfsOutPath);
        if (list==null){
            list = hdfsUtils.exists(hdfsOutPath)? hdfsUtils.catFile(hdfsOutPath, errorDataSize) : new ArrayList<>(); //无输出的规则或者执行异常的任务HDFS上是不会有输出结果的，返回空数据
        }
        errorDataCache.put(hdfsOutPath,list);
        getDataByList(list,parameters,errorData);
        return errorData;
    }


    public File exportExcelErrorData(String executionId,String subTaskId,String tenantId) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        List<TaskRuleExecutionRecord> records = taskManageDAO.getTaskRuleExecutionRecords(executionId,subTaskId,tenantId);
        Integer subTaskSequence = taskManageDAO.getSubTaskSequence(subTaskId);
        File tmpFile = new File("/tmp/metaspace/"+executionId,"子任务"+subTaskSequence+".xlsx");
        if (tmpFile.exists()){
            return tmpFile;
        }

        for (TaskRuleExecutionRecord record:records){
            String sheetName;
            TaskType taskType = TaskType.getTaskByCode(record.getTaskType());
            List<String> list;
            String objectId = record.getObjectId();
            HdfsUtils hdfsUtils = new HdfsUtils();
            if (record.getScope()!=2) {
                String fileName = LivyTaskSubmitHelper.getOutName("data");
                String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(record.getRuleExecutionId(), record.getCreateTime().getTime(), fileName);
                list = hdfsUtils.catFile(hdfsOutPath, -1);
                if (list==null||list.size()==0){
                    continue;
                }
                CustomizeParam paramInfo = GsonUtils.getInstance().fromJson(objectId, CustomizeParam.class);
                sheetName = paramInfo.getTable();

            } else if(!TaskType.CONSISTENCY.equals(taskType)){

                String fileName = LivyTaskSubmitHelper.getOutName("data");
                String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(record.getRuleExecutionId(), record.getCreateTime().getTime(), fileName);
                list = hdfsUtils.catFile(hdfsOutPath, -1);
                if (list==null||list.size()==0){
                    continue;
                }
                List<CustomizeParam> paramInfo = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<CustomizeParam>>() {
                }.getType());
                sheetName = paramInfo.get(0).getTable();
            } else{
                List<ConsistencyParam> params = GsonUtils.getInstance().fromJson(objectId, new TypeToken<List<ConsistencyParam>>() {
                }.getType());
                params.forEach( param->{
                    String tableId = param.getId();
                    String fileName = LivyTaskSubmitHelper.getOutName(tableId);
                    String hdfsOutPath = LivyTaskSubmitHelper.getHdfsOutPath(record.getRuleExecutionId(), record.getCreateTime().getTime(), fileName);
                    try {
                        List<String> lineList = hdfsUtils.catFile(hdfsOutPath, -1);
                        List<Map<String, Object>> data = lineList.stream().map(line -> {
                            Map<String, Object> map = GsonUtils.getInstance().fromJson(line, new TypeToken<Map<String, Object>>() {
                            }.getType());
                            return map;
                        }).collect(Collectors.toList());
                        List<List<String>> values = data.stream().map(map -> {
                            List<Object> arrayList = new ArrayList();
                            arrayList.addAll(map.values());
                            List<String> valueList = arrayList.stream().map(object -> object.toString()).collect(Collectors.toList());
                            return valueList;
                        }).collect(Collectors.toList());

                        PoiExcelUtils.createSheet(workbook,param.getTable(),new ArrayList<>(data.get(0).keySet()),values);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                break;
            }

            List<Map<String, Object>> data = list.stream().map(line -> {
                Map<String, Object> map = GsonUtils.getInstance().fromJson(line, new TypeToken<Map<String, Object>>() {
                }.getType());
                return map;
            }).collect(Collectors.toList());
            List<List<String>> values = data.stream().map(map -> {
                List<Object> arrayList = new ArrayList();
                arrayList.addAll(map.values());
                List<String> valueList = arrayList.stream().map(object -> object.toString()).collect(Collectors.toList());
                return valueList;
            }).collect(Collectors.toList());

            PoiExcelUtils.createSheet(workbook,sheetName,new ArrayList<>(data.get(0).keySet()),values);

        }
        return workbook2file(workbook,"子任务"+subTaskSequence,executionId);
    }

    private File workbook2file(Workbook workbook,String name,String id) throws IOException {
        File tmpFile = new File("/tmp/metaspace",id);
        tmpFile.mkdirs();
        File dataFile = new File(tmpFile,name+".xlsx");
        try (FileOutputStream output = new FileOutputStream(dataFile)) {
            workbook.write(output);
            output.flush();
            output.close();
        }
        return dataFile;
    }

    public ErrorData getDataByList(List<String> list,Parameters parameters,ErrorData errorData){
        if (parameters.getOffset()>errorDataSize){
            errorData.setTotalSize(list.size());
            errorData.setCurrentSize(0);
            errorData.setData(new ArrayList<>());
            return errorData;
        }
        int limit = Math.min(parameters.getLimit(),errorDataSize-parameters.getOffset());
        if (limit == -1){
            limit=errorDataSize-parameters.getOffset();
        }
        List<Map<String, Object>> data = list.stream().skip(parameters.getOffset()).limit(limit).map(line -> {
            Map<String, Object> map = GsonUtils.getInstance().fromJson(line, new TypeToken<Map<String, Object>>() {
            }.getType());
            return map;
        }).collect(Collectors.toList());
        errorData.setTotalSize(list.size());
        errorData.setCurrentSize(data.size());
        errorData.setData(data);
        return errorData;
    }

    public File downTaskReportData(String executionId, String subTaskId, String tenantId, InputStream fileInputStream,String fileName) throws Exception {
        File tmpFile = new File("/tmp/metaspace",executionId);
        tmpFile.mkdirs();
        File dataFile = new File(tmpFile,fileName);
        FileUtils.copyInputStreamToFile(fileInputStream, dataFile);
        String subTaskName;
        if ("all".equals(subTaskId)){
            List<String> subTaskIds = taskManageDAO.getSubTaskId(executionId);
            for (String subId:subTaskIds){
                exportExcelErrorData(executionId,subId,tenantId);
            }
            subTaskName="all";
        }else{
            exportExcelErrorData(executionId,subTaskId,tenantId);
            Integer subTaskSequence = taskManageDAO.getSubTaskSequence(subTaskId);
            subTaskName="子任务"+subTaskSequence+".xlsx";
        }
        File zipFile = new File("/tmp/metaspace",executionId+subTaskId);
        zipFile.mkdirs();
        File file = new File(zipFile, "任务执行结果.zip");
        FileOutputStream fos1 = new FileOutputStream(file);
        toZip(tmpFile.getPath(),fos1,subTaskName,fileName);
        return file;
    }



    /**

     * 递归压缩方法
     * @param sourceFile 源文件
     * @param zos        zip输出流
     * @param subTaskName       压缩后的名称
     *                          false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */

    private void compress(File sourceFile, ZipOutputStream zos,
                                 String subTaskName,String fileName) throws Exception{
        byte[] buf = new byte[100*1024];
        File[] listFiles = sourceFile.listFiles();
        for (File file : listFiles) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            if (!"all".equals(subTaskName)&&!file.getName().contains(subTaskName)&&!file.getName().contains(fileName)){
                continue;
            }
            zos.putNextEntry(new ZipEntry(file.getName()));
            // copy文件到zip输出流中
            int len;
            FileInputStream in = new FileInputStream(file);
            while ((len = in.read(buf)) != -1){
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        }
    }

    public void toZip(String srcDir, OutputStream out, String subTaskName,String fileName)
            throws RuntimeException{
        ZipOutputStream zos = null ;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile,zos,subTaskName,fileName);
            long end = System.currentTimeMillis();
        } catch (Exception e) {
            throw new RuntimeException("zip error ",e);
        }finally{
            if(zos != null){
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
