package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.modifiermanage.Data;
import io.zeta.metaspace.model.modifiermanage.Qualifier;
import io.zeta.metaspace.model.modifiermanage.QualifierParameters;
import io.zeta.metaspace.model.modifiermanage.QualifierType;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.web.service.QualifierService;
import io.zeta.metaspace.web.util.ReturnUtil;
import lombok.val;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;

import java.util.ArrayList;
import java.util.List;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;
import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.UPDATE;

@Path("/qualifier")
@Service
public class QualifierREST {

    @Autowired
    private QualifierService qualifierService;

    private static final Logger PERF_LOG = LoggerFactory.getLogger(QualifierREST.class);

    /**
     * 批量添加修饰词
     *
     * @param dataList
     * @throws AtlasBaseException
     */

    @PUT
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    @Path("/add")
    public Result addQualifier(Qualifier dataList, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.MODIFIER.getAlias(), dataList.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.addQualifier()");
            }
            List<String> markList = new ArrayList<>();
            List<String> nameList = new ArrayList<>();
            dataList.getDataList().forEach((data -> {
                markList.add(data.getMark());
                nameList.add(data.getName());
            }));
            List<Qualifier> oldMarkList = qualifierService.getIdByMark(markList, dataList.getDataList().get(0).getTypeId(), tenantId);
            List<Qualifier> oldNameList = qualifierService.getIdByName(nameList, dataList.getDataList().get(0).getTypeId(), tenantId);
            val namecount = nameList.stream().distinct().count();
            val markcount = markList.stream().distinct().count();
            if (namecount == nameList.size()) {
                if (!oldNameList.isEmpty()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词名称已存在");
                }
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "批量添加的名称中，有重复");
            }
            if (markcount == markList.size()) {
                if (!oldMarkList.isEmpty()) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词标识已存在");
                }
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "批量添加的标识中，有重复");
            }
            return ReturnUtil.success(qualifierService.addQualifier(dataList.getDataList(), tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "批量添加修饰词失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    /**
     * 批量删除修饰词
     *
     * @param ids
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/delete")
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteQualifier(Data ids, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.MODIFIER.getAlias(),ids.getName() );
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.deleteQualifier()");
            }
            return ReturnUtil.success(qualifierService.deleteQualifier(ids));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "批量删除修饰词失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 编辑修饰词
     *
     * @param data
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/edit")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result editQualifier(Data data, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.MODIFIER.getAlias(), data.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.editQualifier()");
            }
            List<Qualifier> nameList = qualifierService.getIdsByNameOrMark(data.getId(), data.getName(), null, data.getTypeId(), tenantId);
            if (!nameList.isEmpty()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词名字已存在");
            }
            List<Qualifier> markList = qualifierService.getIdsByNameOrMark(data.getId(), null, data.getMark(), data.getTypeId(), tenantId);
            if (!markList.isEmpty()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词标识已存在");
            }
            qualifierService.editQualifier(data, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑修饰词失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取全部修饰词
     *
     * @param tenantId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/all")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getAllQualifierList(QualifierParameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.getAllQualifierList()");
            }
            return ReturnUtil.success(qualifierService.getAllQualifierList(parameters, tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取全部修饰词失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 获取修饰词列表
     *
     * @param tenantId
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/list")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getQualifierList(QualifierParameters parameters, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.getQualifierList()");
            }
            return ReturnUtil.success(qualifierService.getQualifierList(parameters, tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取用户列表失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*
     * 获取修饰词引用列表
     *
     * @param id  修饰词ID
     * @param tenantId
     * @param limit
     * @param offset
     * @throws AtlasBaseException
     */
    @GET
    @Path("/relation")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getQualifierRelationList(@QueryParam("id") String id, @DefaultValue("0") @QueryParam("offset") int offset,
                                           @DefaultValue("-1") @QueryParam("limit") int limit,
                                           @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            return ReturnUtil.success(qualifierService.getQualifierRelationList(id, tenantId, offset, limit));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取修饰词引用列表失败");
        }
    }

    /**
     * 添加修饰词类型
     *
     * @param data
     * @return
     * @throws Exception
     */
    @PUT
    @Path("/type/add")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public Result addQualifierType(Data data, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.MODIFIER.getAlias(), data.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.addQualifierType()");
            }
            List<QualifierType> nameList = qualifierService.getIdByTypeNameOrMark(data.getName(), null, tenantId);
            if (!nameList.isEmpty()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词类型名称已存在");
            }
            List<QualifierType> markList = qualifierService.getIdByTypeNameOrMark(null, data.getMark(), tenantId);
            if (!markList.isEmpty()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词类型标识已存在");
            }
            return ReturnUtil.success(qualifierService.addQualifierType(data, tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "添加修饰词类型失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 编辑修饰词类型
     *
     * @param data
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @PUT
    @Path("/type/edit")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(UPDATE)
    public Result editQualifierType(Data data, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.MODIFIER.getAlias(), data.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.editQualifierType()");
            }
            List<QualifierType> nameList = qualifierService.getIdsByTypeNameOrMark(data.getId(), data.getName(), null, tenantId);
            if (!nameList.isEmpty()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词类型名称已存在");
            }

            List<QualifierType> markList = qualifierService.getIdsByTypeNameOrMark(data.getId(), null, data.getMark(), tenantId);
            if (!markList.isEmpty()) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修饰词类型标识已存在");
            }
            qualifierService.editQualifierType(data, tenantId);
            return ReturnUtil.success();
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "编辑修饰词类型失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /*
     * 获取修饰词类型目录
     *
     * @param tenantId
     * @throws AtlasBaseException
     */
    @GET
    @Path("/type/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getQualifierTypeList(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.getQualifierTypeList()");
            }
            return ReturnUtil.success(qualifierService.getQualifierTypeList(tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取修饰词类型目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除修饰词目录
     *
     * @param ids
     * @param tenantId
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @Path("/type/delete")
    @OperateType(OperateTypeEnum.DELETE)
    public Result deleteQualifierType(Data ids, @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        HttpRequestContext.get().auditLog(ModuleEnum.MODIFIER.getAlias(), ids.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "QualifierREST.deleteQualifierType()");
            }
            return ReturnUtil.success(qualifierService.deleteQualifierType(ids, tenantId));
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "删除修饰词目录失败");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }
}
