package io.zeta.metaspace.web.dao.dataquality;

import org.apache.ibatis.annotations.Param;

/**
 * @author 周磊
 */
public interface TaskExecuteDAO {
    
    /**
     * 根据任务执行记录ID查询所属的任务ID
     *
     * @param id 执行记录ID
     * @return 任务ID
     */
    String queryTaskIdByExecuteId(@Param("id") String id);
    
    /**
     * 查询任务执行记录的状态
     *
     * @param id 执行记录ID
     * @return 执行状态 1-执行中,2-成功,3-失败,0-未执行,4-取消
     */
    Integer queryTaskExecuteStatus(@Param("id") String id);
}