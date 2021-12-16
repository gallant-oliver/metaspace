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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.datasource.*;
import io.zeta.metaspace.model.dto.indices.ColumnDTO;
import io.zeta.metaspace.model.dto.indices.DataBaseDTO;
import io.zeta.metaspace.model.dto.indices.OptionalDataSourceDTO;
import io.zeta.metaspace.model.dto.indices.TableDTO;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIIdAndName;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.utils.AESUtils;
import io.zeta.metaspace.web.model.HiveConstant;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("datasource")
@Singleton
@Service
public class DataSourceREST {
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private DataSourceService dataSourceService;
    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;
    private Map<String,AtomicBoolean> importings = new HashMap<>();

    /**
     * 添加数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.INSERT)
    public boolean setNewDataSource(DataSourceBody dataSourceBody,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        dataSourceBody.setSourceId(UUID.randomUUID().toString());
        if (dataSourceService.isSourceName(dataSourceBody.getSourceName(),dataSourceBody.getSourceId(),tenantId)!=0){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
        }
        HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());


        dataSourceBody.setPassword(AESUtils.aesEncode(dataSourceBody.getPassword()));
        dataSourceService.setNewDataSource(dataSourceBody,false,tenantId);
        return true;
    }

    /**
     * 添加api数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/api")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.INSERT)
    public boolean setNewApiDataSource(DataSourceBody dataSourceBody,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        dataSourceBody.setSourceId(UUID.randomUUID().toString());
        if (dataSourceService.isSourceName(dataSourceBody.getSourceName(),dataSourceBody.getSourceId(),tenantId)!=0){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
        }

        HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());

        dataSourceBody.setPassword(AESUtils.aesEncode(dataSourceBody.getPassword()));
        dataSourceService.setNewDataSource(dataSourceBody,true,tenantId);
        return true;
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
    public boolean updateDateSource(DataSourceBody dataSourceBody,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {

        if (dataSourceService.isSourceName(dataSourceBody.getSourceName(),dataSourceBody.getSourceId(),tenantId)!=0){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
        }

        HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());

        if (dataSourceBody.isRely()){
            dataSourceService.updateRelyDataSource(dataSourceBody);
        }else{
            dataSourceService.updateNoRelyDataSource(dataSourceBody);
        }

        return true;
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
        if(CollectionUtils.isEmpty(sourceIds)){
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"要删除的数据源id为空");
        }
        List<String> sourceNameList= dataSourceService.getSourceNameForSourceIds(sourceIds);
        HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), String.join(",",sourceNameList));
       /* for (String sourceId:sourceIds) {
            String sourceName = dataSourceService.getSourceNameForSourceId(sourceId);
            if (sourceName!=null){
                HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), sourceName);
            }
        }*/
        dataSourceService.deleteDataSource(sourceIds);
        return true;
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
        DataSourceInfo dataSourceInfo =  dataSourceService.getDataSourceInfo(sourceId);
        dataSourceInfo.setPassword(null);
        dataSourceInfo.setUserName(dataSourceService.processingUsername(dataSourceInfo.getUserName()));
        return dataSourceInfo;
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
        return dataSourceService.testConnection(sourceId);
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
        return dataSourceService.testConnection(dataSourceConnection);
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
                                                        @QueryParam("updateTime") String updateTime,@QueryParam("updateUserName") String updateUserName,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        PageResult<DataSourceHead> pageResult= dataSourceService.searchDataSources(limit,offset,sortby,order,sourceName,sourceType,createTime,updateTime,updateUserName,true,tenantId);
        pageResult.getLists().stream().forEach(dataSourceHead -> {
            if (importings.containsKey(dataSourceHead.getSourceId())&&importings.get(dataSourceHead.getSourceId()).get()==true){
                dataSourceHead.setSynchronize(true);
            }else{
                dataSourceHead.setSynchronize(false);
            }
        });


        return pageResult;

    }

    /**
     * 查询api数据源
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
    @Path("/api")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DataSourceHead> searchApiDataSources(@QueryParam("limit") int limit,@QueryParam("offset") int offset,@QueryParam("sortBy") String sortby,@QueryParam("order") String order,
                                                        @QueryParam("sourceName") String sourceName,@QueryParam("sourceType") String sourceType,@QueryParam("createTime") String createTime,
                                                        @QueryParam("updateTime") String updateTime,@QueryParam("updateUserName") String updateUserName,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        PageResult<DataSourceHead> pageResult= dataSourceService.searchNotHiveDataSources(limit,offset,sortby,order,sourceName,sourceType,createTime,updateTime,updateUserName,true,tenantId);
        return pageResult;

    }

    /**
     * 导出数据源
     * @throws AtlasBaseException
     * @throws IOException
     * @throws SQLException
     */
    @POST
    @Path("/export")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadExcelTemplate(List<String> sourceIds,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException, IOException, SQLException {
        try {
            File xlsxFile = dataSourceService.exportExcel(sourceIds,tenantId);
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            String fileName = new String( xlsxFile.getName());
            // Content-disposition属性设置成以附件方式进行下载
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(FileUtils.readFileToByteArray(xlsxFile));
            os.close();
            xlsxFile.delete();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"下载报告失败");
        }
    }

    /**
     * 导入数据源
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceCheckMessage checkColumnNam(@FormDataParam("file") InputStream fileInputStream,
                                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        File file = null;
        try {
            String name =URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            if (!(name.endsWith(ExportDataPathUtils.EXCEL_FORMAT_XLSX) || name.endsWith(ExportDataPathUtils.EXCEL_FORMAT_XLS))) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
            }

            file = new File(name);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            if(file.length() > MAX_EXCEL_FILE_SIZE) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
            }
            return dataSourceService.importDataSource(file,tenantId);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"文件上传失败");
       } finally {
            if(Objects.nonNull(file) && file.exists()) {
                file.delete();
            }
        }
    }

    @GET
    @Path("/authorize/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceAuthorizeUser getAuthorizeUserId(@PathParam("sourceId") String sourceId) throws AtlasBaseException {
        return dataSourceService.getAuthorizeUser(sourceId,false);
    }

    @GET
    @Path("/noAuthorize/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceAuthorizeUser getNoAuthorizeUserId(@PathParam("sourceId") String sourceId,@QueryParam("query") String query,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataSourceService.getNoAuthorizeUser(sourceId,query,false,tenantId);
    }

    @POST
    @Path("/authorize")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response dataSourceAuthorize(DataSourceAuthorizeUserId dataSourceAuthorizeUserId) throws AtlasBaseException {
        dataSourceService.dataSourceAuthorize(dataSourceAuthorizeUserId);
        return Response.status(200).entity("success").build();
    }


    @GET
    @Path("/api/authorize/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceAuthorizeUser getApiAuthorizeUserId(@PathParam("sourceId") String sourceId) throws AtlasBaseException {
        return dataSourceService.getAuthorizeUser(sourceId,true);
    }

    @GET
    @Path("/api/noAuthorize/{sourceId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DataSourceAuthorizeUser getApiNoAuthorizeUserId(@PathParam("sourceId") String sourceId,@QueryParam("query") String query,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return dataSourceService.getNoAuthorizeUser(sourceId,query,true,tenantId);
    }

    @POST
    @Path("/api/authorize")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response dataSourceApiAuthorize(DataSourceAuthorizeUserId dataSourceAuthorizeUserId) throws AtlasBaseException {
        dataSourceService.dataSourceApiAuthorize(dataSourceAuthorizeUserId);
        return Response.status(200).entity("success").build();
    }

    /**
     * 获取更新用户
     * @param tenantId
     * @return
     * @throws Exception
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/updateUser")
    public List<String> getUpdateUserName(@HeaderParam("tenantId")String tenantId) throws Exception {
        return dataSourceService.getUpdateUserName(false,tenantId);
    }

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/api/updateUser")
    public List<String> getApiUpdateUserName(@HeaderParam("tenantId")String tenantId) throws Exception {
        return dataSourceService.getUpdateUserName(true,tenantId);
    }

    /**
     * 变更管理者
     * @param userId
     * @param sourceId
     * @return
     * @throws Exception
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/manager/{sourceId}/{userId}")
    public boolean updateManager(@PathParam("userId") String userId,@PathParam("sourceId") String sourceId) throws Exception {
        dataSourceService.updateManager(sourceId,userId);
        Response.status(200).entity("success").build();
        return true;
    }

    /**
     * 可成为管理者用户
     * @param tenantId
     * @return
     * @throws Exception
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/manager")
    public List<UserIdAndName> getManager(@HeaderParam("tenantId")String tenantId) throws Exception {
        return dataSourceService.getManager(tenantId);
    }

    /**
     * 获取ORACLE数据源的schema
     * @param limit
     * @param offset
     * @param dataSourceConnection
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/oracle/schemas/{limit}/{offset}")
    public PageResult getSchema(@PathParam("limit") int limit,@PathParam("offset") int offset,DataSourceConnection dataSourceConnection) throws AtlasBaseException {
        return dataSourceService.getSchema(limit,offset,dataSourceConnection);
    }

    /**
     * 获取对当前项目无权限的用户组
     * @param id
     * @param offset
     * @param limit
     * @param search
     * @return
     * @throws Exception
     */
    @GET
    @Path("/unuserGroups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getNoUserGroupByDataSource(@HeaderParam("tenantId")String tenantId, @QueryParam("datasourceid") String id, @QueryParam("offset")int offset, @QueryParam("limit")@DefaultValue("10") int limit, @QueryParam("search")String search)
            throws Exception
    {
        try {
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            parameters.setQuery(search);
            return ReturnUtil.success(dataSourceService.getNoUserGroupByDataSource(tenantId, parameters, id));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取无权限用户组失败");
        }
    }

    /**
     * 获取权限用户组列表
     * @param tenantId
     * @param id
     * @param offset
     * @param limit
     * @param search
     * @return
     * @throws Exception
     */
    @GET
    @Path("/userGroup/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupByDataSource(@HeaderParam("tenantId")String tenantId,@PathParam("id") String id, @QueryParam("offset")int offset, @QueryParam("limit")@DefaultValue("10") int limit, @QueryParam("search")String search)
            throws Exception
    {
        try {
            Parameters parameters = new Parameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            parameters.setQuery(search);
            return ReturnUtil.success(dataSourceService.getUserGroupByDataSource(tenantId,parameters,id));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取权限用户组失败");
        }
    }

    /**
     * 删除用户组权限
     * @param id
     * @param userGroups
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("userGroups/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteUserGroupByDataSource(@PathParam("id") String id, List<String> userGroups)
            throws Exception
    {
        try {
            dataSourceService.deleteUserGroupByDataSource(userGroups,id);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"删除用户组权限失败");
        }
    }

    /**
     * 更新用户组权限
     * @param id
     * @param privileges
     * @return
     * @throws Exception
     */
    @POST
    @Path("{id}/userGroups")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result updateUserGroupByDataSource(@PathParam("id") String id, DataSourcePrivileges privileges)
            throws Exception
    {
        try {
            dataSourceService.updateUserGroupByDataSource(privileges,id);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"更新用户组权限失败");
        }
    }

    /**
     * 新增用户组权限
     * @param id
     * @param privileges
     * @return
     * @throws Exception
     */
    @PUT
    @Path("{id}/privileges")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addUserGroup2DataSource(@PathParam("id") String id, DataSourcePrivileges privileges)
            throws Exception
    {
        try {
            dataSourceService.addUserGroup2DataSource(id,privileges);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"新增项目用户组权限失败");
        }

    }

    @POST
    @Path("/rely")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getAPIRely(List<String> ids)
            throws Exception
    {
        try {
            List<APIIdAndName> apiRely = dataSourceService.getAPIRely(ids);
            return ReturnUtil.success(apiRely);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST,e,"获取数据源依赖失败");
        }
    }

    /**
     * 获取数据源类型
     * @param type dbr 数据库登记的类型
     * @return
     */
    @GET
    @Path("/type")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataSourceType(@QueryParam("type") String type){
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(type);
        return ReturnUtil.success(dataSourceType);
    }

    /**
     * 获取用户组管理配置权限时数据库搜索的数据源类型
     * @return
     */
    @GET
    @Path("/database/typeForSearch")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataSourceTypeForSearch() {
        List<DataSourceTypeInfo> dataSourceType = dataSourceService.getDataSourceType(null);
        if (!dataSourceType.stream().filter(dataSourceTypeInfo -> {
            return HiveConstant.SOURCE_TYPE.equals(dataSourceTypeInfo.getName());
        }).findAny().isPresent()) {
            DataSourceTypeInfo hiveInfo = new DataSourceTypeInfo();
            hiveInfo.setName(HiveConstant.SOURCE_TYPE);
            dataSourceType.add(hiveInfo);
        }
        return ReturnUtil.success(dataSourceType);
    }

    /**
     * 根据用户组权限，获取数据源
     * @param tenantId
     * @return
     */
    @GET
    @Path("/dataSource")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalDataSource(@HeaderParam("tenantId") String tenantId) throws Exception {
        try {
            List<OptionalDataSourceDTO> optionalDataSourceDTOs = dataSourceService.getOptionalDataSource(tenantId);
            return ReturnUtil.success(optionalDataSourceDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 根据用户组权限，获取数据库
     * @param tenantId
     * @return
     */
    @GET
    @Path("/dataSource/db")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalDb(@QueryParam("sourceId") String sourceId, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (Objects.isNull(sourceId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不能为空");
        }
        List<DataBaseDTO> databaseList;
        try {
            databaseList=dataSourceService.getOptionalDb(sourceId,tenantId);
            return ReturnUtil.success(databaseList);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 获取数据库下数据表
     * @param tenantId
     * @return
     */
    @GET
    @Path("/dataSource/db/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalTable(@QueryParam("sourceId") String sourceId,@QueryParam("databaseId") String databaseId, @HeaderParam("tenantId") String tenantId) throws Exception {
        if (StringUtils.isBlank(sourceId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不能为空");
        }
        if (StringUtils.isBlank(databaseId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库id不能为空");
        }
        List<TableDTO> table;
        try {
            table=dataSourceService.getOptionalTable(sourceId,databaseId);
            return ReturnUtil.success(table);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 获取数据表字段
     * @param tableId
     * @return
     */
    @GET
    @Path("/dataSource/db/table/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getOptionalColumn(@QueryParam("sourceId") String sourceId,@QueryParam("tableId") String tableId) throws Exception {
        if (StringUtils.isBlank(sourceId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源id不能为空");
        }
        if (StringUtils.isBlank(tableId)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据表id不能为空");
        }
        try {
            List<ColumnDTO> columnDTOs = dataSourceService.getOptionalColumn(sourceId,tableId);
            return ReturnUtil.success(columnDTOs);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }
}
