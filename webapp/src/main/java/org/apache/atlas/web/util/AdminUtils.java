package org.apache.atlas.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import java.util.Map;

public class AdminUtils {
    @Context
    private static HttpServletRequest httpServletRequest;
    public static String getUserName(){
        Map<String,String> user = (Map)httpServletRequest.getSession().getAttribute("user");
        String userName = user.get("LoginEmail").split("@")[0];
        return userName;
    }
}
