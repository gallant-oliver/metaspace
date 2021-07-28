package io.zeta.metaspace.web.dao.sourceinfo;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);

    @Update("UPDATE db_info SET category_id = #{categoryId} WHERE database_guid = #{databaseId}")
    void updateDatabaseRelationToCategory(@Param("databaseId") String databaseId, @Param("categoryId") String categoryId);
}
