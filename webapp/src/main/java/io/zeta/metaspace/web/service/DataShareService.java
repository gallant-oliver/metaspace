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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/26 19:56
 */
package io.zeta.metaspace.web.service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.FilterColumn;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import javafx.concurrent.Task;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.xmlbeans.impl.jam.mutable.MPackage;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 19:56
 */
@Service
public class DataShareService {

    private static final Logger LOG = LoggerFactory.getLogger(DataShareService.class);

    @Autowired
    DataShareDAO shareDAO;
    @Autowired
    DataManageService dataManageService;

    ExecutorService pool = Executors.newFixedThreadPool(100);

    public int insertAPIInfo(APIInfo info) throws AtlasBaseException {
        try {
            String guid = UUID.randomUUID().toString();
            //guid
            info.setGuid(guid);
            String user = AdminUtils.getUserData().getUsername();
            //keeper
            info.setKeeper(user);
            //updater
            info.setUpdater(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long currentTime = System.currentTimeMillis();
            String currentTimeFormat = sdf.format(currentTime);
            //generateTime
            info.setGenerateTime(currentTimeFormat);
            //updateTime
            info.setUpdateTime(currentTimeFormat);
            //publish
            info.setPublish(false);
            //star
            info.setStar(false);
            return shareDAO.insertAPIInfo(info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "插入失败");
        }
    }

    public boolean querySameName(String name) {
        return shareDAO.querySameName(name)==0?false:true;
    }

    public int deleteAPIInfo(String guid) throws AtlasBaseException {
        try {
            return shareDAO.deleteAPIInfo(guid);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    public int updateAPIInfo(String guid, APIInfo info) throws AtlasBaseException {
        try {
            info.setGuid(guid);
            String user = AdminUtils.getUserData().getUsername();
            //updater
            info.setUpdater(user);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long currentTime = System.currentTimeMillis();
            String currentTimeFormat = sdf.format(currentTime);
            //updateTime
            info.setUpdateTime(currentTimeFormat);
            return shareDAO.updateAPIInfo(info);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改失败");
        }
    }

    public APIInfo getAPIInfo(String guid) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfoByGuid(guid);
            List<APIInfo.Field> fields = getQueryFileds(guid);
            List<String> dataOwner = getDataOwner(info.getTableGuid());
            info.setDataOwner(dataOwner);
            info.setFields(fields);
            return info;
        } catch (Exception e ) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取信息失败");
        }
    }

    public List<APIInfoHeader> getAPIList(String guid, Integer my, Integer publish) throws AtlasBaseException {
        try {
            String user = AdminUtils.getUserData().getUsername();
            List<APIInfoHeader> list = shareDAO.getAPIList(guid, my, publish, user);
            for(APIInfoHeader header : list) {
                List<String> dataOwner = getDataOwner(header.getTableGuid());
                header.setDataOwner(dataOwner);
            }
            return list;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取失败");
        }
    }

    public List<APIInfo.Field> getQueryFileds(String guid) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object fields = shareDAO.getQueryFiledsByGuid(guid);
            PGobject pGobject = (PGobject)fields;
            String value = pGobject.getValue();
            List<APIInfo.Field> values = gson.fromJson(value, List.class);
            return values;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }

    public List<String> getDataOwner(String guid) throws AtlasBaseException {
        try {
            Gson gson = new Gson();
            Object dataOwnerObject = shareDAO.getDataOwnerByGuid(guid);
            PGobject pGobject = (PGobject)dataOwnerObject;
            String value = pGobject.getValue();
            List<String> values = gson.fromJson(value, List.class);
            List<LinkedTreeMap> organization = dataManageService.getOrganization();
            List<String> dataOwner = new ArrayList<>();
            for(LinkedTreeMap map : organization) {
                String id = map.get("id").toString();
                if(values.contains(id)) {
                    dataOwner.add(map.get("name").toString());
                }
            }
            return dataOwner;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取数据失败");
        }
    }


    public int updateStarStatus(String apiGuid, Integer status) throws AtlasBaseException {
        try {
            return shareDAO.updateStarStatus(apiGuid, status);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int updatePublishStatus(List<String> apiGuid, Integer status) throws AtlasBaseException {
        try {
            return shareDAO.updatePublishStatus(apiGuid, status);
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    public List<Map> testAPI(String randomName, QueryParameter parameter) throws AtlasBaseException,Exception {
        try {
            SQLTask task = new SQLTask(randomName, parameter);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    class SQLTask implements Callable<List<Map>> {
        private QueryParameter parameter;
        private String name;
        SQLTask(String name, QueryParameter parameter) {
            this.name = name;
            this.parameter = parameter;
        }
        SQLTask(QueryParameter parameter) {
            this.parameter = parameter;
        }
        @Override
        public List<Map> call() throws Exception {
            try {
                if (Objects.nonNull(name)) {
                    Thread.currentThread().setName(name);
                }
                String tableGuid = parameter.getTableGuid();
                Long limit = parameter.getLimit();
                Long offset = parameter.getOffset();
                String tableName = shareDAO.queryTableNameByGuid(tableGuid);
                String dbName = shareDAO.querydbNameByGuid(tableGuid);
                List<QueryParameter.Parameter> parameters = parameter.getParameter();
                List<String> columns = parameter.getQueryFields();
                String sql = getQuertSQL(tableName, columns, parameters, limit, offset);
                ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);
                Map map = new HashMap();
                List<Map> result = new ArrayList<>();
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                if (resultSet.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        String value = resultSet.getString(columnName);
                        map.put(columnName, value);
                    }
                    result.add(map);
                }
                return result;
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public void cancelSQLThread(String name) {
        ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
        int countThreads = currentGroup.activeCount();
        Thread[] lstThreads = new Thread[countThreads];
        currentGroup.enumerate(lstThreads);
        for(int i=0; i<countThreads; i++) {
            if(lstThreads[i].getName().equals(name)) {
                lstThreads[i].interrupt();
                break;
            }
        }
    }


    public List<Map> queryAPIData(String path, HttpServletRequest request) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfo(path);
            String tableGuid = info.getTableGuid();
            String tableName = info.getTableName();
            Boolean publish = info.getPublish();
            if (Objects.nonNull(publish) && !publish) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API未发布");
            }
            Object fieldsObject = shareDAO.getAPIFields(path);
            PGobject pGobject = (PGobject) fieldsObject;
            String valueObject = pGobject.getValue();
            Gson gson = new Gson();
            List<Map> values = gson.fromJson(valueObject, List.class);
            List<String> queryFields = new ArrayList<>();
            List<FilterColumn> filterFileds = new ArrayList<>();

            for (Map map : values) {
                String columnName = map.get("columnName").toString();
                String defaultValue = map.get("defaultValue").toString();
                Boolean filter = Boolean.parseBoolean(map.get("filter").toString());
                Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                queryFields.add(columnName);
                if (filter) {
                    FilterColumn column = new FilterColumn(columnName, defaultValue, fill);
                    filterFileds.add(column);
                }
            }

            Map parameterMap = request.getParameterMap();
            checkFieldName(filterFileds, parameterMap);
            //过滤参数列表
            List<QueryParameter.Parameter> kvList = checkFiedValue(filterFileds, parameterMap);
            //limit
            Object limitObj = parameterMap.get("limit");
            Long limit = null;
            if(Objects.nonNull(limitObj))
                limit = Long.parseLong(limitObj.toString());
            //offset
            Object offsetObj = parameterMap.get("offset");
            long offset = 0;
            if(Objects.nonNull(offsetObj))
                offset = Long.parseLong(offsetObj.toString());

            String sql = getQuertSQL(tableName, queryFields, kvList , limit, offset);


            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setTableGuid(tableGuid);
            queryParameter.setMaxRowNumber(info.getMaxRowNumber());
            if(Objects.nonNull(limit)) {
                queryParameter.setLimit(limit);
            }
            queryParameter.setOffset(offset);
            queryParameter.setQueryFields(queryFields);
            queryParameter.setParameter(kvList);
            String randomName = String.valueOf(System.currentTimeMillis());
            SQLTask task = new SQLTask(randomName, queryParameter);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST , "查询失败");
        }
    }

    public void checkFieldName(List<FilterColumn> filterFileds, Map<String, String> parameterMap) throws AtlasBaseException {
        for(String key : parameterMap.keySet()) {
            //过滤字段列表中是否包含请求字段名
            if(filterFileds.stream().filter(filed -> filed.getColumnName().equals(key)).count() == 0) {
                if(!"limit".equals(key) && !"offset".equals("key")) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的查询字段:" + key);
                }
            }
        }
    }

    public List<QueryParameter.Parameter> checkFiedValue(List<FilterColumn> filterFileds, Map parameterMap) throws AtlasBaseException {
        List<QueryParameter.Parameter> kvList = new ArrayList<>();
        for(FilterColumn field : filterFileds) {
            //必传值
            if(field.getFill()) {
                //未传值
                if(!parameterMap.containsKey(field.getColumnName())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未填必传");
                } else {
                    QueryParameter.Parameter parameter = new QueryParameter.Parameter();
                    parameter.setColumnName(field.getColumnName());
                    String key = field.getColumnName();
                    Object value = parameterMap.get(key);
                    if(value instanceof String[]) {
                        String[] values = (String[])value;
                        parameter.setValue(Arrays.asList(values));
                    }
                    kvList.add(parameter);
                }
                //非必传
            } else {
                QueryParameter.Parameter parameter = new QueryParameter.Parameter();
                parameter.setColumnName(field.getColumnName());
                //已传值
                if(parameterMap.containsKey(field.getColumnName())) {
                    String key = field.getColumnName();
                    Object value = parameterMap.get(key);
                    if(value instanceof String[]) {
                        String[] values = (String[])value;
                        parameter.setValue(Arrays.asList(values));
                    }
                //默认值
                } else {
                    parameter.setValue(Arrays.asList(field.getValue()));
                }
                kvList.add(parameter);
            }
        }
        return kvList;
    }


    public String getQuertSQL(String tableName, List<String> columns, List<QueryParameter.Parameter> kvList, Long limit, Long offset) {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        StringJoiner columnJoiner = new StringJoiner(",");
        columns.stream().forEach(column -> columnJoiner.add(column));
        sb.append(columnJoiner.toString());
        sb.append(" from ");
        sb.append(tableName);
        if(Objects.nonNull(kvList) && kvList.size()>0) {
            sb.append(" where ");
            StringJoiner filterJoiner = new StringJoiner(" and ");
            kvList.stream().forEach((kv) -> {
                List<String> valueList = kv.getValue();
                StringBuilder valueBuild = new StringBuilder();
                valueBuild.append("(");
                StringJoiner valueJoiner = new StringJoiner(",");
                valueList.forEach(value -> valueJoiner.add(value));
                valueBuild.append(valueJoiner.toString());
                valueBuild.append(")");
                filterJoiner.add(kv.getColumnName() + " in " + valueBuild.toString());
            });
            sb.append(filterJoiner.toString());
        }

        if(Objects.nonNull(limit) && -1!=limit) {
            sb.append(" limit ");
            sb.append(limit);
        }
        sb.append(" offset ");
        sb.append(offset);
        return sb.toString();
    }
}
