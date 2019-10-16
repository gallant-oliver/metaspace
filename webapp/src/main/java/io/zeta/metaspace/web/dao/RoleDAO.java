package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.pojo.TableInfo;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.user.User;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

public interface RoleDAO {
    @Insert("insert into role values(#{role.roleId},#{role.roleName},#{role.description},#{role.privilegeId},#{role.updateTime},#{role.status},#{role.createTime},#{role.disable},#{role.delete},#{role.edit},#{role.valid},#{role.creator},#{role.updater})")
    public int addRoles(@Param("role") Role role);

    @Select("select count(*) from role where rolename=#{roleName} and valid=true")
    public Integer ifRole(@Param("roleName") String roleName);

    @Update("update role set status=#{status},updateTime=#{updateTime},updater=#{updater} where roleid=#{roleId}")
    public int updateRoleStatus(@Param("roleId") String roleId, @Param("status") int status, @Param("updateTime") String updateTime, @Param("updater") String updater);

    @Update("update role set valid=#{valid},updater=#{updater},updateTime=#{updateTime} where roleId=#{roleId}")
    public int updateValidStatus(@Param("roleId") String roleId, @Param("valid") boolean valid, @Param("updater") String updater, @Param("updateTime") String updateTime);

    @Select({"<script>",
            " select userid,username,account,users.roleid,rolename",
            " from users,role",
            " where role.roleid=users.roleid",
            " and users.roleid=#{roleId}",
            " and users.valid=true and role.valid=true",
            " <if test=\"query != null and query!=''\">",
            " and (username like '%'||#{query}||'%' ESCAPE '/' or account like '%'||#{query}||'%' ESCAPE '/')",
            "</if>",
            " order by username",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<User> getUsers(@Param("roleId") String roleId, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);


    @Select({"<script>",
            " select count(1)",
            " from users,role",
            " where role.roleid=users.roleid",
            " and users.valid=true and role.valid=true",
            " and users.roleid=#{roleId}",
            " <if test=\"query != null and query!=''\">",
            " and (username like '%'||#{query}||'%' ESCAPE '/' or account like '%'||#{query}||'%' ESCAPE '/')",
            "</if>",
            " </script>"})
    public long getUsersCount(@Param("roleId") String roleId, @Param("query") String query);

    @Select({"<script>",
            " select role.*,privilegename,(select count(1)",
            " from users where users.roleid=role.roleid and users.valid=true) members",
            " from role,privilege where role.privilegeid=privilege.privilegeid and rolename like '%'||#{query}||'%' ESCAPE '/'",
            " and role.valid=true",
            " <if test='contain == false'>",
            " and status=1 and role.roleId!='1'",
            " </if>",
            " order by roleid",
            " <if test='limit!= -1'>",
            " limit #{limit} ",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<Role> getRoles(@Param("query") String query, @Param("offset") long offset, @Param("limit") long limit, @Param("contain") boolean contain);

    @Select("select * from role where status=1 and (updateTime>=#{startTime} or createTime>=#{startTime})")
    public List<Role> getIncrRoles(@Param("startTime") String startTime);

    @Select({"<script>",
            " select count(1) from role",
            " where rolename like '%'||#{query}||'%' ESCAPE '/'",
            " and valid=true",
            " <if test='contain == false'>",
            " and status=1 and roleId!='1'",
            " </if>",
            " </script>"})
    public long getRolesCount(@Param("query") String query, @Param("contain") boolean contain);

    //添加成员&更换一批人的角色
    @Update({"<script>update users set roleid=#{roleId},valid=#{valid},update_time=#{updateTime} where userid in",
            "<foreach item='item' index='index' collection='userIds'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    public int updateUsers(@Param("roleId") String roleId, @Param("userIds") List<String> userIds, @Param("valid") Boolean valid, @Param("updateTime") Timestamp updateTime);

    //添加成员&更换一个角色的成员的角色
    @Update("update users set roleid=#{roleId},update_time=#{updateTime} where userid in (select userid from users where roleid=#{oldRoleId} and users.valid=true)")
    public int updateUsersByRoleId(@Param("roleId") String roleId, @Param("oldRoleId") String oldRoleId, @Param("updateTime") Timestamp updateTime);

    //获取角色方案
    @Select("select privilege.privilegeid,privilegename from role,privilege where role.privilegeid=privilege.privilegeid and roleid=#{roleId} and valid=true")
    public PrivilegeInfo getPrivilegeByRoleId(String roleId);

    //修改角色方案
    @Update("update role set privilegeid=#{privilegeId},updatetime=#{updateTime} where roleid=#{roleId}")
    public int updateCategory(@Param("privilegeId") String privilegeId, @Param("roleId") String roleId, @Param("updateTime") String updateTime);

    //删除授权范围
    @Delete("delete from role2category where roleid=#{roleId}")
    public int deleteRole2category(String roleId);

    @Delete("delete from role2category where categoryId=#{guid}")
    public int deleteRole2categoryByUserId(String guid);

    //添加授权范围
    @Insert("insert into role2category values(#{roleId},#{categoryId},#{operation})")
    public int addRole2category(@Param("roleId") String roleId, @Param("categoryId") String categoryId, @Param("operation") int operation);

    //根据userid查roleid
    @Select("select roleid from users where userid=#{userId} and valid=true")
    public String getRoleIdByUserId(String userId);

    //递归找子节点
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where parentCategoryGuid in" +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid" +
            ")" +
            "SELECT * FROM categoryTree</script>")
    public List<RoleModulesCategories.Category> getChildCategorys(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("categoryType") int categoryType);

    //递归找子节点加自己
    @Select("<script>WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where parentCategoryGuid in " +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid" +
            ")" +
            "SELECT * FROM categoryTree" +
            " UNION " +
            "SELECT * FROM category where guid in " +
            "    <foreach item='item' index='index' collection='parentCategoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<RoleModulesCategories.Category> getChildAndOwnerCategorys(@Param("parentCategoryGuid") List<String> parentCategoryGuid, @Param("categoryType") int categoryType);

    //递归找父节点,结果集不包含自己了
    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where " +
            "    guid in " +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid" +
            ")" +
            "SELECT * from categoryTree where guid not in" +
            "    <foreach item='item' index='index' collection='guid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<RoleModulesCategories.Category> getParentCategorys(@Param("guid") List<String> guid, @Param("categoryType") int categoryType);

    //找出合集外的目录
    @Select("<script>SELECT * from category where " +
            "    <if test='categories!=null and categories.size()>0'>" +
            "     guid not in" +
            "    <foreach item='item' index='index' collection='categories'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item.guid}" +
            "    </foreach>" +
            "    and " +
            "    </if>" +
            "    categoryType = #{categoryType}" +
            "</script>")
    public List<RoleModulesCategories.Category> getOtherCategorys(@Param("categories") List<RoleModulesCategories.Category> categories, @Param("categoryType") int categoryType);

    //找出合集外的目录2
    @Select("<script>SELECT * from category where " +
            "    <if test='categories!=null and categories.size()>0'>" +
            "     guid not in" +
            "    <foreach item='item' index='index' collection='categories'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item.guid}" +
            "    </foreach>" +
            "    and " +
            "    </if>" +
            "    categoryType = #{categoryType}" +
            "</script>")
    public List<RoleModulesCategories.Category> getOtherCategorys2(@Param("categories") List<CategoryPrivilege> categories, @Param("categoryType") int categoryType);
    //获取授权范围id
    @Select("select categoryid guid from role2category,category where role2category.categoryid=category.guid and roleid=#{roleId} and categorytype=#{categoryType}")
    public List<String> getCategorysByTypeIds(@Param("roleId") String roleId, @Param("categoryType") int categoryType);

    //查找权限节点
    @Select("select * from role2category,category where role2category.categoryid=category.guid and roleid=#{roleId} and categorytype=#{categoryType}")
    public List<RoleModulesCategories.Category> getCategorysByType(@Param("roleId") String roleId, @Param("categoryType") int categoryType);

    @Select("select * from category where categoryType=#{categoryType}")
    public List<RoleModulesCategories.Category> getAllCategorys(@Param("categoryType") int categoryType);


    @Select("select role.* from users,role where users.roleid=role.roleid and userId=#{userId} and role.valid=true and users.valid=true")
    public Role getRoleByUsersId(String userId);

    @Select("select * from role where roleid=#{roleId} and valid=true")
    public Role getRoleByRoleId(String roleId);

    @Select("<script>select DISTINCT tableinfo.databaseGuid,tableinfo.dbname,tableinfo.databasestatus from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and tableinfo.dbname like '%'||#{query}||'%' ESCAPE '/' order by tableinfo.dbname <if test='limit!= -1'>limit #{limit}</if> offset #{offset}</script>")
    public List<DatabaseHeader> getDBInfo(@Param("guids") List<String> guids, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);


    @Select("<script>select COUNT(DISTINCT tableinfo.databaseGuid) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "    and tableinfo.dbname like '%'||#{query}||'%' ESCAPE '/'</script>")
    public long getDBCountV2(@Param("guids") List<String> guids, @Param("query") String query);

    @Select("<script>select distinct tableinfo.tableGuid,tableinfo.tableName,tableinfo.dbName,tableinfo.databaseGuid,tableinfo.display_name as displayName from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.databaseGuid=#{DB} order by tableinfo.tablename</script>")
    public List<TableInfo> getTableInfosByDBId(@Param("guids") List<String> guids, @Param("DB") String DB);

    @Select("<script>select distinct tableinfo.tableguid,tableinfo.tablename,tableinfo.dbname,tableinfo.status,tableinfo.createtime,tableinfo.databaseguid from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.tablename like '%'||#{query}||'%' ESCAPE '/' order by tableinfo.tablename <if test='limit!= -1'>limit #{limit}</if> offset #{offset}</script>")
    public List<TechnologyInfo.Table> getTableInfosV2(@Param("guids") List<String> guids, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

    @Select("<script>select count(1) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.tablename like '%'||#{query}||'%' ESCAPE '/'</script>")
    public long getTableCountV2(@Param("guids") List<String> guids, @Param("query") String query);

    @Select("select guid from category where categorytype=#{categoryType} and level = 1")
    public List<String> getTopCategoryGuid(int categoryType);

    @Update("update role set description=#{description},updateTime=#{updateTime},updater=#{updater} where roleid=#{roleId}")
    public int editRole(Role role);

    @Select("<script>select distinct tableinfo.tableGuid,tableinfo.tableName,tableinfo.dbName,tableinfo.databaseGuid,tableinfo.status,tableinfo.createtime from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.databaseGuid=#{DB} order by tableinfo.tablename <if test='limit!= -1'>limit #{limit}</if> offset #{offset}</script>")
    public List<TechnologyInfo.Table> getTableInfosByDBIdByParameters(@Param("guids") List<String> guids, @Param("DB") String DB, @Param("offset") long offset, @Param("limit") long limit);

    @Select("<script>select count(distinct tableinfo.tableGuid) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid in " +
            "    <foreach item='item' index='index' collection='guids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "     and tableinfo.databaseGuid=#{DB} </script>")
    public long getTableInfosByDBIdCount(@Param("guids") List<String> guids, @Param("DB") String DB);

    @Select("select userId from users where valid=true")
    public List<String> getUserIdList();

    @Update("update users set username=#{user.username},account=#{user.account},update_time=#{user.updateTime} where userId=#{user.userId}")
    public int updateUserInfo(@Param("user") User user);

    @Update("update users set valid=false and update_time=#{updateTime} where userId=#{userId}")
    public int deleteUser(@Param("userId") String userId, @Param("updateTime") Timestamp updateTime);
}
