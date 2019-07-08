package io.zeta.metaspace.model.operatelog;

import com.google.gson.JsonObject;

public enum OperateResult {

    LOGIN("success", "成功"),
    LOGOUT("failed", "系统异常"),
    QUERY("noPermission", "越权访问");

    private String en;
    private String cn;

    OperateResult(String en, String cn) {
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

    public static OperateResult of(String en) {
        for (OperateResult operateType : OperateResult.values()) {
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
