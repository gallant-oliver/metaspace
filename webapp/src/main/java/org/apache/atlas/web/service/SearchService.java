package org.apache.atlas.web.service;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.EntityDiscoveryService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.discovery.AtlasSearchResult;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import org.apache.atlas.model.metadata.*;
import org.apache.atlas.model.result.BuildTableSql;
import org.apache.atlas.model.result.PageResult;
import org.apache.atlas.model.result.TableShow;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.rest.DiscoveryREST;
import org.apache.atlas.web.rest.EntityREST;
import org.apache.atlas.web.util.HiveJdbcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import javax.ws.rs.QueryParam;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@AtlasService
public class SearchService {
    @Autowired
    private DiscoveryREST discoveryREST;
    @Autowired
    private EntityREST entityREST;
    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    EntityDiscoveryService entityDiscoveryService;

    @Cacheable(value = "databaseCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Database> getDatabasePageResult(Parameters parameters) throws AtlasBaseException {
        PageResult<Database> pageResult = new PageResult<>();
        List<Database> databases = new ArrayList<>();
        String s = parameters.getQuery() == null ? "" : parameters.getQuery();
        List<List<Object>> hiveDbs = discoveryREST.searchUsingDSL("name like '*" + s + "*' where __state = 'ACTIVE' select name,__guid orderby __timestamp", "hive_db", "", parameters.getLimit(), parameters.getOffset()).getAttributes().getValues();
        if (hiveDbs == null) {
            throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
        }
        for (List<Object> db : hiveDbs) {
            Database database = new Database();
            ArrayList<Table> tables = new ArrayList<>();
            String databaseGuid = db.get(1).toString();
            String databaseName = db.get(0).toString();
            AtlasEntity.AtlasEntityWithExtInfo databaseInfo = entityREST.getById(databaseGuid, true);
            Map<String, Object> databaseAttr = databaseInfo.getEntity().getAttributes();
            String databaseDescription = databaseAttr.get("description") == null ? "null" : databaseAttr.get("description").toString();
            database.setDatabaseId(databaseGuid);
            database.setDatabaseName(databaseName);
            database.setDatabaseDescription(databaseDescription);
            Map<String, Object> databaseRelationshipAttribute = databaseInfo.getEntity().getRelationshipAttributes();
            List<AtlasRelatedObjectId> databaseAtlasRelatedObjectIds = (List) databaseRelationshipAttribute.get("tables");
            for (AtlasRelatedObjectId databaseAtlasRelatedObjectId : databaseAtlasRelatedObjectIds) {
                Table table = new Table();
                table.setDatabaseId(databaseGuid);
                table.setDatabaseName(databaseName);
                table.setTableId(databaseAtlasRelatedObjectId.getGuid());
                table.setTableName(databaseAtlasRelatedObjectId.getDisplayText());
                AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(table.getTableId(), true);
                Map<String, Object> tableAttributes = tableInfo.getEntity().getAttributes();
                String tableDescription = tableAttributes.get("comment") == null ? "null" : tableAttributes.get("comment").toString();
                table.setDescription(tableDescription);
                tables.add(table);
            }
            database.setTableList(tables);
            databases.add(database);
        }
        AtlasSearchResult numResult = discoveryREST.searchUsingDSL("name like '*" + s + "*' where __state = 'ACTIVE' select count()", "hive_db", "", 1, 0);
        Object values = numResult.getAttributes().getValues().get(0).get(0);
        pageResult.setOffset(parameters.getOffset());
        pageResult.setCount(databases.size());
        pageResult.setLists(databases);
        pageResult.setSum(Integer.valueOf(values.toString()));
        return pageResult;
    }

    @Cacheable(value = "tablePageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Table> getTablePageResult(Parameters parameters) throws AtlasBaseException {
        PageResult<Table> pageResult = new PageResult<>();
        String s = parameters.getQuery() == null ? "" : parameters.getQuery();

        List<List<Object>> hiveTables = discoveryREST.searchUsingDSL("name like '*" + s + "*' where __state = 'ACTIVE' select name,__guid orderby __timestamp", "hive_table", "", parameters.getLimit(), parameters.getOffset()).getAttributes().getValues();
        if (hiveTables == null) {
            throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
        }
        List<Table> tables = new ArrayList<>();
        for (List<Object> nameId : hiveTables) {
            Table table = new Table();
            table.setTableId(nameId.get(1).toString());
            table.setTableName(nameId.get(0).toString());
//            AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(table.getTableId(), true);
//            AtlasEntity tableEntity = tableInfo.getEntity();
            List<String> attributes = new ArrayList<>();
            attributes.add("name");
            List<String> re = new ArrayList<>();
            re.add("db");
            AtlasEntity tableEntity = entitiesStore.getByIdWithAttributes(table.getTableId(), attributes, re).getEntity();
            Map<String, Object> tableAttributes = tableEntity.getAttributes();
            String tableDescription = tableAttributes.get("comment") == null ? "null" : tableAttributes.get("comment").toString();
            table.setDescription(tableDescription);
            Map<String, Object> relationshipAttributes = tableEntity.getRelationshipAttributes();
            AtlasRelatedObjectId db = (AtlasRelatedObjectId) relationshipAttributes.get("db");
            table.setDatabaseId(db.getGuid());
            table.setDatabaseName(db.getDisplayText());
            tables.add(table);
        }
        AtlasSearchResult numResult = discoveryREST.searchUsingDSL("name like '*" + s + "*' where __state = 'ACTIVE' select count()", "hive_table", "", 1, 0);
        Object values = numResult.getAttributes().getValues().get(0).get(0);
        pageResult.setOffset(parameters.getOffset());
        pageResult.setCount(tables.size());
        pageResult.setLists(tables);
        pageResult.setSum(Integer.valueOf(values.toString()));
        return pageResult;
    }

    @Cacheable(value = "columnPageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Column> getColumnPageResult(Parameters parameters) throws AtlasBaseException {
        PageResult<Column> pageResult = new PageResult<>();
        String s = parameters.getQuery() == null ? "" : parameters.getQuery();
        List<List<Object>> hiveColumns = discoveryREST.searchUsingDSL("name like '*" + s + "*' where __state = 'ACTIVE' select name,__guid orderby __timestamp", "hive_column", "", parameters.getLimit(), parameters.getOffset()).getAttributes().getValues();
        if (hiveColumns == null) {
            throw new AtlasBaseException(AtlasErrorCode.EMPTY_RESULTS, parameters.getQuery());
        }
        List<Column> columns = new ArrayList<>();
        for (List<Object> nameId : hiveColumns) {
            Column column = new Column();
            column.setColumnId(nameId.get(1).toString());
            column.setColumnName(nameId.get(0).toString());
            AtlasEntity.AtlasEntityWithExtInfo columnInfo = entityREST.getById(column.getColumnId(), true);
            AtlasEntity columnEntity = columnInfo.getEntity();
            Map<String, Object> relationshipAttributes = columnEntity.getRelationshipAttributes();
            AtlasRelatedObjectId table = (AtlasRelatedObjectId) relationshipAttributes.get("table");
            column.setTableId(table.getGuid());
            column.setTableName(table.getDisplayText());
            AtlasEntity.AtlasEntityWithExtInfo tableInfo = entityREST.getById(table.getGuid(), true);
            AtlasEntity tableEntity = tableInfo.getEntity();
            Map<String, Object> dbRelationshipAttributes = tableEntity.getRelationshipAttributes();
            AtlasRelatedObjectId db = (AtlasRelatedObjectId) dbRelationshipAttributes.get("db");
            column.setDatabaseId(db.getGuid());
            column.setDatabaseName(db.getDisplayText());
            columns.add(column);
        }
        AtlasSearchResult numResult = discoveryREST.searchUsingDSL("name like '*" + s + "*' where __state = 'ACTIVE' select count()", "hive_column", "", 1, 0);
        Object values = numResult.getAttributes().getValues().get(0).get(0);
        pageResult.setOffset(parameters.getOffset());
        pageResult.setCount(columns.size());
        pageResult.setLists(columns);
        pageResult.setSum(Integer.valueOf(values.toString()));
        return pageResult;
    }

    public TableShow getTableShow(GuidCount guidCount) throws AtlasBaseException, SQLException {
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
        ResultSet resultSet = HiveJdbcUtils.selectBySQL(sql, dbDisplayText);
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
                String s = resultSet.getObject(column)==null?"NULL":resultSet.getObject(column).toString();
                map.put(column, s);
            }
            resultList.add(map);
        }
        resultSet.close();
        tableShow.setTableId(guidCount.getGuid());
        tableShow.setColumnNames(columns);
        tableShow.setLines(resultList);
        resultSet.close();
        return tableShow;
    }

    public BuildTableSql getBuildTableSql(String tableId) throws AtlasBaseException, SQLException {
        BuildTableSql buildTableSql = new BuildTableSql();
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        List<String> relationshipAttributes = new ArrayList<>();
        relationshipAttributes.add("db");
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
        ResultSet resultSet = HiveJdbcUtils.selectBySQL(sql, dbDisplayText);
        StringBuffer stringBuffer = new StringBuffer();
        while (resultSet.next()) {
            Object object = resultSet.getObject(1);
            stringBuffer.append(object.toString());
        }
        buildTableSql.setSql(stringBuffer.toString());
        buildTableSql.setTableId(tableId);
        resultSet.close();
        return buildTableSql;
    }

}
