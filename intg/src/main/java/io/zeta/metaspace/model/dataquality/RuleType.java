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
 * @date 2019/1/12 11:02
 */
package io.zeta.metaspace.model.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/12 11:02
 */
public enum RuleType {
    TABLE(0,"表") , COLUMN(1,"字段");
    public int code;
    public String desc;

    RuleType(int code, String desc) {
        this.code = code;
        this.desc = desc;
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

    public static RuleType getRuleTypeByCode(Integer code) {
        RuleType defaultRuleType = RuleType.TABLE;
        for(RuleType rt : RuleType.values()) {
            if(rt.code == code)
                return rt;

        }
        return defaultRuleType;
    }

    public static String getDescByCode(Integer code) {
        return getRuleTypeByCode(code).desc;
    }
}
