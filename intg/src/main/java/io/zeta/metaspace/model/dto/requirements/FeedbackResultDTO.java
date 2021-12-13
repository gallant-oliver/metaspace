package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @Author wuyongliang
 * @Date 2021/12/8 16:27
 * @Description 反馈结果
 */

@Data
public class FeedbackResultDTO {
    /**
     * 资源类型 1：API 2：中间库 3：消息队列
     */
    private Integer resourceType;

    /**
     * 反馈结果-api
     */
    private RequirementsApiDetailDTO api;

    /**
     * 反馈结果-中间库
     */
    private RequirementsDatabaseDetailDTO database;

    /**
     * 反馈结果-消息队列
     */
    private RequirementsMqDetailDTO mq;


    /**
     * 需求处理结果（租户资产下查看反馈结果时需要返回）
     */
    private DealDetailDTO handle;
}
