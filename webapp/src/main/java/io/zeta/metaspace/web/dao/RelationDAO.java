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

import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    public int delete(@Param("relationshipGuid")String guid);

    @Select({"<script>",
             " select table_relation.relationshipGuid,table_relation.categoryGuid,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status",
             " from table_relation,tableInfo where categoryGuid=#{categoryGuid} and tableInfo.tableGuid=table_relation.tableGuid",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RelationEntityV2> queryRelationByCategoryGuid(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit,@Param("offset") int offset);

    @Select("select * from table_relation,tableInfo where table_relation.tableGuid=#{tableGuid} and tableinfo.tableGuid=#{tableGuid}")
    public List<RelationEntityV2> queryRelationByTableGuid(@Param("tableGuid")String tableGuid) throws SQLException;

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryTotalNumByCategoryGuid(@Param("categoryGuid")String categoryGuid);

    @Select({"<script>",
             " select * from table_relation",
             " join tableInfo on",
             " table_relation.tableGuid=tableInfo.tableGuid",
             " where",
             " categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " <if test=\"tableName != null and tableName!=''\">",
             " and",
             " tableInfo.tableName like '%${tableName}%'",
             " </if>",
             " <if test=\"tagName != null and tagName!=''\">",
             " and",
             " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like '%${tagName}%')",
             " </if>",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RelationEntityV2> queryByTableName(@Param("tableName")String tableName, @Param("tagName")String tagName, @Param("ids") List<String> categoryIds,@Param("limit")int limit,@Param("offset") int offset);

    @Select({"<script>",
             " select count(*) from table_relation",
             " join tableInfo on",
             " table_relation.tableGuid=tableInfo.tableGuid",
             " where",
             " categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " <if test=\"tableName != null and tableName!=''\">",
             " and",
             " tableInfo.tableName like '%${tableName}%'",
             " </if>",
             " <if test=\"tagName != null and tagName!=''\">",
             " and",
             " table_relation.tableGuid in (select tableGuid from table2tag join tag on table2tag.tagId=tag.tagId where tag.tagName like '%${tagName}%')",
             " </if>",
             " </script>"})
    public int queryTotalNumByName(@Param("tableName")String tableName, @Param("tagName")String tagName, @Param("ids") List<String> categoryIds);

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryRelationNumByCategoryGuid(@Param("categoryGuid")String categoryGuid);

    @Select("select count(*) from business_relation where categoryGuid=#{categoryGuid}")
    public int queryBusinessRelationNumByCategoryGuid(@Param("categoryGuid")String categoryGuid);

    //@Update("update table_relation set status=#{status} where tableGuid=#{tableGuid}")
    @Update("update tableInfo set status=#{status} where tableGuid=#{tableGuid}")
    public int updateTableStatus(@Param("tableGuid")String tableGuid,@Param("status")String status);

    @Select("select count(*) from tableinfo where tableGuid=#{tableGuid}")
    public int queryTableInfo(@Param("tableGuid")String tableGuid);

    @Insert("insert into tableInfo(tableName,dbName,tableGuid,status,createTime)values(#{tableName},#{dbName},#{tableGuid},#{status},#{createTime})")
    public int addTableInfo(RelationEntityV2 entity) throws SQLException;

}
