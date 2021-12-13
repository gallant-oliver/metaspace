package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @Author wuyongliang
 * @Date 2021/12/8 16:36
 * @Description 反馈结果-消息队列
 */

@Data
public class RequirementsMqDetailDTO {
    /**
     * 需求guid
     */
    private String guid;

    /**
     * 所属租户
     */
    private String tenant;

    /**
     * 消息队列英文名称
     */
    private String mqNameEn;

    /**
     * 消息队列中文名称
     */
    private String mqNameCh;

    /**
     * 格式
     */
    private String format;

    /**
     * 状态
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
