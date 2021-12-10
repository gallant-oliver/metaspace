package io.zeta.metaspace.model.po.requirements;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.enums.ApiProtocol;
import io.zeta.metaspace.model.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementsPO {
    private String guid;
    
    private String name;
    
    private String num;
    /**
     * 资源类型 {@link ResourceType#getCode()}
     */
    private Integer resourceType;
    
    private String version;
    /**
     * {@code this.resourceType == ResourceType.API } 时字段有效;
     * <p>
     * 参数协议 {@link ApiProtocol#name()}
     */
    private String agreement;
    /**
     * {@code this.resourceType == ResourceType.API } 时字段有效;
     * <p>
     * 请求方式: GET/POST
     */
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
    
    @JsonIgnore
    private Long total;

    /**
     * 业务目录ID
     */
    private String businessCategoryId;
}