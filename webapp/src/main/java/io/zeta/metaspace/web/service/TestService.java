// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.service;

import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.ApiInfoV2;
import io.zeta.metaspace.model.share.DataType;
import io.zeta.metaspace.model.share.HiveQueryParameter;
import io.zeta.metaspace.model.share.OracleQueryParameter;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import io.zeta.metaspace.web.util.ImpalaJdbcUtils;
import io.zeta.metaspace.web.util.OracleJdbcUtils;
import io.zeta.metaspace.web.util.QualityEngine;
import oracle.jdbc.OracleBfile;
import oracle.sql.Datum;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author lixiang03
 * @Data 2020/6/8 16:19
 */
public class TestService {
    DataShareDAO shareDAO;
    private static String engine;
    private DataSourceService dataSourceService;
    private static final Logger LOG = LoggerFactory.getLogger(TestService.class);;
    Map<String, CompletableFuture> taskMap = new HashMap<>();

    public Map getOracleQueryResult(String sourceId, String querySql, String countSql) throws AtlasBaseException {
        Map resultMap = new HashMap();
        try {
            Connection conn = dataSourceService.getConnection(sourceId);
            ResultSet resultSet = OracleJdbcUtils.query(conn, querySql);
            List<LinkedHashMap> result = extractResultSetData(resultSet, conn);
            resultMap.put("queryResult", result);
            if(countSql != null) {
                ResultSet countSet = OracleJdbcUtils.query(conn, countSql);
                while(countSet.next()) {
                    Long total = countSet.getLong(1);
                    resultMap.put("queryCount", total);
                }
            }
            return resultMap;
        } catch (Exception e) {
            LOG.error("查询失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }

    public Map getHiveQueryResult(String dbName, String querySql, Boolean test,String pool) throws AtlasBaseException {
        Map resultMap = new HashMap();
        Connection conn = null;
        long count = 0;
        try {
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                conn = ImpalaJdbcUtils.getSystemConnection(dbName, pool);
            } else {
                conn = HiveJdbcUtils.getSystemConnection(dbName,pool);
            }
            ResultSet resultSet = null;
            if(Objects.nonNull(engine) && QualityEngine.IMPALA.getEngine().equals(engine)) {
                resultSet = ImpalaJdbcUtils.selectBySQLWithSystemCon(conn, querySql);
            } else {
                resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(conn, querySql);
            }
            List<LinkedHashMap> result = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                LinkedHashMap map = new LinkedHashMap();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(columnName);
                    if(i != columnCount) {
                        if(test) {
                            map.put(columnName, String.valueOf(value));
                        } else {
                            map.put(columnName, value);
                        }
                    } else if(!test && i==columnCount) {
                        if(Objects.nonNull(value)) {
                            count = Long.parseLong(value.toString());
                        } else {
                            count = (long)map.size();
                        }
                        continue;
                    }
                }
                result.add(map);
            }
            resultMap.put("queryResult", result);
            resultMap.put("queryCount", count);
            return resultMap;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "查询失败");
        }
    }
    public List<LinkedHashMap> extractResultSetData(ResultSet resultSet, Connection connection) throws AtlasBaseException {
        List<LinkedHashMap> result = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                LinkedHashMap map = new LinkedHashMap();
                for (int i = 2; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = resultSet.getObject(columnName);
                    if (value instanceof Clob) {
                        try {
                            Clob clob = (Clob) value;
                            StringBuffer buffer = new StringBuffer();
                            clob.getCharacterStream();
                            BufferedReader br = new BufferedReader(clob.getCharacterStream());
                            clob.getCharacterStream();
                            String line = br.readLine();
                            while (line != null) {
                                buffer.append(line);
                                line = br.readLine();
                            }
                            value = buffer.toString();
                        } catch (Exception e) {
                            LOG.error("处理查询结果失败", e);
                        }
                    } else if(value instanceof Timestamp) {
                        Timestamp timValue = (Timestamp)value;
                        value = timValue.toString();
                    } else if(value instanceof Blob || value instanceof OracleBfile) {
                        value = "不支持展示的数据类型";
                    } else if(value instanceof Datum) {
                        value = ((Datum)value).stringValue(connection);
                    }
                    map.put(columnName, value);
                }
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            LOG.error("解析查询结果失败", e);
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "解析查询结果失败");
        }
    }
}
