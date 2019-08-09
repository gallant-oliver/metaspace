package io.zeta.metaspace.model.operatelog;

import com.google.gson.JsonObject;

public enum OperateTypeEnum {

    LOGIN("login", "登录"),
    LOGOUT("logout", "登出"),
    INSERT("insert", "新增"),
    /**
     * 查询在权限访问filter里插入
     */
//    QUERY("query", "查询"),
    DELETE("delete", "删除"),
    UPDATE("update", "更新"),
    UNKOWN("unkown", "未知");

    private String en;
    private String cn;

    OperateTypeEnum(String en, String cn) {
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

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("en", getEn());
        obj.addProperty("cn", getCn());
        return obj;
    }

    public static OperateTypeEnum of(String en) {
        for (OperateTypeEnum operateTypeEnum : OperateTypeEnum.values()) {
            if (en.equalsIgnoreCase(operateTypeEnum.getEn())) {
                return operateTypeEnum;
            }
        }
        return UNKOWN;
    }

}
