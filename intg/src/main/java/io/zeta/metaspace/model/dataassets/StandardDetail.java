package io.zeta.metaspace.model.dataassets;

import io.zeta.metaspace.model.dataquality2.RuleTemplate;
import io.zeta.metaspace.model.datastandard.DataStandard;
import lombok.Data;

import java.util.List;

/**
 * @author w
 */
@Data
public class StandardDetail {
    /**
     * 标准详情
     */
    private DataStandard dataStandard;
    /**
     * 质量规则列表
     */
    private List<RuleTemplate> ruleTemplates;
}
