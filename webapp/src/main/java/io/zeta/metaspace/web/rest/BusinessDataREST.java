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
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.ColumnCheckMessage;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.util.ExportDataPathUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FileUtils;
import org.mybatis.spring.MyBatisSystemException;
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
import java.util.Objects;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @author lixiang03
 * @Data 2020/8/5 11:49
 */

@Singleton
@Service
@Path("/data")
public class BusinessDataREST {
    private static final Logger PERF_LOG = LoggerFactory.getLogger(BusinessREST.class);
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private BusinessService businessService;
    @Context
    private HttpServletResponse response;

    @POST
    @Path("/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByName(parameters,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取业务对象关联表
     * @param businessId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{businessId}/tables")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<TableHeader> getBussinessRelatedTableList(@PathParam("businessId") String businessId, Parameters parameters) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessREST.getBussinessRelatedTableList()");
            }
            return businessService.getPermissionBusinessRelatedTableList(businessId, parameters);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取表字段列表
     * @param tableGuid
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/{guid}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getTableColumnList(@PathParam("guid") String tableGuid, Parameters parameters, @DefaultValue("columnName") @QueryParam("sortAttribute") final String sortAttribute, @DefaultValue("asc") @QueryParam("sort") final String sort) throws AtlasBaseException {
        try {
            return businessService.getTableColumnList(tableGuid, parameters, sortAttribute, sort);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    @PUT
    @Path("/table/{guid}/columns")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String editTableColumnDisplayName(@PathParam("guid") String tableGuid, Column column) throws AtlasBaseException {
        try {
            businessService.editSingleColumnDisplayName(tableGuid, column);
            return "success";
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 编辑表显示名称
     * @param tableHeader
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/table")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String editTableDisplayName(TableHeader tableHeader) throws AtlasBaseException {
        try {
            businessService.editTableDisplayName(tableHeader);
            return "success";
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 导入字段中文别名Excel
     * @param tableGuid
     * @param fileInputStream
     * @param contentDispositionHeader
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/excel/import/{guid}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public ColumnCheckMessage checkColumnName(@PathParam("guid") String tableGuid, @FormDataParam("file") InputStream fileInputStream,
                                              @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws AtlasBaseException {
        File file = null;
        try {
            String name = URLDecoder.decode(contentDispositionHeader.getFileName(), "GB18030");
            file = ExportDataPathUtils.fileCheck(name, fileInputStream);
            return businessService.importColumnWithDisplayText(tableGuid, file);
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

    /**
     * 下载编辑字段中文别名模板
     * @param tableGuid
     * @throws AtlasBaseException
     * @throws IOException
     * @throws SQLException
     */
    @GET
    @Path("/excel/{tableGuid}/template")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void downloadExcelTemplate(@PathParam("tableGuid") String tableGuid) throws AtlasBaseException, IOException, SQLException {
        try {
            File xlsxFile = businessService.exportExcel(tableGuid);
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
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "下载报告失败");
        }
    }
}
