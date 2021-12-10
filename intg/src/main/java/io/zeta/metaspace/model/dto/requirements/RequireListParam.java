package io.zeta.metaspace.model.dto.requirements;

import io.zeta.metaspace.model.dto.Page;
import lombok.Data;

@Data
public class RequireListParam extends Page {
    private Integer status;

    private Integer type;

    private String order;
}
