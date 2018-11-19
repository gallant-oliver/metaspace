package org.apache.atlas.web.util;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Map;

public class AdminUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AdminUtils.class);

    public static String getUserName(){
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            Map<String,String> user = (Map)request.getSession().getAttribute("user");
            String userName = user.get("LoginEmail").split("@")[0];
            return userName;
        }catch (Exception e){
            LOG.error(e.getMessage(), e);
            return "";
        }

    }
}
