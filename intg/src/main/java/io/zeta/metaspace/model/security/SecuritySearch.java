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

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/24 16:18
 */
public class SecuritySearch {
    private String tenantId;
    private String toolName;
    private String userName;
    private List<String> existEmails;
    private List<String> emails;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> email) {
        this.emails = email;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getExistEmails() {
        return existEmails;
    }

    public void setExistEmails(List<String> existEmails) {
        this.existEmails = existEmails;
    }

    @Override
    public String toString() {
        return "SecuritySearch{" +
               "tenantId='" + tenantId + '\'' +
               ", toolName='" + toolName + '\'' +
               ", userName='" + userName + '\'' +
               ", existEmails=" + existEmails +
               ", emails=" + emails +
               '}';
    }
}
