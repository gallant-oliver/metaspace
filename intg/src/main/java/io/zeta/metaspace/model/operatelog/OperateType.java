package io.zeta.metaspace.model.operatelog;

import com.google.gson.JsonObject;

public enum OperateType {

    LOGIN("login", "登录"),
    LOGOUT("logout", "登出"),
    INSERT("insert", "新增"),
    QUERY("query", "查询"),
    DELETE("delete", "删除"),
    UPDATE("update", "更新");

    private String en;
    private String cn;

    OperateType(String en, String cn) {
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
        obj.addProperty("en", getEn() );
        obj.addProperty("cn", getCn() );
        return obj;
    }

    public static OperateType of(String en) {
        for (OperateType operateType : OperateType.values()) {
            if (en.equalsIgnoreCase(operateType.getEn())) {
                return operateType;
            }
        }
        throw new RuntimeException("找不到对应的操作类型:" + en);
    }

    public String en2cn(String en) {
        return of(en).getCn();
    }
}
