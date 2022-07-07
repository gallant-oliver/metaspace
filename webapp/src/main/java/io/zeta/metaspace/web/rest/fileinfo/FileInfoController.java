package io.zeta.metaspace.web.rest.fileinfo;


import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.fileinfo.FileComment;
import io.zeta.metaspace.model.fileinfo.FileCommentVO;
import io.zeta.metaspace.model.fileinfo.FileInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.fileinfo.FileCommentService;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

/**
 * 文件归档控制层
 *
 * @author w
 */
@Path("/file")
@Singleton
@Service
public class FileInfoController {

    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private FileCommentService fileCommentService;

    private static final Logger LOG = LoggerFactory.getLogger(FileInfoController.class);

    /**
     * 查询文件归档列表
     *
     * @return 返回文件归档列表
     */
    @GET
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult getFileInfo(@QueryParam("limit") int limit, @QueryParam("offset") int offset,
                                  @QueryParam("query") String query) {
        try {
            return fileInfoService.getList(query, limit, offset);
        } catch (Exception e) {
            LOG.error("文件归档列表查询失败", e);
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "文件归档列表查询失败");
        }
    }

    /**
     * 根据当前文件归档ID查询文件归档备注列表
     *
     * @param fileId 当前文件归档ID
     * @return 返回文件归档备注列表
     */
    @GET
    @Path("/comment/list/{fileId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getFileInfo(@PathParam("fileId") String fileId) {
        try {
            List<FileCommentVO> fileCommentList = fileCommentService.getFileCommentList(fileId);
            return ReturnUtil.success(fileCommentList);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "查询文件备注失败");
        }
    }

    /**
     * 添加文件归档备注
     *
     * @param fileComment 文件归档备注详情
     * @return 返回是否成功
     */
    @POST
    @Path("/comment/add")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.INSERT)
    public Result addFileComment(FileComment fileComment) {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.ARCHIVEDFILE.getAlias(), "添加文件归档备注");
            fileCommentService.addFileComment(fileComment);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加文件备注失败");
        }
    }

    /**
     * 移除文件归档备注（谁创建谁删除）
     *
     * @param commentId 文件备注ID
     * @return 返回是否成功
     */
    @GET
    @Path("/comment/remove/{commentId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result removeFileComment(@PathParam("commentId") String commentId) {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.ARCHIVEDFILE.getAlias(), "移除文件归档备注");
            fileCommentService.deleteFileComment(commentId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除文件备注失败");
        }
    }

}
