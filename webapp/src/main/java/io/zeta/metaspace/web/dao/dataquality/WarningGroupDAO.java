package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.ErrorInfo;
import io.zeta.metaspace.model.dataquality2.TaskErrorHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.dataquality2.WarningInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

public interface WarningGroupDAO {

    @Insert({" insert into warning_group(id,name,type,contacts,category_id,description,create_time,update_time,creator,delete) ",
             " values(#{id},#{name},#{type},#{contacts},#{categoryId},#{description},#{createTime},#{updateTime},#{creator},#{delete})"})
    public int insert(WarningGroup warningGroup);

    @Insert(" update warning_group set name=#{name},type=#{type},contacts=#{contacts},category_id=#{categoryId},description=#{description},update_time=#{updateTime} where id=#{id}")
    public int update(WarningGroup warningGroup);

    @Select({" select a.id,a.name,a.type,a.contacts,a.category_id as categoryId,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false and a.id=#{id}"})
    public WarningGroup getById(@Param("id") String id);

    @Select({" select a.id,a.name,a.type,a.contacts,a.category_id as categoryId,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false and a.name = #{name} "})
    public WarningGroup getByName(@Param("name") String name);

    @Select("update warning_group set delete=true where id=#{id}")
    public void deleteById(@Param("id") String id);

    @Insert({" <script>",
             " update warning_group set delete=true where id in ",
             " <foreach collection='idList' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public void deleteByIdList(@Param("idList") List<String> idList);


    @Select({"<script>",
             " select a.id,a.name,a.type,a.contacts,a.category_id as categoryId,a.description,a.create_time as createTime,a.update_time as updateTime,b.username as creator,a.delete ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false ",
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
    public List<WarningGroup> search(@Param("params") Parameters params);

    @Select({"<script>",
             " select count(1) ",
             " from warning_group a inner join users b on a.creator=b.userid where a.delete=false ",
             " <if test=\"query != null and query!=''\">",
             " and (a.name like '%${query}%' ESCAPE '/' ) ",
             " </if>",
             " </script>"})
    public long countBySearch(@Param("query") String query);


    @Select({"<script>",
             " select id,name,description,warning_group.create_time as createTime,category_id as categoryId,contacts,users.username as creator,type",
             " from warning_group join users on users.userid=creator where delete=false",
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
    public List<WarningGroup> getWarningGroup(@Param("params") Parameters params);

    @Select({"<script>",
             " select count(*) from",
             " warning_group where delete=false",
             " <if test=\"params.query != null and params.query!=''\">",
             " and (a.name like '%${params.query}%' ESCAPE '/' or  category.name like '%${params.query}%' ESCAPE '/')",
             " </if>",
             " </script>"})
    public long countWarningGroup(@Param("params") Parameters params);


    @Select({" <script>",
             " select data_quality_task.id as taskId, data_quality_task_execute.id as executionId, data_quality_task.name as taskName,data_quality_task_execute.number,warning_status as warningStatus,orange_warning_count as orangeWarningCount,",
             " red_warning_count as redWarningCount,execute_time as executionTime from data_quality_task_execute join data_quality_task on data_quality_task_execute.task_id=data_quality_task.id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
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
    public List<TaskWarningHeader> getWarningList(@Param("warningType")Integer warningType, @Param("params") Parameters params);

    @Select({" <script>",
             " select data_quality_task.id as taskId,data_quality_task_execute.id as executionId,data_quality_task.name as taskName,data_quality_task_execute.number,error_status as errorStatus,",
             " error_msg as errorMsg,execute_time as executionTime from data_quality_task_execute join data_quality_task on data_quality_task_execute.task_id=data_quality_task.id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
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
    public List<TaskErrorHeader> getErrorWarningList(@Param("errorType")Integer errorType, @Param("params") Parameters params);

    @Select({" <script>",
             " select count(*)",
             " from data_quality_task_execute join data_quality_task on data_quality_task.id=data_quality_task_execute.task_id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " <if test='warningType==1'>",
             " and warning_status=1",
             " </if>",
             " <if test='warningType==2'>",
             " and warning_status=2",
             " </if>",
             " <if test='warningType==0'>",
             " and warning_status!=0",
             " </if>",
             " </script>"})
    public Long countWarning(@Param("warningType")Integer warningType,@Param("params") Parameters params);

    @Select({" <script>",
             " select count(*)",
             " from data_quality_task_execute join data_quality_task on data_quality_task.id=data_quality_task_execute.task_id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " <if test='errorType==1'>",
             " and error_status=1",
             " </if>",
             " <if test='errorType==2'>",
             " and error_status=2",
             " </if>",
             " <if test='errorType==0'>",
             " and error_status!=0",
             " </if>",
             " </script>"})
    public Long countError(@Param("errorType")Integer errorType, @Param("params") Parameters params);

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
             " select a.objectId,a.result,data_quality_rule.name as ruleName,data_quality_rule.check_threshold_unit as unit,0 as warningType from",
             " (select data_quality_sub_task_rule.ruleid as ruleId,data_quality_task_rule_execute.subtask_object_id as objectId,",
             " data_quality_task_rule_execute.result from data_quality_task_rule_execute ",
             " join data_quality_sub_task_rule on data_quality_sub_task_rule.id=data_quality_task_rule_execute.subtask_rule_id",
             " where data_quality_task_rule_execute.subtask_id=#{subTaskId} and orange_warning_check_status=1 and task_execute_id=#{taskExecutionId}) a",
             " join data_quality_rule on data_quality_rule.id=a.ruleid",
             " UNION ALL",
             " select a.objectId,a.result,data_quality_rule.name as ruleName,data_quality_rule.check_threshold_unit as unit,1 as warningType from",
             " (select data_quality_sub_task_rule.ruleid as ruleId,data_quality_task_rule_execute.subtask_object_id as objectId,",
             " data_quality_task_rule_execute.result from data_quality_task_rule_execute ",
             " join data_quality_sub_task_rule on data_quality_sub_task_rule.id=data_quality_task_rule_execute.subtask_rule_id",
             " where data_quality_task_rule_execute.subtask_id=#{subTaskId} and red_warning_check_status=1 and task_execute_id=#{taskExecutionId}) a",
             " join data_quality_rule on data_quality_rule.id=a.ruleid",
             " </script>"})
    public List<WarningInfo.SubTaskRuleWarning> getSubTaskRuleWarning(@Param("taskExecutionId")String taskExecutionId, @Param("subTaskId")String subTaskId);


    @Select("select task_execute_id as taskExecuteId,create_time as executeTime,error_msg as errorMessage from data_quality_task_rule_execute where task_execute_id=#{executionId} and error_status!=0  ORDER BY create_time desc limit 1;")
    public ErrorInfo getErrorInfo(@Param("executionId")String executionId);




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

}
