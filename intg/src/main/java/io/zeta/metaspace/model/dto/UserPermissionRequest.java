package io.zeta.metaspace.model.dto;

import lombok.Data;

@Data
public class UserPermissionRequest {
    private String userId;
    private String username;
    private String account;
}
