package org.zeta.metaspace.model.metadata;

import java.io.Serializable;

public class TablePermission implements Serializable {
    //更改表结构，创建分区
    private boolean ALTER ;
    //删除表，或分区
    private boolean DROP ;
    //创建和删除索引
    private boolean INDEX ;
    //锁定表，保证并发
    private boolean LOCK  ;
    //查询表权限
    private boolean SELECT ;
    //为表加载本地数据的权限
    private boolean UPDATE  ;

    public void setALTER(boolean ALTER) {
        this.ALTER = ALTER;
    }

    public boolean isALTER() {
        return ALTER;
    }

    public void setDROP(boolean DROP) {
        this.DROP = DROP;
    }

    public boolean isDROP() {
        return DROP;
    }

    public void setINDEX(boolean INDEX) {
        this.INDEX = INDEX;
    }

    public boolean isINDEX() {
        return INDEX;
    }

    public void setLOCK(boolean LOCK) {
        this.LOCK = LOCK;
    }

    public boolean isLOCK() {
        return LOCK;
    }

    public void setSELECT(boolean SELECT) {
        this.SELECT = SELECT;
    }

    public boolean isSELECT() {
        return SELECT;
    }

    public void setUPDATE(boolean UPDATE) {
        this.UPDATE = UPDATE;
    }

    public boolean isUPDATE() {
        return UPDATE;
    }
}
