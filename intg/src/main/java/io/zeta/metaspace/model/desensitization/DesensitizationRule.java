package io.zeta.metaspace.model.desensitization;

import lombok.Data;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
import java.util.List;

@Data
public class DesensitizationRule {

    private String id;
    //名称
    private String name;
    //描述
    private String description;
    //脱敏类型
    private DesensitizationAlgorithm type;
    //参数
    private List<String> params;
    //是否启用
    private boolean enable;

    private long total;

    private String creatorId;

    private String creator;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

}
