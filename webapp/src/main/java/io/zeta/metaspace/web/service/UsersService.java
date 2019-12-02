package io.zeta.metaspace.web.service;

import com.gridsum.gdp.library.commons.utils.StringUtils;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.Item;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CategoryDAO categoryDAO;
    @Autowired
    private RoleDAO roleDAO;

    @Bean(name = "getUserService")
    public UsersService getUserService() {
        return usersService;
    }

    public boolean isRole(String userId){
        List<String> roleId = userDAO.getRoleIdByUserId(userId);
        return roleId==null||roleId.size()==0;
    }


    public void addUser(Map data) {
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
            if (user.getUsername().equals("msadmin")) {
                role.setRoleId(SystemRole.ADMIN.getCode());
            } else {
                role.setRoleId(SystemRole.ADMIN.getCode());
            }
            user.setRoles(roles);
            userDAO.addUser(user);
        } /*else {
            User user = userDAO.getUser(userId);
            if(!account.equals(user.getAccount()) || !displayName.equals(user.getUsername())) {
                User userInfo = new User();
                userInfo.setUserId(userId);
                userInfo.setAccount(account);
                userInfo.setUsername(displayName);
                userDAO.updateUserInfo(userInfo);
            }
        }*/
    }

    @Transactional
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
                    addUserCategory(userTechCategoryList,entity,0);
                }
                if (technicalCategoryList!=null&&technicalCategoryList.size()!=0){
                    List<RoleModulesCategories.Category> techChildCategorys = roleDAO.getChildCategorys(technicalCategoryList.stream().map(categoryEntityV2 -> categoryEntityV2.getGuid()).collect(Collectors.toList()), 0);
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
                    addUserCategory(userBusiCategoryList,entity,1);
                }
                if (businessCategoryList!=null&&businessCategoryList.size()!=0){
                    List<RoleModulesCategories.Category> busiChildCategorys = roleDAO.getChildCategorys(businessCategoryList.stream().map(categoryEntityV2 -> categoryEntityV2.getGuid()).collect(Collectors.toList()), 1);
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
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户信息失败");
        }
    }

    public void addUserCategory(List list,CategoryEntityV2 entity,int categoryType){
        String guid = entity.getGuid();
        String name = entity.getName();
        String pathStr = categoryDAO.queryPathByGuid(guid);
        String path = pathStr.substring(1, pathStr.length() - 1);
        path = path.replace(",", ".").replace("\"", "");
        String level2Category = null;
        String[] pathArr = path.split("\\.");
        int level = pathArr.length;
        if (level >= 2) {
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
            //long userCount = userDAO.getUsersCount(query);
            long userTotalSize = 0;
            if (userList.size()!=0){
                userTotalSize = userList.get(0).getTotal();
            }
            //userPageResult.setOffset(offset);
            userPageResult.setCurrentSize(userList.size());
            userPageResult.setTotalSize(userTotalSize);
            return userPageResult;
        } catch (Exception e) {
            LOG.error(e.getMessage());
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

    public Item getUserItems() throws AtlasBaseException {
        Item item = new Item();
        String userId = AdminUtils.getUserData().getUserId();
        List<Role> roleByUserIds = userDAO.getRoleByUserId(userId);
        if(roleByUserIds.stream().allMatch(role -> role.getStatus() == 0)) {
            item.setModules(new ArrayList<>());
            item.setRoles(roleByUserIds);
            return item;
        }
        List<Module> modules = userDAO.getModuleByUserId(userId);
        for (Role role:roleByUserIds){
            String roleId = role.getRoleId();
            List<UserInfo.Module> moduleList = userDAO.getModuleByRoleId(roleId);
            for (UserInfo.Module module:moduleList){
                if (!modules.stream().anyMatch(userModule -> userModule.getModuleId()==module.getModuleId())){
                    modules.add(new Module(module));
                }
            }
        }
        item.setRoles(roleByUserIds);
        item.setModules(modules);
        return item;
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
            //userPageResult.setOffset(offset);
            userPageResult.setCurrentSize(userList.size());
            userPageResult.setTotalSize(userCount);
            return userPageResult;
        } catch (Exception e) {
            LOG.error("获取成员失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取成员失败");
        }
    }
}
