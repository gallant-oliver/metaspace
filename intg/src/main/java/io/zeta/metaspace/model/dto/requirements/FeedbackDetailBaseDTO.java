package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/10 10:07
 */
@Data
public class FeedbackDetailBaseDTO {
    /**
     * 结果： 同意  拒绝
     */
    private Integer result;

    /**
     * 处理人
     */
    private String user;

    /**
     * 处理说明
     */
    private String description;

    /**
     * API
     */
    private RequirementsApiDetailDTO api;

    /**
     * 中间库
     */
    private RequirementsDatabaseDetailDTO database;

    /**
     * 消息队列
     */
    private RequirementsMqDetailDTO mq;

}
