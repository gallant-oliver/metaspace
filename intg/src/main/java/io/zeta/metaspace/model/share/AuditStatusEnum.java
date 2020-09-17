package io.zeta.metaspace.model.share;

public enum AuditStatusEnum {
    NEW("NEW", "待处理"),
    CANCEL("CANCEL", "取消"),
    AGREE("AGREE", "同意"),
    DISAGREE("DISAGREE", "不同意");


    private String name;
    private String str;

    AuditStatusEnum(String name, String str) {
        this.name = name;
        this.str = str;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public static AuditStatusEnum getAuditStatusEnum(String name) {
        for (AuditStatusEnum value : AuditStatusEnum.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
