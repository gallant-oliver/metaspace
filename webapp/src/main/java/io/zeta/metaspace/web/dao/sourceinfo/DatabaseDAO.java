package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.source.DataBaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatabaseDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);

    @Update("UPDATE db_info SET category_id = #{categoryId}  WHERE database_guid = #{databaseId}")
    void updateDatabaseRelationToCategory(@Param("databaseId") String databaseId, @Param("categoryId") String categoryId);

    @Update("<script>" +
            " UPDATE db_info " +
            " SET category_id = null  " +
            " WHERE database_guid IN " +
            "<foreach collection='databaseIds' item='databaseId' separator=',' open='(' close=')'>"+
            "#{databaseId}"+
            "</foreach>" +
            "</script>")
    void updateDatabaseRelationToCategoryNull(@Param("databaseIds") List<String> databaseId);

    @Select("SELECT " +
            "info.database_guid as databaseId, " +
            "info.database_name as databaseName " +
            "FROM " +
            "db_info AS info " +
            "INNER JOIN source_db AS sd ON info.database_guid = sd.db_guid " +
            "WHERE " +
            "sd.source_id = #{sourceId} " +
            "AND info.database_guid NOT IN ( SELECT DISTINCT database_id FROM source_info WHERE tenant_id = #{tenantId} AND version = 0)")
    List<DataBaseInfo> getDataBaseCode(@Param("sourceId") String sourceId,  @Param("tenantId") String tenantId);

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
