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
    //@Insert("insert into table_relation(relationshipGuid,categoryGuid,tableName,dbName,tableGuid,path,status)values(#{relationshipGuid},#{categoryGuid},#{tableName},#{dbName},#{tableGuid},#{path},#{status})")
    @Insert("insert into table_relation(relationshipGuid,categoryGuid,tableGuid,path)values(#{relationshipGuid},#{categoryGuid},#{tableGuid},#{path})")
    public int add(RelationEntityV2 entity) throws SQLException;

    @Delete("delete from table_relation where relationshipGuid=#{relationshipGuid}")
    public int delete(@Param("relationshipGuid")String guid);

    //@Select("select * from table_relation,tableinfo where categoryGuid=#{categoryGuid} and tableinfo.tableGuid=table_relation.tableGuid")
    /*@Select("select table_relation.relationshipGuid,table_relation.categoryGuid,table_relation.path,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status" +
            " from table_relation,tableInfo where categoryGuid=#{categoryGuid} and tableinfo.tableGuid=table_relation.tableGuid")
    public RelationEntityV2 query(@Param("categoryGuid")String categoryGuid);*/

    //@Select("select * from table_relation where categoryGuid=#{categoryGuid} limit #{limit} offset #{offset}")
    @Select("select * from table_relation,tableinfo where categoryGuid=#{categoryGuid} and tableinfo.tableGuid=table_relation.tableGuid limit #{limit} offset #{offset}")
    public List<RelationEntityV2> queryRelationByCategoryGuidByLimit(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit,@Param("offset") int offset);

    //@Select("select * from table_relation where categoryGuid=#{categoryGuid}")
    //@Select("select * from table_relation,tableinfo where categoryGuid=#{categoryGuid} and tableinfo.tableGuid=table_relation.tableGuid")
    @Select("select table_relation.relationshipGuid,table_relation.categoryGuid,table_relation.path,tableInfo.tableName,tableInfo.dbName,tableInfo.tableGuid, tableInfo.status" +
            " from table_relation,tableInfo where categoryGuid=#{categoryGuid} and tableinfo.tableGuid=table_relation.tableGuid")
    public List<RelationEntityV2> queryRelationByCategoryGuid(@Param("categoryGuid")String categoryGuid);

    //@Select("select * from table_relation where tableGuid=#{tableGuid}")
    @Select("select * from table_relation,tableinfo where table_relation.tableGuid=#{tableGuid} and tableinfo.tableGuid=#{tableGuid}")
    public List<RelationEntityV2> queryRelationByTableGuid(@Param("tableGuid")String tableGuid) throws SQLException;

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryTotalNumByCategoryGuid(@Param("categoryGuid")String categoryGuid);

    //@Select("select * from table_relation where tableName like '%${tableName}%' limit #{limit} offset #{offset}")
    @Select("select * from table_relation,tableinfo where table_relation.tableGuid in (select tableGuid from tableinfo where tableName like '%${tableName}%') limit #{limit} offset #{offset}")
    public List<RelationEntityV2> queryByTableName(@Param("tableName")String tableName, @Param("limit")int limit,@Param("offset") int offset);

    //@Select("select count(*) from table_relation where tableName like '%${tableName}%'")
    @Select("select count(*) from table_relation where tableGuid in (select tableGuid from tableinfo where tableName like '%${tableName}%')")
    public int queryTotalNumByName(@Param("tableName")String tableName);

    @Select("select * from table_relation where tableName like '%${tableName}%' and categoryType=#{categoryType} limit #{limit} offset #{offset}")
    public List<RelationEntityV2> queryByTableName(@Param("tableName")String tableName, @Param("limit")int limit,@Param("offset") int offset, @Param("categoryType") int categoryType);

    @Select("select count(*) from table_relation where tableName like '%${tableName}%' and categoryType=#{categoryType}")
    public int queryTotalNumByName(@Param("tableName")String tableName, @Param("categoryType") int categoryType);

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryRelationNumByCatalogGuid(@Param("categoryGuid")String categoryGuid);

    //@Update("update table_relation set status=#{status} where tableGuid=#{tableGuid}")
    @Update("update tableinfo set status=#{status} where tableGuid=#{tableGuid}")
    public int updateTableStatus(@Param("tableGuid")String tableGuid,@Param("status")String status);

    @Select("select count(*) from tableinfo where tableGuid=#{tableGuid}")
    public int queryTableInfo(@Param("tableGuid")String tableGuid);

    @Insert("insert into tableInfo(tableName,dbName,tableGuid,status)values(#{tableName},#{dbName},#{tableGuid},#{status})")
    public int addTableInfo(RelationEntityV2 entity) throws SQLException;
}
