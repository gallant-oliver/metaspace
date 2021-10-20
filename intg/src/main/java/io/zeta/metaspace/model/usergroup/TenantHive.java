package io.zeta.metaspace.model.usergroup;

public class TenantHive {
    private String tenantId;
    private String hiveDb;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHiveDb() {
        return hiveDb;
    }

    public void setHiveDb(String hiveDb) {
        this.hiveDb = hiveDb;
    }
}
