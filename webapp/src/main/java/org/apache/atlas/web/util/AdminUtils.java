package org.apache.atlas.web.util;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Map;

public class AdminUtils {
    public static String getUserName(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Map<String,String> user = (Map)request.getSession().getAttribute("user");
        String userName = user.get("LoginEmail").split("@")[0];
        return userName;
    }
}
