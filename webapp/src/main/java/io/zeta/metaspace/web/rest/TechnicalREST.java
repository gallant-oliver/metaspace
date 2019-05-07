package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.RelationQuery;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.result.AddRelationTable;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.share.Organization;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.service.SearchService;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.utils.AtlasPerfTracer;
import org.apache.atlas.web.util.Servlets;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("technical")
@Singleton
@Service
public class TechnicalREST {

    private static final Logger PERF_LOG = AtlasPerfTracer.getPerfLogger("rest.TechnicalREST");
    @Autowired
    SearchService searchService;
    private static int CATEGORY_TYPE = 0;

    @Autowired
    private DataManageService dataManageService;
    private static final Logger LOG = LoggerFactory.getLogger(TechnicalREST.class);

    /**
     * 添加关联表时搜库
     *
     * @return List<Database>
     */
    @POST
    @Path("/search/database/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Database> getAllDatabase(Parameters parameters, @PathParam("categoryId") String categoryId) throws AtlasBaseException {
        PageResult<Database> pageResult = searchService.getTechnicalDatabasePageResultV2(parameters, categoryId);
        return pageResult;
    }

    /**
     * 添加关联表时搜表
     *
     * @return List<Table>
     */
    @POST
    @Path("/search/table/{categoryId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<AddRelationTable> getTableByQuery(Parameters parameters, @PathParam("categoryId") String categoryId) throws AtlasBaseException {
        PageResult<AddRelationTable> pageResult = searchService.getTechnicalTablePageResultV2(parameters, categoryId);
        return pageResult;
    }

    /**
     * 获取全部目录
     *
     * @param sort
     * @return
     * @throws AtlasBaseException
     */
    @GET
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public List<CategoryPrivilege> getCategories(@DefaultValue("ASC") @QueryParam("sort") final String sort) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getCategories()");
            }
            return dataManageService.getAll(CATEGORY_TYPE);
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加目录 V2
     *
     * @param categoryInfo
     * @return
     * @throws Exception
     */
    @POST
    @Path("/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public CategoryPrivilege createCategory(CategoryInfoV2 categoryInfo) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.createMetadataCategory()");
            }
            return dataManageService.createCategory(categoryInfo, CATEGORY_TYPE);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除目录 V2
     *
     * @param categoryGuid
     * @return
     * @throws Exception
     */
    @DELETE
    @Path("/category/{categoryGuid}")
    public Response deleteCategory(@PathParam("categoryGuid") String categoryGuid) throws Exception {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.deleteCategory(" + categoryGuid + ")");
            }
            dataManageService.deleteCategory(categoryGuid);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 修改目录信息 V2
     *
     * @param categoryInfo
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/update/category")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String updateCategory(CategoryInfoV2 categoryInfo) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.CategoryEntity()");
            }
            return dataManageService.updateCategory(categoryInfo, CATEGORY_TYPE);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 添加关联
     *
     * @param categoryGuid
     * @param relations
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/{categoryGuid}/assignedEntities")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response assignTableToCategory(@PathParam("categoryGuid") String categoryGuid, List<RelationEntityV2> relations) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetadataREST.assignTableToCategory(" + categoryGuid + ")");
            }
            dataManageService.assignTablesToCategory(categoryGuid, relations);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 获取关联关系
     *
     * @param categoryGuid
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/category/relations/{categoryGuid}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getCategoryRelations(@PathParam("categoryGuid") String categoryGuid, RelationQuery relationQuery) throws AtlasBaseException {
        Servlets.validateQueryParamLength("categoryGuid", categoryGuid);
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.getCategoryRelations(" + categoryGuid + ")");
            }
            return dataManageService.getRelationsByCategoryGuid(categoryGuid, relationQuery);
        } catch (MyBatisSystemException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    /**
     * 删除关联关系
     *
     * @param relationshipList
     * @return
     * @throws AtlasBaseException
     */
    @DELETE
    @Path("/category/relation")
    public Response removeRelationAssignmentFromTables(List<RelationEntityV2> relationshipList) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "GlossaryREST.removeRelationAssignmentFromEntities(" + relationshipList + ")");
            }
            dataManageService.removeRelationAssignmentFromTablesV2(relationshipList);
        } catch (CannotCreateTransactionException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据库服务异常");
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    /**
     * 获取表关联
     *
     * @param relationQuery
     * @return
     * @throws AtlasBaseException
     */
    @POST
    @Path("/table/relations/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<RelationEntityV2> getQueryTables(RelationQuery relationQuery) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getQueryTables()");
            }
            return dataManageService.getRelationsByTableName(relationQuery, CATEGORY_TYPE);
        } catch (Exception e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }


    @POST
    @Path("/owner/table/")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Response addOwners(TableOwner tableOwner) throws AtlasBaseException {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.addOwners()");
            }
            dataManageService.addTableOwner(tableOwner);
        } catch (Exception e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
        return Response.status(200).entity("success").build();
    }

    @POST
    @Path("/organization/{pId}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<Organization> getOrganization(@PathParam("pId") String pId, Parameters parameters) throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.getOrganization()");
            }
            return dataManageService.getOrganizationByPid(pId, parameters);
        } catch (Exception e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

    @PUT
    @Path("/organization")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public void updateOrganization() throws Exception {
        AtlasPerfTracer perf = null;
        try {
            if (AtlasPerfTracer.isPerfTraceEnabled(PERF_LOG)) {
                perf = AtlasPerfTracer.getPerfTracer(PERF_LOG, "MetaDataREST.updateOrganization()");
            }
            dataManageService.updateOrganization();
        } catch (AtlasBaseException e) {
            throw e;
        } finally {
            AtlasPerfTracer.log(perf);
        }
    }

}
