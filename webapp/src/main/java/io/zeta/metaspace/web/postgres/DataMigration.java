package io.zeta.metaspace.web.postgres;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.atlas.ApplicationProperties;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.commons.configuration.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
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
@Component
public class DataMigration {
    private static final int pageSize = 10;
    private final AtlasEntityStore entitiesStore;
    private static Configuration conf;
    private static String url;
    private static String username;
    private static String password;
    static {
        try {
            conf = ApplicationProperties.get();
            url = conf.getString("metaspace.database.url");
            username = conf.getString("metaspace.database.username");
            password  =conf.getString("metaspace.database.password");
            log.info("jdbcurl:{}",url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Inject
    public DataMigration(AtlasEntityStore entitiesStore) {
        this.entitiesStore = entitiesStore;
    }

    private  Connection getConnection(Boolean autoCommit) throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection(url,username, password);
        c.setAutoCommit(autoCommit);
        return c;
    }

    /**
     *  tableinfo 数据迁移source_db
     *  url eg:"jdbc:postgresql://localhost:5432/metaspace_dev",     *
     *  username "metaspace",
     * password  "metaspace"
     */
    public  void processSourceDb() {
        Connection c = null;
        try {
            c = getConnection(false);
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

    /**
     * db-info table data
     */
    public  void processDbInfo() {
        Connection c = null;
        PreparedStatement insertStmt = null;
        List<String> databaseGuidList = queryDataBaseGuid(url, username, password);
        if(CollectionUtils.isEmpty(databaseGuidList)){
            log.info("没有要处理的db_guid信息");
            return ;
        }
        try {
            c = getConnection(false);
            log.info("Opened database successfully");
            String insertDbInfo = "INSERT INTO public.db_info(\n" +
                    "\tdatabase_guid, database_name, owner, db_type, status, database_description, instance_guid)\n" +
                    "\tVALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            insertStmt = c.prepareStatement(insertDbInfo);

            for (String guid : databaseGuidList){
                Map<String,Object> resultMap = queryAtlasEntityByGuid(guid);// queryDbInfoByGuid(guid);
                if(resultMap == null || resultMap.isEmpty()){
                    log.error("该 guid： {} 没查找到相关信息",guid);
                    return ;
                }

                String instanceGuid = Objects.toString(resultMap.get("instance_guid"),"");
                insertStmt.setString(1, Objects.toString(resultMap.get("database_guid"),""));
                insertStmt.setString(2, Objects.toString(resultMap.get("database_name"),""));
                insertStmt.setString(3, Objects.toString(resultMap.get("owner"),""));
                insertStmt.setString(4, "");
                insertStmt.setString(6, Objects.toString(resultMap.get("status"),""));
                insertStmt.setString(7, Objects.toString(resultMap.get("database_description"),""));
                insertStmt.setString(8, instanceGuid);

                if(!StringUtils.isEmpty(instanceGuid)){
                    Map<String,Object> instanceMap = queryAtlasEntityByGuid(instanceGuid);
                    insertStmt.setString(4, Objects.toString(resultMap.get("db_type"),""));
                }
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            c.commit();
            log.info("操作执行成功.");
        } catch (Exception e) {
            log.error("操作 db_info 出错了，{}",e);
        } finally {
            try {
                if (c != null) c.close();
                if (insertStmt != null) insertStmt.close();
            } catch (Exception e) {
                log.error("释放资源异常，{}", e);
            }
        }
    }
    /*
     从source_db 获取databaseGuid
     */
    private  List<String> queryDataBaseGuid(String url,String username,String password){
        List<String> resultList = new ArrayList<>();
        Connection c = null;
        ResultSet rs = null;
        PreparedStatement selectStmt = null;
        try {
            c = getConnection(false);
            log.info("Opened database successfully");
            String sql =  "SELECT db_guid FROM public.source_db";
            selectStmt = c.prepareStatement(sql);
            rs = selectStmt.executeQuery();
            while(rs.next()){
                resultList.add(rs.getString("db_guid"));
            }
        }catch (Exception e){
            log.error("获取db_guid出错，{}",e);
        } finally {
            try {
                if (rs != null) rs.close();
                selectStmt.close();
                if (c != null) c.close();
            } catch (Exception e) {
                log.error("释放资源异常，{}", e);
            }
        }
         return resultList;
    }

    private Map<String,Object> queryAtlasEntityByGuid(String guid){
        Map<String,Object> resultMap = new HashMap<>();
        AtlasEntity.AtlasEntityWithExtInfo entityWithExtInfo = entitiesStore.getById(guid, false);
        AtlasEntity  atlasEntity = entityWithExtInfo.getEntity();
        String typeName = atlasEntity.getTypeName();
        log.info("atlasEntity typename = {}",typeName);
        if("rdbms_db".equalsIgnoreCase(typeName)) {
            resultMap.put("database_id",atlasEntity.getGuid());
            resultMap.put("database_name",atlasEntity.getAttribute("name"));
            resultMap.put("owner",atlasEntity.getAttribute("owner"));
            resultMap.put("is_deleted",StringUtils.endsWithIgnoreCase(AtlasEntity.Status.DELETED.name(),atlasEntity.getStatus().name()));
            resultMap.put("status",atlasEntity.getStatus().name());
            resultMap.put("database_description",atlasEntity.getAttribute("description"));

            Gson gson = new Gson();
            Map<String, Object> instanceMap = new HashMap<>();
            instanceMap = gson.fromJson(gson.toJson(atlasEntity.getRelationshipAttribute("instance")),instanceMap.getClass() );

            resultMap.put("instance_id",instanceMap.containsKey("guid") ? instanceMap.get("guid") : "");
        }
        if("rdbms_instance".equalsIgnoreCase(typeName)){
            resultMap.put("db_type",atlasEntity.getAttribute("rdbms_type"));
        }
        return resultMap;
    }


    public static void main(String[] args) {
        //processSourceDb("jdbc:postgresql://localhost:5432/metaspace_dev","metaspace", "metaspace");
    }
}
