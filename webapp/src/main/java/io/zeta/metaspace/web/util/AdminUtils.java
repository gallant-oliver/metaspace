package io.zeta.metaspace.web.util;

import javax.servlet.http.HttpServletRequest;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.model.user.User;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AdminUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUtils.class);
    private static String TICKET_KEY = "X-SSO-FullticketId";
    public final static String USER_CACHE_EXPIRE = "user.info.expire";
    private static Configuration conf;
    private static int USER_INFO_EXPIRE ;
    private static Cache<String, User> userCache;
    static{
        try {
            conf = ApplicationProperties.get();
            USER_INFO_EXPIRE = conf.getInt(USER_CACHE_EXPIRE, 30);
            userCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(USER_INFO_EXPIRE, TimeUnit.MINUTES).build();
        } catch (AtlasException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取到的是账号截取的name，用于hive，hdfs等
     * @return
     * @throws AtlasBaseException
     */
    public static String getUserName() throws AtlasBaseException {
        String userName=null;

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String cacheKey = request.getHeader(TICKET_KEY);
            User user = userCache.getIfPresent(cacheKey);
            if (user!=null){
                return user.getAccount().split("@")[0];
            }
            Map m = GuavaUtils.getUserInfo(request.getHeader(TICKET_KEY));
            user = new User();
            user.setUserId(m.get("AccountGuid").toString());
            user.setAccount(m.get("LoginEmail").toString());
            user.setUsername(m.get("DisplayName").toString());
            userName = user.getAccount().split("@")[0];
            if(userName==null||userName.equals(""))
                throw new AtlasBaseException(AtlasErrorCode.SSO_USER_ERROE);
            userCache.put(cacheKey,user);
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
        try {
            String ssoTicket = getSSOTicket();
            String cacheKey = ssoTicket;
            User user = userCache.getIfPresent(cacheKey);
            if (user!=null){
                return user;
            }
            user = new User();
            Map m = GuavaUtils.getUserInfo(ssoTicket);
            user.setUserId(m.get("AccountGuid").toString());
            user.setAccount(m.get("LoginEmail").toString());
            user.setUsername(m.get("DisplayName").toString());
            userCache.put(cacheKey,user);
            return user;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.SSO_CHECK_ERROE);
        }
    }
    public static void cleanCache() throws AtlasBaseException {
        userCache.invalidate(getSSOTicket());
    }

}
