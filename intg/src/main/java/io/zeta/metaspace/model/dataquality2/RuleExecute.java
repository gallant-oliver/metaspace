package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

@Data
public class RuleExecute {
    private String id;

    private String taskExecuteId;

    private Integer generalWarningCheckStatus;

    private Integer orangeWarningCheckStatus;

    private Integer redWarningCheckStatus;
}
