package io.zeta.metaspace.web.dao.dataquality;

import io.zeta.metaspace.model.dataquality2.Warning;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface WarningDAO {

    /**
     * 任务告警记录
     */
    @Select({" select b.id as taskExecuteId,b.warning_status as warningStatus,a.name as taskName,b.red_warning_count as redWarningCount,a.\"level\" as taskLevel ",
             " from data_quality_task a inner join data_quality_task_execute b on a.id=b.task_id where a.delete=false and b.warning_status=#{warningStatus} and a.tenantid=#{tenantId} order by b.execute_time"})
    public List<Warning> taskWaringLog(@Param("warningStatus") Integer warningStatus,@Param("tenantId") String tenantId);


    /**
     * 任务规则告警记录
     */
    @Select({" select a.create_time,b.yellow_warning_groupid as yellowWarningGroupId,b.redWarningGroupId ",
             " from data_quality_task_rule_execute a inner join data_quality_sub_task_rule b on a.subtask_rule_id=b.id ",
             " inner join data_quality_task_execute c a.task_id=c.task_id " +
             " where c.id=#{taskExecuteId} and a.waring_type!=0 ",
             " order by a.create_time desc "})
    public List<Map<String, Object>> taskRuleWarningLog(@Param("taskExecuteId") String taskExecuteId);


    @Insert({" <script>",
             " update data_quality_task_execute set warning_status=2,closer=#{closer},close_time=now() where id in ",
             " <foreach collection='idList' item='id' index='index' open='(' close=')' separator=','>",
             " #{id}",
             " </foreach>",
             " </script>"})
    public void closeByIdList(@Param("idList") List<String> idList, String closer);

}
