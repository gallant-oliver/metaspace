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
import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/21 10:59
 */
public interface RelationDAO {
    @Insert("insert into table_relation(relationshipGuid,categoryGuid,tableGuid,generateTime)values(#{relationshipGuid},#{categoryGuid},#{tableGuid},#{generateTime})")
    public int add(RelationEntityV2 entity) throws SQLException;

    @Delete("delete from table_relation where relationshipGuid=#{relationshipGuid}")
    public int delete(@Param("relationshipGuid") String guid);

    @Select({"<script>",
            " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status,table_relation.generateTime,tableInfo.description,data_source.source_name sourceName",
            " from table_relation,tableInfo,data_source where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid and tableinfo.source_id = data_source.source_id order by tableInfo.status,table_relation.generateTime desc, tableinfo.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuid(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
             " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status,table_relation.generateTime,tableInfo.description,data_source.source_name sourceName",
             " from table_relation,tableInfo,data_source where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid and tableinfo.source_id = data_source.source_id",
             " and status = 'ACTIVE' " ,
             " and ( tableinfo.dbname in " ,
             " <foreach item='item' index='index' collection='databases'" ,
             " open='(' separator=',' close=')'>" ,
             " #{item}" ,
             " </foreach>  or tableinfo.source_id != 'hive')" ,
             " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive') ",
             " order by tableInfo.status,table_relation.generateTime desc, tableinfo.tablename",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuidV2(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

    @Select({"<script>",
            " select tableInfo.dbName ",
            " from table_relation,tableInfo where ",
            " tableInfo.tableGuid=table_relation.tableGuid ",
            " and (categoryGuid in ",
            " <foreach item='categoryGuid' index='index' collection='categoryGuids'" ,
            " open='(' separator=',' close=')'>" ,
            " #{categoryGuid}" ,
            " </foreach> " ,
            " ) ",
            " and status = 'ACTIVE' " ,
            " and ( tableinfo.dbname in " ,
            " <foreach item='item' index='index' collection='databases'" ,
            " open='(' separator=',' close=')'>" ,
            " #{item}" ,
            " </foreach>  or tableinfo.source_id != 'hive')" ,
            " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive') ",
            " GROUP BY tableInfo.dbName ",
            " </script>"})
    public List<String> queryAllDBNameByCategoryGuidV2(@Param("categoryGuids") List<String> categoryGuids,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

    @Select({"<script>",
            " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status,tableInfo.description,data_source.source_name sourceName",
            " from table_relation,tableInfo,data_source where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid and tableinfo.source_id = data_source.source_id and status !='DELETED' order by tableinfo.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuidFilter(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
             " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status,tableInfo.description,data_source.source_name sourceName",
             " from table_relation,tableInfo ,data_source where categoryGuid=#{categoryGuid} and tableinfo.source_id = data_source.source_id and tableInfo.tableGuid=table_relation.tableGuid and status !='DELETED' ",
             " and ( tableinfo.dbname in " ,
             " <foreach item='item' index='index' collection='databases'" ,
             " open='(' separator=',' close=')'>" ,
             " #{item}" ,
             " </foreach> or tableinfo.source_id != 'hive') " ,
             " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive')  ",
             " order by tableinfo.tablename",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuidFilterV2(@Param("categoryGuid") String categoryGuid,@Param("tenantId") String tenantId, @Param("limit") int limit, @Param("offset") int offset,@Param("databases")List<String> databases);

    @Select("select * from table_relation,tableInfo where table_relation.tableGuid=#{tableGuid} and tableinfo.tableGuid=#{tableGuid}")
    public List<RelationEntityV2> queryRelationByTableGuid(@Param("tableGuid") String tableGuid) throws SQLException;


    @Select({"<script>",
            " select *,count(*)over() total from table_relation",
            " join tableInfo on",
            " table_relation.tableGuid=tableInfo.tableGuid",
            " where",
            " categoryGuid in",
            " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
            " #{categoryGuid}",
            " </foreach>",
            " <if test=\"tableName != null and tableName!=''\">",
            " and",
            " tableInfo.tableName like '%${tableName}%' ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and",
            " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like '%${tagName}%' ESCAPE '/') ",
            " </if>",
            " order by tableinfo.tablename ",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryByTableName(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
             " select *,count(*)over() total from table_relation",
             " join tableInfo on",
             " table_relation.tableGuid=tableInfo.tableGuid",
             " where",
             " tableInfo.status = 'ACTIVE' ",
             " and categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
             " #{categoryGuid}",
             " </foreach>",
             " and ( tableinfo.dbname in " ,
             " <foreach item='item' index='index' collection='databases'" ,
             " open='(' separator=',' close=')'>" ,
             " #{item}" ,
             " </foreach> or tableinfo.source_id != 'hive') " ,
             " <if test=\"tableName != null and tableName!=''\">",
             " and",
             " tableInfo.tableName like '%${tableName}%' ESCAPE '/'",
             " </if>",
             " <if test=\"tagName != null and tagName!=''\">",
             " and",
             " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like '%${tagName}%' ESCAPE '/') ",
             " </if>" ,
             " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive')",
             " order by tableinfo.tablename ",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RelationEntityV2> queryByTableNameV2(@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

    @Select({"<script>",
            " select count(*)over() total,* from table_relation",
            " join tableInfo on",
            " table_relation.tableGuid=tableInfo.tableGuid",
            " where",
            " categoryGuid in",
            " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
            " #{categoryGuid}",
            " </foreach>",
            " <if test=\"tableName != null and tableName!=''\">",
            " and",
            " tableInfo.tableName like '%${tableName}%' ESCAPE '/'",
            " </if>",
            " <if test=\"tagName != null and tagName!=''\">",
            " and",
            " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like '%${tagName}%' ESCAPE '/') ",
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
             " select count(*)over() total,* from table_relation",
             " join tableInfo on",
             " table_relation.tableGuid=tableInfo.tableGuid",
             " where",
             " categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>",
             " #{categoryGuid}",
             " </foreach>",
             " and ( tableinfo.dbname in " ,
             " <foreach item='item' index='index' collection='databases'" ,
             " open='(' separator=',' close=')'>" ,
             " #{item}" ,
             " </foreach> or tableinfo.source_id != 'hive') " ,
             " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive') ",
             " <if test=\"tableName != null and tableName!=''\">",
             " and",
             " tableInfo.tableName like '%${tableName}%' ESCAPE '/'",
             " </if>",
             " <if test=\"tagName != null and tagName!=''\">",
             " and",
             " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like '%${tagName}%' ESCAPE '/') ",
             " </if>",
             " and status !='DELETED' ",
             " order by tableinfo.tablename ",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RelationEntityV2> queryByTableNameFilterV2(@Param("tenantId") String tenantId,@Param("tableName") String tableName, @Param("tagName") String tagName, @Param("ids") List<String> categoryIds, @Param("limit") int limit, @Param("offset") int offset,@Param("databases")List<String> databases);

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryRelationNumByCategoryGuid(@Param("categoryGuid") String categoryGuid);

    @Update("<script>" +
            "update table_relation set categoryGuid=#{newCategoryId} where categoryGuid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public int updateRelationByCategoryGuid(@Param("ids") List<String> categoryGuids,@Param("newCategoryId")String newCategoryId);

    @Select("select count(*) from business_relation where categoryGuid=#{categoryGuid}")
    public int queryBusinessRelationNumByCategoryGuid(@Param("categoryGuid") String categoryGuid);

    @Select("<script>" +
            "select businessid from business_relation where categoryGuid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getBusinessIdsByCategoryGuid(@Param("ids") List<String> categoryGuids);

    //@Update("update table_relation set status=#{status} where tableGuid=#{tableGuid}")
    @Update("update tableInfo set status=#{status} where tableGuid=#{tableGuid}")
    public int updateTableStatus(@Param("tableGuid") String tableGuid, @Param("status") String status);

    @Update("update tableInfo set status=#{status} where tableGuid in (#{tableGuids})")
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
    public List<TableInfo> getDbTables(@Param("databaseGuid")String databaseId, @Param("query")String query , @Param("limit")Long limit, @Param("offset")Long offset);



    @Select({" <script>",
             " select * from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/' and tableName not like 'values__tmp__table__%' ESCAPE '/'",
             " and status='ACTIVE'",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<TableInfo> getDbTablesWithoutTmp(@Param("databaseGuid")String databaseId, @Param("query")String query , @Param("limit")Long limit, @Param("offset")Long offset);

    @Select({" <script>",
             " select count(1) from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/' and tableName not like 'values_tmp_table_%' ESCAPE '/'",
             " and status='ACTIVE'",
             " </script>"})
    public int countDbTablesWithoutTmp(@Param("databaseGuid")String databaseId, @Param("query")String query);

    @Select({" <script>",
             " select count(1) from tableinfo where databaseGuid=#{databaseGuid} and tableName like '%'||#{query}||'%' ESCAPE '/' and status='ACTIVE'",
             " </script>"})
    public int countDbTables(@Param("databaseGuid")String databaseId, @Param("query")String query);

    @Delete("delete from table_relation where tableguid=#{tableGuid}")
    public int deleteByTableGuid(@Param("tableGuid") String tableGuid);

    @Update(" <script>" +
            " update table_relation set categoryGuid=#{categoryGuid},generateTime=#{time} where tableguid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            " </script>")
    public int updateByTableGuids(@Param("ids") List<String> ids, @Param("categoryGuid")String categoryGuid, @Param("time") String time);

    @Insert("insert into table_relation values (#{item.relationshipGuid},#{item.categoryGuid},#{item.tableGuid},#{item.generateTime}) ")
    public int addRelation(@Param("item") TableRelation tableRelation);

    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where " +
            "    guid =#{guid} and tenantid=#{tenantId}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid and category.tenantid=#{tenantId}" +
            ")" +
            "SELECT guid from categoryTree where parentcategoryguid is null or parentcategoryguid =''</script>")
    public String getTopGuidByGuid(@Param("guid") String guid,@Param("tenantId") String tenantId);

    @Select("select * from table_relation where relationshipguid=#{guid}")
    public RelationEntityV2 getRelationInfoByGuid(String guid);

    //判断关联是否已存在
    @Select("select count(1) from table_relation where categoryguid=#{categoryGuid} and tableguid=#{tableGuid}")
    public int ifRelationExists(@Param("categoryGuid") String categoryGuid,@Param("tableGuid") String tableGuid);

    @Update("update tableInfo set databasestatus=#{status} where databaseGuid=#{databaseGuid}")
    public int updateDatabaseStatus(@Param("databaseGuid") String databaseGuid, @Param("status") String status);

    @Update("update tableInfo set databasestatus=#{status} where databaseGuid in (#{databaseGuids})")
    public int updateDatabaseStatusBatch(@Param("databaseGuids") String databaseGuids, @Param("status") String status);

    @Select({" select tableGuid from table_relation where categoryGuid=#{categoryGuid}" })
    public List<String> getAllTableGuidByCategoryGuid(@Param("categoryGuid") String categoryGuid);

}
