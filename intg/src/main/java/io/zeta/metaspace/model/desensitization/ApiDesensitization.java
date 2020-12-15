package io.zeta.metaspace.model.desensitization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiDesensitization {
    private String field;
    private String ruleId;
    private String ruleName;
}
