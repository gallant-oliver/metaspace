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

import io.zeta.metaspace.model.metadata.DataOwner;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryPath;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import io.zeta.metaspace.model.metadata.CategoryEntity;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 19:54
 */
public interface CategoryDAO {


    @Insert("insert into category(guid,name,description,upBrotherCategoryGuid,downBrotherCategoryGuid,parentCategoryGuid,qualifiedName,categoryType,level,safe,tenantid,createtime,creator,code)" +
            "values(#{category.guid},#{category.name},#{category.description},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid},#{category.parentCategoryGuid},#{category.qualifiedName},#{category.categoryType},#{category.level},#{category.safe},#{tenantId},#{category.createTime},#{category.creator},#{category.code})")
    public int add(@Param("category") CategoryEntityV2 category,@Param("tenantId") String tenantId);

    @Select("select count(*) from category where categoryType=#{categoryType} and (tenantid=#{tenantId})")
    public int ifExistCategory(@Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Select("select * from category where categoryType=#{categoryType} and (tenantid=#{tenantId})")
    public Set<CategoryEntityV2> getAll(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId) throws SQLException;

    @Select("select * from category where categoryType=#{categoryType} and tenantid=#{tenantId} and (code=#{code} or (level=#{level} and name=#{name}))")
    public Set<CategoryEntityV2> getCategoryByNameOrCode(@Param("tenantId")String tenantId,@Param("categoryType") int categoryType,@Param("name") String name,@Param("code") String code,@Param("level") int level) throws SQLException;

    @Select("select * from category where guid=#{guid} and tenantid=#{tenantId}")
    public CategoryEntityV2 queryByGuid(@Param("guid") String categoryGuid,@Param("tenantId")String tenantId) throws SQLException;

    @Select("select * from category where categorytype=#{type} and downbrothercategoryguid is null and level=1 and tenantid=#{tenantId}")
    public CategoryEntityV2 getQuery(@Param("type") int type,@Param("tenantId")String tenantId) throws SQLException;

    @Select("select * from category where guid=#{guid} and tenantid=#{tenantId}")
    public CategoryPrivilege queryByGuidV2(@Param("guid") String categoryGuid,@Param("tenantId")String tenantId) throws SQLException;

    @Select("select name from category where guid=#{guid} and tenantid=#{tenantId}")
    public String queryNameByGuid(@Param("guid") String categoryGuid,@Param("tenantId")String tenantId) throws SQLException;

    @Select({" <script>",
             " select count(1) from category where name=#{name} and (tenantid=#{tenantId}) ",
             " <choose>",
             " <when test='parentCategoryGuid != null'>",
             " and parentCategoryGuid=#{parentCategoryGuid}",
             " </when>",
             " <otherwise>",
             " and parentCategoryGuid is null",
             " </otherwise>",
             " </choose>",
             " and categoryType=#{type}",
             " </script>"})
    public int querySameNameNum(@Param("name") String categoryName, @Param("parentCategoryGuid")String parentCategoryGuid, @Param("type")int type,@Param("tenantId") String tenantId);

    @Select("select qualifiedName from category where guid=#{guid} and tenantid=#{tenantId}")
    public String queryQualifiedName(@Param("guid")String guid,@Param("tenantId")String tenantId);

    @Select("select count(*) from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId}")
    public int queryChildrenNum(@Param("parentCategoryGuid")String guid,@Param("tenantId")String tenantId);

    @Select("select guid from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId} and (downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='') ")
    public String queryLastChildCategory(@Param("parentCategoryGuid")String guid,@Param("tenantId")String tenantId);

    @Update("update category set upBrotherCategoryGuid=#{upBrotherCategoryGuid},updater=#{updater},updatetime=#{updateTime} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateUpBrotherCategoryGuid(@Param("guid")String guid, @Param("upBrotherCategoryGuid")String upBrothCatalogGuid,@Param("tenantId")String tenantId,@Param("updater")String updater,@Param("updateTime") Timestamp updateTime);

    @Update("update category set parentcategoryguid=#{parentcategoryguid},upBrotherCategoryGuid=#{upBrotherCategoryGuid},downBrotherCategoryGuid=#{downBrotherCategoryGuid},updater=#{updater},updatetime=#{updateTime} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateParentCategoryGuid(@Param("guid")String guid, @Param("parentcategoryguid")String parentcategoryguid, @Param("upBrotherCategoryGuid")String upBrothCatalogGuid,@Param("downBrotherCategoryGuid")String downBrothCatalogGuid,@Param("tenantId")String tenantId,@Param("updater")String updater,@Param("updateTime") Timestamp updateTime);

    @Update("update category set downBrotherCategoryGuid=#{downBrotherCategoryGuid},updater=#{updater},updatetime=#{updateTime} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateDownBrotherCategoryGuid(@Param("guid")String guid, @Param("downBrotherCategoryGuid")String downBrothCatalogGuid,@Param("tenantId")String tenantId,@Param("updater")String updater,@Param("updateTime") Timestamp updateTime);

    @Update("update category set name=#{category.name},description=#{category.description},qualifiedName=#{category.qualifiedName},safe=#{category.safe},updater=#{updater},updatetime=#{updateTime},code=#{category.code} where guid=#{category.guid} and tenantid=#{tenantId}")
    public int updateCategoryInfo(@Param("category")CategoryEntity category,@Param("tenantId")String tenantId,@Param("updater")String updater,@Param("updateTime") Timestamp updateTime);

    @Delete("delete from category where guid=#{guid} and tenantid=#{tenantId}")
    public int delete(@Param("guid")String guid,@Param("tenantId")String tenantId) throws SQLException;

    @Delete("<script>" +
            "delete from category where tenantid=#{tenantId} and guid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int deleteCategoryByIds(@Param("ids")List<String> ids,@Param("tenantId")String tenantId);

    @Select("select * from category where parentCategoryGuid in (select guid from category where parentCategoryGuid is null) and categoryType=#{categoryType} and tenantid=#{tenantId}")
    public Set<CategoryEntityV2> getAllDepartments(@Param("categoryType") int categoryType,@Param("tenantId") String tenantId) throws SQLException;

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId}" +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid and D.tenantid=#{tenantId}) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryPathByGuid(@Param("guid")String guid,@Param("tenantId") String tenantId);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId} " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid and tenantid=#{tenantId}) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryPathIdsByGuid(@Param("guid")String guid,@Param("tenantId") String tenantId);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId} " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid and tenantid=#{tenantId}) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryGuidPathByGuid(@Param("guid")String guid,@Param("tenantId") String tenantId);





    @Select("WITH RECURSIVE categoryTree AS  " +
            "(SELECT * from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId} " +
            "UNION " +
            "SELECT category.* from categoryTree JOIN category on categoryTree.guid = category.parentCategoryGuid and category.tenantid=#{tenantId}) " +
            "SELECT guid FROM categoryTree")
    public List<String> queryChildrenCategoryId(@Param("parentCategoryGuid")String parentCategoryGuid,@Param("tenantId")String tenantId);

    /*@Delete({"<script>",
             "delete from table_relation where tableGuid=#{tableGuid} and categoryGuid in ",
             "<foreach item='guid' index='index' collection='categoryList' separator=',' open='(' close=')'>" ,
             "#{guid}",
             "</foreach>",
             "</script>"})
    public int deleteChildrenRelation(@Param("tableGuid")String tableGuid, @Param("categoryList")List<String> categoryList);*/

    /*@Select("select * from table_relation where relationShipGuid=#{relationShipGuid}")
    public RelationEntityV2 getRelationByGuid(@Param("relationShipGuid")String relationShipGuid);*/

//    @Select("select guid from category where categoryType=#{categoryType}")
//    public List<String> getAllCategory(@Param("categoryType")int categoryType);

    @Select("select guid from category where categoryType=#{categoryType} and tenantid=#{tenantId}")
    public List<String> getAllCategory(@Param("categoryType") int categoryType, @Param("tenantId")String tenantId);

    @Select("select level from category where guid=#{guid} and tenantid=#{tenantId}")
    public int getCategoryLevel(@Param("guid")String guid, @Param("tenantId")String tenantId);

    /*@Update({" <script>",
             " update tableInfo set dataOwner=#{owners,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg} where tableGuid in",
             "<foreach item='guid' index='index' collection='tables' separator=',' open='(' close=')'>" ,
             "#{guid}",
             "</foreach>",
             "</script>"})
    public int addTableOwners(TableOwner owner) throws SQLException;*/

    @Update({" <script>",
             " insert into table2owner(tableGuid,ownerId,keeper,generateTime,pkid)values",
             " <foreach item='owner' index='index' collection='ownerList' separator=',' close=';'>",
             " (#{owner.tableGuid},#{owner.ownerId},#{owner.keeper},#{owner.generateTime},#{owner.pkId})",
             " </foreach>",
             " </script>"})
    public int addDataOwner(@Param("ownerList")List<DataOwner> ownerList);

    @Update({" <script>",
             " delete from table2owner where tableGuid in",
             " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>" ,
             " #{tableGuid}",
             " </foreach>",
             " </script>"})
    public int deleteDataOwner(@Param("tableList")List<String> tableList);

    @Select("select category.guid from category,table_relation where table_relation.tableguid=#{guid} and table_relation.categoryguid=category.guid and tenantid=#{tenantId}")
    public List<String> getCategoryGuidByTableGuid(@Param("guid")String guid, @Param("tenantId")String tenantId);

    @Select("select category.guid from category,business_relation where business_relation.businessid=#{guid} and business_relation.categoryguid=category.guid and tenantid=#{tenantId}")
    public List<String> getCategoryGuidByBusinessGuid(@Param("guid")String guid, @Param("tenantId")String tenantId);

    @Select("select name from category where guid=#{guid} and tenantid=#{tenantId}")
    public String getCategoryNameById(@Param("guid")String guid, @Param("tenantId")String tenantId);

    @Select("select category.name from category join table_relation on table_relation.categoryguid=category.guid where relationshipguid=#{guid} and tenantid=#{tenantId}")
    public String getCategoryNameByRelationId(@Param("guid")String guid, @Param("tenantId")String tenantId);

    @Insert(" <script>" +
            "INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,categorytype,level,safe,createtime,tenantid,creator,code) VALUES " +
            "<foreach item='category' index='index' collection='categorys' separator='),(' open='(' close=')'>" +
            "#{category.guid},#{category.name},#{category.description},#{category.parentCategoryGuid},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid},#{category.categoryType},#{category.level},#{category.safe},#{category.createTime},#{tenantId},#{category.creator},#{category.code}"+
            "</foreach>" +
            " </script>")
    public int addAll(@Param("categorys") List<CategoryEntityV2> categorys,@Param("tenantId") String tenantId);


    @Select("select name from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId}")
    public List<String> getChildCategoryName(@Param("parentCategoryGuid")String guid,@Param("tenantId")String tenantId);

    @Select("select guid from category where categorytype=#{type} and (downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='') and level=1 and tenantid=#{tenantId}")
    public String queryLastCategory(@Param("type")int type,@Param("tenantId")String tenantId);

    @Select("select name from category where categorytype=#{type} and level=1 and tenantid=#{tenantId}")
    public List<String> getChildCategoryNameByType(@Param("type")int type,@Param("tenantId")String tenantId);

    @Update(" <script>" +
            "update category set upbrothercategoryguid=tmp.upbrothercategoryguid,downbrothercategoryguid=tmp.downbrothercategoryguid,updater=#{updater},updatetime=#{updateTime} " +
            " from (values" +
            " <foreach item='category' index='index' collection='categories' separator='),(' open='(' close=')'>" +
            " #{category.guid},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid}" +
            " </foreach>" +
            " ) as tmp (guid,upbrothercategoryguid,downbrothercategoryguid) where category.guid=tmp.guid and tenantid=#{tenantId}" +
            " </script>")
    public int updateCategoryTree(@Param("categories")List<RoleModulesCategories.Category> categories,@Param("tenantId")String tenantId,@Param("updater")String updater,@Param("updateTime") Timestamp updateTime);

    @Select("select guid,name,description,level from category where guid=#{guid} and tenantid=#{tenantId}")
    public RoleModulesCategories.Category getCategoryByGuid(@Param("guid") String categoryGuid,@Param("tenantId")String tenantId);

    @Update(" <script>" +
            "update category set upbrothercategoryguid=tmp.upbrothercategoryguid,downbrothercategoryguid=tmp.downbrothercategoryguid,updater=#{updater},updatetime=#{updateTime} " +
            " from (values" +
            " <foreach item='category' index='index' collection='categories' separator='),(' open='(' close=')'>" +
            " #{category.guid},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid}" +
            " </foreach>" +
            " ) as tmp (guid,upbrothercategoryguid,downbrothercategoryguid) where category.guid=tmp.guid and tenantid=#{tenantId}; " +
            " </script>")
    public int updateCategoryEntityV2Tree(@Param("categories")List<CategoryEntityV2> categories,@Param("tenantId")String tenantId,@Param("updater")String updater,@Param("updateTime") Timestamp updateTime);

    @Select(" select count(1) from category where name=#{name} " +
            " and categorytype=#{type} and level=1 and tenantid=#{tenantId}")
    public int querySameNameOne(@Param("name") String categoryName, @Param("type")int type, @Param("tenantId")String tenantId);

    @Select(" WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            " (SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            " FROM category WHERE (parentCategoryGuid IS NULL OR parentCategoryGuid='') and categorytype=#{categoryType} and tenantid=#{tenantId}" +
            " UNION ALL " +
            " SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            " FROM category D JOIN T ON D.parentCategoryGuid = T.guid where D.categorytype=#{categoryType} and D.categorytype=#{categoryType} and D.tenantid=#{tenantId}) " +
            " SELECT  guid,PATH path FROM T " +
            " ORDER BY PATH")
    public List<CategoryPath> getPath(@Param("categoryType") int categoryType, @Param("tenantId")String tenantId);

    @Select(" <script>" +
            " WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            " (SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            " FROM category WHERE (parentCategoryGuid IS NULL OR parentCategoryGuid='') and categorytype=#{categoryType} and tenantid=#{tenantId} " +
            " UNION ALL " +
            " SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            " FROM category D JOIN T ON D.parentCategoryGuid = T.guid where D.categorytype=#{categoryType} and D.tenantid=#{tenantId}) " +
            " SELECT  guid,PATH path FROM T where guid in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            " ORDER BY PATH" +
            " </script>")
    public List<CategoryPath> getPathByIds(@Param("ids")List<String> ids,@Param("categoryType") int categoryType, @Param("tenantId")String tenantId);

}
