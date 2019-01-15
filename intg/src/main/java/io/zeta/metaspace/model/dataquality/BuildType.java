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
 * @date 2019/1/12 10:30
 */
package io.zeta.metaspace.model.dataquality;

/*
 * @description
 * @author sunhaoning
 * @date 2019/1/12 10:30
 */
public enum BuildType {
    CYCLE(0,"周期生成") , ONCE(1,"单次生成");
    public Integer code;
    public String desc;

    BuildType(Integer code, String desc) {
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

    public static BuildType getBuildTypeByCode(Integer code) {
        BuildType defaultBuildType = BuildType.ONCE;
        for(BuildType bt : BuildType.values()) {
            if(bt.code == code)
                return bt;
        }
        return defaultBuildType;
    }

    public static String getDescByCode(Integer code) {
        return getBuildTypeByCode(code).desc;
    }
}
