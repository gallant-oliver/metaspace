package io.zeta.metaspace.model.measure;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class MeasureLivyResult {
    public String id;
    public String appId;
    public Map<String, String> appInfo;
    public List<String> log;
    public String state;


}
