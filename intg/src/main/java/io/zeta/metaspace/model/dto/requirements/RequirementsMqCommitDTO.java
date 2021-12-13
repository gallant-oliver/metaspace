package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/9 15:45
 */
@Data
public class RequirementsMqCommitDTO {
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
     * 状态 1-上线  2-下线
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}
