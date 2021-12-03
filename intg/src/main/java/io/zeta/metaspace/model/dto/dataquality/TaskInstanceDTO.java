package io.zeta.metaspace.model.dto.dataquality;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务执行实例
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskInstanceDTO {
    private String instanceId;
    private String instanceName;
    private String state;
}
