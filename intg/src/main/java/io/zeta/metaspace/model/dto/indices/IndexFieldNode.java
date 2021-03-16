package io.zeta.metaspace.model.dto.indices;

import org.apache.atlas.model.metadata.CategoryEntityV2;

import java.util.List;

public class IndexFieldNode {

    private IndexFieldNode parentNode;
    private IndexFieldNode preNode;
    private CategoryEntityV2 current;
    private IndexFieldNode nextNode;
    private List<IndexFieldNode> childNodes;
    private boolean add;
    private String code;

    public IndexFieldNode(IndexFieldNode parentNode, IndexFieldNode preNode, CategoryEntityV2 current, IndexFieldNode nextNode, List<IndexFieldNode> childNodes, boolean add, String code) {
        this.parentNode = parentNode;
        this.preNode = preNode;
        this.current = current;
        this.nextNode = nextNode;
        this.childNodes = childNodes;
        this.add = add;
        this.code=code;
    }

    public IndexFieldNode() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isAdd() {
        return add;
    }

    public void setAdd(boolean add) {
        this.add = add;
    }

    public IndexFieldNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(IndexFieldNode parentNode) {
        this.parentNode = parentNode;
    }

    public IndexFieldNode getPreNode() {
        return preNode;
    }

    public void setPreNode(IndexFieldNode preNode) {
        this.preNode = preNode;
    }

    public IndexFieldNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(IndexFieldNode nextNode) {
        this.nextNode = nextNode;
    }

    public CategoryEntityV2 getCurrent() {
        return current;
    }

    public void setCurrent(CategoryEntityV2 current) {
        this.current = current;
    }

    public List<IndexFieldNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<IndexFieldNode> childNodes) {
        this.childNodes = childNodes;
    }
}
