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
 * @date 2018/11/19 19:54
 */
package io.zeta.metaspace.web.dao;

import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import io.zeta.metaspace.model.metadata.CategoryEntity;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 19:54
 */
public interface CategoryDAO {


    @Insert("insert into category(guid,name,description,upBrotherCategoryGuid,downBrotherCategoryGuid,parentCategoryGuid,qualifiedName,categoryType)" +
            "values(#{guid},#{name},#{description},#{upBrotherCategoryGuid},#{downBrotherCategoryGuid},#{parentCategoryGuid},#{qualifiedName},#{categoryType})")
    public int add(CategoryEntityV2 category);

    @Select("select * from category where categoryType=#{categoryType}")
    public Set<CategoryEntityV2> getAll(@Param("categoryType") int categoryType) throws SQLException;

    @Select("select * from category where guid=#{guid}")
    public CategoryEntityV2 queryByGuid(@Param("guid") String categoryGuid) throws SQLException;

    @Select("select name from category where guid=#{guid}")
    public String queryNameByGuid(@Param("guid") String categoryGuid) throws SQLException;

    @Select("select count(*) from category where qualifiedName=#{qualifiedName}")
    public int queryQualifiedNameNum(@Param("qualifiedName")String qualifiedName);

    @Select("select qualifiedName from category where guid=#{guid}")
    public String queryQualifiedName(@Param("guid")String guid);

    @Select("select count(*) from category where parentCategoryGuid=#{parentCategoryGuid}")
    public int queryChildrenNum(@Param("parentCategoryGuid")String guid);

    @Select("select guid from category where parentCategoryGuid=#{parentCategoryGuid} and downBrotherCategoryGuid is NULL")
    public String queryLastChildCategory(@Param("parentCategoryGuid")String guid);

    @Update("update category set upBrotherCategoryGuid=#{upBrotherCategoryGuid} where guid=#{guid}")
    public int updateUpBrotherCategoryGuid(@Param("guid")String guid, @Param("upBrotherCategoryGuid")String upBrothCatalogGuid);

    @Update("update category set downBrotherCategoryGuid=#{downBrotherCategoryGuid} where guid=#{guid}")
    public int updateDownBrotherCategoryGuid(@Param("guid")String guid, @Param("downBrotherCategoryGuid")String downBrothCatalogGuid);

    @Update("update category set name=#{name},description=#{description},qualifiedName=#{qualifiedName} where guid=#{guid}")
    public int updateCategoryInfo(CategoryEntity category);

    @Delete("delete from category where guid=#{guid}")
    public int delete(@Param("guid")String guid) throws SQLException;

    @Select("select * from category where parentCategoryGuid in (select guid from category where parentCategoryGuid is null) and categoryType=#{categoryType}")
    public Set<CategoryEntityV2> getAllDepartments(@Param("categoryType") int categoryType) throws SQLException;

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryPathByGuid(@Param("guid")String guid);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid) " +
            "SELECT  DEPTH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public int queryLevelByGuid(@Param("guid")String guid);
}
