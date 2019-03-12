package io.zeta.metaspace.web.util;

import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import io.zeta.metaspace.model.user.User;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

public class AdminUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUtils.class);
    private static String TICKET_KEY = "X-SSO-FullticketId";
    public static String getUserName() throws AtlasBaseException {
        String userName=null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Map<String, String> user = GuavaUtils.getUserInfo(request.getHeader(TICKET_KEY));
            userName = user.get("LoginEmail").split("@")[0];
            if(userName==null||userName.equals(""))
                throw new AtlasBaseException(AtlasErrorCode.SSO_USER_ERROE);
            return userName;
        }catch (AtlasBaseException e){
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.SSO_CHECK_ERROE);
        }

    }
    public static String getSSOTicket() throws AtlasBaseException {
        String SSOTicket=null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            SSOTicket =  request.getHeader(TICKET_KEY);
            if(SSOTicket==null||SSOTicket.equals(""))
                throw new AtlasBaseException(AtlasErrorCode.SSO_USER_ERROE);
            return SSOTicket;
        }catch (AtlasBaseException e){
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.SSO_CHECK_ERROE);
        }

    }
    public static User getUserData() throws AtlasBaseException {
        User user = new User();
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Map m = GuavaUtils.getUserInfo(request.getHeader(TICKET_KEY));
            user.setUserId(m.get("AccountGuid").toString());
            user.setAccount(m.get("LoginEmail").toString());
            user.setUsername(m.get("DisplayName").toString());
            return user;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.SSO_CHECK_ERROE);
        }

    }
}
