// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2018/11/21 10:59
 */
package io.zeta.metaspace.web.dao;


import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.pojo.TableRelation;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/21 10:59
 */
public interface RelationDAO {
    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    " ti.tableGuid,\n" +
                    " COUNT ( * ) OVER () total,\n" +
                    " COALESCE(tdsr.data_source_id,(SELECT data_source_id FROM source_info WHERE \"version\" = 0 AND category_id = #{categoryGuid} AND tenant_id = #{tenantId} ),'ID') AS sourceId,\n" +
                    "\n" +
                    " COALESCE (\n" +
                    "  ( SELECT source_name FROM data_source WHERE source_id = tdsr.data_source_id ),\n" +
                    "  (\n" +
                    "  SELECT\n" +
                    "   ds.source_name \n" +
                    "  FROM\n" +
                    "   source_info si\n" +
                    "   LEFT JOIN data_source ds ON si.data_source_id = ds.source_id \n" +
                    "  WHERE\n" +
                    "   VERSION = 0 \n" +
                    "   AND category_id = #{categoryGuid} \n" +
                    "   AND tenant_id = #{tenantId} \n" +
                    "  ),\n" +
                    "  'hive' \n" +
                    " ) AS sourceName," +
                    " (SELECT id FROM source_info WHERE \"version\" = 0 AND category_id = #{categoryGuid} AND tenant_id = #{tenantId} ) AS sourceInfoId, " +
                    " tdsr.category_id AS categoryGuid,\n" +
                    " ti.tableName,\n" +
                    " ti.dbName,\n" +
                    " ti.databaseguid AS dbId,\n" +
                    " ti.tableGuid,\n" +
                    " ti.status,\n" +
                    " tdsr.update_time AS generateTime,\n" +
                    " ti.description \n" +
                    "FROM\n" +
                    " tableinfo ti\n" +
                    " LEFT JOIN table_data_source_relation tdsr ON tdsr.table_id = ti.tableGuid \n" +
                    "WHERE\n" +
                    " ((\n" +
                    "   tdsr.category_id = #{categoryGuid} \n" +
                    "   AND tdsr.tenant_id = #{tenantId} \n" +
                    "   ) \n" +
                    "  OR ti.databaseguid = ( SELECT db_guid FROM db_category_relation dcr WHERE dcr.category_id = #{categoryGuid} AND dcr.tenant_id = #{tenantId} ) \n" +
                    " ) \n" +
                    " AND ti.status = 'ACTIVE' \n" +
                    "ORDER BY\n" +
                    " ti.status,\n" +
                    " tdsr.update_time DESC,\n" +
                    " ti.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    List<RelationEntityV2> queryRelationByCategoryGuidV2(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset,  @Param("tenantId") String tenantId);


    //获取非关系型数据库
    @Select({"<script>",
            "SELECT db_info.database_name FROM db_info,source_db, data_source WHERE db_info.database_guid = source_db.db_guid and source_db.source_id = data_source.source_id ",
            "and db_info.status = 'ACTIVE' and db_info.db_type != 'HIVE' AND data_source.tenantid = #{tenantId} GROUP BY db_info.database_name ",
            " </script>"})
    public List<String> queryRDBNameByCategoryGuidV2(@Param("tenantId") String tenantId);

    @Select({"<script>",
            " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status,tableInfo.description,data_source.source_name sourceName",
            " from table_relation,tableInfo,data_source where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid and tableinfo.source_id = data_source.source_id and status !='DELETED' order by tableinfo.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuidFilter(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset);

//    @Select({"<script>",
//            " select COUNT( * ) OVER ( ) total,A.relationshipGuid,A.categoryGuid,A.tableName,A.dbName,A.tableGuid,A.status,A.description,A.source_name AS sourceName ",
//            " from ( SELECT table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid,tableInfo.status, tableInfo.description,data_source.source_name ",
//            " FROM table_relation,tableInfo,data_source WHERE table_relation.categoryGuid = #{categoryGuid} AND tableinfo.source_id = data_source.source_id AND tableInfo.tableGuid = table_relation.tableGuid ",
//            " AND status != 'DELETED' AND data_source.tenantid = #{tenantId} UNION SELECT table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid,tableInfo.status,",
//            " tableInfo.description, 'hive' as source_name  FROM table_relation,tableInfo WHERE table_relation.categoryGuid = #{categoryGuid} AND tableInfo.tableGuid = table_relation.tableGuid ",
//            " AND status != 'DELETED' AND tableinfo.dbname in",
//            " <foreach item='item' index='index' collection='databases' separator=',' open='(' close=')'>",
//            " #{item}",
//            " </foreach>",
//            " AND tableinfo.source_id = 'hive'",
//            " ) AS A ORDER BY A.tablename",
//            " <if test='limit!= -1'>",
//            " limit #{limit}",
//            " </if>",
//            " offset #{offset}",
//            " </script>"})
    @Select({"<script>",
            "SELECT DISTINCT\n" +
                    " ti.tableGuid,\n" +
                    " COUNT ( * ) OVER () total,\n" +
                    " COALESCE(tdsr.data_source_id,(SELECT data_source_id FROM source_info WHERE \"version\" = 0 AND category_id = #{categoryGuid} AND tenant_id = #{tenantId} ),'ID') AS sourceId,\n" +
                    "\n" +
                    " COALESCE (\n" +
                    "  ( SELECT source_name FROM data_source WHERE source_id = tdsr.data_source_id ),\n" +
                    "  (\n" +
                    "  SELECT\n" +
                    "   ds.source_name \n" +
                    "  FROM\n" +
                    "   source_info si\n" +
                    "   LEFT JOIN data_source ds ON si.data_source_id = ds.source_id \n" +
                    "  WHERE\n" +
                    "   VERSION = 0 \n" +
                    "   AND category_id = #{categoryGuid} \n" +
                    "   AND tenant_id = #{tenantId} \n" +
                    "  ),\n" +
                    "  'hive' \n" +
                    " ) AS sourceName," +
                    " (SELECT id FROM source_info WHERE \"version\" = 0 AND category_id = #{categoryGuid} AND tenant_id = #{tenantId} ) AS sourceInfoId, " +
                    " tdsr.category_id AS categoryGuid,\n" +
                    " ti.tableName,\n" +
                    " ti.dbName,\n" +
                    " ti.databaseguid AS dbId,\n" +
                    " ti.tableGuid,\n" +
                    " ti.status,\n" +
                    " tdsr.update_time AS generateTime,\n" +
                    " ti.description \n" +
                    "FROM\n" +
                    " tableinfo ti\n" +
                    " LEFT JOIN table_data_source_relation tdsr ON tdsr.table_id = ti.tableGuid \n" +
                    "WHERE\n" +
                    " ((\n" +
                    "   tdsr.category_id = #{categoryGuid} \n" +
                    "   AND tdsr.tenant_id = #{tenantId} \n" +
                    "   ) \n" +
                    "  OR ti.databaseguid = ( SELECT db_guid FROM db_category_relation dcr WHERE dcr.category_id = #{categoryGuid} AND dcr.tenant_id = #{tenantId} ) \n" +
                    " ) \n" +
                    " AND ti.status = 'ACTIVE'" +
                    " <if test=\"tableName != '' and tableName != null\">",
                    " AND ti.tablename like concat('%',#{tableName},'%')" +
                    " </if>",
                    " ORDER BY" +
                    " ti.status,\n" +
                    " tdsr.update_time DESC,\n" +
                    " ti.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuidFilterV2(@Param("categoryGuid") String categoryGuid, @Param("tenantId") String tenantId, @Param("limit") int limit, @Param("offset") int offset,@Param("tableName") String tableName);

    @Select({"<script>",
            " SELECT COUNT ( * ) OVER ( ) total,A.tableGuid,A.tableName,A.databaseguid,A.dbName,A.status FROM ( SELECT tableInfo.tableGuid,tableInfo.tableName,tableInfo.dbName,",
            " tableInfo.databaseguid,tableInfo.status FROM tableInfo,data_source WHERE tableinfo.source_id = data_source.source_id AND status != 'DELETED' ",
            " AND data_source.tenantid = #{tenantId} AND tableInfo.tablename LIKE concat(#{tableName},'%')",
            " UNION",
            " SELECT tableInfo.tableGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.databaseguid,tableInfo.status FROM tableInfo WHERE status != 'DELETED' AND tableinfo.dbname IN ",
            " <foreach item='item' index='index' collection='databases' separator=',' open='(' close=')'>",
            " #{item}",
            " </foreach>",
            " AND tableinfo.source_id = 'hive' AND tableInfo.tablename LIKE concat(#{tableName},'%')",
            " ) AS A ORDER BY A.tableGuid",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> selectListByTableName(@Param("tableName") String tableName, @Param("tenantId") String tenantId, @Param("limit") Long limit, @Param("offset") Long offset, @Param("databases") List<String> databases);

    @Select({"<script>",
            " SELECT COUNT ( * ) OVER ( ) total,A.databaseguid as tableGuid,A.dbName as tableName,A.status FROM ( SELECT DISTINCT tableInfo.dbName,tableInfo.databaseguid,tableInfo.status FROM",
            " tableInfo,data_source ",
            " WHERE tableinfo.source_id = data_source.source_id AND status != 'DELETED' AND data_source.tenantid = #{tenantId} AND tableInfo.dbname LIKE concat(#{dbName},'%')",
            " UNION",
            " SELECT DISTINCT tableInfo.dbName,tableInfo.databaseguid,tableInfo.status FROM tableInfo WHERE status != 'DELETED' AND tableinfo.dbname IN ",
            " <foreach item='item' index='index' collection='databases' separator=',' open='(' close=')'>",
            " #{item}",
            " </foreach>",
            " AND tableinfo.source_id = 'hive' AND tableInfo.dbname LIKE concat(#{dbName},'%')",
            " ) AS A ORDER BY A.databaseguid",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> selectListByDbName(@Param("dbName") String dbName, @Param("tenantId") String tenantId, @Param("limit") Long limit, @Param("offset") Long offset, @Param("databases") List<String> databases);

    @Select("select table_data_source_relation.category_id as categoryGuid, table_data_source_relation.table_id as tableGuid, table_data_source_relation.table_id as tenantId, " +
            "table_data_source_relation.create_time as createDate,category.name as categoryName " +
            "from table_data_source_relation join category on (table_data_source_relation.tenant_id = category.tenantid and category.guid = table_data_source_relation.category_id) " +
            "where table_data_source_relation.tenant_id = #{tenantId} and table_data_source_relation.table_id = #{tableGuid}")
    List<TableRelation> queryTableCategoryRelations(@Param("tableGuid") String tableGuid, @Param("tenantId") String tenantId);

    @Select("select category_id as categoryGuid, category.name as categoryName, #{tableGuid} as tableGuid , #{tenantId} as tenantId, create_time as createDate " +
            "from tableinfo t1 join source_info t2 on t1.databaseguid = t2.database_id join category on (t2.category_id = category.guid and t2.tenant_id=category.tenantid) " +
            "where t2.tenant_id = #{tenantId} and t1.tableguid = #{tableGuid} and t2.version = 0")
    List<TableRelation> queryTableCategoryRelationsFromDb(@Param("tableGuid") String tableGuid, @Param("tenantId") String tenantId);

    @Select({"<script>",
                "select name from category where tenantid = #{tenantId} and guid in ",
                " <foreach item='categoryId' index='index' collection='categoryIds' separator=',' open='(' close=')'>",
                    " #{categoryId}",
                " </foreach>",
            " </script>"})
    List<String> queryCategoryNames(@Param("categoryIds") List<String> categoryIds, @Param("tenantId") String tenantId);



    @Select({"<script>",
            " select tableInfo.tablename,",
            " tableInfo.dbname,",
            " tableInfo.tableguid,",
            " tableInfo.status,",
            " tableInfo.createtime,",
            " tableInfo.dataowner,",
            " tableinfo.description,",
            " table_relation.relationshipguid,",
            " table_relation.categoryguid,",
            " case tableInfo.source_id when 'hive' then 'hive' else data_source.source_name end as source_name,",
            " count(*)over() total from table_relation",
            " join tableInfo on",
            " table_relation.tableGuid=tableInfo.tableGuid",
            " left join data_source ",
            " on tableInfo.source_id = data_source.source_id",
            " where",
            " categoryGuid in",
            " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
            " #{categoryGuid}",
            " </foreach>",
            " <if test=\"tableName != null and tableName!=''\">",
            " and",
            " tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and",
            " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like concat('%',#{tagName},'%') ESCAPE '/') ",
            " </if>",
            " order by tableinfo.tablename ",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryByTableName(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
            " SELECT\n" +
                    "   tableInfo.tablename,\n" +
                    "   tableInfo.dbname,\n" +
                    "   tableInfo.tableguid,\n" +
                    "   tableInfo.status,\n" +
                    "   tableInfo.createtime,\n" +
                    "   tableInfo.dataowner,\n" +
                    "   tableInfo.description,\n" +
                    "   tableinfo.databaseguid AS dbId,",
                    "   data_source.source_id AS sourceId,\n" +
                    "   data_source.source_type AS dataSourceType,\n" +
                    "   source_info.category_id AS categoryGuid,\n" +
                    "   source_info.id AS sourceInfoId,\n" +
                    "CASE\n" +
                    "      data_source.source_type\n" +
                    "      WHEN 'ORACLE' THEN\n" +
                    "      data_source.database ELSE '' \n" +
                    "   END AS databaseInstance,\n" +
                    "CASE\n" +
                    "     WHEN  data_source.source_id = \n" +
                    "      'hive' THEN\n" +
                    "      'hive' " +
                    "     WHEN  data_source.source_id is \n" +
                    "     null THEN\n" +
                    "      'hive' "+
                    "ELSE data_source.source_name \n" +
                    "   END AS source_name,\n" +
                    "   COUNT ( * ) OVER () total \n" +
                    "FROM\n" +
                    "   tableInfo " +
                    "   LEFT JOIN source_info ON tableinfo.databaseguid = source_info.database_id\n" +
                    "   LEFT JOIN data_source ON source_info.data_source_id = data_source.source_id \n" +
                    "WHERE\n" +
                    "   tableInfo.status = 'ACTIVE' \n" +
                    "   AND (\n" +
                    "       ( tableinfo.dbname IN" +
                    "         <foreach item='item' index='index' collection='databases' open='(' separator=',' close=')'>",
            "           #{item}",
            "        </foreach>"+
                    " OR tableinfo.source_id != 'hive' ) \n" +
                    "      OR (\n" +
                    "      tableinfo.databaseguid IN ( SELECT db_guid FROM db_category_relation dcr WHERE dcr.category_id IN" +
                    " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
            " #{categoryGuid}",
            " </foreach>",
            " AND dcr.tenant_id = #{tenantId} )) \n" +
                    "   ) " +
                    "   AND source_info.version = 0",
            " <if test=\"tableName != null and tableName!=''\">",
            " and",
            " tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and",
            " tableInfo.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like concat('%',#{tagName},'%') ESCAPE '/') ",
            " </if>",
            " order by tableinfo.tablename ",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryByTableNameV2(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset, @Param("databases") List<String> databases, @Param("tenantId") String tenantId);

    @Select({"<script>",
                    " SELECT tableInfo.tablename," +
                    "   tableInfo.dbname," +
                    "   tableInfo.tableguid," +
                    "   tableInfo.status," +
                    "   tableInfo.createtime," +
                    "   tableInfo.dataowner," +
                    "   tableInfo.description," +
                    "   tableinfo.databaseguid AS dbId,",
                    "   data_source.source_id AS sourceId," +
                    "   data_source.source_type AS dataSourceType," +
                    "   source_info.category_id AS categoryGuid," +
                    "   source_info.tenant_id AS tenantId," +
                    "   source_info.id AS sourceInfoId," +
                    " CASE" +
                    "      data_source.source_type" +
                    "      WHEN 'ORACLE' THEN" +
                    "      data_source.database ELSE '' " +
                    "   END AS databaseInstance," +
                    " CASE" +
                    "     WHEN  data_source.source_id = " +
                    "      'hive' THEN" +
                    "      'hive' " +
                    "     WHEN  data_source.source_id is " +
                    "     null THEN" +
                    "      'hive' "+
                    " ELSE data_source.source_name " +
                    "   END AS source_name," +
                    "   COUNT ( * ) OVER () total " +
                    " FROM" +
                    "   tableInfo " +
                    "   LEFT JOIN source_info ON tableinfo.databaseguid = source_info.database_id" +
                    "   LEFT JOIN data_source ON source_info.data_source_id = data_source.source_id " +
                    " WHERE" +
                    "  tableInfo.status = 'ACTIVE' AND tableInfo.databasestatus = 'ACTIVE' " +
                    "  AND tableinfo.databaseguid IN ( SELECT db_guid FROM db_category_relation dcr WHERE dcr.category_id IN" +
                    "       <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
                    "           #{categoryGuid}",
                    "       </foreach>)",
                    "   AND source_info.version = 0 AND source_info.category_id IS NOT NULL AND source_info.category_id != ''",
                    " <if test=\"tableName != null and tableName!=''\">",
                    " and tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/'",
                    " </if>",
                    " <if test=\"tagName != null and tagName!=''\">",
                    " and tableInfo.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like concat('%',#{tagName},'%') ESCAPE '/') ",
                    " </if>",
                    " order by tableinfo.tablename ",
                    " <if test='limit!= -1'>",
                    " limit #{limit}",
                    " </if>",
                    " offset #{offset}",
                    " </script>"})
    List<RelationEntityV2> queryByTableNameV2General(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
            " SELECT tableInfo.tablename," +
                    "   tableInfo.dbname," +
                    "   tableInfo.tableguid," +
                    "   tableInfo.status," +
                    "   tableInfo.createtime," +
                    "   tableInfo.dataowner," +
                    "   tableInfo.description," +
                    "   tableinfo.databaseguid AS dbId,",
            "   data_source.source_id AS sourceId," +
                    "   data_source.source_type AS dataSourceType," +
                    "   source_info.category_id AS categoryGuid," +
                    "   source_info.tenant_id AS tenantId," +
                    "   source_info.id AS sourceInfoId," +
                    " CASE" +
                    "      data_source.source_type" +
                    "      WHEN 'ORACLE' THEN" +
                    "      data_source.database ELSE '' " +
                    "   END AS databaseInstance," +
                    " CASE" +
                    "     WHEN  data_source.source_id = " +
                    "      'hive' THEN" +
                    "      'hive' " +
                    "     WHEN  data_source.source_id is " +
                    "     null THEN" +
                    "      'hive' "+
                    " ELSE data_source.source_name " +
                    "   END AS source_name," +
                    "   COUNT ( * ) OVER () total " +
                    " FROM" +
                    "   tableInfo " +
                    "   LEFT JOIN source_info ON tableinfo.databaseguid = source_info.database_id" +
                    "   LEFT JOIN data_source ON source_info.data_source_id = data_source.source_id " +
                    " WHERE" +
                    "  tableInfo.status = 'ACTIVE' AND tableInfo.databasestatus = 'ACTIVE' " +
            "   AND source_info.version = 0 AND source_info.category_id IS NOT NULL AND source_info.category_id != ''",
            " <if test=\"tableName != null and tableName!=''\">",
            " and tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and tableInfo.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like concat('%',#{tagName},'%') ESCAPE '/') ",
            " </if>",
            " order by tableinfo.tablename ",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    List<RelationEntityV2> queryByTableNameV2Global(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
            " select tableInfo.tablename,",
            " tableInfo.dbname,",
            " tableInfo.tableguid,",
            " tableInfo.status,",
            " tableInfo.createtime,",
            " tableInfo.dataowner,",
            " tableinfo.description,",
            " table_relation.relationshipguid,",
            " table_relation.categoryguid,",
            " case tableInfo.source_id when 'hive' then 'hive' else data_source.source_name end as source_name,",
            " count(*)over() total from table_relation",
            " join tableInfo on",
            " table_relation.tableGuid=tableInfo.tableGuid",
            " left join data_source ",
            " on tableInfo.source_id = data_source.source_id",
            " where",
            " categoryGuid in",
            " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
            " #{categoryGuid}",
            " </foreach>",
            " <if test=\"tableName != null and tableName!=''\">",
            " and",
            " tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and",
            " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like concat('%',#{tagName},'%') ESCAPE '/') ",
            " </if>",
            " and status !='DELETED' ",
            " order by tableinfo.tablename ",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryByTableNameFilter(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
            " select tableInfo.tablename,",
            " tableInfo.dbname,",
            " tableInfo.tableguid,",
            " tableInfo.status,",
            " tableInfo.createtime,",
            " tableInfo.dataowner,",
            " tableinfo.description,",
            " table_relation.relationshipguid,",
            " table_relation.categoryguid,",
            " case tableInfo.source_id when 'hive' then 'hive' else data_source.source_name end as source_name,",
            " count(*)over() total from table_relation",
            " join tableInfo on",
            " table_relation.tableGuid=tableInfo.tableGuid",
            " left join data_source ",
            " on tableInfo.source_id = data_source.source_id",
            " where",
            " categoryGuid in",
            " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
            " #{categoryGuid}",
            " </foreach>",
            " and ( tableinfo.dbname in ",
            " <foreach item='item' index='index' collection='databases'",
            " open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach> or tableinfo.source_id != 'hive') ",
            " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive') ",
            " <if test=\"tableName != null and tableName!=''\">",
            " and",
            " tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and",
            " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like concat('%',#{tagName},'%') ESCAPE '/') ",
            " </if>",
            " and status !='DELETED' ",
            " order by tableinfo.tablename ",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryByTableNameFilterV2(@Param("tenantId") String tenantId, @Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset, @Param("databases") List<String> databases);

    @Select("select count(*) from business_relation where categoryGuid=#{categoryGuid}")
    public int queryBusinessRelationNumByCategoryGuid(@Param("categoryGuid") String categoryGuid);

    @Select("<script>" +
            "select businessid from business_relation where categoryGuid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getBusinessIdsByCategoryGuid(@Param("ids") List<String> categoryGuids);

    @Update("update tableInfo set status=#{status} where tableGuid=#{tableGuid}")
    public int updateTableStatus(@Param("tableGuid") String tableGuid, @Param("status") String status);

    @Update("update tableInfo set status=#{status} where tableGuid in ('${tableGuids}')")
    public int updateTableStatusBatch(@Param("tableGuids") String tableGuids, @Param("status") String status);

    @Select("select count(*) from tableinfo where tableGuid=#{tableGuid}")
    public int queryTableInfo(@Param("tableGuid") String tableGuid);

    @Insert("insert into tableInfo(tableName,dbName,tableGuid,status,createTime)values(#{tableName},#{dbName},#{tableGuid},#{status},#{createTime})")
    public int addTableInfo(RelationEntityV2 entity) throws SQLException;

    @Select({" <script>",
            " select * from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/'",
            " and status='ACTIVE'",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<TableInfo> getDbTables(@Param("databaseGuid") String databaseId, @Param("query") String query, @Param("limit") Long limit, @Param("offset") Long offset);


    @Select({" <script>",
            " select * from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/' and tableName not like 'values__tmp__table__%' ESCAPE '/'",
            " and status='ACTIVE'",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<TableInfo> getDbTablesWithoutTmp(@Param("databaseGuid") String databaseId, @Param("query") String query, @Param("limit") Long limit, @Param("offset") Long offset);

    @Select({" <script>",
            " select count(1) from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/' and tableName not like 'values_tmp_table_%' ESCAPE '/'",
            " and status='ACTIVE'",
            " </script>"})
    public int countDbTablesWithoutTmp(@Param("databaseGuid") String databaseId, @Param("query") String query);

    @Select({" <script>",
            " select count(1) from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/' and status='ACTIVE'",
            " </script>"})
    public int countDbTables(@Param("databaseGuid") String databaseId, @Param("query") String query);

    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where " +
            "    guid =#{guid} and tenantid=#{tenantId}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid and category.tenantid=#{tenantId}" +
            ")" +
            "SELECT guid from categoryTree where parentcategoryguid is null or parentcategoryguid =''</script>")
    public String getTopGuidByGuid(@Param("guid") String guid, @Param("tenantId") String tenantId);
}
