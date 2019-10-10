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
    @Insert("insert into table_relation(relationshipGuid,categoryGuid,tableGuid,generateTime)values(#{relationshipGuid},#{categoryGuid},#{tableGuid},#{generateTime})")
    public int add(RelationEntityV2 entity) throws SQLException;

    @Delete("delete from table_relation where relationshipGuid=#{relationshipGuid}")
    public int delete(@Param("relationshipGuid") String guid);

    @Select({"<script>",
            " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status,table_relation.generateTime",
            " from table_relation,tableInfo where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid order by tableInfo.status,table_relation.generateTime desc, tableinfo.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuid(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset);

    @Select({"<script>",
            " select count(*)over() total,table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status",
            " from table_relation,tableInfo where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid and status !='DELETED' order by tableinfo.tablename",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuidFilter(@Param("categoryGuid") String categoryGuid, @Param("limit") int limit, @Param("offset") int offset);

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


    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryRelationNumByCategoryGuid(@Param("categoryGuid") String categoryGuid);

    @Select("select count(*) from business_relation where categoryGuid=#{categoryGuid}")
    public int queryBusinessRelationNumByCategoryGuid(@Param("categoryGuid") String categoryGuid);

    //@Update("update table_relation set status=#{status} where tableGuid=#{tableGuid}")
    @Update("update tableInfo set status=#{status} where tableGuid=#{tableGuid}")
    public int updateTableStatus(@Param("tableGuid") String tableGuid, @Param("status") String status);

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

    @Insert("insert into table_relation values (#{item.relationshipGuid},#{item.categoryGuid},#{item.tableGuid},#{item.generateTime}) ")
    public int addRelation(@Param("item") TableRelation tableRelation);

    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where " +
            "    guid =#{guid} " +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid" +
            ")" +
            "SELECT guid from categoryTree where parentcategoryguid is null or parentcategoryguid =''</script>")
    public String getTopGuidByGuid(@Param("guid") String guid);

    @Select("select * from table_relation where relationshipguid=#{guid}")
    public RelationEntityV2 getRelationInfoByGuid(String guid);

    //判断关联是否已存在
    @Select("select count(1) from table_relation where categoryguid=#{categoryGuid} and tableguid=#{tableGuid}")
    public int ifRelationExists(@Param("categoryGuid") String categoryGuid,@Param("tableGuid") String tableGuid);

    @Update("update tableInfo set databasestatus=#{status} where databaseGuid=#{databaseGuid}")
    public int updateDatabaseStatus(@Param("databaseGuid") String databaseGuid, @Param("status") String status);

    @Select({" select tableGuid from table_relation where categoryGuid=#{categoryGuid}" })
    public List<String> getAllTableGuidByCategoryGuid(@Param("categoryGuid") String categoryGuid);

}
