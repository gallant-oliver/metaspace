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
 * @date 2019/2/19 15:29
 */
package io.zeta.metaspace.model.privilege;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 15:29
 */
public enum  SystemPrivilege {
    ADMIN("1","平台管理员"),GUEST("2","访客"),BUSINESSE("3","业务"),TECHNIQUE("4","技术"),MANAGE("5","管理");
    private String code;
    private String desc;
    public String getCode() {
        return code;
    }

    SystemPrivilege(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getDescByCode(String code){
        return getSystemPrivilegeByCode(code).desc;
    }
    public SystemPrivilege getSystemPrivilegeByCode(String code) {
        SystemPrivilege systemPrivilege = SystemPrivilege.GUEST;
        for (SystemPrivilege privilege : SystemPrivilege.values()) {
            if(privilege.code.equals(code))
                systemPrivilege = privilege;
        }
        return systemPrivilege;
    }
}
