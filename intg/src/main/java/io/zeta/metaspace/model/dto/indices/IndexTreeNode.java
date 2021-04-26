package io.zeta.metaspace.model.dto.indices;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class IndexTreeNode {

    private IndexLinkEntity node; //指标节点

    private List<IndexLinkEntity> parentNode = new LinkedList<>();

    private List<IndexLinkEntity> childNode = new LinkedList<>();



}
