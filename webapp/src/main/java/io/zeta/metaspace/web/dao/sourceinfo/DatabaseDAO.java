package io.zeta.metaspace.web.dao.sourceinfo;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);
}
