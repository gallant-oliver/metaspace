package io.zeta.metaspace.web.filter;

import com.google.gson.Gson;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.service.RoleService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Component
public class PrivilegeFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(PrivilegeFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        ServletContext servletContext = httpServletRequest.getServletContext();
        WebApplicationContext requiredWebApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        UsersService usersService = (UsersService) requiredWebApplicationContext.getBean("getUserService");
        RoleService roleService = (RoleService) requiredWebApplicationContext.getBean("getRoleService");

        String requestURL = httpServletRequest.getRequestURL().toString();
        if (requestURL.contains("v2/entity/uniqueAttribute/type/") || requestURL.endsWith("api/metaspace/v2/entity/") || requestURL.contains("/api/metaspace/admin/status")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String check = requestURL.replaceFirst(".*/api/metaspace/", "").replaceAll("/.*", "");
        String userId = "";
        String username = "";
        try {
            userId = AdminUtils.getUserData().getUserId();
            username = AdminUtils.getUserData().getUsername();
        } catch (AtlasBaseException e) {
            LOG.warn("查询用户信息失败", e);
            loginSkip(httpServletResponse, "查询用户信息失败");
            return;
        }
        Role roleByUserId = usersService.getRoleByUserId(userId);
        switch (check) {
            case "privilegecheck": {
                if(roleByUserId.getStatus() == 0){
                    loginSkip(httpServletResponse, "当前用户的角色已被禁用");
                    return;
                }
                String privilegeType = "";
                String privilegeGuid = "";
                try {
                     privilegeType = httpServletRequest.getParameter("privilegeType");
                     privilegeGuid = httpServletRequest.getParameter("privilegeGuid");


                    switch (privilegeType) {
                        case "table": {
                            //到这里再查库
                            String roleIdByUserId = usersService.getRoleIdByUserId(userId);
                            //超级管理员直接过
                            if (roleIdByUserId.equals(SystemRole.ADMIN.getCode())) {
                                filterChain.doFilter(servletRequest, servletResponse);
                                return;
                            }
                            Map<String, RoleModulesCategories.Category> userCatagory = getUserCatagory(roleService, userId);
                            Collection<RoleModulesCategories.Category> categories = userCatagory.values();
                            ArrayList<String> categoryGuids = new ArrayList<>();
                            for (RoleModulesCategories.Category category : categories) {
                                if (category.isShow()) categoryGuids.add(category.getGuid());
                            }
                            List<Integer> sum = usersService.ifPrivilege(categoryGuids, privilegeGuid);
                            if (sum.size() > 0) {
                                filterChain.doFilter(servletRequest, servletResponse);
                                return;
                            } else {
                                loginSkip(httpServletResponse, "当前用户权限不足");
                            }
                            break;
                        }
                        default:
                            loginSkip(httpServletResponse, "当前用户权限不足");
                    }
                    break;
                } catch (Exception e) {
                    LOG.warn("用户" + username + "没有" + privilegeType + " " + privilegeGuid + " 的权限", e);
                    loginSkip(httpServletResponse, "当前用户没有该表的权限");
                }
                break;
            }
            case "technical": {
                if(roleByUserId.getStatus() == 0){
                    loginSkip(httpServletResponse, "当前用户的角色已被禁用");
                    return;
                }
                UserInfo userInfo = getUserInfo(httpServletResponse, usersService, userId);
                List<UserInfo.Module> modules = userInfo.getModules();
                for (UserInfo.Module module : modules) {
                    if (module.getModuleId() == SystemModule.TECHNICAL_CHECK.getCode() || module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }
                }
                loginSkip(httpServletResponse, "当前用户没有该菜单的权限");
                break;
            }
            case "businesses": {
                if(roleByUserId.getStatus() == 0){
                    loginSkip(httpServletResponse, "当前用户的角色已被禁用");
                    return;
                }
                UserInfo userInfo = getUserInfo(httpServletResponse, usersService, userId);
                List<UserInfo.Module> modules = userInfo.getModules();
                for (UserInfo.Module module : modules) {
                    if (module.getModuleId() == SystemModule.BUSINESSE_CHECK.getCode() || module.getModuleId() == SystemModule.BUSINESSE_OPERATE.getCode()) {
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }
                }
                loginSkip(httpServletResponse, "当前用户没有该菜单的权限");
                break;
            }
            case "businessManage": {
                if(roleByUserId.getStatus() == 0){
                    loginSkip(httpServletResponse, "当前用户的角色已被禁用");
                    return;
                }
                UserInfo userInfo = getUserInfo(httpServletResponse, usersService, userId);
                List<UserInfo.Module> modules = userInfo.getModules();
                for (UserInfo.Module module : modules) {
                    if (module.getModuleId() == SystemModule.BUSINESSE_MANAGE.getCode()) {
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }
                }
                loginSkip(httpServletResponse, "当前用户没有该菜单的权限");
                break;
            }
            case "role":
            case "privilege": {
                if(roleByUserId.getStatus() == 0){
                    loginSkip(httpServletResponse, "当前用户的角色已被禁用");
                    return;
                }
                UserInfo userInfo = getUserInfo(httpServletResponse, usersService, userId);
                List<UserInfo.Module> modules = userInfo.getModules();
                for (UserInfo.Module module : modules) {
                    if (module.getModuleId() == SystemModule.PRIVILEGE_MANAGE.getCode()) {
                        filterChain.doFilter(servletRequest, servletResponse);
                        return;
                    }
                }
                loginSkip(httpServletResponse, "当前用户没有该菜单的权限");
                break;
            }
            default:{
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
        };
    }

    private void loginSkip(HttpServletResponse httpServletResponse, String error) throws IOException {
        httpServletResponse.setStatus(403);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("text/plain;charset=utf-8");
        PrintWriter writer = httpServletResponse.getWriter();
        HashMap<String, String> hashMap = new HashMap();
        hashMap.put("errorMessage", error);
        String j = new Gson().toJson(hashMap);
        writer.print(j);
    }

    private UserInfo getUserInfo(HttpServletResponse response, UsersService usersService, String userId) throws IOException {
        UserInfo userInfo = null;

        try {
            userInfo = usersService.getUserInfoById(userId);

        } catch (Exception e) {
            loginSkip(response, "获取用户权限信息失败");
        }
        return userInfo;
    }

    private Map<String, RoleModulesCategories.Category> getUserCatagory(RoleService roleService, String userId) {

        //技术目录
        Map<String, RoleModulesCategories.Category> userStringCategoryMap = roleService.getUserStringCategoryMap(roleService.getRoleIdBYUserId(userId).getRoleId(), 0);
        return userStringCategoryMap;
    }

    @Override
    public void destroy() {

    }
}
