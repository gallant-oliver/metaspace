package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import io.zeta.metaspace.SSOConfig;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.OpType;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.model.user.UserWithRole;
import io.zeta.metaspace.utils.OKHttpClient;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.PrivilegeDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.DateUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class RoleService {
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);
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
        try {
            User user = AdminUtils.getUserData();
            String now = DateUtils.getNow();
            role.setRoleId("m" + UUID.randomUUID().toString());
            role.setCreateTime(now);
            role.setUpdateTime(now);
            role.setStatus(1);
            role.setDisable(1);
            role.setEdit(1);
            role.setDelete(1);
            role.setValid(true);
            String userId = user.getUserId();
            role.setCreator(userId);
            role.setUpdater(userId);
            if (roleDAO.ifRole(role.getRoleName())>0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色已存在");
            }
            roleDAO.addRoles(role);
            return "success";
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public Role getRoleById(String roleId) {
        return roleDAO.getRoleByRoleId(roleId);
    }

    public String updateRoleStatus(String roleId, int status) throws AtlasBaseException {
        Role role = roleDAO.getRoleByRoleId(roleId);
        if (role.getDisable() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色不允许禁用");
        }
        User user = AdminUtils.getUserData();
        String userId = user.getUserId();
        roleDAO.updateRoleStatus(roleId, status, DateUtils.getNow(), userId);
        return "success";
    }

    @Transactional(rollbackFor=Exception.class)
    public String deleteRole(String roleId) throws AtlasBaseException {
        Role role = roleDAO.getRoleByRoleId(roleId);
        if (role.getDelete() == 0) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "该角色不允许删除");
        }
        User user = AdminUtils.getUserData();
        String userId = user.getUserId();
        //roleDAO.deleteRole(roleId);
        //删除更新状态
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        List<User> users = roleDAO.getUsers(roleId,null,0,-1);
        roleDAO.updateValidStatus(roleId, false, userId, DateUtils.getNow());
        roleDAO.deleteRole2category(roleId);
        List<String> updateUserIds = new ArrayList<>();
        List<String> deleteUserIds = new ArrayList<>();
        for (User updateUser:users ){
            if (roleDAO.getCountByUser(updateUser.getUserId())==0&&!deleteUserIds.contains(updateUser.getUserId())){
                deleteUserIds.add(updateUser.getUserId());
                continue;
            }
            updateUserIds.add(updateUser.getUserId());
        }
        roleDAO.deleteUser2Role(roleId);
        if (updateUserIds.size()!=0)
            roleDAO.updateUsers(updateUserIds,true,timestamp);
        if (deleteUserIds.size()!=0)
            roleDAO.updateUsers(deleteUserIds,false,timestamp);
        return "success";
    }

    @Transactional(rollbackFor=Exception.class)
    public PageResult<User> getUsers(String roleId, String query, long offset, long limit) throws AtlasBaseException {
        try {
            PageResult<User> userPageResult = new PageResult<>();
            List<User> users;
        /*if (limit == -1) {
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            users = roleDAO.getUser(roleId, query, offset);
        } else {*/
            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            users = roleDAO.getUsers(roleId, query, offset, limit);
            for (User user : users){
                List<UserInfo.Role> roles =  userDAO.getRolesByUser(user.getUserId());
                user.setRoles(roles);
            }
            //}
            //long usersTotalSize = roleDAO.getUsersCount(roleId, query);
            long usersTotalSize = 0;
            if (users.size()!=0){
                usersTotalSize = users.get(0).getTotal();
            }
            userPageResult.setLists(users);
            userPageResult.setTotalSize(usersTotalSize);
            userPageResult.setCurrentSize(users.size());
            return userPageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public PageResult<Role> getRoles(String query, long offset, long limit, boolean containUnenable) throws AtlasBaseException {
        try {
            PageResult<Role> rolePageResult = new PageResult<>();
            if (Objects.nonNull(query)) {
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            } else {
                query = "";
            }
            List<Role> roles = roleDAO.getRoles(query, offset, limit, containUnenable);
            //long rolesTotalSize = roleDAO.getRolesCount(query, containUnenable);
            long rolesTotalSize = 0;
            if (roles.size()!=0){
                rolesTotalSize = roles.get(0).getTotal();
            }
            rolePageResult.setLists(roles);
            //rolePageResult.setOffset(offset);
            rolePageResult.setTotalSize(rolesTotalSize);
            rolePageResult.setCurrentSize(roles.size());
            return rolePageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public List<Role> getIncrRoles(String startTime) throws AtlasBaseException {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateFormat startDf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date startDate = startDf.parse(startTime);
            String formatStartTimeStr = df.format(startDate);
            List<Role> roles = roleDAO.getIncrRoles(formatStartTimeStr);
            for(int i=0; i<roles.size(); i++) {
                Role role = roles.get(i);
                Boolean valid = role.isValid();
                if(false == valid || "1".equals(role.getRoleId())) {
                    role.setOpType(OpType.DELETE.getDesc());
                    continue;
                }
                String createTime = role.getCreateTime();
                String updateTime = role.getUpdateTime();
                if(Objects.nonNull(createTime) && Objects.nonNull(updateTime)) {
                    Date createDate = df.parse(createTime);
                    if(createDate.getTime() > startDate.getTime()) {
                        role.setOpType(OpType.ADD.getDesc());
                    } else {
                        role.setOpType(OpType.UPDATE.getDesc());
                    }
                }
            }
            return roles;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public String addUsers(String roleId, List<String> users) throws AtlasBaseException {
        List<String> updateUsers = new ArrayList<>();
        for (String user : users) {
            List<String> roles = usersService.getRoleIdByUserId(user);
            if (roles.contains(SystemRole.ADMIN.getCode())) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许修改平台管理员用户");
            }
            if (!roles.contains(roleId)&&!updateUsers.contains(user)){
                updateUsers.add(user);
            }
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (updateUsers.size() > 0) {
            roleDAO.addUsers2Role(roleId, updateUsers);
            roleDAO.updateUsers(updateUsers, true, timestamp);
        }
        return "success";
    }

    @Transactional(rollbackFor=Exception.class)
    public void addRoleToUser(List<UserWithRole> userWithRoleList) throws AtlasBaseException {
        for(UserWithRole userWithRole: userWithRoleList) {
            List<String> roleIds = userWithRole.getRoleId();
            if(Objects.isNull(roleIds) || roleIds.size() == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户角色不能为空");
            }
            for (String roleId:roleIds){
                Role role = roleDAO.getRoleByRoleId(roleId);
                if(Objects.isNull(role) || !role.isValid()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无法完成当前操作，用户角色 " + roleId +" 已删除");
                }
                if("1".equals(roleId)) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许修改用户角色为平台管理员");
                }
                int status = role.getStatus();
                if(0 == status) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无法完成当前操作，用户角色 " + roleId +" 已被禁用");
                }
                List<String> users = userWithRole.getUserIds();
                List<String> userIds = new ArrayList<>();
                List<User> role2User = roleDAO.getUsers(roleId,null,0,-1);
                for (String userId : users) {
                    if (Objects.isNull(userId)) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户Id不能为空");
                    }
                    if (role2User.stream().anyMatch(user->user.getUserId().equals(userId))){
                        continue;
                    }
                    User userInfo = userDAO.getUserInfo(userId);
                    List<String> userRoleIds = userDAO.getRoleIdByUserId(userId);
                    if (Objects.isNull(userInfo)) {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        User user = new User();
                        user.setUserId(userId);
                        user.setValid(true);
                        user.setCreateTime(timestamp);
                        user.setUpdateTime(timestamp);
                        userDAO.addUser(user);
                        userIds.add(userId);
                    } else if (userRoleIds!=null && userRoleIds.contains(SystemRole.ADMIN.getCode())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, userId + "为平台管理员，不允许修改平台管理员用户");
                    } else {
                        userIds.add(userId);
                    }
                }
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                if (userIds.size() > 0) {
                    roleDAO.addUsers2Role(roleId, userIds);
                    roleDAO.updateUsers(userIds, true, timestamp);
                }
            }
            //更新用户信息
            updateUserInfo();
        }
    }


    @Transactional(rollbackFor=Exception.class)
    public String removeUser(String roleId,List<String> users) throws AtlasBaseException {
        List<String> deleteUsers = new ArrayList<>();
        if (SystemRole.ADMIN.getCode().equals(roleId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许修改平台管理员用户");
        }
        for (String user : users) {
            roleDAO.deleteUser2RoleByUser(user,roleId);
            if (roleDAO.getCountByUser(user)==0){
                deleteUsers.add(user);
            }
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (deleteUsers.size() > 0) {
            roleDAO.deleteUser2RoleByUsers(deleteUsers);
            roleDAO.updateUsers(deleteUsers, false, timestamp);
        }
        return "success";
    }

    @Transactional(rollbackFor=Exception.class)
    public void removeUserRole(List<UserWithRole> userWithRoleList) throws AtlasBaseException {
        for(UserWithRole userWithRole: userWithRoleList) {
            List<String> roleIds = userWithRole.getRoleId();
            if (Objects.nonNull(roleIds) && roleIds.size() == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户角色不能为空");
            }
            List<String> deleteUsers = new ArrayList<>();
            for (String roleId:roleIds){
                Role role = roleDAO.getRoleByRoleId(roleId);
                if(Objects.isNull(role) || !role.isValid()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无法完成当前操作，用户角色 " + roleId +" 已删除");
                }
                int status = role.getStatus();
                if(0 == status) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无法完成当前操作，用户角色 " + roleId +" 已被禁用");
                }

                List<String> users = userWithRole.getUserIds();
                for (String userId : users) {
                    List<String> realRoleId = usersService.getRoleIdByUserId(userId);
                    if (realRoleId.contains(SystemRole.ADMIN.getCode())) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不允许修改平台管理员用户");
                    }
                    roleDAO.deleteUser2RoleByUser(userId,roleId);
                    if (roleDAO.getCountByUser(userId)==0&&!deleteUsers.contains(userId)){
                        deleteUsers.add(userId);
                    }
                }
            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            if (deleteUsers.size() > 0) {
                roleDAO.updateUsers(deleteUsers, false, timestamp);
            }
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public RoleModulesCategories getPrivileges(String roleId) throws AtlasBaseException {
        RoleModulesCategories roleModulesCategories = new RoleModulesCategories();
        User user = AdminUtils.getUserData();
        List<Role> roles = roleDAO.getRoleByUsersId(user.getUserId());
        if(roles.stream().allMatch(role -> role.getStatus() == 0)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        }
        String userId = user.getUserId();
        List<String> userRoleIds = roleDAO.getRoleIdByUserId(userId);

        PrivilegeInfo privilege = roleDAO.getPrivilegeByRoleId(roleId);
        List<Module> modules = privilegeDAO.getRelatedModuleWithPrivilege(privilege.getPrivilegeId());
        privilege.setModules(modules);

        roleModulesCategories.setPrivilege(privilege);
        List<RoleModulesCategories.Category> bcategorys = getCategorys(roleId, userRoleIds, 1);

        roleModulesCategories.setBusinessCategories(bcategorys);
        List<RoleModulesCategories.Category> tcategorys = getCategorys(roleId, userRoleIds, 0);
        roleModulesCategories.setTechnicalCategories(tcategorys);
        Role roleByRoleId = roleDAO.getRoleByRoleId(roleId);
        roleModulesCategories.setEdit(roleByRoleId.getEdit());
        return roleModulesCategories;
    }

    @Transactional(rollbackFor=Exception.class)
    public List<RoleModulesCategories.Category> getCategorys(String roleId, List<String> userRoleIds, int categorytype) {
        //用户有权限的Category
        //上级不打勾，不展示，去重;同级，下级不打勾，展示
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        if (userRoleIds.contains(SystemRole.ADMIN.getCode())){
            userCategorys = getUserStringCategoryMap(SystemRole.ADMIN.getCode(), categorytype);
        }else {
            for (String userRoleId:userRoleIds){
                Map<String, RoleModulesCategories.Category> roleCategorys = getUserStringCategoryMap(userRoleId, categorytype);
                for (Map.Entry<String, RoleModulesCategories.Category> stringCategoryEntry : roleCategorys.entrySet()) {
                    String key = stringCategoryEntry.getKey();
                    RoleModulesCategories.Category value = stringCategoryEntry.getValue();
                    if (userCategorys.containsKey(key)) {
                        value.setShow(userCategorys.get(key).isShow() || value.isShow());
                    }
                    userCategorys.put(key,value);
                }
            }
        }

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

    @Transactional(rollbackFor=Exception.class)
    public void setOtherCategory(int categorytype, List<RoleModulesCategories.Category> resultList) {
        List<RoleModulesCategories.Category> otherCategorys = roleDAO.getOtherCategorys(resultList, categorytype,TenantService.defaultTenant);
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            otherCategory.setShow(false);
            otherCategory.setHide(true);
            otherCategory.setStatus(0);

        }
        resultList.addAll(otherCategorys);
    }

    @Transactional(rollbackFor=Exception.class)
    public Map<String, RoleModulesCategories.Category> getRoleStringCategoryMap(String roleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> categorys = new HashMap<>();
        if (roleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype,TenantService.defaultTenant);
            setMap(categorys, allCategorys, 1, false);
        } else {
            List<String> businessCategories = roleDAO.getCategorysByTypeIds(roleId, categorytype,TenantService.defaultTenant);
            if (businessCategories.size() > 0) {
                List<RoleModulesCategories.Category> childCategorys = roleDAO.getChildCategorys(businessCategories, categorytype,TenantService.defaultTenant);
                List<RoleModulesCategories.Category> parentCategorys = roleDAO.getParentCategorys(businessCategories, categorytype,TenantService.defaultTenant);
                List<RoleModulesCategories.Category> privilegeCategorys = roleDAO.getCategorysByType(roleId, categorytype,TenantService.defaultTenant);
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

    @Transactional(rollbackFor=Exception.class)
    public Map<String, RoleModulesCategories.Category> getUserStringCategoryMap(String userRoleId, int categorytype) {
        Map<String, RoleModulesCategories.Category> userCategorys = new HashMap<>();
        if (userRoleId.equals(SystemRole.ADMIN.getCode())) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype, TenantService.defaultTenant);
            setMap(userCategorys, allCategorys, 0, true);
        } else {
            List<String> userBusinessCategories = roleDAO.getCategorysByTypeIds(userRoleId, categorytype,TenantService.defaultTenant);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = roleDAO.getChildCategorys(userBusinessCategories, categorytype,TenantService.defaultTenant);
                List<RoleModulesCategories.Category> userParentCategorys = roleDAO.getParentCategorys(userBusinessCategories, categorytype,TenantService.defaultTenant);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = roleDAO.getCategorysByType(userRoleId, categorytype,TenantService.defaultTenant);
                //得到用户的带权限的目录树
                setMap(userCategorys, userChildCategorys, 0, true);
                setMap(userCategorys, userParentCategorys, 0, false);
                setMap(userCategorys, userPrivilegeCategorys, 0, true);
            }
        }
        return userCategorys;
    }

    @Transactional(rollbackFor=Exception.class)
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
            List<UserInfo.Role> roles =  userDAO.getRolesByUser(user.getUserId());
            user.setRoles(roles);
            if (roles.stream().anyMatch(role -> role.getRoleId().equals(roleId))) user.setStatus(1);
        }
        return userList;
    }

    public List<Role> getRoleIdBYUserId(String userId) {
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

    @Transactional(rollbackFor=Exception.class)
    public List<CategoryPrivilege> getUserCategory(String userRoleId, int categorytype) {
        List<CategoryPrivilege> userCategorys = new ArrayList<>();
        int ruleType = 4;
        if (userRoleId.equals(SystemRole.ADMIN.getCode()) || ruleType == categorytype) {
            List<RoleModulesCategories.Category> allCategorys = roleDAO.getAllCategorys(categorytype,TenantService.defaultTenant);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
            addPrivilege(userCategorys, allCategorys, privilege, categorytype);
        } else {
            List<String> userBusinessCategories = roleDAO.getCategorysByTypeIds(userRoleId, categorytype,TenantService.defaultTenant);
            if (userBusinessCategories.size() > 0) {
                List<RoleModulesCategories.Category> userChildCategorys = roleDAO.getChildCategorys(userBusinessCategories, categorytype,TenantService.defaultTenant);
                List<RoleModulesCategories.Category> userParentCategorys = roleDAO.getParentCategorys(userBusinessCategories, categorytype,TenantService.defaultTenant);
                List<RoleModulesCategories.Category> userPrivilegeCategorys = roleDAO.getCategorysByType(userRoleId, categorytype,TenantService.defaultTenant);
                //按角色方案
                List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(userRoleId);
                List<Integer> modules = new ArrayList<>();
                for (UserInfo.Module module : moduleByRoleId) {
                    modules.add(module.getModuleId());
                }
                CategoryPrivilege.Privilege childPrivilege = null;
                CategoryPrivilege.Privilege parentPrivilege = null;
                CategoryPrivilege.Privilege ownerPrivilege = null;
                //技术目录
                switch (categorytype) {
                    //技术目录
                    case 0: {
                        //按角色方案
                        if (modules.contains(SystemModule.TECHNICAL_OPERATE.getCode()) && modules.contains(SystemModule.TECHNICAL_CATALOG.getCode())) {
                            //按勾选的目录
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, true, false, true, true, true,false);

                        } else if (modules.contains(SystemModule.TECHNICAL_CATALOG.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, false, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, false, false, false, false, true,false);
                        } else if (modules.contains(SystemModule.TECHNICAL_OPERATE.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                        } else {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                        }
                        break;
                    }
                    //业务目录
                    case 1: {
                        //按角色方案
                        if (modules.contains(SystemModule.BUSINESSE_OPERATE.getCode()) && modules.contains(SystemModule.BUSINESSE_CATALOG.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, true, true, true, true, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, true, true, true, true, true,false);
                        } else if (modules.contains(SystemModule.BUSINESSE_CATALOG.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, true, true, false, true, false, true, true,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, true, false, true, false, true, true,false);
                        } else if (modules.contains(SystemModule.BUSINESSE_OPERATE.getCode())) {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, true, false, true, true, false,false);
                        } else {
                            childPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                            parentPrivilege = new CategoryPrivilege.Privilege(false, true, false, false, false, false, false, false, false,false);
                            ownerPrivilege = new CategoryPrivilege.Privilege(false, false, false, false, false, false, false, false, false,false);
                        }
                        break;
                    }
                    default:break;
                }
                addPrivilege(userCategorys, userChildCategorys, childPrivilege, categorytype);
                addPrivilege(userCategorys, userParentCategorys, parentPrivilege, categorytype);
                addPrivilege(userCategorys, userPrivilegeCategorys, ownerPrivilege, categorytype);
            }
        }
        addOtherCategory(categorytype, userCategorys);
        return userCategorys;
    }

    private void addPrivilege(List<CategoryPrivilege> userCategorys, List<RoleModulesCategories.Category> allCategorys, CategoryPrivilege.Privilege privilege, int categorytype) {
        String[] systemCategoryGuids = {"1", "2", "3", "4", "5"};
        List<String> lists = Arrays.asList(systemCategoryGuids);
        for (RoleModulesCategories.Category category : allCategorys) {
            CategoryPrivilege.Privilege privilegeinfo = new CategoryPrivilege.Privilege(privilege);
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(category);
            //系统系统目录不允许删除和编辑
            if (lists.contains(category.getGuid())) {
                privilegeinfo.setDelete(false);
                if (privilegeinfo.isEdit()){
                    privilegeinfo.setEditSafe(true);
                }
                privilegeinfo.setEdit(false);
            }
            //技术目录一级目录不允许删关联
            if (categorytype == 0 && category.getLevel() == 1) {
                privilegeinfo.setDeleteRelation(false);
            }
            categoryPrivilege.setPrivilege(privilegeinfo);
            userCategorys.add(categoryPrivilege);
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void addOtherCategory(int categorytype, List<CategoryPrivilege> resultList) {
        List<RoleModulesCategories.Category> otherCategorys = roleDAO.getOtherCategorys2(resultList, categorytype,TenantService.defaultTenant);
        ArrayList<CategoryPrivilege> others = new ArrayList<>();
        for (RoleModulesCategories.Category otherCategory : otherCategorys) {
            CategoryPrivilege categoryPrivilege = new CategoryPrivilege(otherCategory);
            CategoryPrivilege.Privilege privilege = new CategoryPrivilege.Privilege(true, false, false, false, false, false, false, false, false,false);
            categoryPrivilege.setPrivilege(privilege);
            others.add(categoryPrivilege);
        }
        resultList.addAll(others);
    }

    @Transactional(rollbackFor=Exception.class)
    public String editRole(Role role) throws AtlasBaseException {
        try {
            String id = role.getRoleId();
            role.getDescription();
            if (id.equals("") || id == null) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "角色id不能为空");
            }
            User user = AdminUtils.getUserData();
            String userId = user.getUserId();
            role.setUpdater(userId);
            String updateTime = DateUtils.getNow();
            role.setUpdateTime(updateTime);
            roleDAO.editRole(role);
            return "success";
        } catch (Exception e) {
            LOG.info(e.toString());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public void updateUserInfo() throws AtlasBaseException {
        try {
            Timestamp updateTime = new Timestamp(System.currentTimeMillis());
            List<String> userIdList = roleDAO.getUserIdList();
            for (String userId : userIdList) {
                User user = getUserInfo(userId);
                if(null==user || "".equals(user.getUsername())) {
                    roleDAO.deleteUser(userId, updateTime);
                }
                user.setUpdateTime(updateTime);
                roleDAO.updateUserInfo(user);
            }
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public User getUserInfo(String userId) throws AtlasBaseException {
        try {
            String userInfoURL = SSOConfig.getUserInfoURL();
            HashMap<String, String> header = new HashMap<>();
            Map<String, String> queryDataParamMap = new HashMap<>();
            queryDataParamMap.put("id", userId);
            String userSession = OKHttpClient.doGet(userInfoURL, queryDataParamMap, header);
            Gson gson = new Gson();
            Map userBody = gson.fromJson(userSession, Map.class);
            String data = "data";
            if (StringUtils.isEmpty(userBody.get(data).toString())) {
                return null;
            }
            Map userData = (Map) userBody.get(data);
            String email = userData.get("loginEmail").toString();
            String name = userData.get("displayName").toString();
            User user = new User();
            user.setUserId(userId);
            user.setUsername(name);
            user.setAccount(email);
            return user;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

}
