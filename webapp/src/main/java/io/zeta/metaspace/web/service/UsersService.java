package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.Item;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.security.RoleResource;
import io.zeta.metaspace.model.security.SecuritySearch;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.model.user.UserInfoGroup;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.dao.UserGroupDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import kafka.security.auth.Alter;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);
    @Autowired
    private TenantService tenantService;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private RoleDAO roleDAO;
    @Autowired
    private UserGroupDAO userGroupDAO;

    @Bean(name = "getUserService")
    public UsersService getUserService() {
        return usersService;
    }

    public boolean isRole(String userId){
        List<String> roleId = userDAO.getRoleIdByUserId(userId);
        return roleId==null||roleId.size()==0;
    }


    public void addUser(Map data) throws AtlasBaseException {
        try {
            String userId = data.get("AccountGuid").toString();
            String account = data.get("LoginEmail").toString();
            String displayName = data.get("DisplayName").toString();
            if (userDAO.ifUserExists(userId) == 0) {
                User user = new User();
                user.setUserId(userId);
                user.setAccount(account);
                user.setUsername(displayName);
                List<UserInfo.Role> roles = new ArrayList<>();
                UserInfo.Role role = new UserInfo.Role();
                roles.add(role);
                String msadmin = "msadmin";
                if (msadmin.equals(user.getUsername())) {
                    role.setRoleId(SystemRole.ADMIN.getCode());
                } else {
                    role.setRoleId(SystemRole.ADMIN.getCode());
                }
                user.setRoles(roles);
                userDAO.addUser(user);
            }
        } catch (Exception e) {
            LOG.error("添加用户失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加用户失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public UserInfo getUserInfoById(String userId) throws AtlasBaseException {
        try {
            UserInfo info = new UserInfo();
            //user
            User userTmp = userDAO.getUser(userId);
            UserInfo.User user = new UserInfo.User();
            user.setUserId(userTmp.getUserId());
            user.setUsername(userTmp.getUsername());
            user.setAccount(userTmp.getAccount());
            info.setUser(user);
            //role
            List<Role> roleTmps = userDAO.getRoleByUserId(userId);
            List<UserInfo.Role> roles = new ArrayList<>();
            for (Role roleTmp:roleTmps){
                UserInfo.Role role = new UserInfo.Role();
                role.setRoleId(roleTmp.getRoleId());
                role.setRoleName(roleTmp.getRoleName());
                roles.add(role);
            }
            info.setRoles(roles);
            List<String> technicalChildCategorys = new ArrayList<>();
            List<String> businessChildCategorys = new ArrayList<>();
            List<UserInfo.BusinessCategory> userBusiCategoryList = new ArrayList<>();
            List<UserInfo.TechnicalCategory> userTechCategoryList = new ArrayList<>();
            List<UserInfo.Module> modules = new ArrayList<>();
            for (UserInfo.Role role:roles){
                String roleId = role.getRoleId();
                //module
                List<UserInfo.Module> moduleList = userDAO.getModuleByRoleId(roleId);
                for (UserInfo.Module module:moduleList){
                    if (!modules.stream().anyMatch(userModule -> userModule.getModuleId()==module.getModuleId())){
                        modules.add(module);
                    }
                }
                //technicalCategory
                List<CategoryEntityV2> technicalCategoryList = userDAO.getTechnicalCategoryByRoleId(roleId);
                for (CategoryEntityV2 entity : technicalCategoryList) {
                    if (userTechCategoryList.stream().anyMatch(technicalCategory -> technicalCategory.getGuid().equals(entity.getGuid()))){
                        continue;
                    }
                    if (technicalChildCategorys.contains(entity.getGuid())){
                        continue;
                    }
                    addUserCategory(userTechCategoryList,entity,0,"default");
                }
                if (technicalCategoryList!=null&&technicalCategoryList.size()!=0){
                    List<RoleModulesCategories.Category> techChildCategorys = roleDAO.getChildCategorys(technicalCategoryList.stream().map(categoryEntityV2 -> categoryEntityV2.getGuid()).collect(Collectors.toList()), 0,TenantService.defaultTenant);
                    for (RoleModulesCategories.Category category:techChildCategorys){
                        if (!technicalChildCategorys.contains(category.getGuid())){
                            technicalChildCategorys.add(category.getGuid());
                        }
                    }
                }
                //businessCategory
                List<CategoryEntityV2> businessCategoryList = userDAO.getBusinessCategoryByRoleId(roleId);
                for (CategoryEntityV2 entity : businessCategoryList) {
                    if (userBusiCategoryList.stream().anyMatch(technicalCategory -> technicalCategory.getGuid().equals(entity.getGuid()))){
                        continue;
                    }
                    if (businessChildCategorys.contains(entity.getGuid())){
                        continue;
                    }
                    addUserCategory(userBusiCategoryList,entity,1,"default");
                }
                if (businessCategoryList!=null&&businessCategoryList.size()!=0){
                    List<RoleModulesCategories.Category> busiChildCategorys = roleDAO.getChildCategorys(businessCategoryList.stream().map(categoryEntityV2 -> categoryEntityV2.getGuid()).collect(Collectors.toList()), 1,TenantService.defaultTenant);
                    for (RoleModulesCategories.Category category:busiChildCategorys){
                        if (!businessChildCategorys.contains(category.getGuid())){
                            businessChildCategorys.add(category.getGuid());
                        }
                    }
                }
            }
            userTechCategoryList = userTechCategoryList.stream().filter(category->!technicalChildCategorys.contains(category.getGuid())).collect(Collectors.toList());
            userBusiCategoryList = userBusiCategoryList.stream().filter(category->!businessChildCategorys.contains(category.getGuid())).collect(Collectors.toList());
            info.setModules(modules);
            info.setTechnicalCategory(userTechCategoryList);
            info.setBusinessCategory(userBusiCategoryList);
            return info;
        } catch (Exception e) {
            LOG.error("获取用户信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }

    public void addUserCategory(List list,CategoryEntityV2 entity,int categoryType,String tenantId){
        String guid = entity.getGuid();
        String name = entity.getName();
        String pathStr = categoryDAO.queryPathByGuid(guid,tenantId);
        String path = pathStr.substring(1, pathStr.length() - 1);
        path = path.replace(",", ".").replace("\"", "");
        String level2Category = null;
        String[] pathArr = path.split("\\.");
        int level = pathArr.length;
        int length = 2;
        if (level >= length) {
            level2Category = pathArr[1];
        }
        if (categoryType==0){
            UserInfo.TechnicalCategory category = new UserInfo.TechnicalCategory(guid, name, level, level2Category);
            list.add(category);
        }else if (categoryType==1){
            UserInfo.BusinessCategory category = new UserInfo.BusinessCategory(guid, name, level, level2Category);
            list.add(category);
        }
    }


    public PageResult<User> getUserList(Parameters parameters) throws AtlasBaseException {

        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        try {
            PageResult<User> userPageResult = new PageResult<>();
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<User> userList = userDAO.getUserList(query, limit, offset);
            for (User user:userList){
                user.setRoles(userDAO.getRolesByUser(user.getUserId()));
            }
            userPageResult.setLists(userList);
            long userTotalSize = 0;
            if (userList.size()!=0){
                userTotalSize = userList.get(0).getTotal();
            }
            userPageResult.setCurrentSize(userList.size());
            userPageResult.setTotalSize(userTotalSize);
            return userPageResult;
        } catch (Exception e) {
            LOG.error("获取用户列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }

    public Integer ifPrivilege(List<String> categoryGuid, String tableGuid) {
        if (categoryGuid.size() > 0) {
            return userDAO.ifPrivilege(categoryGuid, tableGuid);
        } else {
            return 0;
        }
    }

    public Item getUserItems(String tenantId) throws AtlasBaseException {
        try {
            Item item = new Item();
            List<Module> modules;
            if (TenantService.defaultTenant.equals(tenantId)){
                String userId = AdminUtils.getUserData().getUserId();
                List<Role> roleByUserIds = userDAO.getRoleByUserId(userId);
                if (roleByUserIds.stream().allMatch(role -> role.getStatus() == 0)) {
                    item.setModules(new ArrayList<>());
                    item.setRoles(roleByUserIds);
                    return item;
                }
                modules = userDAO.getModuleByUserId(userId);
                for (Role role : roleByUserIds) {
                    String roleId = role.getRoleId();
                    List<UserInfo.Module> moduleList = userDAO.getModuleByRoleId(roleId);
                    for (UserInfo.Module module : moduleList) {
                        if (!modules.stream().anyMatch(userModule -> userModule.getModuleId() == module.getModuleId())) {
                            modules.add(new Module(module));
                        }
                    }
                }
                item.setRoles(roleByUserIds);
            }else{
                modules = tenantService.getModule(tenantId);
            }
            item.setModules(modules);
            return item;
        } catch (Exception e) {
            LOG.error("获取失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户功能模块失败");
        }
    }

    public List<Module> getModules(String userId) {
        return userDAO.getModuleByUserId(userId);
    }

    public List<String> getRoleIdByUserId(String userId) {
        return roleDAO.getRoleIdByUserId(userId);
    }

    public List<Role> getRoleByUserId(String userId) {
        return userDAO.getRoleByUserId(userId);
    }

    public PageResult<User> getUserListFilterAdmin(Parameters parameters) throws AtlasBaseException {

        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        try {
            PageResult<User> userPageResult = new PageResult<>();
            if(Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            List<User> userList = userDAO.getUserListFilterAdmin(query, limit, offset);
            userPageResult.setLists(userList);
            long userCount = userDAO.getUsersCount(query);
            userPageResult.setCurrentSize(userList.size());
            userPageResult.setTotalSize(userCount);
            return userPageResult;
        } catch (Exception e) {
            LOG.error("获取成员失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取成员失败");
        }
    }

    @Transactional(rollbackFor=Exception.class)
    public UserInfoGroup getUserInfoByIdV2(String tenantId,String userId) throws AtlasBaseException {
        try {
            UserInfoGroup info = new UserInfoGroup();
            //user
            User userTmp = userDAO.getUser(userId);
            UserInfo.User user = new UserInfo.User();
            user.setUserId(userTmp.getUserId());
            user.setUsername(userTmp.getUsername());
            user.setAccount(userTmp.getAccount());
            info.setUser(user);
            //userGroups
            List<String> userGroupIds = userDAO.getUserGroupIdByUser(userId,tenantId);
            List<String> technicalChildCategorys = new ArrayList<>();
            List<String> businessChildCategorys = new ArrayList<>();
            List<UserInfo.BusinessCategory> userBusiCategoryList = new ArrayList<>();
            List<UserInfo.TechnicalCategory> userTechCategoryList = new ArrayList<>();
            for (String userGroupId:userGroupIds){
                //technicalCategory
                List<CategoryEntityV2> technicalCategoryList = userDAO.getTechnicalCategoryByUserGroup(userGroupId,tenantId);
                for (CategoryEntityV2 entity : technicalCategoryList) {
                    if (userTechCategoryList.stream().anyMatch(technicalCategory -> technicalCategory.getGuid().equals(entity.getGuid()))){
                        continue;
                    }
                    if (technicalChildCategorys.contains(entity.getGuid())){
                        continue;
                    }
                    addUserCategory(userTechCategoryList,entity,0,tenantId);
                }
                if (technicalCategoryList!=null&&technicalCategoryList.size()!=0){
                    List<RoleModulesCategories.Category> techChildCategorys = userGroupDAO.getChildCategorys(technicalCategoryList.stream().map(categoryEntityV2 -> categoryEntityV2.getGuid()).collect(Collectors.toList()), 0,tenantId);
                    for (RoleModulesCategories.Category category:techChildCategorys){
                        if (!technicalChildCategorys.contains(category.getGuid())){
                            technicalChildCategorys.add(category.getGuid());
                        }
                    }
                }
                //businessCategory
                List<CategoryEntityV2> businessCategoryList = userDAO.getBusinessCategoryByUserGroup(userGroupId,tenantId);
                for (CategoryEntityV2 entity : businessCategoryList) {
                    if (userBusiCategoryList.stream().anyMatch(technicalCategory -> technicalCategory.getGuid().equals(entity.getGuid()))){
                        continue;
                    }
                    if (businessChildCategorys.contains(entity.getGuid())){
                        continue;
                    }
                    addUserCategory(userBusiCategoryList,entity,1,tenantId);
                }
                if (businessCategoryList!=null&&businessCategoryList.size()!=0){
                    List<RoleModulesCategories.Category> busiChildCategorys = userGroupDAO.getChildCategorys(businessCategoryList.stream().map(categoryEntityV2 -> categoryEntityV2.getGuid()).collect(Collectors.toList()), 1,tenantId);
                    for (RoleModulesCategories.Category category:busiChildCategorys){
                        if (!businessChildCategorys.contains(category.getGuid())){
                            businessChildCategorys.add(category.getGuid());
                        }
                    }
                }
            }
            SecuritySearch search = new SecuritySearch();
            search.setUserName(user.getUsername());
            List<String> list = new ArrayList<>();
            list.add(user.getAccount());
            search.setEmails(list);
            search.setTenantId(tenantId);
            PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(0, -1, search);
            Map<Integer,UserInfoGroup.Group> modules = new HashMap<>();
            for (UserAndModule userAndModule:userAndModules.getLists()){
                if (userAndModule.getUserName().equals(user.getUsername()) && userAndModule.getEmail().equals(user.getAccount()) && userAndModule.getToolRoleResources()!=null){
                    for (RoleResource roleResource:userAndModule.getToolRoleResources()){
                        ModuleEnum moduleEnum = ModuleEnum.getModuleEnum(roleResource);
                        if (moduleEnum!=null){
                            UserInfo.Module module = moduleEnum.getUserInfoModule();
                            if (modules.containsKey(module.getGroupId())){
                                modules.get(module.getGroupId()).getPrivilege().add(module);
                            }else{
                                UserInfoGroup.Group group = new UserInfoGroup.Group();
                                List<UserInfo.Module> moduleList = new ArrayList<>();
                                moduleList.add(module);
                                group.setGroupId(module.getGroupId());
                                group.setGroupName(module.getGroupName());
                                group.setPrivilege(moduleList);
                                modules.put(module.getGroupId(),group);
                            }
                        }
                    }

                }
            }

            info.setModules(new ArrayList<>(modules.values()));
            userTechCategoryList = userTechCategoryList.stream().filter(category->!technicalChildCategorys.contains(category.getGuid())).collect(Collectors.toList());
            userBusiCategoryList = userBusiCategoryList.stream().filter(category->!businessChildCategorys.contains(category.getGuid())).collect(Collectors.toList());
            info.setTechnicalCategory(userTechCategoryList);
            info.setBusinessCategory(userBusiCategoryList);
            info.setUserGroups(userDAO.getUserGroupNameByUserId(userId, tenantId));
            return info;
        } catch (AtlasBaseException e){
            throw e;
        } catch (Exception e) {
            LOG.error("获取用户信息失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }

    public PageResult<User> getUserListV2(String tenantId,Parameters parameters) throws AtlasBaseException {


        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        SecuritySearch search = new SecuritySearch();
        search.setTenantId(tenantId);
        search.setUserName(parameters.getQuery());
        PageResult<User> userPageResult = new PageResult<>();
        List<User> users = new ArrayList<>();
        try {
            PageResult<UserAndModule> userAndModules = tenantService.getUserAndModule(offset, limit, search);
            for (UserAndModule userAndModule:userAndModules.getLists()){
                User user = userDAO.getUserByName(userAndModule.getUserName(), userAndModule.getEmail());
                if (user==null){
                    break;
                }
                List<UserGroupIdAndName> userGroupName = userDAO.getUserGroupNameByUserId(user.getUserId(), tenantId);
                user.setUserGroups(userGroupName);
                users.add(user);
            }
            userPageResult.setLists(users);
            long userTotalSize = 0;
            if (users.size()!=0){
                userTotalSize = userAndModules.getTotalSize();
            }
            userPageResult.setCurrentSize(users.size());
            userPageResult.setTotalSize(userTotalSize);
            return userPageResult;
        } catch (AtlasBaseException e){
            throw e;
        }catch (Exception e) {
            LOG.error("获取用户列表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }
    public void updateGroupByUser(String userId,List<String> userGroups) throws AtlasBaseException {
        userDAO.deleteGroupByUser(userId);
        if (userGroups==null||userGroups.size()==0){
            return;
        }
        userDAO.addGroupByUser(userId,userGroups);
    }
}
