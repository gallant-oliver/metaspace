package io.zeta.metaspace.model.privilege;

import lombok.Data;

@Data
public class UserPermission {
    private String userId;
    private String username;
    private String account;
    private boolean permissions;
    private String createTime ;
    private int total;
}
