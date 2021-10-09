package io.zeta.metaspace.model.global;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserPermissionPO {
    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 账户
     */
    private String account;

    /**
     * 是否全局权限
     */
    private Boolean permissions;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
}
