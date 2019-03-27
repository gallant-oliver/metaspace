package io.zeta.metaspace.model.privilege;

public enum SystemModule {
    TECHNICAL_CHECK(1,"浏览技术信息"),BUSINESSE_CHECK(2,"浏览业务信息"),TECHNICAL_OPERATE(3,"管理技术目录"),BUSINESSE_OPERATE(4,"管理业务目录")
    ,BUSINESSE_MANAGE(5,"业务对象管理"),PRIVILEGE_MANAGE(6,"权限模块管理"),METADATA_MANAGE(7,"元数据管理"),TECHNICAL_EDIT(8,"编辑技术信息"),BUSINESSE_EDIT(9,"编辑业务目录"),API_CHECK(10,"管理API信息");
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
