package io.zeta.metaspace.web.dao.dataquality;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskRuleExecuteDAO {
    
    /**
     * 根据任务执行记录ID查询执行记录日志
     *
     * @param executionId 任务执行记录ID 非空
     * @return 执行记录日志集合
     */
    List<String> queryExecutionLogByExecutionId(@Param("executionId") String executionId);
}