package io.zeta.metaspace.model.measure;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class Measure {

    public String name;

    @SerializedName("process.type")
    public String  processType = "BATCH";
    public Long timestamp;
    public List<String> sinks = Arrays.asList("CONSOLE", "HDFS");

    @SerializedName("data.sources")
    public List<MeasureDataSource> dataSources;

    @SerializedName("evaluate.rule")
    public MeasureEvaluateRule rule;
}
