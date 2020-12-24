package io.zeta.metaspace.model.measure;

import lombok.Data;

@Data
public class MeasureRuleOut {
    public Type type;
    public String name;
    public String flatten = "array";

    public MeasureRuleOut(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public MeasureRuleOut(Type type, String name, String flatten) {
        this.type = type;
        this.name = name;
        this.flatten = flatten;
    }

    public enum Type {
        METRIC, RECORD
    }
}
