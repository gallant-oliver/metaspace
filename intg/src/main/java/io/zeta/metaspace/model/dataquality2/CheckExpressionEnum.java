package io.zeta.metaspace.model.dataquality2;

public enum CheckExpressionEnum {
    EQ(0, "等于"),
    NEQ(1, "不等于"),
    GT(2, "大于"),
    GTE(3, "大于等于"),
    LT(4, "小于"),
    LTE(5, "小于等于"),
    ;


    public Integer code;
    public String desc;

    CheckExpressionEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static CheckExpressionEnum getRuleCheckTypeByCode(Integer code) {
        for (CheckExpressionEnum ce : CheckExpressionEnum.values()) {
            if (ce.code.equals(code)) {
                return ce;
            }
        }
        throw new RuntimeException("没有该checkExpression" + code);
    }

    public static String getDescByCode(Integer code) {
        return getRuleCheckTypeByCode(code).desc;
    }

}
