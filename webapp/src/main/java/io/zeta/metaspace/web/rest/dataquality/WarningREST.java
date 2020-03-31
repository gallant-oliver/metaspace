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
package io.zeta.metaspace.web.rest.dataquality;


import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.DELETE;

import io.zeta.metaspace.model.dataquality2.Warning;
import io.zeta.metaspace.model.dataquality2.WarningStatusEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.web.service.dataquality.WarningService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


/**
 * 告警
 */
@Singleton
@Service
@Path("/dataquality/warning")
public class WarningREST {

    @Autowired
    private WarningService warningService;

    /**
     * 待处理告警
     */
    @GET
    @Path("/todo")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<Warning> todo(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return warningService.list(WarningStatusEnum.TODO,tenantId);
    }

    /**
     * 已关闭告警
     */
    @GET
    @Path("/done")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(DELETE)
    public List<Warning> done(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        return warningService.list(WarningStatusEnum.DONE,tenantId);
    }

    /**
     * 关闭报警
     */
    @POST
    @Path("/close")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void close(List<String> idList) throws AtlasBaseException {
        warningService.close(idList);
    }

}
