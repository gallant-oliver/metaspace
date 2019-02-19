package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import org.apache.atlas.model.metadata.CategoryInfoV2;
import org.apache.ibatis.annotations.*;

import javax.ws.rs.PathParam;
import java.util.List;

public interface RoleDAO {
    @Insert("insert into role values(#{role.roleId},#{role.roleName},#{role.description},#{role.privilegeId},#{role.updateTime},#{role.status},#{role.createTime})")
    public int addRoles(Role role);

    @Update("update role set status=#{status} where roleid={roleId}")
    public int updateRoleStatus(@Param("roleId") String roleId, @Param("status") int status);

    @Delete("delete from role where roleid=#{roleId}")
    public int deleteRole(String roleId);

    @Select("select * from user where roleid=#{roleId} and username like '%'||#{query}||'%' order by username limit #{limit} offset #{offset}")
    public List<User> getUsers(@Param("roleId") String roleId, @Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

    @Select("select count(1) from user where roleid=#{roleId} and username like '%'||#{query}||'%'")
    public long getUsersCount(@Param("roleId") String roleId, @Param("query") String query);

    @Select("select * from role where rolename like '%'||#{query}||'%' order by rolename limit #{limit} offset #{offset}")
    public List<Role> getRoles(@Param("query") String query, @Param("offset") long offset, @Param("limit") long limit);

    @Select("select count(1) from role where rolename like '%'||#{query}||'%'")
    public long getRolesCount(@Param("query") String query);

    //添加成员&更换一批人的角色
    //@Update("update user set roleid=#{roleId} where userid in ")
    @Update({"<script>update user set roleid=#{roleId} where userid in",
            "<foreach item='item' index='index' collection='list'",
            "open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    public int updateUsers(@Param("roleId") String roleId, @Param("userIds") List<String> userIds);

    //移除成员&更换角色
    @Update("update user set roleid=#{roleId} where userid=#{userId}")
    public int updateUser(@Param("roleId") String roleId, @Param("userId") String userId);

   //获取授权范围
   @Select("select categoryid ,name categoryname,paretcategoryguid,upbrothercategoryguid,downbrothercategoryguid from role2category,category where role2category.categoryid=category.guid and roleid=#{roleId} and categorytype=#{categoryType}")
    public List<CategoryEntity> getCategorysByType(String roleId,int categoryType);

   //获取角色方案
    @Select("")
    public List<Module> getModulesBy

   //修改角色方案
    @Update("update role set privilegeid=#{privilegeId} where roleid=#{roleId}")
    public int updateCategorysByType(@Param("privilegeId") String privilegeId,@Param("roleId") String roleId);

    //删除授权范围
    @Delete("delete from role2category where roleid=#{roleId}")
    public int deleteCategorysByType(String roleId);

    //添加授权范围
    @Insert("insert into role2category values(#{roleId},#{categoryId},#{operation})")
    public int addCategorysByType(String roleId,String categoryId,int operation);

}
