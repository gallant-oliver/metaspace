package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/9 16:04
 */
@Data
public class RequirementsFeedbackCommit {
    /**
     * 需求id
     */
    private String requirementsId;

    /**
     * 资源类型 1：API 2：中间库 3：消息队列
     */
    private Integer resourceType;

    /**
     * API
     */
    private RequirementsApiCommitDTO api;

    /**
     * 中间库
     */
    private RequirementsDatabaseCommitDTO database;

    /**
     * 消息队列
     */
    private RequirementsMqCommitDTO mq;
}
