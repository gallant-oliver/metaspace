package io.zeta.metaspace.model.po.requirements;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RequirementsPO {
    private String guid;

    private String name;

    private String num;

    private Integer resourceType;

    private String version;

    private String agreement;

    private String requestMode;

    private String aimingField;

    private String fileName;

    private String filePath;

    private String description;

    private String businessId;

    private String tableId;

    private String sourceId;

    private String tenantId;

    private String creator;
    
    private Integer status;
    
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    
    private Integer delete;
    
    private Long total;
}