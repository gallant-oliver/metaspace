package io.zeta.metaspace.model.dto.requirements;

import io.zeta.metaspace.model.enums.ApiProtocol;
import io.zeta.metaspace.model.enums.ResourceType;
import lombok.Data;

import java.util.List;

/**
 * 需求管理 - 需求
 *
 * @author 周磊
 * @version 1.0
 * @date 2021-12-07
 */
@Data
public class RequirementDTO {
    /**
     * 需求ID
     */
    private String guid;
    /**
     * 业务对象ID
     */
    private String businessId;
    /**
     * 需求关联的数据表ID
     */
    private String tableId;
    /**
     * 数据源ID
     */
    private String sourceId;
    /**
     * 需求关联的数据表所在的租户的ID
     */
    private String tenantId;
    /**
     * 需求名称
     */
    private String name;
    /**
     * 需求编码
     */
    private String num;
    /**
     * 资源类型 {@link ResourceType#getCode()}
     */
    private ResourceType resourceType;
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
    /**
     * 目标字段ID
     */
    private List<String> targetFieldIDs;

    /**
     * 目标字段名称
     */
    private List<String> targetFieldNames;

    /**
     * 过滤字段名称
     */
    private List<String> filterFieldNames;

    /**
     * 过滤字段
     */
    private List<FilterConditionDTO> filterConditions;
    /**
     * 文件原始名称
     */
    private String fileName;
    /**
     * HDFS服务器文件存储路径
     */
    private String filePath;
    /**
     * 描述
     */
    private String description;
}
