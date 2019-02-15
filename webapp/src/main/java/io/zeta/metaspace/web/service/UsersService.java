package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

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
}
