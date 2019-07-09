package io.zeta.metaspace.model.operatelog;

public enum OperateResultEnum {

    SUCCESS("success", "成功"),
    FAILED("failed", "系统异常"),
    UNAUTHORIZED("unauthorized", "越权访问");

    private String en;
    private String cn;

    OperateResultEnum(String en, String cn) {
        this.en = en;
        this.cn = cn;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public static OperateResultEnum of(String en) {
        for (OperateResultEnum operateType : OperateResultEnum.values()) {
            if (en.equalsIgnoreCase(operateType.getEn())) {
                return operateType;
            }
        }
        throw new RuntimeException("找不到对应的操作结果:" + en);
    }

    public String en2cn(String en) {
        return of(en).getCn();
    }
}
