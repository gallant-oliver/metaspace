package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.global.UserPermissionPO;
import io.zeta.metaspace.model.privilege.UserPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionDAO {
    @Select("<script>" +
            " select count(*)over() as total,user_id as userId, username ,account , permissions , create_time as createTime from user_permission where 1=1 "+
            "<if test=\"name != null and name != ''\">"+
            " and username like CONCAT('%',#{name},'%') "+
            "</if>" +
            "<if test=\"orderBy!=null and orderBy !=''\">" +
            "order by ${orderBy} " +
                "<if test='sortType!=null '>" +
                " ${sortType} " +
                "</if>" +
            "</if>" +
            " LIMIT #{limit} OFFSET #{offset} "+
            "</script>")
    List<UserPermission> getUserPermissionPageList(@Param("name") String name, @Param("offset") int offset, @Param("limit") int limit,
                                                   @Param("sortType") String sortType,@Param("orderBy") String orderBy);

    @Insert("<script>" +
            " insert into user_permission (user_id, username ,account , permissions , create_time) "+
            "VALUES\n" +
            "  <foreach item='item' index='index' collection='list' separator=','> " +
            "(  #{item.userId},#{item.username} , #{item.account},#{item.permissions}, NOW( ) )" +
            "</foreach>" +
            "</script>")
    int batchSave(@Param("list")List<UserPermission> list);

    @Delete("delete from user_permission where user_id=#{userId}")
    int deleteByUserId(@Param("userId") String userId);

    @Select({"<script>",
            "select user_id as userId from  user_permission where user_id in ",
            " <foreach item='userId' index='index' collection='list' separator=',' open='(' close=')'>",
            " #{userId}",
            " </foreach>",
            "</script>"})
    List<String> getByUserIdList( @Param("list") List<String> userIdList);

    @Select("select * from user_permission where user_id = #{userId}")
    UserPermissionPO selectListByUsersId(@Param("userId") String userId);
}
