package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import org.apache.atlas.model.metadata.CategoryInfoV2;
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

    @Select("select * from role where roleId in (select roleId from user where userId=#{roleId})")
    public Role getRoleByUserId(@Param("roleId") String roleId);

    @Select("select * from category where guid in (select cagetoryId from role2category where roleId=#{roleId}) and type=0")
    public List<UserInfo.TechnicalCategory> getTechnicalCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from category where guid in (select cagetoryId from role2category where roleId=#{roleId}) and type=1")
    public List<UserInfo.BusinessCategory> getBusinessCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from module where moduleId in (select moduleId from privilege2module where privilegeId in (select privilegeId from privilege where roleId=#{roleId}))")
    public List<UserInfo.Module> getModuleByRoleId(@Param("roleId") String roleId);

    @Select("select * from users where  username like '%${username}%' limit #{limit} offset #{offset}")
    public List<User> getUserList(@Param("username") String query, @Param("limit") int limit, @Param("limit") int offset);

    @Select("select count(1) from users where username like '%'||#{query}||'%'")
    public long getUsersCount(@Param("query") String query);
}
