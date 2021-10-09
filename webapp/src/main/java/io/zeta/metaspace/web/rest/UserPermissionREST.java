package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.UserPermissionRequest;
import io.zeta.metaspace.web.service.UserPermissionService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

@Path("permission/users")
@Singleton
@Service
public class UserPermissionREST {
    private static Logger log = LoggerFactory.getLogger(UserPermissionREST.class);
    @Autowired
    private UserPermissionService userPermissionService;

    @GET
    @Path("/list")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result queryUserPermissionPageList(@HeaderParam("tenantId")String tenantId,
                                  @DefaultValue("0")@QueryParam("offset") int offset,
                                  @DefaultValue ("10") @QueryParam("limit") int limit,
                                  @QueryParam("name")String name){
        return ReturnUtil.success(userPermissionService.queryUserPermissionPageList(name, offset, limit));
    }

    @POST
    @Path("/saveList")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
   // @OperateType(OperateTypeEnum.INSERT)
    public Result addUserPermission(@RequestBody List<UserPermissionRequest> userPermissionList){
        if(CollectionUtils.isEmpty(userPermissionList)){
            return new Result("400","没有要保存的数据。");
        }
        int saveCount = userPermissionService.savePermission(userPermissionList);
        log.info("保存用户全局权限:{}",saveCount);
        return ReturnUtil.success();
    }

    @Path("/{userId}")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteUserPermissionByUserId(@PathParam(value = "userId") String userId){
        int delCount = userPermissionService.removeUserPermission(userId);
        log.info("移除用户全局权限操作:{}",delCount);
        return ReturnUtil.success();
    }

    @GET
    @Path("/selectList")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result querySSOUserPageList(@HeaderParam("tenantId")String tenantId,
                                              @DefaultValue("0")@QueryParam("offset") int offset,
                                              @DefaultValue ("10") @QueryParam("limit") int limit,
                                              @QueryParam("name")String name){
        return ReturnUtil.success(userPermissionService.querySSOUsers( offset, limit,name));
    }
}
