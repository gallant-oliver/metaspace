package io.zeta.metaspace.model.dataquality;

public enum DataType {
    NUMERIC(1,"数值型"),UNNUMERIC(2,"非数值型");
    int code;
    String desc;

    DataType(int code, String desc) {
        this.code=code;
        this.desc=desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
