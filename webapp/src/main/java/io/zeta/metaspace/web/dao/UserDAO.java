package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.security.UserAndModule;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.user.UserInfo;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

public interface UserDAO {
    @Select("select user2role.roleid from users join user2role on users.userid=user2role.userid where users.userid=#{userId} and users.valid=true")
    public List<String> getRoleIdByUserId(String userId);

    @Select("select user_group_relation.group_id from users join user_group_relation on users.userid=user_group_relation.user_id  join user_group on user_group.id=user_group_relation.group_id " +
            " where users.userid=#{userId} and users.valid=true and user_group.tenant=#{tenantId}")
    public List<String> getUserGroupIdByUser(@Param("userId") String userId,@Param("tenantId")String tenantId);


    @Select("select count(1) from users where userid=#{userid} and valid=true")
    public Integer ifUserExists(String userid);

    @Insert("insert into users(userid,username,account,create_time,update_time,valid) values(#{user.userId},#{user.username},#{user.account},#{user.createTime},#{user.updateTime},#{user.valid})")
    public int addUser(@Param("user") User user);

    @Select("select * from users where userId=#{userId} and valid=true")
    public User getUser(@Param("userId") String userId);

    @Select("select username from users where userId=#{userId}")
    public String getUserName(@Param("userId") String userId);

    @Select("select * from users where userId=#{userId}")
    public User getUserInfo(@Param("userId") String userId);

    @Select("select * from role where roleId in (select user2role.roleId from users join user2role on users.userid=user2role.userid" +
            " where users.userId=#{userId} and users.valid=true) and role.valid=true")
    public List<Role> getRoleByUserId(@Param("userId") String userId);

    @Select("select * from category where guid in (select categoryId from role2category where roleId=#{roleId}) and categoryType=0")
    public List<CategoryEntityV2> getTechnicalCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from category where guid in (select categoryId from role2category where roleId=#{roleId}) and categoryType=1")
    public List<CategoryEntityV2> getBusinessCategoryByRoleId(@Param("roleId") String roleId);

    @Select("select * from module where moduleId in (select moduleId from privilege2module where privilegeId in (select privilegeId from role where roleId=#{roleId} and valid=true))")
    public List<UserInfo.Module> getModuleByRoleId(@Param("roleId") String roleId);

    @Select({" <script>",
             " select count(*)over() total,users.* from users ",
             " where users.valid=true",
             " <if test=\"query != null and query!=''\">",
             " and  username like concat('%',#{query},'%') ESCAPE '/' or account like concat('%',#{query},'%') ESCAPE '/'",
             " order by",
             " (case ",
             " when username=#{query} or account=#{query} then 1",
             " when username like concat('%',#{query},'%') ESCAPE '/' or account like concat('%',#{query},'%') ESCAPE '/' then 2",
             " when username like concat('%',#{query},'%') ESCAPE '/' or account like concat('%',#{query},'%') ESCAPE '/' then 3",
             " when username like concat('%',#{query},'%') ESCAPE '/' or account like concat('%',#{query},'%') ESCAPE '/' then 4",
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

    @Select("select role.roleid,role.roleName from user2role join role on user2role.roleid=role.roleid where user2role.userid=#{userId}")
    public List<UserInfo.Role> getRolesByUser(@Param("userId")String userId);

    @Select("<script> select userid,username,account,create_time createTime,update_time updateTime,valid from users " +
            "where  username like concat('%',#{username},'%') ESCAPE '/' and users.valid=true and userid not in (select user2role.userid from user2role where user2role.roleid='1') " +
            "<if test='limit!= -1'> limit #{limit} </if> offset #{offset} </script>")
    public List<User> getUserListFilterAdmin(@Param("username") String query, @Param("limit") int limit, @Param("offset") int offset);


    @Select({" <script>",
             " select count(1) from users",
             " where valid=true",
             " <if test=\"query != null and query!=''\">",
             " and username like concat('%',#{query},'%') ESCAPE '/' or account like concat('%',#{query},'%') ESCAPE '/'",
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

    @Select("select module.moduleid,modulename,type from users,user2role,role,privilege,privilege2module,module " +
            "where user2role.roleid=role.roleid and role.privilegeid=privilege.privilegeid and privilege.privilegeid=privilege2module.privilegeid " +
            "and privilege2module.moduleid=module.moduleid and users.valid=true and user2role.userid=users.userid and users.userid=#{userId}")
    public List<Module> getModuleByUserId(String userId);

    @Select("select account from users join metadata_subscribe on metadata_subscribe.user_id=users.userid where table_guid=#{tableGuid}")
    public List<String> getUsersEmail(@Param("tableGuid")String tableGuid);

    @Select("select id from user_group where id in (select group_id from user_group_relation " +
            " where user_id=#{userId}) and tenant=#{tenantId}")
    public List<String> getUserGroupIdByUserId(@Param("userId") String userId,@Param("tenantId")String tenantId);
    @Select("select id,name from user_group where id in (select group_id from user_group_relation " +
            " where user_id=#{userId}) and tenant=#{tenantId}")
    public List<UserGroupIdAndName> getUserGroupNameByUserId(@Param("userId") String userId, @Param("tenantId")String tenantId);

    @Select("select * from category where guid in (select category_id from category_group_relation where group_id=#{groupId}) and categoryType=0 and tenantid=#{tenantId}")
    public List<CategoryEntityV2> getTechnicalCategoryByUserGroup(@Param("groupId") String groupId,@Param("tenantId")String tenantId);

    @Select("select * from category where guid in (select category_id from category_group_relation where group_id=#{groupId}) and categoryType=1 and tenantid=#{tenantId}")
    public List<CategoryEntityV2> getBusinessCategoryByUserGroup(@Param("groupId") String roleId,@Param("tenantId")String tenantId);

    @Select("select *,create_time as createTime,update_time as updateTime from users where username=#{userName} and account=#{account} and valid=true")
    public User getUserByName(@Param("userName") String userName,@Param("account")String account);

    @Delete("delete from user_group_relation where user_id=#{userId} and  group_id in (select id from user_group where tenant=#{tenantId})")
    public void deleteGroupByUser(@Param("userId") String userId,@Param("tenantId") String tenantId);

    @Insert({"<script>insert into user_group_relation (group_id,user_id) values ",
             "<foreach item='item' index='index' collection='groupIds'",
             "open='(' separator='),(' close=')'>",
             "#{item},#{userId}",
             "</foreach>",
             "</script>"})
    public void addGroupByUser(@Param("userId") String userId, @Param("groupIds") List<String> groupIds);

    @Select("select userid from users where username=#{name}")
    public String getUserIdByName(@Param("name")String name);

    @Select("select * from users")
    public List<User> getAllUser();
    @Update("update users set " +
            " username=#{userAndModule.userName}," +
            " account=#{userAndModule.email}, " +
            " update_time=#{updateTime}," +
            " valid=true " +
            " where userid=#{userAndModule.accountGuid}")
    public Integer updateUser(@Param("userAndModule") UserAndModule userAndModule, @Param("updateTime") Timestamp updateTime);

    @Insert("insert into users(userid,username,account,create_time,update_time,valid) values(#{userAndModule.accountGuid},#{userAndModule.userName},#{userAndModule.email},#{updateTime},#{updateTime},true)")
    public int insertUser(@Param("userAndModule") UserAndModule userAndModule, @Param("updateTime") Timestamp updateTime);

    @Select("<script>" +
            " select username from users where userid in " +
            "<foreach item='item' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{item} " +
            "</foreach>" +
            "</script>")
    public List<String> getUserNameByIds(@Param("ids") List ids);

    @Select("<script>" +
            "select account from users where userid in " +
            "    <foreach item='item' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<String> getUsersEmailByIds(@Param("ids")List<String> ids);

    @Select("<script>" +
            "select * from users where userid in " +
            "    <foreach item='item' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{item}" +
            "    </foreach>" +
            "</script>")
    public List<User> getUsersByIds(@Param("ids")List<String> ids);
}
