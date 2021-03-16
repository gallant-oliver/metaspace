package io.zeta.metaspace.model.approve;

public enum ApproveType {

    PUBLISH("1","发布") , OFFLINE("2","下线");
    public String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name;

    ApproveType(String code, String name) {
        this.name = name;
        this.code = code;
    }

    public static ApproveType getApproveTypeByCode(String code){
        for (ApproveType type : ApproveType.values()) {
            if(type.code.equals(code)){
                return type;
            }
        }
        return null;
    }




}
