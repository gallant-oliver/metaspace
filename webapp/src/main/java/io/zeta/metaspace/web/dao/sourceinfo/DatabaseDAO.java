package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.source.DataBaseInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatabaseDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId") String databaseId);

    @Select("SELECT " +
            "info.database_id, " +
            "info.database_name  " +
            "FROM" +
            "db_info AS info " +
            "INNER JOIN source_db AS sd ON info.database_id = sd.source_id " +
            "WHERE " +
            "sd.source_id = #{sourceId} " +
            "AND info.db_type = #{dbType} " +
            "AND info.database_id NOT IN ( SELECT DISTINCT database_id FROM source_info WHERE tenant_id = #{tenantId} AND ( category_id IS NULL OR category_id = '' ))")
    List<DataBaseInfo> getDataBaseCode(@Param("sourceId") String sourceId, @Param("dbType") String dbType, @Param("tenantId") String tenantId);
}
