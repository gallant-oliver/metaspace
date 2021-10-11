package io.zeta.metaspace.model.privilege;

import lombok.Data;

@Data
public class SSOAccount {
    private String accountGuid;
    private String loginEmail;
    private String displayName;
    private String type;
    private String userName;
}
