package io.zeta.metaspace.model.security;

public class TenantExtInfo extends Tenant{
    private String bizTreeId;

    public String getBizTreeId() {
        return bizTreeId;
    }

    public void setBizTreeId(String bizTreeId) {
        this.bizTreeId = bizTreeId;
    }
}
