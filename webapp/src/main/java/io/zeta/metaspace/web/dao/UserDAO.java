package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserDAO {
    @Select("select 1 from users where userid=#{userid}")
    public List<User> ifUserExists(String userid);

    @Insert("insert into users(userid,username,account,roleid) values(#{user.userId},#{user.username},#{user.account},#{user.roleId})")
    public int addUser(@Param("user") User user);

    @Select("select * from users where userId=#{userId}")
    public User getUser(@Param("userId") String userId);

    @Select("select * from role where roleId in (select roleId from users where userId=#{userId})")
    public Role getRoleByUserId(@Param("userId") String userId);

    @Select("select * from category where guid in (select categoryId from role2category where roleId=#{roleId}) and categoryType=0")
    public List<CategoryEntityV2> getTechnicalCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from category where guid in (select categoryId from role2category where roleId=#{roleId}) and categoryType=1")
    public List<CategoryEntityV2> getBusinessCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from module where moduleId in (select moduleId from privilege2module where privilegeId in (select privilegeId from role where roleId=#{roleId}))")
    public List<UserInfo.Module> getModuleByRoleId(@Param("roleId") String roleId);

    @Select("<script> select users.*,role.rolename from users join role on (users.roleId = role.roleId) where  username like '%${username}%' <if test='limit!= -1'> limit #{limit} </if> offset #{offset} </script>")
    public List<User> getUserList(@Param("username") String query, @Param("limit") int limit, @Param("offset") int offset);

    @Select("<script> select users.*,role.rolename from users join role on (users.roleId = role.roleId) where  username like '%${username}%' and role.roleid!='1' <if test='limit!= -1'> limit #{limit} </if> offset #{offset} </script>")
    public List<User> getUserListFilterAdmin(@Param("username") String query, @Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(1) from users where username like '%'||#{query}||'%'")
    public long getUsersCount(@Param("query") String query);

    @Select("<script>select count(1) from table_relation where categoryguid in" +
            "    <foreach item='item' index='index' collection='categoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            " and tableguid=#{tableGuid}</script>")
    public List<Integer> ifPrivilege(@Param("categoryGuid") List<String> categoryGuid, @Param("tableGuid") String tableGuid);

    @Select("select module.moduleid,modulename,type from users,role,privilege,privilege2module,module where users.roleid=role.roleid and role.privilegeid=privilege.privilegeid and privilege.privilegeid=privilege2module.privilegeid and privilege2module.moduleid=module.moduleid and userid=#{userId}")
    public List<Module> getModuleByUserId(String userId);


}
