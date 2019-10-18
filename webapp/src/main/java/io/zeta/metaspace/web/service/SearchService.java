package io.zeta.metaspace.web.service;

import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.RelationDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.metadata.RMDBEnum;
import io.zeta.metaspace.web.util.AESUtils;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.EntityDiscoveryService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.rest.EntityREST;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import schemacrawler.tools.databaseconnector.DatabaseConnectionSource;
import schemacrawler.tools.databaseconnector.SingleUseUserCredentials;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    DataSourceService  dataSourceService;

    @Cacheable(value = "databaseSearchCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Database> getDatabasePageResult(Parameters parameters) throws AtlasBaseException {
        long limit = parameters.getLimit();
        long offset = parameters.getOffset();
        String queryDb = parameters.getQuery();
        return metaspaceEntityService.getDatabaseByQuery(queryDb, false, offset, limit);
    }

    public PageResult<Database> getActiveDatabase(Parameters parameters) throws AtlasBaseException {
        long limit = parameters.getLimit();
        long offset = parameters.getOffset();
        String queryDb = parameters.getQuery();
        return metaspaceEntityService.getDatabaseByQuery(queryDb, true, offset, limit);
    }

    @Cacheable(value = "RDBMSDataSourceSearchCache", key = "#parameters.query + #parameters.limit + #parameters.offset + #sourceType")
    public PageResult<RDBMSDataSource> getDataSourcePageResult(Parameters parameters,String sourceType) throws AtlasBaseException {
        long limit = parameters.getLimit();
        long offset = parameters.getOffset();
        String querySource = parameters.getQuery();
        return metaspaceEntityService.getRDBMSDataSourceByQuery(querySource,offset, limit,sourceType);
    }
    @Cacheable(value = "RDBMSDBBySourceCache", key = "#sourceId + #offset + #limit")
    public PageResult<RDBMSDatabase> getRDBMSDBBySource(String sourceId, long offset, long limit) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSDBBySource(sourceId, offset, limit);
    }

    @Cacheable(value = "RDBMSTableByDBCache", key = "#databaseId + #offset + #limit")
    public PageResult<RDBMSTable> getRDBMSTableByDB(String databaseId, long offset, long limit) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSTableByDB(databaseId, offset, limit);
    }

    @Cacheable(value = "TableByDBCache", key = "#databaseId + #offset + #limit")
    public PageResult<Table> getTableByDB(String databaseId, long offset, long limit) throws AtlasBaseException {
        return metaspaceEntityService.getTableByDB(databaseId, offset, limit);
    }


    public List<String> getPermissionCategoryIds() throws AtlasBaseException {
        try {
            User user = AdminUtils.getUserData();
            Role role = roleDAO.getRoleByUsersId(user.getUserId());
            if (role.getStatus() == 0)
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
            String roleId = role.getRoleId();
            List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(roleId);
            List<String> categoryIds = new ArrayList<>();

            //admin有全部目录权限，且可以给一级目录加关联
            if (roleId.equals(SystemRole.ADMIN.getCode())) {
                categoryIds = roleDAO.getTopCategoryGuid(0);
            } else {
                categoryIds = roleDAO.getCategorysByTypeIds(roleId, 0);
            }
            if (categoryIds.size()==0){
                return categoryIds;
            }
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            return strings;
        } catch (AtlasBaseException e) {
            throw e;
        }
    }

    public PageResult<TableInfo> getTableByDBWithQueryWithoutTmp(String databaseId, Parameters parameters) throws AtlasBaseException {
        try {
            List<String> categoryIds = getPermissionCategoryIds();
            PageResult<TableInfo> pageResult = new PageResult<>();
            if (Objects.isNull(categoryIds) || categoryIds.size() == 0) {
                return pageResult;
            }
            List<TableInfo> tableList = roleDAO.getTableInfosByDBId(categoryIds, databaseId);

            tableList.forEach(table -> {
                String displayName = table.getDisplayName();
                String tableName = table.getTableName();
                if(Objects.isNull(displayName) || "".equals(displayName.trim())) {
                    table.setDisplayName(tableName);
                }
            });

            pageResult.setLists(tableList);
            pageResult.setTotalSize(tableList.size());
            pageResult.setCurrentSize(tableList.size());
            return pageResult;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    @Cacheable(value = "tablePageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Table> getTablePageResultV2(Parameters parameters) throws AtlasBaseException {
        return metaspaceEntityService.getTableNameAndDbNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
    }

    @Cacheable(value = "RDBMSDBPageCache", key = "#parameters.query + #sourceType + #parameters.limit + #parameters.offset")
    public PageResult<RDBMSDatabase> getRDBMSDBPageResultV2(Parameters parameters,String sourceType) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSDBNameAndSourceNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(),sourceType);
    }

    @Cacheable(value = "RDBMSTablePageCache", key = "#parameters.query + #sourceType + #parameters.limit + #parameters.offset")
    public PageResult<RDBMSTable> getRDBMSTablePageResultV2(Parameters parameters,String sourceType) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSTableNameAndDBAndSourceNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(),sourceType);
    }

    @Cacheable(value = "RDBMSColumnPageCache", key = "#parameters.query + #sourceType + #parameters.limit + #parameters.offset")
    public PageResult<RDBMSColumn> getRDBMSColumnPageResultV2(Parameters parameters,String sourceType) throws AtlasBaseException {
        return metaspaceEntityService.getRDBMSColumnNameTableNameAndDBAndSourceNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit(),sourceType);
    }


    @Cacheable(value = "columnPageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Column> getColumnPageResultV2(Parameters parameters) throws AtlasBaseException {
        return metaspaceEntityService.getColumnNameAndTableNameAndDbNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
    }

    public TableShow getTableShow(GuidCount guidCount) throws AtlasBaseException, SQLException, IOException {
        TableShow tableShow = new TableShow();
        AtlasEntity.AtlasEntityWithExtInfo info = entitiesStore.getById(guidCount.getGuid());
        AtlasEntity entity = info.getEntity();
        String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
        if (name.equals("")) {
            System.out.println("该id不存在");
        }
        AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(guidCount.getGuid(), true);
        AtlasEntity tableEntity = tableInfo.getEntity();
        Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
        AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
        String dbDisplayText = db.getDisplayText();
        String sql = "select * from " + name + " limit " + guidCount.getCount();
        try (Connection conn = HiveJdbcUtils.getConnection(dbDisplayText);
             ResultSet resultSet = conn.createStatement().executeQuery(sql)) {
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
            return tableShow;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有找到数据");
        }


    }

    public BuildTableSql getBuildTableSql(String tableId) throws AtlasBaseException, SQLException, IOException {
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
        try (Connection conn = HiveJdbcUtils.getConnection(dbDisplayText);
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

        String name_path = entity.getAttribute("name_path") == null ? "" : entity.getAttribute("name_path").toString();
        String qualifiedName = entity.getAttribute("qualifiedName") == null ? "" : entity.getAttribute("qualifiedName").toString();
        String sourceId = qualifiedName.split("\\.")[0];
        DataSourceInfo dataSourceInfo = dataSourceService.getDataSourceInfo(sourceId);
        String sql = "";
        if (dataSourceInfo.getSourceType().toLowerCase().equals("mysql")){
            sql = "select * from "+ name_path +" limit " + guidCount.getCount();
        }else if (dataSourceInfo.getSourceType().toLowerCase().equals("oracle")){
            sql = "select * from "+ name_path +" where rownum <" + guidCount.getCount();
        }else {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持数据源类型"+dataSourceInfo.getSourceType());
        }

        try (Connection conn = getConnectionByDataSourceInfo(dataSourceInfo,null);
             ResultSet resultSet = conn.createStatement().executeQuery(sql)) {
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
                    String s = resultSet.getObject(column) == null ? "NULL" : resultSet.getString(column);
                    map.put(column, s);
                }
                resultList.add(map);
            }
            tableShow.setTableId(guidCount.getGuid());
            tableShow.setColumnNames(columns);
            tableShow.setLines(resultList);
            return tableShow;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "没有找到数据"+e.getMessage());
        }


    }

    public BuildTableSql getBuildRDBMSTableSql(String tableId) throws AtlasBaseException, SQLException, IOException {
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

        String name_path = entity.getAttribute("name_path") == null ? "" : entity.getAttribute("name_path").toString();
        String name = entity.getAttribute("name") == null ? "" : entity.getAttribute("name").toString();
        String qualifiedName = entity.getAttribute("qualifiedName") == null ? "" : entity.getAttribute("qualifiedName").toString();
        String sourceId = qualifiedName.split("\\.")[0];
        StringBuffer dbName = new StringBuffer();
        StringBuffer  tableName = new StringBuffer();
        String[] strs = name_path.split("\\.");
        for (int i=0;i<strs.length;i++){
            if (i<strs.length-name.split("\\.").length){
                dbName.append(strs[i]);
            }else{
                tableName.append(strs[i]);
            }
        }

        if (name.equals("")) {
            System.out.println("该id不存在");
        }
        DataSourceInfo dataSourceInfo = dataSourceService.getDataSourceInfo(sourceId);

        String sql = "";
        if (dataSourceInfo.getSourceType().toLowerCase().equals("mysql")){
            sql = "SHOW CREATE TABLE " + tableName;
        }else if(dataSourceInfo.getSourceType().toLowerCase().equals("oracle")){
            sql = "select dbms_metadata.get_ddl('TABLE','"+ tableName +"','"+ dbName +"') from dual";
        }else{
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持数据源类型"+dataSourceInfo.getSourceType());
        }
        try (Connection conn = getConnectionByDataSourceInfo(dataSourceInfo,dbName.toString());
             ResultSet resultSet = conn.createStatement().executeQuery(sql)) {
            StringBuffer stringBuffer = new StringBuffer();
            while (resultSet.next()) {
                String object = resultSet.getString(getSqlPlace(dataSourceInfo.getSourceType()));
                stringBuffer.append(object);
            }
            buildTableSql.setSql(stringBuffer.toString());
            buildTableSql.setTableId(tableId);
            return buildTableSql;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据源服务异常");
        }
    }

    public int getSqlPlace(String sourceType) throws AtlasBaseException {
        if (sourceType.toLowerCase().equals("mysql")){
            return 2;
        }else if(sourceType.toLowerCase().equals("oracle")){
            return 1;
        }else{
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持数据源类型"+sourceType);
        }
    }

    public Connection getConnectionByDataSourceInfo(DataSourceInfo dataSourceInfo,String dbName) throws AtlasBaseException {
        String           ip             = dataSourceInfo.getIp();
        String           port           = dataSourceInfo.getPort();
        String           sourceType     = dataSourceInfo.getSourceType();
        String           jdbcParameter  = dataSourceInfo.getJdbcParameter();
        String           userName       = dataSourceInfo.getUserName();
        String           password       = AESUtils.AESDecode(dataSourceInfo.getPassword());
        String connectUrl = RMDBEnum.of(sourceType).getConnectUrl();
        String connectionUrl = "";
        if(dataSourceInfo.getSourceType().toLowerCase().equals("oracle")|| dbName==null){
            connectionUrl = String.format(connectUrl, ip, port, dataSourceInfo.getDatabase());
        } else if (dataSourceInfo.getSourceType().toLowerCase().equals("mysql")){
            connectionUrl = String.format(connectUrl, ip, port, dbName);
        }else{
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "不支持数据源类型"+dataSourceInfo.getSourceType());
        }
        Map<String,String> map = new HashMap<>();
        if (StringUtils.isNotEmpty(jdbcParameter)) {
            for (String str :jdbcParameter.split("&")){
                String[] strings = str.split("=");
                if (strings.length==2){
                    map.put(strings[0],strings[1]);
                }
            }
        }

        DatabaseConnectionSource dataSource = new DatabaseConnectionSource(connectionUrl, map);
        dataSource.setUserCredentials(new SingleUseUserCredentials(userName, password));
        return dataSource.get();
    }


    //1.4获取关联表，获取有权限的目录下的库
    @Transactional
    public PageResult<DatabaseHeader> getTechnicalDatabasePageResultV2(Parameters parameters, String categoryId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();
        List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(roleId);
        for (UserInfo.Module module : moduleByRoleId) {
            //有管理技术目录权限
            //如果因为校验过多影响性能，可以取消校验
            if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                //admin有全部目录权限，且可以给一级目录加关联
                if (roleId.equals(SystemRole.ADMIN.getCode())) {
                    List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
                    return getDatabaseResultV2(parameters, topCategoryGuid, categoryId);
                } else {
                    List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
                    if (categorysByTypeIds.size() > 0) {
                        List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categorysByTypeIds, 0);
                        for (RoleModulesCategories.Category child : childs) {
                            //当该目录是用户有权限目录的子目录时
                            if (child.getGuid().equals(categoryId)) {
                                return getDatabaseResultV2(parameters, categorysByTypeIds, categoryId);
                            }
                        }
                    }
                }
            }
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
    @Transactional
    public PageResult<AddRelationTable> getTechnicalTablePageResultByDB(Parameters parameters, String databaseGuid, String categoryId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();

        //admin有全部目录权限，且可以给一级目录加关联
        if (roleId.equals(SystemRole.ADMIN.getCode())) {
            List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
            return getTablesByDatabaseGuid(parameters, topCategoryGuid, databaseGuid, categoryId);
        } else {
            List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
            if (categorysByTypeIds.size() > 0) {
                return getTablesByDatabaseGuid(parameters, categorysByTypeIds, databaseGuid, categoryId);
            }
        }

        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");


    }

    //备用，一组目录查子库加表
    @Transactional
    public PageResult<DatabaseHeader> getDatabaseResultV2(Parameters parameters, List<String> categoryIds, String categoryGuid) {
        List<DatabaseHeader> databaseHeaders = null;
        String query = parameters.getQuery();
        if (Objects.nonNull(query))
            query = query.replaceAll("%", "/%").replaceAll("_", "/_");
        PageResult<DatabaseHeader> databasePageResult = new PageResult<>();
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            //如果没目录
            if (strings.size() == 0) {
                //databasePageResult.setOffset(parameters.getOffset());
                databasePageResult.setCurrentSize(0);
                databasePageResult.setTotalSize(0);
                return databasePageResult;
            }
            databaseHeaders = roleDAO.getDBInfo(strings, query, parameters.getOffset(), parameters.getLimit());
            //获取用户有权限的全部表和该目录已加关联的全部表
            List<TechnologyInfo.Table> tables = roleDAO.getTableInfosV2(strings, "", 0, -1);
            List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryGuid);
            Map<String, List<TechnologyInfo.Table>> collect = tables.stream().collect(Collectors.groupingBy(TechnologyInfo.Table::getDatabaseGuid));
            databaseHeaders.forEach(e->{
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
            //databasePageResult.setOffset(parameters.getOffset());
            databasePageResult.setCurrentSize(databaseHeaders.size());
            databasePageResult.setTotalSize(roleDAO.getDBCountV2(strings, parameters.getQuery()));
        }
        return databasePageResult;
    }

    private PageResult<AddRelationTable> getTablesByDatabaseGuid(Parameters parameters, List<String> categoryIds, String databaseGuid, String categoryId) {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            //如果没目录
            if (strings.size() == 0) {
                //tablePageResult.setOffset(parameters.getOffset());
                tablePageResult.setCurrentSize(0);
                tablePageResult.setTotalSize(0);
                return tablePageResult;
            }

            List<TechnologyInfo.Table> tableInfos = roleDAO.getTableInfosByDBIdByParameters(strings, databaseGuid, parameters.getOffset(), parameters.getLimit());
            List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryId);
            List<AddRelationTable> tables = getTables(tableInfos);
            tables.forEach(e -> {
                String tableGuid = e.getTableId();
                if (relationTableGuids.contains(tableGuid)) e.setCheck(1);
                else e.setCheck(0);
            });
            tablePageResult.setLists(tables);
            //tablePageResult.setOffset(parameters.getOffset());
            tablePageResult.setCurrentSize(tableInfos.size());
            tablePageResult.setTotalSize(roleDAO.getTableInfosByDBIdCount(strings, databaseGuid));
        }
        return tablePageResult;

    }

    @Transactional
    public PageResult<AddRelationTable> getTechnicalTablePageResultV2(Parameters parameters, String categoryId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();
        List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(roleId);
        for (UserInfo.Module module : moduleByRoleId) {
            //有管理技术目录权限
            if (module.getModuleId() == SystemModule.TECHNICAL_CATALOG.getCode()) {
                //admin有全部目录权限，且可以给一级目录加关联
                if (roleId.equals(SystemRole.ADMIN.getCode())) {
                    List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
                    return getTableResultV3(parameters, topCategoryGuid,categoryId);
                } else {
                    List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
                    if (categorysByTypeIds.size() > 0) {
                        List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categorysByTypeIds, 0);
                        for (RoleModulesCategories.Category child : childs) {
                            //当该目录是用户有权限目录的子目录时
                            if (child.getGuid().equals(categoryId)) {
                                return getTableResultV3(parameters, categorysByTypeIds,categoryId);
                            }
                        }
                    }
                }
            }
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
    }


    @Transactional
    public PageResult<AddRelationTable> getPermissionTablePageResultV2(Parameters parameters) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();

        //admin有全部目录权限，且可以给一级目录加关联
        if (roleId.equals(SystemRole.ADMIN.getCode())) {
            List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
            return getTableResultV2(parameters, topCategoryGuid);
        } else {
            List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
            if (categorysByTypeIds.size() > 0) {
                return getTableResultV2(parameters, categorysByTypeIds);
            }
        }
        return null;
        //throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
    }


    //一组目录查子表,找出目录已勾选的表
    @Transactional
    public PageResult<AddRelationTable> getTableResultV3(Parameters parameters, List<String> categoryIds,String categoryId) {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();
        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        List<TechnologyInfo.Table> tableInfo = null;
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            if (strings.size() == 0) {
                tablePageResult.setTotalSize(0);
                tablePageResult.setCurrentSize(0);
                //tablePageResult.setOffset(offset);
                return tablePageResult;
            }
            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            tableInfo = roleDAO.getTableInfosV2(strings, query, offset, limit);
            List<String> relationTableGuids = relationDAO.getAllTableGuidByCategoryGuid(categoryId);
            List<AddRelationTable> tables = getTables(tableInfo);
            tables.forEach(e -> {
                String tableGuid = e.getTableId();
                if (relationTableGuids.contains(tableGuid)) e.setCheck(1);
                else e.setCheck(0);
            });
            if (tableInfo.size()!=0){
                tablePageResult.setTotalSize(tableInfo.get(0).getTotal());
            }else{
                tablePageResult.setTotalSize(0);
            }
            //tablePageResult.setTotalSize(roleDAO.getTableCountV2(strings, query));
            tablePageResult.setCurrentSize(tableInfo.size());
            //tablePageResult.setOffset(offset);
            tablePageResult.setLists(tables);
        }
        return tablePageResult;
    }
    //一组目录查子表
    @Transactional
    public PageResult<AddRelationTable> getTableResultV2(Parameters parameters, List<String> categoryIds) {
        PageResult<AddRelationTable> tablePageResult = new PageResult<>();

        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        List<TechnologyInfo.Table> tableInfo = null;
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            if (strings.size() == 0) {
                tablePageResult.setTotalSize(0);
                tablePageResult.setCurrentSize(0);
                //tablePageResult.setOffset(offset);
                return tablePageResult;
            }
            if (Objects.nonNull(query))
                query = query.replaceAll("%", "/%").replaceAll("_", "/_");
            tableInfo = roleDAO.getTableInfosV2(strings, query, offset, limit);
            List<AddRelationTable> tables = getTables(tableInfo);
            //tablePageResult.setTotalSize(roleDAO.getTableCountV2(strings, query));
            tablePageResult.setTotalSize(0);
            if (tableInfo.size()!=0){
                tablePageResult.setTotalSize(tableInfo.get(0).getTotal());
            }
            tablePageResult.setCurrentSize(tableInfo.size());
            //tablePageResult.setOffset(offset);
            tablePageResult.setLists(tables);
        }
        return tablePageResult;
    }
    @Transactional
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
            List<String> categoryGuidByTableGuid = categoryDAO.getCategoryGuidByTableGuid(table.getTableGuid());
            if (categoryGuidByTableGuid == null) {
                tb.setPath("");
            } else if (categoryGuidByTableGuid.size() != 1) {
                tb.setPath("");
            } else {
                tb.setPath(categoryDAO.queryPathByGuid(categoryGuidByTableGuid.get(0)).replace(",", "/").replace("\"", "").replace("{", "").replace("}", ""));
            }
            lists.add(tb);
        }
        return lists;
    }


    @Transactional
    public PageResult<Database> getDatabasePageResultV2(Parameters parameters) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();
        //admin有全部目录权限，且可以给一级目录加关联
        if (roleId.equals(SystemRole.ADMIN.getCode())) {
            List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
            return getDatabaseV2(parameters, topCategoryGuid);
        } else {
            List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
            if (categorysByTypeIds.size() > 0) {
                return getDatabaseV2(parameters, categorysByTypeIds);
            }
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户无获取库表权限");
    }

    @Transactional
    public PageResult<Database> getDatabaseV2(Parameters parameters, List<String> categoryIds) {
        List<DatabaseHeader> dbName = null;
        PageResult<Database> databasePageResult = new PageResult<>();
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            //如果没目录
            if (strings.size() == 0) {
                //databasePageResult.setOffset(parameters.getOffset());
                databasePageResult.setCurrentSize(0);
                databasePageResult.setTotalSize(0);
                return databasePageResult;
            }
            dbName = roleDAO.getDBInfo(strings, parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
            List<Database> lists = new ArrayList<>();
            for (DatabaseHeader db : dbName) {
                Database database = new Database();
                database.setDatabaseId(db.getDatabaseGuid());
                database.setDatabaseName(db.getDbName());
                lists.add(database);
            }
            databasePageResult.setLists(lists);
            //databasePageResult.setOffset(parameters.getOffset());
            databasePageResult.setCurrentSize(dbName.size());
            databasePageResult.setTotalSize(roleDAO.getDBCountV2(strings, parameters.getQuery()));
        }
        return databasePageResult;
    }

    /**
     * 获取用户管理的所有表id
     * @return
     * @throws AtlasBaseException
     */
    public List<String> getUserTableIds() throws AtlasBaseException {
        try {
            List<String> categoryIds = getPermissionCategoryIds();
            if (Objects.isNull(categoryIds) || categoryIds.size() == 0) {
                return new ArrayList<String>();
            }
            return roleDAO.getTableIds(categoryIds);
        }catch (Exception e){
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, e.getMessage());
        }
    }

}
