package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.source.CodeInfo;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.web.service.SourceService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

@Singleton
@Service
@Path("source/info/test")
public class SourceREST {

    @Autowired
    private SourceService sourceService;

    /**
     * 获取数据源下某种类型的数据库
     *
     * @param dataSourceId
     * @param dataSourceType
     * @return
     */
    @GET
    @Path("database")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatabaseByType(@HeaderParam("tenantId") String tenantId, @QueryParam("dataSourceId") String dataSourceId, @QueryParam("dataSourceType") String dataSourceType) {
        List<DataBaseInfo> dataBaseInfoList = sourceService.getDatabaseByType(dataSourceId, dataSourceType, tenantId);
        return ReturnUtil.success(dataBaseInfoList);
    }

    /**
     * 获取可用用户列表
     *
     * @return
     */
    @GET
    @Path("user/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserList(@HeaderParam("tenantId") String tenantId) {
        List<User> userList = sourceService.getUserList();
        return ReturnUtil.success(userList);
    }

    /**
     * 获取源信息状态列表
     *
     * @return
     */
    @GET
    @Path("status/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getStatusList() {
        List<CodeInfo> codeInfoList = sourceService.getStatusList();
        return ReturnUtil.success(codeInfoList);
    }

}
