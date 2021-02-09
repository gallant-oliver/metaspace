package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Permission;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryDeleteReturn;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Singleton;
import javax.ws.rs.*;

import static io.zeta.metaspace.model.operatelog.OperateTypeEnum.INSERT;

@Singleton
@Service
@Path("/indices")
public class IndexREST {

    private static final Logger PERF_LOG = LoggerFactory.getLogger(IndexREST.class);

    @Autowired
    private DataManageService dataManageService;

    //目录类型    指标域
    private static final int CATEGORY_TYPE = 5;
    /**
     * 添加指标域
     *
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    //@Permission({ModuleEnum.INDEX,ModuleEnum.AUTHORIZATION})
    @POST
    @Path("/categories")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(INSERT)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo, @HeaderParam("tenantId")String tenantId) throws Exception {
        //HttpRequestContext.get().auditLog(ModuleEnum.INDEX.getAlias(), categoryInfo.getName());
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.createCategory()");
            }
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE,tenantId);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     *  编辑指标域
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    //@Permission({ModuleEnum.INDEX,ModuleEnum.AUTHORIZATION})
    @PUT
    @Path("/categories/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String updateCategory(@PathParam("categoryId") String categoryGuid,CategoryInfoV2 categoryInfo,@HeaderParam("tenantId")String tenantId) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.updateCategory()");
            }
            categoryInfo.setGuid(categoryGuid);
            return dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE,tenantId);
        }  catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除指标域
     *
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    //@Permission({ModuleEnum.INDEX, ModuleEnum.AUTHORIZATION})
    @DELETE
    @Path("/categories/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result deleteCategory(@PathParam("categoryGuid") String categoryGuid,@QueryParam("deleteIndex") boolean deleteIndex, @HeaderParam("tenantId") String tenantId) throws Exception {
        AtlasPerfTracer perf = null;
        CategoryDeleteReturn deleteReturn = null;
        try {
            Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "IndexREST.deleteCategory(" + categoryGuid + ")");
            }
            deleteReturn = deleteIndexField(categoryGuid, tenantId, CATEGORY_TYPE,deleteIndex);
            return ReturnUtil.success(deleteReturn);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除指标域
     */
    @Transactional(rollbackFor = Exception.class)
    public CategoryDeleteReturn deleteIndexField(String guid,String tenantId,int type,boolean deleteIndex) throws Exception {
        if(deleteIndex){
            //删除目录下所有指标
        }else {
            //将目录下所有指标都转移到默认域
        }
        //删除目录
        CategoryDeleteReturn deleteReturn =dataManageService.deleteCategory(guid,tenantId,type);
        return deleteReturn;
    }

}
