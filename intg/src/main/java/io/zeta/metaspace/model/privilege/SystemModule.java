package io.zeta.metaspace.model.privilege;

public enum SystemModule {
    TECHNICAL_CHECK(1,"技术数据查看"),BUSINESSE_CHECK(2,"业务对象查看"),TECHNICAL_OPERATE(3,"技术信息操作"),BUSINESSE_OPERATE(4,"业务信息操作"),BUSINESSE_MANAGE(5,"业务对象管理"),PRIVILEGE_MANAGE(6,"权限模块管理");
    private int code;
    private String desc;

    SystemModule(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getDescByCode(int code){
        return getSystemRoleByCode(code).desc;
    }
    public SystemModule getSystemRoleByCode(int code){
        SystemModule systemModule=null;
        for (SystemModule module : SystemModule.values()) {
            if(module.code==code)
                systemModule=module;
        }
        return systemModule;
    }

}
