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

import io.zeta.metaspace.model.dataquality2.AtomicTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTask;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskObject;
import io.zeta.metaspace.model.dataquality2.DataQualitySubTaskRule;
import io.zeta.metaspace.model.dataquality2.DataQualityTask;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskExecute;
import io.zeta.metaspace.model.dataquality2.DataQualityTaskRuleExecute;
import io.zeta.metaspace.model.dataquality2.Rule;
import io.zeta.metaspace.model.dataquality2.RuleHeader;
import io.zeta.metaspace.model.dataquality2.TaskHeader;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 9:53
 */
public interface TaskManageDAO {

    @Insert(" insert into data_quality_rule(id,rule_template_id,name,code,category_id,enable,description,level,check_type,check_expression_type,check_threshold,create_time,update_time,delete) " +
            " values(#{id},#{ruleTemplateId},#{name},#{code},#{categoryId},#{enable},#{description},#{checkType},#{checkExpressionType},#{checkThreshold},#{createTime},#{updateTime},#{delete})")
    public int insert(Rule rule);

    @Select({"<script>",
             " select data_quality_task.enable, data_quality_task.number as taskId, data_quality_task.name as taskName, data_quality_task.description, data_quality_task.start_time as startTime, data_quality_task.end_time as endTime, data_quality_task.level as taskLevel",
             " data_quality_task_execute.red_warning_count as redWarningCount, data_quality_task_execute.yellow_warning_count as yellowWarningCount, data_quality_task_execute.rule_error_count as ruleErrorCount, data_quality_task_execute.execute_status as executeStatus, data_quality_task_execute.percent",
             " from data_quality_task, data_quality_task_execute",
             " where data_quality_task_execute.task_id=data_quality_task.id",
             " and (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task.number like '%${params.query}%' ESCAPE '/')",
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

    @Select({"<script>",
             " select count(*)",
             " from data_quality_task, data_quality_task_execute",
             " where data_quality_task_execute.task_id=data_quality_task.id",
             " and (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task.number like '%${params.query}%' ESCAPE '/')",
             " <if test='my==0'>",
             " and data_quality_task.creator=#{creator}",
             " </if>",
             " </script>"})
    public long countTaskList(@Param("my") Integer my, @Param("creator") String creator, @Param("params") Parameters params);

    @Update({" <script>",
             " update data_quality_task set delete=true where id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " </script>"})
    public int deleteTaskList(@Param("taskIdList")List<String> taskIdList);

    @Update({" <script>",
             " update data_quality_sub_task set delete=true where task_id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " </script>"})
    public int deleteSubTaskList(@Param("taskIdList")List<String> taskIdList);

    @Update({" <script>",
             " update data_quality_sub_task_object set delete=true where subtask_id in",
             " (select id from data_quality_sub_task where task_id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " )",
            " </script>"})
    public int deleteSubTaskObjectList(@Param("taskIdList")List<String> taskIdList);

    @Update({" <script>",
             " update data_quality_sub_task_rule set delete=true where subtask_id in",
             " (select id from data_quality_sub_task where task_id in",
             " <foreach item='taskId' index='index' collection='taskIdList' separator=',' open='(' close=')'>" ,
             " ${taskId}",
             " </foreach>",
             " )",
             " </script>"})
    public int deleteSubTaskRuleList(@Param("taskIdList")List<String> taskIdList);

    @Select({"<script>",
             " select data_quality_rule.id, data_quality_rule.name, data_quality_rule.rule_template_id as ruleTemplateId, data_quality_rule_template.scope",
             " from data_quality_rule, data_quality_rule_template",
             " where data_quality_rule.category_id=#{categoryId}",
             " and data_quality_rule.rule_template_id=data_quality_rule_template.id",
             " </script>"})
    public List<RuleHeader> getRuleListByCategoryId(@Param("categoryId")String categoryId);

    @Select({" <script>",
             " select DISTINCT type from column_info where column_guid in ",
             " <foreach item='columnId' index='index' collection='columnIdList' separator=',' open='(' close=')'>" ,
             " ${columnId}",
             " </foreach>",
             " </script>"})
    public List<String> getColumnTypeList(@Param("columnIdList")List<String> columnIdList);

    @Select("select id from data_quality_rule_template where category_id=5")
    public List<String> getNumericTypeTemplateRuleId();


    @Insert({" insert into data_quality_task(id,name,level,description,cron_expression,error_warning_group_id,enable,start_time,end_time,create_time,update_time,creator,delete) ",
            " values(#{task.id},#{task.name},#{task.level},#{task.description},#{task.cronExpression},#{task.errorWarningGroupIds},#{task.enable},#{task.startTime},#{task.endTime},#{task.createTime},#{task.updateTime},#{task.creator},#{task.delete})"})
    public int addDataQualityTask(@Param("task")DataQualityTask task);

    @Insert({" insert into data_quality_sub_task(id,task_id,datasource_type,sequence,create_time,update_time,delete) ",
             " values(#{subTask.id},#{subTask.taskId},#{subTask.dataSourceType},#{subTask.sequence},#{subTask.createTime},#{subTask.updateTime},#{subTask.delete})"})
    public int addDataQualitySubTask(@Param("subTask")DataQualitySubTask subTask);

    @Insert({" insert into data_quality_sub_task_object(id,subtask_id,object_id,sequence,create_time,update_time,delete) ",
             " values(#{taskObject.id},#{taskObject.subTaskId},#{taskObject.objectId},#{taskObject.sequence},#{taskObject.createTime},#{taskObject.updateTime},#{taskObject.delete})"})
    public int addDataQualitySubTaskObject(@Param("taskObject")DataQualitySubTaskObject taskObject);


    @Insert({" insert into data_quality_sub_task_rule(id,subtask_id,ruleId,check_threshold,orange_check_type,orange_check_expression_type,orange_threshold,orange_warning_groupid,red_check_type,red_check_expression_type,red_threshold,red_warning_groupid,sequence,create_time,update_time,delete) ",
             " values(#{rule.id},#{rule.subTaskId},#{rule.ruleId},#{rule.checkThreshold},#{rule.orangeCheckType},#{rule.orangeCheckExpression},#{rule.orangeThreshold},#{rule.orangeWarningGroupIds},#{rule.redCheckType},#{rule.redCheckExpression},#{rule.redThreshold},#{rule.redWarningGroupIds},#{rule.sequence},#{rule.createTime},#{rule.updateTime},#{rule.delete})"})
    public int addDataQualitySubTaskRule(@Param("rule")DataQualitySubTaskRule taskRule);

    @Select("select id, name, number as taskId, level, description, enable, creator, create_time as createTime, cron as cronExpression from data_quality_task where id=#{id}")
    public DataQualityTask getTaskBasicInfo(@Param("id")String id);

    @Select("select qrtz_job from data_quality_task where id=#{id}")
    public String getQrtzJobByTaskId(@Param("id")String id);

    /**
     * 根据taskId查询模板定时周期
     * @param id
     * @return
     */
    @Select("select cron_expression as cronExpression,start_time as startTime,end_time as endTime,level from data_quality_task where id=#{id}")
    public DataQualityTask getQrtzInfoByTemplateId(@Param("id")String id);

    @Update("update data_quality_task set enable=#{status}")
    public int updateTaskStatus(@Param("id")String id, @Param("status")boolean enable);

    @Update("update data_quality_task set qrtz_job=#{qrtzName}")
    public int updateTaskQrtzName(@Param("id")String id, @Param("qrtzName")String name);


    @Select({" <script>",
             " select relation.ruleid as subTaskRuleId,relation.subtask_id as subTaskId,relation.rule_template_id as ruleTemplateId,relation.object_id as objectId,",
             " data_quality_rule_template.type as taskType,data_quality_rule_template.scope from data_quality_rule_template",
             " join",
             " (select template_rule.ruleid,template_rule.subtask_id,template_rule.rule_template_id,obj.object_id from" ,
             " (select sub_rule.*,data_quality_rule.rule_template_id from data_quality_rule join",
             " (select ruleid,subtask_id from data_quality_sub_task_rule where subtask_id in",
             " (select id from data_quality_sub_task where task_id=(select id from data_quality_task where qrtz_job=#{qrtzName}'))) sub_rule on sub_rule.ruleid=data_quality_rule.id) template_rule",
             " join",
             " (select object_id,subtask_id from data_quality_sub_task_object where subtask_id in",
             " (select id from data_quality_sub_task where task_id=(select id from data_quality_task where qrtz_job=#{qrtzName}))) obj",
             " on template_rule.subtask_id=obj.subtask_id) relation",
             " on relation.rule_template_id=data_quality_rule_template.id",
             " </script>"})
    public List<AtomicTask> getObjectWithRuleRelation(@Param("qrtzName")String qrtzName);

    @Select("select tableName,dbName as databaseName from tableInfo where tableGuid=#{guid}")
    public Table getDbAndTableName(@Param("guid")String guid);

    @Select("select column_info.column_name as columnName, column_info.type, tableInfo.tableName, tableInfo.dbName as databaseName from column_info join tableInfo on table_guid=tableGuid where column_guid=#{guid}")
    public Column getDbAndTableAndColumnName(@Param("guid")String guid);

    @Insert("insert into data_quality_task_execute(id,task_id,percent,execute_status,executor,execute_time) values(#{task.id},#{task.taskId},#{task.percent},#{task.executeStatus},#{task.executeTime})")
    public void initTaskExecuteInfo(@Param("task")DataQualityTaskExecute taskExecute);

    @Select("select enable from data_quality_task where id=#{taskId}")
    public Boolean isRuning(@Param("taskId")String taskId);

    @Update("update data_quality_task_execute set percent=#{percent} where id=#{id}")
    public int updateTaskFinishedPercent(@Param("id")String id, @Param("percent")Float percent);


    @Insert("insert into data_quality_task_rule_execute(id,task_execute_id,task_id,subtask_id,subtask_object_id,subtask_rule_id)values(#{id},#{taskExecuteId},#{taskId},#{subTaskId},#{objectId},#{subTaskRuleId})")
    public int initRuleExecuteInfo(@Param("id")String id, @Param("taskExecuteId")String taskExecuteId, @Param("taskId")String taskId, @Param("subTaskId")String subTaskId, @Param("objectId")String objectId, @Param("subTaskRuleId")String subTaskRuleId);

    /**
     * 获取库中最新值
     * @param subTaskRuleId
     * @return
     */
    @Select("select reference_value from data_quality_rule_execute where subtask_rule_id=#{subTaskRuleId} order by create_time desc limit 1)")
    public Float getLastValue(@Param("subTaskRuleId") String subTaskRuleId);

    @Update("update data_quality_rule_execute set result=#{result}, reference=#{referenceValue} where id=#{ruleExecuteId}")
    public int completeCalculationResult(@Param("ruleExecuteId") String ruleExecuteId, @Param("result")Float value, @Param("referenceValue") Float referenceValue);


    @Update("")
    public int updateTaskExecuteWarningInfo();

    @Select({" <script>",
             " select check_threshold as checkThreshold,orange_check_type as orangeCheckType,orange_check_expression_type as orangeCheckExpression,orange_threshold as orangeThreshold,",
             " orange_warning_groupid as orangeWarningGroupIds,red_check_type as redCheckType,red_check_expression_type as redCheckExpression,red_threshold as redThreshold,red_warning_groupid as redWarningGroupIds",
             " from data_quality_sub_task_rule where id=#{subTaskRuleId}",
            " </script>"})
    public DataQualitySubTaskRule getSubTaskRuleInfo(@Param("subTaskRuleId") String subTaskRuleId);

    @Select("select check_type from data_quality where id=(select ruleId from data_quality_sub_task_rule where id=#{subTaskRuleId})")
    public Integer getRuleCheckType(@Param("subTaskRuleId") String subTaskRuleId);
}
