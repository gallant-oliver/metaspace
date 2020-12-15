package io.zeta.metaspace.model.share;

import io.zeta.metaspace.model.desensitization.ApiDesensitization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * API  策略实体，包括黑白名单测试和脱敏策略
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiPolyEntity {
    private List<ApiDesensitization> desensitization;
}
