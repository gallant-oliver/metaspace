package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.po.sourceinfo.TableDataSourceRelationPO;
import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatabaseDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);

    @Update("UPDATE db_info SET category_id = #{categoryId}  WHERE database_guid = #{databaseId}")
    void updateDatabaseRelationToCategory(@Param("databaseId") String databaseId, @Param("categoryId") String categoryId);

    @Delete("DELETE FROM db_category_relation " +
            "WHERE db_guid = #{databaseId} AND category_id = #{categoryId}")
    void deleteDbCategoryRelation(@Param("databaseId") String databaseId, @Param("categoryId") String categoryId);

    @Insert("INSERT INTO db_category_relation" +
            " (id,db_guid,category_id,tenant_id) " +
            "VALUES" +
            " (#{uuid},#{databaseId},#{categoryId},#{tenantId})")
    void insertDbCategoryRelation(@Param("tenantId")String tenantId,@Param("uuid") String uuid,@Param("databaseId") String databaseId, @Param("categoryId") String categoryId);

    @Update("<script>" +
            "DELETE FROM db_category_relation " +
            " WHERE category_id IN " +
            "<foreach collection='categoryIds' item='categoryId' separator=',' open='(' close=')'>"+
            "#{categoryId}"+
            "</foreach>" +
            "</script>")
    void deleteDbCategoryRelationByList(@Param("categoryIds") List<String> categoryId);

    @Select("<script>" +
            " SELECT DISTINCT " +
            " info.database_guid as databaseId, " +
            " info.database_name as databaseName " +
            " FROM " +
            " db_info AS info " +
            " INNER JOIN database_group_relation dgr ON dgr.database_guid = info.database_guid " +
            " INNER JOIN user_group_relation ugr ON ugr.group_id = dgr.group_id AND ugr.user_id = #{userId} " +
            " LEFT JOIN source_db AS sd ON info.database_guid = sd.db_guid " +
            " where info.database_name in " +
            " <foreach collection='databases' item='item' separator=',' open='(' close=')'>"+
            "    #{item}"+
            "  </foreach>" +
            " and info.db_type = 'HIVE' and sd.id is null" +
            " AND info.database_guid NOT IN ( SELECT DISTINCT database_id FROM source_info WHERE tenant_id = #{tenantId} AND version = 0)" +
            "</script>")
    List<DataBaseInfo> getHiveDataBaseCode(@Param("tenantId") String tenantId, @Param("databases") List<String> databases, @Param("userId") String userId);

    @Select("<script>" +
            " SELECT DISTINCT " +
            " info.database_guid as databaseId, " +
            " info.database_name as databaseName " +
            " FROM " +
            " db_info AS info " +
            " INNER JOIN database_group_relation dgr ON dgr.database_guid = info.database_guid " +
            " INNER JOIN user_group_relation ugr ON ugr.group_id = dgr.group_id AND ugr.user_id = #{userId} " +
            " INNER JOIN user_group ug ON ug.id=ugr.group_id AND ug.valid=true and ug.tenant=#{tenantId} " +
            " INNER JOIN source_db AS sd ON info.database_guid = sd.db_guid " +
            " WHERE " +
            " sd.source_id = #{sourceId} " +
            " AND info.database_guid NOT IN ( SELECT DISTINCT database_id FROM source_info WHERE tenant_id = #{tenantId} AND version = 0)" +
            "</script>")
    List<DataBaseInfo> getRBMSDataBaseCode(@Param("sourceId") String sourceId,  @Param("tenantId") String tenantId, @Param("userId") String userId);

   /* @Select("<script>"+
            "select  tb.db_type AS dbType,tb.database_guid AS databaseId,tb.database_name AS databaseName," +
            " ts.tenant_id AS tenantId, ts.database_alias AS databaseAlias,ts.category_id AS  categoryId ,\n" +
            "  (select source_id from source_db where db_guid=tb.database_guid limit 1 ) AS sourceId "+
            " from db_info tb left join source_info ts on tb.database_guid = ts.database_id\n" +
            " where ts.tenant_id=#{tenantId} and ts.version = 0 and tb.database_name in "+
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            "#{dbName}"+
            "</foreach>" +
            "</script>")
    List<DatabaseInfoForDb> findDbInfoByDbName(@Param("dbNameList")List<String> dbNameList,@Param("tenantId") String tenantId);*/

    @Select("<script>"+
            "select tb.db_type AS dbType,tb.database_guid AS databaseId,tb.database_name AS databaseName " +
           // "  (select source_id from source_db where db_guid=tb.database_guid limit 1 ) AS sourceId "+
            "from db_info tb where status='ACTIVE' and database_name in "+
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            "#{dbName}"+
            "</foreach>"+
            "</script>")
    List<DatabaseInfoForDb> findExistDbName(@Param("dbNameList")List<String> dbNameList);

    @Select("<script>" +
            " SELECT db.database_guid AS databaseId,db.database_name AS databaseName,db.db_type AS dbType,db.instance_guid AS instanceGuid,sd.source_id as sourceId," +
            " ds.source_name AS sourceName, ds.DATABASE as database" +
            " FROM db_info AS db INNER JOIN source_db AS sd ON db.database_guid = sd.db_guid INNER JOIN data_source AS ds ON ds.source_id = sd.source_id " +
            " WHERE ds.tenantid = #{tenantId} AND db.status = 'ACTIVE' AND db.database_name IN " +
            " <foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            " #{dbName}"+
            " </foreach>" +
            " <if test='hiveList != null and hiveList.size()>0'>" +
            " UNION " +
            " SELECT db.database_guid AS databaseId,db.database_name AS databaseName,db.db_type AS dbType,'hive' AS instanceGuid,'hive' as sourceId," +
            " 'hive' AS sourceName,'hive' as database" +
            " FROM db_info AS db" +
            " WHERE db.status = 'ACTIVE' AND db.db_type = 'HIVE' AND db.database_name IN" +
            " <foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            " #{dbName}"+
            " </foreach>" +
            " AND db.database_name IN" +
            " <foreach collection='hiveList' item='dbName' separator=',' open='(' close=')'>"+
            " #{dbName}"+
            " </foreach>" +
            " </if>" +
            "</script>")
    List<DatabaseInfoForDb> selectByTenantIdAndDbName(@Param("dbNameList")List<String> dbNameList, @Param("tenantId") String tenantId, @Param("hiveList") List<String> hiveList);

    @Select("<script>" +
            "select  ts.tenant_id AS tenantId, ts.database_alias AS databaseAlias,ts.category_id AS  categoryId, " +
            " db.database_name databaseName,db.db_type AS dbType " +
            " from source_info ts,db_info db " +
            " where db.database_guid=ts.database_id and ts.tenant_id=#{tenantId} and ts.version = 0  " +
        
            " <if test='dbNameList!=null and dbNameList.size()>0'>" +
            " and ( ts.database_alias in " +
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>" +
            "#{dbName}" +
            "</foreach>" +
        
            "    <if test='dbEnNameList!=null and dbEnNameList.size()>0'>" +
            " OR  db.database_name in " +
            "<foreach collection='dbEnNameList' item='enName' separator=',' open='(' close=')'>" +
            "#{enName}" +
            "</foreach>" +
            "    </if>" +
            ") " +
            "    </if>" +
        
            "</script>")
    List<DatabaseInfoForDb> findSourceInfoByDbZHName(@Param("dbNameList") List<String> dbZHNameList, @Param("dbEnNameList") List<String> searchDbEnList, @Param("tenantId") String tenantId);
    
    /**
     * 查询当前用户在当前租户下有权限访问的HIVE数据库ID
     */
    List<String> getHiveDataBaseName(@Param("tenantId") String tenantId,
                                     @Param("userId") String userId);

    TableDataSourceRelationPO selectByTableGuid(@Param("tableGuid") String tableGuid);
}
