package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.datasource.DataSource;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface WarningGroupDAO {

    @Insert({" insert into warning_group(id,name,type,contacts,description,create_time,update_time,creator,delete,tenantId) ",
             " values(#{warningGroup.id},#{warningGroup.name},#{warningGroup.type},#{warningGroup.contacts},#{warningGroup.description},#{warningGroup.createTime},#{warningGroup.updateTime},#{warningGroup.creator},#{warningGroup.delete},#{tenantId})"})
    public int insert(@Param("warningGroup") WarningGroup warningGroup,@Param("tenantId")String tenantId);

    @Insert(" update warning_group set name=#{name},type=#{type},contacts=#{contacts},description=#{description},update_time=#{updateTime} where id=#{id}")
    public int update(WarningGroup warningGroup);

    @Select({" select a.id,a.name,a.type,a.contacts,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false and a.id=#{id}"})
    public WarningGroup getById(@Param("id") String id);

    @Select({" <script>",
             " select id,name,type,contacts,description,create_time as createTime,update_time as updateTime,delete ",
             " from warning_group where delete=false and name=#{name} and tenantid=#{tenantId}",
             " <if test='id != null'>",
             " and id!=#{id}",
             " </if>",
             " </script>"})
    public WarningGroup getByName(@Param("name") String name,@Param("id") String id,@Param("tenantId")String tenantId);

    @Select("update warning_group set delete=true where id=#{id}")
    public void deleteById(@Param("id") String id);

    @Insert({" <script>",
             " update warning_group set delete=true where id in ",
             " <foreach collection='idList' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public void deleteByIdList(@Param("idList") List<String> idList);

    @Select("select count(*) from data_quality_task2warning_group where warning_group_id=#{warningGroupId}")
    public int countWarningGroupUserd(@Param("warningGroupId")String id);


    @Select({"<script>",
             " select count(*)over() total,a.id,a.name,a.type,a.contacts,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false and tenantid=#{tenantId} ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (a.name like '%${params.query}%' ESCAPE '/' ) ",
             " </if>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<WarningGroup> search(@Param("params") Parameters params,@Param("tenantId")String tenantId);



    @Select({"<script>",
             " select count(*)over() total,id,name,description,warning_group.create_time as createTime,contacts,users.username as creator,type",
             " from warning_group join users on users.userid=creator where delete=false and tenantid=#{tenantId} ",
             " <if test=\"params.query != null and params.query!=''\">",
             " and name like '%${params.query}%' ESCAPE '/'",
             " </if>",
             " <if test='params.sortby != null and params.order != null'>",
             " order by ${params.sortby} ${params.order}",
             " </if>",
             " <if test='params.limit != null and params.limit != -1'>",
             " limit #{params.limit} offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<WarningGroup> getWarningGroup(@Param("params") Parameters params,@Param("tenantId")String tenantId) throws SQLException;

    @Select({" <script>",
             " select count(*)over() total,data_quality_task.id as taskId, data_quality_task_execute.id as executionId, data_quality_task.name as taskName,data_quality_task_execute.number,warning_status as warningStatus,orange_warning_count as orangeWarningCount,",
             " red_warning_count as redWarningCount,execute_time as executionTime from data_quality_task_execute join data_quality_task on data_quality_task_execute.task_id=data_quality_task.id",
             " where data_quality_task.delete=false and data_quality_task.tenantId=#{tenantId} and (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " <if test='warningType==1'>",
             " and warning_status=1",
             " </if>",
             " <if test='warningType==2'>",
             " and warning_status=2",
             " </if>",
             " <if test='warningType==0'>",
             " and warning_status!=0",
             " </if>",
             " order by executionTime desc",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<TaskWarningHeader> getWarningList(@Param("warningType")Integer warningType, @Param("params") Parameters params,@Param("tenantId")String tenantId);

    @Select({" <script>",
             " select count(*)over() total,data_quality_task.id as taskId,data_quality_task_execute.id as executionId,data_quality_task.name as taskName,data_quality_task_execute.number,error_status as errorStatus,",
             " error_msg as errorMsg,execute_time as executionTime from data_quality_task_execute join data_quality_task on data_quality_task_execute.task_id=data_quality_task.id",
             " where data_quality_task.delete=false and data_quality_task.tenantId=#{tenantId} and (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " <if test='errorType==1'>",
             " and error_status=1",
             " </if>",
             " <if test='errorType==2'>",
             " and error_status=2",
             " </if>",
             " <if test='errorType==0'>",
             " and error_status!=0",
             " </if>",
             " order by executionTime desc",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<TaskErrorHeader> getErrorWarningList(@Param("errorType")Integer errorType, @Param("params") Parameters params,@Param("tenantId")String tenantId);


    @Select("select id,name from warning_group where id in (select warning_group_id from data_quality_task2warning_group where task_id=#{taskId} and warning_type=#{warningType})")
    public List<TaskWarningHeader.WarningGroupHeader> getWarningGroupList(@Param("taskId") String taskId, @Param("warningType") Integer warningType);

    /**
     * 关闭告警
     */
    @Update({" <script>",
             " update data_quality_task_execute set ",
             " <if test='warningType==0'>",
             " warning_status=2",
             " </if>",
             " <if test='warningType==1'>",
             " error_status=2",
             " </if>",
             " where id in",
             " <foreach collection='executionIdList' item='executionId' index='index' open='(' close=')' separator=','>",
             " #{executionId}",
             " </foreach>",
             " </script>"})
    public int closeTaskExecutionWarning(@Param("warningType")Integer warningType, @Param("executionIdList")List<String> executionIdList);

    /**
     * 关闭告警
     */
    @Update("update data_quality_task_execute set warning_status = 2 where id = #{executeId}")
    public int closeExecutionWarning(@Param("executeId")String executeId);

    /**
     * 关闭告警
     */
    @Update("update data_quality_task_rule_execute set general_warning_check_status= #{ruleExecute.generalWarningCheckStatus}," +
            "orange_warning_check_status = #{ruleExecute.orangeWarningCheckStatus}," +
            "red_warning_check_status = #{ruleExecute.redWarningCheckStatus}" +
            "where id = #{ruleExecute.id}")
    public int closeRuleExecuteWarn(@Param("ruleExecute")RuleExecute ruleExecute);




    @Update({" <script>",
             " update data_quality_task_rule_execute set",
             " <if test='warningType==0'>",
             " warning_status=2",
             " </if>",
             " <if test='warningType==1'>",
             " error_status=2",
             " </if>",
             " close_time=#{closeTime},",
             " closer=#{closer}",
             " where task_execute_id in",
             " <foreach collection='executionIdList' item='executionId' index='index' open='(' close=')' separator=','>",
             " #{executionId}",
             " </foreach>",
             " </script>"})
    public int closeAllTaskRuleExecutionWarning(@Param("warningType")Integer warningType, @Param("executionIdList")List<String> executionIdList, @Param("closeTime")Timestamp closeTime, @Param("closer")String closer);

    @Update({" <script>",
             " update data_quality_task_rule_execute set",
             " <if test='warningType==0'>",
             " warning_status=2",
             " </if>",
             " <if test='warningType==1'>",
             " error_status=2",
             " </if>",
             " close_time=#{closeTime},",
             " closer=#{closer}",
             " where id in",
             " <foreach collection='executionRuleIdList' item='executionRuleId' index='index' open='(' close=')' separator=','>",
             " #{executionRuleId}",
             " </foreach>",
             " </script>"})
    public int closeTaskRuleExecutionWarning(@Param("warningType")Integer warningType, @Param("executionRuleIdList")List<String> executionRuleIdList, @Param("closeTime")Timestamp closeTime, @Param("closer")String closer);

    @Select({" <script>",
             " select count(*) from data_quality_task_rule_execute where task_execute_id=#{executionId}",
             " <if test='warningType==0'>",
             " and warning_status=1",
             " </if>",
             " <if test='warningType==1'>",
             " and error_status=1",
             " </if>",
             " </script>"})
    public int coutTaskRuleExecutionOpenWarning(@Param("warningType")Integer warningType, @Param("executionId")String taskIdList);

    @Select({" <script>",
             " select data_quality_task.name as taskName,data_quality_task_execute.id as executionId,data_quality_task_execute.execute_time as executeTime,",
             " data_quality_task_execute.closer,data_quality_task_execute.close_time as closeTime,data_quality_task_execute.warning_status as warningStatus",
             " from data_quality_task_execute join data_quality_task on data_quality_task.id=data_quality_task_execute.task_id where",
             " data_quality_task_execute.id=#{executionId}",
             " </script>"})
    public WarningInfo getWarningBasicInfo(@Param("executionId")String executionId);

    @Select("select id as subTaskId,sequence,datasource_type as ruleType from data_quality_sub_task where task_id=(select task_id from data_quality_task_execute where id=#{executionId})")
    public List<WarningInfo.SubTaskWarning> getSubTaskWarning(@Param("executionId")String executionId);

    @Select({" <script>",
             " select a.objectId,a.result,data_quality_rule_template.name as ruleName,data_quality_rule_template.unit as unit,0 as warningType,data_quality_rule_template.scope,data_quality_rule_template.type from",
             " (select data_quality_sub_task_rule.ruleid as ruleId,data_quality_task_rule_execute.subtask_object_id as objectId,",
             " data_quality_task_rule_execute.result from data_quality_task_rule_execute ",
             " join data_quality_sub_task_rule on data_quality_sub_task_rule.id=data_quality_task_rule_execute.subtask_rule_id",
             " where data_quality_task_rule_execute.subtask_id=#{subTaskId} and orange_warning_check_status=1 and task_execute_id=#{taskExecutionId}) a",
             " join data_quality_rule_template on data_quality_rule_template.id=a.ruleid and data_quality_rule_template.tenantId = #{tenantId} ",
             " UNION ALL",
             " select a.objectId,a.result,data_quality_rule_template.name as ruleName,data_quality_rule_template.unit as unit,1 as warningType,data_quality_rule_template.scope,data_quality_rule_template.type from",
             " (select data_quality_sub_task_rule.ruleid as ruleId,data_quality_task_rule_execute.subtask_object_id as objectId,",
             " data_quality_task_rule_execute.result from data_quality_task_rule_execute ",
             " join data_quality_sub_task_rule on data_quality_sub_task_rule.id=data_quality_task_rule_execute.subtask_rule_id",
             " where data_quality_task_rule_execute.subtask_id=#{subTaskId} and red_warning_check_status=1 and task_execute_id=#{taskExecutionId}) a",
             " join data_quality_rule_template on data_quality_rule_template.id=a.ruleid and data_quality_rule_template.tenantId = #{tenantId}",
             " </script>"})
    public List<WarningInfo.SubTaskRuleWarning> getSubTaskRuleWarning(@Param("taskExecutionId")String taskExecutionId, @Param("subTaskId")String subTaskId,@Param("tenantId")String tenantId);


    @Select("select task_execute_id as taskExecuteId,create_time as executeTime,error_msg as errorMessage from data_quality_task_rule_execute where task_execute_id=#{executionId} and error_status!=0  ORDER BY create_time desc limit 1")
    public ErrorInfo getErrorInfo(@Param("executionId")String executionId);

    @Select("select source_id,source_name, database from data_source where source_id = #{sourceId}")
    public DataSource getDataSource(@Param("sourceId")String sourceId);


    @Select({" <script>",
             " select contacts from warning_group where id in",
             " (select warning_group_id from data_quality_task2warning_group WHERE task_id=",
             " (select task_id from data_quality_task_rule_execute where id=#{executionRuleId}))",
             " </script>"})
    public List<String> getWarningGroupMemberList(@Param("executionRuleId")String executionRuleId);

    @Select({" <script>",
             " select scope from data_quality_rule_template where id=(select rule_template_id from data_quality_rule where id=",
             " (select rule_template_id from data_quality_rule where id=(select ruleid from data_quality_sub_task_rule where id=",
             " (select subtask_rule_id from data_quality_task_rule_execute where id=#{executionRuleId})))",
             " </script>"})
    public Integer getRuleScope(@Param("executionRuleId")String executionRuleId);

    @Select("select subtask_object_id from data_quality_task_rule_execute where id=#{executionRuleId}")
    public String getObjectId(@Param("executionRuleId")String executionRuleId);

    @Select({"<script>",
            "select count(*)over() total, tt.* from (",
            "select t1.id as taskId, t3.general_warning_check_status as status, t3.id as warnNo, t1.name as taskName,t4.sequence, t6.name as ruleName," ,
            "t3.create_time as warnTime, 0 as warnGrade,t6.description as ruleDescription,t6.scope,t6.type,",
            "t5.check_type as checkType,t5.check_threshold_max_value checkMaxValue,t5.check_threshold_min_value as checkMinValue,",
            "t3.subtask_object_id as objectId,t6.sql,t3.result,t6.unit from data_quality_task t1 join data_quality_task_execute t2 on t1.id = t2.task_id" ,
            "join data_quality_task_rule_execute t3 on t2.id = t3.task_execute_id join data_quality_sub_task t4 on (t3.subtask_id = t4.id and t4.delete = false)",
            "join data_quality_sub_task_rule t5 on (t3.subtask_rule_id = t5.id and t5.delete = false) " ,
            "join data_quality_rule_template t6 on (ruleid = t6.id and t6.tenantid = t1.tenantid and t6.delete = false) ",
            " where t1.delete=false and t1.tenantId=#{tenantId} and (t1.name like '%${params.query}%' ESCAPE '/' or t3.id like '%${params.query}%' ESCAPE '/')",
            " and t3.general_warning_check_status != 0",
            "UNION",
            "select t1.id as taskId, t3.orange_warning_check_status as status, t3.id as warnNo, t1.name as taskName,t4.sequence, t6.name as ruleName," ,
            "t3.create_time as warnTime, 1 as warnGrade,t6.description as ruleDescription,t6.scope,t6.type,",
            "t5.check_type as checkType,t5.check_threshold_max_value checkMaxValue,t5.check_threshold_min_value as checkMinValue,",
            "t3.subtask_object_id as objectId,t6.sql,t3.result,t6.unit from data_quality_task t1 join data_quality_task_execute t2 on t1.id = t2.task_id" ,
            "join data_quality_task_rule_execute t3 on t2.id = t3.task_execute_id join data_quality_sub_task t4 on (t3.subtask_id = t4.id and t4.delete = false)",
            "join data_quality_sub_task_rule t5 on (t3.subtask_rule_id = t5.id and t5.delete = false) " ,
            "join data_quality_rule_template t6 on (ruleid = t6.id and t6.tenantid = t1.tenantid and t6.delete = false) ",
            " where t1.delete=false and t1.tenantId=#{tenantId} and (t1.name like '%${params.query}%' ESCAPE '/' or t3.id like '%${params.query}%' ESCAPE '/')",
            " and t3.orange_warning_check_status != 0",
            "UNION",
            "select t1.id as taskId, t3.red_warning_check_status as status, t3.id as warnNo, t1.name as taskName,t4.sequence, t6.name as ruleName," ,
            "t3.create_time as warnTime, 2 as warnGrade,t6.description as ruleDescription,t6.scope,t6.type,",
            "t5.check_type as checkType,t5.check_threshold_max_value checkMaxValue,t5.check_threshold_min_value as checkMinValue,",
            "t3.subtask_object_id as objectId,t6.sql,t3.result,t6.unit from data_quality_task t1 join data_quality_task_execute t2 on t1.id = t2.task_id" ,
            "join data_quality_task_rule_execute t3 on t2.id = t3.task_execute_id join data_quality_sub_task t4 on (t3.subtask_id = t4.id and t4.delete = false)",
            "join data_quality_sub_task_rule t5 on (t3.subtask_rule_id = t5.id and t5.delete = false) " ,
            "join data_quality_rule_template t6 on (ruleid = t6.id and t6.tenantid = t1.tenantid and t6.delete = false) ",
            " where t1.delete=false and t1.tenantId=#{tenantId} and (t1.name like '%${params.query}%' ESCAPE '/' or t3.id like '%${params.query}%' ESCAPE '/')",
            " and t3.red_warning_check_status != 0) tt",
            " order by tt.warnTime desc, tt.warnNo",
            " <if test='params.limit!=null and params.limit!= -1'>",
            " limit #{params.limit}",
            " </if>",
            " <if test='params.offset!=null'>",
            " offset #{params.offset}",
            " </if>",
            "</script>"})
    List<WarnInformation> getWarns(@Param("params") Parameters params,@Param("tenantId")String tenantId);

    @Select("select id, task_execute_id, general_warning_check_status, orange_warning_check_status, red_warning_check_status from data_quality_task_rule_execute where id = #{id}")
    RuleExecute getRuleExecute(@Param("id")String id);

    @Select("select id, task_execute_id, general_warning_check_status, orange_warning_check_status, red_warning_check_status from data_quality_task_rule_execute where task_execute_id = #{taskExecuteId}")
    List<RuleExecute> getRuleExecutes(@Param("taskExecuteId")String taskExecuteId);
}
