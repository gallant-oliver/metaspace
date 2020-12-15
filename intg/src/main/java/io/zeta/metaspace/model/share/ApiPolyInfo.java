package io.zeta.metaspace.model.share;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

/**
 * 用于API脱敏和黑白名单策略详情展示
 */
@Data
public class ApiPolyInfo {
    private String guid;
    private String name;
    private String version;
    private String description;
    private String projectId;
    private String projectName;
    private String status;
    private String creatorName;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    //脱敏字段
    private String desensitizationFields;
    @JsonIgnore
    private int total;
}
