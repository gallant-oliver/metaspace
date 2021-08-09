package io.zeta.metaspace.web.dao.sourceinfo;

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
            " SELECT " +
            " info.database_guid as databaseId, " +
            " info.database_name as databaseName " +
            " FROM " +
            " db_info AS info " +
            " LEFT JOIN source_db AS sd ON info.database_guid = sd.db_guid " +
            " WHERE " +
            " <choose>" +
            "   <when test=\"sourceId != 'hive'\">"+
            "      sd.source_id = #{sourceId} "+
            "   </when>" +
            " <otherwise>" +
            " info.database_name in " +
            " <foreach collection='databases' item='item' separator=',' open='(' close=')'>"+
            "    #{item}"+
            "  </foreach>" +
            " and info.db_type = 'HIVE' and sd.id is null" +
            " </otherwise>" +
            " </choose>" +
            "AND info.database_guid NOT IN ( SELECT DISTINCT database_id FROM source_info WHERE tenant_id = #{tenantId} AND version = 0)" +
            "</script>")
    List<DataBaseInfo> getDataBaseCode(@Param("sourceId") String sourceId,  @Param("tenantId") String tenantId, @Param("databases") List<String> databases);

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
            "select tb.db_type AS dbType,tb.database_guid AS databaseId,tb.database_name AS databaseName ," +
            "  (select source_id from source_db where db_guid=tb.database_guid limit 1 ) AS sourceId "+
            "from db_info tb where database_name in "+
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            "#{dbName}"+
            "</foreach>"+
            "</script>")
    List<DatabaseInfoForDb> findExistDbName(@Param("dbNameList")List<String> dbNameList);

    @Select("<script>"+
            "select  ts.tenant_id AS tenantId, ts.database_alias AS databaseAlias,ts.category_id AS  categoryId " +
            " from source_info ts " +
            " where ts.tenant_id=#{tenantId} and ts.version = 0 and ts.database_alias in "+
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            "#{dbName}"+
            "</foreach>" +
            "</script>")
    List<DatabaseInfoForDb> findSourceInfoByDbZHName(@Param("dbNameList")List<String> dbZHNameList,@Param("tenantId") String tenantId);
}
