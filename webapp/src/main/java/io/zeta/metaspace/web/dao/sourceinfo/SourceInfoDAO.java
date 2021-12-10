package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.po.requirements.RequirementIssuedPO;
import io.zeta.metaspace.model.po.sourceinfo.SourceInfo;
import io.zeta.metaspace.model.po.sourceinfo.TableDataSourceRelationPO;
import io.zeta.metaspace.model.result.AddRelationTable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SourceInfoDAO {

    @Select("<script> " +
            " SELECT tableguid as tableId,source.data_source_id as dataSourceId FROM source_info as source INNER JOIN tableinfo ON source.database_id=tableinfo.databaseguid" +
            " WHERE category_id = #{categoryId} AND tenant_id = #{tenantId} AND version = 0" +
            "</script>")
    List<TableDataSourceRelationPO> getTableGuidByCategoryIdAndTenantId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId);

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.data_source_id AS dataSourceId,SOURCE.category_id " +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and SOURCE.category_id = #{categoryId} AND SOURCE.tenant_id = #{tenantId} " +
            " AND SOURCE.VERSION = 0 AND SOURCE.data_source_id = #{sourceId} AND SOURCE.database_id = #{dbGuid} " +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryIdAndSourceIdAndDb(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId, @Param("sourceId") String sourceId, @Param("dbGuid") String dbGuid);

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.data_source_id AS dataSourceId,SOURCE.database_id AS databaseId,SOURCE.category_id" +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and SOURCE.category_id = #{categoryId} AND SOURCE.tenant_id = #{tenantId} " +
            " AND SOURCE.VERSION = 0 " +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId);


    @Select("<script>" +
            " SELECT SOURCE.category_id,SOURCE.create_time,SOURCE.data_source_id,tableinfo.tableguid as tableId " +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid " +
            " WHERE tableinfo.tableguid IN" +
            " <foreach item='item' index='index' collection='list' open='(' separator=',' close=')'>" +
            "   #{item.tableId}" +
            " </foreach> " +
            " AND tenant_id = #{tenantId} AND VERSION = 0 AND SOURCE.category_id IS NOT NULL AND SOURCE.category_id != ''" +
            "</script>")
    List<TableDataSourceRelationPO> selectByTableGuidAndTenantId(@Param("list") List<AddRelationTable> list, @Param("tenantId") String tenantId);

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.database_id AS databaseId" +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' and tableinfo.databasestatus = 'ACTIVE' and SOURCE.category_id = #{categoryId} AND SOURCE.tenant_id = #{tenantId} AND SOURCE.VERSION = 0 AND SOURCE.data_source_id = #{sourceId}" +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryIdAndTenantIdAndSourceId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId, @Param("sourceId") String sourceId);

    @Select("<script>" +
            " SELECT tableguid AS tableId,SOURCE.database_id AS databaseId,SOURCE.data_source_id" +
            " FROM source_info AS SOURCE INNER JOIN tableinfo ON SOURCE.database_id = tableinfo.databaseguid" +
            " WHERE tableinfo.status = 'ACTIVE' and tableinfo.databasestatus = 'ACTIVE' and category_id = #{categoryId} AND tenant_id = #{tenantId} AND VERSION = 0 and tableinfo.dbname in" +
            " <foreach item='item' index='index' collection='dbNameList' open='(' separator=',' close=')'>" +
            "   #{item}" +
            " </foreach> " +
            "</script>")
    List<TableDataSourceRelationPO> selectListByCategoryIdAndTenantId(@Param("categoryId") String categoryId, @Param("tenantId") String tenantId, @Param("dbNameList") List<String> dbNameList);
    
    @Select("SELECT category_id FROM source_info WHERE tenant_id = #{tenantId} AND version = 0 and category_id is not null and category_id != ''")
    List<String> selectCategoryListByTenantId(@Param("tenantId") String tenantId);
    
    @Select("SELECT category_id,count(*) as count FROM source_info si INNER JOIN tableinfo on si.database_id = tableinfo.databaseguid WHERE tenant_id = #{tenantId} AND version = 0 and tableinfo.status = 'ACTIVE' and category_id is not null and category_id != '' GROUP BY category_id")
    List<SourceInfo> selectCategoryListAndCount(@Param("tenantId") String tenantId);
    
    /**
     * 需求管理 - 查询需求下发的相关展示信息
     */
    RequirementIssuedPO queryIssuedInfo(@Param("tableId") String tableId, @Param("sourceId") String sourceId);
}
