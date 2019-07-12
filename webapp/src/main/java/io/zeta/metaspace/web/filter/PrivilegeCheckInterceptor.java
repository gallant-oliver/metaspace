// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/11 18:11
 */
package io.zeta.metaspace.web.filter;

import com.gridsum.gdp.library.commons.utils.UUIDUtils;

import io.zeta.metaspace.model.operatelog.OperateLog;
import io.zeta.metaspace.model.operatelog.OperateResultEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.utils.DateUtils;
import io.zeta.metaspace.web.dao.ApiModuleDAO;
import io.zeta.metaspace.web.service.OperateLogService;
import io.zeta.metaspace.web.service.RoleService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.FilterUtils;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/11 18:11
 */

@Component
public class PrivilegeCheckInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(PrivilegeCheckInterceptor.class);

    @Autowired
    UsersService usersService;
    @Autowired
    RoleService roleService;
    @Autowired
    ApiModuleDAO apiModuleDAO;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Object response = invocation.proceed();
        String requestURL = request.getRequestURL().toString();
        if (FilterUtils.isSkipUrl(requestURL)) {
            return response;
        } else {
            String prefix = requestURL.replaceFirst(".*/api/metaspace/", "").replaceAll("/.*", "");
            String userId = "";
            String username = "";
            try {
                userId = AdminUtils.getUserData().getUserId();
                username = AdminUtils.getUserData().getUsername();
            } catch (AtlasBaseException e) {
                LOG.warn("查询用户信息失败", e);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询用户信息失败");
            }

            Method method = invocation.getMethod();
            Path path = method.getAnnotation(Path.class);
            String pathStr = path.value();
            String urlStr = prefix + "/" + pathStr;

            String requestMethod = request.getMethod();

            Role roleByUserId = usersService.getRoleByUserId(userId);
            if (roleByUserId.getStatus() == 0) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户的角色已被禁用");
            }
            UserInfo userInfo = usersService.getUserInfoById(userId);
            List<UserInfo.Module> modules = userInfo.getModules();
            List<Integer> moduleIds = modules.stream().map(module -> module.getModuleId()).collect(Collectors.toList());

            Integer moduleId = apiModuleDAO.getModuleByPathAndMethod(urlStr, requestMethod);

            if("privilegecheck".equals(prefix.toLowerCase().trim())) {
                if (roleByUserId.getStatus() == 0) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户的角色已被禁用");
                }
                String privilegeType = "";
                String privilegeGuid = "";
                try {
                    privilegeType = request.getParameter("privilegeType");
                    privilegeGuid = request.getParameter("privilegeGuid");
                    switch (privilegeType) {
                        case "table": {
                            //到这里再查库
                            String roleIdByUserId = usersService.getRoleIdByUserId(userId);
                            //超级管理员直接过
                            if (roleIdByUserId.equals(SystemRole.ADMIN.getCode())) {
                                return response;
                            }
                            Map<String, RoleModulesCategories.Category> userCatagory = getUserCatagory(userId);
                            Collection<RoleModulesCategories.Category> categories = userCatagory.values();
                            ArrayList<String> categoryGuids = new ArrayList<>();
                            for (RoleModulesCategories.Category category : categories) {
                                if (category.isShow())
                                    categoryGuids.add(category.getGuid());
                            }
                            List<Integer> sum = usersService.ifPrivilege(categoryGuids, privilegeGuid);
                            if (sum.size() > 0) {
                                return response;
                            } else {
                                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,  "当前用户权限不足");
                            }
                        }
                        default:
                            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,  "当前用户权限不足");
                    }
                } catch (Exception e) {
                    LOG.warn("用户" + username + "没有" + privilegeType + " " + privilegeGuid + " 的权限", e);
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户没有该表的权限");
                }
            } else {

            }

            switch (prefix) {
                case "privilegecheck":


                case "technical":
                case "businesses":
                case "businessManage":
                case "role":
                case "privilege":
                    if (roleByUserId.getStatus() == 0) {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户的角色已被禁用");
                    }
                    if(moduleIds.contains(moduleId) || Objects.isNull(moduleId)) {
                        return response;
                    } else {
                        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户没有当前调用接口的权限");
                    }

                default:
                    return response;

            }

        }

    }


    private Map<String, RoleModulesCategories.Category> getUserCatagory(String userId) {
        //技术目录
        Map<String, RoleModulesCategories.Category> userStringCategoryMap = roleService.getUserStringCategoryMap(roleService.getRoleIdBYUserId(userId).getRoleId(), 0);
        return userStringCategoryMap;
    }
}
