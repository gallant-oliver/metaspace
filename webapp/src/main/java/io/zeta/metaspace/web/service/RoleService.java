package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.PathParam;
import java.util.List;
import java.util.UUID;

@Service
public class RoleService {
    @Autowired
    RoleDAO roleDAO;
    public String addRole(Role role) {
        String now = DateUtils.getNow();
        role.setRoleId(UUID.randomUUID().toString());
        role.setCreateTime(now);
        role.setUpdateTime(now);
        role.setStatus(0);
        roleDAO.addRoles(role);
        return "success";
    }
    public String updateRoleStatus(String roleId,int status){
        roleDAO.updateRoleStatus(roleId,status);
        return "success";
    }
    public String deleteRole(String roleId){
        roleDAO.deleteRole(roleId);
        return "success";
    }
    public PageResult<User> getUsers(String roleId, String query, long offset, long limit){
        PageResult<User> userPageResult = new PageResult<>();
        List<User> users = roleDAO.getUsers(roleId, query, offset, limit);
        userPageResult.setLists(users);
        userPageResult.setOffset(offset);
        userPageResult.setSum(users.size());
        return userPageResult;
    }
}
