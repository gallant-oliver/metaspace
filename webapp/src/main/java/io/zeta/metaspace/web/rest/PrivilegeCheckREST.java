package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.metadata.LineageDepthInfo;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.util.EntityUtil;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.Map;

@Path("privilegecheck")
@Singleton
@Service
public class PrivilegeCheckREST {
    @Autowired
    MetaDataService metaDataService;

    /**
     * 获取表详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/metadata/table/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table getTableInfoById(@QueryParam("bizTreeId")String bizTreeId,@PathParam("guid") String guid, @HeaderParam("tenantId")String tenantId,
                                  @RequestParam("sourceId")@DefaultValue("") String sourceId,
                                  @QueryParam("currentTenantId")String currentTenantId) throws AtlasBaseException {
        if(StringUtils.isNotBlank(currentTenantId)){
            tenantId = currentTenantId;
        }else{
            if(StringUtils.isNotBlank(bizTreeId)){
                Map<String,String> map = EntityUtil.decodeBusinessId(bizTreeId);
                if(map != null){
                    tenantId = map.get("tenantId");
                }
            }
        }
        return metaDataService.getTableInfoById(guid,tenantId, sourceId);
    }

    /**
     * 表血缘深度详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/metadata/table/lineage/depth/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public LineageDepthInfo getTableLineageDepthInfo(@PathParam("guid") String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        return metaDataService.getTableLineageDepthInfo(guid);
    }

    /**
     * 字段血缘深度详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/metadata/column/lineage/depth/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public LineageDepthInfo getColumnLineageDepthInfo(@PathParam("guid") String guid) throws AtlasBaseException {
        Servlets.validateQueryParamLength("guid", guid);
        return metaDataService.getColumnLineageDepthInfo(guid);
    }
}
