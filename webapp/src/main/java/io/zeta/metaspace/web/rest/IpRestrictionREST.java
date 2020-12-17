package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.ip.restriction.IpRestriction;
import io.zeta.metaspace.model.ip.restriction.IpRestrictionType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiPolyInfo;
import io.zeta.metaspace.web.service.IpRestrictionService;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.Arrays;
import java.util.UUID;

@Singleton
@Component
@Path("/ip-restriction")
public class IpRestrictionREST {

    @Autowired
    private IpRestrictionService ipRestrictionService;

    /**
     * 创建黑白名单
     */
    @POST
    @Path("")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean createIpRestrictionRule(IpRestriction ipRestriction, @HeaderParam("tenantId") String tenantId) {
        ipRestriction.setId(UUID.randomUUID().toString());
        ipRestrictionService.checkDuplicateName(ipRestriction.getId(), ipRestriction.getName(), tenantId);
        ipRestrictionService.createIpRestriction(ipRestriction, tenantId);
        return true;
    }

    /**
     * 更新黑白名单
     */
    @PUT
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean putIpRestrictionRule(@PathParam("id") String id, IpRestriction ipRestriction, @HeaderParam("tenantId") String tenantId) {
        ipRestriction.setId(id);
        ipRestrictionService.updateIpRestriction(ipRestriction, tenantId);
        return true;
    }

    /**
     * 黑白名单启用禁用
     */
    @PUT
    @Path("{id}/enable/{enable}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean putIpRestrictionRuleStatus(@PathParam("id") String id, @PathParam("enable") Boolean enable, @HeaderParam("tenantId") String tenantId) {
        ipRestrictionService.updateIpRestrictionEnable(id, enable, tenantId);
        return true;
    }

    /**
     * 删除黑白名单
     */
    @DELETE
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean deletedIpRestrictionRule(@PathParam("id") String ids, @HeaderParam("tenantId") String tenantId) {
        ipRestrictionService.deletedIpRestriction(Arrays.asList(ids.split(",")), tenantId);
        return true;
    }

    /**
     * 获取黑白名单详情
     */
    @GET
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public IpRestriction getIpRestrictionRule(@PathParam("id") String id, @HeaderParam("tenantId") String tenantId) {
        return ipRestrictionService.getIpRestriction(id);
    }

    /**
     * 获取黑白名单列表
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<IpRestriction> getIpRestrictionRuleList(@QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("query") String query, @QueryParam("type") String typeStr, @QueryParam("enable") String enableStr, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setQuery(query);
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        Boolean enable = StringUtils.isNotEmpty(enableStr) ? Boolean.valueOf(enableStr) : null;
        IpRestrictionType type = StringUtils.isNotEmpty(typeStr) ? IpRestrictionType.valueOf(typeStr) : null;

        return ipRestrictionService.getIpRestrictionList(parameters, enable, type, tenantId);

    }

    /**
     * 获取黑白名单关联API
     */
    @GET
    @Path("{id}/api")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<ApiPolyInfo> getIpRestrictionApiPolyInfoList(@PathParam("id") String id, @QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("status") String status, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        return ipRestrictionService.getIpRestrictionApiPolyInfoList(id, parameters, status, tenantId);
    }


}
