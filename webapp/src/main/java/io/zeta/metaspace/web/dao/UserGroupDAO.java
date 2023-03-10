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

package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.metadata.CategoryExport;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.share.ProjectHeader;
import io.zeta.metaspace.model.table.DataSourceHeader;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.DBInfo;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupPrivileges;
import io.zeta.metaspace.model.usergroup.result.*;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/24 15:54
 */
public interface UserGroupDAO {
    /**
     * 一.用户组列表及搜索
     */

    //实现用户组列表及搜索
    @Select("<script>" +
            "select count(*) over() totalSize,u.id,u.name,u.description,case when m.member is NULL then '0' else m.member end member,u.creator,u.createtime,u.updatetime,u.authorize_user authorize,u.authorize_time authorizeTime " +
            " from user_group u left join " +
            " (select g.id id,count(*) member " +
            " from user_group  g " +
            " join user_group_relation r " +
            " on g.id=r.group_id  where r.user_id in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " GROUP BY g.id) m " +
            " on u.id=m.id " +
            " where u.tenant=#{tenantId} and valid=true" +
            "<if test='search!=null'>" +
            " and u.name like concat('%',#{search},'%') ESCAPE '/' " +
            "</if>" +
            "<if test=\"sortBy!=null and sortBy!=''\">" +
            "order by ${sortBy} " +
                "<if test=\"order!=null and order!=''\">" +
                " ${order} " +
                "</if>" +
            "</if>" +

            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupListAndSearchResult> getUserGroupSortByUpdateTime(@Param("tenantId") String tenantId, @Param("offset") int offset, @Param("limit") int limit,
                                                                           @Param("sortBy")String sortBy, @Param("order") String order, @Param("search") String search,
                                                                           @Param("ids")List<String> ids) throws SQLException;


    @Select("select username from users where userid=#{userId}")
    public String getUserNameById(String userId);


    /**
     * 二.用户组详情
     */
    @Select("select name,description from user_group where id=#{id}")
    public UserGroup getUserGroupByID(String id);

    @Select("SELECT u.account from user_group_relation ugr INNER JOIN users u on u.userid=ugr.user_id\n" +
            "where ugr.group_id=#{id} and u.valid=true")
    public List<String> getAllUserByGroupId(String id);

    /**
     * 二.用户组详情
     */
    @Select("<script>" +
            " select name,description from user_group where id in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public List<UserGroup> getUserGroupByIDs(@Param("ids") List<String> ids);


    /**
     * 三.新建用户组
     */

    @Insert("insert into user_group (id,tenant,name,creator,description,createtime,updatetime,valid) values (#{group.id},#{tenantId},#{group.name},#{group.creator},#{group.description},#{group.createTime},#{group.updateTime},true)")
    public Integer addUserGroup(@Param("tenantId") String tenantId, @Param("group") UserGroup group);

    //判断租户是否已经存在，true为存在，false为不存在
    @Select("select count(*) from user_group where name=#{name} and id!=#{id} and tenant=#{tenantId} and valid=true")
    public Integer isNameById(@Param("tenantId") String tenantId, @Param("name") String name, @Param("id") String id);


    /**
     * 四.删除用户组信息
     */
    @Update("update user_group set " +
            " valid=false " +
            "where id=#{id}")
    public void deleteUserGroupByID(String id);


    @Delete("delete from user_group_relation where group_id=#{id}")
    public void deleteUserGroupRelationByID(String id);


    @Delete("delete from category_group_relation where group_id=#{id}")
    public void deleteCategoryGroupRelationByID(String id);

    @Delete("delete from category_group_relation where category_id=#{id}")
    public void deleteCategoryGroupRelationByCategory(String id);

    @Delete("<script>" +
            "delete from category_group_relation where  category_id in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public void deleteCategoryGroupRelationByCategoryIds(@Param("ids") List<String> ids);

    @Delete("delete from datasource_group_relation where group_id=#{id}")
    public void deleteUserGroupDataSourceRelationByID(String id);

    @Delete("delete from project_group_relation where group_id=#{id}")
    public void deleteUserGroupProjectRelationByID(String id);


    /**
     * 五.用户组成员列表及搜索
     */
    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username,u.account from users u join user_group_relation g on u.userid=g.user_id " +
            "where g.group_id=#{id} and u.userid in " +
            "<foreach collection='ids' item='userId' index='index' separator=',' open='(' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "<if test='search!=null'>" +
            " and u.username like concat('%',#{search},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<MemberListAndSearchResult> getMemberListAndSearch(@Param("id")String id,@Param("offset") int offset,
                                                                  @Param("limit") int limit, @Param("search") String search,@Param("ids")List<String> ids);


    /**
     * 六.用户组添加成员列表及搜索
     */


    @Select("<script>" +
            " select u.username  " +
            " from user_group_relation g  " +
            " join users u " +
            " on g.user_id=u.userid " +
            " join user_group  r " +
            " on g.group_id=r.id " +
            " where g.group_id=#{groupId} " +
            " and r.tenant=#{tenantId} " +
            "</script>")
    public List<String> getUserNameByGroupId(@Param("tenantId") String tenantId, @Param("groupId") String groupId);


    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username,u.account email from users u " +
            " where u.username in " +
            "<foreach collection='userNameList' item='userName' index='index' separator=',' open='(' close=')'>" +
            "#{userName}" +
            "</foreach>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupMemberSearch> getUserGroupMemberSearch(@Param("userNameList") List<String> userNameList, @Param("offset") int offset, @Param("limit") int limit);


    /**
     * 七.用户组添加成员
     */

    @Insert({"<script>insert into user_group_relation (group_id,user_id) values ",
             "<foreach item='item' index='index' collection='userIds'",
             "open='(' separator='),(' close=')'>",
             "#{groupId},#{item}",
             "</foreach>",
             "</script>"})
    public void addUserGroupByID(@Param("groupId") String groupId, @Param("userIds") List<String> userIds);


    /**
     * 八.用户组移除成员
     */
    @Delete({"<script>",
             "delete from user_group_relation where group_id=#{groupId} and user_id in ",
             "<foreach collection='userIds' item='userId' index='index' separator=',' open='(' close=')'>",
             "#{userId}",
             "</foreach>",
             "</script>"})
    public void deleteUserByGroupId(@Param("groupId") String groupId, @Param("userIds") List<String> userIds);

    /**
     * 十五.修改用户组管理信息
     */
    @Update("update user_group set " +
            "name=#{group.name} ," +
            "description=#{group.description} ," +
            "updatetime=#{updateTime} " +
            "where id=#{groupId}")
    public void updateUserGroupInformation(@Param("groupId") String groupId, @Param("group") UserGroup group, @Param("updateTime") Timestamp updateTime);


    //判断用户组Id是否已经存在，true为存在，false为不存在
    @Select("select count(*) from user_group where id=#{groupId}")
    public Integer existGroupId(@Param("groupId") String groupId);

    @Select("select g.*,g.tenant tenantId from user_group g join user_group_relation u on g.id=u.group_id where u.user_id=#{userId} and g.valid=true and tenant=#{tenantId}")
    public List<UserGroup> getuserGroupByUsersId(@Param("userId") String userId,@Param("tenantId") String tenantId);

    @Select("<script>" +
            "select g.*,g.tenant tenantId from user_group g where g.valid=true " +
            " and g.tenant in  "+
            "<foreach collection='list' item='tid' index='index' separator=',' open='(' close=')'>"+
            "#{tid}"+
            "</foreach>"+
            "</script>"
    )
    public List<UserGroup> getuserGroupByUid(@Param("list") List<String> tenantParamList);

    @Select("<script>" +
            "select g.*,g.tenant tenantId from user_group g inner join user_group_relation as relation on g.id = relation.group_id where g.valid=true and relation = #{userId}" +
            " and g.tenant in  "+
            "<foreach collection='list' item='tid' index='index' separator=',' open='(' close=')'>"+
            "#{tid}"+
            "</foreach>"+
            "</script>"
    )
    List<UserGroup> getuserGroupByUidAndUserId(@Param("list") List<String> tenantParamList, @Param("userId") String userId);

    @Select("select * from category where categoryType=#{categoryType} and tenantid=#{tenantId}")
    public List<RoleModulesCategories.Category> getAllCategorys(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select category.*,item.count count from category " +
            " left join ( " +
            " select count(*) count," +
            "<choose>" +
            "    <when test=\"categoryType==0\">" +
            "        categoryguid as guid from table_relation join tableinfo on table_relation.tableguid=tableinfo.tableguid where tableinfo.tableguid=table_relation.tableguid and (dbname is null " +
            "        <if test='dbNames!=null and dbNames.size()>0'>" +
            "          or dbname in " +
            "          <foreach item='item' index='index' collection='dbNames'" +
            "          open='(' separator=',' close=')'>" +
            "          #{item}" +
            "          </foreach>" +
            "        </if>" +
            "        )" +
            "    </when>" +
            "    <when test=\"categoryType==5\">" +
            "        guid from ( " +
            "        select index_field_id as guid,index_id ,row_number() over(partition by index_id order by version desc) as rn from index_atomic_info where  tenant_id=#{tenantId}  " +
            "        union  " +
            "        select index_field_id as guid,index_id ,row_number() over(partition by index_id order by version desc) as rn from index_derive_info where  tenant_id=#{tenantId}  " +
            "        union  " +
            "        select index_field_id as guid,index_id ,row_number() over(partition by index_id order by version desc) as rn from index_composite_info where  tenant_id=#{tenantId}  " +
            "        ) adc where adc.rn=1 " +
            "    </when>" +
            "    <when test=\"categoryType==1\">" +
            "        categoryguid as guid from business_relation " +
            "    </when>" +
            "    <when test=\"categoryType==3\">" +
            "        guid from ( select b.categoryid as guid from data_standard b where delete=false and version=0 and tenantid=#{tenantId} ) ds " +
            "    </when>" +
            "    <otherwise>" +
            "        category_id as guid from data_quality_rule where delete=false" +
            "    </otherwise>" +
            "</choose>" +
            " group by guid" +
            ") item on category.guid=item.guid " +
            " where categoryType=#{categoryType} and tenantid=#{tenantId}" +
            "</script>")
    List<RoleModulesCategories.Category> getAllCategorysAndCount(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId,@Param("dbNames") List<String> dbNames);

    @Select("<script>" +
            "select guid,description,name,parentcategoryguid,categorytype,level,safe,tenantid,createtime,creator,sort,upbrothercategoryguid,downbrothercategoryguid from category " +
            " where categoryType=#{categoryType} and tenantid=#{tenantId}" +
            "</script>")
    List<RoleModulesCategories.Category> getAllCategorysByType(@Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    // 获取租户数据库登记所创建的目录
    @Select("select category_id " +
            "from source_info " +
            "where tenant_id=#{tenantId} " +
            "and version = 0 " +
            "and importance=#{importance} " +
            "and category_id is not null " +
            "and category_id != ''")
    List<String> getRegisteredCategorys(@Param("tenantId")String tenantId, @Param("importance")boolean importance);

    // 获取手动创建的目录（即非数据库登记创建）和 租户数据库登记（“是否重要”属性选择为“是”）所创建的目录
    @Select("select c.guid, c.description, c.name, c.parentcategoryguid, c.categorytype, c.level, c.safe, c.tenantid, c.createtime, c.creator, c.sort " +
            "from category c " +
            "where categoryType=#{categoryType} and tenantid=#{tenantId} " +
            "and c.guid not in (select si.category_id from source_info si where tenant_id=#{tenantId} and si.version = 0 and category_id is not null and category_id != '') " +
            "union all " +
            "select c.guid, c.description, c.name, c.parentcategoryguid, c.categorytype, c.level, c.safe, c.tenantid, c.createtime, c.creator, c.sort " +
            "from category c " +
            "inner join source_info si on si.category_id=c.guid and si.version = 0 and si.importance=true " +
            "where categoryType=#{categoryType} and tenantid=#{tenantId}")
    List<RoleModulesCategories.Category> getAllowAuthedCategorysByType(@Param("categoryType")Integer categoryType, @Param("tenantId")String tenantId);

    @Select("select DISTINCT t2.guid, t2.name, t2.level, t2.qualifiedname, t2.parentcategoryguid, t2.upbrothercategoryguid, t2.downbrothercategoryguid,t2.description, t2.safe " +
            "FROM category_group_relation t1 JOIN category t2 ON t1.category_id = t2.guid\n" +
            "JOIN user_group t3 ON (t1.group_id = t3.id and t3.valid = true)\n" +
            "JOIN user_group_relation t4 ON t3.id = t4.group_id\n" +
            "WHERE t2.tenantid = #{tenantId}\n" +
            "AND t3.tenant = #{tenantId}\n" +
            "AND t4.user_id = #{userId}\n" +
            "AND t2.categorytype = 0")
    public List<CategoryPrivilegeV2> getUserCategories(@Param("tenantId") String tenantId, @Param("userId") String userId);

    @Select({"<script>",
            "select guid, name, level, qualifiedname, parentcategoryguid, upbrothercategoryguid, downbrothercategoryguid,description, safe ",
            " from category where tenantid = #{tenantId} and categorytype = 0 and guid in ",
            "<foreach collection='guids' item='guid' index='index' separator=',' open='(' close=')'>",
            "#{guid}",
            "</foreach>",
            "</script>"})
    public List<CategoryPrivilegeV2> getUserCategoriesByIds(@Param("guids") List<String> guids, @Param("tenantId")String tenantId);

    @Select("select c.guid from category_group_relation g join category c on g.category_id=c.guid where g.group_id=#{userGroupId} and c.categorytype=#{categoryType} and c.tenantid=#{tenantId} and g.read=true")
    public List<String> getCategorysByTypeIds(@Param("userGroupId") String userGroupId, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Select("select c.* from category_group_relation g join category c on g.category_id=c.guid where g.group_id=#{userGroupId} and c.categorytype=#{categoryType} and c.tenantid=#{tenantId}")
    public List<RoleModulesCategories.Category> getCategorysByType(@Param("userGroupId") String userGroupId, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Select("<script>" +
            "select c.*,item.count from category_group_relation g join category c on g.category_id=c.guid " +
            " left join ( " +
            " select count(*) count," +
            "<choose>" +
            "    <when test=\"categoryType==0\">" +
            "        categoryguid as guid from table_relation join tableinfo on table_relation.tableguid=tableinfo.tableguid where tableinfo.tableguid=table_relation.tableguid and (dbname is null " +
            "        <if test='dbNames!=null and dbNames.size()>0'>" +
            "          or dbname in " +
            "          <foreach item='item' index='index' collection='dbNames'" +
            "          open='(' separator=',' close=')'>" +
            "          #{item}" +
            "          </foreach>" +
            "        </if>" +
            "        )" +
            "    </when>" +
            "    <when test=\"categoryType==1\">" +
            "        categoryguid as guid from business_relation " +
            "    </when>" +
            "    <when test=\"categoryType==3\">" +
            "        guid from ( select b.categoryid as guid from data_standard b where delete=false and version=0 and tenantid=#{tenantId} ) ds " +
            "    </when>" +
            "    <otherwise>" +
            "        category_id as guid from data_quality_rule where delete=false " +
            "    </otherwise>" +
            "</choose>" +
            " group by guid" +
            ") item on c.guid=item.guid " +
            " where g.group_id=#{userGroupId} and c.categorytype=#{categoryType} and c.tenantid=#{tenantId}" +
            "</script>")
    public List<RoleModulesCategories.Category> getCategorysByTypeAndCount(@Param("userGroupId") String userGroupId, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId,@Param("dbNames") List<String> dbNames);

    //递归找子节点
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where tenantid=#{tenantId} and parentCategoryGuid in" +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId}" +
            ")" +
            "SELECT * FROM categoryTree</script>")
    public List<RoleModulesCategories.Category> getChildCategorys(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where tenantid=#{tenantId} and parentCategoryGuid in" +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId}" +
            ")" +
            "SELECT categoryTree.*,item.count FROM categoryTree " +
            " left join ( " +
            " select count(*) count," +
            "<choose>" +
            "    <when test=\"categoryType==0\">" +
            "        categoryguid as guid from table_relation join tableinfo on table_relation.tableguid=tableinfo.tableguid where tableinfo.tableguid=table_relation.tableguid and (dbname is null " +
            "        <if test='dbNames!=null and dbNames.size()>0'>" +
            "          or dbname in " +
            "          <foreach item='item' index='index' collection='dbNames'" +
            "          open='(' separator=',' close=')'>" +
            "          #{item}" +
            "          </foreach>" +
            "        </if>" +
            "        )" +
            "    </when>" +
            "    <when test=\"categoryType==1\">" +
            "        categoryguid as guid from business_relation " +
            "    </when>" +
            "    <when test=\"categoryType==3\">" +
            "        guid from ( select b.categoryid as guid from data_standard b where delete=false and version=0 and tenantid=#{tenantId} ) ds " +
            "    </when>" +
            "    <otherwise>" +
            "        category_id as guid from data_quality_rule where delete=false " +
            "    </otherwise>" +
            "</choose>" +
            " group by guid" +
            ") item on categoryTree.guid=item.guid" +
            "</script>")
    public List<RoleModulesCategories.Category> getChildCategorysAndCount(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId,@Param("dbNames") List<String> dbNames);

    //递归找父节点,结果集不包含自己了
    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where tenantid=#{tenantId} and " +
            "    guid in " +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid where category.tenantid=#{tenantId} " +
            ")" +
            "SELECT * from categoryTree where guid not in" +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<RoleModulesCategories.Category> getParentCategorys(@Param("guid") List<String> guid, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    //找出合集外的目录
    @Select("<script>SELECT * from category where tenantid=#{tenantId} " +
            "    <if test='categories!=null and categories.size()>0'>" +
            "    and guid not in" +
            "    <foreach item='item' index='index' collection='categories'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item.guid}" +
            "    </foreach>" +
            "    </if>" +
            "    and categoryType = #{categoryType}" +
            "</script>")
    public List<RoleModulesCategories.Category> getOtherCategorys(@Param("categories") List<RoleModulesCategories.Category> categories, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    //找出合集外的目录2
    @Select("<script>SELECT * from category where tenantid=#{tenantId} " +
            "    <if test='categories!=null and categories.size()>0'>" +
            "    and guid not in" +
            "    <foreach item='item' index='index' collection='categories'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item.guid}" +
            "    </foreach>" +
            "    </if>" +
            "    and categoryType = #{categoryType}" +
            "</script>")
    public List<RoleModulesCategories.Category> getOtherCategorys2(@Param("categories") List<CategoryPrivilege> categories, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Insert("insert into category_group_relation values(#{categoryId},#{groupId})")
    public int addUserGroup2category(@Param("groupId") String groupId, @Param("categoryId") String categoryId);

    //删除授权范围
    @Delete("delete from category_group_relation where group_id=#{groupId}")
    public int deleteUserGroup2category(String groupId);

    //获取用户组的用户id
    @Select("select user_id from user_group_relation where group_id=#{groupId}")
    public List<String> getUserIdByUserGroup(String groupId);

    //更新用户组
    @Update("update user_group set updatetime=#{updateTime},authorize_user=#{userId},authorize_time=#{updateTime} where id=#{groupId}")
    public int updateCategory(@Param("groupId") String groupId, @Param("updateTime") Timestamp updateTime,@Param("userId") String userId);

    //批量更新用户组
    @Update("<script>update user_group set updatetime=#{updateTime},authorize_user=#{userId},authorize_time=#{updateTime} where id in" +
            "<foreach item='groupId' index='index' collection='groupIds' open='(' separator=',' close=')'>" +
            "#{groupId}" +
            "</foreach>" +
            "</script>")
    public int updateCategorys(@Param("groupIds") List<String> groupIds, @Param("updateTime") Timestamp updateTime,@Param("userId") String userId);

    @Select("select guid from category where categorytype=#{categoryType} and level = 1 adn tenantid=#{tenantId}")
    public List<String> getTopCategoryGuid(int categoryType,@Param("tenantId") String tenantId);

    //递归找子节点加自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where tenantid=#{tenantId} and parentCategoryGuid in " +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId}" +
            ")" +
            "SELECT * FROM categoryTree" +
            " UNION " +
            "SELECT * FROM category where tenantid=#{tenantId} and guid in " +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<RoleModulesCategories.Category> getChildAndOwnerCategorys(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);


//    @Select("<script>select count(*) over() as total , t.* from ( select DISTINCT tableinfo.source_id as sourceId,(case tableinfo.source_id = 'hive' when true then  'hive' else  data_source.source_name end) as sourceName ,( case (select count(*)>0  from data_source where source_id = tableinfo.source_id) or (tableinfo.source_id = 'hive') when true then 'ACTIVE' else 'DELETED' end ) as sourceStatus from category,table_relation,tableinfo " +
//            " left join data_source on data_source.source_id = tableinfo.source_id " +
//            " where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and databasestatus='ACTIVE' and category.tenantid=#{tenantId} " +
//            " and ( tableinfo.source_id in (select source_id from data_source where tenantid = #{tenantId}) or tableinfo.source_id = 'hive')" +
//            "  and category.guid in " +
//            "    <foreach item='item' index='index' collection='guids'" +
//            "    open='(' separator=',' close=')'>" +
//            "    #{item}" +
//            "    </foreach>" +
//            "    ) t " +
//            "  where t.sourceName like '%'||#{query}||'%' ESCAPE '/' " +
//            "order by t.sourceId <if test='limit!= -1'>limit #{limit}</if> offset #{offset} " +
//            "</script>")
    @Select("<script>" +
            " SELECT" +
            " source.source_id AS sourceId," +
            " source.source_name AS sourceName," +
            " 'ACTIVE' AS sourceStatus " +
            " FROM" +
            " data_source AS source " +
            " WHERE" +
            " source.tenantid = #{tenantId} " +
            " AND source.source_name  like '%'||#{query}||'%' ESCAPE '/' " +
            " ORDER BY source.source_id <if test='limit!= -1'>limit #{limit}</if> offset #{offset}"+
            "</script>")
    public List<DataSourceHeader> getSourceInfo(@Param("guids") List<String> guids, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit, @Param("tenantId") String tenantId);


    @Select("<script>select DISTINCT tableinfo.databaseGuid,tableinfo.dbname,tableinfo.databasestatus from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and databasestatus='ACTIVE' and category.tenantid=#{tenantId} and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and tableinfo.dbname like '%'||#{query}||'%' ESCAPE '/' " +
            "    and tableinfo.dbname in " +
            "    <foreach item='item' index='index' collection='databases'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    order by tableinfo.dbname <if test='limit!= -1'>limit #{limit}</if> offset #{offset}" +
            "</script>")
    public List<DatabaseHeader> getDBInfo(@Param("guids") List<String> guids, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);


    @Select("<script>" +
            " SELECT COUNT ( * ) OVER ( ) AS total, T.* " +
            " FROM ( SELECT DISTINCT db.database_guid as databaseGuid,db.database_name as dbName,db.status as databasestatus,source.source_id as sourceId,source.source_name as sourceName" +
            " FROM db_info as db INNER JOIN source_db as sd on db.database_guid = sd.db_guid INNER JOIN data_source as source on source.source_id = sd.source_id" +
            " WHERE db.status = 'ACTIVE' and sd.source_id = #{sourceId}" +
            " <if test='databases != null and databases.size() > 0 '>" +
            " UNION" +
            " SELECT DISTINCT db.database_guid as databaseGuid,db.database_name as dbName,db.status as databasestatus,'hive' as sourceId,'hive' as sourceName FROM db_info as db" +
            " WHERE db.status = 'ACTIVE' AND db.db_type = 'HIVE'" +
            " AND db.database_name IN" +
            " <foreach item='item' index='index' collection='databases' open='(' separator=',' close=')'>" +
            "   #{item}" +
            " </foreach>" +
            " </if>"+
            " ) AS T ORDER BY t.dbname"+
            " <if test='limit != -1'>limit #{limit}</if> offset #{offset} "+
            "</script>")
    List<DatabaseHeader> getDBInfo2(@Param("offset") long offset, @Param("limit") long limit, @Param("databases") List<String> databases, @Param("sourceId") String sourceId);



    @Select("<script>" +
            " SELECT COUNT ( * ) OVER ( ) AS total, T.* " +
            " FROM ( SELECT DISTINCT db.database_guid as databaseGuid,db.database_name as dbName,db.status as databasestatus,source.source_id as sourceId,source.source_name as sourceName" +
            " FROM db_info as db INNER JOIN source_db as sd on db.database_guid = sd.db_guid INNER JOIN data_source as source on source.source_id = sd.source_id" +
            " WHERE db.status = 'ACTIVE' and source.tenantid = #{tenantId} AND db.database_name like '%'||#{dbName}||'%' ESCAPE '/'" +
            " <if test='databases != null and databases.size() > 0 '>" +
            " UNION" +
            " SELECT DISTINCT db.database_guid as databaseGuid,db.database_name as dbName,db.status as databasestatus,'hive' as sourceId,'hive' as sourceName FROM db_info as db" +
            " WHERE db.status = 'ACTIVE' AND db.db_type = 'HIVE' AND db.database_name IN " +
            " <foreach item='item' index='index' collection='databases' open='(' separator=',' close=')'>" +
            "   #{item}" +
            " </foreach>" +
            " AND db.database_name like '%'||#{dbName}||'%' ESCAPE '/'" +
            " </if>"+
            " ) AS T ORDER BY t.dbname" +
            " <if test='limit != -1'>limit #{limit}</if> offset #{offset} "+
            "</script>")
    List<DatabaseHeader> selectDbByNameAndTenantId(@Param("offset") long offset, @Param("limit") long limit, @Param("databases") List<String> databases, @Param("tenantId") String tenantId, @Param("dbName") String dbName);


    @Select("<script>select COUNT(DISTINCT tableinfo.databaseGuid) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and databasestatus='ACTIVE' and category.tenantid=#{tenantId} and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and tableinfo.dbname like '%'||#{query}||'%' ESCAPE '/' " +
            "    and tableinfo.dbname in " +
            "    <foreach item='item' index='index' collection='databases'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public long getDBCountV2(@Param("guids") List<String> guids, @Param("query") String query,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

    @Select("<script>" +
            " SELECT count(*) over() as total,t.* FROM ("+
            " SELECT sd.source_id as sourceId," +
            " source.source_name as sourceName,"+
            " tableinfo.databaseguid," +
            " tableinfo.dbname," +
            " tableinfo.tableguid," +
            " tableinfo.tablename," +
            " tableinfo.status," +
            " tableinfo.createtime" +
            " FROM tableinfo INNER JOIN source_db as sd on tableinfo.databaseguid=sd.db_guid" +
            " INNER JOIN data_source as source on source.source_id = sd.source_id "+
            " WHERE source.tenantid = #{tenantId} and tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' and tableinfo.tablename like '%'||#{query}||'%' ESCAPE '/'" +
            " <if test='databases != null and databases.size()>0'>" +
            " union" +
            " SELECT 'hive' as sourceId," +
            " 'hive' as sourceName," +
            " tableinfo.databaseguid," +
            " tableinfo.dbname," +
            " tableinfo.tableguid," +
            " tableinfo.tablename," +
            " tableinfo.status," +
            " tableinfo.createtime" +
            " FROM tableinfo " +
            " INNER JOIN db_info as db on tableinfo.databaseguid = db.database_guid" +
            " WHERE db.db_type = 'HIVE' and tableinfo.status = 'ACTIVE' AND tableinfo.databasestatus = 'ACTIVE' AND db.database_name in " +
            " <foreach item='item' index='index' collection='databases' open='(' separator=',' close=')'>" +
            "   #{item}" +
            " </foreach> " +
            " and tableinfo.tablename like '%'||#{query}||'%' ESCAPE '/'" +
            " </if>" +
            " ) as t order by t.tableguid " +
            " <if test='limit!= -1'>limit #{limit}</if> offset #{offset}" +
            "</script>")
    List<TechnologyInfo.Table> getTableInfosV2(@Param("guids") List<String> guids, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

    @Select("<script>select distinct tableinfo.tableGuid from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.tenantid=#{tenantId} and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and tableinfo.dbname in " +
            "    <foreach item='item' index='index' collection='databases'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<String> getTableIds(@Param("guids") List<String> guids,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

    @Select("<script> " +
            "select count(*)over() totalSize,u.id,u.name from user_group u where u.tenant=#{tenantId} and u.valid=true" +
            "<if test='parameters.query!=null'>" +
            " and u.name like concat('%',#{parameters.query},'%') ESCAPE '/'  " +
            "</if>" +
            "<if test='parameters.limit!=-1'>" +
            " limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            " offset ${parameters.offset} " +
            "</if> " +
            " </script>")
    public List<UserGroupIdAndName> getUserGroup(@Param("tenantId")String tenantId, @Param("parameters") Parameters parameters);

    @Select("<script>" +
            "select count(*)over() totalSize,d.source_id sourceId,d.source_name sourceName,g.privilege_code privilegeCode,d.source_type sourceType " +
            " from data_source d " +
            " join datasource_group_relation g " +
            " on d.source_id=g.source_id " +
            " where g.group_id = #{groupId} " +
            "<if test='search!=null'>" +
            " and d.source_name like concat('%',#{search},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<SourceAndPrivilege> getSourceBySearch(@Param("groupId") String groupId, @Param("offset") int offset, @Param("limit") int limit, @Param("search") String search);

    @Select("<script>" +
            " select count(*) over() totalSize,d.source_id sourceId,d.source_name sourceName from data_source d " +
            " where d.source_id not in ( " +
            " select r.source_id from datasource_group_relation r  where r.group_id=#{groupId} ) " +
            " and d.tenantid= #{tenantId} " +
            "<if test='search!=null'>" +
            " and d.source_name like concat('%',#{search},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<DataSourceIdAndName> getNoSourceBySearch(@Param("tenantId") String tenantId, @Param("groupId") String groupId, @Param("offset") int offset, @Param("limit") int limit, @Param("search") String search);

    @Insert({"<script>insert into datasource_group_relation (source_id,group_id,privilege_code) values ",
             "<foreach item='item' index='index' collection='sourceIds'",
             "open='(' separator='),(' close=')'>",
             "#{item},#{groupId},#{privilege}",
             "</foreach>",
             "</script>"})
    public void addDataSourceByGroupId(@Param("groupId") String groupId, @Param("sourceIds") List<String> sourceIds,@Param("privilege") String privilege);

    @Update({"<script> " +
             "update datasource_group_relation set " ,
             "privilege_code=#{privileges.privilegeCode} " ,
             "where group_id=#{groupId} and source_id in ",
             "<foreach collection='privileges.sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" ,
             "#{sourceId}",
             "</foreach>",
             " </script>"})
    public Integer updateDataSourceByGroupId(@Param("groupId")String groupId,@Param("privileges") UserGroupPrivileges privileges);

    @Delete({"<script>",
             "delete from datasource_group_relation where group_id=#{groupId} and source_id in ",
             "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>",
             "#{sourceId}",
             "</foreach>",
             "</script>"})
    public void deleteDataSourceByGroupId(@Param("groupId") String groupId, @Param("sourceIds") List<String> sourceIds);

    @Select("<script>" +
            " SELECT guid,name,description FROM category where tenantid=#{tenantId} and guid in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public List<CategoryExport> getCategoryByIds(@Param("ids") List<String> ids, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    //递归找子节点
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where tenantid=#{tenantId} and parentCategoryGuid in" +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId}" +
            ")" +
            " SELECT guid,name,description,level,parentCategoryGuid," +
            " lag(guid,1) over(partition by (case when parentcategoryguid='' then null else parentcategoryguid end) order by " +
            "<choose>" +
            "    <when test=\"sort=='name'\">" +
            "        convert_to(name, 'GBK')" +
            "    </when>" +
            "    <otherwise>" +
            "        createtime ${order},name" +
            "    </otherwise>" +
            "</choose>" +
            " ${order}) upbrothercategoryguid," +
            " lead(guid,1) over(partition by (case when parentcategoryguid='' then null else parentcategoryguid end) order by " +
            "<choose>" +
            "    <when test=\"sort=='name'\">" +
            "        convert_to(name, 'GBK')" +
            "    </when>" +
            "    <otherwise>" +
            "        createtime ${order},name" +
            "    </otherwise>" +
            "</choose>" +
            " ${order}) downbrothercategoryguid " +
            " FROM categoryTree</script>")
    public List<RoleModulesCategories.Category> getChildCategorysAndSort(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("categoryType") int categoryType,@Param("sort") String sort,@Param("order") String order,@Param("tenantId") String tenantId);

    @Select("<script>" +
            "select guid,name,description,level,parentCategoryGuid," +
            " lag(guid,1) over(partition by (case when parentcategoryguid='' then null else parentcategoryguid end) order by " +
            "<choose>" +
            "    <when test=\"sort=='name'\">" +
            "        convert_to(name, 'GBK')" +
            "    </when>" +
            "    <otherwise>" +
            "        createtime ${order},name" +
            "    </otherwise>" +
            "</choose>" +
            " ${order}) upbrothercategoryguid," +
            " lead(guid,1) over(partition by (case when parentcategoryguid='' then null else parentcategoryguid end) order by " +
            "<choose>" +
            "    <when test=\"sort=='name'\">" +
            "        convert_to(name, 'GBK')" +
            "    </when>" +
            "    <otherwise>" +
            "        createtime ${order},name " +
            "    </otherwise>" +
            "</choose>" +
            " ${order}) downbrothercategoryguid " +
            "from category where categoryType=#{categoryType} and tenantid=#{tenantId}" +
            "</script>")
    public List<RoleModulesCategories.Category> getAllCategorysAndSort(@Param("categoryType") int categoryType,@Param("sort") String sort,@Param("order") String order,@Param("tenantId")String tenantId);

    @Insert({"<script>insert into project_group_relation (project_id,group_id) values ",
             "<foreach item='item' index='index' collection='projectIds'",
             "open='(' separator='),(' close=')'>",
             "#{item},#{groupId}",
             "</foreach>",
             "</script>"})
    public void addProjectByGroupId(@Param("groupId") String groupId, @Param("projectIds") List<String> projectIds);

    @Select("<script>" +
            "select count(*)over() totalSize,p.id,p.name,p.description from project_group_relation r join project p on r.project_id=p.id where " +
            "r.group_id=#{groupId} and p.tenantid=#{tenantId} and p.valid=true " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and p.name like concat('%',#{param.query},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            " offset ${param.offset} " +
            "</if>" +
            "</script>")
    public List<ProjectHeader> getRelationProject(@Param("groupId")String groupId, @Param("param")Parameters parameters,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select count(*)over() totalSize,p.id,p.name,p.description from project p left join (" +
            " select * from project_group_relation where group_id=#{groupId} ) r " +
            " on r.project_id=p.id where p.tenantid=#{tenantId} and r.group_id is null and p.valid=true " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and p.name like concat('%',#{param.query},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            " offset ${param.offset} " +
            "</if>" +
            "</script>")
    public List<ProjectHeader> getNoRelationProject(@Param("groupId")String groupId,@Param("param")Parameters parameters, @Param("tenantId")String tenantId);

    @Select("<script>" +
            "select count(*)over() totalSize,id,name,description from project where tenantid=#{tenantId} and valid=true " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and name like concat('%',#{param.query},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            " offset ${param.offset} " +
            "</if>" +
            "</script>")
    public List<ProjectHeader> getAllProject(@Param("param")Parameters parameters, @Param("tenantId")String tenantId);

    @Delete("<script>" +
            " delete from project_group_relation where  group_id=#{id} and project_id in " +
            " <foreach item='project' index='index' collection='projects' separator=',' open='(' close=')'>" +
            "#{project}" +
            " </foreach>" +
            "</script>")
    public int deleteProjectToUserGroup(@Param("id")String id,@Param("projects") List<String> projects);

    //递归找子节点包含自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category " +
            "    where tenantid=#{tenantId} and guid in " +
            "    <foreach item='item' index='index' collection='parentCategoryGuid' " +
            "    open='(' separator=',' close=')'>" +
            "    #{item} " +
            "    </foreach>" +
            "    and categoryType=#{categoryType} " +
            "    UNION " +
            "    SELECT category.* from categoryTree " +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId} " +
            ") " +
            "SELECT *,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM categoryTree c inner join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} " +
            "</script>")
    public List<CategoryPrivilegeV2> getChildCategoriesPrivileges(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("userGroupId")String userGroupId, @Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    //递归找父节点包含自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category " +
            "    where tenantid=#{tenantId} and guid in " +
            "    <foreach item='item' index='index' collection='categoryGuid' " +
            "    open='(' separator=',' close=')'>" +
            "    #{item} " +
            "    </foreach>" +
            "    and categoryType=#{categoryType} " +
            "    UNION " +
            "    SELECT category.* from categoryTree " +
            "    JOIN category on categoryTree.parentCategoryGuid = category.guid where category.tenantid=#{tenantId} " +
            ") " +
            "SELECT *,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM categoryTree c left join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} " +
            "</script>")
    public List<CategoryPrivilegeV2> getParentCategoriesPrivileges(@Param("categoryGuid") List<String> categoryGuid, @Param("userGroupId")String userGroupId, @Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    //递归找同一用户组下子节点包含自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category " +
            "    where tenantid=#{tenantId} and guid in " +
            "    <foreach item='item' index='index' collection='parentCategoryGuid' " +
            "    open='(' separator=',' close=')'>" +
            "    #{item} " +
            "    </foreach>" +
            "    and categoryType=#{categoryType} " +
            "    UNION " +
            "    SELECT category.* from categoryTree " +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId} " +
            ") " +
            "SELECT *,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM categoryTree c inner join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} " +
            "</script>")
    public List<CategoryPrivilegeV2> getChildCategoriesPrivilegesInSameGroup(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("userGroupId") String userGroupId, @Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

    //获取当前目录在当前用户组的权限
    @Select("<script>" +
            "SELECT c.*,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM category c left join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} where c.guid=#{categoryGuid}" +
            "</script>")
    public List<CategoryPrivilegeV2> getCurrentCategoriesPrivileges(@Param("categoryGuid") String categoryGuid, @Param("userGroupId") String userGroupId);

    //递归找发生变化的子节点包含自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category " +
            "    where tenantid=#{tenantId} and guid=#{category.guid} " +
            "    and categoryType=#{categoryType} " +
            "    UNION " +
            "    SELECT category.* from categoryTree " +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId} " +
            ") " +
            "SELECT count(*)over() total,*,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM categoryTree c left join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} " +
            " where read!=#{category.read} or edit_category!=#{category.editCategory} or edit_item!=#{category.editItem} " +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<CategoryPrivilegeV2> getMandatoryUpdateChildCategoriesPrivileges(@Param("category") CategoryPrivilegeV2 category, @Param("userGroupId")String userGroupId, @Param("categoryType") int categoryType,
                                                                        @Param("tenantId") String tenantId, @Param("limit") int limit, @Param("offset") int offset);

    //递归找发生变化的子节点包含自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category " +
            "    where tenantid=#{tenantId} and guid=#{category.guid} " +
            "    and categoryType=#{categoryType} " +
            "    UNION " +
            "    SELECT category.* from categoryTree " +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid where category.tenantid=#{tenantId} " +
            ") " +
            "SELECT count(*)over() total,*,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM categoryTree c left join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} " +
            " where " +
            " read!=true " +
            "<if test='category.editCategory==true'>" +
            " or edit_category!=true " +
            "</if>" +
            "<if test='category.editItem==true'>" +
            " or edit_item!=true " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<CategoryPrivilegeV2> getUpdateChildCategoriesPrivileges(@Param("category") CategoryPrivilegeV2 category, @Param("userGroupId")String userGroupId, @Param("categoryType") int categoryType,
                                                                                  @Param("tenantId") String tenantId, @Param("limit") int limit, @Param("offset") int offset);


    @Update ("<script>" +
            "update category_group_relation set " +
             " read=true " +
             " ,edit_category=#{privilege.editCategory} " +
             " ,edit_item=#{privilege.editItem} " +
             " where group_id=#{userGroupId} and category_id in " +
             "    <foreach item='id' index='index' collection='categoryIds' " +
             "    open='(' separator=',' close=')'>" +
             "    #{id} " +
             "    </foreach>" +
             "</script>")
    public int updateChildCategoryPrivileges(@Param("categoryIds") List<String> categoryIds,@Param("userGroupId")String userGroupId,@Param("privilege")CategoryPrivilegeV2 privilege);

    @Update ("<script>" +
             "update category_group_relation set " +
             " read=#{privilege.read} " +
             " ,edit_category=#{privilege.editCategory} " +
             " ,edit_item=#{privilege.editItem} " +
             " where group_id=#{userGroupId} and category_id in " +
             "    <foreach item='id' index='index' collection='categoryIds' " +
             "    open='(' separator=',' close=')'>" +
             "    #{id} " +
             "    </foreach>" +
             "</script>")
    public int updateMandatoryChildCategoryPrivileges(@Param("categoryIds") List<String> categoryIds,@Param("userGroupId")String userGroupId,@Param("privilege")CategoryPrivilegeV2 privilege);


    @Update ("<script>" +
             "update category_group_relation set " +
             " read=#{privilege.read}, " +
             " edit_category=#{privilege.editCategory}, " +
             " edit_item=#{privilege.editItem} " +
             " where group_id=#{userGroupId} and category_id in " +
             "    <foreach item='id' index='index' collection='categoryIds' " +
             "    open='(' separator=',' close=')'>" +
             "    #{id} " +
             "    </foreach>" +
             "</script>")
    public void updateCategoryPrivileges(@Param("categoryIds") List<String> categoryIds,@Param("userGroupId")String userGroupId,@Param("privilege")CategoryPrivilegeV2 privilege);

    @Insert("<script>" +
            "insert into category_group_relation(category_id,group_id,read,edit_category,edit_item) values " +
            "    <foreach item='id' index='index' collection='categoryIds' " +
            "    open='(' separator='),(' close=')'>" +
            "    #{id},#{userGroupId},#{privilege.read},#{privilege.editCategory},#{privilege.editItem}" +
            "    </foreach>" +
            "</script>")
    public int addCategoryPrivileges(@Param("categoryIds") List<String> categoryIds,@Param("userGroupId")String userGroupId,@Param("privilege")CategoryPrivilegeV2 privilege);


    @Select("select category_id guid,read,edit_category editCategory,edit_item editItem  from category_group_relation where category_id=#{guid} and group_id=#{userGroupId}")
    public CategoryPrivilegeV2 getCategoriesPrivileges(@Param("guid") String guid, @Param("userGroupId")String userGroupId);

    @Select("select *,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem from category_group_relation g join category c on c.guid=g.category_id and g.group_id=#{userGroupId} where c.tenantid=#{tenantId} and c.categorytype=#{categoryType}")
    public List<CategoryPrivilegeV2> getUserGroupCategory(@Param("userGroupId")String userGroupId, @Param("tenantId")String tenantId, @Param("categoryType") int categoryType);

    //递归找父节点
    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where tenantid=#{tenantId} and " +
            "    guid in " +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid where category.tenantid=#{tenantId} " +
            ")" +
            "SELECT distinct * from categoryTree where guid not in(" +
            "  select category_id from category_group_relation where group_id=#{userGroupId}" +
            "  )" +
            "</script>")
    public List<CategoryPrivilegeV2> getParentCategoryNoPrivilege(@Param("guid") List<String> guid, @Param("categoryType") int categoryType,@Param("userGroupId")String userGroupId,@Param("tenantId") String tenantId);

    @Select("<script> " +
            " select *,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem,item.count count from category_group_relation g join category c on c.guid=g.category_id " +
            " left join ( " +
            " select count(*) count," +
            "<choose>" +
            "    <when test=\"categoryType==0\">" +
            "       guid from ( SELECT t.table_id,t.data_source_id,t.category_id as guid FROM ( " +
            "       SELECT table_id,data_source_id,category_id from table_data_source_relation as relation WHERE tenant_id = #{tenantId}" +
            "       UNION ALL" +
            "       SELECT tableinfo.tableguid,source.data_source_id,category_id FROM source_info as source INNER JOIN tableinfo on source.database_id=tableinfo.databaseguid  " +
            "       WHERE source.tenant_id = #{tenantId} AND source.version = 0 AND source.category_id is not null and source.category_id != '' AND tableinfo.status = 'ACTIVE'" +
            "       ) as t ) as d" +
            "    </when>" +
            "    <when test=\"categoryType==5\">" +
            "        guid from ( " +
            "        select index_field_id as guid,index_id ,row_number() over(partition by index_id order by version desc) as rn from index_atomic_info where  tenant_id=#{tenantId}  " +
            "        union  " +
            "        select index_field_id as guid,index_id ,row_number() over(partition by index_id order by version desc) as rn from index_derive_info where  tenant_id=#{tenantId}  " +
            "        union  " +
            "        select index_field_id as guid,index_id ,row_number() over(partition by index_id order by version desc) as rn from index_composite_info where  tenant_id=#{tenantId}  " +
            "        ) adc where adc.rn=1 " +
            "    </when>" +
            "    <when test=\"categoryType==1\">" +
            "        categoryguid as guid from business_relation " +
            "    </when>" +
            "    <when test=\"categoryType==3\">" +
            "        guid from ( select b.categoryid as guid from data_standard b where delete=false and version=0 and tenantid=#{tenantId} ) ds " +
            "    </when>" +
            "    <otherwise>" +
            "        category_id as guid from data_quality_rule where delete=false" +
            "    </otherwise>" +
            "</choose>" +
            " group by guid" +
            " )item on c.guid=item.guid " +
            " where c.tenantid=#{tenantId} and c.categorytype=#{categoryType}" +
            " and g.group_id in " +
            "    <foreach item='id' index='index' collection='userGroupIds'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    List<CategoryPrivilegeV2> getUserGroupsCategory(@Param("userGroupIds")List<String> userGroupIds, @Param("tenantId")String tenantId, @Param("categoryType") int categoryType,@Param("dbNames") List<String> dbNames);
    //递归找父节点
    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where tenantid=#{tenantId} and " +
            "    guid in " +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid where category.tenantid=#{tenantId} " +
            ")" +
            "SELECT distinct * from categoryTree where guid not in " +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<CategoryPrivilegeV2> getParentCategory(@Param("guid") List<String> guid, @Param("categoryType") int categoryType,@Param("tenantId") String tenantId);

    @Delete("<script>" +
            " delete from category_group_relation where group_id=#{userGroupId} and category_id in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public int deleteCategoryPrivilege(@Param("ids") List<String> ids,@Param("userGroupId")String userGroupId);

    @Select("select *,true \"read\",true editItem,true editCategory from category where categoryType=#{categoryType} and tenantid=#{tenantId}")
    public List<CategoryPrivilegeV2> getAllCategoryPrivilege(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId);

    @Select("select c.group_id id,read,edit_category editCategory,edit_item editItem,category_id categoryId  from category_group_relation c " +
            " join user_group u on c.group_id=u.id where c.category_id=#{guid} and u.tenant=#{tenantId}")
    public List<GroupPrivilege> getCategoryGroupPrivileges(@Param("guid") String guid, @Param("tenantId")String tenantId);

    @Insert("<script>" +
            "insert into category_group_relation(category_id,group_id,read,edit_category,edit_item) values " +
            "    <foreach item='groupPrivilege' index='index' collection='groupIds' " +
            "    open='(' separator='),(' close=')'>" +
            "    #{groupPrivilege.categoryId},#{groupPrivilege.id},#{groupPrivilege.read},#{groupPrivilege.editCategory},#{groupPrivilege.editItem}" +
            "    </foreach>" +
            "</script>")
    public int addUserGroupPrivileges(@Param("groupIds") List<GroupPrivilege> groupIds);

    @Insert("<script>" +
            "insert into category_group_relation(category_id,group_id,read,edit_category,edit_item) values " +
            "    <foreach item='groupPrivilege' index='index' collection='groupIds' " +
            "    open='' separator=',' close=''>" +
            "       <foreach item='category' index='index' collection='categoryIds' " +
            "       open='' separator=',' close=''>" +
            "       (#{category.guid},#{groupPrivilege.id},#{groupPrivilege.read},#{groupPrivilege.editCategory},#{groupPrivilege.editItem}) " +
            "       </foreach>" +
            "    </foreach>" +
            "</script>")
    public int addUserGroupCategoryPrivileges(@Param("groupIds") List<GroupPrivilege> groupIds,@Param("categoryIds") List<CategoryEntityV2> categoryIds);


    @Update ("<script>" +
             "update category_group_relation set read=tmp.read,edit_category=tmp.edit_category,edit_item=tmp.edit_item from (values " +
             "    <foreach item='groupPrivilege' index='index' collection='groupPrivileges' " +
             "    open='(' separator='),(' close=')'>" +
             "    #{groupPrivilege.categoryId},#{groupPrivilege.id},#{groupPrivilege.read},#{groupPrivilege.editCategory},#{groupPrivilege.editItem}" +
             "    </foreach>" +
             " ) as tmp (category_id,group_id,read,edit_category,edit_item) " +
             " where category_group_relation.category_id=tmp.category_id and category_group_relation.group_id=tmp.group_id " +
             "</script>")
    public int updateUserGroupPrivileges(@Param("groupPrivileges") List<GroupPrivilege> groupPrivileges);

    @Delete("<script>" +
            " delete from category_group_relation where group_id in " +
            "    <foreach item='id' index='index' collection='userGroupIds'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            " and category_id in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public int deleteGroupPrivilege(@Param("ids") List<String> ids,@Param("userGroupIds")List<String> userGroupIds);

    //实现用户组列表及搜索
    @Results({
            @Result(property = "businessInfos",javaType = List.class,column = "{categoryId = categoryIdVal,tenantId = tenantId,userGroupId=groupId}",many = @Many(select = "queryBusinessesByUserGroup"))
    })
    @Select("<script>" +
            "select count(*) over() totalSize,u.id,u.id groupId,u.name,u.description,u.creator,u.createtime,u.updatetime,u.authorize_user authorize,u.authorize_time authorizeTime,c.read,c.edit_item editItem,c.edit_category editCategory, " +
            "c.category_id categoryId,c.category_id categoryIdVal,#{tenantId} tenantId" +
            " from user_group u join " +
            " category_group_relation c " +
            " on u.id=c.group_id " +
            " where u.tenant=#{tenantId} and u.valid=true and c.category_id=#{category.guid} " +
            "<if test='category.read'>" +
            " and c.read=true " +
            "</if>" +
            "<if test='category.editCategory'>" +
            " and c.edit_category=true " +
            "</if>" +
            "<if test='category.editItem'>" +
            " and c.edit_item=true " +
            "</if>" +
            "<if test='parameters.query!=null'>" +
            " and u.name like concat('%',#{parameters.query},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='parameters.sortBy!=null'>" +
            " order by ${parameters.sortBy} " +
            "<if test='parameters.order!=null '>" +
            " ${parameters.order} " +
            "</if>" +
            "</if>" +
            "<if test='parameters.limit!=-1'>" +
            " limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            " offset ${parameters.offset} " +
            "</if>" +
            "</script>")
    public List<GroupPrivilege> getUserGroupByCategory(@Param("category")CategoryGroupPrivilege category, @Param("parameters")Parameters parameters, @Param("tenantId")String tenantId);

    @Select("select bi.businessid businessId, bi.name " +
            "from businessinfo bi " +
            "inner join business_relation br on br.businessid=bi.businessid and br.categoryguid=#{categoryId} " +
            "join business_2_group bg on bg.business_id=bi.businessid and bg.group_id=#{userGroupId} " +
            "where bi.tenantid=#{tenantId}")
    List<BusinessInfo> queryBusinessesByUserGroup(@Param("categoryId")String categoryId, @Param("tenantId")String tenantId, @Param("userGroupId")String userGroupId);

    //更新用户组
    @Update("<script>" +
            "update user_group set updatetime=#{updateTime},authorize_user=#{userId},authorize_time=#{updateTime} where id in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public int updateUserGroups(@Param("ids") List<String> ids, @Param("updateTime") Timestamp updateTime,@Param("userId") String userId);

    @Select({"<script>" ,
                "select count(*) from user_group_relation u join user_group g on u.group_id=g.id and g.tenant=#{tenantId} join category_group_relation c on g.id=c.group_id " +
                " where u.user_id=#{userId} and c.edit_item=true and c.category_id in ",
                "<foreach item='categoryId' index='index' collection='categoryIds' open='(' separator=',' close=')'>" ,
                    "#{categoryId}",
                "</foreach>" +
            "</script>"})
    public int useCategoryPrivilege(@Param("userId") String userId,@Param("categoryIds") List<String> categoryIds,@Param("tenantId") String tenantId);

    @Insert("<script>" +
            "INSERT INTO category_group_relation(category_id,group_id,read,edit_category,edit_item) values " +
            "    <foreach item='groupId' index='index' collection='userGroupIds' " +
            "    open='(' separator='),(' close=')'>" +
            "    #{categoryId},#{groupId},#{read},#{editCategory},#{editItem}" +
            "    </foreach>" +
            " ON conflict (group_id,category_id) DO NOTHING" +
            "</script>")
    void insertGroupRelations(@Param("userGroupIds") List<String> userGroupIds,@Param("categoryId")  String guid,@Param("read")  Boolean read, @Param("editCategory") Boolean editCategory, @Param("editItem") Boolean editItem);
	
	
    @Select("<script>" +
            "select count(*)over() totalSize,dgr.id, di.database_name databaseName," +
            " case when dgr.source_id = 'hive' then 'hive' else d.source_name end as sourceName," +
            " case when dgr.source_id = 'hive' then 'HIVE' else d.source_type end as sourceType " +
            " from database_group_relation dgr " +
            " left join data_source d  " +
            " on d.source_id=dgr.source_id " +
            " inner join db_info di  " +
            " on di.database_guid=dgr.database_guid " +
            " where dgr.group_id = #{groupId} " +
            " and di.status='ACTIVE' " +
            "<if test='sourceId!=null'>" +
            " and dgr.source_id =#{sourceId}" +
            "</if>"+
            "<if test='search!=null'>" +
            " and di.database_name like concat('%',#{search},'%') ESCAPE '/' " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupDatabaseResult> getDatabaseBySearch(@Param("groupId") String groupId, @Param("offset") int offset, @Param("limit") int limit, @Param("sourceId") String sourceId,@Param("search") String search);

    @Select("<script>" +
            "select count(*)over() totalSize, id, databaseName,source_name,source_type from(" +
            " select dgr.id, di.database_name databaseName,source_name,source_type " +
            " from database_group_relation dgr " +
            "  left join data_source d  " +
            "  on d.source_id=dgr.source_id " +
            "  inner join db_info di  " +
            "  on di.database_guid=dgr.database_guid " +
            "  where dgr.group_id = #{groupId} " +
            "  and di.status='ACTIVE' and dgr.source_id != 'hive' " +
            "  and d.source_id is not null" +
            "  <if test='sourceId!=null'>" +
            "   and dgr.source_id =#{sourceId}" +
            "  </if>"+
            "  <if test='search!=null'>" +
            "   and di.database_name like concat('%',#{search},'%') ESCAPE '/' " +
            "  </if>" +
            " union " +
            " select dgr.id, di.database_name databaseName, " +
            "  'hive' as sourceName, " +
            "  'HIVE' as sourceType " +
            "  from database_group_relation dgr  " +
            "   inner join db_info di   " +
            "   on di.database_guid=dgr.database_guid  " +
            "   where dgr.group_id = #{groupId}  " +
            "   and di.status='ACTIVE' and dgr.source_id = 'hive' "+
            "  <if test='sourceId!=null'>" +
            "   and dgr.source_id =#{sourceId}" +
            "  </if>"+
            "  <if test='search!=null'>" +
            "   and di.database_name like concat('%',#{search},'%') ESCAPE '/' " +
            "  </if>" +
            " )as A " +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupDatabaseResult> getDatabaseBySearchStr(@Param("groupId") String groupId, @Param("offset") int offset, @Param("limit") int limit, @Param("sourceId") String sourceId,@Param("search") String search);

    @Insert("insert into database_group_relation (id,group_id,source_id,database_guid) values (#{id},#{groupId},#{sourceId},#{databaseGuid})")
    public Integer addDataBaseByGroupId(@Param("id") String id, @Param("groupId") String groupId,@Param("sourceId") String sourceId,@Param("databaseGuid") String databaseGuid);

    @Delete({"<script>",
            "delete from database_group_relation where id in ",
            "<foreach collection='idsList' item='id' index='index' separator=',' open='(' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"})
    public void deleteDataBaseByGroupId(@Param("idsList") List<String> idsList);


    @Select({"<script>" ,
            "select dgr.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,ds.ip,ds.description from datasource_group_relation dgr" +
            " inner join data_source ds on dgr.source_id=ds.source_id" +
            " where dgr.group_id=#{groupId} " +
            "<if test='search != null'>" +
            " and ds.source_name like concat('%',#{search},'%') ESCAPE '/' " +
            "</if>" +
            "</script>"})
    public List<NotAllotDatabaseSearchResult> getSourceIdByGroupId(@Param("groupId") String groupId, @Param("search") String search);

    @Select({"<script>" ,
            "select sd.db_guid databaseGuid,di.database_name databaseName  from source_db sd" +
            " inner join db_info di on di.database_guid=sd.db_guid and di.status='ACTIVE'" +
            " where sd.source_id=#{sourceId}" +
            " and sd.db_guid not in (select database_guid from database_group_relation where group_id=#{groupId} and source_id=#{sourceId})"+
            "</script>"})
    public List<DBInfo> getDataBasesBysourceId(@Param("groupId") String groupId, @Param("sourceId") String sourceId);


    @Select({"<script>",
            "select count(*) from database_group_relation where source_id =#{sourceId} and database_guid=#{databaseGuid}"+
            "<if test='groupIds != null and groupIds.size()>0'>" +
            " and group_id in"+
            "<foreach collection='groupIds' item='id' index='index' separator=',' open='(' close=')'>",
            "#{id}",
            "</foreach>"+
            "</if>" +
            "<if test='groupIds.size()==0'>" +
            " and group_id is null"+
             "</if>" +
            "</script>"})
    public int getDatabaseIdNum(@Param("groupIds") List<String> groupIds,@Param("sourceId") String sourceId,@Param("databaseGuid") String databaseGuid);

    @Select({"<script>",
            "select count(*) from database_group_relation where source_id =#{sourceId} and group_id=#{groupId}",
            "</script>"})
    public int getDatabaseGroupRelationNum(@Param("sourceId") String sourceId,@Param("groupId") String groupId);


    @Select({"<script>",
            "select source_name from data_source where source_id =#{sourceId}",
            "</script>"})
    public String getSourceName(@Param("sourceId") String sourceId);

    @Delete("<script>" +
            "delete from business_2_group " +
            "where " +
            "group_id in " +
            "<foreach collection='groupIds' item='groupId' index='index' separator=',' open='(' close=')'>" +
            "#{groupId}" +
            "</foreach>" +
            " and business_id in " +
            "<foreach collection='businessIds' item='businessId' index='index' separator=',' open='(' close=')'>" +
            "#{businessId}" +
            "</foreach>" +
            "</script>" )
    void deleteBusinessPrivileges(@Param("businessIds")List<String> businessIds, @Param("groupIds")List<String> groupIds);

    @Insert("<script>" +
            "insert into business_2_group(business_id, group_id, read) values" +
            "<foreach item='businessId' index='index' collection='businessIds' open='' separator=',' close=''>" +
            "<foreach item='groupId' index='index' collection='groupIds' open='(' separator='),(' close=')'>" +
            "#{businessId}, #{groupId}, #{read}" +
            "</foreach>" +
            "</foreach>" +
            "</script>")
    void addBusinessPrivileges(@Param("businessIds")List<String> businessIds, @Param("groupIds")List<String> groupIds, @Param("read")Boolean read);

    @Select("<script>" +
            "select bi.businessid businessId, bi.name " +
            "from businessinfo bi " +
            "<if test='groupId!=null'>" +
            "inner join business_2_group b2g on b2g.business_id=bi.businessid and b2g.group_id=#{groupId} " +
            "</if>" +
            "where bi.tenantid=#{tenantId}" +
            "</script>")
    List<BusinessInfo> getBusinessesByTenantId(@Param("tenantId")String tenantId, @Param("groupId")String groupId);


    @Select("<script>" +
            " SELECT c.*,case  when ai.status is null then '0' else ai.status end FROM category c left join approval_item ai on ai.id=c.approval_id  WHERE c.tenantid = #{tenantId} AND c.private_status = 'PUBLIC' AND c.categorytype = #{categoryType}" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT c.*,case  when ai.status is null then '0' else ai.status end FROM category c INNER JOIN category_group_relation as relation on c.guid = relation.category_id " +
            " left join approval_item ai on ai.id=c.approval_id"+
            " WHERE c.tenantid = #{tenantId} AND c.private_status = 'PRIVATE' AND c.categorytype =#{categoryType} AND relation.group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            " UNION" +
            " SELECT c.*,case  when ai.status is null then '0' else ai.status end FROM category c" +
            " left join approval_item ai on ai.id=c.approval_id"+
            " WHERE c.tenantid = #{tenantId} AND c.private_status = 'PRIVATE' AND c.categorytype =#{categoryType} and c.creator=#{creator}"+
            "</script>")
    public List<CategorycateQueryResult> getAllCategory(@Param("groupIdList") List<String> groupIdList,@Param("categoryType") int categoryType,@Param("tenantId")String tenantId,@Param("creator")String creator);


    @Select("<script>" +
            " SELECT * FROM category c" +
            " WHERE parentcategoryguid = #{guid}" +
            "</script>")
    public List<CategorycateQueryResult> getCanotDeleteChrildCategory(@Param("guid") String guid);

    @Select("<script>" +
            " SELECT c.* FROM category c left join approval_item ai on ai.id=c.approval_id  WHERE  c.private_status = 'PUBLIC' AND c.categorytype = #{categoryType}" +
            " <if test='groupIdList != null and groupIdList.size() > 0'>"+
            " UNION" +
            " SELECT DISTINCT c.* FROM category c INNER JOIN category_group_relation as relation on c.guid = relation.category_id " +
            " left join approval_item ai on ai.id=c.approval_id"+
            " WHERE  c.private_status = 'PRIVATE' AND c.categorytype =#{categoryType} AND relation.group_id in" +
            " <foreach item='item' index='index' collection='groupIdList' separator=',' open='(' close=')'>" +
            "   #{item} "+
            " </foreach>" +
            " </if>"+
            " UNION" +
            " SELECT c.* FROM category c" +
            " WHERE  c.private_status = 'PRIVATE' AND c.categorytype =#{categoryType} and c.creator=#{creator}"+
            "</script>")
    public List<CategoryEntityV2> getAllCategoryByCommonTenant(@Param("groupIdList") List<String> groupIdList, @Param("categoryType") int categoryType, @Param("creator")String creator);

    @Select("select c.*,case  when ai.status is null then '0' else ai.status end from category c left join approval_item ai on ai.id=c.approval_id where c.categoryType=#{categoryType} and c.tenantid=#{tenantId} and guid=#{guid}")
    public CategorycateQueryResult getCategory(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId);


    @Select({"<script>",
            "select distinct read,edit_category as editCategory,edit_item as editItem FROM category_group_relation " +
            "WHERE category_id = #{guid}"+
            "<choose>" +
            " <when test='userGroupIds != null and userGroupIds.size()>0'>" +
                " and group_id in "+
                "<foreach collection='userGroupIds' item='id' index='index' separator=',' open='(' close=')'>",
                "#{id}",
                "</foreach>"+
            " </when>" +
            " <otherwise>" +
            "   and category_id is null" +
            " </otherwise>" +
            "</choose>" +
            "</script>"})
    public List<UserGroupPrivilege> getCataUserGroupPrivilege(@Param("guid") String guid, @Param("userGroupIds") List<String> userGroupIds);


    @Select("select g.*,g.tenant tenantId from user_group g join user_group_relation u on g.id=u.group_id where u.user_id=#{userId} and g.valid=true")
    public List<UserGroup> getAlluserGroupByUsersId(@Param("userId") String userId);


    @Select("select count(*) from category_group_relation where category_id=#{guid}")
    int getCateUserGroupRelationNum(@Param("guid") String guid);

	@Select("select g.*,g.tenant tenantId from user_group g join user_group_relation u on g.id=u.group_id where u.user_id=#{userId} and g.valid=true")
    List<UserGroup> selectListByUsersId(@Param("userId") String userId);

    @Select({"<script>" ,
            "select di.database_guid databaseGuid,di.database_name databaseName from db_info di " +
                    " where di.status='ACTIVE' and di.db_type='HIVE'" +
                    " and di.database_name in " +
                    " <foreach collection='dbs' item='item' separator=',' open='(' close=')'>"+
                    "    #{item}"+
                    "  </foreach>" +
                    " and di.database_guid not in (select database_guid from database_group_relation " +
                    " where group_id=#{groupId} and source_id=#{sourceId})" +
                    "</script>"})
    public List<DBInfo> getNotAuthHiveDataBases(@Param("groupId") String groupId, @Param("sourceId") String sourceId,
                                                @Param("dbs") List<String> dbs);

    @Delete("delete from business_2_group where group_id=#{groupId} and business_id=#{businessId}")
    void removeBusiness(@Param("groupId")String groupId, @Param("businessId")String businessId);

    @Select("<script>" +
            "select bi.businessid businessId, bi.name, br.categoryguid categoryGuid " +
            "from business_relation br " +
            "join businessinfo bi on bi.businessid=br.businessid and bi.tenantid=#{tenantId} " +
            "join business_2_group bg on bg.business_id=bi.businessid and bg.group_id=#{groupId} " +
            "where br.categoryguid in " +
            "<foreach collection='categoryGuids' item='id' index='index' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<BusinessInfoHeader> getBusinessesByGroup(@Param("categoryGuids")List<String> categoryGuids, @Param("groupId")String groupId, @Param("tenantId")String tenantId);


    List<String> getDbIds(List<String> ids);

    List<String> getUserGroupByUsrId(@Param("userId") String userId,@Param("tenantId") String tenantId);

    List<GroupAccountDTO> getAccountByUserGroupIds(@Param("ids") List<String> groupIds);
}
