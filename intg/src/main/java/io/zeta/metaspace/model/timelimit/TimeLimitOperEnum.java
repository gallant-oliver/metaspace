package io.zeta.metaspace.model.timelimit;

public enum TimeLimitOperEnum {

    PUBLISH("1", "发布"),
    CANCEL("2", "下线"),
    DELETE("3", "删除");


    private String name;

    public String code;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    TimeLimitOperEnum(String name, String str) {
        this.name = name;
        this.code = str;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static TimeLimitOperEnum getOpreateEnum(String name) {
        for (TimeLimitOperEnum value : TimeLimitOperEnum.values()) {
            if (value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}



