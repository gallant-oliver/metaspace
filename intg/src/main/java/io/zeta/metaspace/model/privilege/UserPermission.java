package io.zeta.metaspace.model.privilege;

import lombok.Data;

@Data
public class UserPermission {
    private String id;
    private String userId;
    private String username;
    private String account;
    private String permissions;
    private String createTime ;
    private int total;
}
