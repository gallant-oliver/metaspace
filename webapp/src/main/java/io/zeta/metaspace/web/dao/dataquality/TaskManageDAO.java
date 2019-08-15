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
 * @date 2019/7/24 9:53
 */
package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.AtomicTaskExecution;
import io.zeta.metaspace.model.dataquality2.DataQualityBasicInfo;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskObject;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTask;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskExecute;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskRuleExecute;
import io.zeta.metaspace.model.dataquality2.EditionTaskInfo;
import io.zeta.metaspace.model.dataquality2.ExecutionLog;
import io.zeta.metaspace.model.dataquality2.ExecutionLogHeader;
import io.zeta.metaspace.model.dataquality2.RuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskExecutionReport;
import io.zeta.metaspace.model.dataquality2.TaskHeader;
import io.zeta.metaspace.model.dataquality2.TaskInfo;
import io.zeta.metaspace.model.dataquality2.TaskRuleExecutionRecord;
import io.zeta.metaspace.model.dataquality2.TaskRuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 9:53
 */
public interface TaskManageDAO {


    /**
     * 获取任务列表
     * @param my
     * @param creator
     * @param params
     * @return
     */
    @Select({"<script>",
             " select data_quality_task.id,data_quality_task.enable,'TID-'||number as taskId,name as taskName,description,current_execution_status as executeStatus,",
             " current_execution_percent as percent,orange_warning_total_count as orangeWarningTotalCount,red_warning_total_count as redWarningTotalCount,",
             " error_total_count as ruleErrorTotalCount,start_time as startTime,end_time as endTime,level as taskLevel,users.username as creator",
             " from data_quality_task join users on users.userid=data_quality_task.creator",
             " where ",
             " (data_quality_task.name like '%${params.query}%' ESCAPE '/' or 'TID-'||data_quality_task.number like '%${params.query}%' ESCAPE '/')",
             " <if test='my==0'>",
             " and data_quality_task.creator=#{creator}",
             " </if>",
             " order by data_quality_task.number",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<TaskHeader> getTaskList(@Param("my") Integer my, @Param("creator") String creator, @Param("params") Parameters params);

    /**
     * 任务总数
     * @param my
     * @param creator
     * @param params
     * @return
     */
    @Select({"<script>",
             " select count(*)",
             " from data_quality_task",
             " where ",
             " (name like '%${params.query}%' ESCAPE '/' or 'TID-'||number like '%${params.query}%' ESCAPE '/')",
             " <if test='my==0'>",
             " and creator=#{creator}",
             " </if>",
             " </script>"})
    public long countTaskList(@Param("my") Integer my, @Param("creator") String creator, @Param("params") Parameters params);

    /**
     * 删除任务
     * @param taskIdList
     * @return
     */
    @Update({" <script>",
             " update data_quality_task set delete=true where id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " </script>"})
    public int deleteTaskList(@Param("taskIdList")List<String> taskIdList);

    /**
     * 删除子任务
     * @param taskIdList
     * @return
     */
    @Update({" <script>",
             " update data_quality_sub_task set delete=true where task_id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " </script>"})
    public int deleteSubTaskList(@Param("taskIdList")List<String> taskIdList);

    /**
     * 删除子任务关联对象
     * @param taskIdList
     * @return
     */
    @Update({" <script>",
             " update data_quality_sub_task_object set delete=true where subtask_id in",
             " (select id from data_quality_sub_task where task_id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " )",
            " </script>"})
    public int deleteSubTaskObjectList(@Param("taskIdList")List<String> taskIdList);

    /**
     * 删除子任务规则
     * @param taskIdList
     * @return
     */
    @Update({" <script>",
             " update data_quality_sub_task_rule set delete=true where subtask_id in",
             " (select id from data_quality_sub_task where task_id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " )",
             " </script>"})
    public int deleteSubTaskRuleList(@Param("taskIdList")List<String> taskIdList);

    /**
     * 获取规则分组下规则列表
     * @param categoryId
     * @return
     */
    @Select({"<script>",
             " select data_quality_rule.id, data_quality_rule.name, data_quality_rule.rule_template_id as ruleTemplateId, data_quality_rule_template.scope",
             " from data_quality_rule, data_quality_rule_template",
             " where data_quality_rule.category_id=#{categoryId}",
             " and data_quality_rule.rule_template_id=data_quality_rule_template.id",
             " and enable=true",
             " </script>"})
    public List<RuleHeader> getRuleListByCategoryId(@Param("categoryId")String categoryId);

    /**
     * 获取字段列表
     * @param columnIdList
     * @return
     */
    @Select({" <script>",
             " select DISTINCT type from column_info where column_guid in ",
             " <foreach item='columnId' index='index' collection='columnIdList' separator=',' open='(' close=')'>" ,
             " #{columnId}",
             " </foreach>",
             " </script>"})
    public List<String> getColumnTypeList(@Param("columnIdList")List<String> columnIdList);

    /**
     * 获取数值类型的规则
     * @return
     */
    @Select("select id from data_quality_rule_template where category_id='5'")
    public List<String> getNumericTypeTemplateRuleId();

    /**
     * 根据分组信息获取告警组
     * @param categoryId
     * @return
     */
    @Select("select id,name from warning_group where category_id=#{categoryId}")
    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroupList(@Param("categoryId")String categoryId);

    @Select("select id,name from warning_group")
    public List<TaskWarningHeader.WarningGroupHeader> getAllWarningGroup();

    /**
     * 添加数据质量任务
     * @param task
     * @return
     */
    @Insert({" insert into data_quality_task(id,name,level,description,cron_expression,enable,start_time,end_time,create_time,update_time,creator,updater,delete,orange_warning_total_count,red_warning_total_count,error_total_count,execution_count) ",
            " values(#{task.id},#{task.name},#{task.level},#{task.description},#{task.cronExpression},#{task.enable},#{task.startTime},#{task.endTime},#{task.createTime},#{task.updateTime},#{task.creator},#{task.updater},#{task.delete},#{task.orangeWarningTotalCount},#{task.redWarningTotalCount},#{task.errorTotalCount},#{task.executionCount})"})
    public int addDataQualityTask(@Param("task")DataQualityTask task);

    @Insert({" <script>",
             " insert into data_quality_task2warning_group(task_id, warning_group_id,warning_type)values",
             " <foreach collection='list' item='warningGroupId' index='index'  separator=','>",
             " (#{taskRuleId},#{warningGroupId},#{warningType})",
             " </foreach>",
             " </script>"})
    public int addTaskWarningGroup(@Param("taskRuleId")String taskId, @Param("warningType")Integer warningType, @Param("list")List<String> list);

    /**
     * 添加数据质量子任务
     * @param subTask
     * @return
     */
    @Insert({" insert into data_quality_sub_task(id,task_id,datasource_type,sequence,create_time,update_time,delete) ",
             " values(#{subTask.id},#{subTask.taskId},#{subTask.dataSourceType},#{subTask.sequence},#{subTask.createTime},#{subTask.updateTime},#{subTask.delete})"})
    public int addDataQualitySubTask(@Param("subTask")DataQualitySubTask subTask);

    /**
     * 添加数据质量子任务关联对象
     * @param taskObject
     * @return
     */
    @Insert({" insert into data_quality_sub_task_object(id,task_id,subtask_id,object_id,sequence,create_time,update_time,delete) ",
             " values(#{taskObject.id},#{taskObject.taskId},#{taskObject.subTaskId},#{taskObject.objectId},#{taskObject.sequence},#{taskObject.createTime},#{taskObject.updateTime},#{taskObject.delete})"})
    public int addDataQualitySubTaskObject(@Param("taskObject")DataQualitySubTaskObject taskObject);

    /**
     * 添加数据质量子任务下子规则
     * @param taskRule
     * @return
     */
    @Insert({" insert into data_quality_sub_task_rule(id,subtask_id,ruleId,check_type,check_expression_type,check_threshold_min_value,check_threshold_max_value,orange_check_type,orange_check_expression_type,orange_threshold_min_value,orange_threshold_max_value,red_check_type,red_check_expression_type,red_threshold_min_value,red_threshold_max_value,sequence,create_time,update_time,delete) ",
             " values(#{rule.id},#{rule.subTaskId},#{rule.ruleId},#{rule.checkType},#{rule.checkExpression},#{rule.checkThresholdMinValue},#{rule.checkThresholdMaxValue},#{rule.orangeCheckType},#{rule.orangeCheckExpression},#{rule.orangeThresholdMinValue},#{rule.orangeThresholdMaxValue},#{rule.redCheckType},#{rule.redCheckExpression},#{rule.redThresholdMinValue},#{rule.redThresholdMaxValue},#{rule.sequence},#{rule.createTime},#{rule.updateTime},#{rule.delete})"})
    public int addDataQualitySubTaskRule(@Param("rule")DataQualitySubTaskRule taskRule);

    @Select("select id,name as taskName,level,description,'TID-'||number as taskID, start_time as startTime,end_time as endTime,cron_expression as cronExpression from data_quality_task where id=#{taskId}")
    public EditionTaskInfo getTaskInfo(@Param("taskId")String taskId);

    @Select("select id as subTaskId,datasource_type as dataSourceType,sequence from data_quality_sub_task where task_id=#{taskId}")
    public List<EditionTaskInfo.SubTask> getSubTaskInfo(@Param("taskId")String taskId);

    @Select("select object_id as objectId,sequence from data_quality_sub_task_object where subtask_id=#{subTaskId}")
    public List<EditionTaskInfo.ObjectInfo> getSubTaskRelatedObject(@Param("subTaskId")String subTaskId);

    @Select({"select data_quality_rule.name as ruleName,data_quality_rule.id as ruleId,category_id as categoryId,",
             " data_quality_sub_task_rule.id as subTaskRuleId,data_quality_sub_task_rule.check_type as checkType,data_quality_sub_task_rule.check_expression_type as checkExpression,",
             " data_quality_sub_task_rule.check_threshold_min_value as checkThresholdMinValue,data_quality_sub_task_rule.check_threshold_max_value as checkThresholdMaxValue,",
             " orange_check_type as orangeWarningCheckType,orange_check_expression_type as orangeWarningCheckExpression,orange_threshold_min_value as orangeWarningCheckThresholdMinValue,",
             " orange_threshold_max_value as orangeWarningCheckThresholdMaxValue,red_check_type as redWarningCheckType,red_check_expression_type as redWarningCheckExpression,",
             " red_threshold_min_value as redWarningCheckThresholdMinValue,red_threshold_max_value as redWarningCheckThresholdMaxValue",
             " from data_quality_sub_task_rule join data_quality_rule on data_quality_rule.id=data_quality_sub_task_rule.ruleId where data_quality_sub_task_rule.subtask_id=#{subTaskId}",
    })
    public List<EditionTaskInfo.SubTaskRule> getSubTaskRule(@Param("subTaskId")String subTaskId);

    @Select("select warning_group.id as warningGroupId,warning_group.name as warningGroupName from data_quality_task2warning_group join warning_group on warning_group.id=data_quality_task2warning_group.warning_group_id where task_id=#{taskId} and data_quality_task2warning_group.warning_type=#{warningType}")
    public List<EditionTaskInfo.WarningGroup> getWarningGroup(@Param("taskId")String taskId,@Param("warningType")Integer type);


    /**
     * 获取任务基本信息
     * @param id
     * @return
     */
    @Select("select id, name, 'TID-'||number as taskId, level, description, enable, creator, create_time as createTime,cron_expression as cronExpression from data_quality_task where id=#{id}")
    public DataQualityBasicInfo getTaskBasicInfo(@Param("id")String id);

    /**
     * 根据任务名获取任务Id
     * @param id
     * @return
     */
    @Select("select qrtz_job from data_quality_task where id=#{id}")
    public String getQrtzJobByTaskId(@Param("id")String id);

    /**
     * 根据taskId查询模板定时周期
     * @param id
     * @return
     */
    @Select("select cron_expression as cronExpression,start_time as startTime,end_time as endTime,level from data_quality_task where id=#{id}")
    public DataQualityTask getQrtzInfoByTemplateId(@Param("id")String id);

    /**
     * 更新任务启用状态
     * @param id
     * @param enable
     * @return
     */
    @Update("update data_quality_task set enable=#{status} where id=#{id}")
    public int updateTaskEnableStatus(@Param("id")String id, @Param("status")boolean enable);

    /**
     * 添加quartz名称
     * @param id
     * @param name
     * @return
     */
    @Update("update data_quality_task set qrtz_job=#{qrtzName}")
    public int updateTaskQrtzName(@Param("id")String id, @Param("qrtzName")String name);

    /**
     * 获取任务对象与规则对应关系
     * @param taskId
     * @return
     */
    @Select({" <script>",
             " select relation.subTaskRuleId,relation.subtask_id as subTaskId,relation.rule_template_id as ruleTemplateId,relation.object_id as objectId,",
             " data_quality_rule_template.type as taskType,data_quality_rule_template.scope from data_quality_rule_template",
             " join",
             " (select template_rule.ruleid,template_rule.subtask_id,template_rule.rule_template_id,obj.object_id,template_rule.subTaskRuleId from" ,
             " (select sub_rule.*,data_quality_rule.rule_template_id from data_quality_rule join",
             " (select id as subTaskRuleId,ruleid,subtask_id from data_quality_sub_task_rule where subtask_id in ",
             " (select id from data_quality_sub_task where task_id=#{taskId})) sub_rule on sub_rule.ruleid=data_quality_rule.id) template_rule",
             " join",
             " (select object_id,subtask_id from data_quality_sub_task_object where subtask_id in",
             " (select id from data_quality_sub_task where task_id=#{taskId})) obj",
             " on template_rule.subtask_id=obj.subtask_id) relation",
             " on relation.rule_template_id=data_quality_rule_template.id",
             " </script>"})
    public List<AtomicTaskExecution> getObjectWithRuleRelation(@Param("taskId")String taskId);

    /**
     * 根据qrtzName获取任务Id
     * @param qrtzName
     * @return
     */
    @Select("select id from data_quality_task where qrtz_job=#{qrtzName}")
    public String getTaskIdByQrtzName(@Param("qrtzName")String qrtzName);

    /**
     * 根据表guid获取数据库名与表名
     * @param guid
     * @return
     */
    @Select("select tableName,dbName as databaseName from tableInfo where tableGuid=#{guid}")
    public Table getDbAndTableName(@Param("guid")String guid);

    /**
     * 根据字段Id获取数据库名、表名及字段名
     * @param guid
     * @return
     */
    @Select("select column_info.column_name as columnName, column_info.type, tableInfo.tableName, tableInfo.dbName as databaseName from column_info join tableInfo on table_guid=tableGuid where column_guid=#{guid}")
    public Column getDbAndTableAndColumnName(@Param("guid")String guid);

    /**
     * 添加任务执行信息
     * @param taskExecute
     */
    @Insert("insert into data_quality_task_execute(id,task_id,percent,execute_status,executor,execute_time,orange_warning_count,red_warning_count,rule_error_count,number,counter,warning_status,error_status)values(#{task.id},#{task.taskId},#{task.percent},#{task.executeStatus},#{task.executor},#{task.executeTime},#{task.orangeWarningCount},#{task.redWarningCount},#{task.ruleErrorCount},#{task.number},#{task.counter},#{task.warningStatus},#{task.errorStatus})")
    public void initTaskExecuteInfo(@Param("task")DataQualityTaskExecute taskExecute);

    /**
     * 获取任务启动状态
     * @param taskId
     * @return
     */
    @Select("select enable from data_quality_task where id=#{taskId}")
    public Boolean isRuning(@Param("taskId")String taskId);

    /**
     * 更新任务规则完成占比
     * @param id
     * @param percent
     * @return
     */
    @Update("update data_quality_task_execute set percent=#{percent} where id=#{id}")
    public int updateTaskExecutionFinishedPercent(@Param("id")String id, @Param("percent")Float percent);

    @Update("update data_quality_task set current_execution_percent=#{percent} where id=#{taskId}")
    public int updateTaskFinishedPercent(@Param("taskId")String id, @Param("percent")Float percent);

    /**
     * 初始化规则执行信息
     * @param id
     * @param taskExecuteId
     * @param taskId
     * @param subTaskId
     * @param objectId
     * @param subTaskRuleId
     * @return
     */
    @Insert("insert into data_quality_task_rule_execute(id,task_execute_id,task_id,subtask_id,subtask_object_id,subtask_rule_id,create_time, update_time,warning_status,error_status)values(#{id},#{taskExecuteId},#{taskId},#{subTaskId},#{objectId},#{subTaskRuleId},#{createTime},#{updateTime},#{warningStatus},#{errorStatus})")
    public int initRuleExecuteInfo(@Param("id")String id, @Param("taskExecuteId")String taskExecuteId, @Param("taskId")String taskId, @Param("subTaskId")String subTaskId, @Param("objectId")String objectId, @Param("subTaskRuleId")String subTaskRuleId, @Param("createTime")Timestamp createTime, @Param("updateTime")Timestamp updateTime,@Param("warningStatus")Integer warningStatus,@Param("errorStatus")Integer errorStatus);

    /**
     * 获取库中最新值
     * @param subTaskRuleId
     * @return
     */
    @Select("select reference_value from data_quality_rule_execute where subtask_rule_id=#{subTaskRuleId} order by create_time desc limit 1)")
    public Float getLastValue(@Param("subTaskRuleId") String subTaskRuleId);

    /**
     * 获取子任务规则信息
     * @param subTaskRuleId
     * @return
     */
    @Select({" <script>",
             " select check_threshold_min_value as checkThresholdMinValue,check_threshold_max_value as checkThresholdMaxValue,orange_check_type as orangeCheckType,orange_check_expression_type as orangeCheckExpression,orange_threshold_min_value as orangeThresholdMinValue,orange_threshold_max_value as orangeThresholdMaxValue,",
             " red_check_type as redCheckType,red_check_expression_type as redCheckExpression,red_threshold_min_value as redThresholdMinValue,red_threshold_max_value as redThresholdMaxValue",
             " from data_quality_sub_task_rule where id=#{subTaskRuleId}",
            " </script>"})
    public DataQualitySubTaskRule getSubTaskRuleInfo(@Param("subTaskRuleId") String subTaskRuleId);

    /**
     * 获取规则类型
     * @param subTaskRuleId
     * @return
     */
    @Select("select check_type from data_quality_rule where id=(select ruleId from data_quality_sub_task_rule where id=#{subTaskRuleId})")
    public Integer getRuleCheckType(@Param("subTaskRuleId") String subTaskRuleId);

    /**
     * 获取规则校验表达式
     * @param subTaskRuleId
     * @return
     */
    @Select("select check_expression_type from data_quality_rule where id=(select ruleId from data_quality_sub_task_rule where id=#{subTaskRuleId})")
    public Integer getRuleCheckExpression(@Param("subTaskRuleId") String subTaskRuleId);

    /**
     * 插入任务规则执行信息
     * @param taskRuleExecute
     * @return
     */
    @Insert({" <script>",
             " insert into data_quality_task_rule_execute(id,task_execute_id,task_id,subtask_id,subtask_object_id,subtask_rule_id,result,reference_value,check_status,warning_status,create_time,update_time,orange_warning_check_status,red_warning_check_status)",
             " values(#{task.id},#{task.taskExecuteId},#{task.taskId},#{task.subTaskId},#{task.subTaskObjectId},#{task.subTaskRuleId},#{task.result},#{task.referenceValue},#{task.checkStatus},#{task.warningStatus},#{task.createTime},#{task.updateTime},#{task.orangeWarningCheckStatus},#{task.redWarningCheckStatus})",
             " </script>"})
    public int insertDataQualityTaskRuleExecute(@Param("task")DataQualityTaskRuleExecute taskRuleExecute);

    @Update({" <script>",
             " update data_quality_task_rule_execute set result=#{task.result},reference_value=#{task.referenceValue},check_status=#{task.checkStatus},warning_status=#{task.warningStatus},",
             " orange_warning_check_status=#{task.orangeWarningCheckStatus},red_warning_check_status=#{task.redWarningCheckStatus} where id=#{task.id}",
             " </script>"})
    public int updateRuleExecutionWarningInfo(@Param("task")DataQualityTaskRuleExecute taskRuleExecute);

    /**
     * 更新任务红色告警数量
     * @param id
     * @return
     */
    @Update("update data_quality_task_execute set orange_warning_count=orange_warning_count+1 where id=#{id}")
    public int updateTaskExecuteOrangeWarningNum(@Param("id")String id);

    @Update("update data_quality_task_execute set warning_status=#{status}")
    public int updateTaskExecuteWarningStatus(@Param("ruleExecuteId")String id, @Param("status")Integer status);

    @Update("update data_quality_task_rule_execute set warning_status=#{status}")
    public int updateTaskExecuteRuleWarningStatus(@Param("ruleExecuteId")String id, @Param("status")Integer status);


    @Update("update data_quality_task_execute set error_status=#{status}")
    public int updateTaskExecuteErrorStatus(@Param("ruleExecuteId")String id, @Param("status")Integer status);

    @Update("update data_quality_task_rule_execute set error_status=#{status}")
    public int updateTaskExecuteRuleErrorStatus(@Param("ruleExecuteId")String id, @Param("status")Integer status);

    /**
     * 更新任务红色告警数量
     * @param id
     * @return
     */
    @Update("update data_quality_task_execute set red_warning_count=red_warning_count+1 where id=#{id}")
    public int updateTaskExecuteRedWarningNum(@Param("id")String id);

    /**
     * 更新执行失败规则数量
     * @param id
     * @return
     */
    @Update("update data_quality_task_execute set rule_error_count=rule_error_count+1 where id=#{id}")
    public int updateTaskExecuteRuleErrorNum(@Param("id")String id);

    /**
     * 更新任务执行错误信息
     * @param id
     * @param msg
     * @return
     */
    @Update("update data_quality_task_execute set error_msg=#{msg} where id=#{id}")
    public int updateTaskExecuteErrorMsg(@Param("id")String id, @Param("msg")String msg);

    @Update("update data_quality_task_rule_execute set error_msg=#{msg} where id=#{id}")
    public int updateTaskRuleExecutionErrorMsg(@Param("id")String id, @Param("msg")String msg);

    /**
     * 更新任务执行耗时
     * @param id
     * @param time
     * @return
     */
    @Update("update data_quality_task_execute set cost_time=#{time} where id=#{id}")
    public int updateDataTaskCostTime(@Param("id")String id, @Param("time")Long time);

    @Update("update data_quality_task_execute set execute_status=#{status} where id=#{id}")
    public int updateTaskExecuteStatus(@Param("id")String id, @Param("status")Integer status);

    @Update("update data_quality_task set current_execution_status=#{status} where id=#{taskId}")
    public int updateTaskStatus(@Param("taskId")String id, @Param("status")Integer status);

    @Select({" <script>",
             " select max(create_time) as lastExecuteTime,b.* from data_quality_task_rule_execute",
             " join",
             " (select data_quality_sub_task_rule.id as subtaskruleid,a.* from data_quality_sub_task_rule",
             " join",
             " (select data_quality_rule.id as ruleid,data_quality_rule.code,data_quality_rule.description",
             " from data_quality_rule join data_quality_rule_template on data_quality_rule.rule_template_id=data_quality_rule_template.id) a",
             " on",
             " data_quality_sub_task_rule.ruleid = a.ruleid) b",
             " on",
             " data_quality_task_rule_execute.subtask_rule_id = b.subtaskruleid where data_quality_task_rule_execute.task_id=#{taskId} group by b.subtaskruleid,b.ruleid,b.code,b.description",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<TaskRuleHeader> getRuleList(@Param("taskId")String id, @Param("params") Parameters params);

    @Select({" <script>",
             " select count(*) from",
             " (select max(create_time) as lastExecuteTime,b.subtaskruleid from data_quality_task_rule_execute",
             " join",
             " (select data_quality_sub_task_rule.id as subtaskruleid,a.* from data_quality_sub_task_rule",
             " join",
             " (select data_quality_rule.id as ruleid,data_quality_rule.code,data_quality_rule.description",
             " from data_quality_rule join data_quality_rule_template on data_quality_rule.rule_template_id=data_quality_rule_template.id) a",
             " on",
             " data_quality_sub_task_rule.ruleid = a.ruleid) b",
             " on",
             " data_quality_task_rule_execute.subtask_rule_id = b.subtaskruleid where data_quality_task_rule_execute.task_id=#{taskId} group by b.subtaskruleid) c",
             " </script>"})
    public long countRuleList(@Param("taskId")String id);

    @Select("select category_id from data_quality_rule_template where id=(select rule_template_id from data_quality_rule where id=#{ruleId})")
    public String getTypeByRuleId(@Param("ruleId")String id);

    @Select("select id from data_quality_task_execute where task_id=#{taskId}")
    public String getExecuteIdByTaskId(@Param("taskId")String id);

    @Select("select id as taskId,name as taskName,level,description,execution_count as executeCount,orange_warning_total_count as orangeWarningTotalCount,red_warning_total_count as redWarningTotalCount,error_total_count as errorTotalCount,start_time as startTime,end_time as endTime from data_quality_task where id=#{taskId}")
    public TaskExecutionReport getTaskExecutionInfo(@Param("taskId")String id);

    @Select("select id as executionId,number,orange_warning_count as orangeWarningCount,red_warning_count as redWarningCount,rule_error_count as errorCount,execute_time as executeTime from data_quality_task_execute where task_id=#{taskId}")
    public List<TaskExecutionReport.ExecutionRecord> getTaskExecutionRecord(@Param("taskId")String id);

    @Select("select updater from data_quality_task where id=#{taskId}")
    public String getTaskUpdater(@Param("taskId")String id);


    @Update("update data_quality_task set orange_warning_total_count=orange_warning_total_count+1 where id=#{taskId}")
    public int updateTaskOrangeWarningCount(@Param("taskId")String id);

    @Update("update data_quality_task set red_warning_total_count=red_warning_total_count+1 where id=#{taskId}")
    public int updateTaskRedWarningCount(@Param("taskId")String id);

    @Update("update data_quality_task set error_total_count=error_total_count+1 where id=#{taskId}")
    public int updateTaskErrorCount(@Param("taskId")String id);

    @Update("update data_quality_task set execution_count=execution_count+1 where id=#{taskId}")
    public int updateTaskExecutionCount(@Param("taskId")String id);


    @Select({" <script>",
             " select c.id as ruleExecutionId,c.task_execute_id as executionId,c.subtask_rule_id as subTaskRuleId,c.subtask_object_id as objectId, c.result, c.check_status as checkStatus,c.orange_warning_check_status as orangeCheckStatus, c.red_warning_check_status as redCheckStatus,",
             " d.name as ruleName,d.scope as objectType,d.description,d.check_type as checkType, d.check_expression_type as checkExpression,d.check_threshold_min_value as checkMinValue,d.check_threshold_max_value as checkMaxValue,d.orange_check_type as orangeWarningCheckType,d.orange_check_expression_type as orangeWarningcheckExpression,",
             " d.orange_threshold_min_value as orangeWarningMinValue,d.orange_threshold_max_value as orangeWarningMaxValue,d.red_check_type as redWarningCheckType,d.red_check_expression_type as redWarningcheckExpression,d.red_threshold_min_value as redWarningMinValue,d.red_threshold_max_value as redWarningMaxValue",
             " from (select * from data_quality_task_rule_execute as rule_execute where task_execute_id=#{ruleExecutionId}) c",
             " join",
             " (select a.*,b.* from (select data_quality_sub_task_rule.*,data_quality_rule.name,data_quality_rule.description,data_quality_rule.scope from data_quality_sub_task_rule join data_quality_rule on data_quality_sub_task_rule.ruleid=data_quality_rule.id) a",
             " join",
             " (select subtask_id,object_id from data_quality_sub_task_object where task_id=(select task_id from data_quality_task_execute where id=#{ruleExecutionId})) b",
             " on a.subtask_id = b.subtask_id) d",
             " on d.id=c.subtask_rule_id and d.object_id=c.subtask_object_id",
             " </script>"})
    List<TaskRuleExecutionRecord> getTaskRuleExecutionRecordList(@Param("ruleExecutionId")String ruleExecutionId);

    @Select("select name from data_quality_rule where id=(select ruleId from data_quality_sub_task_rule where id=#{subTaskRuleId})")
    public String getRuleCheckName(@Param("subTaskRuleId")String subTaskRuleId);

    /**
     * 获取当前任务最大检验次数
     * @param taskId
     * @return
     */
    @Select("select max(counter) from data_quality_task_execute where task_id=#{taskId}")
    public Integer getMaxCounter(@Param("taskId")String taskId);

    /**
     * 任务执行日志列表
     * @param taskId
     * @return
     */
    @Select({" <script>",
             " select id as executionId,execute_status as executeStatus,rule_error_count as errorCount, users.userName as executor,execute_time as executeTime,cost_time as costTime from data_quality_task_execute",
             " join users on users.userId=data_quality_task_execute.executor",
             " where task_id=#{taskId}",
             " and (executor like '%${params.query}%' ESCAPE '/')",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<ExecutionLogHeader> getExecutionLogList(@Param("taskId")String taskId, @Param("params")Parameters parameters);

    /**
     * 统计任务执行日志列表
     * @param taskId
     * @param parameters
     * @return
     */
    @Select({" <script>",
             " select count(*) from",
             " (select * from data_quality_task_execute",
             " join users on users.userId=data_quality_task_execute.executor",
             " where task_id=#{taskId}",
             " and (users.userName like '%${params.query}%' ESCAPE '/')) as info",
             " </script>"})
    public Long countExecutionLogList(@Param("taskId")String taskId, @Param("params")Parameters parameters);

    @Select("select data_quality_task.name as taskName, execute_time as executeTime,counter,execute_status as executeStatus from data_quality_task_execute join data_quality_task on data_quality_task.id=data_quality_task_execute.task_id where data_quality_task_execute.id=#{ruleExecutionId}")
    public ExecutionLog getExecutionInfo(@Param("ruleExecutionId")String ruleExecutionId);

    @Select("select error_msg from data_quality_task_rule_execute where task_execute_id=#{ruleExecutionId}")
    public List<String> getRuleExecutionLog(@Param("ruleExecutionId")String ruleExecutionId);

    @Update("update data_quality_task set name=#{taskInfo.name},level=#{taskInfo.level},description=#{taskInfo.description},cron_expression=#{taskInfo.cronExpression},start_time=#{taskInfo.startTime},end_time=#{taskInfo.endTime},update_time=#{taskInfo.updateTime},updater=#{taskInfo.updater} where id=#{taskInfo.id}")
    public int updateTaskInfo(@Param("taskInfo")DataQualityTask taskInfo);

    @Select("select qrtz_job from data_quality_task where id=#{taskId}")
    public String getJobName(@Param("taskId")String taskId);
}
