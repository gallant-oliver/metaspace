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

import com.google.gson.Gson;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.dataSource.*;
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
import io.zeta.metaspace.web.model.Progress;
import io.zeta.metaspace.web.model.TableSchema;
import io.zeta.metaspace.web.service.DataSourceService;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.util.AESUtils;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
import javax.ws.rs.core.MediaType;
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
    private static final int MAX_EXCEL_FILE_SIZE = 10*1024*1024;
    private AtomicBoolean importing = new AtomicBoolean(false);
    @Autowired
    private MetaDataService metadataService;



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
            if (Objects.isNull(dataSourceConnection)){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源id不存在");
            }
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
    public void downloadExcelTemplate(List<String> sourceIds) throws AtlasBaseException, IOException, SQLException {
        try {
            File xlsxFile = dataSourceService.exportExcel(sourceIds);
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            //String fileName = new String( new String(xlsxFile.getName()).getBytes(), "ISO-8859-1");
            String fileName = new String( xlsxFile.getName());
            // Content-disposition属性设置成以附件方式进行下载
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            OutputStream os = httpServletResponse.getOutputStream();
            os.write(FileUtils.readFileToByteArray(xlsxFile));
            os.close();
            xlsxFile.delete();
        }  catch (Exception e) {
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "下载报告失败");
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
                                                 @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws AtlasBaseException {
        File file = null;
        try {
            String name =URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            if(name.length() < 6 || !name.substring(name.length() - 5).equals(".xlsx")) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件格式错误");
            }

            file = new File(name);
            FileUtils.copyInputStreamToFile(fileInputStream, file);
            if(file.length() > MAX_EXCEL_FILE_SIZE) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件大小不能超过10M");
            }
            return dataSourceService.importDataSource(file);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.toString());
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

    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/import/{databaseType}/{sourceId}")
    public Response synchronizeMetaData(@PathParam("databaseType") String databaseType, @PathParam("sourceId")String sourceId) throws Exception {
        TableSchema  tableSchema = new TableSchema();
        tableSchema.setInstance(sourceId);
        try {
            if (!dataSourceService.isAuthorizeUser(sourceId)) {
                throw new AtlasBaseException(AtlasErrorCode.UNAUTHORIZED_ACCESS, "当前用户", "不能采集元数据,没有该数据源的权限");
            }
        } catch (AtlasBaseException e) {
            LOG.error("获取当前用户的userId出错", e);
            throw new AtlasBaseException(AtlasErrorCode.UNAUTHORIZED_ACCESS, "当前用户", "不能采集元数据,没有该数据源的权限");
        }

        if (!importing.getAndSet(true)) {
            CompletableFuture.runAsync(() -> {
                metadataService.synchronizeMetaData(databaseType, tableSchema);
                importing.set(false);
                metadataService.refreshCache();
            });
        } else {
            return Response.status(400).entity(String.format("%s元数据正在同步中", databaseType)).build();
        }
        return Response.status(202).entity(String.format("%s元数据增量同步已开始", databaseType)).build();
    }

    @GET
    @Path("/import/progress/{databaseType}/{sourceId}")
    public Response importProgress(@PathParam("databaseType") String databaseType,@PathParam("sourceId") String sourceId) throws Exception {
        Progress progress = metadataService.importProgress(databaseType, sourceId);
        return Response.status(200).entity(new Gson().toJson(progress)).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/updateUser")
    public List<String> getUpdateUserName() throws Exception {
        try {
            return dataSourceService.getUpdateUserName();
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"查询失败:"+e.getMessage());
        }
    }
}
