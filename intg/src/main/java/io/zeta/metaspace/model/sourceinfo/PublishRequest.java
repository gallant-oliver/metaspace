package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

import java.util.List;

@Data
public class PublishRequest {
    private List<String> idList;

    private String approveGroupId;
}
