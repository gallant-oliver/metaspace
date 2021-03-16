package io.zeta.metaspace.model.po.indices;

/**
 * 派生指标与复合指标关系
 */
public class IndexDeriveCompositeRelationPO {

    /**
     * 派生指标id
     */
    private String deriveIndexId;
    /**
     * 复合指标id
     */
    private String compositeIndexId;

    public String getDeriveIndexId() {
        return deriveIndexId;
    }

    public void setDeriveIndexId(String deriveIndexId) {
        this.deriveIndexId = deriveIndexId;
    }

    public String getCompositeIndexId() {
        return compositeIndexId;
    }

    public void setCompositeIndexId(String compositeIndexId) {
        this.compositeIndexId = compositeIndexId;
    }
}
