package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

@Data
public class RuleStatistics {
    private String ruleName;

    private Integer count;

    private String ruleType;
}
