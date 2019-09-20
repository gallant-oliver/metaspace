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

package io.zeta.metaspace.web.rest;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.dataSource.DataSourceAuthorizeUser;
import io.zeta.metaspace.model.dataSource.DataSourceAuthorizeUserId;
import io.zeta.metaspace.model.dataSource.DataSourceBody;
import io.zeta.metaspace.model.dataSource.DataSourceConnection;
import io.zeta.metaspace.model.dataSource.DataSourceHead;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.util.AESUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("datasource")
@Singleton
@Service
public class DataSourceREST {
    private static final Logger LOG = LoggerFactory.getLogger(DataSourceREST.class);
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 添加数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean setNewDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException {

        try {
            dataSourceBody.setSourceId(UUID.randomUUID().toString());
            if (dataSourceService.isSourceName(dataSourceBody.getSourceName(),dataSourceBody.getSourceId())!=0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());


            dataSourceBody.setPassword(AESUtils.AESEncode(dataSourceBody.getPassword()));
            dataSourceService.setNewDataSource(dataSourceBody);
            return true;
        }catch (Exception e){
            LOG.warn("添加失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"添加失败"+e.getMessage());
        }
    }

    /**
     * 更新数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public boolean updateDateSource(DataSourceBody dataSourceBody) throws AtlasBaseException {

        try {
            if (dataSourceService.isSourceName(dataSourceBody.getSourceName(),dataSourceBody.getSourceId())!=0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
            }

            HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());

            if (dataSourceBody.isRely()){
                dataSourceService.updateRelyDataSource(dataSourceBody);
            }else{
                dataSourceService.updateNoRelyDataSource(dataSourceBody);
            }

            return true;
        }catch (Exception e){
            LOG.warn("更新失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"更新失败"+e.getMessage());
        }
    }

    /**
     * 删除数据源
     * @param sourceIds
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public boolean deleteDataSource(List<String> sourceIds) throws AtlasBaseException {
        for (String sourceId:sourceIds
             ) {
            String sourceName = dataSourceService.getSourceNameForSourceId(sourceId);
            if (sourceName!=null){
                HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), sourceName);
            }
        }
        try {
            dataSourceService.deleteDataSource(sourceIds);
            return true;
        }catch (Exception e){
            LOG.warn("删除失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"删除失败"+e.getMessage());
        }
    }

    /**
     * 获取数据源详情
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceInfo getDataSourceInfo(@PathParam("sourceId") String sourceId) throws AtlasBaseException {
        try {
            return dataSourceService.getDataSourceInfo(sourceId);
        }catch (Exception e){
            LOG.warn("获取数据源信息失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"获取数据源信息失败"+e.getMessage());
        }
    }

    /**
     * 测试连接
     * @param sourceId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/testjdbc/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean testConnection1(@PathParam("sourceId") String sourceId) throws AtlasBaseException {
        try {
            DataSourceConnection dataSourceConnection = dataSourceService.getDataSourceConnection(sourceId);
            return dataSourceService.testConnection(dataSourceConnection);
        }catch (Exception e){
            LOG.warn("连接失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"连接失败"+e.getMessage());
        }
    }

    /**
     * 测试连接
     * @param dataSourceConnection
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/testjdbc")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean testConnection2(DataSourceConnection dataSourceConnection) throws AtlasBaseException {
        try {
            return dataSourceService.testConnection(dataSourceConnection);
        }catch (Exception e){
            LOG.warn("连接失败");
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"连接失败"+e.getMessage());
        }
    }

    /**
     * 查询数据源
     * @param limit
     * @param offset
     * @param sortby
     * @param order
     * @param sourceName
     * @param sourceType
     * @param createTime
     * @param updateTime
     * @param updateUserName
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataSourceHead> searchDataSources(@QueryParam("limit") int limit,@QueryParam("offset") int offset,@QueryParam("sortby") String sortby,@QueryParam("order") String order,
                                                        @QueryParam("sourceName") String sourceName,@QueryParam("sourceType") String sourceType,@QueryParam("createTime") String createTime,
                                                        @QueryParam("updateTime") String updateTime,@QueryParam("updateUserName") String updateUserName) throws AtlasBaseException {
        try {
            return dataSourceService.searchDataSources(limit,offset,sortby,order,sourceName,sourceType,createTime,updateTime,updateUserName);
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"查询失败"+e.getMessage());
        }

    }

    @GET
    @Path("/authorize/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceAuthorizeUser getAuthorizeUserId(@PathParam("sourceId") String sourceId) throws AtlasBaseException {
        try {

            return dataSourceService.getAuthorizeUser(sourceId);
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"查询失败:"+e.getMessage());
        }
    }

    @GET
    @Path("/noAuthorize/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceAuthorizeUser getNoAuthorizeUserId(@PathParam("sourceId") String sourceId,@QueryParam("query") String query) throws AtlasBaseException {
        try {
            return dataSourceService.getNoAuthorizeUser(sourceId,query);
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"查询失败:"+e.getMessage());
        }
    }

    @POST
    @Path("/authorize")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response dataSourceAuthorize(DataSourceAuthorizeUserId dataSourceAuthorizeUserId) throws AtlasBaseException {
        try {
            dataSourceService.dataSourceAuthorize(dataSourceAuthorizeUserId);
            return Response.status(200).entity("success").build();
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源授权失败:"+e.getMessage());

        }
    }
}
