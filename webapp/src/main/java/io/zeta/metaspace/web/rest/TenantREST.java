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

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author lixiang03
 * @Data 2020/2/27 14:08
 */
@Path("tenant")
@Singleton
@Service
public class TenantREST {
    @Autowired
    TenantService tenantService;
    @Autowired
    UsersService usersService;

    /**
     * 获取租户列表
     * @return
     * @throws Exception
     */
    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getTenantList()
            throws Exception
    {
        try {
            return ReturnUtil.success(tenantService.getTenants());
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取租户列表失败");
        }
    }

    /**
     * 初始化技术目录
     * @return
     */
    @GET
    @Path("init/technical/category")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result initTechnicalCategory(){
        try {
            return ReturnUtil.success(tenantService.initTechnicalCategory());
        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "初始化技术目录失败");
        }
    }

    /**
     * 获取模块列表
     * @param tenantId
     * @return
     * @throws Exception
     */
    @GET
    @Path("module")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getUsers(@HeaderParam("tenantId")String tenantId)
            throws Exception
    {
        try {
            return ReturnUtil.success(usersService.getUserItems(tenantId).getModules());
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "用户详情获取识别");
        }

    }

    /**
     * 获取资源池
     * @param tenantId
     * @return
     * @throws Exception
     */
    @GET
    @Path("/pools")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Result getPools(@HeaderParam("tenantId")String tenantId)
            throws Exception {
        try {
            return ReturnUtil.success(tenantService.getPools(tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取资源池失败，请查看是否配置正确");
        }

    }

}
