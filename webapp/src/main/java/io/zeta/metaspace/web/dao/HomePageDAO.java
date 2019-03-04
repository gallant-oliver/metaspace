// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/4 9:57
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.homepage.RoleUseInfo;
import io.zeta.metaspace.model.homepage.TableUseInfo;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:57
 */
public interface HomePageDAO {

    @Select({" <script>",
             " select tableInfo.tableGuid,tableInfo.tableName,count(*) as times from business2Table",
             " join tableInfo on business2table.tableGuid=tableInfo.tableGuid group by tableInfo.tableGuid,tableInfo.tableName",
             " order by times desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<TableUseInfo> getTableRelatedInfo(@Param("limit")int limit, @Param("offset")int offset);

    //@Select("SELECT sum(total) from (select count(*) as total from role join users on role.roleId=users.roleId group by role.roleId) A")
    @Select("select count(*) from business2table")
    public long getTotalTableUserTimes();

    @Select("SELECT count(distinct tableGuid) from  business2Table")
    public long getCountBusinessRelatedTable();

    @Select({" <script>",
             " select role.roleId,role.roleName,count(*) as number from role",
             " join users on role.roleId=users.roleId group by role.roleId,role.roleName",
             " order by number desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<RoleUseInfo> getRoleRelatedInfo(@Param("limit")int limit, @Param("offset")int offset);

    @Select("select count(*) from users")
    public long getTotalUserNumber();

    @Select("select count(*) from role")
    public long getCountRole();

    @Select("select * from role order by roleId")
    public List<Role> getAllRole();

    @Select({" <script>",
             " select * from users",
             " join role on users.roleId=role.roleId",
             " where",
             " users.roleId=#{roleId}",
             " order by userId",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<User> getUserListByRoleId(@Param("roleId")String roleId, @Param("limit")int limit, @Param("offset")int offset);

    @Select({" <script>",
             " select count(*) from users",
             " where",
             " roleId = #{roleId}",
             " order by count desc",
             " </script>"})
    public long getCountUserRelatedRole(@Param("roleId")String roleId);

    @Select("select count(*) from businessInfo where technicalStatus=#{technicalStatus}")
    public long getTechnicalStatusNumber(@Param("technicalStatus")int type);

}
