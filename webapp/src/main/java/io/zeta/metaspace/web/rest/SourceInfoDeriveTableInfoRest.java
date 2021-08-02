package io.zeta.metaspace.web.rest;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.Constant;
import io.zeta.metaspace.model.sourceinfo.derivetable.constant.DeriveTableStateEnum;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
    public Result createSaveAndSubmitDeriveTableInfo(@ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
                                                     @ApiParam(value = "请求头-租户Id", type = "String", required = true) @HeaderParam(value = "tenantId") String tenantId,
                                                     SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {
        Result result = checkAddOrEditDeriveTableEntity(sourceInfoDeriveTableColumnDto);
        if (!result.getCode().equals("200")) {
            return result;
        }
        try {
            sourceInfoDeriveTableInfoService.createSaveAndSubmitDeriveTableInfo(sourceInfoDeriveTableColumnDto, tenantId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "新建衍生表登记异常");
        }
        return ReturnUtil.success(true);
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
    public Result updateSaveAndSubmitDeriveTableInfo(@ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
                                                     @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
                                                     SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {

        Result result = checkAddOrEditDeriveTableEntity(sourceInfoDeriveTableColumnDto);
        if (!result.getCode().equals("200")) {
            return result;
        }
        try {
            sourceInfoDeriveTableInfoService.updateSaveAndSubmitDeriveTableInfo(sourceInfoDeriveTableColumnDto, tenantId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "编辑衍生表登记异常");
        }
        return ReturnUtil.success(true);
    }

    /**
     * 校验新增-编辑衍生表中参数
     *
     * @param sourceInfoDeriveTableColumnDto
     * @return
     */
    private Result checkAddOrEditDeriveTableEntity(SourceInfoDeriveTableColumnDTO sourceInfoDeriveTableColumnDto) {
        // 检验表英文名
        if (!checkTableOrColumnNameEnPattern(sourceInfoDeriveTableColumnDto.getTableNameEn())) {
            return ReturnUtil.error("400", "衍生表英文名不符合规范");
        }
        // 字段
        List<SourceInfoDeriveColumnInfo> sourceInfoDeriveColumnInfos = sourceInfoDeriveTableColumnDto.getSourceInfoDeriveColumnInfos();
        // 校验字段英文名
        List<String> errorNames = sourceInfoDeriveColumnInfos.stream().filter(e -> !checkColumnNameEn(e.getColumnNameEn())).map(SourceInfoDeriveColumnInfo::getColumnNameEn).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorNames)) {
            return ReturnUtil.error("400", "衍生表字段英文名不符合规范:" + errorNames.toString());
        }
        // 根据数据源类型校验数据类型
        List<String> dataTypeList = Constant.DATA_TYPE_MAP.get((sourceInfoDeriveTableColumnDto.getDbType()));
        List<String> errorDbTypes = sourceInfoDeriveColumnInfos.stream().filter(e -> !dataTypeList.contains(e.getDataType())).map(SourceInfoDeriveColumnInfo::getDataType).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorNames)) {
            return ReturnUtil.error("400", "衍生表字段数据类型不符合规范:" + errorDbTypes.toString());
        }
        if (sourceInfoDeriveTableColumnDto.getDbType().equals("ORACLE")) {
            long count = sourceInfoDeriveColumnInfos.stream().filter(e -> Arrays.asList("long", "long raw").contains(e.getDataType())).count();
            if (count > 1) {
                return ReturnUtil.error("400", "一张表最多存在一个long类型数据，包含'long' 和 'long raw'数据类型");
            }
        }
        return ReturnUtil.success();
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
            return ReturnUtil.success(sourceInfoDeriveTableInfoService.getTechnicalCategory(source, tenantId));
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询表列表异常");
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询衍生表列表异常");
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
            e.printStackTrace();
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
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询衍生表版本异常");
        }
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
        return ReturnUtil.success(checkColumnNameEn(name));

    }

    @ApiOperation(value = "根据数据库类型获取数据类型", tags = "源信息登记-衍生表登记")
    @Path("/column/type")
    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDataTypeByDbType(
            @ApiParam(value = "请求头-用户token", required = true) @HeaderParam(value = "X-SSO-FullticketId") String ticket,
            @ApiParam(value = "请求头-租户Id", required = true) @HeaderParam(value = "tenantId") String tenantId,
            @ApiParam(value = "数据库类型,ORACLE或HIVE", required = true) @QueryParam(value = "dbType") String dbType) {
        List<String> list = Constant.DATA_TYPE_MAP.get(dbType);
        if (CollectionUtils.isEmpty(list)) {
            return ReturnUtil.error("400", "数据库类型异常，只能是ORACLE或HIVE");
        }
        return ReturnUtil.success(list);
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
            e.printStackTrace();
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除衍生表异常");
        }
    }

    private boolean checkTableOrColumnNameEnPattern(String name) {
        return name.matches(Constant.pattern);
    }

    private boolean checkColumnNameEn(String name) {
        return checkTableOrColumnNameEnPattern(name) && !Constant.HIVE_KEYWORD.contains(name.toUpperCase());
    }

}

