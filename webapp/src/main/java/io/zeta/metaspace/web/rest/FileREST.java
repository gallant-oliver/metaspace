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

import com.sun.jersey.multipart.FormDataParam;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.PageList;
import io.zeta.metaspace.model.file.File;
import io.zeta.metaspace.model.file.FileRequest;
import org.apache.atlas.query.QueryParams;
import io.zeta.metaspace.utils.BytesUtils;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.utils.ParamChecker;
import io.zeta.metaspace.web.service.FileService;
import io.zeta.metaspace.web.util.HdfsUtils;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("file")
@Singleton
@Service
public class FileREST {

    private static final Logger log = LoggerFactory.getLogger(FileREST.class);


    /**
     * 上传文件到指定路径
     *
     * @param fileInputStream
     * @param filePath
     * @return
     * @throws Exception
     */
    @POST
    @Path("/upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response upload(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("filePath") String filePath,
            @FormDataParam("override") boolean override) throws Exception {
        if (!override && HdfsUtils.exist(filePath)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件已存在: " + filePath);
        }

        String dir = filePath.substring(0, filePath.lastIndexOf("/"));
        if(org.apache.commons.lang3.StringUtils.isBlank(dir)){
            dir = "/";
        }
        if(!HdfsUtils.canAccess(dir,"w")){
            throw new AtlasBaseException(AtlasErrorCode.UNAUTHORIZED_ACCESS, filePath, "上传文件");
        }
        try {
            HdfsUtils.uploadFile(fileInputStream, filePath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件没有权限: " + filePath);
        }
        return Response.status(200).entity("success").build();
    }


    @GET
    @Path("/download")
    public Response download(@QueryParam("filePath") String filePath) throws Exception {

        if (!HdfsUtils.exist(filePath)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "文件不存在: " + filePath);
        }

        if(!HdfsUtils.canAccess(filePath,"r")){
            throw new AtlasBaseException(AtlasErrorCode.UNAUTHORIZED_ACCESS, filePath, "下载文件");
        }

        String filename = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        StreamingOutput fileStream = new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream) throws IOException, WebApplicationException {
                try {
                    InputStream inputStream = HdfsUtils.downloadFile(filePath);
                    IOUtils.copyBytes(inputStream, outputStream, 4096, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = " + filename)
                .build();
    }


    /**
     * 文件/目录 列表
     *
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageList<File> list(FileRequest request) throws Exception {
        ParamChecker.checkPaging(request.getOffset(), request.getLimit());
        QueryParams params = QueryParams.getNormalizedParams(request.getLimit(), request.getOffset());

        Pair<Integer, List<FileStatus>> pair = FileService.listStatus(request.getFilePath(), request.getModifyDate(), request.getOwner(), params.offset(), params.limit(), request.getOrderBy(), request.getSortType());
        List<File> list = pair.getRight().stream()
                .map(f -> {
                    String type = f.isDirectory() ? "Directory" : "File";
                    String size = BytesUtils.humanReadableByteCount(f.getLen());
                    return new File(f.getPath().getName(), f.getPath().toUri().getPath(), size, DateUtils.formatDateTime(f.getModificationTime()), f.getOwner(), type, f.getPermission().toString());
                })
                .collect(Collectors.toList());
        return new PageList<>(params.offset(), pair.getLeft(), list);
    }

    /**
     * 文件搜索
     *
     * @param request
     * @return
     * @throws Exception
     */
    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageList<File> search(FileRequest request) throws Exception {

        ParamChecker.checkPaging(request.getOffset(), request.getLimit());
        QueryParams params = QueryParams.getNormalizedParams(request.getLimit(), request.getOffset());
        Pair<Integer, List<FileStatus>> pair = FileService.listFiles(request.getFileName(), request.getModifyDate(), request.getOwner(), params.offset(), params.limit(), true, request.getOrderBy(), request.getSortType());

        List<File> list = pair.getRight().stream()
                .map(f -> {
                    String type = f.isDirectory() ? "Directory" : "File";
                    String size = BytesUtils.humanReadableByteCount(f.getLen());
                    return new File(f.getPath().getName(), f.getPath().toUri().getPath(), size, DateUtils.formatDateTime(f.getModificationTime()), f.getOwner(), type, f.getPermission().toString());
                })
                .collect(Collectors.toList());
        return new PageList<>(params.offset(), pair.getLeft(), list);
    }

}

