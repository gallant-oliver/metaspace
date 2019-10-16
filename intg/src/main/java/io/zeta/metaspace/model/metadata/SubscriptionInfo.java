package io.zeta.metaspace.model.metadata;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class SubscriptionInfo {

    private String userId;
    private String tableGuid;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    public SubscriptionInfo(String userId, String tableGuid, Timestamp createTime) {
        this.userId = userId;
        this.tableGuid = tableGuid;
        this.createTime = createTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
