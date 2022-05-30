package io.zeta.metaspace.adapter.oscar;

import com.healthmarketscience.sqlbuilder.*;
import io.zeta.metaspace.adapter.AbstractAdapterExecutor;
import io.zeta.metaspace.adapter.AdapterSource;
import io.zeta.metaspace.adapter.AdapterTransformer;
import io.zeta.metaspace.model.TableSchema;
import io.zeta.metaspace.model.dataquality2.HiveNumericType;
import io.zeta.metaspace.model.metadata.MetaDataInfo;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerColumn;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerForeignKey;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerIndex;
import io.zeta.metaspace.model.schemacrawler.SchemaCrawlerTable;
import io.zeta.metaspace.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.IndexType;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.utility.SchemaCrawlerUtility;
import sf.util.Utility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class OscarAdapterExecutor extends AbstractAdapterExecutor {

    public OscarAdapterExecutor(AdapterSource adapterSource) {
        super(adapterSource);
    }

    @Override
    public LocalDateTime getTableCreateTime(String schemaName, String tableName) {
        String sql = "SELECT o.created AS create_time from all_objects o where o.object_name=? and o.owner=?";

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, tableName);
            statement.setString(2, schemaName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String time = resultSet.getString("create_time");
                return DateUtils.parseDateTime(time);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return null;
    }

    @Override
    public boolean isIgnoreColumn(String columnName) {
        return super.isIgnoreColumn(columnName) || AdapterTransformer.TEMP_COLUMN_RNUM.equalsIgnoreCase(columnName);
    }

    @Override
    public float getTableSize(String db, String tableName, String pool) {
        String querySQL = "select s.size*1024 as data_length from sys_class c, v_segment_info s, sys_tablespace ts, v_sys_user u where c.oid = s.relid and c.relname='%s' and s.fileid = ts.tsid and u.usesysid = c.relowner and u.usename='%s' and rownum = 1 order by s.size desc";
        db=db.replaceAll("'","''");
        tableName=tableName.replaceAll("'","''");
        querySQL=String.format(querySQL,tableName,db);
        Connection connection = getAdapterSource().getConnection();
        return queryResult(connection, querySQL, resultSet -> {
            try {
                float totalSize = 0;
                while (resultSet.next()) {
                    totalSize = resultSet.getLong("data_length");
                }
                return totalSize;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询表大小失败", e);
            }
        });
    }


    @Override
    public String getCreateTableSql(String schema, String table) {
        String tableName=table.replaceAll("\"","");
        String schemaName=schema.replaceAll("\"","");
        String querySql = "select sys_get_tabledef from v_sys_table where tableowner = '" + schemaName + "' and tablename = '" + tableName + "'";
        return queryResult(querySql, resultSet -> {
            try {
                String sql = null;
                if (resultSet.next()) {
                    sql = resultSet.getString(1);
                }
                return sql;
            } catch (SQLException e) {
                throw new AtlasBaseException("查询建表语句失败", e);
            }
        });
    }

    @Override
    public Map<String, String> getUserObject(String schemaName, List<String> tableNameList) {
        Map<String, String> result = new HashMap<>();
        if(CollectionUtils.isEmpty(tableNameList)){
            return result;
        }
        List<String> tableList = tableNameList.stream()
                .map(v->v.contains(".") ? v.substring(v.lastIndexOf(".")+1) : v).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder(" Select owner,object_name,object_type From all_objects Where object_name in ( ");
        int length = tableNameList.size();
        for(int i = 0;i < length;i++){
            if(i == length-1){
                sql.append("?");
            }else{
                sql.append("?,");
            }
        }
        sql.append(")");

        try (Connection connection = getAdapterSource().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql.toString());
            int index = 1;
            for (String tableName : tableList){
                statement.setString(index++, tableName.toUpperCase());
            }
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String owner = resultSet.getString("owner");
                String objectName = resultSet.getString("object_name");
                String objectType = resultSet.getString("object_type"); //TABLE VIEW

                result.put(objectName,objectType);
            }
        } catch (SQLException e) {
            throw new AtlasBaseException(e);
        }
        return result;
    }
}
