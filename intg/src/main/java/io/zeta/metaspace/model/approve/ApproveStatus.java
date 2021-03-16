package io.zeta.metaspace.model.approve;

public enum ApproveStatus {

    FINISH("2","已审批") , WAITING("1","待审批"), REJECTED("3","驳回");
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

    ApproveStatus(String code,String name) {
        this.name = name;
        this.code = code;
    }




}
