package io.zeta.metaspace.web.util;

import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import com.google.gson.reflect.TypeToken;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.cache.MetaspaceContext;
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

    /**
     * 获取到的是账号截取的name，用于hive，hdfs等
     * @return
     * @throws AtlasBaseException
     */
    public static String getUserName() throws AtlasBaseException {
        String userName=null;
        String key= "";
        try{
            String ssoTicket = getSSOTicket();
            key = String.format("getUserData(ticket:%s)",ssoTicket);
            Object o = MetaspaceContext.get(key);
            if (o!=null&&o instanceof String){
                return o.toString();
            }
        }catch(Exception e){
            LOG.warn("获取缓存失败", e);
        }
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Map<String, String> user = GuavaUtils.getUserInfo(request.getHeader(TICKET_KEY));
            userName = user.get("LoginEmail").split("@")[0];
            if(userName==null||userName.equals(""))
                throw new AtlasBaseException(AtlasErrorCode.SSO_USER_ERROE);
            MetaspaceContext.set(key,userName);
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
            if (SSOTicket == null || SSOTicket == "") {
                SSOTicket = request.getParameter(TICKET_KEY);
            }
            if(SSOTicket==null||SSOTicket.equals(""))
                throw new AtlasBaseException(AtlasErrorCode.SSO_USER_ERROE);
            return SSOTicket;
        }catch (AtlasBaseException e){
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.SSO_CHECK_ERROE);
        }

    }

    /**
     * 仅可以获取UserId，Account，Username
     * @return
     * @throws AtlasBaseException
     */
    public static User getUserData() throws AtlasBaseException {
        User user = new User();
        String key= "";
        try{
            String ssoTicket = getSSOTicket();
            key = String.format("getUserData(ticket:%s)",ssoTicket);
            Object o = MetaspaceContext.get(key);
            if (o!=null&&o instanceof User){
                return (User) o;
            }
        }catch(Exception e){
            LOG.warn("获取缓存失败", e);
        }
        try {
            String ssoTicket = getSSOTicket();
            Map m = GuavaUtils.getUserInfo(ssoTicket);
            user.setUserId(m.get("AccountGuid").toString());
            user.setAccount(m.get("LoginEmail").toString());
            user.setUsername(m.get("DisplayName").toString());
            MetaspaceContext.set(key,user);
            return user;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.SSO_CHECK_ERROE);
        }
    }

}
