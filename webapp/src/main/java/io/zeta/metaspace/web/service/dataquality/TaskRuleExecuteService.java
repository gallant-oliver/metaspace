package io.zeta.metaspace.web.service.dataquality;

import io.zeta.metaspace.web.dao.dataquality.TaskRuleExecuteDAO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 数据质量-任务管理-任务规则业务层
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-01
 */
@Slf4j
@Service
public class TaskRuleExecuteService {
    @Autowired
    private TaskRuleExecuteDAO taskRuleExecuteDAO;
    
    public List<String> queryExecutionLog(String executionId) {
        if (StringUtils.isBlank(executionId)) {
            return Collections.emptyList();
        }
        return taskRuleExecuteDAO.queryExecutionLogByExecutionId(executionId);
    }
}
