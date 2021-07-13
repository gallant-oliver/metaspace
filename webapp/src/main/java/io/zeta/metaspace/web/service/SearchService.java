package io.zeta.metaspace.web.service;

import com.healthmarketscience.sqlbuilder.CustomSql;
import com.healthmarketscience.sqlbuilder.SelectQuery;
import io.zeta.metaspace.MetaspaceConfig;
import io.zeta.metaspace.adapter.AdapterExecutor;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.table.DataSourceHeader;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.utils.AdapterUtils;
import io.zeta.metaspace.utils.ThreadPoolUtil;
import io.zeta.metaspace.web.dao.*;
import io.zeta.metaspace.web.util.AdminUtils;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.AtlasException;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.EntityDiscoveryService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.rest.EntityREST;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.HeaderParam;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@AtlasService
public class SearchService {

    public static final Log LOG = LogFactory.getLog(SearchService.class);
    @Autowired
    private EntityREST entityREST;
    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    EntityDiscoveryService entityDiscoveryService;
    @Autowired
    MetaspaceGremlinQueryService metaspaceEntityService;
    @Autowired
    RoleDAO roleDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    CategoryDAO categoryDAO;
    @Autowired
    RelationDAO relationDAO;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    MetadataSubscribeDAO subscribeDAO;
    @Autowired
    private UserGroupDAO userGroupDAO;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private UserGroupService userGroupService;
    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private TableDAO tableDAO;

    public PageResult<Database> getDatabases(String sourceId, long offset, long limit, String query, boolean active, String tenantId, boolean queryCount) {
        List<String> dbs = tenantService.getDatabase(tenantId);
        String guids = "";
        if (StringUtils.isEmpty(sourceId)) {
            dbs = dbs.stream().filter(db->db.contains(query)).collect(Collectors.toList());
            List<TableInfo> rdbmsTableInfoList = tableDAO.selectDatabaseByTenantId(tenantId);
            List<String> collect = rdbmsTableInfoList.stream().filter(rdbmsTableInfo -> rdbmsTableInfo.getDbName().contains(query)).map(TableInfo::getDatabaseGuid).collect(Collectors.toList());
            guids = dbsToString(collect);
        }
        String dbsToString = dbsToString(dbs);
        return metaspaceEntityService.getSchemaList(sourceId, guids, offset, limit, dbsToString, queryCount);

    }

    public PageResult<TableEntity> getTable(String schemaId, boolean active, long offset, long limit, String query, Boolean isView, boolean queryInfo, String tenantId) {
        ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.getThreadPoolExecutor();
        try {
            // 在主线程获取用户信息，下面任务子线程获取不到，共享的话session混乱
            Configuration conf = ApplicationProperties.get();
            boolean secure = conf.getBoolean("metaspace.secureplus.enable", true);
            String user = !secure ? MetaspaceConfig.getHiveAdmin() : AdminUtils.getUserName();
            String dbsToString = "";
            String guid = "";
            if (StringUtils.isNotBlank(query)) {
                List<String> dbs = tenantService.getDatabase(tenantId);
                dbsToString = dbsToString(dbs);
                List<String> guidList = tableDAO.selectDatabaseGuidByTenantId(tenantId);
                guid = dbsToString(guidList);
            }
            List<AtlasVertex> vertices = new ArrayList<>();
            PageResult<TableEntity> result = metaspaceEntityService.getTableList(vertices, dbsToString, guid, schemaId, active, offset, limit, query, isView);
            List<CompletableFuture> completableFutures = new ArrayList<>();
            if (queryInfo && result.getCurrentSize() > 0) {
                for (TableEntity tableEntity : result.getLists()) {
                    completableFutures.add(CompletableFuture.runAsync(() -> {
                        AtlasVertex atlasVertex = vertices.stream().filter(vertex -> Objects.equals(tableEntity.getId(), vertex.getProperty("__guid", String.class))).findAny().get();
                        Map<String, Object> tableInfo = metaDataService.getTableType(metaDataService.vertexToEntityInfo(atlasVertex, null, null).getEntity());
                        String sourceId = String.valueOf(tableInfo.get("sourceId"));
                        String databasesId = String.valueOf(tableInfo.get("schemaId"));
                        String schema = String.valueOf(tableInfo.get("schemaName"));
                        tableEntity.setSourceId(sourceId);
                        tableEntity.setDatabaseId(databasesId);
                        boolean view = tableEntity.getTableType().toLowerCase().contains("view");
                        if (tableEntity.isHiveTable()) {
                            if (view) {
                                try {
                                    tableEntity.setSql(this.getBuildTableSql(tableEntity.getId(), user).getSql());
                                } catch (AtlasException e) {
                                    throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取列表失败");
                                }
                            } else {
                                // 查询返回单位是字节
                                float size = AdapterUtils.getHiveAdapterSource().getNewAdapterExecutor().getTableSize(schema, tableEntity.getName(), "metaspace");
                                tableEntity.setTableSize(String.format("%.3f", size / 1024 / 1024));
                            }
                        } else {
                            if (view) {
                                tableEntity.setSql(this.getBuildRDBMSTableSql(tableEntity.getId()).getSql());
                            } else {
                                AdapterExecutor adapterExecutor = AdapterUtils.getAdapterExecutor(dataSourceService.getUnencryptedDataSourceInfo(sourceId));
                                float size = adapterExecutor.getTableSize(schema, tableEntity.getName(), null);
                                tableEntity.setTableSize(String.format("%.3f", size / 1024 / 1024));
                            }
                        }
                    }, threadPoolExecutor));
                }
                try {
                    CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[]{})).get(30, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    LOG.error("获取表列表出错", e);
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取表列表出错");
                }
            }
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(e.getMessage(), AtlasErrorCode.BAD_REQUEST, e, "获取表列表失败");
        }
    }


    @Cacheable(value = "databaseSearchCache", key = "#parameters.query + #active + #parameters.limit + #parameters.offset+#tenantId+#account")
    public PageResult<Database> getDatabasePageResult(Boolean active, Parameters parameters, String tenantId, String account) throws AtlasBaseException {
        long limit = parameters.getLimit();
        long offset = parameters.getOffset();
        String queryDb = parameters.getQuery();
        //判断独立部署和多租户
        if (TenantService.defaultTenant.equals(tenantId)) {
            return metaspaceEntityService.getDatabaseByQuery(queryDb, active, offset, limit);
        } else {
            List<String> dbs = tenantService.getDatabase(tenantId);
            String dbsToString = dbsToString(dbs);
            return metaspaceEntityService.getDatabaseByQuery(queryDb, active, offset, limit, dbsToString);
        }

    }

    @Cacheable(value = "RDBMSDataSourceSearchCache", key = "#parameters.query + #parameters.limit + #parameters.offset + #sourceType + #active")
    public PageResult<RDBMSDataSource> getDataSourcePageResult(Parameters parameters, String sourceType, Boolean active) throws AtlasBaseException {
        long limit = parameters.getLimit();
        long offset = parameters.getOffset();
        String querySource = parameters.getQuery();
        return metaspaceEntityService.getRDBMSDataSourceByQuery(querySource, offset, limit, sourceType, active);
    }

    @Cacheable(value = "RDBMSDBBySourceCache", key = "#sourceId + #offset + #limit + #active")
    public PageResult<RDBMSDatabase> getRDBMSDBBySource(String sourceId, long offset, long limit, Boolean active) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSDBBySource(sourceId, offset, limit, active);
    }

    @Cacheable(value = "RDBMSTableByDBCache", key = "#databaseId + #offset + #limit + #active")
    public PageResult<RDBMSTable> getRDBMSTableByDB(String databaseId, long offset, long limit, Boolean active) throws AtlasBaseException {
        PageResult<RDBMSTable> pageResult = metaspaceEntityService.getRDBMSTableByDB(databaseId, offset, limit, active);
        List<RDBMSTable> tableList = pageResult.getLists();
        String userId = AdminUtils.getUserData().getUserId();
        tableList.forEach(table -> {
            boolean subTo = subscribeDAO.existSubscribe(table.getTableId(), userId) == 0 ? false : true;
            table.setSubscribeTo(subTo);
        });
        return pageResult;
    }

    @Cacheable(value = "TableByDBCache", key = "#databaseId + #active + #offset + #limit")
    public PageResult<Table> getTableByDB(String databaseId, Boolean active, long offset, long limit) throws AtlasBaseException {
        PageResult<Table> pageResult = metaspaceEntityService.getTableByDB(databaseId, active, offset, limit);
        List<Table> tableList = pageResult.getLists();
        String userId = AdminUtils.getUserData().getUserId();
        tableList.forEach(table -> {
            boolean subTo = subscribeDAO.existSubscribe(table.getTableId(), userId) == 0 ? false : true;
            table.setSubscribeTo(subTo);
        });
        return pageResult;
    }


    //独立部署
    public List<String> getPermissionCategoryIds() throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<Role> roles = roleDAO.getRoleByUsersId(user.getUserId());
            if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            }
            ArrayList<String> strings = new ArrayList<>();
            List<String> categoryIds = null;
            if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                categoryIds = roleDAO.getTopCategoryGuid(0, TenantService.defaultTenant);
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0, TenantService.defaultTenant);
                for (RoleModulesCategories.Category child : childs) {
                    if (!strings.contains(child.getGuid())) {
                        strings.add(child.getGuid());
                    }
                }
                return strings;
            }
            for (Role role : roles) {
                String roleId = role.getRoleId();
                if (role.getStatus() == 0) {
                    continue;
                }
                categoryIds = roleDAO.getCategorysByTypeIds(roleId, 0, TenantService.defaultTenant);
                if (categoryIds.size() == 0) {
                    continue;
                }
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0, TenantService.defaultTenant);
                for (RoleModulesCategories.Category child : childs) {
                    if (!strings.contains(child.getGuid())) {
                        strings.add(child.getGuid());
                    }
                }
            }
            return strings;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    //多租户
    public List<String> getPermissionCategoryIds(String tenantId) throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            ArrayList<String> strings = new ArrayList<>();
            List<String> categoryIds = null;
            for (UserGroup userGroup : userGroups) {
                String userGroupId = userGroup.getId();
                categoryIds = userGroupDAO.getCategorysByTypeIds(userGroupId, 0, tenantId);
                if (categoryIds.size() == 0) {
                    continue;
                }
                List<RoleModulesCategories.Category> childs = userGroupDAO.getChildAndOwnerCategorys(categoryIds, 0, tenantId);
                for (RoleModulesCategories.Category child : childs) {
                    if (!strings.contains(child.getGuid())) {
                        strings.add(child.getGuid());
                    }
                }
            }
            return strings;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public PageResult<TableInfo> getTableByDBWithQueryWithoutTmp(String databaseId, Parameters parameters, String tenantId) throws AtlasBaseException {
        try {
            List<String> categoryIds = TenantService.defaultTenant.equals(tenantId) ? getPermissionCategoryIds() : getPermissionCategoryIds(tenantId);
            PageResult<TableInfo> pageResult = new PageResult<>();
            if (Objects.isNull(categoryIds) || categoryIds.size() == 0) {
                return pageResult;
            }
            List<TableInfo> tableList = roleDAO.getTableInfosByDBId(categoryIds, databaseId);

            tableList.forEach(table -> {
                String displayName = table.getDisplayName();
                String tableName = table.getTableName();
                if (Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    table.setDisplayName(tableName);
                }
            });

            pageResult.setLists(tableList);
            pageResult.setTotalSize(tableList.size());
            pageResult.setCurrentSize(tableList.size());
            return pageResult;
        } catch (Exception e) {
            LOG.error("获取失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    @Cacheable(value = "tablePageCache", key = "#active + #parameters.query + #parameters.limit + #parameters.offset+#tenantId+#account")
    public PageResult<Table> getTablePageResultV2(Boolean active, Parameters parameters, String tenantId, String account) throws AtlasBaseException {
        //判断独立部署和多租户
        if (TenantService.defaultTenant.equals(tenantId)) {
            return metaspaceEntityService.getTableNameAndDbNameByQuery(parameters.getQuery(), active, parameters.getOffset(), parameters.getLimit());
        } else {
            List<String> dbs = tenantService.getDatabase(tenantId);
            String dbsToString = dbsToString(dbs);
            return metaspaceEntityService.getTableNameAndDbNameByQuery(parameters.getQuery(), active, parameters.getOffset(), parameters.getLimit(), dbsToString);
        }
    }

    @Cacheable(value = "RDBMSDBPageCache", key = "#parameters.query + #sourceType + #parameters.limit + #parameters.offset + #active")
    public PageResult<RDBMSDatabase> getRDBMSDBPageResultV2(Parameters parameters, String sourceType, Boolean active) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSDBNameAndSourceNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(), sourceType, active);
    }

    @Cacheable(value = "RDBMSTablePageCache", key = "#parameters.query + #sourceType + #parameters.limit + #parameters.offset + #active")
    public PageResult<RDBMSTable> getRDBMSTablePageResultV2(Parameters parameters, String sourceType, Boolean active) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSTableNameAndDBAndSourceNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(), sourceType, active);
    }

    @Cacheable(value = "RDBMSColumnPageCache", key = "#parameters.query + #sourceType + #parameters.limit + #parameters.offset")
    public PageResult<RDBMSColumn> getRDBMSColumnPageResultV2(Parameters parameters, String sourceType) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSColumnNameTableNameAndDBAndSourceNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(), sourceType);
    }


    @Cacheable(value = "columnPageCache", key = "#parameters.query + #active + #parameters.limit + #parameters.offset+#tenantId+#account")
    public PageResult<Column> getColumnPageResultV2(Boolean active, Parameters parameters, String tenantId, String account) throws AtlasBaseException {
        //判断独立部署和多租户
        if (TenantService.defaultTenant.equals(tenantId)) {
            return metaspaceEntityService.getColumnNameAndTableNameAndDbNameByQuery(parameters.getQuery(), active, parameters.getOffset(), parameters.getLimit());
        } else {
            List<String> dbs = tenantService.getDatabase(tenantId);
            String dbsToString = dbsToString(dbs);
            return metaspaceEntityService.getColumnNameAndTableNameAndDbNameByQuery(parameters.getQuery(), active, parameters.getOffset(), parameters.getLimit(), dbsToString);
        }
    }

    public TableShow getTableShow(GuidCount guidCount, boolean admin) throws AtlasBaseException, SQLException, IOException {
        TableShow tableShow;
        try {
            AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guidCount.getGuid());
            AtlasEntity entity = info.getEntity();
            String dbType = entity.getTypeName();
            if (dbType.equals("rdbms_table")) {
                tableShow = getRDBMSTableShow(guidCount);
            } else if (dbType.equals("hive_table")) {
                tableShow = getHiveTableShow(guidCount, admin, entity);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持的数据库类型");
            }
        } catch (AtlasBaseException e) {
            LOG.info("数据预览错误", e);
            throw e;
        } catch (Exception e) {
            LOG.info("数据预览错误", e);
            throw e;
        }
        return tableShow;

    }

    public TableShow getHiveTableShow(GuidCount guidCount, boolean admin, AtlasEntity entity) throws AtlasBaseException, SQLException {
        TableShow tableShow = new TableShow();
        String tableName = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
        String dbType = entity.getTypeName();
        Map<String, Object> attributes = entity.getAttributes();
        String qualifiedName = attributes != null ? (String) attributes.get("qualifiedName") : null;
        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(dbType) || StringUtils.isEmpty(qualifiedName)) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "找不到表");
        }
        String[] splits = qualifiedName.split("\\.");
        //hive
        AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(guidCount.getGuid(), true);
        AtlasEntity tableEntity = tableInfo.getEntity();
        Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
        AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
        String dbDisplayText = db.getDisplayText();
        String user = admin ? MetaspaceConfig.getHiveAdmin() : AdminUtils.getUserName();
        Connection conn = AdapterUtils.getHiveAdapterSource().getConnection(user, dbDisplayText, MetaspaceConfig.getHiveJobQueueName());
        String sql = "select * from " + splits[0] + ".`" + tableName + "` limit " + guidCount.getCount();

        if (conn != null) {
            try (ResultSet resultSet = conn.createStatement().executeQuery(sql)) {
                List<String> columns = new ArrayList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                List<Map<String, String>> resultList = new ArrayList<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    columns.add(columnName);
                }
                while (resultSet.next()) {
                    Map<String, String> map = new HashMap<>();
                    for (String column : columns) {
                        String s = resultSet.getObject(column) == null ? "NULL" : resultSet.getObject(column).toString();
                        map.put(column, s);
                    }
                    resultList.add(map);
                }
                tableShow.setTableId(guidCount.getGuid());
                tableShow.setColumnNames(columns);
                tableShow.setLines(resultList);
            }
        }
        return tableShow;
    }


    public BuildTableSql getBuildTableSql(String tableId, String user) throws AtlasBaseException, AtlasException {
        BuildTableSql buildTableSql = new BuildTableSql();
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        List<String> relationshipAttributes = new ArrayList<>();
        relationshipAttributes.add("db");
        if (Objects.isNull(tableId) || tableId.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        AtlasEntity entity = entitiesStore.getByIdWithAttributes(tableId, attributes, relationshipAttributes).getEntity();

        String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
        if (name.equals("")) {
            System.out.println("该id不存在");
        }
        AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(tableId, true);
        AtlasEntity tableEntity = tableInfo.getEntity();
        Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
        AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
        String dbDisplayText = db.getDisplayText();
        String sql = "show create table " + name;
        try (Connection conn = AdapterUtils.getHiveAdapterSource().getConnection(user, dbDisplayText, MetaspaceConfig.getHiveJobQueueName());
             ResultSet resultSet = conn.createStatement().executeQuery(sql)) {
            StringBuffer stringBuffer = new StringBuffer();
            while (resultSet.next()) {
                Object object = resultSet.getObject(1);
                stringBuffer.append(object.toString());
            }
            buildTableSql.setSql(stringBuffer.toString());
            buildTableSql.setTableId(tableId);
            return buildTableSql;
        } catch (Exception e) {
            LOG.error("获取hive连接失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "Hive服务异常");
        }
    }

    public TableShow getRDBMSTableShow(GuidCount guidCount) throws AtlasBaseException, SQLException, IOException {
        TableShow tableShow = new TableShow();
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("name_path");
        attributes.add("qualifiedName");
        List<String> relationshipAttributes = new ArrayList<>();
        relationshipAttributes.add("db");
        if (Objects.isNull(guidCount.getGuid()) || guidCount.getGuid().isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        AtlasEntity entity = entitiesStore.getByIdWithAttributes(guidCount.getGuid(), attributes, relationshipAttributes).getEntity();

        String namePath = entity.getAttribute("name_path") == null ? "" : entity.getAttribute("name_path").toString();
        String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
        String qualifiedName = entity.getAttribute("qualifiedName") == null ? "" : entity.getAttribute("qualifiedName").toString();
        String sourceId = qualifiedName.split("\\.")[0];
        DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(sourceId);
        StringBuffer dbName = new StringBuffer();
        StringBuffer tableName = new StringBuffer();
        String[] strs = namePath.split("\\.");
        for (int i = 0; i < strs.length; i++) {
            if (i < strs.length - name.split("\\.").length) {
                dbName.append(strs[i]);
                dbName.append(".");
            } else {
                tableName.append(strs[i]);
                tableName.append(".");
            }
        }

        String db = dbName.substring(0, dbName.length() - 1);
        String start = "\"";
        if (db.startsWith(start)) {
            db = db.substring(1, db.length() - 1);
        }
        String table = "";
        if (tableName.substring(0, tableName.length() - 1).equalsIgnoreCase(name)) {
            table = tableName.substring(0, tableName.length() - 1);
        } else {
            table = tableName.substring(1, tableName.length() - 2);
        }

        AdapterExecutor adapterExecutor = AdapterUtils.getAdapterExecutor(dataSourceInfo);
        AdapterTransformer adapterTransformer = adapterExecutor.getAdapterSource().getAdapter().getAdapterTransformer();
        SelectQuery selectQuery = adapterTransformer.addLimit(
                new SelectQuery()
                        .addAllColumns()
                        .addCustomFromTable(new CustomSql(adapterTransformer.caseSensitive(db) + "." + adapterTransformer.caseSensitive(table)))
                , guidCount.getCount(), 0);

        try (Connection conn = adapterExecutor.getAdapterSource().getConnection()) {
            LOG.info("执行sql" + selectQuery.toString());
            ResultSet resultSet = conn.createStatement().executeQuery(selectQuery.toString());
            List<String> columns = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Map<String, String>> resultList = new ArrayList<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnName = metaData.getColumnName(i);
                columns.add(columnName);
            }
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                for (String column : columns) {
                    String s = null;
                    Object object = resultSet.getObject(column) == null ? "NULL" : resultSet.getObject(column);
                    if (object instanceof Clob) {
                        try {
                            Clob clob = (Clob) object;
                            StringBuffer buffer = new StringBuffer();
                            clob.getCharacterStream();
                            BufferedReader br = new BufferedReader(clob.getCharacterStream());
                            clob.getCharacterStream();
                            String line = br.readLine();
                            while (line != null) {
                                buffer.append(line);
                                line = br.readLine();
                            }
                            s = buffer.toString();
                        } catch (Exception e) {
                            LOG.error(e.getMessage());
                            s = object.toString();
                        }

                    } else {
                        s = adapterTransformer.convertColumnValue(object).toString();
                    }
                    map.put(column, s);
                }
                resultList.add(map);
            }
            tableShow.setTableId(guidCount.getGuid());
            tableShow.setColumnNames(columns);
            tableShow.setLines(resultList);
            return tableShow;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有找到数据" + e.getMessage());
        }


    }

    public BuildTableSql getBuildRDBMSTableSql(String tableId) throws AtlasBaseException {
        BuildTableSql buildTableSql = new BuildTableSql();
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        attributes.add("name_path");
        attributes.add("qualifiedName");
        List<String> relationshipAttributes = new ArrayList<>();
        relationshipAttributes.add("db");
        if (Objects.isNull(tableId) || tableId.isEmpty()) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询条件异常");
        }
        AtlasEntity entity = entitiesStore.getByIdWithAttributes(tableId, attributes, relationshipAttributes).getEntity();

        String namePath = entity.getAttribute("name_path") == null ? "" : entity.getAttribute("name_path").toString();
        String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
        String qualifiedName = entity.getAttribute("qualifiedName") == null ? "" : entity.getAttribute("qualifiedName").toString();
        String sourceId = qualifiedName.split("\\.")[0];
        StringBuffer dbName = new StringBuffer();
        StringBuffer tableName = new StringBuffer();
        String[] strs = namePath.split("\\.");
        for (int i = 0; i < strs.length; i++) {
            if (i < strs.length - name.split("\\.").length) {
                dbName.append(strs[i]);
                dbName.append(".");
            } else {
                tableName.append(strs[i]);
                tableName.append(".");
            }
        }
        String db = dbName.substring(0, dbName.length() - 1);
        String start = "\"";
        if (db.startsWith(start)) {
            db = db.substring(1, db.length() - 1);
        }
        String table = "";
        if (tableName.substring(0, tableName.length() - 1).equalsIgnoreCase(name)) {
            table = tableName.substring(0, tableName.length() - 1);
        } else {
            table = tableName.substring(1, tableName.length() - 2);
        }

        if (name.equals("")) {
            System.out.println("该id不存在");
        }
        DataSourceInfo dataSourceInfo = dataSourceService.getUnencryptedDataSourceInfo(sourceId);

        AdapterExecutor adapterExecutor = AdapterUtils.getAdapterExecutor(dataSourceInfo);
        AdapterTransformer adapterTransformer = adapterExecutor.getAdapterSource().getAdapter().getAdapterTransformer();
        //buildTableSql.setSql(adapterExecutor.getCreateTableSql(adapterTransformer.caseSensitive(db),adapterTransformer.caseSensitive(table)));
        buildTableSql.setSql("");
        buildTableSql.setTableId(tableId);
        return buildTableSql;
    }

    public Connection getConnectionByDataSourceInfo(DataSourceInfo dataSourceInfo, String dbName) throws AtlasBaseException {
        dataSourceInfo.setDatabase(dbName);
        return AdapterUtils.getAdapterSource(dataSourceInfo).getConnection();
    }

    //1.4获取关联表，获取有权限的目录下的库
    @Transactional(rollbackFor = Exception.class)
    public PageResult<DataSourceHeader> getTechnicalDataSourcePageResultV2(Parameters parameters, String categoryId, String tenantId) throws AtlasBaseException {
        List<String> strings = new ArrayList<>();
        Map<String, CategoryPrivilegeV2> userPrivilegeCategory = userGroupService.getUserPrivilegeCategory(tenantId, 0, false);
        for (CategoryPrivilegeV2 categoryPrivilegeV2 : userPrivilegeCategory.values()) {
            if (categoryPrivilegeV2.getEditItem()) {
                strings.add(categoryPrivilegeV2.getGuid());
            }
        }
        if (strings != null && strings.contains(categoryId)) {
            return getDataSourceResultV2(parameters, strings, categoryId, tenantId);
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
    }

    //备用，一组目录查子库加表
    @Transactional(rollbackFor = Exception.class)
    public PageResult<DataSourceHeader> getDataSourceResultV2(Parameters parameters, List<String> strings, String categoryGuid, String tenantId) throws AtlasBaseException {
        String query = parameters.getQuery();
        if (Objects.nonNull(query))
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        PageResult<DataSourceHeader> databasePageResult = new PageResult<>();
        //如果没目录
        if (strings.size() == 0) {
            databasePageResult.setCurrentSize(0);
            databasePageResult.setTotalSize(0);
            return databasePageResult;
        }

        List<DataSourceHeader> databaseHeaders = userGroupDAO.getSourceInfo(strings, query, parameters.getOffset(), parameters.getLimit(), tenantId);

        List<String> databases = tenantService.getDatabase(tenantId);
        //获取用户有权限的全部表和该目录已加关联的全部表
        List<TechnologyInfo.Table> tables = userGroupDAO.getTableInfosV2(strings, "", 0, -1, databases, tenantId);

        if(CollectionUtils.isEmpty(tables)){
            return databasePageResult;
        }
        List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryGuid);
        Map<String, List<TechnologyInfo.Table>> collect = tables.stream().collect(Collectors.groupingBy(TechnologyInfo.Table::getSourceId));
        databaseHeaders.forEach(e -> {
            String sourceId = e.getSourceId();
            List<String> table = collect.get(sourceId).stream().map(TechnologyInfo.Table::getTableGuid).collect(Collectors.toList());
            if (collect != null && collect.size() > 0) {
                if (relationTableGuids.containsAll(table)) {
                    //全被勾选
                    e.setCheck(1);
                } else {
                    table.retainAll(relationTableGuids);
                    if (table.size() > 0) {
                        //勾选了部分
                        e.setCheck(2);
                    } else {
                        //全部未勾选
                        e.setCheck(0);
                    }
                }
            }
        });
        databasePageResult.setLists(databaseHeaders);
        databasePageResult.setCurrentSize(databaseHeaders.size());
        databasePageResult.setTotalSize(databaseHeaders.size() > 0 ? databaseHeaders.get(0).getTotal() : 0);
        return databasePageResult;
    }

    //1.4获取关联表，获取有权限的目录下的库
    @Transactional(rollbackFor = Exception.class)
    public PageResult<DatabaseHeader> getTechnicalDatabasePageResultV2(Parameters parameters, String sourceId, String categoryId, String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        List<String> strings = new ArrayList<>();

        Map<String, CategoryPrivilegeV2> userPrivilegeCategory = userGroupService.getUserPrivilegeCategory(tenantId, 0, false);
        for (CategoryPrivilegeV2 categoryPrivilegeV2 : userPrivilegeCategory.values()) {
            if (categoryPrivilegeV2.getEditItem()) {
                strings.add(categoryPrivilegeV2.getGuid());
            }
        }

        if (strings != null && strings.contains(categoryId)) {
            return getDatabaseResultV2(parameters, strings, sourceId, categoryId, tenantId);
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
    }

    /**
     * 1.4获取关联表，根据库获取表
     *
     * @param parameters
     * @return
     * @throws AtlasBaseException
     */
    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddRelationTable> getTechnicalTablePageResultByDB(Parameters parameters, String databaseGuid, String categoryId, String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        List<String> strings = new ArrayList<>();
        //判断多租户和独立部署
        if (TenantService.defaultTenant.equals(tenantId)) {
            List<Role> roles = roleDAO.getRoleByUsersId(user.getUserId());
            if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, " 当前用户所属角色已被禁用");
            }
            if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                List<String> topCategoryGuid = roleDAO.getAllCategorys(0, tenantId).stream().map(category -> category.getGuid()).collect(Collectors.toList());
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(topCategoryGuid, 0, tenantId);
                for (RoleModulesCategories.Category child : childs) {
                    strings.add(child.getGuid());
                }
                return getTablesByDatabaseGuid(parameters, strings, databaseGuid, categoryId, tenantId);
            }
            strings = getChildAndOwnerCategorysByRoles(roles);
        } else {
            Map<String, CategoryPrivilegeV2> userPrivilegeCategory = userGroupService.getUserPrivilegeCategory(tenantId, 0, false);
            for (CategoryPrivilegeV2 categoryPrivilegeV2 : userPrivilegeCategory.values()) {
                if (categoryPrivilegeV2.getEditItem()) {
                    strings.add(categoryPrivilegeV2.getGuid());
                }
            }
        }
        if (strings != null && strings.size() != 0) {
            return getTablesByDatabaseGuid(parameters, strings, databaseGuid, categoryId, tenantId);
        }

        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");


    }

    //备用，一组目录查子库加表
    @Transactional(rollbackFor = Exception.class)
    public PageResult<DatabaseHeader> getDatabaseResultV2(Parameters parameters, List<String> strings, String sourceId, String categoryGuid, String tenantId) throws AtlasBaseException {
        List<DatabaseHeader> databaseHeaders = new ArrayList<>();
        String query = parameters.getQuery();
        if (Objects.nonNull(query))
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        PageResult<DatabaseHeader> databasePageResult = new PageResult<>();
        //如果没目录
        if (strings.size() == 0) {
            databasePageResult.setCurrentSize(0);
            databasePageResult.setTotalSize(0);
            return databasePageResult;
        }
        List<TechnologyInfo.Table> tables = new ArrayList<>();

        User user = AdminUtils.getUserData();
        List<String> databases = tenantService.getDatabase(tenantId);
        if (databases != null && databases.size() != 0) {
            databaseHeaders = userGroupDAO.getDBInfo2(strings, query, parameters.getOffset(), parameters.getLimit(), databases, sourceId, tenantId);
            //获取用户有权限的全部表和该目录已加关联的全部表
            tables = userGroupDAO.getTableInfosV2(strings, "", 0, -1, databases, tenantId);
        }
        List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryGuid);
        Map<String, List<TechnologyInfo.Table>> collect = tables.stream().collect(Collectors.groupingBy(TechnologyInfo.Table::getDatabaseGuid));
        databaseHeaders.forEach(e -> {
            String databaseGuid = e.getDatabaseGuid();
            List<String> table = collect.get(databaseGuid).stream().map(TechnologyInfo.Table::getTableGuid).collect(Collectors.toList());
            if (relationTableGuids.containsAll(table)) {
                //全被勾选
                e.setCheck(1);
            } else {
                table.retainAll(relationTableGuids);
                if (table.size() > 0) {
                    //勾选了部分
                    e.setCheck(2);
                } else {
                    //全部未勾选
                    e.setCheck(0);
                }
            }
        });
        databasePageResult.setLists(databaseHeaders);
        databasePageResult.setCurrentSize(databaseHeaders.size());
        databasePageResult.setTotalSize(databaseHeaders.size() > 0 ? databaseHeaders.get(0).getTotal() : 0);
        return databasePageResult;
    }

    private PageResult<AddRelationTable> getTablesByDatabaseGuid(Parameters parameters, List<String> strings, String databaseGuid, String categoryId, String tenantId) {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();
        //如果没目录
        if (strings.size() == 0) {
            tablePageResult.setCurrentSize(0);
            tablePageResult.setTotalSize(0);
            return tablePageResult;
        }

        List<TechnologyInfo.Table> tableInfos = roleDAO.getTableInfosByDBIdByParameters(strings, databaseGuid, parameters.getOffset(), parameters.getLimit(), tenantId);
        List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryId);
        List<AddRelationTable> tables = getTables(tableInfos);
        supplyPath(tables, tenantId);
        tables.forEach(e -> {
            String tableGuid = e.getTableId();
            if (relationTableGuids.contains(tableGuid)) e.setCheck(1);
            else e.setCheck(0);
        });
        tablePageResult.setLists(tables);
        tablePageResult.setCurrentSize(tableInfos.size());
        tablePageResult.setTotalSize(roleDAO.getTableInfosByDBIdCount(strings, databaseGuid));
        return tablePageResult;

    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddRelationTable> getTechnicalTablePageResultV2(Parameters parameters, String categoryId, String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        List<String> strings = new ArrayList<>();
        //判断多租户和独立部署
        if (TenantService.defaultTenant.equals(tenantId)) {
            List<Role> roles = roleDAO.getRoleByUsersId(user.getUserId());
            if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            }
            if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                List<String> topCategoryGuid = roleDAO.getAllCategorys(0, tenantId).stream().map(category -> category.getGuid()).collect(Collectors.toList());
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(topCategoryGuid, 0, tenantId);
                for (RoleModulesCategories.Category child : childs) {
                    strings.add(child.getGuid());
                }
                return getTableResultV3(parameters, strings, categoryId, tenantId);
            }

            List<Module> moduleByRoleId = userDAO.getModuleByUserId(user.getUserId());
            for (Module module : moduleByRoleId) {
                //有管理技术目录权限
                //如果因为校验过多影响性能，可以取消校验
                if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                    strings = getChildAndOwnerCategorysByRoles(roles);
                    break;
                }
            }
        } else {
            Map<String, CategoryPrivilegeV2> userPrivilegeCategory = userGroupService.getUserPrivilegeCategory(tenantId, 0, false);
            for (CategoryPrivilegeV2 categoryPrivilegeV2 : userPrivilegeCategory.values()) {
                if (categoryPrivilegeV2.getEditItem()) {
                    strings.add(categoryPrivilegeV2.getGuid());
                }
            }
        }

        if (strings != null && strings.contains(categoryId)) {
            return getTableResultV3(parameters, strings, categoryId, tenantId);
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
    }


    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddRelationTable> getPermissionTablePageResultV2(Parameters parameters, String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        //判断多租户和独立部署
        if (TenantService.defaultTenant.equals(tenantId)) {
            //独立部署
            List<Role> roles = roleDAO.getRoleByUsersId(user.getUserId());
            if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            }
            List<String> strings = new ArrayList<>();
            if (roles != null && roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0, tenantId);
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(topCategoryGuid, 0, TenantService.defaultTenant);
                for (RoleModulesCategories.Category child : childs) {
                    strings.add(child.getGuid());
                }
                return getTableResultV2(parameters, strings);
            }
            strings = getChildAndOwnerCategorysByRoles(roles);
            if (strings.size() > 0) {
                return getTableResultV2(parameters, strings);
            }
        } else {
            //多租户
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            List<String> strings = new ArrayList<>();
            strings = getChildAndOwnerCategorysByRoles(userGroups, tenantId);
            if (strings.size() > 0) {
                return getTableResultV2(parameters, strings, tenantId);
            }
        }
        return null;
    }


    //一组目录查子表,找出目录已勾选的表
    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddRelationTable> getTableResultV3(Parameters parameters, List<String> strings, String categoryId, String tenantId) throws AtlasBaseException {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();
        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        List<TechnologyInfo.Table> tableInfo = new ArrayList<>();
        if (strings.size() == 0) {
            tablePageResult.setTotalSize(0);
            tablePageResult.setCurrentSize(0);
            return tablePageResult;
        }
        if (Objects.nonNull(query))
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        //判断独立部署和多租户
        if (TenantService.defaultTenant.equals(tenantId)) {
            tableInfo = roleDAO.getTableInfosV2(strings, query, offset, limit);
        } else {
            User user = AdminUtils.getUserData();
            List<String> databases = tenantService.getDatabase(tenantId);
            if (databases != null && databases.size() != 0)
                tableInfo = userGroupDAO.getTableInfosV2(strings, query, offset, limit, databases, tenantId);
        }

        List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryId);
        List<AddRelationTable> tables = getTables(tableInfo);
        supplyPath(tables, tenantId);
        tables.forEach(e -> {
            String tableGuid = e.getTableId();
            if (relationTableGuids.contains(tableGuid)) e.setCheck(1);
            else e.setCheck(0);
        });
        if (tableInfo.size() != 0) {
            tablePageResult.setTotalSize(tableInfo.get(0).getTotal());
        } else {
            tablePageResult.setTotalSize(0);
        }
        tablePageResult.setCurrentSize(tableInfo.size());
        tablePageResult.setLists(tables);
        return tablePageResult;
    }

    //一组目录查子表
    //独立部署
    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddRelationTable> getTableResultV2(Parameters parameters, List<String> categoryIds) {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();

        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        List<TechnologyInfo.Table> tableInfo = null;
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0, TenantService.defaultTenant);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            if (strings.size() == 0) {
                tablePageResult.setTotalSize(0);
                tablePageResult.setCurrentSize(0);
                return tablePageResult;
            }
            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            tableInfo = roleDAO.getTableInfosV2(strings, query, offset, limit);
            List<AddRelationTable> tables = getTables(tableInfo);
            tablePageResult.setTotalSize(0);
            if (tableInfo.size() != 0) {
                tablePageResult.setTotalSize(tableInfo.get(0).getTotal());
            }
            tablePageResult.setCurrentSize(tableInfo.size());
            tablePageResult.setLists(tables);
        }
        return tablePageResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AddRelationTable> getTables(List<TechnologyInfo.Table> tableInfo) {
        List<AddRelationTable> lists = new ArrayList<>();
        for (TechnologyInfo.Table table : tableInfo) {
            AddRelationTable tb = new AddRelationTable();
            tb.setTableId(table.getTableGuid());
            tb.setTableName(table.getTableName());
            tb.setDatabaseName(table.getDbName());
            tb.setCreateTime(table.getCreateTime());
            tb.setDatabaseId(table.getDatabaseGuid());
            tb.setStatus(table.getStatus());
            lists.add(tb);
        }
        return lists;
    }

    //多租户
    @Transactional(rollbackFor = Exception.class)
    public PageResult<AddRelationTable> getTableResultV2(Parameters parameters, List<String> categoryIds, String tenantId) throws AtlasBaseException {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();
        User user = AdminUtils.getUserData();
        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        List<TechnologyInfo.Table> tableInfo = new ArrayList<>();
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = userGroupDAO.getChildAndOwnerCategorys(categoryIds, 0, tenantId);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            if (strings.size() == 0) {
                tablePageResult.setTotalSize(0);
                tablePageResult.setCurrentSize(0);
                return tablePageResult;
            }
            List<String> databases = tenantService.getDatabase(tenantId);
            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            if (databases != null && databases.size() != 0)
                tableInfo = userGroupDAO.getTableInfosV2(strings, query, offset, limit, databases, tenantId);
            List<AddRelationTable> tables = getTables(tableInfo);
            tablePageResult.setTotalSize(0);
            if (tableInfo.size() != 0) {
                tablePageResult.setTotalSize(tableInfo.get(0).getTotal());
            }
            tablePageResult.setCurrentSize(tableInfo.size());
            tablePageResult.setLists(tables);
        }
        return tablePageResult;
    }

    public void supplyPath(List<AddRelationTable> list, String tenantId) {
        Map<String, String> category2Path = new HashMap();
        for (AddRelationTable table : list) {
            List<String> categoryGuidByTableGuid = categoryDAO.getCategoryGuidByTableGuid(table.getTableId(), tenantId);
            if (categoryGuidByTableGuid == null) {
                table.setPath("");
            } else if (categoryGuidByTableGuid.size() != 1) {
                table.setPath("");
            } else {
                String categoryGuid = categoryGuidByTableGuid.get(0);
                if (category2Path.containsKey(categoryGuid)) {
                    table.setPath(category2Path.get(categoryGuid));
                } else {
                    String path = categoryDAO.queryPathByGuid(categoryGuid, tenantId).replace(",", "/").replace("\"", "").replace("{", "").replace("}", "");
                    table.setPath(path);
                    category2Path.put(categoryGuid, path);
                }
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public PageResult<Database> getDatabasePageResultV2(Parameters parameters, String tenantId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        List<String> strings = new ArrayList<>();
        //判断多租户和独立部署
        if (TenantService.defaultTenant.equals(tenantId)) {
            List<Role> roles = roleDAO.getRoleByUsersId(user.getUserId());
            if (roles.stream().anyMatch(role -> SystemRole.ADMIN.getCode().equals(role.getRoleId()))) {
                List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0, tenantId);
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(topCategoryGuid, 0, tenantId);
                for (RoleModulesCategories.Category child : childs) {
                    strings.add(child.getGuid());
                }
                return getDatabaseV2(parameters, strings, tenantId);
            }
            if (roles.stream().allMatch(role -> role.getStatus() == 0)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            }
            strings = getChildAndOwnerCategorysByRoles(roles);
        } else {
            List<UserGroup> userGroups = userGroupDAO.getuserGroupByUsersId(user.getUserId(), tenantId);
            strings = getChildAndOwnerCategorysByRoles(userGroups, tenantId);
        }
        if (strings.size() > 0) {
            return getDatabaseV2(parameters, strings, tenantId);
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户无获取库表权限");
    }


    //独立部署
    public List<String> getChildAndOwnerCategorysByRoles(List<Role> roles) {
        ArrayList<String> strings = new ArrayList<>();
        for (Role role : roles) {
            String roleId = role.getRoleId();
            if (role.getStatus() == 0) {
                continue;
            }
            List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0, TenantService.defaultTenant);
            if (categorysByTypeIds.size() > 0) {
                List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categorysByTypeIds, 0, TenantService.defaultTenant);
                for (RoleModulesCategories.Category child : childs) {
                    if (!strings.contains(child.getGuid())) {
                        strings.add(child.getGuid());
                    }
                }
            }
        }
        return strings;
    }

    //多租户
    public List<String> getChildAndOwnerCategorysByRoles(List<UserGroup> userGroups, String tenantId) {
        ArrayList<String> strings = new ArrayList<>();
        for (UserGroup userGroup : userGroups) {
            String userGroupId = userGroup.getId();
            List<String> categorysByTypeIds = userGroupDAO.getCategorysByTypeIds(userGroupId, 0, tenantId);
            if (categorysByTypeIds.size() > 0) {
                List<RoleModulesCategories.Category> childs = userGroupDAO.getChildAndOwnerCategorys(categorysByTypeIds, 0, tenantId);
                for (RoleModulesCategories.Category child : childs) {
                    if (!strings.contains(child.getGuid())) {
                        strings.add(child.getGuid());
                    }
                }
            }
        }
        return strings;
    }

    @Transactional(rollbackFor = Exception.class)
    public PageResult<Database> getDatabaseV2(Parameters parameters, List<String> strings, String tenantId) throws AtlasBaseException {
        List<DatabaseHeader> dbName = null;
        long totalSize;
        PageResult<Database> databasePageResult = new PageResult<>();
        //如果没目录
        if (strings.size() == 0) {
            databasePageResult.setCurrentSize(0);
            databasePageResult.setTotalSize(0);
            return databasePageResult;
        }
        //判断独立部署和多租户
        if (TenantService.defaultTenant.equals(tenantId)) {
            dbName = roleDAO.getDBInfo(strings, parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
            totalSize = roleDAO.getDBCountV2(strings, parameters.getQuery());
        } else {
            User user = AdminUtils.getUserData();
            List<String> databases = tenantService.getDatabase(tenantId);
            if (databases != null && databases.size() != 0)
                dbName = userGroupDAO.getDBInfo(strings, parameters.getQuery(), parameters.getOffset(), parameters.getLimit(), databases, tenantId);
            totalSize = databases != null && databases.size() != 0 ? userGroupDAO.getDBCountV2(strings, parameters.getQuery(), databases, tenantId) : 0;
        }
        List<Database> lists = new ArrayList<>();
        if (dbName == null) {
            return databasePageResult;
        }
        for (DatabaseHeader db : dbName) {
            Database database = new Database();
            database.setDatabaseId(db.getDatabaseGuid());
            database.setDatabaseName(db.getDbName());
            lists.add(database);
        }
        databasePageResult.setLists(lists);
        databasePageResult.setCurrentSize(dbName.size());
        databasePageResult.setTotalSize(totalSize);
        return databasePageResult;
    }

    /**
     * 获取用户管理的所有表id
     *
     * @return
     * @throws AtlasBaseException
     */
    public List<String> getUserTableIds(@HeaderParam("tenantId") String tenantId) throws AtlasBaseException {
        try {
            //判断独立部署和多租户
            if (TenantService.defaultTenant.equals(tenantId)) {
                List<String> categoryIds = getPermissionCategoryIds();
                if (Objects.isNull(categoryIds) || categoryIds.size() == 0) {
                    return new ArrayList<String>();
                }
                return roleDAO.getTableIds(categoryIds);
            } else {
                User user = AdminUtils.getUserData();
                List<String> categoryIds = getPermissionCategoryIds(tenantId);
                if (Objects.isNull(categoryIds) || categoryIds.size() == 0) {
                    return new ArrayList<String>();
                }
                List<String> databases = tenantService.getDatabase(tenantId);
                return databases != null && databases.size() != 0 ? userGroupDAO.getTableIds(categoryIds, databases, tenantId) : new ArrayList<>();
            }
        } catch (Exception e) {
            LOG.error("获取用户管理的所有表失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

    public String dbsToString(List<String> dbs) {
        if (dbs == null || dbs.size() == 0) {
            return "";
        }
        StringBuffer str = new StringBuffer();
        for (String db : dbs) {
            str.append("'");
            str.append(db.replaceAll("'", "\\\\'"));
            str.append("'");
            str.append(",");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }
}
