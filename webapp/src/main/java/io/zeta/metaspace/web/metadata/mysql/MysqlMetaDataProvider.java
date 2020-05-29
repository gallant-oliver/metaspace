package io.zeta.metaspace.web.metadata.mysql;

import io.zeta.metaspace.web.metadata.IMetaDataProvider;
import io.zeta.metaspace.web.metadata.AbstractMetaDataProvider;
import io.zeta.metaspace.web.metadata.RMDBEnum;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import schemacrawler.schema.*;
import schemacrawler.utility.SchemaCrawlerUtility;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static io.zeta.metaspace.web.metadata.BaseFields.*;

/**
 * mysql元数据获取
 * @author zhuxuetong
 * @date 2019-08-21 17:27
 */
@Singleton
@Component
public class MysqlMetaDataProvider extends AbstractMetaDataProvider implements IMetaDataProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MysqlMetaDataProvider.class);

    public MysqlMetaDataProvider() {
        super();
    }

    @Override
    protected String getRMDBType() {
        return RMDBEnum.MYSQL.getName();
    }


    @Override
    protected AtlasEntity.AtlasEntityWithExtInfo toTableEntity(AtlasEntity dbEntity, String instanceId, String databaseName, Table tableName, AtlasEntity.AtlasEntityWithExtInfo tableEntity,String instanceGuid) throws AtlasBaseException {
        tableEntity = super.toTableEntity(dbEntity,instanceId,databaseName,tableName,tableEntity,instanceGuid);
        AtlasEntity table = tableEntity.getEntity();
        List<AtlasEntity> columns = toColumns(tableName.getColumns(), tableEntity);
        List<AtlasEntity> indexes = toIndexes(tableName.getIndexes(), columns, tableEntity,databaseName,instanceGuid);
        List<AtlasEntity> foreignKeys = toForeignKeys(tableName.getForeignKeys(), columns, tableEntity,databaseName);
        setTableAttribute(columns,indexes,foreignKeys,tableEntity,table);
        return tableEntity;
    }


    protected Map<String, String> getTableCreateTime(String databaseName,String tableName) {
        Map<String, String> pair = new HashMap<>();
        final String                   query = String.format("select create_time from information_schema.tables where table_schema= '%s' and table_name = '%s'", databaseName,tableName);
        try (final Connection connection = getConnection();
             final Statement statement = connection.createStatement();
             final ResultSet results = statement.executeQuery(query)) {
            // Get result set metadata
            resultToMap(results,pair);
        } catch (Exception e) {
            LOG.info("获取mysql表创建时间错误", e);
            return new HashMap<>();
        }
        return pair;
    }

    @Override
    protected String getSkipSchemas() {
        return "sys";
    }

    @Override
    protected String getSkipTables() {
        return null;
    }

    @Override
    protected void jdbcConnectionProperties(Map<String,String> map){
        map.put("useInformationSchema","true");
    }
}
