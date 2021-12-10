package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @Author wuyongliang
 * @Date 2021/12/8 16:30
 * @Description 反馈结果-中间库
 */

@Data
public class RequirementsDatabaseDetailDTO {
    /**
     * 需求guid
     */
    private String guid;

    /**
     * 所属租户
     */
    private String tenant;

    /**
     * 中间库类型
     */
    private String middleType;

    /**
     * 中间库类型
     */
    private String database;

    /**
     * 数据表英文名称
     */
    private String tableNameEn;

    /**
     * 数据表中文名称
     */
    private String tableNameCh;

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
