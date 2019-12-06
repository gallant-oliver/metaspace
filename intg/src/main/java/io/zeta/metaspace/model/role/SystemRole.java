package io.zeta.metaspace.model.role;

public enum SystemRole {
    ADMIN("1","平台管理员"),MANAGE("3","管理"),BUSINESSE("4","业务"),TECHNIQUE("5","技术"), BUSINESSE_CATEGORY("6","业务目录管理员"),TECHNIQUE_CATEGORY("7","技术目录管理员"),;
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
        SystemRole systemRole=null;
        for (SystemRole role : SystemRole.values()) {
            if(role.code.equals(code))
                systemRole=role;
        }
        return systemRole;
    }

}
