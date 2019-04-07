package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserDAO;
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
    @Autowired
    private UsersService usersService;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private UserDAO userDAO;

    @Bean(name = "getRoleService")
    public RoleService getRoleService() {
        return roleService;
    }

    public String addRole(Role role) throws AtlasBaseException {
        String now = DateUtils.getNow();
        role.setRoleId("m" + UUID.randomUUID().toString());
        role.setCreateTime(now);
        role.setUpdateTime(now);
        role.setStatus(1);
        role.setDisable(1);
        role.setEdit(1);
        role.setDelete(1);
        if (roleDAO.ifRole(role.getRoleName()).size() != 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色已存在");
        }
        ;
        roleDAO.addRoles(role);
        return "success";
    }

    public String updateRoleStatus(String roleId, int status) throws AtlasBaseException {
        Role role = roleDAO.getRoleByRoleId(roleId);
        if (role.getDisable() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色不允许禁用");
        }
        roleDAO.updateRoleStatus(roleId, status);
        return "success";
    }

    @Transactional
    public String deleteRole(String roleId) throws AtlasBaseException {
        Role role = roleDAO.getRoleByRoleId(roleId);
        if (role.getDelete() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色不允许删除");
        }
        roleDAO.deleteRole(roleId);
        roleDAO.deleteRole2category(roleId);
        roleDAO.updateUsersByRoleId(SystemRole.GUEST.getCode(), roleId);
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
        List<Role> roles = roleDAO.getRoles(query, offset, limit);
        long rolesCount = roleDAO.getRolesCount(query);
        rolePageResult.setLists(roles);
        rolePageResult.setOffset(offset);
        rolePageResult.setSum(rolesCount);
        rolePageResult.setCount(roles.size());
        return rolePageResult;
    }

    public String addUsers(String roleId, List<String> users) throws AtlasBaseException {
        for (String user : users) {
            String role = usersService.getRoleIdByUserId(user);
            if (role.equals("1")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许修改平台管理员用户");
            }
        }
        if (users.size() > 0)
            roleDAO.updateUsers(roleId, users);
        return "success";
    }

    public String removeUser(List<String> users) throws AtlasBaseException {
        for (String user : users) {
            String role = usersService.getRoleIdByUserId(user);
            if (role.equals("1")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许修改平台管理员用户");
            }
        }
        if (users.size() > 0)
            roleDAO.updateUsers(SystemRole.GUEST.getCode(), users);
        return "success";
    }

    @Transactional
    public RoleModulesCategories getPrivileges(String roleId) throws AtlasBaseException {
        RoleModulesCategories roleModulesCategories = new RoleModulesCategories();
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String userId = user.getUserId();
        String userRoleId = roleDAO.getRoleIdByUserId(userId);

        PrivilegeInfo privilege = roleDAO.getPrivilegeByRoleId(roleId);
        List<Module> modules = privilegeDAO.getRelatedModuleWithPrivilege(privilege.getPrivilegeId());
        privilege.setModules(modules);

        roleModulesCategories.setPrivilege(privilege);
        List<RoleModulesCategories.Category> bcategorys = getCategorys(roleId, userRoleId, 1);

        roleModulesCategories.setBusinessCategories(bcategorys);
        List<RoleModulesCategories.Category> tcategorys = getCategorys(roleId, userRoleId, 0);
        roleModulesCategories.setTechnicalCategories(tcategorys);
        Role roleByRoleId = roleDAO.getRoleByRoleId(roleId);
        roleModulesCategories.setEdit(roleByRoleId.getEdit());
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

        List<RoleModulesCategories.Category> resultList = new ArrayList<>(result.values());
        setOtherCategory(categorytype, resultList);
        CategoryRelationUtils.cleanInvalidBrother(resultList);
        return resultList;
    }
    @Transactional
    public void setOtherCategory(int categorytype, List<RoleModulesCategories.Category> resultList) {
        List<RoleModulesCategories.Category> otherCategorys = roleDAO.getOtherCategorys(resultList, categorytype);
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            otherCategory.setShow(false);
            otherCategory.setHide(true);
            otherCategory.setStatus(0);

        }
        resultList.addAll(otherCategorys);
    }

    @Transactional
    public Map<String, RoleModulesCategories.Category> getRoleStringCategoryMap(String roleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> categorys = new HashMap<>();
        if (roleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype);
            setMap(categorys, allCategorys, 1, false);
        } else {
            List<String> businessCategories = roleDAO.getCategorysByTypeIds(roleId, categorytype);
            if (businessCategories.size() > 0) {
                List<RoleModulesCategories.Category> childCategorys = roleDAO.getChildCategorys(businessCategories, categorytype);
                List<RoleModulesCategories.Category> parentCategorys = roleDAO.getParentCategorys(businessCategories, categorytype);
                List<RoleModulesCategories.Category> privilegeCategorys = roleDAO.getCategorysByType(roleId, categorytype);
                //得到角色的带权限的目录树
                setMap(categorys, childCategorys, 1, false);
                setMap(categorys, parentCategorys, 0, false);
                setMap(categorys, privilegeCategorys, 1, false);
            }
        }
        return categorys;
    }

    private void setMap(Map<String, RoleModulesCategories.Category> categorys, List<RoleModulesCategories.Category> allCategorys, int status, boolean show) {
        for (RoleModulesCategories.Category allCategory : allCategorys) {
            RoleModulesCategories.Category category = new RoleModulesCategories.Category(allCategory);
            category.setStatus(status);
            category.setShow(show);
            category.setHide(false);
            categorys.put(category.getGuid(), category);
        }
    }

    @Transactional
    public Map<String, RoleModulesCategories.Category> getUserStringCategoryMap(String userRoleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        if (userRoleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype);
            setMap(userCategorys, allCategorys, 0, true);
        } else {
            List<String> userBusinessCategories = roleDAO.getCategorysByTypeIds(userRoleId, categorytype);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = roleDAO.getChildCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userParentCategorys = roleDAO.getParentCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = roleDAO.getCategorysByType(userRoleId, categorytype);
                //得到用户的带权限的目录树
                setMap(userCategorys, userChildCategorys, 0, true);
                setMap(userCategorys, userParentCategorys, 0, false);
                setMap(userCategorys, userPrivilegeCategorys, 0, true);
            }
        }
        return userCategorys;
    }

    @Transactional
    public String putPrivileges(String roleId, RoleModulesCategories roleModulesCategories) throws AtlasBaseException {
        Role role = roleDAO.getRoleByRoleId(roleId);
        if (role.getEdit() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色不允许编辑");
        }
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
        if (businessCategories != null) {
            for (RoleModulesCategories.Category businessCategory : businessCategories) {
                roleDAO.addRole2category(roleId, businessCategory.getGuid(), 1);
            }
        }

        //
        if (technicalCategories != null) {
            for (RoleModulesCategories.Category technicalCategory : technicalCategories) {
                roleDAO.addRole2category(roleId, technicalCategory.getGuid(), 1);
            }
        }
        return "success";
    }

    public PageResult<User> getAllUsers(Parameters parameters, String roleId) throws AtlasBaseException {
        PageResult<User> userList = usersService.getUserListFilterAdmin(parameters);
        List<User> lists = userList.getLists();
        for (User user : lists) {
            if (user.getRoleId().equals(roleId)) user.setStatus(1);
        }
        return userList;
    }

    public Role getRoleIdBYUserId(String userId) {
        return roleDAO.getRoleByUsersId(userId);
    }

    /**
     * 获取用户目录树，有权限首级目录不能加关联
     * 1.4新权限 有管理目录权限的可以编辑目录和添加关联，其他人只能看
     * 业务目录的，有管理目录权限的编辑目录，有编辑业务信息权限的可以创建业务对象和编辑业务对象
     *
     * @param userRoleId
     * @param categorytype
     * @return
     */
    @Transactional
    public ArrayList<RoleModulesCategories.Category> getUserCategory(String userRoleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        if (userRoleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype);
            setMap(userCategorys, allCategorys, 2, true);
        } else {
            List<String> userBusinessCategories = roleDAO.getCategorysByTypeIds(userRoleId, categorytype);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = roleDAO.getChildCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userParentCategorys = roleDAO.getParentCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = roleDAO.getCategorysByType(userRoleId, categorytype);
                //按角色方案
                List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(userRoleId);
                List<Integer> modules = new ArrayList<>();
                for (UserInfo.Module module : moduleByRoleId) {
                    modules.add(module.getModuleId());
                }
                //技术目录
                switch (categorytype) {
                    //技术目录
                    case 0: {
                        //按角色方案
                        if (modules.contains(SystemModule.TECHNICAL_OPERATE.getCode())) {
                            //按勾选的目录
                            setMap(userCategorys, userChildCategorys, 2, true);
                            setMap(userCategorys, userParentCategorys, 0, false);
                            setMap(userCategorys, userPrivilegeCategorys, 1, true);
                        } else {
                            setMap(userCategorys, userChildCategorys, 0, true);
                            setMap(userCategorys, userParentCategorys, 0, false);
                            setMap(userCategorys, userPrivilegeCategorys, 0, true);
                        }
                        break;
                    }
                    //业务目录
                    case 1: {
                        //按角色方案
                        if (modules.contains(SystemModule.BUSINESSE_OPERATE.getCode()) && modules.contains(SystemModule.BUSINESSE_EDIT.getCode())) {
                            setMap(userCategorys, userChildCategorys, 2, true);
                            setMap(userCategorys, userParentCategorys, 0, false);
                            setMap(userCategorys, userPrivilegeCategorys, 1, true);
                        } else if (modules.contains(SystemModule.BUSINESSE_OPERATE.getCode())) {
                            setMap(userCategorys, userChildCategorys, 3, true);
                            setMap(userCategorys, userParentCategorys, 0, false);
                            setMap(userCategorys, userPrivilegeCategorys, 4, true);
                        } else if (modules.contains(SystemModule.BUSINESSE_EDIT.getCode())) {
                            setMap(userCategorys, userChildCategorys, 5, true);
                            setMap(userCategorys, userParentCategorys, 0, false);
                            setMap(userCategorys, userPrivilegeCategorys, 5, true);
                        } else {
                            setMap(userCategorys, userChildCategorys, 0, true);
                            setMap(userCategorys, userParentCategorys, 0, false);
                            setMap(userCategorys, userPrivilegeCategorys, 0, true);
                        }
                        break;
                    }
                }

            }
        }

        Collection<RoleModulesCategories.Category> valueCollection = userCategorys.values();
        ArrayList<RoleModulesCategories.Category> categories = new ArrayList<>(valueCollection);
        setOtherCategory(categorytype, categories);

        return categories;
    }

    @Transactional
    public List<CategoryPrivilege> getUserCategory2(String userRoleId, int categorytype) {
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        if (userRoleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,true);
            addPrivilege(userCategorys, allCategorys,privilege);
        }else {
            List<String> userBusinessCategories = roleDAO.getCategorysByTypeIds(userRoleId, categorytype);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = roleDAO.getChildCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userParentCategorys = roleDAO.getParentCategorys(userBusinessCategories, categorytype);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = roleDAO.getCategorysByType(userRoleId, categorytype);
                //按角色方案
                List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(userRoleId);
                List<Integer> modules = new ArrayList<>();
                for (UserInfo.Module module : moduleByRoleId) {
                    modules.add(module.getModuleId());
                }
                CategoryPrivilege.Privilege childPrivilege=null;
                CategoryPrivilege.Privilege parentPrivilege=null;
                CategoryPrivilege.Privilege ownerPrivilege=null;
                //技术目录
                switch (categorytype) {
                    //技术目录
                    case 0: {
                        //按角色方案
                        if (modules.contains(SystemModule.TECHNICAL_OPERATE.getCode())&& modules.contains(SystemModule.TECHNICAL_EDIT.getCode())) {
                            //按勾选的目录
                             childPrivilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,true);
                             parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                             ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,true,true,true,true,true);

                        } else if(modules.contains(SystemModule.TECHNICAL_OPERATE.getCode())){
                             childPrivilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,false);
                             parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                             ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,true,true,true,true,false);
                        } else if(modules.contains(SystemModule.TECHNICAL_EDIT.getCode())){
                            childPrivilege = new CategoryPrivilege.Privilege(false,false,false,false,false,false,false,true);
                            parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,false,false,false,false,true);
                        } else {
                            childPrivilege = new CategoryPrivilege.Privilege(false,false,false,false,false,false,false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,false,false,false,false,false);
                        }
                        break;
                    }
                    //业务目录
                    case 1: {
                        //按角色方案
                        if (modules.contains(SystemModule.BUSINESSE_OPERATE.getCode()) && modules.contains(SystemModule.BUSINESSE_EDIT.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,true);
                            parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,true,true,true,true,true);
                        } else if (modules.contains(SystemModule.BUSINESSE_OPERATE.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false,false,true,true,false,true,true,true);
                            parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,true,false,true,true,true);
                        } else if (modules.contains(SystemModule.BUSINESSE_EDIT.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false,false,true,true,true,true,true,true);
                            parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,true,true,true,true,true);
                        } else {
                            childPrivilege = new CategoryPrivilege.Privilege(false,false,false,false,false,false,false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false,true,false,false,false,false,false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false,false,false,false,false,false,false,false);
                        }
                        break;
                    }
                }
                addPrivilege(userCategorys, userChildCategorys,childPrivilege);
                addPrivilege(userCategorys, userParentCategorys,parentPrivilege);
                addPrivilege(userCategorys, userPrivilegeCategorys,ownerPrivilege);
            }
        }
        addOtherCategory(categorytype, userCategorys);
        return userCategorys;
    }

    private void addPrivilege(List<CategoryPrivilege> userCategorys, List<RoleModulesCategories.Category> allCategorys,CategoryPrivilege.Privilege privilege) {
        String[] systemCategoryGuids={"1","2","3","4","5"};
        List<String> lists =Arrays.asList(systemCategoryGuids);
        for (RoleModulesCategories.Category category : allCategorys) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(category);
            //系统系统目录不允许删除
            if (lists.contains(category.getGuid())){
                privilege.setDelete(false);
            }
            //一级目录不允许删关联
            if(category.getLevel()==1){
                privilege.setDeleteRelation(false);
            }
            categoryPrivilege.setPrivilege(privilege);
            userCategorys.add(categoryPrivilege);
        }
    }

    @Transactional
    public void addOtherCategory(int categorytype, List<CategoryPrivilege> resultList) {
        List<RoleModulesCategories.Category> otherCategorys = roleDAO.getOtherCategorys2(resultList, categorytype);
        ArrayList<CategoryPrivilege> others = new ArrayList<>();
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(otherCategory);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(true,false,false,false,false,false,false,false);
            categoryPrivilege.setPrivilege(privilege);
            others.add(categoryPrivilege);
        }
        resultList.addAll(others);
    }


}
