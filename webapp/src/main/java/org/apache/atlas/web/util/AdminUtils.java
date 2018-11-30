package org.apache.atlas.web.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

public class AdminUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUtils.class);

    public static String getUserName() throws AtlasBaseException {
        String userName=null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Map<String, String> user = (Map) request.getSession().getAttribute("user");
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
}
