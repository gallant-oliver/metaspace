package io.zeta.metaspace.model.dto.dataquality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务列表
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TasksDTO {
    private int total;
    private List<TaskDTO> taskList;
}
