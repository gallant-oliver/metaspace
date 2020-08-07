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

import io.zeta.metaspace.model.HookCheck;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.web.service.HookService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author lixiang03
 * @Data 2020/4/14 10:37
 */
@Path("hook")
@Singleton
@Service
public class HookREST {
    @Autowired
    HookService hookService;


    private static final Logger LOG = LoggerFactory.getLogger(HookREST.class);

    /**
     * 获取kafka消费积压情况
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/kafkaCheck")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result kafkaCheck() throws AtlasBaseException {
        try {
            long b = hookService.kafkaCheck();
            return ReturnUtil.success(b);
        }catch (Exception e) {
            LOG.error("获取kafka消费积压情况失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e, "获取kafka消费积压情况失败");
        }
    }

    /**
     * 获取hook配置情况
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/hookConfigCheck")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result hookConfigCheck() throws AtlasBaseException {
        try {
            List<String> map = hookService.hookConfigCheck();
            return ReturnUtil.success(map);
        }catch (Exception e) {
            LOG.error("获取hook配置情况失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取hook配置情况失败");
        }
    }

    /**
     * 获取hook的jar包加载情况
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/hookJar")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result hookJar() throws AtlasBaseException {
        try {
            boolean str = hookService.hookJar();
            return ReturnUtil.success(str);
        }catch (Exception e) {
            LOG.error("获取hookjar加载情况失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取hookjar加载情况失败");
        }
    }

    /**
     * 获取消费者线程情况
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/consumerThread")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result consumerThread() throws AtlasBaseException {
        try {
            Map<String, Boolean> alive = hookService.consumerThread();
            return ReturnUtil.success(alive);
        }catch (Exception e) {
            LOG.error("获取消费者线程情况失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取消费者线程情况失败");
        }
    }

    /**
     * 获取hook的所有检验
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/all")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result all() throws AtlasBaseException {
        try {
            HookCheck all = hookService.all();
            return ReturnUtil.success(all);
        }catch (Exception e) {
            LOG.error("获取所有检验失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST,e,"获取所有检验失败:"+e.getMessage());
        }
    }
}
