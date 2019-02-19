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
 * @date 2019/2/19 9:29
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 9:29
 */

import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.service.UsersService;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path("")
@Singleton
@Service
public class UserREST {

    @Autowired
    private UsersService userService;

    @GET
    @Path("/users/{userId}")
    public UserInfo UserInfo(@PathParam("userId") String userId) throws AtlasBaseException {
        try {
            return userService.getUserInfoById(userId);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/users")
    public PageResult<User> UserList(@QueryParam("query") String query, @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) throws AtlasBaseException {
        try {
            return userService.getUserList(query, offset, limit);
        } catch (Exception e) {
            throw e;
        }
    }
}
