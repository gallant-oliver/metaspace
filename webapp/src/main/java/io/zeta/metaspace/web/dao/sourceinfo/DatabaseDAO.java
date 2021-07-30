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
    @Select("SELECT " +
            "info.database_id, " +
            "info.database_name  " +
            "FROM" +
            "db_info AS info " +
            "INNER JOIN source_db AS sd ON info.database_id = sd.source_id " +
            "WHERE " +
            "sd.source_id = #{sourceId} " +
            "AND info.db_type = #{dbType} " +
            "AND info.database_id NOT IN ( SELECT DISTINCT database_id FROM source_info WHERE tenant_id = #{tenantId} AND version = 0)")
    List<DataBaseInfo> getDataBaseCode(@Param("sourceId") String sourceId, @Param("dbType") String dbType, @Param("tenantId") String tenantId);

    @Select("<script>"+
            "select  tb.db_type AS dbType,tb.database_guid AS databaseId,tb.database_name AS databaseName," +
            " ts.tenant_id AS tenantId, ts.database_alias AS databaseAlias,ts.category_id AS  categoryId ,\n" +
            "  (select source_id from source_db where db_guid=tb.database_guid limit 1 ) AS sourceId "+
            " from db_info tb left join source_info ts on tb.database_guid = ts.database_id\n" +
            " where ts.tenant_id=#{tenantId} and ts.version = 0 and tb.database_name in "+
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            "#{dbName}"+
            "</foreach>" +
            "</script>")
    List<DatabaseInfoForDb> findDbInfoByDbName(@Param("dbNameList")List<String> dbNameList,@Param("tenantId") String tenantId);
}
