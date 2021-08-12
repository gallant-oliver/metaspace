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
            " SELECT tableguid AS tableId,SOURCE.data_source_id AS dataSourceId " +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and SOURCE.category_id = #{categoryId} AND SOURCE.tenant_id = #{tenantId} " +
            " AND SOURCE.VERSION = 0 AND SOURCE.data_source_id = #{sourceId} AND SOURCE.database_id = #{dbGuid} " +
            " UNION" +
            " SELECT table_id AS tableId,data_source_id AS dataSourceId " +
            " FROM table_data_source_relation AS relation INNER JOIN tableinfo ON relation.table_id = tableinfo.tableguid" +
            " WHERE tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and relation.category_id = #{categoryId} AND relation.tenant_id = #{tenantId} AND relation.data_source_id = #{sourceId} " +
            " AND tableinfo.databaseguid = #{dbGuid}" +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryIdAndSourceIdAndDb(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId, @Param("sourceId") String sourceId, @Param("dbGuid") String dbGuid);

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.data_source_id AS dataSourceId,SOURCE.database_id AS databaseId " +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and SOURCE.category_id = #{categoryId} AND SOURCE.tenant_id = #{tenantId} " +
            " AND SOURCE.VERSION = 0 " +
            " UNION" +
            " SELECT relation.table_id AS tableId,relation.data_source_id AS dataSourceId,tableinfo.databaseguid AS databaseId " +
            " FROM table_data_source_relation AS relation INNER JOIN tableinfo ON relation.table_id = tableinfo.tableguid" +
            " WHERE tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and relation.category_id = #{categoryId} AND relation.tenant_id = #{tenantId} " +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId);


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

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.database_id AS databaseId" +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' and tableinfo.databasestatus = 'ACTIVE' and SOURCE.category_id = #{categoryId} AND SOURCE.tenant_id = #{tenantId} AND SOURCE.VERSION = 0 AND SOURCE.data_source_id = #{sourceId}" +
            " UNION" +
            " SELECT table_id AS tableId,tableinfo.databaseguid AS databaseId" +
            " FROM table_data_source_relation AS relation INNER JOIN tableinfo ON relation.table_id = tableinfo.tableguid" +
            " WHERE tableinfo.status = 'ACTIVE' and tableinfo.databasestatus = 'ACTIVE' and relation.category_id = #{categoryId} AND relation.tenant_id = #{tenantId} AND relation.data_source_id = #{sourceId}" +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryIdAndTenantIdAndSourceId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId, @Param("sourceId") String sourceId);

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.database_id AS databaseId,SOURCE.data_source_id" +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' and tableinfo.databasestatus = 'ACTIVE' and category_id = #{categoryId} AND tenant_id = #{tenantId} AND VERSION = 0 and tableinfo.dbname in" +
            " <foreach item='item' index='index' collection='dbNameList' open='(' separator=',' close=')'>" +
            "   #{item}" +
            " </foreach> " +
            " UNION" +
            " SELECT table_id AS tableId,tableinfo.databaseguid AS databaseId,relation.data_source_id" +
            " FROM table_data_source_relation AS relation INNER JOIN tableinfo ON relation.table_id = tableinfo.tableguid " +
            " WHERE tableinfo.status = 'ACTIVE' and tableinfo.databasestatus = 'ACTIVE' and relation.category_id = #{categoryId} AND relation.tenant_id = #{tenantId} and tableinfo.dbname in" +
            " <foreach item='item' index='index' collection='dbNameList' open='(' separator=',' close=')'>" +
            "   #{item}" +
            " </foreach> " +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryIdAndTenantId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId, @Param("dbNameList") List<String> dbNameList);
}
