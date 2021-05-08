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

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.approvegroup.ApproveGroup;
import io.zeta.metaspace.model.approvegroup.ApproveGroupListAndSearchResult;
import io.zeta.metaspace.model.approvegroup.ApproveGroupMemberSearch;
import io.zeta.metaspace.model.approvegroup.ApproveGroupParas;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.web.service.ApproveGroupService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.inject.Singleton;
import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

/**
 * @author lixiang03
 * @Data 2020/2/24 11:14
 */
@Path("approveGroups")
@Singleton
@Service
public class ApproveGroupREST {
    @Autowired
    ApproveGroupService approveGroupService;

    /**
     * 审批组列表及搜索
     */

    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApproveGroupListAndSearch(
            @HeaderParam("tenantId") String tenantId, Parameters params) throws AtlasBaseException {
        try {

            //参数检查
            if(StringUtils.isBlank(params.getSortby())){
                params.setSortby("createTime");  //默认排序字段
            }

            if(StringUtils.isBlank(params.getOrder())){
                params.setOrder("desc");  //默认降序排列
            }

            PageResult<ApproveGroupListAndSearchResult> pageResult = approveGroupService.getApproveGroupListAndSearch(tenantId, params);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组列表及搜索失败，您的租户ID为:" + tenantId + ",请检查好是否配置正确");
        }
    }

    /**
     * 审批组列表及搜索
     */

    @POST
    @Path("/moduleList")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getApproveGroupListByModule(
            @HeaderParam("tenantId") String tenantId, ApproveGroupParas params) throws AtlasBaseException {
        try {
            //参数检查
            if(StringUtils.isBlank(params.getSortBy())){
                params.setSortBy("createTime");  //默认排序字段
            }
            if(StringUtils.isBlank(params.getOrder())){
                params.setOrder("desc");  //默认降序排列
            }
            if(params.getLimit() ==0){
                params.setLimit(10);  //默认分页条数
            }
            PageResult<ApproveGroupListAndSearchResult> pageResult = approveGroupService.getApproveGroupByModuleId(params,tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"用户组列表及搜索失败，您的租户ID为:" + tenantId + ",请检查好是否配置正确");
        }
    }





    /**
     * 新建审批组
     */
    @Path("/add")
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result addGroup(@HeaderParam("tenantId") String tenantId, ApproveGroup approveGroup) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.APPROVERMANAGE.getAlias(), "新建审批组："+approveGroup.getName());
            approveGroupService.addApproveGroup(tenantId, approveGroup);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"新建用户组失败");
        }
    }

    /**
     * 四.删除审批组
     */
    @DELETE
    @Path("/delete")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteGroupByID(@HeaderParam("tenantId") String tenantId ,Map<String,List<String>> map) throws AtlasBaseException {
        try {
            //查询审批组，生成审计日志
            List<ApproveGroup> groupsByIDs = approveGroupService.getApproveGroupByIDs(map.get("groupIds"));
            approveGroupService.deleteApproveGroupByIDs(map.get("groupIds"));
            HttpRequestContext.get().auditLog(ModuleEnum.APPROVERMANAGE.getAlias(), "删除审批组："+groupsByIDs.stream().map(group->group.getName()).collect(Collectors.joining(",")));
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"删除用户组信息失败");
        }
    }


    /**
     * 五.审批组成员列表及搜索
     */

    @GET
    @Path("/users")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupMemberListAndSearch(
            @QueryParam("id") String id,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("10") @QueryParam("limit") int limit,
            @QueryParam("search") String search,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            PageResult<MemberListAndSearchResult> pageResult = approveGroupService.getApproveGroupMemberListAndSearch(id,offset, limit, search,tenantId);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"审批组成员列表获取失败");
        }
    }


    /**
     * 六.审批组添加成员列表及搜索
     */

    @GET
    @Path("/{id}/add/members")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getUserGroupMemberSearch(
            @HeaderParam("tenantId") String tenantId,
            @PathParam("id") String groupId,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("limit") int limit,
            @QueryParam("search") String search) throws AtlasBaseException {
        try {
            PageResult<ApproveGroupMemberSearch> pageResult = approveGroupService.getApproveGroupMemberSearch(tenantId, groupId, offset, limit, search);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            throw new AtlasBaseException("获取审批组添加成员列表及搜索失败，您的租户ID为:" + tenantId + ",审批组ID为:" + groupId + ",请检查好是否配置正确"+e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"审批组成员列表获取失败");
        }

    }


    /**
     * 审批组添加成员
     */
    @POST
    @Path("/addUsers")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result addUserByGroupId(
            Map<String,Object> map) throws AtlasBaseException {
        try {
            String id = map.get("id").toString(); //审批组ID
            List<String> userIds = (List<String>)map.get("userIds");  //用户ID
            ApproveGroup approveGroupByID = approveGroupService.getApproveGroupById(id);
            HttpRequestContext.get().auditLog(ModuleEnum.APPROVERMANAGE.getAlias(), "审批组添加成员："+approveGroupByID.getName());
            approveGroupService.addUserGroupByID(id, userIds);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"审批组添加成员失败");
        }
    }

    /**
     * 审批模块
     */
    @POST
    @Path("/module")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result getModuleByGroupId(
            Map<String,Object> map) throws AtlasBaseException {
        try {
            String id = map.get("groupId").toString(); //审批组ID
            if(map.get("groupId") == null  || map.get("groupId").toString().equals("")){  //新建
                return ReturnUtil.success(approveGroupService.getApproveModule());
            }else{ //edit
                return ReturnUtil.success(approveGroupService.getApproveModule(id));
            }
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"获取审批模块失败");
        }
    }



    /**
     * 审批组移除成员
     */

    @DELETE
    @Path("/deleteUsers")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteUserByGroupId(Map<String,Object> map) throws AtlasBaseException {
        try {
            String id = map.get("id").toString(); //审批组ID
            List<String> userIds = (List<String>)map.get("userIds");  //用户ID
            ApproveGroup approveGroupByID = approveGroupService.getApproveGroupById(id);
            HttpRequestContext.get().auditLog(ModuleEnum.APPROVERMANAGE.getAlias(), "审批组移除成员："+approveGroupByID.getName());
            approveGroupService.deleteUserByGroupId(id, userIds);
            return ReturnUtil.success();
        }catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(),AtlasErrorCode.BAD_REQUEST, e,"审批组移除成员失败");
        }
    }

    /**
     * 审批组编辑
     */

    @PUT
    @Path("edit")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateUserGroupInformation(ApproveGroup approveGroup,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        try {
            HttpRequestContext.get().auditLog(ModuleEnum.APPROVERMANAGE.getAlias(), "修改审批组信息"+approveGroup.getName());
            approveGroupService.updateUserGroupInformation(approveGroup.getId(), approveGroup,tenantId);
            return ReturnUtil.success();
        }catch (AtlasBaseException e){
            throw e;
        }catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e,"修改审批组信息失败");
        }
    }





}
