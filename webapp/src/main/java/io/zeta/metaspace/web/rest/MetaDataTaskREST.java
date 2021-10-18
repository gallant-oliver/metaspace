package io.zeta.metaspace.web.rest;

import io.zeta.metaspace.HttpRequestContext;
import io.zeta.metaspace.model.Result;
import io.zeta.metaspace.model.metadata.ColumnParameters;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.operatelog.ModuleEnum;
import io.zeta.metaspace.model.operatelog.OperateType;
import io.zeta.metaspace.model.operatelog.OperateTypeEnum;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.model.sync.SyncTaskInstance;
import io.zeta.metaspace.web.service.DataShareService;
import io.zeta.metaspace.web.service.MetaDataTaskService;
import io.zeta.metaspace.web.service.SearchService;
import io.zeta.metaspace.web.service.TenantService;
import io.zeta.metaspace.web.util.ReturnUtil;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.web.util.Servlets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Path("/metadata/task")
@Component
public class MetaDataTaskREST {

    @Autowired
    private MetaDataTaskService metaDataTaskService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private DataShareService shareService;
    @Autowired
    private TenantService tenantService;


    /**
     * 创建任务
     */
    @POST
    @Path("")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.INSERT)
    public boolean createSyncTaskDefinition(SyncTaskDefinition syncTaskDefinition, @HeaderParam("tenantId") String tenantId) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "创建采集任务定义: " + syncTaskDefinition.getName());
        metaDataTaskService.createSyncTaskDefinition(syncTaskDefinition, tenantId);
        return true;
    }

    @PUT
    @Path("/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.UPDATE)
    public boolean updateSyncTaskDefinition(@PathParam("id") String id, SyncTaskDefinition syncTaskDefinition, @HeaderParam("tenantId") String tenantId) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "更新采集任务定义: " + syncTaskDefinition.getName());
        syncTaskDefinition.setId(id);
        metaDataTaskService.updateSyncTaskDefinition(syncTaskDefinition, tenantId);
        return true;
    }

    @PUT
    @Path("{id}/enable/{enable}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.UPDATE)
    public boolean putSyncTaskDefinitionStatus(@PathParam("id") String id, @PathParam("enable") Boolean enable, @HeaderParam("tenantId") String tenantId) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "更新采集任务定义定时状态: " + id + (enable ? "启动" : "禁用"));
        metaDataTaskService.updateSyncTaskDefinitionEnable(id, enable, tenantId);
        return true;
    }

    @DELETE
    @Path("{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public boolean deletedSyncTaskDefinition(@PathParam("id") String ids, @HeaderParam("tenantId") String tenantId) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "删除采集任务定义: " + ids);
        metaDataTaskService.deleteSyncTaskDefinition(Arrays.asList(ids.split(",")), tenantId);
        return true;
    }


    @DELETE
    @Path("/instance/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.DELETE)
    public boolean deletedSyncTaskInstance(@PathParam("id") String ids, @HeaderParam("tenantId") String tenantId) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "删除采集任务实例: " + ids);
        metaDataTaskService.deleteSyncTaskInstance(Arrays.asList(ids.split(",")), tenantId);
        return true;
    }

    @GET
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<SyncTaskDefinition> getSyncTaskDefinitionList(@QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("query") String query, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setQuery(query);
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        return metaDataTaskService.getSyncTaskDefinitionList(parameters, tenantId);
    }

    @GET
    @Path("/{id}/instance")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public PageResult<SyncTaskInstance> getSyncTaskInstanceList(@PathParam("id") String id, @QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("query") String query, @QueryParam("status") SyncTaskInstance.Status status, @HeaderParam("tenantId") String tenantId) {
        Parameters parameters = new Parameters();
        parameters.setQuery(query);
        parameters.setLimit(limit);
        parameters.setOffset(offset);

        return metaDataTaskService.getSyncTaskInstanceList(id, parameters, status, tenantId);
    }


    @GET
    @Path("/instance/{id}/log")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public String getSyncTaskInstanceLog(@PathParam("id") String id) {
        return metaDataTaskService.getSyncTaskInstanceLog(id);
    }


    @PUT
    @Path("/start/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.UPDATE)
    public boolean startSyncTaskDefinition(@PathParam("id") String id) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "手动启动采集任务定义: " + id);
        metaDataTaskService.startManualJob(id);
        return true;
    }

    @PUT
    @Path("/stop/{id}")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    @OperateType(OperateTypeEnum.UPDATE)
    public boolean stopSyncTaskInstance(@PathParam("id") String id) {
        HttpRequestContext.get().auditLog(ModuleEnum.METADATACOLLECTION.getAlias(), "停止采集任务实例: " + id);
        metaDataTaskService.stopSyncTaskInstance(id);
        return true;
    }


    @GET
    @Path("/{sourceId}/schema")
    @Consumes(Servlets.JSON_MEDIA_TYPE)
    @Produces(Servlets.JSON_MEDIA_TYPE)
    public Result getDatabaseByQuery(@PathParam("sourceId") String sourceId, @QueryParam("limit") int limit, @QueryParam("offset") int offset, @QueryParam("query") String query,
                                     @HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            if ("hive".equalsIgnoreCase(sourceId)) {
                List<String> schemaList = tenantService.getDatabase(tenantId);
                PageResult<LinkedHashMap<String, Object>> result = new PageResult<>();
                result.setTotalSize(schemaList.size());
                result.setLists(schemaList.stream().sorted().skip(offset).limit(limit < 0 ? result.getTotalSize() : limit).map(schema -> {
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("schemaName", schema);
                    return map;
                }).collect(Collectors.toList()));
                result.setCurrentSize(result.getLists().size());
                return ReturnUtil.success(result);
            }

            ColumnParameters parameters = new ColumnParameters();
            parameters.setLimit(limit);
            parameters.setOffset(offset);
            parameters.setQuery(query);
            PageResult<LinkedHashMap<String, Object>> result = shareService.getDataList(null, DataShareService.SEARCH_TYPE.SCHEMA, parameters, tenantId, sourceId);
            return ReturnUtil.success(result);
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取库列表失败");
        }
    }
}
