package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approvegroup.ApproveGroup;
import io.zeta.metaspace.model.source.CodeInfo;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.source.DataSourceInfo;
import io.zeta.metaspace.model.source.SourceUserInfo;
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
@Path("source/info")
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
    @Path("/database")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatabaseByType(@HeaderParam("tenantId") String tenantId, @QueryParam("dataSourceId") String dataSourceId, @QueryParam("dataSourceType") String dataSourceType) {
        List<DataBaseInfo> dataBaseInfoList = sourceService.getDatabaseByType(dataSourceId, dataSourceType);
        return ReturnUtil.success(dataBaseInfoList);
    }

    /**
     * 获取数据库类型列表
     *
     * @return
     */
    @GET
    @Path("datasource/type/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getTypeList() {
        List<CodeInfo> codeInfoList = sourceService.getTypeList();
        return ReturnUtil.success(codeInfoList);
    }

    /**
     * 获取可用用户列表
     *
     * @return
     */
    @GET
    @Path("userList")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserList(@HeaderParam("tenantId") String tenantId) {
        List<SourceUserInfo> userList = sourceService.getUserList();
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

    /**
     * 获取可用审批组列表
     * @return
     */
    @GET
    @Path("audit/approveGroupList")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApproveGroupList() {
        List<ApproveGroup> approveGroupList = sourceService.getApproveGroupList();
        return ReturnUtil.success(approveGroupList);
    }

    /**
     * 获取数据源列表
     * @return
     */
    @GET
    @Path("datasource/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatasourceList(){
        List<DataSourceInfo> datasourceList = sourceService.getDatasourceList();
        return ReturnUtil.success(datasourceList);
    }

}
