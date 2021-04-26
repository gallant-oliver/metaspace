package io.zeta.metaspace.model.dto.indices;

import lombok.Data;

import java.util.Collections;
import java.util.List;


@Data
public class IndexLinkDto {

    private List<IndexLinkEntity> nodes = Collections.emptyList();

    private List<IndexLinkRelation> relations = Collections.emptyList();

}
