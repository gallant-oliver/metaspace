package io.zeta.metaspace.model.approve;


public enum ApproveOperate {

    APPROVE("1","审批") , REJECTED("2","驳回"), CANCEL("3","驳回");
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

    ApproveOperate(String code, String name) {
        this.name = name;
        this.code = code;
    }


    public static ApproveOperate getOprateByCode(String code){
        for (ApproveOperate operate : ApproveOperate.values()) {
            if(operate.code.equals(code)){
                return operate;
            }
        }
        return null;
    }



}
