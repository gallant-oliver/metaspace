package io.zeta.metaspace.model.dto.requirements;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RequirementsListDTO {
    private String guid;

    /**
     * 需求名称
     */
    private String name;

    /**
     * 需求编号
     */
    private String num;

    /**
     * 资源类型
     */
    private Integer resourceType;

    /**
     * 资源类型名称
     */
    private String resourceTypeName;

    /**
     * 业务对象ID
     */
    private String businessId;

    /**
     * 数据表ID
     */
    private String tableId;

    /**
     * 数据源ID
     */
    private String sourceId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 所属业务目录路径
     */
    private String categoryPath;
    
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    private Long total;

    /**
     * 业务目录ID
     */
    private String businessCategoryId;
}