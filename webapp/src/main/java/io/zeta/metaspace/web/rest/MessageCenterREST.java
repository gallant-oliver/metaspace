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
 * @date 2019/7/24 10:47
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author liwenfeng
 * @date 2022/7/2 10:47
 */

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.entities.MessageEntity;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.MessageCenterService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

@Service
@Path("/message")
public class MessageCenterREST {
    @Autowired
    MessageCenterService messageCenterService;

    /**
     * 我的消息分页列表
     *
     * @param type
     * @param status
     * @param search
     * @param offset
     * @param limit
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Map<String, Object>> getMyMessageList(@QueryParam("type") Integer type, @HeaderParam("tenantId") String tenantId,
                                                            @QueryParam("status") Integer status, @QueryParam("search") String search,
                                                            @QueryParam("offset") long offset, @QueryParam("limit") long limit) throws AtlasBaseException {
        return messageCenterService.getMyMessageList(type, tenantId, status, search, offset, limit);
    }

    /**
     * 消息详情
     *
     * @param id
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/detail/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getMessageDetail(@PathParam("id") String id, @HeaderParam("tenantId") String tenantId) {
        return messageCenterService.getMessageDetail(id, tenantId);
    }

    /**
     * 未读消息数量
     *
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/unreadnum")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUnReadNum(@HeaderParam("tenantId") String tenantId) {
        return messageCenterService.getUnReadNum(tenantId);
    }

    /**
     * 批量标记已读
     *
     * @param ids
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/batch/toread")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result batchToRead(List<String> ids) {
        return messageCenterService.batchToRead(ids);
    }

    /**
     * 批量删除消息
     *
     * @param ids
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/delete")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result batchDelte(List<String> ids, @QueryParam("delAll") String delAll, @HeaderParam("tenantId") String tenantId) {
        return messageCenterService.batchDelte(ids, delAll, tenantId);
    }

    /**
     * 新增消息接口——仅用于测试，后面删除
     *
     * @param
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/add")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result batchDelte(MessageEntity message, @HeaderParam("tenantId") String tenantId) {
        return messageCenterService.addMessage(message, tenantId);
    }

}
