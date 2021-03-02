package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.web.service.Approve.ApproveService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import javax.ws.rs.*;

@Path("approve")
@Singleton
@Service
public class ApproveREST {
    @Autowired
    private ApproveService approveService;

    /**
     *
     */

    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApproveList(
            @HeaderParam("tenantId") String tenantId, Parameters params) throws AtlasBaseException {
        try {

            //参数检查
            if(StringUtils.isBlank(params.getSortby())){
                params.setSortby("createTime");  //默认排序字段
            }

            if(StringUtils.isBlank(params.getOrder())){
                params.setOrder("desc");  //默认降序排列
            }

            if(params.getLimit() ==0){
                params.setLimit(10);  //默认分页条数
            }
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e,"用户组列表及搜索失败，您的租户ID为:" + tenantId + ",请检查好是否配置正确");
        }
    }


}
