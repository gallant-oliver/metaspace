package io.zeta.metaspace.model.global;

import lombok.Data;
import org.apache.atlas.model.metadata.CategoryEntityV2;

import java.util.List;

@Data
public class CategoryGlobal {

    /**
     * 租户id
     */
    private String tenantId;

    private String tenantName;

    /**
     * 目录列表
     */
    private List<CategoryEntityV2> children;
}
