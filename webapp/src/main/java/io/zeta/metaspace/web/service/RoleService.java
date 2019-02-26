package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class RoleService {
    @Autowired
    private RoleDAO roleDAO;
    @Autowired
    private PrivilegeDAO privilegeDAO;
    @Autowired
    private RoleService roleService;


    @Bean(name = "getRoleService")
    public RoleService getRoleService() {
        return roleService;
    }

    public String addRole(Role role) throws AtlasBaseException {
        String now = DateUtils.getNow();
        role.setRoleId(UUID.randomUUID().toString());
        role.setCreateTime(now);
        role.setUpdateTime(now);
        role.setStatus(1);
        if (roleDAO.ifRole(role.getRoleName()).size() != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色已存在");
        }
        ;
        roleDAO.addRoles(role);
        return "success";
    }

    public String updateRoleStatus(String roleId, int status) throws AtlasBaseException {
        roleDAO.updateRoleStatus(roleId, status);
        return "success";
    }

    public String deleteRole(String roleId) throws AtlasBaseException {
        roleDAO.deleteRole(roleId);
        roleDAO.deleteRole2category(roleId);
        return "success";
    }

    @Transactional
    public PageResult<User> getUsers(String roleId, String query, long offset, long limit) throws AtlasBaseException {
        PageResult<User> userPageResult = new PageResult<>();
        List<User> users;
        if (limit == -1) {
            users = roleDAO.getUser(roleId, query, offset);
        } else {
            users = roleDAO.getUsers(roleId, query, offset, limit);
        }
        long usersCount = roleDAO.getUsersCount(roleId, query);
        userPageResult.setLists(users);
        userPageResult.setOffset(offset);
        userPageResult.setSum(usersCount);
        userPageResult.setCount(users.size());
        return userPageResult;
    }

    @Transactional
    public PageResult<Role> getRoles(String query, long offset, long limit) throws AtlasBaseException {
        PageResult<Role> rolePageResult = new PageResult<>();
        List<Role> roles;
        if (limit == -1) {
            roles = roleDAO.getRole(query, offset);
        } else {
            roles = roleDAO.getRoles(query, offset, limit);
        }

        long rolesCount = roleDAO.getRolesCount(query);
        rolePageResult.setLists(roles);
        rolePageResult.setOffset(offset);
        rolePageResult.setSum(rolesCount);
        rolePageResult.setCount(roles.size());
        return rolePageResult;
    }

    public String addUsers(String roleId, List<String> users) throws AtlasBaseException {
        roleDAO.updateUsers(roleId, users);
        return "success";
    }

    public String removeUser(List<String> users) throws AtlasBaseException {
        roleDAO.updateUsers(SystemRole.GUEST.getCode(), users);
        return "success";
    }

    @Transactional
    public RoleModulesCategories getPrivileges(String roleId) throws AtlasBaseException {
        RoleModulesCategories roleModulesCategories = new RoleModulesCategories();
        String userId = AdminUtils.getUserData().getUserId();
        String userRoleId = roleDAO.getRoleIdByUserId(userId);

        PrivilegeInfo privilege = roleDAO.getPrivilegeByRoleId(roleId);
        List<Module> modules = privilegeDAO.getRelatedModuleWithPrivilege(privilege.getPrivilegeId());
        privilege.setModules(modules);

        roleModulesCategories.setPrivilege(privilege);
        List<Integer> moduleIds = new ArrayList<>();
        for (Module module : modules) {
            moduleIds.add(module.getModuleId());
        }

        if (moduleIds.contains(SystemModule.BUSINESSE_CHECK.getCode()) || moduleIds.contains(SystemModule.BUSINESSE_OPERATE.getCode()) || moduleIds.contains(SystemModule.BUSINESSE_MANAGE.getCode())) {
            List<RoleModulesCategories.Category> categorys = getCategorys(roleId, userRoleId, 1);
            roleModulesCategories.setBusinessCategories(categorys);
        }
        if (moduleIds.contains(SystemModule.TECHNICAL_CHECK.getCode()) || moduleIds.contains(SystemModule.TECHNICAL_OPERATE.getCode())) {
            List<RoleModulesCategories.Category> categorys = getCategorys(roleId, userRoleId, 0);
            roleModulesCategories.setTechnicalCategories(categorys);
        }


        return roleModulesCategories;
    }

    @Transactional
    public List<RoleModulesCategories.Category> getCategorys(String roleId, String userRoleId, int categorytype) {
        //用户有权限的Category
        //上级不打勾，不展示，去重;同级，下级不打勾，展示

        Map<String, RoleModulesCategories.Category> userCategorys = getUserStringCategoryMap(userRoleId, categorytype);

        //角色有权限的Category
        //上级不打勾，不展示，去重;同级，下级打勾，不展示
        Map<String, RoleModulesCategories.Category> categorys = getRoleStringCategoryMap(roleId, categorytype);

        //结果合并
        //上级合并去重；同级，下级有相同的，或逻辑判断打勾和展示并合并；最后上下级合并
        //求交集
        Map<String, RoleModulesCategories.Category> result = new HashMap<>();
        for (Map.Entry<String, RoleModulesCategories.Category> stringCategoryEntry : categorys.entrySet()) {
            String key = stringCategoryEntry.getKey();
            RoleModulesCategories.Category value = stringCategoryEntry.getValue();
            if (userCategorys.containsKey(key)) {
                value.setShow(userCategorys.get(key).isShow());
            }
            result.put(key, value);

        }
        //合并剩下的子集
        for (Map.Entry<String, RoleModulesCategories.Category> stringCategoryEntry : userCategorys.entrySet()) {
            String key = stringCategoryEntry.getKey();
            RoleModulesCategories.Category value = stringCategoryEntry.getValue();
            if (!result.containsKey(key)) {
                result.put(key, value);
            }
        }
        return new ArrayList<>(result.values());
    }

    @Transactional
    public Map<String, RoleModulesCategories.Category> getRoleStringCategoryMap(String roleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> categorys = new HashMap<>();
        if (roleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype);
            for (RoleModulesCategories.Category allCategory : allCategorys) {
                allCategory.setStatus(1);
                allCategory.setShow(false);
                categorys.put(allCategory.getGuid(), allCategory);
            }
        } else {
            List<String> businessCategories = roleDAO.getCategorysByTypeIds(roleId, categorytype);
            if (businessCategories.size() > 0) {
                List<RoleModulesCategories.Category> childCategorys = roleDAO.getChildCategorys(businessCategories, categorytype);
                List<RoleModulesCategories.Category> parentCategorys = roleDAO.getParentCategorys(businessCategories, categorytype);
                List<RoleModulesCategories.Category> privilegeCategorys = roleDAO.getCategorysByType(roleId, categorytype);
                //得到角色的带权限的目录树
                for (RoleModulesCategories.Category childCategory : childCategorys) {
                    childCategory.setStatus(1);
                    childCategory.setShow(false);
                    categorys.put(childCategory.getGuid(), childCategory);
                }
                for (RoleModulesCategories.Category parentCategory : parentCategorys) {
                    parentCategory.setStatus(0);
                    parentCategory.setShow(false);
                    categorys.put(parentCategory.getGuid(), parentCategory);
                }
                for (RoleModulesCategories.Category privilegeCategory : privilegeCategorys) {
                    privilegeCategory.setStatus(1);
                    privilegeCategory.setShow(false);
                    categorys.put(privilegeCategory.getGuid(), privilegeCategory);
                }
            }
        }
        return categorys;
    }

    @Transactional
    public Map<String, RoleModulesCategories.Category> getUserStringCategoryMap(String userRoleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        if (userRoleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype);
            for (RoleModulesCategories.Category allCategory : allCategorys) {
                allCategory.setStatus(0);
                allCategory.setShow(true);
                userCategorys.put(allCategory.getGuid(), allCategory);
            }
        } else {
            List<String> userBusinessCategories = roleDAO.getCategorysByTypeIds(userRoleId, categorytype);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = roleDAO.getChildCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userParentCategorys = roleDAO.getParentCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = roleDAO.getCategorysByType(userRoleId, categorytype);
                //得到用户的带权限的目录树

                for (RoleModulesCategories.Category userChildCategory : userChildCategorys) {
                    userChildCategory.setStatus(0);
                    userChildCategory.setShow(true);
                    userCategorys.put(userChildCategory.getGuid(), userChildCategory);
                }
                for (RoleModulesCategories.Category userParentCategory : userParentCategorys) {
                    userParentCategory.setStatus(0);
                    userParentCategory.setShow(false);
                    userCategorys.put(userParentCategory.getGuid(), userParentCategory);
                }
                for (RoleModulesCategories.Category userPrivilegeCategory : userPrivilegeCategorys) {
                    userPrivilegeCategory.setStatus(0);
                    userPrivilegeCategory.setShow(true);
                    userCategorys.put(userPrivilegeCategory.getGuid(), userPrivilegeCategory);
                }
            }
        }
        return userCategorys;
    }

    @Transactional

    public String putPrivileges(String roleId, RoleModulesCategories roleModulesCategories) throws AtlasBaseException {
        PrivilegeInfo privilege = roleModulesCategories.getPrivilege();
        String privilegeId = privilege.getPrivilegeId();
        roleDAO.updateCategory(privilegeId, roleId, DateUtils.getNow());
        roleDAO.deleteRole2category(roleId);
        List<Module> modules = privilege.getModules();
        List<Integer> moduleIds = new ArrayList<>();
        for (Module module : modules) {
            moduleIds.add(module.getModuleId());
        }
        List<RoleModulesCategories.Category> businessCategories = roleModulesCategories.getBusinessCategories();
        List<RoleModulesCategories.Category> technicalCategories = roleModulesCategories.getTechnicalCategories();
        if (moduleIds.contains(SystemModule.BUSINESSE_OPERATE.getCode())) {
            for (RoleModulesCategories.Category businessCategory : businessCategories) {
                if (businessCategory.getStatus() == 1)
                    roleDAO.addRole2category(roleId, businessCategory.getGuid(), 1);
            }
        } else if (moduleIds.contains(SystemModule.BUSINESSE_CHECK.getCode())) {
            for (RoleModulesCategories.Category businessCategory : businessCategories) {
                if (businessCategory.getStatus() == 1)
                    roleDAO.addRole2category(roleId, businessCategory.getGuid(), 0);
            }
        }
        if (moduleIds.contains(SystemModule.TECHNICAL_OPERATE.getCode()) || moduleIds.contains(SystemModule.BUSINESSE_MANAGE.getCode())) {
            for (RoleModulesCategories.Category technicalCategory : technicalCategories) {
                if (technicalCategory.getStatus() == 1)
                    roleDAO.addRole2category(roleId, technicalCategory.getGuid(), 1);
            }

        } else if (moduleIds.contains(SystemModule.TECHNICAL_CHECK.getCode())) {
            for (RoleModulesCategories.Category technicalCategory : technicalCategories) {
                if (technicalCategory.getStatus() == 1)
                    roleDAO.addRole2category(roleId, technicalCategory.getGuid(), 0);
            }
        }
        return "success";
    }


}
