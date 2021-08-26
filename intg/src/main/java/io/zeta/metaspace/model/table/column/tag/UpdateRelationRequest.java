package io.zeta.metaspace.model.table.column.tag;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRelationRequest {

    private String columnId;

    private List<String> taqIdList;
}
