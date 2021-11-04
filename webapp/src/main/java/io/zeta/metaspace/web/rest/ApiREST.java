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
 * @date 2019/4/12 17:14
 */
package io.zeta.metaspace.web.rest;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/12 17:14
 */

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.dto.MailRequest;
import io.zeta.metaspace.model.share.QueryInfo;
import io.zeta.metaspace.model.share.QueryResult;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.UsersService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("api")
@Singleton
@Service
public class ApiREST {

    @Autowired
    private DataShareService shareService;
    @Context
    private HttpServletRequest httpServletRequest;

    @Autowired
    private UsersService usersService;

    @Autowired
    private DataManageService dataManageService;
    
    @POST
    @Path("/{version}/share/{url}")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public QueryResult queryAPIData(@PathParam("url") String url, QueryInfo info) throws Exception {
        try {
            String acceptHeader = httpServletRequest.getHeader("Accept");
            return shareService.queryAPIData(url, info, acceptHeader);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    @POST
    @Path("/mail")
    @Produces({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({Servlets.JSON_MEDIA_TYPE, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Result sendMail(@RequestBody MailRequest mailRequest){
        List<String> emails = usersService.getMailsByGroups(mailRequest.getToList());
        dataManageService.sendMail(emails,mailRequest.getSubject(),mailRequest.getContent());
        return ReturnUtil.success();
    }
}
