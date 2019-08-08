package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.TaskErrorHeader;
import io.zeta.metaspace.model.dataquality2.TaskWarningHeader;
import io.zeta.metaspace.model.dataquality2.WarningGroup;
import io.zeta.metaspace.model.metadata.Parameters;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface WarningGroupDAO {

    @Insert({" insert into warning_group(id,name,type,contacts,category_id,description,create_time,update_time,creator,delete) ",
             " values(#{id},#{name},#{type},#{contacts},#{categoryId},#{description},#{createTime},#{updateTime},#{creator},#{delete})"})
    public int insert(WarningGroup warningGroup);

    @Insert(" update warning_group set name=#{name},type=#{type},contacts=#{contacts},category_id=#{categoryId},description=#{description},update_time=#{updateTime}")
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


    @Select({" <script>",
             " select data_quality_task.id as taskId, data_quality_task_execute.id as executionId, data_quality_task.name as taskName,data_quality_task_execute.number,warning_status as warningStatus,orange_warning_count as orangeWarningCount,",
             " red_warning_count as redWarningCount,execute_time as executionTime from data_quality_task_execute join data_quality_task on data_quality_task_execute.task_id=data_quality_task.id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " order by executionTime desc",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<TaskWarningHeader> getWarningList(@Param("params") Parameters params);

    @Select({" <script>",
             " select data_quality_task.id as taskId,data_quality_task_execute.id as executionId,data_quality_task.name as taskName,data_quality_task_execute.number,error_status as errorStatus,",
             " error_msg as errorMsg,execute_time as executionTime from data_quality_task_execute join data_quality_task on data_quality_task_execute.task_id=data_quality_task.id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " and error_status!=0",
             " order by executionTime desc",
             " <if test='params.limit!=null and params.limit!= -1'>",
             " limit #{params.limit}",
             " </if>",
             " <if test='params.offset!=null'>",
             " offset #{params.offset}",
             " </if>",
             " </script>"})
    public List<TaskErrorHeader> getErrorWarningList(@Param("params") Parameters params);

    @Select({" <script>",
             " select count(*)",
             " from data_quality_task_execute join data_quality_task on data_quality_task.id=data_quality_task_execute.task_id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " </script>"})
    public Long countWarning(@Param("params") Parameters params);

    @Select({" <script>",
             " select count(*)",
             " from data_quality_task_execute join data_quality_task on data_quality_task.id=data_quality_task_execute.task_id",
             " where (data_quality_task.name like '%${params.query}%' ESCAPE '/' or data_quality_task_execute.number like '%${params.query}%' ESCAPE '/')",
             " and error_status!=0",
             " </script>"})
    public Long countError(@Param("params") Parameters params);

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
             " where task_execute_id in",
             " <foreach collection='executionIdList' item='executionId' index='index' open='(' close=')' separator=','>",
             " #{executionId}",
             " </foreach>",
             " </script>"})
    public int closeAllTaskRuleExecutionWarning(@Param("warningType")Integer warningType, @Param("executionIdList")List<String> executionIdList);

    @Update({" <script>",
             " update data_quality_task_rule_execute set",
             " <if test='warningType==0'>",
             " warning_status=2",
             " </if>",
             " <if test='warningType==1'>",
             " error_status=2",
             " </if>",
             " where id in",
             " <foreach collection='executionRuleIdList' item='executionRuleId' index='index' open='(' close=')' separator=','>",
             " #{executionRuleId}",
             " </foreach>",
             " </script>"})
    public int closeTaskRuleExecutionWarning(@Param("warningType")Integer warningType, @Param("executionRuleIdList")List<String> executionIdList);

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

}
