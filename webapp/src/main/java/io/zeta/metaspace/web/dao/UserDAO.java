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
    @Select("select roleid from users where userid=#{userId} and valid=true")
    public String getRoleIdByUserId(String userId);


    @Select("select count(1) from users where userid=#{userid} and valid=true")
    public Integer ifUserExists(String userid);

    @Insert("insert into users(userid,username,account,roleid,create_time,update_time,valid) values(#{user.userId},#{user.username},#{user.account},#{user.roleId},#{user.createTime},#{user.updateTime},#{user.valid})")
    public int addUser(@Param("user") User user);

    @Select("select * from users where userId=#{userId} and valid=true")
    public User getUser(@Param("userId") String userId);

    @Select("select * from users where userId=#{userId}")
    public User getUserInfo(@Param("userId") String userId);

    @Select("select * from role where roleId in (select roleId from users where userId=#{userId} and users.valid=true) and role.valid=true")
    public Role getRoleByUserId(@Param("userId") String userId);

    @Select("select * from category where guid in (select categoryId from role2category where roleId=#{roleId}) and categoryType=0")
    public List<CategoryEntityV2> getTechnicalCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from category where guid in (select categoryId from role2category where roleId=#{roleId}) and categoryType=1")
    public List<CategoryEntityV2> getBusinessCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from module where moduleId in (select moduleId from privilege2module where privilegeId in (select privilegeId from role where roleId=#{roleId} and valid=true))")
    public List<UserInfo.Module> getModuleByRoleId(@Param("roleId") String roleId);

    @Select({" <script>",
             " select count(*)over() total,users.*,role.roleName from users join role on users.roleId = role.roleId",
             " where users.valid=true",
             " <if test=\"query != null and query!=''\">",
             " and  username like '%${query}%' ESCAPE '/' or account like '%${query}%' ESCAPE '/'",
             " order by",
             " (case ",
             " when username=#{query} or account=#{query} then 1",
             " when username like '${query}%' ESCAPE '/' or account like '${query}%' ESCAPE '/' then 2",
             " when username like '%${query}' ESCAPE '/' or account like '%${query}' ESCAPE '/' then 3",
             " when username like '%${query}%' ESCAPE '/' or account like '%${query}%' ESCAPE '/' then 4",
             " else 0",
             " end)",
             " </if>",
             " <if test=\"query == null or query==''\">",
             " order by account",
             " </if>",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<User> getUserList(@Param("query") String query, @Param("limit") int limit, @Param("offset") int offset);

    @Select("<script> select users.*,role.rolename from users join role on (users.roleId = role.roleId) where  username like '%${username}%' ESCAPE '/' and role.roleid!='1' and users.valid=true <if test='limit!= -1'> limit #{limit} </if> offset #{offset} </script>")
    public List<User> getUserListFilterAdmin(@Param("username") String query, @Param("limit") int limit, @Param("offset") int offset);


    @Select({" <script>",
             " select count(1) from users",
             " where valid=true",
             " <if test=\"query != null and query!=''\">",
             " and username like '%${query}%' ESCAPE '/' or account like '%${query}%' ESCAPE '/'",
             " </if>",
             " </script>"})
    public long getUsersCount(@Param("query") String query);

    @Select("<script>select count(1) from table_relation where categoryguid in" +
            "    <foreach item='item' index='index' collection='categoryGuid'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            " and tableguid=#{tableGuid}</script>")
    public Integer ifPrivilege(@Param("categoryGuid") List<String> categoryGuid, @Param("tableGuid") String tableGuid);

    @Select("select module.moduleid,modulename,type from users,role,privilege,privilege2module,module where users.roleid=role.roleid and role.privilegeid=privilege.privilegeid and privilege.privilegeid=privilege2module.privilegeid and privilege2module.moduleid=module.moduleid and users.valid=true and userid=#{userId}")
    public List<Module> getModuleByUserId(String userId);

}
