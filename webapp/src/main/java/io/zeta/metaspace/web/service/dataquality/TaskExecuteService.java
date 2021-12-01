package io.zeta.metaspace.web.service.dataquality;

import io.zeta.metaspace.model.enums.TaskExecuteStatus;
import io.zeta.metaspace.web.dao.dataquality.TaskExecuteDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 数据质量-任务管理-任务执行记录
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-29
 */
@Slf4j
@Service
public class TaskExecuteService {
    @Autowired
    private TaskExecuteDAO taskExecuteDAO;
    
    public String queryTaskIdByExecuteId(String instanceId) {
        return taskExecuteDAO.queryTaskIdByExecuteId(instanceId);
    }
    
    public TaskExecuteStatus queryTaskExecuteStatus(String instanceId) {
        Integer status = Optional.ofNullable(taskExecuteDAO.queryTaskExecuteStatus(instanceId))
                .orElseThrow(() -> new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,
                        String.format("执行记录 %s 无效!", instanceId)));
        return TaskExecuteStatus.parseByCode(status);
    }
}
