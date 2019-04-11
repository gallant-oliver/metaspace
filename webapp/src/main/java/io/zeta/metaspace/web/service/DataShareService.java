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
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.PageResult;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.DataType;
import io.zeta.metaspace.model.share.FilterColumn;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import javafx.concurrent.Task;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.xmlbeans.impl.jam.mutable.MPackage;
import org.json.JSONObject;
import org.omg.CORBA.OBJ_ADAPTER;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
    @Autowired
    private MetaDataService metaDataService;

    ExecutorService pool = Executors.newFixedThreadPool(100);

    public int insertAPIInfo(APIInfo info) throws AtlasBaseException {
        try {
            boolean same = querySameName(info.getName());
            if(same) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字的API");
            }
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
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "插入失败");
        }
    }

    /**
     * 查询同名
     * @param name
     * @return
     */
    public boolean querySameName(String name) {
        return shareDAO.querySameName(name)==0?false:true;
    }

    /**
     * 删除API
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public int deleteAPIInfo(String guid) throws AtlasBaseException {
        try {
            return shareDAO.deleteAPIInfo(guid);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "删除失败");
        }
    }

    /**
     * 更新API信息
     * @param guid
     * @param info
     * @return
     * @throws AtlasBaseException
     */
    public int updateAPIInfo(String guid, APIInfo info) throws AtlasBaseException {
        try {
            String apiName = info.getName();
            APIInfo currentAPI = shareDAO.getAPIInfoByGuid(guid);
            int count = shareDAO.querySameName(apiName);
            if(Objects.isNull(currentAPI)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未查询到当前API信息");
            }
            if(count > 0 && !currentAPI.getName().equals(apiName)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "已存在相同名字的API");
            }
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
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "修改失败");
        }
    }

    /**
     * API详情
     * @param guid
     * @return
     * @throws AtlasBaseException
     */
    public APIInfo getAPIInfo(String guid) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfoByGuid(guid);
            List<APIInfo.Field> fields = getQueryFileds(guid);
            List<String> dataOwner = getDataOwner(info.getTableGuid());
            info.setDataOwner(dataOwner);
            info.setFields(fields);
            return info;
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "获取信息失败");
        }
    }

    /**
     * 获取API列表
     * @param guid
     * @param my
     * @param publish
     * @return
     * @throws AtlasBaseException
     */
    public PageResult<APIInfoHeader> getAPIList(String guid, Integer my, String publish, Parameters parameters) throws AtlasBaseException {
        try {
            String user = AdminUtils.getUserData().getUsername();
            int limit = parameters.getLimit();
            int offset = parameters.getOffset();
            PageResult<APIInfoHeader> pageResult = new PageResult<>();
            List<APIInfoHeader> list = shareDAO.getAPIList(guid, my, publish, user, limit, offset);
            for(APIInfoHeader header : list) {
                List<String> dataOwner = getDataOwner(header.getTableGuid());
                header.setDataOwner(dataOwner);
            }
            int apiCount = shareDAO.getAPICount(guid, my, publish, user);
            pageResult.setSum(apiCount);
            pageResult.setCount(list.size());
            pageResult.setLists(list);
            return pageResult;
        } catch (Exception e) {
            LOG.error(e.getMessage());
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
            List<String> dataOwner = new ArrayList<>();
            if(Objects.nonNull(pGobject)) {
                String value = pGobject.getValue();
                List<Map> onwers = gson.fromJson(value, List.class);
                List<LinkedTreeMap> organization = dataManageService.getOrganization();
                for (LinkedTreeMap map : organization) {
                    String id = map.get("id").toString();
                    String type = map.get("type").toString();
                    for(Map owner : onwers) {
                        if(owner.get("id").toString().equals(id) && owner.get("type").toString().equals(type)) {
                            dataOwner.add(map.get("name").toString());
                        }
                    }
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
            Boolean star = (0==status)?false:true;
            return shareDAO.updateStarStatus(apiGuid, star);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新收藏状态失败");
        }
    }

    public int updatePublishStatus(List<String> apiGuid, Integer status) throws AtlasBaseException {
        try {
            Boolean publish = (0==status)?false:true;
            return shareDAO.updatePublishStatus(apiGuid, publish);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "更新发布状态失败");
        }
    }

    /**
     *
     * @param randomName
     * @param parameter
     * @return
     * @throws AtlasBaseException
     */
    public List<Map> testAPI(String randomName, QueryParameter parameter) throws AtlasBaseException {
        try {
            String tableGuid = parameter.getTableGuid();
            HashMap<String, List> columnMap = new HashMap<>();
            List<QueryParameter.Parameter> parameters = parameter.getParameter();
            parameters.stream().forEach(p -> columnMap.put(p.getColumnName(), p.getValue()));
            Map columnTypeMap = getColumnType(tableGuid, columnMap.keySet());
            String tableName = shareDAO.queryTableNameByGuid(tableGuid);
            String dbName = shareDAO.querydbNameByGuid(tableGuid);
            long limit = parameter.getLimit();
            long offset = parameter.getOffset();
            List<String> queryColumns = parameter.getQueryFields();

            String sql = getQuerySQL(tableName, columnTypeMap, parameters, queryColumns, limit, offset);

            APITask task = new APITask(randomName, sql, dbName);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    class APITask implements Callable<List<Map>> {
        private String name;
        private String sql;
        private String dbName;
        APITask(String name, String sql, String dbName) {
            this.name = name;
            this.sql = sql;
            this.dbName = dbName;
        }
        @Override
        public List<Map> call() throws Exception {
            try {
                if (Objects.nonNull(name)) {
                    Thread.currentThread().setName(name);
                }
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
            } catch (AtlasBaseException e) {
                LOG.error(e.getMessage());
                throw e;
            } catch (Exception e) {
                LOG.error(e.getMessage());
                throw e;
            }
        }
    }

    /**
     * 取消查询线程
     * @param name
     * @throws AtlasBaseException
     */
    public void cancelAPIThread(String name) throws AtlasBaseException {
        try {
            ThreadGroup currentGroup = Thread.currentThread().getThreadGroup();
            int countThreads = currentGroup.activeCount();
            Thread[] lstThreads = new Thread[countThreads];
            currentGroup.enumerate(lstThreads);
            for (int i = 0; i < countThreads; i++) {
                if (lstThreads[i].getName().equals(name)) {
                    lstThreads[i].interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "任务取消失败");
        }
    }

    /**
     * API请求数据
     * @param path
     * @param request
     * @return
     * @throws AtlasBaseException
     */
    public List<Map> queryAPIData(String path, HttpServletRequest request) throws AtlasBaseException {
        try {
            APIInfo info = shareDAO.getAPIInfo(path);
            if(Objects.isNull(info)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "路径错误，未查询到相关API信息");
            }
            String tableGuid = info.getTableGuid();
            Boolean publish = info.getPublish();
            if(Objects.isNull(publish)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API发布情况未知");
            }
            if (Objects.nonNull(publish) && !publish) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "当前API未发布");
            }
            //查询数据分享API字段详情
            Object fieldsObject = shareDAO.getAPIFields(path);
            PGobject pGobject = (PGobject) fieldsObject;
            String valueObject = pGobject.getValue();
            Gson gson = new Gson();
            List<Map> values = gson.fromJson(valueObject, List.class);
            Set<String> fields = new HashSet<>();
            List<FilterColumn> filterFileds = new ArrayList<>();
            for (Map map : values) {
                String columnName = map.get("columnName").toString();
                String defaultValue = map.get("defaultValue").toString();
                Boolean filter = Boolean.parseBoolean(map.get("filter").toString());
                Boolean fill = Boolean.parseBoolean(map.get("fill").toString());
                fields.add(columnName);
                if (filter) {
                    FilterColumn column = new FilterColumn(columnName, defaultValue, fill);
                    filterFileds.add(column);
                }
            }
            Map parameterMap = request.getParameterMap();
            //请求中查询字段
            Object columnObj = parameterMap.get("columns");
            String columnStr = "";
            if(columnObj instanceof String[]) {
                String[] str = (String[])columnObj;
                columnStr = str[0];
            }
            String[] columnArr = columnStr.substring(1, columnStr.length()-1).split(",");
            List<String> queryFiles = Arrays.asList(columnArr);
            //请求中过滤字段
            Object filterColumnObj = parameterMap.get("filters");
            String filterColumnStr = "";
            if(filterColumnObj instanceof String[]) {
                String[] str = (String[])filterColumnObj;
                filterColumnStr = str[0];
            }
            String[] filterColumnArr = filterColumnStr.substring(1, filterColumnStr.length()-1).split(";");
            Map filterColumnMap = new HashMap();
            for(String filterColumn : filterColumnArr) {
                String[] kvArr = filterColumn.split(":");
                String columnName = kvArr[0];
                List<String> valueList = new ArrayList<>();
                if(kvArr[1].contains("{")) {
                    String valueStr = kvArr[1];
                    String[] valueArr = valueStr.substring(1, valueStr.length()-1).split(",");
                    valueList = Arrays.asList(valueArr);
                } else {
                    valueList.add(kvArr[1]);
                }
                filterColumnMap.put(columnName, valueList);
            }
            //检查请求中查询字段是否包含于详情中字段
            checkFieldName(fields, queryFiles);
            //获取字段类型
            Map columnTypeMap = getColumnType(tableGuid, fields);
            //校验过滤参数列表
            List<QueryParameter.Parameter> kvList = checkFieldValue(filterFileds, filterColumnMap);
            //limit
            Object limitObj = parameterMap.get("limit");
            Long limit = null;
            if(Objects.nonNull(limitObj) && limitObj instanceof String[]) {
                String[] str = (String[])limitObj;
                limit = Long.parseLong(str[0]);
            }
            //offset
            Object offsetObj = parameterMap.get("offset");
            long offset = 0;
            if(Objects.nonNull(offsetObj) && offsetObj instanceof String[]) {
                String[] str = (String[])offsetObj;
                offset = Long.parseLong(str[0]);
            }
            //对比limit和maxRowNumber
            long maxRowNumber = info.getMaxRowNumber();
            if(Objects.nonNull(limit)) {
                limit = Math.min(limit, maxRowNumber);
            } else {
                limit = maxRowNumber;
            }
            String tableName = shareDAO.queryTableNameByGuid(tableGuid);
            String dbName = shareDAO.querydbNameByGuid(tableGuid);
            //sql
            String sql = getQuerySQL(tableName, columnTypeMap, kvList, queryFiles, limit, offset);
            //任务
            String randomName = String.valueOf(System.currentTimeMillis());
            APITask task = new APITask(randomName, sql, dbName);
            Future<List<Map>> futureResult = pool.submit(task);
            List<Map> result = futureResult.get();
            return result;
        } catch (ExecutionException e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST , e.getCause().getMessage());
        } catch (AtlasBaseException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST , "查询失败");
        }
    }

    /**
     * 查询字段是否在允许范围内
     * @param fields
     * @param queryFields
     * @throws AtlasBaseException
     */
    public void checkFieldName(Set<String> fields, List<String> queryFields) throws AtlasBaseException {
        for(String field : queryFields) {
            if(!fields.contains(field)) {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "无效的过滤查询字段:" + field);
            }
        }
    }

    /**
     * 校验查询字段取值
     * @param filterFileds
     * @param filterColumnMap
     * @return
     * @throws AtlasBaseException
     */
    public List<QueryParameter.Parameter> checkFieldValue(List<FilterColumn> filterFileds, Map filterColumnMap) throws AtlasBaseException {
        List<QueryParameter.Parameter> kvList = new ArrayList<>();
        for(FilterColumn field : filterFileds) {
            //必传值
            if(field.getFill()) {
                //未传值
                if(!filterColumnMap.containsKey(field.getColumnName())) {
                    throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "请求错误，未填必传值字段:" + field.getColumnName());
                } else {
                    QueryParameter.Parameter parameter = new QueryParameter.Parameter();
                    parameter.setColumnName(field.getColumnName());
                    String key = field.getColumnName();
                    Object value = filterColumnMap.get(key);
                    if(value instanceof String[]) {
                        String[] values = (String[])value;
                        parameter.setValue(Arrays.asList(values));
                    } else if(value instanceof List) {
                        parameter.setValue((List<Object>) value);
                    }
                    kvList.add(parameter);
                }
                //非必传
            } else {
                QueryParameter.Parameter parameter = new QueryParameter.Parameter();
                parameter.setColumnName(field.getColumnName());
                //已传值
                if(filterColumnMap.containsKey(field.getColumnName())) {
                    String key = field.getColumnName();
                    Object value = filterColumnMap.get(key);
                    if(value instanceof String[]) {
                        String[] values = (String[])value;
                        parameter.setValue(Arrays.asList(values));
                    } else if(value instanceof List) {
                        parameter.setValue((List<Object>) value);
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

    public Map<String, String> getColumnType(String tableGuid, Set<String> columSet) throws AtlasBaseException {
        Map<String, String> columnTypeMap = new HashMap();
        List<Column> columnList = metaDataService.getTableInfoById(tableGuid).getColumns();
        Map<String, String> columnMap = new HashMap<>();
        columnList.forEach(column -> columnMap.put(column.getColumnName(), column.getType()));
        for (String columnName : columSet) {
            if (columnMap.containsKey(columnName)) {
                String type = columnMap.get(columnName);
                columnTypeMap.put(columnName, type);
            } else {
                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "未知的查询字段");
            }
        }
        return columnTypeMap;
    }

    /**
     * 拼接查询SQL
     * @param tableName
     * @param columnTypeMap
     * @param kvList
     * @param limit
     * @param offset
     * @return
     */
    public String getQuerySQL(String tableName, Map<String, String> columnTypeMap, List<QueryParameter.Parameter> kvList, List<String> queryColumns, Long limit, Long offset) throws AtlasBaseException {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("select ");
            StringJoiner columnJoiner = new StringJoiner(",");
            queryColumns.stream().forEach(column -> columnJoiner.add(column));
            sb.append(columnJoiner.toString());
            sb.append(" from ");
            sb.append(tableName);
            //过滤条件
            if (Objects.nonNull(kvList) && kvList.size() > 0) {
                sb.append(" where ");
                StringJoiner filterJoiner = new StringJoiner(" and ");
                for (QueryParameter.Parameter kv : kvList) {
                    StringBuffer valueBuffer = new StringBuffer();
                    StringJoiner valueJoiner = new StringJoiner(",");
                    String type = columnTypeMap.get(kv.getColumnName());
                    DataType dataType = DataType.parseOf(type);

                    List<Object> valueList = kv.getValue();
                    //验证设置取值是否正确
                    valueList.stream().forEach(value -> dataType.valueOf(value).get());
                    if(DataType.BOOLEAN == dataType) {
                        for(Object value : valueList) {
                            if(!value.equals("true") && !value.equals("false") && !value.equals("0") && !value.equals("1")) {
                                throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, kv.getColumnName() + "取值需为bool类型");
                            }
                        }
                    }
                    if (DataType.STRING == dataType) {
                        kv.getValue().forEach(value -> valueJoiner.add("\"" + value.toString() + "\""));
                    } else {
                        kv.getValue().forEach(value -> valueJoiner.add(value.toString()));
                    }
                    if (valueList.size() > 1) {
                        valueBuffer.append(kv.getColumnName()).append(" in ").append("(").append(valueJoiner.toString()).append(")");
                    } else {
                        valueBuffer.append(kv.getColumnName()).append("=").append(valueJoiner.toString());
                    }
                    filterJoiner.add(valueBuffer.toString());
                }
                sb.append(filterJoiner.toString());
            }
            //limit
            if (Objects.nonNull(limit) && -1 != limit) {
                sb.append(" limit ");
                sb.append(limit);
            }
            //offset
            sb.append(" offset ");
            sb.append(offset);
            LOG.info("SQL：" + sb.toString());
            return sb.toString();
        } catch (NumberFormatException e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "数据类型错误");
        } catch (AtlasBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "SQL查询语句异常");
        }
    }
}
