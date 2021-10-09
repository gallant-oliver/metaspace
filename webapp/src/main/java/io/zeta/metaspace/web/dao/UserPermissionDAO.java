package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.global.UserPermissionPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserPermissionDAO {

    @Select("select * from user_permission where user_id = #{userId}")
    UserPermissionPO selectListByUsersId(@Param("userId") String userId);
}
