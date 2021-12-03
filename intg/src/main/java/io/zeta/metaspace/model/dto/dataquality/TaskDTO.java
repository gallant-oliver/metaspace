package io.zeta.metaspace.model.dto.dataquality;

import lombok.Builder;
import lombok.Data;

/**
 * 任务调度系统对接 任务管理 - 任务
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-29
 */
@Data
@Builder
public class TaskDTO {
    private String taskId;
    private String taskName;
}
