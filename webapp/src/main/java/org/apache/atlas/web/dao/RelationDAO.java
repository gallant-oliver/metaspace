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
package org.apache.atlas.web.dao;

import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/21 10:59
 */
public interface RelationDAO {
    @Insert("insert into table_relation(relationshipGuid,categoryGuid,tableName,dbName,tableGuid,path,status)" +
            "values(#{relationshipGuid},#{categoryGuid},#{tableName},#{dbName},#{tableGuid},#{path},#{status})")
    public int add(RelationEntityV2 entity);

    @Delete("delete from table_relation where relationshipGuid=#{relationshipGuid}")
    public int delete(@Param("relationshipGuid")String guid);

    @Select("select * from table_relation where categoryGuid=#{categoryGuid}")
    public RelationEntityV2 query(@Param("categoryGuid")String categoryGuid);

    @Select("select * from table_relation where categoryGuid=#{categoryGuid} limit #{limit} offset #{offset}")
    public List<RelationEntityV2> queryRelationByCategoryGuidByLimit(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit,@Param("offset") int offset);

    @Select("select * from table_relation where categoryGuid=#{categoryGuid}")
    public List<RelationEntityV2> queryRelationByCategoryGuid(@Param("categoryGuid")String categoryGuid);

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryTotalNumByCategoryGuid(@Param("categoryGuid")String categoryGuid);


    @Select("select * from table_relation where tableName like '%${tableName}%' limit #{limit} offset #{offset}")
    public List<RelationEntityV2> queryByTableName(@Param("tableName")String tableName, @Param("limit")int limit,@Param("offset") int offset);

    @Select("select count(*) from table_relation where tableName like '%${tableName}%'")
    public int queryTotalNumByName(@Param("tableName")String tableName);

    @Select("select count(*) from table_relation where categoryGuid=#{categoryGuid}")
    public int queryRelationNumByCatalogGuid(@Param("categoryGuid")String categoryGuid);


}
