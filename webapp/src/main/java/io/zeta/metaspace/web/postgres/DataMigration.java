package io.zeta.metaspace.web.postgres;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 数据血缘 tableInfo -> source_db\db_info 旧数据迁移处理
 * SELECT pg_reload_conf()  pg_hba.conf
 */
@Slf4j
public class DataMigration {
    private static int pageSize = 10;
    private static final String baseUrl = "http://localhost:21000/";

    private static Connection getConnection(Boolean autoCommit,String url,String username,String password) throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(url,username, password);
        c.setAutoCommit(autoCommit);
        return c;
    }

    /**
     *  tableinfo 数据迁移source_db
     * @param url eg:"jdbc:postgresql://localhost:5432/metaspace_dev",     *
     * @param username "metaspace",
     * @param password  "metaspace"
     */
    public static void processSourceDb(String url,String username,String password) {
        Connection c = null;
        try {
            c = getConnection(false,url, username, password);
            log.info("Opened database successfully");

            String insertSourceDb = "INSERT INTO public.source_db(\r\n" +
                    "	id, source_id, db_guid)\r\n" +
                    "	VALUES (?, ?, ?)";
            PreparedStatement insertStmt = c.prepareStatement(insertSourceDb);

            //组装source_db 查询的sql所需字段
            String sql =  "SELECT distinct source_id, databaseguid FROM public.tableinfo order by source_id " +
                    "limit "+pageSize+" offset ? ;";
            PreparedStatement selectStmt = c.prepareStatement(sql);
            int start = 0;
            ResultSet rs = null;
            while(true) {
                selectStmt.setInt(1, start);
                rs = selectStmt.executeQuery();

                boolean hasMore = rs.next();
                if(!hasMore) {
                    break;
                }

                while (hasMore) {
                    String  sourceId = rs.getString("source_id");
                    String  dbGuid = rs.getString("databaseguid");

                    if(StringUtils.isEmpty(sourceId)) {//数据库该字段设置为not null
                        log.error("source_id 为空，databaseguid ={}",dbGuid);
                        hasMore = rs.next();
                        continue;
                    }
                    String uid = UUID.randomUUID().toString();
                    insertStmt.setString(1, uid);
                    insertStmt.setString(2, sourceId);
                    insertStmt.setString(3, dbGuid);

                    insertStmt.addBatch();
                    hasMore = rs.next();
                }

                start = start + pageSize;
                log.info("execute batch ...");
                insertStmt.executeBatch();
            }

            c.commit();
            if(rs != null) rs.close();
            insertStmt.close();
            selectStmt.close();
            if(c != null) c.close();
        }catch ( Exception e ) {
            log.error( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        log.info("Operation done successfully");
    }

    public static void processDbInfo(String url,String username,String password) {
        Connection c = null;
        List<String> databaseGuidList = queryDataBaseGuid(url, username, password);
        if(CollectionUtils.isEmpty(databaseGuidList)){
            log.info("没有要处理的db_guid信息");
            return ;
        }
        try {
            c = getConnection(false,url, username, password);
            log.info("Opened database successfully");
            String insertDbInfo = "INSERT INTO public.db_info(\n" +
                    "\tdatabase_guid, database_name, owner, db_type, is_deleted, status, database_description, instance_guid)\n" +
                    "\tVALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement insertStmt = c.prepareStatement(insertDbInfo);

            for (String guid : databaseGuidList){
                Map<String,Object> resultMap = queryDbInfoByGuid(guid);
                if(resultMap == null || resultMap.isEmpty()){
                    log.error("该 guid： {} 没查找到相关信息",guid);
                    return ;
                }

                String instanceGuid = Objects.toString(resultMap.get("instance_guid"),"");
                insertStmt.setString(1, Objects.toString(resultMap.get("database_guid"),""));
                insertStmt.setString(2, Objects.toString(resultMap.get("database_name"),""));
                insertStmt.setString(3, Objects.toString(resultMap.get("owner"),""));
                insertStmt.setString(4, "");
                insertStmt.setBoolean(5, (Boolean)resultMap.get("is_deleted"));
                insertStmt.setString(6, Objects.toString(resultMap.get("status"),""));
                insertStmt.setString(7, Objects.toString(resultMap.get("database_description"),""));
                insertStmt.setString(8, instanceGuid);

                if(!StringUtils.isEmpty(instanceGuid)){
                    Map<String,Object> instanceMap = queryDbInfoByGuid(instanceGuid);
                    insertStmt.setString(4, Objects.toString(resultMap.get("db_type"),""));
                }
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            c.commit();
            insertStmt.close();
            if(c!= null) c.close();
            log.info("操作执行成功.");
        } catch (Exception e) {
            log.error("操作 db_info 出错了，{}",e);
        }
    }
    /*
     从source_db 获取databaseGuid
     */
    private static List<String> queryDataBaseGuid(String url,String username,String password){
        List<String> resultList = new ArrayList<>();
        Connection c = null;
        try {
            c = getConnection(false, url, username, password);
            log.info("Opened database successfully");
            String sql =  "SELECT db_guid FROM public.source_db";
            PreparedStatement selectStmt = c.prepareStatement(sql);
            ResultSet rs = selectStmt.executeQuery();
            while(rs.next()){
                resultList.add(rs.getString("db_guid"));
            }
            if(rs != null) rs.close();
            selectStmt.close();
            if(c != null) c.close();
        }catch (Exception e){
            log.error("获取db_guid出错，{}",e);
        }
         return resultList;
    }
    /**
     * 根据 db guid 查询图数据库获取相关信息
     * @param guid dbguid、instanceGuid 只获取db_type
     * @return
     */
    private static Map<String,Object> queryDbInfoByGuid(String guid){
        Map<String,Object> resultMap = new HashMap<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //url 参数
       // String guid = "98001962-6ae5-41c3-bedc-6fa86be85b8e"; //"844a5ad2-029d-441f-a35a-07cc47feb467";
        String url = baseUrl+"api/metaspace/v2/entity/guid/"+guid ;

        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("X-SSO-FullticketId", "TGT-13017-a9UBvf3orbhzxRZLTnWgV59MJLbFyuvxKy6DEee5TmP29lXRd2-x-sso");
        httpGet.addHeader("tenantId", "ccb9b4fd05894248a57b4cf80dd28731");
        //设置超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(30000)
                .setSocketTimeout(30000).build();
        httpGet.setConfig(requestConfig);

        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(httpGet);
            String result = null;
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                result = EntityUtils.toString(entity);
            }
            release(response, httpClient);

            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<String, Object>();
            map = gson.fromJson(result, map.getClass());

            Map<String, Object> entityMap = new HashMap<String, Object>();
            entityMap = gson.fromJson(gson.toJson(map.get("entity")),entityMap.getClass() );
            String typeName = entityMap.get("typeName").toString();

            Map<String, Object> attributesMap = new HashMap<String, Object>();
            attributesMap = gson.fromJson(gson.toJson(entityMap.get("attributes")),attributesMap.getClass() );
           // List<Object> tableList = (List<Object>) attributesMap.get("tables");
            String instanceGuid = "";
            if("rdbms_db".equalsIgnoreCase(typeName)) {
                Map<String, Object> instanceMap = new HashMap<String, Object>();
                instanceMap = gson.fromJson(gson.toJson(attributesMap.get("instance")),instanceMap.getClass() );
                instanceGuid = instanceMap.get("guid").toString();
                log.info("rdbms_db instanceGuid={}",instanceGuid);

                String status = entityMap.get("status")+"";
                resultMap.put("database_id",entityMap.get("guid"));
                resultMap.put("database_name",entityMap.get("name"));
                resultMap.put("owner",entityMap.get("owner"));
                resultMap.put("is_deleted",StringUtils.endsWithIgnoreCase("DELETED",status));
                resultMap.put("status",status);
                // resultMap.put("table_count",tableList != null ? tableList.size() : 0);
                resultMap.put("database_description",entityMap.get("description"));
                resultMap.put("instance_id",instanceGuid);
                return resultMap;
            }
            if("rdbms_instance".equalsIgnoreCase(typeName)){
                resultMap.put("db_type",attributesMap.get("rdbms_type"));
                return resultMap;
            }
        } catch (IOException e) {
            log.error("操作出错了：{}",e);
        }

        return resultMap;
    }

    private static void release(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient) throws IOException {
        // 释放资源
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    public static void main(String[] args) {
        processSourceDb("jdbc:postgresql://localhost:5432/metaspace_dev",
                "metaspace", "metaspace");
    }
}
