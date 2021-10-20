package io.zeta.metaspace.model.usergroup;

import java.util.List;

public class TenantGroup {
    private String tenantId;
    private List<String> groupList;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }
}
