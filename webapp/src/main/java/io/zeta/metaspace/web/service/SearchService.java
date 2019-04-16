package io.zeta.metaspace.web.service;

import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.*;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.privilege.SystemModule;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.role.SystemRole;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.web.dao.CategoryDAO;
import io.zeta.metaspace.web.dao.RelationDAO;
import io.zeta.metaspace.web.dao.RoleDAO;
import io.zeta.metaspace.web.dao.UserDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.EntityDiscoveryService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.rest.EntityREST;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

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

/*    @Transactional
    public PageResult<Database> getTechnicalDatabasePageResult(Parameters parameters, String categoryId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();
        RoleModulesCategories.Category category = roleDAO.getCategoryByGuid(categoryId);
        if (SystemRole.ADMIN.getCode().equals(roleId) && (category.getParentCategoryGuid() == null || category.getParentCategoryGuid().equals(""))) {
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            String queryDb = parameters.getQuery();
            return metaspaceEntityService.getAllDBAndTable(queryDb, limit, offset);
        } else if (SystemRole.ADMIN.getCode().equals(roleId)) {
            return getDatabaseResult(parameters, category.getParentCategoryGuid());
        } else {
            List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
            if (categorysByTypeIds.size() > 0) {
                List<RoleModulesCategories.Category> childs = roleDAO.getChildCategorys(categorysByTypeIds, 0);
                for (RoleModulesCategories.Category child : childs) {
                    if (child.getGuid().equals(categoryId)) {
                        return getDatabaseResult(parameters, category.getParentCategoryGuid());
                    }
                }
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
        }
    }

    @Transactional
    public PageResult<Database> getDatabaseResult(Parameters parameters, String categoryId) {
        List<String> dbName = null;
        dbName = roleDAO.getDBNames(categoryId, parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
        PageResult<Database> databasePageResult = new PageResult<>();

        List<Database> lists = new ArrayList<>();
        for (String s : dbName) {
            Database database = new Database();
            database.setDatabaseName(s);

            List<TechnologyInfo.Table> tbs = roleDAO.getTableInfosByDB(categoryId, s);
            List<Table> tables = getTables(tbs);
            database.setTableList(tables);
            lists.add(database);
        }
        databasePageResult.setLists(lists);
        databasePageResult.setOffset(parameters.getOffset());
        databasePageResult.setCount(dbName.size());
        databasePageResult.setSum(roleDAO.getDBCount(categoryId, parameters.getQuery()));
        return databasePageResult;
    }

    @Transactional
    public PageResult<Table> getTechnicalTablePageResult(Parameters parameters, String categoryId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();
        RoleModulesCategories.Category category = roleDAO.getCategoryByGuid(categoryId);
        if (SystemRole.ADMIN.getCode().equals(roleId) && (category.getParentCategoryGuid() == null || category.getParentCategoryGuid().equals(""))) {
            return metaspaceEntityService.getTableNameAndDbNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
        } else if (SystemRole.ADMIN.getCode().equals(roleId)) {
            PageResult<Table> table = getTableResult(parameters, category.getParentCategoryGuid());
            return table;
        } else {
            List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
            if (categorysByTypeIds.size() > 0) {
                List<RoleModulesCategories.Category> childs = roleDAO.getChildCategorys(categorysByTypeIds, 0);
                for (RoleModulesCategories.Category child : childs) {
                    if (child.getGuid().equals(categoryId)) {
                        PageResult<Table> table = getTableResult(parameters, category.getParentCategoryGuid());
                        return table;
                    }
                }
            }
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
        }
    }

    @Transactional
    public PageResult<Table> getTableResult(Parameters parameters, String categoryId) {
        PageResult<Table> tablePageResult = new PageResult<>();
        String query = parameters.getQuery();
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        List<TechnologyInfo.Table> tableInfo = null;
        tableInfo = roleDAO.getTableInfos(categoryId, query, offset, limit);
        List<Table> tables = getTables(tableInfo);
        tablePageResult.setSum(roleDAO.getTableCount(categoryId, query));
        tablePageResult.setCount(tableInfo.size());
        tablePageResult.setOffset(offset);
        tablePageResult.setLists(tables);
        return tablePageResult;
    }*/

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

    @Cacheable(value = "TableByDBCache", key = "#databaseId + #offset + #limit")
    public PageResult<Table> getTableByDB(String databaseId, long offset, long limit) throws AtlasBaseException {
        return metaspaceEntityService.getTableByDB(databaseId, offset, limit);
    }

    public PageResult<TableInfo> getTableByDBWithQueryWithoutTmp(String databaseId, Parameters parameters) throws AtlasBaseException {
        try {
            long limit = parameters.getLimit();
            long offset = parameters.getOffset();
            String query = parameters.getQuery();
            PageResult<TableInfo> pageResult = new PageResult<>();
            List<TableInfo> tableList = relationDAO.getDbTablesWithoutTmp(databaseId, query, limit, offset);
            int countDbTable = relationDAO.countDbTablesWithoutTmp(databaseId, query);
            pageResult.setLists(tableList);
            pageResult.setSum(countDbTable);
            pageResult.setCount(tableList.size());
            return pageResult;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "");
        }
    }

    @Cacheable(value = "tablePageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Table> getTablePageResultV2(Parameters parameters) throws AtlasBaseException {
        return metaspaceEntityService.getTableNameAndDbNameByQuery(parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
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
        if (name == "") {
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
        if (name == "") {
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

    //1.4获取关联表，获取有权限的目录下的表
    @Transactional
    public PageResult<Database> getTechnicalDatabasePageResultV2(Parameters parameters, String categoryId) throws AtlasBaseException {
        User user = AdminUtils.getUserData();
        Role role = roleDAO.getRoleByUsersId(user.getUserId());
        if (role.getStatus() == 0)
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前用户所属角色已被禁用");
        String roleId = role.getRoleId();
        List<UserInfo.Module> moduleByRoleId = userDAO.getModuleByRoleId(roleId);
        for (UserInfo.Module module : moduleByRoleId) {
            //有管理技术目录权限
            if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                //admin有全部目录权限，且可以给一级目录加关联
                if (roleId.equals(SystemRole.ADMIN.getCode())) {
                    List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
                    return getDatabaseResultV2(parameters, topCategoryGuid);
                } else {
                    List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
                    if (categorysByTypeIds.size() > 0) {
                        List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categorysByTypeIds, 0);
                        for (RoleModulesCategories.Category child : childs) {
                            //当该目录是用户有权限目录的子目录时
                            if (child.getGuid().equals(categoryId)) {
                                return getDatabaseResultV2(parameters, categorysByTypeIds);
                            }
                        }
                    }

                }
            }
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");


    }

    //备用，一组目录查子库加表
    @Transactional
    public PageResult<Database> getDatabaseResultV2(Parameters parameters, List<String> categoryIds) {
        List<String> dbName = null;
        PageResult<Database> databasePageResult = new PageResult<>();
        if (categoryIds.size() > 0) {
            List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categoryIds, 0);
            ArrayList<String> strings = new ArrayList<>();
            for (RoleModulesCategories.Category child : childs) {
                strings.add(child.getGuid());
            }
            //如果没目录
            if (strings.size() == 0) {
                databasePageResult.setOffset(parameters.getOffset());
                databasePageResult.setCount(0);
                databasePageResult.setSum(0);
                return databasePageResult;
            }
            dbName = roleDAO.getDBNamesV2(strings, parameters.getQuery(), parameters.getOffset(), parameters.getLimit());
            List<Database> lists = new ArrayList<>();
            for (String s : dbName) {
                Database database = new Database();
                database.setDatabaseName(s);
                List<TechnologyInfo.Table> tbs = roleDAO.getTableInfosByDBV2(strings, s);
                List<AddRelationTable> tables = getTables(tbs);
                database.setTableList(tables);
                lists.add(database);
            }
            databasePageResult.setLists(lists);
            databasePageResult.setOffset(parameters.getOffset());
            databasePageResult.setCount(dbName.size());
            databasePageResult.setSum(roleDAO.getDBCountV2(strings, parameters.getQuery()));
        }
        return databasePageResult;
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
            if (module.getModuleId() == SystemModule.TECHNICAL_OPERATE.getCode()) {
                //admin有全部目录权限，且可以给一级目录加关联
                if (roleId.equals(SystemRole.ADMIN.getCode())) {
                    List<String> topCategoryGuid = roleDAO.getTopCategoryGuid(0);
                    return getTableResultV2(parameters, topCategoryGuid);
                } else {
                    List<String> categorysByTypeIds = roleDAO.getCategorysByTypeIds(roleId, 0);
                    if (categorysByTypeIds.size() > 0) {
                        List<RoleModulesCategories.Category> childs = roleDAO.getChildAndOwnerCategorys(categorysByTypeIds, 0);
                        for (RoleModulesCategories.Category child : childs) {
                            //当该目录是用户有权限目录的子目录时
                            if (child.getGuid().equals(categoryId)) {
                                return getTableResultV2(parameters, categorysByTypeIds);
                            }
                        }
                    }
                }
            }
        }
        throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "用户对该目录没有添加关联表的权限");
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
                tablePageResult.setSum(0);
                tablePageResult.setCount(0);
                tablePageResult.setOffset(offset);
                return tablePageResult;
            }
            tableInfo = roleDAO.getTableInfosV2(strings, query, offset, limit);
            List<AddRelationTable> lists = getTables(tableInfo);
            tablePageResult.setSum(roleDAO.getTableCountV2(strings, query));
            tablePageResult.setCount(tableInfo.size());
            tablePageResult.setOffset(offset);
            tablePageResult.setLists(lists);
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
            tb.setStatus(table.getStatus());
            String categoryGuidByTableGuid = categoryDAO.getCategoryGuidByTableGuid(table.getTableGuid());
            if (categoryGuidByTableGuid == null) {
                tb.setPath("");
            } else {
                tb.setPath(categoryDAO.queryPathByGuid(categoryGuidByTableGuid).replace(",", ".").replace("\"", "").replace("{", "").replace("}", ""));
            }
            lists.add(tb);
        }
        return lists;
    }
}
