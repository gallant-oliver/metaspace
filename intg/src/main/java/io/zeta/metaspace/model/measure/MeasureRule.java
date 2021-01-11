package io.zeta.metaspace.model.measure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MeasureRule {
    public String rule;
    @SerializedName("dsl.type")
    public String dslType = "spark-sql";
    @SerializedName("out.dataframe.name")
    public String outName;
    public boolean cache;
    public List<MeasureRuleOut> out;

    public MeasureRule(String rule, String outName, boolean cache, List<MeasureRuleOut> out) {
        this.rule = rule;
        this.outName = outName;
        this.cache = cache;
        this.out = out;
    }
}
