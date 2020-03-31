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
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import io.zeta.metaspace.model.metadata.CategoryEntity;
import org.springframework.security.access.method.P;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/19 19:54
 */
public interface CategoryDAO {


    @Insert("insert into category(guid,name,description,upBrotherCategoryGuid,downBrotherCategoryGuid,parentCategoryGuid,qualifiedName,categoryType,level,safe,tenantid)" +
            "values(#{category.guid},#{category.name},#{category.description},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid},#{category.parentCategoryGuid},#{category.qualifiedName},#{category.categoryType},#{category.level},#{category.safe},#{tenantId})")
    public int add(@Param("category") CategoryEntityV2 category,@Param("tenantId") String tenantId);

    @Select("select count(*) from category where categoryType=#{categoryType} and (tenantid=#{tenantId} or tenantid='all')")
    public int ifExistCategory(@Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Select("select * from category where categoryType=#{categoryType} and (tenantid=#{tenantId} or tenantid='all')")
    public Set<CategoryEntityV2> getAll(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId) throws SQLException;

    @Select("select * from category where guid=#{guid}")
    public CategoryEntityV2 queryByGuid(@Param("guid") String categoryGuid) throws SQLException;

    @Select("select * from category where categorytype=#{type} and downbrothercategoryguid is null and level=1")
    public CategoryEntityV2 getQuery(@Param("type") int type) throws SQLException;

    @Select("select * from category where guid=#{guid}")
    public CategoryPrivilege queryByGuidV2(@Param("guid") String categoryGuid) throws SQLException;

    @Select("select name from category where guid=#{guid}")
    public String queryNameByGuid(@Param("guid") String categoryGuid) throws SQLException;

    @Select({" <script>",
             " select count(1) from category where name=#{name} and (tenantid=#{tenantId} or tenantid='all') ",
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

    @Select("select qualifiedName from category where guid=#{guid}")
    public String queryQualifiedName(@Param("guid")String guid);

    @Select("select count(*) from category where parentCategoryGuid=#{parentCategoryGuid}")
    public int queryChildrenNum(@Param("parentCategoryGuid")String guid);

    @Select("select guid from category where parentCategoryGuid=#{parentCategoryGuid} and (tenantid='all' or tenantid=#{tenantId}) and downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='' ")
    public String queryLastChildCategory(@Param("parentCategoryGuid")String guid,@Param("tenantId")String tenantId);

    @Update("update category set upBrotherCategoryGuid=#{upBrotherCategoryGuid} where guid=#{guid}")
    public int updateUpBrotherCategoryGuid(@Param("guid")String guid, @Param("upBrotherCategoryGuid")String upBrothCatalogGuid);

    @Update("update category set downBrotherCategoryGuid=#{downBrotherCategoryGuid} where guid=#{guid}")
    public int updateDownBrotherCategoryGuid(@Param("guid")String guid, @Param("downBrotherCategoryGuid")String downBrothCatalogGuid);

    @Update("update category set name=#{name},description=#{description},qualifiedName=#{qualifiedName},safe=#{safe} where guid=#{guid}")
    public int updateCategoryInfo(CategoryEntity category);

    @Delete("delete from category where guid=#{guid}")
    public int delete(@Param("guid")String guid) throws SQLException;

    @Select("select * from category where parentCategoryGuid in (select guid from category where parentCategoryGuid is null) and categoryType=#{categoryType} and (tenantid=#{tenantId} or tenantid='all')")
    public Set<CategoryEntityV2> getAllDepartments(@Param("categoryType") int categoryType,@Param("tenantId") String tenantId) throws SQLException;

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
            "(SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryPathIdsByGuid(@Param("guid")String guid);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryGuidPathByGuid(@Param("guid")String guid);


    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid) " +
            "SELECT  DEPTH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public int queryLevelByGuid(@Param("guid")String guid);


    @Select("WITH RECURSIVE categoryTree AS  " +
            "(SELECT * from category where parentCategoryGuid=#{parentCategoryGuid} " +
            "UNION " +
            "SELECT category.* from categoryTree JOIN category on categoryTree.guid = category.parentCategoryGuid) " +
            "SELECT guid FROM categoryTree")
    public List<String> queryChildrenCategoryId(@Param("parentCategoryGuid")String parentCategoryGuid);

    @Delete({"<script>",
             "delete from table_relation where tableGuid=#{tableGuid} and categoryGuid in ",
             "<foreach item='guid' index='index' collection='categoryList' separator=',' open='(' close=')'>" ,
             "#{guid}",
             "</foreach>",
             "</script>"})
    public int deleteChildrenRelation(@Param("tableGuid")String tableGuid, @Param("categoryList")List<String> categoryList);

    @Select("select * from table_relation where relationShipGuid=#{relationShipGuid}")
    public RelationEntityV2 getRelationByGuid(@Param("relationShipGuid")String relationShipGuid);

//    @Select("select guid from category where categoryType=#{categoryType}")
//    public List<String> getAllCategory(@Param("categoryType")int categoryType);

    @Select("select guid from category where categoryType=#{categoryType} and (tenantid=#{tenantId} or tenantid='all')")
    public List<String> getAllCategory(@Param("categoryType") int categoryType, @Param("tenantId")String tenantId);

    @Select("select level from category where guid=#{guid}")
    public int getCategoryLevel(@Param("guid")String guid);

    @Update({" <script>",
             " update tableInfo set dataOwner=#{owners,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg} where tableGuid in",
             "<foreach item='guid' index='index' collection='tables' separator=',' open='(' close=')'>" ,
             "#{guid}",
             "</foreach>",
             "</script>"})
    public int addTableOwners(TableOwner owner) throws SQLException;

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

    @Select("select category.guid from category,table_relation where table_relation.tableguid=#{guid} and table_relation.categoryguid=category.guid")
    public List<String> getCategoryGuidByTableGuid(String guid);

    @Select("select name from category where guid=#{guid}")
    public String getCategoryNameById(String guid);

    @Select("select category.name from category join table_relation on table_relation.categoryguid=category.guid where relationshipguid=#{guid}")
    public String getCategoryNameByRelationId(String guid);

}
