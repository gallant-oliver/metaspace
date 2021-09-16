package io.zeta.metaspace.model.table.column.tag;

import lombok.Data;

import java.util.List;

@Data
public class DeleteRelationRequest {

    private String columnId;

    private String tagId;
}
