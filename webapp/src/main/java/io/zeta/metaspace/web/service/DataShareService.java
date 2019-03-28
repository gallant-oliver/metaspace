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
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.web.dao.DataShareDAO;
import io.zeta.metaspace.web.util.AdminUtils;
import io.zeta.metaspace.web.util.HiveJdbcUtils;
import javafx.concurrent.Task;
import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

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
            SQLTask task = new SQLTask(parameter);
            FutureTask<List<Map>> futureTask = new FutureTask<>(task);
            List<Map> result = futureTask.get();

            pool.submit(futureTask);
            return result;
        } catch (Exception e) {
            throw new AtlasBaseException(AtlasErrorCode.BAD_REQUEST, "API接口查询失败");
        }
    }

    class SQLTask implements Callable<List<Map>> {
        private QueryParameter parameter;
        SQLTask(QueryParameter parameter) {
            this.parameter = parameter;
        }
        @Override
        public List<Map> call() throws Exception {
            String tableGuid = parameter.getTableGuid();
            long limit = parameter.getLimit();
            long offset = parameter.getOffset();
            String tableName = shareDAO.queryTableNameByGuid(tableGuid);
            String dbName = shareDAO.querydbNameByGuid(tableGuid);
            List<QueryParameter.Parameter> parameters = parameter.getParameter();
            List<String> columns = new ArrayList<>();
            parameters.stream().forEach(param -> columns.add(param.getColumnName()));
            String sql = getQuertSQL(tableName, columns, parameters, limit, offset);
            ResultSet resultSet = HiveJdbcUtils.selectBySQLWithSystemCon(sql, dbName);
            Map map = new HashMap();
            List<Map> result = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            if(resultSet.next()) {
                for(int i=1; i<=columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String value = resultSet.getString(columnName);
                    map.put(columnName, value);
                }
                result.add(map);
            }
            return result;
        }
    }


    public String getQuertSQL(String tableName, List<String> columns, List<QueryParameter.Parameter> kvList, long limit, long offset) {
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
            kvList.stream().forEach(kv -> filterJoiner.add(kv.getColumnName() + "=" + kv.getValue()));
            sb.append(filterJoiner.toString());
        }

        if(-1 != limit) {
            sb.append(" limit ");
            sb.append(limit);
        }
        sb.append(" offset ");
        sb.append(offset);
        return sb.toString();
    }
}
