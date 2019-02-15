package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.table.Tag;
import io.zeta.metaspace.model.user.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserDAO {
    @Select("select 1 from users where userid=#{userid}")
    public List<User> ifUserExists(String userid);
    @Insert("insert into users(userid,username,account,roleid) values(#{user.userId},#{user.username},#{user.account},#{user.roleId})")
    public int addUser(@Param("user") User user);
}
