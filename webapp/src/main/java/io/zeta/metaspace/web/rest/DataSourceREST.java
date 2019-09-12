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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.dataSource.*;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.DataSourceService;
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



    /**
     * 添加数据源
     * @param dataSourceBody
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Response setNewDataSource(DataSourceBody dataSourceBody) throws AtlasBaseException {

        try {
            if (dataSourceService.isSourceName(dataSourceBody.getSourceName())!=0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
            }
            HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());

            dataSourceBody.setSourceId(UUID.randomUUID().toString());
            dataSourceService.setNewDataSource(dataSourceBody);
            return Response.status(200).entity("success").build();
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
    @OperateType(UPDATE)
    public Response updataDateSource(DataSourceBody dataSourceBody) throws AtlasBaseException {

        try {
            if (dataSourceService.isSourceName(dataSourceBody.getSourceName())!=0){
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,"数据源名称存在");
            }

            HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), dataSourceBody.getSourceName());

            dataSourceService.updateDataSource(dataSourceBody);
            return Response.status(200).entity("success").build();
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
    @OperateType(OperateTypeEnum.DELETE)
    public Response deleteDataSource(List<String> sourceIds) throws AtlasBaseException {
        for (String sourceId:sourceIds
             ) {
            String sourceName = dataSourceService.getSourceNameForSourceId(sourceId);
            HttpRequestContext.get().auditLog(ModuleEnum.DATASOURCE.getAlias(), sourceName);
        }
        try {
            dataSourceService.deleteDataSource(sourceIds);
            return Response.status(200).entity("success").build();
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
     * @param dataSourceConnection
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/testjdbc")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean testConnection(DataSourceConnection dataSourceConnection) throws AtlasBaseException {
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
    @GET
    @Path("/export")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadExcelTemplate() throws AtlasBaseException, IOException, SQLException {
        try {
            File xlsxFile = dataSourceService.exportExcel();
            httpServletResponse.setContentType("application/msexcel;charset=utf-8");
            httpServletResponse.setCharacterEncoding("utf-8");
            String fileName = new String( new String(xlsxFile.getName()).getBytes(), "ISO-8859-1");
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
            if((name.length() < 6 || !name.substring(name.length() - 5).equals(".xlsx")) && (name.length() < 5 || !name.substring(name.length() - 4).equals(".xls"))) {
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







}
