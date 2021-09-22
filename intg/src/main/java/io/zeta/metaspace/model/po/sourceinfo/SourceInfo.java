package io.zeta.metaspace.model.po.sourceinfo;

import lombok.Data;

@Data
public class SourceInfo {
    /**
     * 目录ID
     */
    private String categoryId;

    /**
     * 关联数量
     */
    private Integer count;
}
