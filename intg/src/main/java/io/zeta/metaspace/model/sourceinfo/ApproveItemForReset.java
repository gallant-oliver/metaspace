package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

@Data
public class ApproveItemForReset {
    private String objectId;

    private int version;
}
