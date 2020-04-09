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
import io.zeta.metaspace.web.service.UserGroupService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author lixiang03
 * @Data 2020/2/27 14:08
 */
@Path("tenant")
@Singleton
@Service
public class TenantREST {
    private static final Logger LOG = LoggerFactory.getLogger(TenantREST.class);
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
        }catch (AtlasBaseException e){
            LOG.error("获取租户列表失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("获取租户列表失败",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "获取租户列表失败,请检查好是否配置正确");
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
        }catch (AtlasBaseException e){
            LOG.error("获取用户详情失败",e);
            throw e;
        }catch (Exception e){
            LOG.error("用户详情获取识别，请查看是否配置正确",e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"用户详情获取识别，请查看是否配置正确，错误信息：" + e.getMessage());

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
        } catch (AtlasBaseException e) {
            LOG.error("获取资源池失败", e);
            throw e;
        } catch (Exception e) {
            LOG.error("获取资源池失败，请查看是否配置正确", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "获取资源池失败，请查看是否配置正确，错误信息：" + e.getMessage());

        }

    }

}
