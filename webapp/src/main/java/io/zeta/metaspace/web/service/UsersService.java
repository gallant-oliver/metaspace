package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.dao.UserDAO;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class UsersService {
    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private UsersService usersService;
    @Bean(name="getUserService")
    public UsersService getUserService(){
        return usersService;
    }
    public void addUser(Map data) {
        String userId = data.get("AccountGuid").toString();
        if(userDAO.ifUserExists(userId).size()==0){
            String account = data.get("LoginEmail").toString();
            String displayName=data.get("DisplayName").toString();
            User user = new User();
            user.setUserId(userId);
            user.setAccount(account);
            user.setUsername(displayName);
            user.setRoleId(SystemRole.GUEST.getCode());
            userDAO.addUser(user);
        }
    }


    private final  Integer TECHNICAL_TYPE = 0;

    @Transactional
    public UserInfo getUserInfoById(String userId) throws AtlasBaseException {
        try {
            UserInfo info = new UserInfo();
            //user
            User user = userDAO.getUser(userId);
            info.setUser(user);
            //role
            Role role = userDAO.getRoleByUserId(userId);
            info.setRole(role);
            String roleId = role.getRoleId();
            //module
            List<UserInfo.Module> moduleList = userDAO.getModuleByRoleId(roleId);
            //technicalCategory
            List<UserInfo.TechnicalCategory> technicalCategoryList = userDAO.getTechnicalCategoryByRoleId(roleId);
            info.setTechnicalCategory(technicalCategoryList);
            //businessCategory
            List<UserInfo.BusinessCategory> businessCategoryList = userDAO.getBusinessCategoryByRoleId(roleId);
            info.setBusinessCategory(businessCategoryList);

            return info;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    public PageResult<User> getUserList(String query, Integer limit, Integer offset) throws AtlasBaseException {
        try {
            PageResult<User> userPageResult = new PageResult<>();
            List<User> userList = userDAO.getUserList(query, limit, offset);
            userPageResult.setLists(userList);
            long userCount = userDAO.getUsersCount(query);
            userPageResult.setCount(userCount);
            userPageResult.setSum(userList.size());
            return userPageResult;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }
}
