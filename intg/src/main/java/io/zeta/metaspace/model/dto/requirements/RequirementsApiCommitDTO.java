package io.zeta.metaspace.model.dto.requirements;

import lombok.Data;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/12/9 15:36
 */
@Data
public class RequirementsApiCommitDTO {
    /**
     * api项目Id
     */
    private String projectId;

    /**
     * 目录id
     */
    private String categoryId;

    /**
     * api id
     */
    private String apiId;

    /**
     * 描述
     */
    private String description;
}
