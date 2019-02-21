package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.Privilege;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RoleService {
    @Autowired
    RoleDAO roleDAO;
    @Autowired
    PrivilegeDAO privilegeDAO;
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
    @Transactional
    public PageResult<User> getUsers(String roleId, String query, long offset, long limit){
        PageResult<User> userPageResult = new PageResult<>();
        List<User> users = roleDAO.getUsers(roleId, query, offset, limit);
        long usersCount = roleDAO.getUsersCount(roleId, query);
        userPageResult.setLists(users);
        userPageResult.setOffset(offset);
        userPageResult.setSum(users.size());
        userPageResult.setCount(usersCount);
        return userPageResult;
    }
    @Transactional
    public PageResult<Role> getRoles(String query, long offset, long limit){
        PageResult<Role> rolePageResult = new PageResult<>();
        List<Role> roles = roleDAO.getRoles(query, offset, limit);
        long rolesCount = roleDAO.getRolesCount( query);
        rolePageResult.setLists(roles);
        rolePageResult.setOffset(offset);
        rolePageResult.setSum(roles.size());
        rolePageResult.setCount(rolesCount);
        return rolePageResult;
    }
    public String addUsers(String roleId,List<String> users){
        roleDAO.updateUsers(roleId,users);
        return "success";
    }
    public String removeUser( String userId){
        roleDAO.updateUser(SystemRole.GUEST.getCode(),userId);
        return "success";
    }
    @Transactional
    public RoleModulesCategories getPrivileges(String roleId){
        RoleModulesCategories roleModulesCategories = new RoleModulesCategories();
        roleModulesCategories.setBusinessCategories(roleDAO.getCategorysByType(roleId, 1));
        roleModulesCategories.setTechnicalCategories(roleDAO.getCategorysByType(roleId, 0));
        Privilege privilege = roleDAO.getPrivilegeByRoleId(roleId);
        privilege.setModules(privilegeDAO.getRelatedModuleWithPrivilege(privilege.getPrivilegeId()));
        roleModulesCategories.setPrivilege(privilege);
        return roleModulesCategories;
    }
    @Transactional
    public String putPrivileges(String roleId,RoleModulesCategories roleModulesCategories){
        Privilege privilege = roleModulesCategories.getPrivilege();
        String privilegeId = privilege.getPrivilegeId();
        roleDAO.updateCategory(privilegeId,roleId);
        roleDAO.deleteRole2category(roleId);
        List<Module> modules = privilege.getModules();
        List<Integer> moduleIds=new ArrayList<>();
        for (Module module : modules) {
            moduleIds.add(module.getModuleId());
        }
        List<CategoryEntity> businessCategories = roleModulesCategories.getBusinessCategories();
        List<CategoryEntity> technicalCategories = roleModulesCategories.getTechnicalCategories();
        if(moduleIds.contains(SystemModule.BUSINESSE_OPERATE)) {
            for (CategoryEntity businessCategory : businessCategories) {
                roleDAO.addRole2category(roleId, businessCategory.getGuid(), 1);
            }
        }else if(moduleIds.contains(SystemModule.BUSINESSE_CHECK)){
            for (CategoryEntity businessCategory : businessCategories) {
                roleDAO.addRole2category(roleId, businessCategory.getGuid(), 0);
            }
        }
        if(moduleIds.contains(SystemModule.TECHNICAL_OPERATE)) {
            for (CategoryEntity technicalCategory : technicalCategories) {
                roleDAO.addRole2category(roleId, technicalCategory.getGuid(), 1);
            }

        }else if(moduleIds.contains(SystemModule.TECHNICAL_CHECK)){
            for (CategoryEntity technicalCategory : technicalCategories) {
                roleDAO.addRole2category(roleId, technicalCategory.getGuid(), 0);
            }
        }
        return "success";
    }

}
