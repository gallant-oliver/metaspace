package io.zeta.metaspace.web.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.zeta.metaspace.model.dataassets.ColumnInfo;
import io.zeta.metaspace.model.dataassets.DataAssets;
import io.zeta.metaspace.model.dataassets.TableInfo;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import io.zeta.metaspace.web.dao.DataAssetsRetrievalDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author wuyongliang
 * @Date 2021/11/9 17:27
 * @Description 数据资产检索
 */

@Service
public class DataAssetsRetrievalService {
    /**
     * 租户标签缓存（是否公共租户）
    */
    private static Cache<String, Boolean> tenantTagCache;

    private final static String USER_CACHE_EXPIRE = "user.info.expire";

    @Autowired
    private PublicService publicService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private MetaDataService metadataService;

    @Autowired
    private SearchService searchService;

    // 租户资产-业务目录模块id
    private int TENANT_ASSETS_BUSINESS_MODULE = 138;

    @Autowired
    private DataAssetsRetrievalDAO dataAssetsRetrievalDAO;

    //当前租户是否是公共租户
    private boolean isPublicTenant(String tenantId) throws AtlasException {
        initTenantTagCache();

        String cacheKey = AdminUtils.getSSOTicket() + tenantId;

        Boolean isPublic = tenantTagCache.getIfPresent(cacheKey);
        if (isPublic == null) {
            List<Module> modules = tenantService.getModule(tenantId);
            if (!CollectionUtils.isEmpty(modules)) {
                List<Integer> moduleIds = modules.stream().map(m -> m.getModuleId()).collect(Collectors.toList());
                if (moduleIds.contains(TENANT_ASSETS_BUSINESS_MODULE)) {
                    isPublic = true;
                }
                else {
                    isPublic = false;
                }
            }

            tenantTagCache.put(cacheKey, isPublic);
        }

        return isPublic;
    }

    //当前租户是否是公共租户
    private boolean isGlobalUser() {
        return publicService.isGlobal();
    }

    /**
     * 初始化租户标签缓存
    */
    private void initTenantTagCache() throws AtlasException {
        if (tenantTagCache == null) {
            // 缓存失效时间（分钟）
            int expireTime = ApplicationProperties.get().getInt(USER_CACHE_EXPIRE, 30);
            tenantTagCache = CacheBuilder.newBuilder().maximumSize(10000).expireAfterWrite(expireTime, TimeUnit.MINUTES).build();
        }
    }

    public PageResult<DataAssets> search(int type, int offset, int limit, String tenantId, String name) throws AtlasException {
        PageResult<DataAssets> pageResult = new PageResult<>();

        String userId = AdminUtils.getUserData().getUserId();

        // 是否公共租户
        boolean isPublic = isPublicTenant(tenantId);

        // 当前用户是否有全局权限
        boolean isGlobal = isGlobalUser();

        List<DataAssets> list;

        // 搜索类型：0全部；1业务对象；2数据表
        switch (type){
            case 1:
                list = dataAssetsRetrievalDAO.searchBusinesses(tenantId, userId, isPublic, isGlobal, offset, limit, name);
                break;
            case 2:
                list = dataAssetsRetrievalDAO.searchTables(tenantId, userId, isPublic, isGlobal, offset, limit, name);
                break;
            default:
                list = dataAssetsRetrievalDAO.searchAll(tenantId, userId, isPublic, isGlobal, offset, limit, name);
        }

        Long totalSize = 0L;
        if (list.size() != 0) {
            totalSize = Long.valueOf(list.get(0).getTotal());

            for (DataAssets dataAssets: list) {
                String businessPath = dataAssets.getBusinessPath();
                String technicalPath = dataAssets.getTechnicalPath();
                dataAssets.setBusinessPath(formatPath(businessPath));
                dataAssets.setTechnicalPath(formatPath(technicalPath));
            }
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);

        return pageResult;
    }

    /**
    * 规范路径（目录路径、技术路径等）
    */
    private String formatPath(String path) {
        if(!StringUtils.isEmpty(path)) {
            path = path.substring(1, path.length() - 1);
            path = path.replace(",", "/").replace("\"", "");
        }

        return path;
    }

    public DataAssets getDataAssetsById(String id, int type, String tenantId) {
        DataAssets result;
        // 搜索类型：1业务对象；2数据表
        switch (type) {
            case 1:
                result = dataAssetsRetrievalDAO.searchBusinessById(id, tenantId);
                break;
            case 2:
                result = dataAssetsRetrievalDAO.searchTableById(id,tenantId);
                break;
            default:
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据资产类别错误: " + type);
        }

        if (result != null) {
            String businessPath = result.getBusinessPath();
            String technicalPath = result.getTechnicalPath();
            result.setBusinessPath(formatPath(businessPath));
            result.setTechnicalPath(formatPath(technicalPath));
        }
        else {
            result = new DataAssets();
        }

        return result;
    }

    public TableShow dataPreview(String tableId, int count) {
        GuidCount gc = new GuidCount();
        gc.setGuid(tableId);
        gc.setCount(count);

        return searchService.getRDBMSTableShow(gc);
    }

    public PageResult<TableInfo> getTableInfoByBusinessId(String businessId, String tenantId, int limit, int offset) {
        PageResult<TableInfo> pageResult = new PageResult<>();

        List<TableInfo> list = dataAssetsRetrievalDAO.getTableInfos(businessId, tenantId, offset, limit);

        Long totalSize = 0L;
        if (list.size() != 0) {
            totalSize = Long.valueOf(list.get(0).getTotal());

            for (TableInfo tableInfo : list) {
                tableInfo.setCategory(formatPath(tableInfo.getCategory()));
                tableInfo.setTenantId(tenantId);
            }
        }
        pageResult.setTotalSize(totalSize);
        pageResult.setCurrentSize(list.size());
        pageResult.setLists(list);

        return pageResult;
    }

    public List<ColumnInfo> getColumnInfoByTableId(String tableId, String tenantId) {
        List<ColumnInfo> columns;
        //获取entity
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = metadataService.getEntityInfoByGuid(tableId, false);

            columns = extractColumnInfo(info);

            if (!CollectionUtils.isEmpty(columns)) {
                List<String> columnIds = columns.stream().map(c -> c.getColumnId()).collect(Collectors.toList());

                // 查询字段关联的衍生表信息及字段标签等
                List<ColumnInfo> deriveColumnInfos = dataAssetsRetrievalDAO.getDeriveColumnInfo(columnIds, tenantId, tableId);
                if (!CollectionUtils.isEmpty(deriveColumnInfos)) {
                    Map<String, ColumnInfo> m = deriveColumnInfos.stream().collect(Collectors.toMap(ColumnInfo::getColumnId, Function.identity(), (key1, key2) -> key2));
                    for (ColumnInfo column1 : columns) {
                        ColumnInfo column2 = m.get(column1.getColumnId());
                        if (column2 != null) {
                            column1.setColumnNameZh(column2.getColumnNameZh());
                            column1.setSecret(column2.getSecret());
                            column1.setPeriod(column2.getPeriod());
                            column1.setImportant(column2.getImportant());
                            column1.setTags(column2.getTags());
                            column1.setRemark(column2.getRemark());
                        }
                    }
                }

            }

        } catch (AtlasBaseException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常，未找到表字段信息");
        }

        return columns;
    }

    private List<ColumnInfo> extractColumnInfo(AtlasEntity.AtlasEntityWithExtInfo info) throws AtlasBaseException {
        Map<String, AtlasEntity> referredEntities = info.getReferredEntities();
        List<ColumnInfo> columns = new ArrayList<>();

        List<AtlasObjectId> columnsObjectIdList = (List<AtlasObjectId>) info.getEntity().getAttribute("columns");

        if (columnsObjectIdList != null) {
            columnsObjectIdList.stream().map(AtlasObjectId::getGuid).forEach(key -> {
                AtlasEntity referredEntity = referredEntities.get(key);
                if (referredEntity.getTypeName().contains("column") && referredEntity.getStatus().equals(AtlasEntity.Status.ACTIVE)) {
                    ColumnInfo column = new ColumnInfo();

                    extractAttributeInfo(referredEntity, column);

                    column.setColumnId(referredEntity.getGuid());
                    columns.add(column);
                }
            });
        }
        return columns;
    }

    private void extractAttributeInfo(AtlasEntity referredEntity, ColumnInfo column) {
        Map<String, Object> attributes = referredEntity.getAttributes();

        String nameAttribute = "name";
        if (attributes.containsKey(nameAttribute) && Objects.nonNull(attributes.get(nameAttribute))) {
            column.setColumnNameEn(attributes.get(nameAttribute).toString());
        } else {
            column.setColumnNameEn("");
        }

        String dataTypeAttribute = "data_type";
        if (attributes.containsKey(dataTypeAttribute) && Objects.nonNull(attributes.get(dataTypeAttribute))) {
            column.setType(attributes.get(dataTypeAttribute).toString());
        } else {
            column.setType("");
        }

        String isPrimaryKeyAttribute = "isPrimaryKey";
        if (attributes.containsKey(isPrimaryKeyAttribute) && Objects.nonNull(attributes.get(isPrimaryKeyAttribute))) {
            column.setPrimaryKey((boolean) attributes.get(isPrimaryKeyAttribute));
        } else {
            column.setPrimaryKey(false);
        }
    }
}
