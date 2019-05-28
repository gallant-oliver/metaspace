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
 * @date 2019/5/28 14:52
 */
package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.web.service.BusinessService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MarketService;
import io.zeta.metaspace.web.service.MetaDataService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Set;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/*
 * @description
 * @author sunhaoning
 * @date 2019/5/28 14:52
 */
@Singleton
@Service
@Path("/market/businesses")
public class MarketREST {
    private static final Logger PERF_LOG = LoggerFactory.getLogger(MarketREST.class);
    private static final int CATEGORY_TYPE = 1;
    @Context
    private HttpServletRequest httpServletRequest;
    @Context
    private HttpServletResponse httpServletResponse;
    @Autowired
    private BusinessService businessService;
    @Autowired
    MetaDataService metadataService;
    @Autowired
    DataShareService shareService;
    @Autowired
    MarketService marketService;

    /**
     * 获取全部目录
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Set<CategoryEntityV2> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MarketREST.getCategories()");
            }
            return marketService.getAll(CATEGORY_TYPE);
        }  finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 业务对象列表
     * @param categoryId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessListWithCondition(@PathParam("categoryId") String categoryId, Parameters parameters) throws AtlasBaseException {
        try {
            return businessService.getBusinessListByCategoryId(categoryId, parameters);
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        }
    }

    /**
     * 搜索业务对象(全局)
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/relations")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<BusinessInfoHeader> getBusinessList(Parameters parameters) throws AtlasBaseException {
        try {
            return marketService.getBusinessListByName(parameters);
        } catch (Exception e) {
            throw e;
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
            return marketService.getBusinessInfo(businessId);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取API信息列表
     * @param businessId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/{businessId}/datashare")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<APIInfoHeader> getBusinessTableRelatedAPI(@PathParam("businessId") String businessId, Parameters parameters) throws AtlasBaseException {
        try {
            return marketService.getBusinessTableRelatedAPI(businessId, parameters);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 业务对象关联技术信息列表
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
            return marketService.getRelatedTableList(businessId);
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
    public Table getTableInfoById(@PathParam("guid") String guid) throws AtlasBaseException {
        return metadataService.getTableInfoById(guid);
    }

}