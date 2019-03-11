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

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessQueryParameter;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.MetaDataService;
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

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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

    @GET
    @Path("/departments")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryEntityV2> getAllDepartment() throws AtlasBaseException {
        try {
            return dataManageService.getAllDepartments(BUSINESS_CATEGORY_TYPE);
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
    public PageResult<BusinessInfoHeader> getBusinessListWithManage(BusinessQueryParameter parameter) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCondition(parameter);
        } catch (Exception e) {
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
    public Response updateTechnicalInfo(@PathParam("businessId") String businessId, List<String> tableIdList) throws AtlasBaseException {
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
    public List<RoleModulesCategories.Category> getAllCategory() throws AtlasBaseException {
        try {
            return dataManageService.getAll(TECHNICAL_CATEGORY_TYPE);
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
    public PageResult<RelationEntityV2> getCategoryRelation(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessManageREST.getCategoryRelation(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuid(categoryGuid, relationQuery);
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
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "BusinessManageREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableName(relationQuery, TECHNICAL_CATEGORY_TYPE);
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
    public BusinessInfo getBusiness(@PathParam("businessId") String businessId) throws AtlasBaseException {
        try {
            return businessService.getBusinessInfo(businessId);
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
    public TechnologyInfo getBusinessRelatedTables(@PathParam("businessId") String businessId) throws AtlasBaseException {
        try {
            return businessService.getRelatedTableList(businessId);
        } catch (Exception e) {
            throw e;
        }
    }
}
