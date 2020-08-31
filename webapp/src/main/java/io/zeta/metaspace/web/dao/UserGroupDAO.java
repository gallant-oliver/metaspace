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

import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.metadata.CategoryExport;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.CategoryGroupPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.CategoryPrivilegeV2;
import io.zeta.metaspace.model.result.GroupPrivilege;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.share.ProjectHeader;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupPrivileges;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupMemberSearch;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

import javax.ws.rs.DELETE;

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
            " on g.id=r.group_id " +
            " GROUP BY g.id) m " +
            " on u.id=m.id " +
            " where u.tenant=#{tenantId} and valid=true" +
            "<if test='search!=null'>" +
            " and u.name like '%${search}%' ESCAPE '/' " +
            "</if>" +
            "<if test='sortBy!=null'>" +
            "order by ${sortBy} " +
            "</if>" +
            "<if test='order!=null '>" +
            " ${order} " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupListAndSearchResult> getUserGroupSortByUpdateTime(@Param("tenantId") String tenantId, @Param("offset") int offset, @Param("limit") int limit,@Param("sortBy")String sortBy, @Param("order") String order, @Param("search") String search);


    @Select("select username from users where userid=#{userId}")
    public String getUserNameById(String userId);


    /**
     * 二.用户组详情
     */
    @Select("select name,description from user_group where id=#{id}")
    public UserGroup getUserGroupByID(String id);

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

    @Delete("delete from datasource_group_relation where group_id=#{id}")
    public void deleteUserGroupDataSourceRelationByID(String id);

    @Delete("delete from project_group_relation where group_id=#{id}")
    public void deleteUserGroupProjectRelationByID(String id);


    /**
     * 五.用户组成员列表及搜索
     */
    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username,u.account from users u join user_group_relation g on u.userid=g.user_id " +
            "where g.group_id=#{id} " +
            "<if test='search!=null'>" +
            " and u.username like '%${search}%' ESCAPE '/' " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<MemberListAndSearchResult> getMemberListAndSearch(@Param("id")String id,@Param("offset") int offset, @Param("limit") int limit, @Param("search") String search);


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
            "    <when test=\"categoryType==1\">" +
            "        categoryguid as guid from business_relation " +
            "    </when>" +
            "    <when test=\"categoryType==3\">" +
            "        guid from ( " +
            "          select b.number,b.categoryid as guid from " +
            "          (select number,max(version) as version from data_standard where delete=false and tenantid=#{tenantId} group by number) as a " +
            "          inner join data_standard b on a.number=b.number and a.version=b.version ) ds  " +
            "    </when>" +
            "    <otherwise>" +
            "        category_id as guid from data_quality_rule where delete=false" +
            "    </otherwise>" +
            "</choose>" +
            " group by guid" +
            ") item on category.guid=item.guid " +
            "where categoryType=#{categoryType} and tenantid=#{tenantId}" +
            "</script>")
    public List<RoleModulesCategories.Category> getAllCategorysAndCount(@Param("categoryType") int categoryType,@Param("tenantId")String tenantId,@Param("dbNames") List<String> dbNames);

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
            "        guid from ( " +
            "          select b.number,b.categoryid as guid from " +
            "          (select number,max(version) as version from data_standard where delete=false and tenantid=#{tenantId} group by number) as a " +
            "          inner join data_standard b on a.number=b.number and a.version=b.version ) ds  " +
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
            "        guid from ( " +
            "          select b.number,b.categoryid as guid from " +
            "          (select number,max(version) as version from data_standard where delete=false and tenantid=#{tenantId} group by number) as a " +
            "          inner join data_standard b on a.number=b.number and a.version=b.version ) ds  " +
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

    @Select("<script>select distinct tableinfo.tableguid,tableinfo.tablename,tableinfo.dbname,tableinfo.status,tableinfo.createtime,tableinfo.databaseguid from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.tenantid=#{tenantId} and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.tablename like '%'||#{query}||'%' ESCAPE '/' " +
            "    and tableinfo.dbname in " +
            "    <foreach item='item' index='index' collection='databases'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     order by tableinfo.tablename <if test='limit!= -1'>limit #{limit}</if> offset #{offset}" +
            "</script>")
    public List<TechnologyInfo.Table> getTableInfosV2(@Param("guids") List<String> guids, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit,@Param("databases")List<String> databases,@Param("tenantId") String tenantId);

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
            " and u.name like '%${parameters.query}%' ESCAPE '/'  " +
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
            " and d.source_name like '%${search}%' ESCAPE '/' " +
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
            " and d.source_name like '%${search}%' ESCAPE '/' " +
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
            " and p.name like '%${param.query}%' ESCAPE '/' " +
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
            " and p.name like '%${param.query}%' ESCAPE '/' " +
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
            " and name like '%${param.query}%' ESCAPE '/' " +
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
            "SELECT *,g.category_id guid,g.read,g.edit_category editCategory,g.edit_item editItem FROM categoryTree c left join category_group_relation g on c.guid=g.category_id and g.group_id=#{userGroupId} " +
            "</script>")
    public List<CategoryPrivilegeV2> getChildCategoriesPrivileges(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("userGroupId")String userGroupId, @Param("categoryType") int categoryType, @Param("tenantId") String tenantId);

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
             "<if test='privilege.editCategory==true'>" +
             " ,edit_category=true " +
             "</if>" +
             "<if test='privilege.editItem==true'>" +
             " ,edit_item=true " +
             "</if>" +
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
            "        guid from ( " +
            "          select b.number,b.categoryid as guid from " +
            "          (select number,max(version) as version from data_standard where delete=false and tenantid=#{tenantId} group by number) as a " +
            "          inner join data_standard b on a.number=b.number and a.version=b.version ) ds  " +
            "    </when>" +
            "    <otherwise>" +
            "        category_id as guid from data_quality_rule where delete=false" +
            "    </otherwise>" +
            "</choose>" +
            " group by guid" +
            ") item on c.guid=item.guid " +
            " where c.tenantid=#{tenantId} and c.categorytype=#{categoryType}" +
            " and g.group_id in " +
            "    <foreach item='id' index='index' collection='userGroupIds'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public List<CategoryPrivilegeV2> getUserGroupsCategory(@Param("userGroupIds")List<String> userGroupIds, @Param("tenantId")String tenantId, @Param("categoryType") int categoryType,@Param("dbNames") List<String> dbNames);

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
    @Select("<script>" +
            "select count(*) over() totalSize,u.id,u.name,u.description,u.creator,u.createtime,u.updatetime,u.authorize_user authorize,u.authorize_time authorizeTime,c.read,c.edit_item editItem,c.edit_category editCategory " +
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
            " and u.name like '%${parameters.query}%' ESCAPE '/' " +
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

    //更新用户组
    @Update("<script>" +
            "update user_group set updatetime=#{updateTime},authorize_user=#{userId},authorize_time=#{updateTime} where id in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public int updateUserGroups(@Param("ids") List<String> ids, @Param("updateTime") Timestamp updateTime,@Param("userId") String userId);

    @Select(" select count(*) from user_group_relation u join user_group g on u.group_id=g.id and g.tenant=#{tenantId} join category_group_relation c on g.id=c.group_id " +
            " where u.user_id=#{userId} and c.category_id=#{categoryId} and c.edit_item=true")
    public int useCategoryPrivilege(@Param("userId") String userId,@Param("categoryId") String categoryId,@Param("tenantId") String tenantId);
}
