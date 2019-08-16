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

import io.zeta.metaspace.model.metadata.OperateLogRequest;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateEnum;
import io.zeta.metaspace.model.operatelog.OperateLog;
import io.zeta.metaspace.model.operatelog.OperateModule;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.OperateLogService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;


@Singleton
@Service
@Path("/operatelog")
public class OperateLogREST {

    @Autowired
    private OperateLogService operateLogService;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @POST
    @Path("/search")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<OperateLog> search(OperateLogRequest operateLogRequest) throws AtlasBaseException {
        return operateLogService.search(operateLogRequest);
    }


    @GET
    @Path("/types")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<OperateEnum> typeList() throws AtlasBaseException {
        return operateLogService.typeList();
    }

    @GET
    @Path("/results")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<OperateEnum> resultList() throws AtlasBaseException {
        return operateLogService.resultList();
    }

    @GET
    @Path("/module")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<OperateModule> moduleList() throws AtlasBaseException {
        return operateLogService.moduleList();
    }

}
