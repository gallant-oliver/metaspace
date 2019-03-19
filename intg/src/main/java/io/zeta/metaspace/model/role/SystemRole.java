package io.zeta.metaspace.model.role;

public enum SystemRole {
    ADMIN("1","平台管理员"),GUEST("2","访客"),BUSINESSE("3","业务"),TECHNIQUE("4","技术"),MANAGE("5","管理");
    private String code;
    private String desc;
    public String getCode() {
        return code;
    }

    SystemRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getDescByCode(String code){
        return getSystemRoleByCode(code).desc;
    }
    public SystemRole getSystemRoleByCode(String code){
        SystemRole systemRole=SystemRole.GUEST;
        for (SystemRole role : SystemRole.values()) {
            if(role.code.equals(code))
                systemRole=role;
        }
        return systemRole;
    }

}
