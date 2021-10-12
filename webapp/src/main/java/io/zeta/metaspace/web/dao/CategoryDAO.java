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

import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.DataOwner;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.CategoryGuidPath;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.atlas.model.metadata.CategoryPath;
import org.apache.ibatis.annotations.*;

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


    @Insert("insert into category(private_status,guid,name,description,upBrotherCategoryGuid,downBrotherCategoryGuid,parentCategoryGuid,qualifiedName,categoryType,level,safe,tenantid,createtime,creator,code,sort,publish,information,approval_id)" +
            "values(#{category.privateStatus},#{category.guid},#{category.name},#{category.description},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid},#{category.parentCategoryGuid},#{category.qualifiedName},#{category.categoryType},#{category.level},#{category.safe},#{tenantId},#{category.createTime},#{category.creator},#{category.code},#{category.sort}," +
            "#{category.publish},#{category.information},#{category.approvalId})")
    public int add(@Param("category") CategoryEntityV2 category, @Param("tenantId") String tenantId);

    @Select("select count(*) from category where categoryType=#{categoryType} and (tenantid=#{tenantId})")
    public int ifExistCategory(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    @Select("select * from category where categoryType=#{categoryType} and (tenantid=#{tenantId})")
    public Set<CategoryEntityV2> getAll(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId) throws SQLException;

    @Select("select * from category where categoryType=#{categoryType} and tenantid=#{tenantId} and (code=#{code} or (level=#{level} and name=#{name}))")
    public Set<CategoryEntityV2> getCategoryByNameOrCode(@Param("tenantId") String tenantId, @Param("categoryType") int categoryType, @Param("name") String name, @Param("code") String code, @Param("level") int level) throws SQLException;

    @Select("select * from category where guid=#{guid} and tenantid=#{tenantId}")
    public CategoryEntityV2 queryByGuid(@Param("guid") String categoryGuid, @Param("tenantId") String tenantId) throws SQLException;

    @Select({"<script>" ,
            " SELECT DISTINCT guid,name FROM category INNER JOIN category_group_relation AS cgr ON category.guid = cgr.category_id WHERE ",
            " category.tenantid = #{tenantId} AND categorytype = 5 AND cgr.READ = TRUE AND cgr.edit_item = TRUE ",
            " AND category.NAME in ",
            " <foreach item='item' index='index' collection='name' open='(' separator=',' close=')'>" ,
            " #{item}" ,
            " </foreach>" ,
            " <if test='userGroupIds.size > 0 '>",
            " AND cgr.group_id in ",
            " <foreach item='item' index='index' collection='userGroupIds' open='(' separator=',' close=')'>" ,
            " #{item}" ,
            " </foreach>" ,
            " </if>",
            " </script>"})
    List<CategoryEntityV2> selectGuidByTenantIdAndGroupIdAndName(@Param("name") Set<String> name, @Param("tenantId") String tenantId, @Param("userGroupIds") List<String> userGroupIds);

    @Select({"<script>" +
            " select * from category where tenantid=#{tenantId} and guid in" +
            " <foreach item='guid' index='index' collection='categoryGuid' open='(' separator=',' close=')'>" +
            " #{guid}" +
            " </foreach>" +
            " </script>"})
    public List<CategoryEntityV2> queryCategoryEntitysByGuids(@Param("categoryGuid") List<String> categoryGuid, @Param("tenantId") String tenantId) throws SQLException;

    @Select("select * from category where categorytype=#{type} and downbrothercategoryguid is null and level=1 and tenantid=#{tenantId}")
    public CategoryEntityV2 getQuery(@Param("type") int type, @Param("tenantId") String tenantId) throws SQLException;

    @Select("select * from category where guid=#{guid} and tenantid=#{tenantId}")
    public CategoryPrivilege queryByGuidV2(@Param("guid") String categoryGuid, @Param("tenantId") String tenantId) throws SQLException;

    @Select("select * from category where tenantid=#{tenantId} and guid = (select category_id from source_info where version = 0 and database_id  = #{databaseId} and tenant_id = #{tenantId} and category_id is not null)")
    public CategoryPrivilege queryByGuidByDBId(@Param("databaseId") String databaseId, @Param("tenantId") String tenantId);

    @Select("select category_id from source_info where version = 0 and database_id  = #{databaseId} and tenant_id = #{tenantId} and category_id is not null")
    public String queryCategoryIdByGuidByDBId(@Param("databaseId") String databaseId, @Param("tenantId") String tenantId);

    @Select("select name from category where guid=#{guid} and tenantid=#{tenantId}")
    public String queryNameByGuid(@Param("guid") String categoryGuid, @Param("tenantId") String tenantId) throws SQLException;

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
    public int querySameNameNum(@Param("name") String categoryName, @Param("parentCategoryGuid") String parentCategoryGuid, @Param("type") int type, @Param("tenantId") String tenantId);

    @Select("select qualifiedName from category where guid=#{guid} and tenantid=#{tenantId}")
    public String queryQualifiedName(@Param("guid") String guid, @Param("tenantId") String tenantId);

    @Select("select count(*) from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId}")
    public int queryChildrenNum(@Param("parentCategoryGuid") String guid, @Param("tenantId") String tenantId);

    @Select("select guid from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId} and (downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='') ")
    public String queryLastChildCategory(@Param("parentCategoryGuid") String guid, @Param("tenantId") String tenantId);

    @Update("update category set upBrotherCategoryGuid=#{upBrotherCategoryGuid} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateUpBrotherCategoryGuid(@Param("guid") String guid, @Param("upBrotherCategoryGuid") String upBrothCatalogGuid, @Param("tenantId") String tenantId);

    @Update("update category set parentcategoryguid=#{parentcategoryguid},upBrotherCategoryGuid=#{upBrotherCategoryGuid},downBrotherCategoryGuid=#{downBrotherCategoryGuid} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateParentCategoryGuid(@Param("guid") String guid, @Param("parentcategoryguid") String parentcategoryguid, @Param("upBrotherCategoryGuid") String upBrothCatalogGuid, @Param("downBrotherCategoryGuid") String downBrothCatalogGuid, @Param("tenantId") String tenantId);

    @Update("update category set downBrotherCategoryGuid=#{downBrotherCategoryGuid} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateDownBrotherCategoryGuid(@Param("guid") String guid, @Param("downBrotherCategoryGuid") String downBrothCatalogGuid, @Param("tenantId") String tenantId);

    @Update("update category set name=#{category.name},description=#{category.description},qualifiedName=#{category.qualifiedName},safe=#{category.safe},updater=#{updater},updatetime=#{updateTime},code=#{category.code} where guid=#{category.guid} and tenantid=#{tenantId}")
    public int updateCategoryInfo(@Param("category") CategoryEntity category, @Param("tenantId") String tenantId, @Param("updater") String updater, @Param("updateTime") Timestamp updateTime);

    @Select("select * from category where categoryType=#{categoryType} and tenantid=#{tenantId} and guid<>#{guid} and (code=#{code} or (level=#{level} and name=#{name}))")
    public Set<CategoryEntityV2> getOtherCategoryByCodeOrName(@Param("tenantId") String tenantId, @Param("guid") String guid, @Param("categoryType") int categoryType, @Param("name") String name, @Param("code") String code, @Param("level") int level) throws SQLException;

    @Select("<script>" +
            "select * from category where tenantid=#{tenantId} and categoryType=#{categoryType} and code in " +
            " <foreach item='code' index='index' collection='codes' separator=',' open='(' close=')'>" +
            " #{code} " +
            " </foreach>" +
            " </script>")
    public List<CategoryEntityV2> getCategoryByCodes(@Param("codes") List<String> codes, @Param("tenantId") String tenantId, @Param("categoryType") int categoryType);

    @Select("select * from category where tenantid=#{tenantId} and categoryType=#{categoryType} and code=#{code} ")
    public CategoryEntityV2 getCategoryByCode(@Param("code") String code, @Param("tenantId") String tenantId, @Param("categoryType") int categoryType);

    @Delete("delete from category where guid=#{guid} and tenantid=#{tenantId}")
    public int delete(@Param("guid") String guid, @Param("tenantId") String tenantId) throws SQLException;

    @Delete("delete FROM category WHERE categorytype = 0")
    int deleteTechnicalCategory();

    @Delete("<script>" +
            "delete from category where tenantid=#{tenantId} and guid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int deleteCategoryByIds(@Param("ids") List<String> ids, @Param("tenantId") String tenantId);

    @Select("select * from category where parentCategoryGuid in (select guid from category where parentCategoryGuid is null) and categoryType=#{categoryType} and tenantid=#{tenantId}")
    public Set<CategoryEntityV2> getAllDepartments(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId) throws SQLException;

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId}" +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid and D.tenantid=#{tenantId}) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryPathByGuid(@Param("guid") String guid, @Param("tenantId") String tenantId);

    @Select("<script>" +
            " WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            " (SELECT guid,name,parentCategoryGuid, ARRAY[name] AS PATH, 1 AS DEPTH" +
            " FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId} AND categorytype = 0" +
            " UNION ALL " +
            " SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.name, T.DEPTH + 1 AS DEPTH" +
            " FROM category D JOIN T ON D.parentCategoryGuid = T.guid and D.tenantid= #{tenantId} AND categorytype = 0)" +
            " SELECT  PATH,guid FROM T WHERE guid in " +
            " <foreach item='guid' index='index' collection='list' separator=',' open='(' close=')'>" +
            "   #{guid} " +
            " </foreach>" +
            " ORDER BY PATH" +
            "</script>")
    Set<CategoryEntityV2> queryPathByGuidAndType(@Param("list") List<String> guid, @Param("tenantId") String tenantId);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId} " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid and tenantid=#{tenantId}) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryPathIdsByGuid(@Param("guid") String guid, @Param("tenantId") String tenantId);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            "(SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            "FROM category WHERE parentCategoryGuid IS NULL and tenantid=#{tenantId} " +
            "UNION ALL " +
            "SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            "FROM category D JOIN T ON D.parentCategoryGuid = T.guid and tenantid=#{tenantId}) " +
            "SELECT  PATH FROM T WHERE guid=#{guid} " +
            "ORDER BY PATH")
    public String queryGuidPathByGuid(@Param("guid") String guid, @Param("tenantId") String tenantId);


    @Select("WITH RECURSIVE categoryTree AS  " +
            "(SELECT * from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId} " +
            "UNION " +
            "SELECT category.* from categoryTree JOIN category on categoryTree.guid = category.parentCategoryGuid and category.tenantid=#{tenantId}) " +
            "SELECT guid FROM categoryTree")
    public List<String> queryChildrenCategoryId(@Param("parentCategoryGuid") String parentCategoryGuid, @Param("tenantId") String tenantId);

    @Select("select guid from category where categoryType=#{categoryType} and tenantid=#{tenantId}")
    public List<String> getAllCategory(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    @Select("select * from category where categoryType=#{categoryType} and tenantid=#{tenantId} and level=#{level}")
    public List<CategoryEntityV2> getAllCategoryByLevel(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId, @Param("level") int level);

    @Select("select level from category where guid=#{guid} and tenantid=#{tenantId}")
    public int getCategoryLevel(@Param("guid") String guid, @Param("tenantId") String tenantId);

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
    public int addDataOwner(@Param("ownerList") List<DataOwner> ownerList);

    @Update({" <script>",
            " delete from table2owner where tableGuid in",
            " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>",
            " #{tableGuid}",
            " </foreach>",
            " </script>"})
    public int deleteDataOwner(@Param("tableList") List<String> tableList);

    @Select("select t1.category_id from table_data_source_relation t1 where t1.tenant_id = #{tenantId} and t1.table_id = #{guid} " +
            "union " +
            "select t2.category_id from  source_info t2 join tableinfo t3 on t2.database_id = t3.databaseguid where t2.tenant_id = #{tenantId} and t3.tableguid = #{guid}")
    public List<String> getCategoryGuidByTableGuid(@Param("guid") String guid, @Param("tenantId") String tenantId);

    @Select("select category.guid from category,business_relation where business_relation.businessid=#{guid} and business_relation.categoryguid=category.guid and tenantid=#{tenantId}")
    public List<String> getCategoryGuidByBusinessGuid(@Param("guid") String guid, @Param("tenantId") String tenantId);

    @Select("select name from category where guid=#{guid} and tenantid=#{tenantId}")
    public String getCategoryNameById(@Param("guid") String guid, @Param("tenantId") String tenantId);
    @Insert(" <script>" +
            "INSERT INTO category(guid,name,description,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid,qualifiedname,categorytype,level,safe,createtime,tenantid,creator,code,sort,private_status,publish) VALUES " +
            "<foreach item='category' index='index' collection='categorys' separator='),(' open='(' close=')'>" +
            "#{category.guid},#{category.name},#{category.description},#{category.parentCategoryGuid},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid},#{category.qualifiedName},#{category.categoryType},#{category.level},#{category.safe},#{category.createTime},#{tenantId},#{category.creator},#{category.code},#{category.sort},#{category.privateStatus},#{category.publish}" +
            "</foreach>" +
            " </script>")
    public int addAll(@Param("categorys") List<CategoryEntityV2> categorys, @Param("tenantId") String tenantId);


    @Select("select name from category where parentCategoryGuid=#{parentCategoryGuid} and tenantid=#{tenantId}")
    public List<String> getChildCategoryName(@Param("parentCategoryGuid") String guid, @Param("tenantId") String tenantId);

    @Select("select guid from category where categorytype=#{type} and (downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='') and level=1 and tenantid=#{tenantId}")
    public String queryLastCategory(@Param("type") int type, @Param("tenantId") String tenantId);

    @Select({" <script>",
            " select * from category where categorytype=#{type} and (downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='') and (tenantid=#{tenantId}) ",
            " <choose>",
            " <when test='parentCategoryGuid != null'>",
            " and parentCategoryGuid=#{parentCategoryGuid}",
            " </when>",
            " <otherwise>",
            " and parentCategoryGuid is null",
            " </otherwise>",
            " </choose>",
            " </script>"})
    public CategoryEntityV2 getLastCategory(@Param("parentCategoryGuid") String parentCategoryGuid, @Param("type") int type, @Param("tenantId") String tenantId);

    @Select("select name from category where categorytype=#{type} and level=1 and tenantid=#{tenantId}")
    public List<String> getChildCategoryNameByType(@Param("type") int type, @Param("tenantId") String tenantId);

    @Update(" <script>" +
            "update category set upbrothercategoryguid=tmp.upbrothercategoryguid,downbrothercategoryguid=tmp.downbrothercategoryguid " +
            " from (values" +
            " <foreach item='category' index='index' collection='categories' separator='),(' open='(' close=')'>" +
            " #{category.guid},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid}" +
            " </foreach>" +
            " ) as tmp (guid,upbrothercategoryguid,downbrothercategoryguid) where category.guid=tmp.guid and tenantid=#{tenantId}" +
            " </script>")
    public int updateCategoryTree(@Param("categories") List<RoleModulesCategories.Category> categories, @Param("tenantId") String tenantId);

    @Select("select guid,name,description,level from category where guid=#{guid} and tenantid=#{tenantId}")
    public RoleModulesCategories.Category getCategoryByGuid(@Param("guid") String categoryGuid, @Param("tenantId") String tenantId);

    @Update(" <script>" +
            "update category set upbrothercategoryguid=tmp.upbrothercategoryguid,downbrothercategoryguid=tmp.downbrothercategoryguid " +
            " from (values" +
            " <foreach item='category' index='index' collection='categories' separator='),(' open='(' close=')'>" +
            " #{category.guid},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid}" +
            " </foreach>" +
            " ) as tmp (guid,upbrothercategoryguid,downbrothercategoryguid) where category.guid=tmp.guid and tenantid=#{tenantId}; " +
            " </script>")
    public int updateCategoryEntityV2Tree(@Param("categories") List<CategoryEntityV2> categories, @Param("tenantId") String tenantId);

    @Update(" <script>" +
            "update category set downbrothercategoryguid=tmp.downbrothercategoryguid " +
            " from (values" +
            " <foreach item='category' index='index' collection='categories' separator='),(' open='(' close=')'>" +
            " #{category.guid},#{category.downBrotherCategoryGuid} " +
            " </foreach>" +
            " ) as tmp (guid,downbrothercategoryguid) where category.guid=tmp.guid and tenantid=#{tenantId}; " +
            " </script>")
    public int updateCategoryEntityV2(@Param("categories") List<CategoryEntityV2> categories, @Param("tenantId") String tenantId);

    @Select(" select count(1) from category where name=#{name} " +
            " and categorytype=#{type} and level=1 and tenantid=#{tenantId}")
    public int querySameNameOne(@Param("name") String categoryName, @Param("type") int type, @Param("tenantId") String tenantId);

    @Select(" WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH, DEPTH)  AS" +
            " (SELECT guid,name,parentCategoryGuid, ARRAY[guid] AS PATH, 1 AS DEPTH " +
            " FROM category WHERE (parentCategoryGuid IS NULL OR parentCategoryGuid='') and categorytype=#{categoryType} and tenantid=#{tenantId}" +
            " UNION ALL " +
            " SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH || D.guid, T.DEPTH + 1 AS DEPTH " +
            " FROM category D JOIN T ON D.parentCategoryGuid = T.guid where D.categorytype=#{categoryType} and D.categorytype=#{categoryType} and D.tenantid=#{tenantId}) " +
            " SELECT  guid,PATH path FROM T " +
            " ORDER BY PATH")
    public List<CategoryPath> getPath(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

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
    public List<CategoryPath> getPathByIds(@Param("ids") List<String> ids, @Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    @Select("SELECT COUNT(1) FROM category WHERE guid = #{id} AND tenantid = #{tenantId}")
    int getCategoryCountById(@Param("id") String id, @Param("tenantId") String tenantId);

    @Select({" <script> ",
            " select distinct c.guid from category c join category_group_relation cgr on c.guid=cgr.category_id where c.categorytype=#{categoryType} and c.tenantid=#{tenantId} and cgr.group_id in  ",
            " <foreach item='groupId' index='index' collection='groupIds' separator=',' open='(' close=')'>",
            " #{groupId} ",
            " </foreach>",
            " </script>"})
    List<String> getCategorysByGroup(@Param("groupIds") List<String> groupIds, @Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    @Update("UPDATE category SET name=#{name}, qualifiedname = #{name},private_status = #{privateStatus} WHERE guid = #{id}")
    void updateCategoryName(@Param("name") String databaseAlias, @Param("id") String id,@Param("privateStatus") String privateStatus);
    @Select("SELECT COUNT(1) FROM category WHERE parentcategoryguid = #{parentId} AND tenantid = #{tenantId} AND name = #{databaseAlias}")
    int getCategoryCountByParentIdAndName(@Param("tenantId") String tenantId, @Param("parentId") String parentId, @Param("databaseAlias") String databaseAlias);

    @Select("SELECT COUNT(1) FROM category WHERE parentcategoryguid = (SELECT parentcategoryguid FROM category WHERE guid = #{categoryId}) AND tenantid = #{tenantId} AND name = #{databaseAlias}")
    int getCategoryCountByIdAndName(@Param("tenantId") String tenantId, @Param("categoryId") String categoryId, @Param("databaseAlias") String databaseAlias);

    @Select("SELECT guid,name,parentcategoryguid as parentCategoryGuid FROM public.category where tenantid=#{tenantId} ")
    List<CategoryEntityV2> queryByTenantId(@Param("tenantId") String tenantId);

    @Select(" select distinct guid,name from category where categorytype = #{categorytype} order by guid ")
    List<CategoryEntityV2> queryNameByType(@Param("categorytype") int categorytype);

    @Select(" select parentcategoryguid from category where guid = #{id} AND tenantid = #{tenant} ")
    String getParentIdByGuid(@Param("id") String guid, @Param("tenant") String tenant);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH)  AS" +
            " (SELECT guid,name,parentCategoryGuid, name as  PATH" +
            " FROM category WHERE parentCategoryGuid IS NULL and tenantid= #{tenantId} and categorytype = #{type}" +
            " UNION ALL " +
            " SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH ||'/'|| D.name" +
            " FROM category D JOIN T ON D.parentCategoryGuid = T.guid and D.tenantid=#{tenantId})" +
            "SELECT  * FROM T ")
    List<CategoryGuidPath> getGuidPathByTenantIdAndCategoryType(@Param("tenantId") String tenantId, @Param("type") int type);

    @Select("WITH RECURSIVE T(guid, name, parentCategoryGuid, PATH)  AS" +
            " (SELECT guid,name,parentCategoryGuid, name as  PATH" +
            " FROM category WHERE parentCategoryGuid IS NULL and tenantid= #{tenantId} and categorytype = #{type}" +
            " UNION ALL " +
            " SELECT D.guid, D.name, D.parentCategoryGuid, T.PATH ||'/'|| D.name" +
            " FROM category D JOIN T ON D.parentCategoryGuid = T.guid and D.tenantid=#{tenantId})" +
            "SELECT  * FROM T where guid = #{guid}")
    List<CategoryGuidPath> getGuidPathByTenantIdAndCategoryTypeAndId(@Param("tenantId") String tenantId, @Param("type") int type, @Param("guid") String guid);

    @Select("<script>" +
            " SELECT category.*,COALESCE(relation.edit_category,false,false) AS edit  FROM category LEFT JOIN category_group_relation as relation on category.guid = relation.category_id WHERE tenantid = #{tenantId} AND private_status = 'PUBLIC' AND categorytype = 0" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT category.*,COALESCE(relation.edit_category,false,false) AS edit FROM category INNER JOIN category_group_relation as relation on category.guid = relation.category_id " +
            " WHERE tenantid = #{tenantId} AND level &lt;= #{maxLevel} AND private_status = 'PRIVATE' AND categorytype = 0 AND group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            "</script>")
    List<CategoryPrivilege> selectListByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("creator") String creator, @Param("groupIdList") List<String> groupIdList,@Param("maxLevel") int maxLevel);

    @Select("<script>" +
            " SELECT * FROM category WHERE tenantid = #{tenantId} AND private_status = 'PUBLIC' AND categorytype = 0" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT category.* FROM category INNER JOIN category_group_relation as relation on category.guid = relation.category_id " +
            " WHERE tenantid = #{tenantId} AND private_status = 'PRIVATE' AND categorytype = 0 AND group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            "</script>")
    Set<CategoryEntityV2> selectSetByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("groupIdList") List<String> groupIdList);


    @Select("<script>" +
            " SELECT *,true as read,true as edit_category, true as edit_item FROM category WHERE tenantid = #{tenantId} AND private_status = 'PUBLIC' AND categorytype = 0" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT category.*,relation.read,relation.edit_category,relation.edit_item FROM category INNER JOIN category_group_relation as relation on category.guid = relation.category_id " +
            " WHERE tenantid = #{tenantId} AND private_status = 'PRIVATE' AND categorytype = 0 AND group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            "</script>")
    List<CategoryPrivilege> selectListByTenantIdAndGroupId(@Param("tenantId") String tenantId, @Param("groupIdList") List<String> groupIdList);

    @Select("<script>" +
            "SELECT COALESCE( MAX(sort),0) + 1  "+
            "FROM\n" +
            " category \n" +
            "WHERE\n" +
            " tenantid = #{ tenantId } \n" +
            "<if test='guid == null'>" +
            " AND parentcategoryguid IS NULL" +
            "</if>" +
            "<if test='guid != null'>" +
            " AND parentcategoryguid =#{guid}" +
            "</if>" +
            "</script>")
    int getMaxSortByParentGuid(@Param("guid") String guid,@Param("tenantId") String tenantId);

    @Select("<script>" +
            "SELECT\n" +
            "  sort \n" +
            "FROM\n" +
            "  category \n" +
            "WHERE\n" +
            "  tenantid = #{tenantId}\n" +
            "  AND guid = #{guid}" +
            "</script>")
    int getCategorySortById(@Param("guid") String guid,@Param("tenantId") String tenantId);

    @Update("<script>" +
            "UPDATE category \n" +
            "SET sort = sort + 1 \n" +
            "WHERE"+
            "  tenantid = #{tenantId}\n" +
            " AND sort &gt;= #{sort}\n"+
            "<if test = 'parentGuid != null'>" +
            "  AND parentcategoryguid = #{parentGuid}" +
            "</if>"+
            "<if test = 'parentGuid == null'>" +
            "  AND parentcategoryguid is null" +
            "</if>"+
            "</script>")
    void updateSort(@Param("sort") int sort,@Param("parentGuid") String parentGuid,@Param("tenantId") String tenantId);


    @Update("<script>" +
            "UPDATE category " +
            "SET updater = #{updater},updatetime=now(),private_status=#{privateStatus},publish=#{isPublish}\n" +
            "WHERE"+
            " guid=#{guid} and  tenantid = #{tenantId}" +
            "</script>")
    void updateCataloguePrivateStatus(@Param("guid") String  guid,@Param("privateStatus") String  privateStatus,@Param("isPublish") Boolean isPublish,@Param("tenantId") String tenantId,@Param("updater") String updater);


    @Update("<script> " +
            "update category set name=#{category.name},description=#{category.description},qualifiedName=#{category.qualifiedName},updater=#{updater},updatetime=#{updateTime}" +
            "<if test = 'category.publish != null'>" +
            ",publish=#{category.publish}" +
            "</if>"+
            "<if test = 'category.code != null'>" +
            ",code=#{category.code}" +
            "</if>"+
            "<if test = 'category.approvalId != null'>" +
            ",approval_id=#{category.approvalId}" +
            "</if>"+
            "<if test = 'category.information != null'>" +
            ",information=#{category.information}" +
            "</if>"+
            " where guid=#{category.guid} and tenantid=#{tenantId}"+
            "</script>")
    public int updateCategoryV2Info(@Param("category") CategoryEntityV2 category, @Param("tenantId") String tenantId, @Param("updater") String updater, @Param("updateTime") Timestamp updateTime);


    @Select("select * from category where categoryType=#{categoryType}")
    Set<CategoryEntityV2> selectGlobal(@Param("categoryType") int categoryType);

    @Select("<script>" +
            " SELECT * FROM category WHERE private_status = 'PUBLIC' AND categorytype = 0" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT category.* FROM category INNER JOIN category_group_relation as relation on category.guid = relation.category_id " +
            " WHERE private_status = 'PRIVATE' AND categorytype = 0 AND group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            "</script>")
    Set<CategoryEntityV2> selectSetByStatus(@Param("groupIdList") List<String> groupIdList);

    @Select("<script>" +
            " SELECT category.* FROM category LEFT JOIN category_group_relation as relation on category.guid = relation.category_id WHERE private_status = 'PUBLIC' AND categorytype = #{categoryType}" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT category.* FROM category INNER JOIN category_group_relation as relation on category.guid = relation.category_id " +
            " WHERE level &lt;= 5 AND private_status = 'PRIVATE' AND categorytype = #{categoryType} AND group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            "</script>")
    Set<CategoryEntityV2> selectListByStatus(@Param("creator") String creator, @Param("groupIdList") List<String> groupIdList, @Param("categoryType") Integer categoryType);


}
