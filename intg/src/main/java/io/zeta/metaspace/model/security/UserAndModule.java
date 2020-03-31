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

package io.zeta.metaspace.model.security;

import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/25 15:53
 */
public class UserAndModule {
    private String userName;
    private String email;
    private String accountGuid;

    public String getAccountGuid() {
        return accountGuid;
    }

    public void setAccountGuid(String accountGuid) {
        this.accountGuid = accountGuid;
    }

    private List<RoleResource> toolRoleResources;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<RoleResource> getToolRoleResources() {
        return toolRoleResources;
    }

    public void setToolRoleResources(List<RoleResource> toolRoleResources) {
        this.toolRoleResources = toolRoleResources;
    }
}
