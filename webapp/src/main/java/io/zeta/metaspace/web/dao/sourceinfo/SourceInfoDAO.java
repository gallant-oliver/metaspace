package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.po.sourceinfo.TableDataSourceRelationPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SourceInfoDAO {

    @Select("<script> " +
            " SELECT tableguid as tableId,source.data_source_id as dataSourceId FROM source_info as source INNER JOIN tableinfo ON source.database_id=tableinfo.databaseguid" +
            " WHERE category_id = #{categoryId} AND tenant_id = #{tenantId} AND version = 0" +
            " UNION" +
            " SELECT table_id as tableId,data_source_id as dataSourceId FROM table_data_source_relation" +
            " WHERE category_id = #{categoryId} AND tenant_id = #{tenantId}" +
            "</script>")
    List<TableDataSourceRelationPO> getTableGuidByCategoryIdAndTenantId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId);

    @Select("<script>" +
            " SELECT t.category_id FROM ("+
            " SELECT source.category_id,source.create_time FROM source_info as source INNER JOIN tableinfo ON source.database_id=tableinfo.databaseguid" +
            " WHERE tableinfo.tableguid = #{tableGuid} AND tenant_id = #{tenantId} AND version = 0 and source.category_id is not null and source.category_id != ''" +
            " UNION" +
            " SELECT category_id,create_time  FROM table_data_source_relation" +
            " WHERE table_id = #{tableGuid} AND tenant_id = #{tenantId}" +
            " ) as t ORDER BY t.create_time limit 1"+
            "</script>")
    List<String> selectByTableGuidAndTenantId(@Param("tableGuid") String tableGuid,@Param("tenantId") String tenantId);
}
