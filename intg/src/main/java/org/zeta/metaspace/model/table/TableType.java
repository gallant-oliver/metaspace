// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package org.zeta.metaspace.model.table;

public enum TableType {

    EXTERNAL("EXTERNAL", 1),
    INTERNAL("", 2);


    private String literal;
    private int code;

    TableType(String literal, int code) {
        this.literal = literal;
        this.code = code;
    }

    public boolean isExternal() {
        return this == EXTERNAL;
    }

    public static TableType of(int code) {
        switch (code) {
            case 1:
                return EXTERNAL;
            case 2:
                return INTERNAL;
            default:
                throw new RuntimeException("code " + code + " is not exist");
        }
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
