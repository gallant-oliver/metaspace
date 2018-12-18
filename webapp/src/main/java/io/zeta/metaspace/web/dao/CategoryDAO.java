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

import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 19:54
 */
public interface CategoryDAO {

    @Insert("insert into table_category(guid,name,description,upBrotherCategoryGuid,downBrotherCategoryGuid,parentCategoryGuid,qualifiedName)" +
            "values(#{guid},#{name},#{description},#{upBrotherCategoryGuid},#{downBrotherCategoryGuid},#{parentCategoryGuid},#{qualifiedName})")
    public int add(CategoryEntityV2 category);

    @Select("select * from table_category")
    public Set<CategoryEntityV2> getAll();

    @Select("select * from table_category where guid=#{guid}")
    public CategoryEntityV2 queryByGuid(@Param("guid") String categoryGuid);

    @Select("select count(*) from table_category where qualifiedName=#{qualifiedName}")
    public int queryQualifiedNameNum(@Param("qualifiedName")String qualifiedName);

    @Select("select qualifiedName from table_category where guid=#{guid}")
    public String queryQualifiedName(@Param("guid")String guid);

    @Select("select count(*) from table_category where parentCategoryGuid=#{parentCategoryGuid}")
    public int queryChildrenNum(@Param("parentCategoryGuid")String guid);

    @Select("select guid from table_category where parentCategoryGuid=#{parentCategoryGuid} and downBrotherCategoryGuid is NULL")
    public String queryLastChildCatalog(@Param("parentCategoryGuid")String guid);

    @Update("update table_category set upBrotherCategoryGuid=#{upBrotherCategoryGuid} where guid=#{guid}")
    public int updateUpBrothCatalogGuid(@Param("guid")String guid, @Param("upBrotherCategoryGuid")String upBrothCatalogGuid);

    @Update("update table_category set downBrotherCategoryGuid=#{downBrotherCategoryGuid} where guid=#{guid}")
    public int updateDownBrothCatalogGuid(@Param("guid")String guid, @Param("downBrotherCategoryGuid")String downBrothCatalogGuid);

    @Update("update table_category set name=#{name},description=#{description},qualifiedName=#{qualifiedName} where guid=#{guid}")
    public int updateCatalogInfo(CategoryEntity category);

    @Delete("delete from table_category where guid=#{guid}")
    public int delete(@Param("guid")String guid);
}
