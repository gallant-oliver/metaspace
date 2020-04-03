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

package io.zeta.metaspace.model.usergroup;

/**
 * @author lixiang03
 * @Data 2020/3/31 11:00
 */
public enum UserPrivilegeDataSource {
    READ("READ","查看","r"),
    WRITE("WRITE","编辑","w"),
    MANAGER("MANAGER","管理者","w"),
    NOPROVILEGE("NOPROVILEGE","无权限",null);
    UserPrivilegeDataSource(String PrivilegeCore, String PrivilegeName,String privilege) {
        this.PrivilegeName = PrivilegeName;
        this.PrivilegeCore = PrivilegeCore;
        this.Privilege = privilege;
    }

    private String PrivilegeName;
    private String PrivilegeCore;
    private String  Privilege;
    public String getPrivilegeName() {
        return PrivilegeName;
    }

    public String getPrivilegeCore() {
        return PrivilegeCore;
    }

    public String getPrivilege() {
        return Privilege;
    }

    public static UserPrivilegeDataSource getUserPrivilegeProjectByName(String PrivilegeCore){
        for (UserPrivilegeDataSource module: UserPrivilegeDataSource.values()){
            if (module.getPrivilegeCore().equalsIgnoreCase(PrivilegeCore)){
                return module;
            }
        }
        return null;
    }
}
