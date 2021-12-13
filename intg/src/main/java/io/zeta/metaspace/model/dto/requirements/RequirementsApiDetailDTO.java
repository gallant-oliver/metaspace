package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @Author wuyongliang
 * @Date 2021/12/8 16:05
 * @Description 反馈结果-api
 */

@Data
public class RequirementsApiDetailDTO {
    /**
     * 需求guid
     */
    private String guid;

    /**
     * 所属租户
     */
    private String tenant;

    /**
     * api项目
     */
    private String project;

    /**
     * 所属目录
     */
    private String category;

    /**
     * api名称
     */
    private String apiName;

    /**
     * 状态：上线；下线
     */
    private String status;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 描述
     */
    private String description;
}
