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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/29 15:48
 */
package io.zeta.metaspace.model.dataquality2;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/29 15:48
 */
public enum  WarningMessageStatus {

    WAITING(1, "待发送"),
    SENDING(2, "发送中"),
    SUCESS(3, "发送成功"),
    FAILURE(4, "发送失败");

    public Integer code;
    public String desc;

    WarningMessageStatus(Integer code, String desc) {
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

    public static WarningMessageStatus getWarningMessageStatusByCode(Integer code) {
        for (WarningMessageStatus rc : WarningMessageStatus.values()) {
            if (rc.code == code) {
                return rc;
            }
        }
        throw new RuntimeException();
    }

    public static String getDescByCode(Integer code) {
        return getWarningMessageStatusByCode(code).desc;
    }
}
