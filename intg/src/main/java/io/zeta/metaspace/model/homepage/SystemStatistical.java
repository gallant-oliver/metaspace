package io.zeta.metaspace.model.homepage;

public enum SystemStatistical {
    DB_TOTAL(1,"数据库总量"),TB_TOTAL(2,"数据表总量"),BUSINESS_TOTAL(3,"业务对象总量"),BUSINESSE_ADD(4,"业务对象已补充"),BUSINESSE_NO_ADD(5,"业务对象未补充");
    private int code;
    private String desc;

    SystemStatistical(int code, String desc) {
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
    public SystemStatistical getSystemRoleByCode(int code){
        SystemStatistical systemModule=null;
        for (SystemStatistical module : SystemStatistical.values()) {
            if(module.code==code)
                systemModule=module;
        }
        return systemModule;
    }

}
