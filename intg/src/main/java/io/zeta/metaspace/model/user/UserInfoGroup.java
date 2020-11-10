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

package io.zeta.metaspace.model.user;

import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/27 15:00
 */
public class UserInfoGroup {
    private UserInfo.User user;
    private List<Group> modules;
    private List<UserInfo.Category> technicalCategory;
    private List<UserInfo.Category> businessCategory;
    private List<UserGroupIdAndName> userGroups;

    public List<UserGroupIdAndName> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroupIdAndName> userGroups) {
        this.userGroups = userGroups;
    }

    public UserInfo.User getUser() {
        return user;
    }

    public void setUser(UserInfo.User user) {
        this.user = user;
    }

    public List<Group> getModules() {
        return modules;
    }

    public void setModules(List<Group> modules) {
        this.modules = modules;
    }

    public List<UserInfo.Category> getTechnicalCategory() {
        return technicalCategory;
    }

    public void setTechnicalCategory(List<UserInfo.Category> technicalCategory) {
        this.technicalCategory = technicalCategory;
    }

    public List<UserInfo.Category> getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(List<UserInfo.Category> businessCategory) {
        this.businessCategory = businessCategory;
    }
    public static class Group{
        private int groupId;
        private String groupName;
        private List<UserInfo.Module> privilege;

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public List<UserInfo.Module> getPrivilege() {
            return privilege;
        }

        public void setPrivilege(List<UserInfo.Module> privilege) {
            this.privilege = privilege;
        }
    }
}
