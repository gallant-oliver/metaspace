package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.Item;
import io.zeta.metaspace.model.result.PageResult;
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

    public void addUser(Map data) {
        String userId = data.get("AccountGuid").toString();
        if (userDAO.ifUserExists(userId).size() == 0) {
            String account = data.get("LoginEmail").toString();
            String displayName = data.get("DisplayName").toString();
            User user = new User();
            user.setUserId(userId);
            user.setAccount(account);
            user.setUsername(displayName);
            user.setRoleId(SystemRole.GUEST.getCode());
            userDAO.addUser(user);
        }
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
            Role roleTmp = userDAO.getRoleByUserId(userId);
            UserInfo.Role role = new UserInfo.Role();
            role.setRoleId(roleTmp.getRoleId());
            role.setRoleName(roleTmp.getRoleName());
            info.setRole(role);
            String roleId = role.getRoleId();
            //module
            List<UserInfo.Module> moduleList = userDAO.getModuleByRoleId(roleId);
            info.setModules(moduleList);
            //technicalCategory
            List<CategoryEntityV2> technicalCategoryList = userDAO.getTechnicalCategoryByRoleId(roleId);
            List<UserInfo.TechnicalCategory> userTechCategoryList = new ArrayList<>();
            for (CategoryEntityV2 entity : technicalCategoryList) {
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
                UserInfo.TechnicalCategory category = new UserInfo.TechnicalCategory(guid, name, level, level2Category);
                userTechCategoryList.add(category);
            }

            info.setTechnicalCategory(userTechCategoryList);
            //businessCategory
            List<CategoryEntityV2> businessCategoryList = userDAO.getBusinessCategoryByRoleId(roleId);
            List<UserInfo.BusinessCategory> userBusiCategoryList = new ArrayList<>();
            for (CategoryEntityV2 entity : businessCategoryList) {
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
                UserInfo.BusinessCategory category = new UserInfo.BusinessCategory(guid, name, level, level2Category);
                userBusiCategoryList.add(category);
            }
            info.setBusinessCategory(userBusiCategoryList);

            return info;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }


    public PageResult<User> getUserList(Parameters parameters) throws AtlasBaseException {

        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        try {
            PageResult<User> userPageResult = new PageResult<>();
            List<User> userList = null;
            userList = userDAO.getUserList(query, limit, offset);
            userPageResult.setLists(userList);
            long userCount = userDAO.getUsersCount(query);
            userPageResult.setCount(userList.size());
            userPageResult.setSum(userCount);
            return userPageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取用户列表失败");
        }
    }

    public List<Integer> ifPrivilege(List<String> categoryGuid, String tableGuid) {
        if (categoryGuid.size() > 0)
            return userDAO.ifPrivilege(categoryGuid, tableGuid);
        else
            return new ArrayList<>();
    }

    public Item getUserItems() throws AtlasBaseException {
        Item item = new Item();
        String userId = AdminUtils.getUserData().getUserId();
        Role roleByUserId = userDAO.getRoleByUserId(userId);
        if(roleByUserId.getStatus() == 0){
            item.setModules(new ArrayList<>());
            item.setRole(roleByUserId);
            return item;
        }
        List<Module> modules = userDAO.getModuleByUserId(userId);
        item.setRole(roleByUserId);
        item.setModules(modules);
        return item;
    }

    public List<Module> getModules(String userId) {
        return userDAO.getModuleByUserId(userId);
    }

    public String getRoleIdByUserId(String userId) {
        return roleDAO.getRoleIdByUserId(userId);
    }

    public Role getRoleByUserId(String userId){
        return userDAO.getRoleByUserId(userId);
    }
}
