package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithmInfo;
import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithmTest;
import io.zeta.metaspace.model.desensitization.DesensitizationRule;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.ApiPolyInfo;
import io.zeta.metaspace.web.service.DesensitizationService;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Singleton
@Service
@Path("/desensitization")
public class DesensitizationREST {

    @Autowired
    private DesensitizationService desensitizationService;

    /**
     * 创建规则
     */
    @POST
    @Path("")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean createDesensitizationRule(DesensitizationRule desensitizationRule, @HeaderParam("tenantId") String tenantId) {
        desensitizationRule.setId(UUID.randomUUID().toString());
        desensitizationService.checkDuplicateName(desensitizationRule.getId(), desensitizationRule.getName(), tenantId);
        desensitizationService.createDesensitizationRule(desensitizationRule, tenantId);
        return true;
    }

    /**
     * 更新规则
     */
    @PUT
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean putDesensitizationRule(@PathParam("id") String id, DesensitizationRule desensitizationRule, @HeaderParam("tenantId") String tenantId) {
        desensitizationRule.setId(id);
        desensitizationService.updateDesensitizationRule(desensitizationRule, tenantId);
        return true;
    }

    /**
     * 规则启用禁用
     */
    @PUT
    @Path("{id}/enable/{enable}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean putDesensitizationRuleStatus(@PathParam("id") String id, @PathParam("enable") Boolean enable, @HeaderParam("tenantId") String tenantId) {
        desensitizationService.updateDesensitizationRuleEnable(id, enable, tenantId);
        return true;
    }

    /**
     * 删除规则
     */
    @DELETE
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public boolean deletedDesensitizationRule(@PathParam("id") String ids, @HeaderParam("tenantId") String tenantId) {
        desensitizationService.deletedDesensitizationRule(Arrays.asList(ids.split(",")), tenantId);
        return true;
    }

    /**
     * 获取规则详情
     */
    @GET
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public DesensitizationRule getDesensitizationRule(@PathParam("id") String id, @HeaderParam("tenantId") String tenantId) {
        return desensitizationService.getDesensitizationRule(id);
    }

    /**
     * 获取规则列表
     */
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<DesensitizationRule> getDesensitizationRuleList(@QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("query") String query, @QueryParam("enable") String enableStr, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setQuery(query);
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        Boolean enable = StringUtils.isNotEmpty(enableStr) ? Boolean.valueOf(enableStr) : null;

        return desensitizationService.getDesensitizationRuleList(parameters, enable, tenantId);

    }

    /**
     * 获取规则关联API
     */
    @GET
    @Path("{id}/api")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<ApiPolyInfo> getDesensitizationApiPolyInfoList(@PathParam("id") String id, @QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("status") String status, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        return desensitizationService.getDesensitizationApiPolyInfoList(id, parameters, status, tenantId);
    }

    /**
     * 获取脱敏算法
     */
    @GET
    @Path("algorithm")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<DesensitizationAlgorithmInfo> getDesensitizationAlgorithm() {
        return desensitizationService.getDesensitizationAlgorithm();
    }

    /**
     * 测试脱敏算法
     */
    @POST
    @Path("algorithm/test")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Object testDesensitizationAlgorithm(DesensitizationAlgorithmTest desensitizationAlgorithmTest) {
        return desensitizationService.testDesensitizationAlgorithm(desensitizationAlgorithmTest);
    }

}
