package io.zeta.metaspace.web.rest;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.Constant;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.DeriveTableStateEnum;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.DeriveTableStateModel;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.SourceInfoDeriveTableVO;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDeriveColumnInfoService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDeriveTableColumnRelationService;
import io.zeta.metaspace.web.service.sourceinfo.SourceInfoDeriveTableInfoService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveColumnInfo;
import io.zeta.metaspace.model.dto.sourceinfo.SourceInfoDeriveTableColumnDTO;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

/**
 * <p>
 * 衍生表信息表 前端控制器
 * </p>
 *
 * @author Echo
 * @since 2021-07-13
 */
@Path("/info/deriveTable")
@Singleton
@Service
public class SourceInfoDeriveTableInfoRest {

    private static final Logger LOG = LoggerFactory.getLogger(SourceInfoDeriveTableInfoRest.class);

    @Autowired
    private SourceInfoDeriveTableInfoService sourceInfoDeriveTableInfoService;

    @Context
    private HttpServletRequest httpServletRequest;

    /**
     * 新建-保存或保存并提交衍生表登记
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    @ApiOperation(value = "新建-保存或保存并提交衍生表登记", tags = "源信息登记-衍生表登记")
    @Path("")
    @POST
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result createSaveAndSubmitDeriveTableInfo(@ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
                                                     @ApiParam(value = "请求头-租户Id", type = "String", required = true) @HeaderParam(value = "tenantId") String tenantId,
                                                     SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {
        try {
            Result result = sourceInfoDeriveTableInfoService.checkAddOrEditDeriveTableEntity(sourceInfoDeriveTableColumnDto, tenantId);
            if (!result.getCode().equals("200")) {
                return result;
            }
            sourceInfoDeriveTableInfoService.createSaveAndSubmitDeriveTableInfo(sourceInfoDeriveTableColumnDto, tenantId);
            boolean submit = sourceInfoDeriveTableColumnDto.isSubmit();
            String operateContent = "新增衍生表登记记录，操作:" + (submit ? "保存" : "保存并提交");
            HttpRequestContext.get().auditLog(ModuleEnum.DERIVEDTABLESREGISTER.getAlias(), operateContent);
            return ReturnUtil.success(true);
        } catch (Exception e) {
            LOG.error("新建衍生表登记异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建衍生表登记异常");
        }
    }

    /**
     * 编辑-保存或保存并提交衍生表登记
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    @ApiOperation(value = "编辑-保存或保存并提交衍生表登记", tags = "源信息登记-衍生表登记")
    @Path("")
    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result updateSaveAndSubmitDeriveTableInfo(@ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
                                                     @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
                                                     SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {
        try {
            Result result = sourceInfoDeriveTableInfoService.checkAddOrEditDeriveTableEntity(sourceInfoDeriveTableColumnDto, tenantId);
            if (!result.getCode().equals("200")) {
                return result;
            }
            // 编辑衍生表ID不能为空
            String id = sourceInfoDeriveTableColumnDto.getId();
            String tableGuid = sourceInfoDeriveTableColumnDto.getTableGuid();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(tableGuid)) {
                return ReturnUtil.error("400", "编辑衍生表ID或GUID为空或记录不存在");
            }
            SourceInfoDeriveTableInfo tableByIdAndGuid = sourceInfoDeriveTableInfoService.getTableByIdAndGuid(id, tableGuid, tenantId);
            if (null == tableByIdAndGuid) {
                return ReturnUtil.error("400", "编辑的记录不存在");
            }
            // 老信息的一些默认属性
            sourceInfoDeriveTableColumnDto.setCreateTime(tableByIdAndGuid.getCreateTimeStr());
            sourceInfoDeriveTableColumnDto.setCreator(tableByIdAndGuid.getCreator());
            sourceInfoDeriveTableColumnDto.setState(tableByIdAndGuid.getState());
            sourceInfoDeriveTableColumnDto.setDdl(tableByIdAndGuid.getDdl());
            sourceInfoDeriveTableColumnDto.setDml(tableByIdAndGuid.getDml());
            sourceInfoDeriveTableInfoService.updateSaveAndSubmitDeriveTableInfo(sourceInfoDeriveTableColumnDto, tenantId);
            boolean submit = sourceInfoDeriveTableColumnDto.isSubmit();
            String operateContent = "编辑衍生表登记记录，操作:" + (submit ? "保存" : "保存并提交");
            HttpRequestContext.get().auditLog(ModuleEnum.DERIVEDTABLESREGISTER.getAlias(), operateContent);
            return ReturnUtil.success(true);
        } catch (Exception e) {
            LOG.error("编辑衍生表登记异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编辑衍生表登记异常");
        }

    }


    @ApiOperation(value = "获取数据层", tags = "源信息登记-衍生表登记")
    @Path("/technical/category")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getTechnicalCategory(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "是否读取贴源层") @QueryParam(value = "source") boolean source) {
        try {
            // 请求参数source 废弃 贴源层不需要进行筛选，支持衍生表
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getTechnicalCategory(true, tenantId));
        } catch (Exception e) {
            LOG.error("查询数据层/库异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询数据层/库异常");
        }
    }

    @ApiOperation(value = "根据数据层ID获取关联表列表", tags = "源信息登记-衍生表登记")
    @Path("/technical/category/relations/{categoryId}")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getSourceTableByCategoryId(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "业务目录Id", required = true) @PathParam(value = "categoryId") String categoryId) {

        try {
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getSourceTableByCategoryId(categoryId));
        } catch (Exception e) {
            LOG.error("查询表列表异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询表列表异常");
        }
    }

    @ApiOperation(value = "根据源表Id获取字段列表", tags = "源信息登记-衍生表登记")
    @Path("/{sourceTableId}/sourceColumns")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getColumnInfoByTableId(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "源表Id", required = true) @PathParam(value = "sourceTableId") String sourceTableId) {
        try {
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getColumnInfoByTableId(sourceTableId));
        } catch (Exception e) {
            LOG.error("查询字段列表异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询字段列表异常");
        }
    }

    @ApiOperation(value = "获取业务目录", tags = "源信息登记-衍生表登记")
    @Path("/businesses/category")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinessCategory(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId) {
        try {
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getBusinessCategory(tenantId));
        } catch (Exception e) {
            LOG.error("查询业务目录异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询业务目录异常");
        }
    }

    @ApiOperation(value = "根据业务目录ID获取关联的业务对象", tags = "源信息登记-衍生表登记")
    @Path("/businesses/category/relations/{categoryId}")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getBusinessByCategoryId(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "业务目录Id", required = true) @PathParam(value = "categoryId") String categoryId) {
        try {
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getBusinessByCategoryId(categoryId, tenantId));
        } catch (Exception e) {
            LOG.error("查询业务对象异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询业务对象异常");
        }
    }

    @ApiOperation(value = "查询衍生表列表", tags = "源信息登记-衍生表登记")
    @Path("")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDeriveTableList(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "信息状态：0：未提交，1：已提交", example = "1") @QueryParam(value = "state") Integer state,
            @ApiParam(value = "衍生表中英文名称") @QueryParam(value = "tableName") String tableName,
            @ApiParam(value = "列表开始下标", required = true, example = "0", defaultValue = "0") @QueryParam(value = "offset") int offset,
            @ApiParam(value = "列表截取长度", required = true, example = "10", defaultValue = "10") @QueryParam(value = "limit") int limit) {

        try {
            PageResult<SourceInfoDeriveTableVO> pageResult = new PageResult<>();
            List<SourceInfoDeriveTableVO> sourceInfoDeriveTableVOS = sourceInfoDeriveTableInfoService.queryDeriveTableList(tenantId, tableName, state, offset, limit);
            pageResult.setCurrentSize(sourceInfoDeriveTableVOS.size());
            pageResult.setOffset(offset);
            pageResult.setTotalSize(CollectionUtils.isEmpty(sourceInfoDeriveTableVOS) ? 0 : sourceInfoDeriveTableVOS.get(0).getTotal());
            pageResult.setLists(sourceInfoDeriveTableVOS);
            return ReturnUtil.success(pageResult);
        } catch (Exception e) {
            LOG.error("查询衍生表列表异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询衍生表列表异常");
        }
    }

    @ApiOperation(value = "根据表ID查看衍生表详情", tags = "源信息登记-衍生表登记")
    @Path("/{tableId}/info")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDeriveTableColumnDetail(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "表的主键id", required = true) @PathParam(value = "tableId") String tableId) {
        try {
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getDeriveTableColumnDetail(tenantId, tableId));
        } catch (Exception e) {
            LOG.error("查询衍生表详情异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询衍生表详情异常");
        }
    }

    @ApiOperation(value = "根据表GUID查看衍生表版本", tags = "源信息登记-衍生表登记")
    @Path("/{tableGuid}/version")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDeriveTableVersion(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "表的guid", required = true) @PathParam(value = "tableGuid") String tableGuid,
            @ApiParam(value = "列表开始下标", required = true, example = "0", defaultValue = "0") @QueryParam(value = "offset") int offset,
            @ApiParam(value = "列表截取长度", required = true, example = "10", defaultValue = "10") @QueryParam(value = "limit") int limit) {
        try {
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getDeriveTableVersion(tableGuid, offset, limit));
        } catch (Exception e) {
            LOG.error("查询衍生表版本异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询衍生表版本异常");
        }
    }

    @ApiOperation(value = "衍生表查询状态下拉框列表", tags = "源信息登记-衍生表登记")
    @Path("/state")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDeriveTableList(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId) {
        return ReturnUtil.success(Stream.of(DeriveTableStateEnum.values()).map(e ->
                new DeriveTableStateModel().setName(e.getName()).setState(e.getState())).collect(Collectors.toList()));
    }

    @ApiOperation(value = "根据数据库类型校验字段英文名", tags = "源信息登记-衍生表登记")
    @Path("/column/name")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkColumnNameEn(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "数据库类型", required = true) @QueryParam(value = "dbType") String dbType,
            @ApiParam(value = "字段英文名", required = true) @QueryParam(value = "name") String name) {
        boolean result = sourceInfoDeriveTableInfoService.checkColumnNameEn(name);
        return result ? ReturnUtil.success(true) : ReturnUtil.success("字段英文名不符合规范", false);
    }

    @ApiOperation(value = "根据数据库ID校验表英文名是否重复", tags = "源信息登记-衍生表登记")
    @Path("/table/name")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result checkTableNameDump(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "数据库Id", required = true) @QueryParam(value = "dbId") String dbId,
            @ApiParam(value = "表id", required = false) @QueryParam(value = "tableId") String tableId,
            @ApiParam(value = "表英文名", required = true) @QueryParam(value = "name") String name) {
        boolean pattern = sourceInfoDeriveTableInfoService.checkTableOrColumnNameEnPattern(name);
        tableId = StringUtils.isEmpty(tableId) ? null : tableId;
        boolean dump = sourceInfoDeriveTableInfoService.checkTableNameDump(name, dbId, tableId);
        String message = !pattern ? "衍生表英文名不符合规范" : (!dump ? "目标库下表英文名已存在" : null);
        return dump && pattern ? ReturnUtil.success(true) : ReturnUtil.success(message, false);
    }

    @ApiOperation(value = "根据数据库类型获取数据类型", tags = "源信息登记-衍生表登记")
    @Path("/column/type")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataTypeByDbType(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "数据库类型", required = true) @QueryParam(value = "dbType") String dbType) {
        return sourceInfoDeriveTableInfoService.getDataTypeByDbType(dbType);
    }

    @ApiOperation(value = "删除", tags = "源信息登记-衍生表登记")
    @Path("")
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Transactional
    public Result deleteDeriveTable(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "要删除的tableGuid集合", required = true) @RequestBody List<String> tableGuids) {
        try {
            List<String> tableGuidList = sourceInfoDeriveTableInfoService.getByGuidsAndTenantId(tenantId, tableGuids);
            if (!tableGuidList.containsAll(tableGuids)) {
                return ReturnUtil.error("400", "要删除部分记录不存在");
            }
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.deleteDeriveTable(tableGuids));
        } catch (Exception e) {
            LOG.error("删除衍生表异常", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除衍生表异常");
        }
    }


}

