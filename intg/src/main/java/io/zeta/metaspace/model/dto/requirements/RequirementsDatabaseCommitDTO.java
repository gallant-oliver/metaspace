package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/9 15:41
 */
@Data
public class RequirementsDatabaseCommitDTO {

    /**
     * 中间库类型
     */
    private String middleType;

    /**
     * 数据库名称
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
     * 状态 1-上线  2-下线
     */
    private Integer status;

    /**
     * 描述
     */
    private String description;
}
