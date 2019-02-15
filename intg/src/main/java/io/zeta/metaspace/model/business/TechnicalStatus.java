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
 * @date 2019/2/15 9:54
 */
package io.zeta.metaspace.model.business;

public enum TechnicalStatus {

    ADDED(0,"added") , BLANK(1,"blank");
    public Integer code;
    public String desc;

    TechnicalStatus(Integer code, String desc) {
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

    public static TechnicalStatus getTechnicalStatusByCode(Integer code) {
        TechnicalStatus defaultTechnicalStatus = TechnicalStatus.BLANK;
        for(TechnicalStatus ts : TechnicalStatus.values()) {
            if(ts.code == code)
                return ts;
        }
        return defaultTechnicalStatus;
    }

    public static String getDescByCode(Integer code) {
        return getTechnicalStatusByCode(code).desc;
    }

    public static TechnicalStatus getTechnicalStatusByDesc(String desc) {
        TechnicalStatus defaultTechnicalStatus = TechnicalStatus.BLANK;
        for(TechnicalStatus ts : TechnicalStatus.values()) {
            if(ts.desc == desc)
                return ts;
        }
        return defaultTechnicalStatus;
    }

    public static Integer getCodeByDesc(String desc) {
        return getTechnicalStatusByDesc(desc).code;
    }
}
