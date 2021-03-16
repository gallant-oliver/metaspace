package io.zeta.metaspace.model.po.indices;

/**
 * 派生指标域修饰词关系
 */
public class IndexDeriveModifierRelationPO {
    /**
     * 派生指标id
     */
    private String deriveIndexId;
    /**
     * 修饰词id
     */
    private String modifierId;

    public String getDeriveIndexId() {
        return deriveIndexId;
    }

    public void setDeriveIndexId(String deriveIndexId) {
        this.deriveIndexId = deriveIndexId;
    }

    public String getModifierId() {
        return modifierId;
    }

    public void setModifierId(String modifierId) {
        this.modifierId = modifierId;
    }
}
