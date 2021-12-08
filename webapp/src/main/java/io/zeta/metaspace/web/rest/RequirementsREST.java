package io.zeta.metaspace.web.rest;

import com.sun.org.apache.regexp.internal.RE;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.RequirementsResultDTO;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.web.service.RequirementsService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;

/**
 *
 */
@Path("requirements")
@Singleton
@Service
@RestController
public class RequirementsREST {

    @Autowired
    private RequirementsService requirementsService;

    @POST
    @Path("test")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void test() {
    }

    /**
     * 需求下发
     *
     * @param
     * @throws
     */
    @PUT
    @Path("/grant/{guid}")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result grant(@PathParam("guid") String guid) {
        try {
            requirementsService.grant(guid);
            return ReturnUtil.success();
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
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
            requirementsService.deleteRequirements(guids);
            return ReturnUtil.success();
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    /**
     * 需求详情
     *
     * @param
     * @throws
     */
    @GET
    @Path("/detail")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void detail() {

    }

    /**
     * 表是否为重要表、保密表
     *
     * @param
     * @throws
     */
    @GET
    @Path("/table/{tableId}/status")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result getTableStatus(@PathParam("tableId") String tableId) {
        try {
            SourceInfoDeriveTableInfo tableStatus = requirementsService.getTableStatus(tableId);
            return ReturnUtil.success(tableStatus);
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    /**
     * 需求处理
     *
     * @param
     * @throws
     */
    @POST
    @Path("/handle")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public Result handle(RequirementsResultDTO resultDTO, ) {
        try {
            requirementsService.handle(resultDTO);
            return ReturnUtil.success();
        }
        catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"需求下发失败");
        }
    }

    @POST
    @Path("test")
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    public void test() {
    }
}
