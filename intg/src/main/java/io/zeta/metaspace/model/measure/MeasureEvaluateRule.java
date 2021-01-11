package io.zeta.metaspace.model.measure;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MeasureEvaluateRule {
    public List<MeasureRule> rules;

    public MeasureEvaluateRule(List<MeasureRule> rules) {
        this.rules = rules;
    }
}
