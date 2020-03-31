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
 * @date 2019/2/21 18:20
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/21 18:20
 */

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.business.BusinessTableList;
import io.zeta.metaspace.model.business.ColumnPrivilege;
import io.zeta.metaspace.model.business.ColumnPrivilegeRelation;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MetaDataService;
import io.zeta.metaspace.web.service.TenantService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Singleton
@Service
@Path("/businessManage")
public class BusinessManageREST {

    private static final Logger PERF_LOG = LoggerFactory.getLogger(BusinessManageREST.class);
    private static final int BUSINESS_CATEGORY_TYPE = 1;
    private static final int TECHNICAL_CATEGORY_TYPE = 0;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;

    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private BusinessService businessService;
    @Autowired
    MetaDataService metadataService;
    @Autowired
    DataShareService shareService;

    @GET
    @Path("/departments")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryEntityV2> getAllDepartment(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return dataManageService.getAllDepartments(BUSINESS_CATEGORY_TYPE,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 业务对象搜索(业务对象管理)
     * @param parameter
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessListWithManage(BusinessQueryParameter parameter,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCondition(parameter,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    @POST
    @Path("/{businessId}/datashare")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(@PathParam("businessId") String businessId, Parameters parameters,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessTableRelatedAPI(businessId, parameters,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    @GET
    @Path("/datashare/{apiGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public APIInfo getAPIInfo(@PathParam("apiGuid")String guid) throws AtlasBaseException {
        try {
            return shareService.getAPIInfo(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    /**
     * 测试API
     * @param randomName
     * @param parameter
     * @return
     * @throws Exception
     */
    @POST
    @Path("/datashare/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<LinkedHashMap> testAPI(@PathParam("randomName") String randomName, QueryParameter parameter) throws Exception {
        try {
            List<LinkedHashMap> result = shareService.testAPI(randomName, parameter);
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    @PUT
    @Path("/datashare/test/{randomName}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void stopTestAPI(@PathParam("randomName") String randomName) throws Exception {
        try {
            shareService.cancelAPIThread(randomName);
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    /**
     * 更新技术
     * @param businessId
     * @param tableIdList
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/{businessId}")
    @OperateType(UPDATE)
    public Response updateTechnicalInfo(@PathParam("businessId") String businessId, BusinessTableList tableIdList,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        BusinessInfo businessInfo = businessService.getBusinessInfo(businessId,tenantId);
        HttpRequestContext.get().auditLog(ModuleEnum.BUSINESSMANAGE.getAlias(), businessInfo.getName());
        try {
            businessService.addBusinessAndTableRelation(businessId, tableIdList);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取技术目录
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/technical/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getAllCategory(@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return TenantService.defaultTenant.equals(tenantId) ? dataManageService.getAll(TECHNICAL_CATEGORY_TYPE) : dataManageService.getAllByUserGroup(TECHNICAL_CATEGORY_TYPE, tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取技术目录关联表
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelation(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessManageREST.getCategoryRelation(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuidFilter(categoryGuid, relationQuery,tenantId);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 业务对象查询表关联
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/technical/table/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessManageREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableNameFilter(relationQuery, TECHNICAL_CATEGORY_TYPE,tenantId);
        } catch (Exception e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 业务对象详情
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public BusinessInfo getBusiness(@PathParam("businessId") String businessId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getBusinessInfo(businessId,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 业务对象关联技术信息详情
     * @param businessId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/{businessId}/technical")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public TechnologyInfo getBusinessRelatedTables(@PathParam("businessId") String businessId,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            return businessService.getRelatedTableList(businessId,tenantId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 更新业务对象信息
     *
     * @param businessId
     * @param business
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/{businessId}/business")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Response updateBusiness(@PathParam("businessId") String businessId, BusinessInfo business, @HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.BUSINESSMANAGE.getAlias(), business.getName());
        try {
            businessService.updateBusiness(businessId, business,tenantId);
            return Response.status(200).entity("success").build();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取表详情
     *
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/table/{guid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Table getTableInfoById(@PathParam("guid") String guid,@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        return businessService.getTableInfoById(guid,tenantId);
    }

    @DELETE
    @Path("/{businessId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response deleteBusiness(@PathParam("businessId") String businessId) throws AtlasBaseException {
        try {
            businessService.deleteBusiness(businessId);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 添加权限字段
     * @param privilege
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/privilege/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addColumnPrivilege(ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            businessService.addColumnPrivilege(privilege);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 删除权限字段
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/privilege/column/{privilegeId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response delColumnPrivilege(@PathParam("privilegeId")Integer guid) throws AtlasBaseException {
        try {
            businessService.deleteColumnPrivilege(guid);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
        return Response.status(200).entity("success").build();
    }

    @PUT
    @Path("/privilege/column/{privilegeId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response updateColumnPrivilege(@PathParam("privilegeId")Integer guid, ColumnPrivilege privilege) throws AtlasBaseException {
        try {
            privilege.setGuid(guid);
            businessService.updateColumnPrivilege(privilege);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
        return Response.status(200).entity("success").build();
    }

    @GET
    @Path("/privilege/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<ColumnPrivilege> getColumnPrivilegeList() throws AtlasBaseException {
        try {
            return businessService.getColumnPrivilegeList();
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    @GET
    @Path("/privilege/{privilegeId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<String> getColumnPrivilegeValue(@PathParam("privilegeId")Integer guid) throws AtlasBaseException {
        try {
            return businessService.getColumnPrivilegeValue(guid);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    /**
     * 添加权限字段关联
     * @param relation
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/relation/column")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addColumnPrivilegeRelation(ColumnPrivilegeRelation relation) throws AtlasBaseException {
        try {
            businessService.addColumnPrivilegeRelation(relation);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "添加失败");
        }
        return Response.status(200).entity("success").build();
    }
}
