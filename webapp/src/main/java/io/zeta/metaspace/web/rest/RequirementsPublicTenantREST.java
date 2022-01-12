package io.zeta.metaspace.web.rest;

import com.google.common.collect.ImmutableList;
import com.gridsum.gdp.library.commons.utils.DateTimeUtils;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.requirements.*;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.HdfsService;
import io.zeta.metaspace.web.service.RequirementsPublicTenantService;
import io.zeta.metaspace.web.util.ReturnUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.zeta.metaspace.web.model.CommonConstant.HEADER_TENANT_ID;

/**
 * 需求管理 - 公共租户
 */
@Singleton
@Service
@Slf4j
@Path("public/tenant/requirements")
@Consumes(Servlets.JSON_MEDIA_TYPE)
@Produces(Servlets.JSON_MEDIA_TYPE)
public class RequirementsPublicTenantREST {
    private static final List<String> ENABLE_UPLOAD_FILE_TYPE =
            ImmutableList.of("pdf", "doc", "xls", "xlsx", "png", "jpg");

    @Autowired
    private RequirementsPublicTenantService publicTenantService;
    @Autowired
    private HdfsService hdfsService;

    @POST
    @Path("/paged/resource")
    public PageResult<ResourceDTO> pagedResource(@QueryParam("tableId") String tableId,
                                                 Parameters parameters) {
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID无效!");
        return publicTenantService.pagedResource(tableId, parameters);
    }

    /**
     * 需求下发
     *
     * @param
     * @throws
     */
    @POST
    @Path("/grant/{requirementId}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result grant(@PathParam("requirementId") String requirementId) {
        try {
            publicTenantService.grant(requirementId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "需求下发失败");
        }
    }

    /**
     * 删除需求
     *
     * @param
     * @throws
     */
    @DELETE
    @Path("")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result delete(List<String> guids) {
        try {
            publicTenantService.deleteRequirements(guids);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "需求删除失败");
        }
    }

    /**
     * 反馈结果
     *
     * @param
     * @throws
     */
    @GET
    @Path("/{requirementId}/feedback")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result feedback(@PathParam("requirementId") String requirementId, @QueryParam("resourceType") Integer resourceType) {
        try {
            FeedbackResultDTO result = publicTenantService.getFeedbackResult(requirementId, resourceType);
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取需求反馈结果失败");
        }
    }

    @POST
    @Path("/create/resource")
    @OperateType(OperateTypeEnum.INSERT)
    public Result createdRequirement(RequirementDTO requirementDTO) {
        Assert.notNull(requirementDTO, "需求对象为空");
        String resourceId = publicTenantService.createdRequirement(requirementDTO);
        HttpRequestContext.get().auditLog(ModuleEnum.REQUIREMENTMANAGEMENTPUBLIC.getAlias(), requirementDTO.getName());
        return ReturnUtil.success((Object) resourceId);
    }

    @PUT
    @Path("/edit/resource")
    @OperateType(OperateTypeEnum.UPDATE)
    public void editedRequirement(RequirementDTO requirementDTO) {
        Assert.notNull(requirementDTO, "需求对象为空");
        publicTenantService.editedRequirement(requirementDTO);
        HttpRequestContext.get().auditLog(ModuleEnum.REQUIREMENTMANAGEMENTPUBLIC.getAlias(), requirementDTO.getGuid());
    }

    @GET
    @Path("/query/columns")
    public List<RequirementColumnDTO> queryColumnsByTableId(@QueryParam("tableId") String tableId) {
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID无效!");
        return publicTenantService.queryColumnsByTableId(tableId);
    }

    @GET
    @Path("/query/issuedInfo")
    public RequirementIssuedDTO queryIssuedInfo(@QueryParam("tableId") String tableId,
                                                @QueryParam("sourceId") String sourceId) {
        Assert.isTrue(StringUtils.isNotBlank(tableId), "数据表ID无效!");
        Assert.isTrue(StringUtils.isNotBlank(sourceId), "数据源ID无效!");
        return publicTenantService.queryIssuedInfo(tableId, sourceId);
    }

    /**
     * 上传文件到 hdfs
     */
    @POST
    @Path("/upload/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @OperateType(OperateTypeEnum.INSERT)
    public FileDTO uploadFile(@HeaderParam(HEADER_TENANT_ID) String tenantId,
                              @FormDataParam("file") InputStream fileInputStream,
                              @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) {
        try {
            String fileName = new String(
                    contentDispositionHeader.getFileName().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8);
            String fileType = FilenameUtils.getExtension(fileName);
            //判断文件格式是否支持
            if (!ENABLE_UPLOAD_FILE_TYPE.contains(fileType)) {
                log.error("不支持的附件上传格式:{}", fileType);
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,
                        "支持上传的文件类型:".concat(ENABLE_UPLOAD_FILE_TYPE.toString()));
            }

            //tenantId 使用租户id作为上传文件子目录
            long timestamp = DateTimeUtils.currentTimeMillis();
            String uploadPath = hdfsService.uploadFile(
                    fileInputStream,
                    timestamp + "." + fileType,
                    tenantId + "/" + DateTimeUtils.formatTime(timestamp, DateTimeUtils.YYYYMMDD));
            HttpRequestContext.get().auditLog(ModuleEnum.REQUIREMENTMANAGEMENTPUBLIC.getAlias(), "上传附件:".concat(fileName));
            return new FileDTO(fileName, uploadPath);
        } catch (Exception e) {
            throw new AtlasBaseException(
                    String.format("文件上传失败:%s", e.getMessage()),
                    AtlasErrorCode.INTERNAL_UNKNOWN_ERROR,
                    e);
        }
    }

    /**
     * 上传文件-批量
     */
    @POST
    @Path("/upload/batch/file")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @OperateType(OperateTypeEnum.INSERT)
    public void uploadFileBatch(@HeaderParam(HEADER_TENANT_ID) String tenantId, FormDataMultiPart form) throws Exception {
//        List<FormDataBodyPart> files = form.getFields("file");
//        for (FormDataBodyPart file : files) {
//            InputStream input = file.getValueAs(InputStream.class);
//            System.out.println(org.apache.commons.io.IOUtils.readLines(input));
//            System.out.println(file.cd.fileName);
//        }
    }

    /**
     * 下载附件
     */
    @GET
    @Path("/download/file")
    public void downloadFile(@Context HttpServletResponse response,
                             @DefaultValue("") @QueryParam("fileName") String fileName,
                             @QueryParam("filePath") String filePath) {
        Assert.isTrue(StringUtils.isNotBlank(filePath), "文件路径不能为空");
        try {
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            if (StringUtils.isNotBlank(fileName)) {
                response.setContentType("application/force-download");// 应用程序强制下载
                response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", fileName));
            }
            IOUtils.copyBytes(
                    hdfsService.getFileInputStream(filePath),
                    response.getOutputStream(),
                    4096,
                    true);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.INTERNAL_UNKNOWN_ERROR, e, "文件下载失败");
        }

    }

    @GET
    @Path("/feedback/detail/base")
    public Result getDetailBase(@QueryParam("id") String id, @QueryParam("type") Integer type) {
        FeedbackDetailBaseDTO result = publicTenantService.getDetailBase(id, type);
        return ReturnUtil.success(result);
    }

    /**
     * 查询需求管理列表
     *
     * @param param
     * @return
     */
    @POST
    @Path("list")
    public PageResult getListByCreatorPage(RequireListParam param) {
        return publicTenantService.getListByCreatorPage(param);
    }
}
