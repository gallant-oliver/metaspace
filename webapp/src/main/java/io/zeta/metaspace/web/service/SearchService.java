package io.zeta.metaspace.web.service;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.annotation.AtlasService;
import org.apache.atlas.discovery.EntityDiscoveryService;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasRelatedObjectId;
import io.zeta.metaspace.discovery.MetaspaceGremlinQueryService;
import io.zeta.metaspace.model.result.BuildTableSql;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.result.TableShow;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.web.rest.EntityREST;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.metadata.GuidCount;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AtlasService
public class SearchService {
    @Autowired
    private EntityREST entityREST;
    @Autowired
    private AtlasEntityStore entitiesStore;
    @Autowired
    EntityDiscoveryService entityDiscoveryService;
    @Autowired
    MetaspaceGremlinQueryService metaspaceEntityService;

    //@Cacheable(value = "databaseCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Database> getDatabasePageResultV2(Parameters parameters) throws AtlasBaseException {
        int limit = parameters.getLimit();
        int offset = parameters.getOffset();
        String queryDb = parameters.getQuery();
        return metaspaceEntityService.getAllDBAndTable(queryDb, limit, offset);
    }

    @Cacheable(value = "tablePageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Table> getTablePageResultV2(Parameters parameters) throws AtlasBaseException {
        return metaspaceEntityService.getTableNameAndDbNameByQuery(parameters.getQuery(), parameters.getOffset(),parameters.getLimit());
    }


    @Cacheable(value = "columnPageCache", key = "#parameters.query + #parameters.limit + #parameters.offset")
    public PageResult<Column> getColumnPageResultV2(Parameters parameters) throws AtlasBaseException {
        return metaspaceEntityService.getColumnNameAndTableNameAndDbNameByQuery(parameters.getQuery(), parameters.getOffset(),parameters.getLimit());
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

    public BuildTableSql getBuildTableSql(String tableId) throws AtlasBaseException, SQLException, IOException {
        BuildTableSql buildTableSql = new BuildTableSql();
        List<String> attributes = new ArrayList<>();
        attributes.add("name");
        List<String> relationshipAttributes = new ArrayList<>();
        relationshipAttributes.add("db");
        if(Objects.isNull(tableId) || tableId.isEmpty()) {
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
