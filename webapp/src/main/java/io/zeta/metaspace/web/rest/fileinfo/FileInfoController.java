package io.zeta.metaspace.web.rest.fileinfo;


import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.fileinfo.FileComment;
import io.zeta.metaspace.model.fileinfo.FileInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.fileinfo.FileInfoService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

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
        return null;
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
    public PageResult getFileInfo(@PathParam("fileId") String fileId) {
        return null;
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
    public Result addFileComment(FileComment fileComment) {
        return ReturnUtil.success();
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
    public Result removeFileComment(@PathParam("commentId") String commentId) {
        return ReturnUtil.success();
    }

    /**
     * 添加文件归档
     *
     * @param fileInfo 文件归档详情
     * @return 返回是否成功
     */
    @POST
    @Path("/add")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result addFileInfo(FileInfo fileInfo) {
        return ReturnUtil.success();
    }

}
