package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForDb;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatabaseDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);

    @Select("<script>"+
            "select  tb. db_type,tb.database_id,tb.database_name, ts.database_alias\n" +
            " from db_info tb left join source_info ts on tb.database_id = ts.database_id\n" +
            " where tb.database_name in "+
            "<foreach collection='dbNameList' item='dbName' separator=',' open='(' close=')'>"+
            "#{dbName}"+
            "</foreach>" +
            "</script>")
    List<DatabaseInfoForDb> findDbInfoByDbName(@Param("dbNameList")List<String> dbNameList);
}
