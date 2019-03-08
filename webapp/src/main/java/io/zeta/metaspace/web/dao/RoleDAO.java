package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import io.zeta.metaspace.model.result.RoleModulesCategories;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RoleDAO {
    @Insert("insert into role values(#{role.roleId},#{role.roleName},#{role.description},#{role.privilegeId},#{role.updateTime},#{role.status},#{role.createTime},#{role.disable},#{role.delete},#{role.edit})")
    public int addRoles(@Param("role") Role role);

    @Select("select 1 from role where rolename=#{roleName}")
    public List<Integer> ifRole(@Param("roleName") String roleName);

    @Update("update role set status=#{status} where roleid=#{roleId}")
    public int updateRoleStatus(@Param("roleId") String roleId, @Param("status") int status);

    @Delete("delete from role where roleid=#{roleId}")
    public int deleteRole(String roleId);

    @Select("select userid,username,account,users.roleid,rolename from users,role where users.roleid=role.roleid and users.roleid=#{roleId} and username like '%'||#{query}||'%' order by username limit #{limit} offset #{offset}")
    public List<User> getUsers(@Param("roleId") String roleId, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

    @Select("select userid,username,account,users.roleid,rolename from users,role where users.roleid=role.roleid and users.roleid=#{roleId} and username like '%'||#{query}||'%' order by username offset #{offset}")
    public List<User> getUser(@Param("roleId") String roleId, @Param("query") String query, @Param("offset") long offset);

    @Select("select count(1) from users where roleid=#{roleId} and username like '%'||#{query}||'%'")
    public long getUsersCount(@Param("roleId") String roleId, @Param("query") String query);

//    @Select("select DISTINCT aa.*,COUNT(1) OVER (PARTITION BY aa.roleid) members from (select role.*,privilegename from role,privilege where role.privilegeid=privilege.privilegeid and rolename like '%'||#{query}||'%' ) aa left join users on  aa.roleid=users.roleid order by roleId limit #{limit} offset #{offset}")
    @Select("select role.*,privilegename,(select count(1) from users where users.roleid=role.roleid) members from role,privilege where role.privilegeid=privilege.privilegeid and rolename like '%'||#{query}||'%' order by roleid limit #{limit} offset #{offset}")
    public List<Role> getRoles(@Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

//    @Select("select DISTINCT aa.*,COUNT(1) OVER (PARTITION BY aa.roleid) members from (select role.*,privilegename from role,privilege where role.privilegeid=privilege.privilegeid and rolename like '%'||#{query}||'%' ) aa left join users on  aa.roleid=users.roleid order by roleId offset #{offset}")
    @Select("select role.*,privilegename,(select count(1) from users where users.roleid=role.roleid) members from role,privilege where role.privilegeid=privilege.privilegeid and rolename like '%'||#{query}||'%' order by roleid offset #{offset}")
    public List<Role> getRole(@Param("query") String query, @Param("offset") long offset);

    @Select("select count(1) from role where rolename like '%'||#{query}||'%'")
    public long getRolesCount(@Param("query") String query);

    //添加成员&更换一批人的角色
    //@Update("update user set roleid=#{roleId} where userid in ")
    @Update({"<script>update users set roleid=#{roleId} where userid in",
            "<foreach item='item' index='index' collection='userIds'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    public int updateUsers(@Param("roleId") String roleId, @Param("userIds") List<String> userIds);

    //移除成员&更换角色
    @Update("update users set roleid=#{roleId} where userid=#{userId}")
    public int updateUser(@Param("roleId") String roleId, @Param("userId") String userId);


    //获取角色方案
    @Select("select privilege.privilegeid,privilegename from role,privilege where role.privilegeid=privilege.privilegeid and roleid=#{roleId}")
    public PrivilegeInfo getPrivilegeByRoleId(String roleId);


    //修改角色方案
    @Update("update role set privilegeid=#{privilegeId},updatetime=#{updateTime} where roleid=#{roleId}")
    public int updateCategory(@Param("privilegeId") String privilegeId, @Param("roleId") String roleId, @Param("updateTime") String updateTime);

    //删除授权范围
    @Delete("delete from role2category where roleid=#{roleId}")
    public int deleteRole2category(String roleId);

    @Delete("delete from role2category where roleId in (select roleId from users where userId=#{userId})")
    public int deleteRole2categoryByUserId(String userId);

    //添加授权范围
    @Insert("insert into role2category values(#{roleId},#{categoryId},#{operation})")
    public int addRole2category(@Param("roleId") String roleId, @Param("categoryId") String categoryId, @Param("operation") int operation);

    //根据userid查roleid
    @Select("select roleid from users where userid=#{userId}")
    public String getRoleIdByUserId(String userId);

    //递归找子节点
    @Select("WITH RECURSIVE categoryTree AS " +
            "(" +
            "    SELECT * from category" +
            "    where parentCategoryGuid = #{parentCategoryGuid} and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.guid = category.parentCategoryGuid" +
            ")" +
            "SELECT * FROM categoryTree")
    public List<RoleModulesCategories.Category> getChilds(@Param("parentCategoryGuid") String parentCategoryGuid, @Param("categoryType") int categoryType);

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

    //递归找父节点，结果集包含自己
    @Select("<script>WITH RECURSIVE categoryTree AS" +
            "(" +
            "    SELECT * from category where " +
            "    guid =#{guid} " +
            "    and categoryType=#{categoryType}" +
            "    UNION " +
            "    SELECT category.* from categoryTree" +
            "    JOIN category on categoryTree.parentCategoryGuid= category.guid" +
            ")" +
            "SELECT * from categoryTree</script>")
    public List<RoleModulesCategories.Category> getParents(@Param("guid") String guid, @Param("categoryType") int categoryType);

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

    //获取授权范围id
    @Select("select categoryid guid from role2category,category where role2category.categoryid=category.guid and roleid=#{roleId} and categorytype=#{categoryType}")
    public List<String> getCategorysByTypeIds(@Param("roleId") String roleId, @Param("categoryType") int categoryType);

    //查找权限节点
    @Select("select categoryid guid,name,parentcategoryguid,upbrothercategoryguid,downbrothercategoryguid from role2category,category where role2category.categoryid=category.guid and roleid=#{roleId} and categorytype=#{categoryType}")
    public List<RoleModulesCategories.Category> getCategorysByType(@Param("roleId") String roleId, @Param("categoryType") int categoryType);

    @Select("select * from category where categoryType=#{categoryType}")
    public List<RoleModulesCategories.Category> getAllCategorys(@Param("categoryType") int categoryType);


    @Update({
            "<script>",
            "<foreach item='roleId' index='index' collection='roleIds' separator=';'>",
            "update role set privilegeId=#{privilegeId} where roleId=#{roleId}",
            "</foreach>",
            "</script>"
    })
    public int updatePrivilege(@Param("roleId") String roleId);

    @Select("select DISTINCT tableinfo.dbname from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid=#{guid} and tableinfo.dbname like '%'||#{query}||'%' order by tableinfo.dbname limit #{limit} offset #{offset}")
    public List<String> getDBNames(@Param("guid") String guid, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

    @Select("select DISTINCT tableinfo.dbname from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid=#{guid} and tableinfo.dbname like '%'||#{query}||'%' order by tableinfo.dbname offset #{offset}")
    public List<String> getDBName(@Param("guid") String guid, @Param("query") String query, @Param("offset") long offset);

    @Select("select COUNT(DISTINCT tableinfo.dbname) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid=#{guid} and tableinfo.dbname like '%'||#{query}||'%' ")
    public long getDBCount(@Param("guid") String guid, @Param("query") String query);

    @Select("select * from tableinfo where dbname=#{DBName}")
    public List<TechnologyInfo.Table> getTableByDBName(String DBName);

    @Select("select tableinfo.* from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid=#{guid} and tableinfo.tablename like '%'||#{query}||'%' order by tableinfo.tablename limit #{limit} offset #{offset}")
    public List<TechnologyInfo.Table> getTableInfos(@Param("guid") String guid, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

    @Select("select tableinfo.* from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid=#{guid} and tableinfo.tablename like '%'||#{query}||'%' order by tableinfo.tablename offset #{offset}")
    public List<TechnologyInfo.Table> getTableInfo(@Param("guid") String guid, @Param("query") String query, @Param("offset") long offset);

    @Select("select count(1) from category,table_relation,tableinfo where category.guid=table_relation.categoryguid and table_relation.tableguid=tableinfo.tableguid and category.guid=#{guid} and tableinfo.tablename like '%'||#{query}||'%'")
    public long getTableCount(@Param("guid") String guid, @Param("query") String query);

    @Select("select * from category where guid=#{guid}")
    public RoleModulesCategories.Category getCategoryByGuid(String guid);

    @Select("select role.* from users,role where users.roleid=role.roleid and userId=#{userId}")
    public Role getRoleByUsersId(String userId);

}
