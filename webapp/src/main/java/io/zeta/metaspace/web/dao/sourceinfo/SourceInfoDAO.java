package io.zeta.metaspace.web.dao.sourceinfo;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SourceInfoDAO {

    @Select("<script> " +
            " SELECT tableguid FROM source_info as source INNER JOIN tableinfo ON source.database_id=tableinfo.databaseguid" +
            " WHERE category_id = #{categoryId} AND tenant_id = #{tenantId} AND version = 0" +
            " UNION" +
            " SELECT table_id as tableguid FROM table_data_source_relation" +
            " WHERE category_id = #{categoryId} AND tenant_id = #{tenantId}" +
            "</script>")
    List<String> getTableGuidByCategoryIdAndTenantId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId);

    @Select("<script>" +
            " SELECT t.category_id FROM ("+
            " SELECT source.category_id,source.create_time FROM source_info as source INNER JOIN tableinfo ON source.database_id=tableinfo.databaseguid" +
            " WHERE tableinfo.tableguid = #{tableGuid} AND tenant_id = #{tenantId} AND version = 0" +
            " UNION" +
            " SELECT category_id,create_time  FROM table_data_source_relation" +
            " WHERE table_id = #{tableGuid} AND tenant_id = #{tenantId}" +
            " ) as t ORDER BY t.create_time limit 1"+
            "</script>")
    List<String> selectByTableGuidAndTenantId(@Param("tableGuid") String tableGuid,@Param("tenantId") String tenantId);
}
