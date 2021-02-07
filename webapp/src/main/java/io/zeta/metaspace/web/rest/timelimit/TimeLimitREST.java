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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/4/12 17:14
 */
package io.zeta.metaspace.web.rest.timelimit;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/12 17:14
 */

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.share.QueryInfo;
import io.zeta.metaspace.model.share.QueryResult;
import io.zeta.metaspace.model.timelimit.TimeLimitRequest;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.timelimit.TimeLimitService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("")
@Singleton
@Service
public class TimeLimitREST {

    @Autowired
    private TimeLimitService timeLimitService;

    @PUT
    @Path("/timelimit/add")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Result add(TimeLimitRequest request, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            timeLimitService.addTimeLimit(request,tenantId);
            return ReturnUtil.success(); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/timelimit/list")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Result list(TimeLimitSearch search, @HeaderParam("tenantId")String tenantId) throws Exception {
        try {
            return ReturnUtil.success(timeLimitService.search(search,tenantId)); //无异常返回成功信息
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }


}
