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
 * @date 2019/2/19 11:01
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.privilege.Module;
import io.zeta.metaspace.model.privilege.PrivilegeHeader;
import io.zeta.metaspace.model.privilege.PrivilegeInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/19 11:01
 */
public interface PrivilegeDAO {

    @Insert("insert into privilege(privilegeId, privilegeName, description, createTime, edit, delete)values(#{privilegeId}, #{privilegeName}, #{description}, #{createTime}, 1, 1)")
    public int addPrivilege(PrivilegeHeader privilege);

    @Select("select count(1) from privilege where privilegeName=#{privilegeName}")
    public int getPrivilegeNameCount(@Param("privilegeName")String privilegeName);

    @Insert("<script>" +
            "insert into privilege2module(privilegeId,moduleId)values" +
            "<foreach collection='list' item='moduleId' index='index'  separator=','>" +
            "(#{privilegeId},#{moduleId})" +
            "</foreach>" +
            "</script>")
    public int addModule2Privilege(@Param("privilegeId")String privilegeId,@Param("list")List<Integer> modules);

    @Delete("delete from privilege2module where privilegeId=#{privilegeId}")
    public int deleteModule2PrivilegeById(@Param("privilegeId")String privilegeId);

    @Select("select * from module")
    public List<Module> getAllModule();

    @Delete("delete from privilege where privilegeId=#{privilegeId}")
    public int deletePrivilege(@Param("privilegeId")String privilegeId);

    @Delete("delete from privilege2module where privilegeId=#{privilegeId}")
    public int deletePrivilege2Module(@Param("privilegeId")String privilegeId);

    @Update("update privilege set privilegeName=#{privilegeName},description=#{description} where privilegeId=#{privilegeId}")
    public int updatePrivilege(PrivilegeHeader privilege);


    @Select({" <script>",
             " select count(*)over() total,* from privilege where privilegeName like '%'||#{privilegeName}||'%' ESCAPE '/' order by privilegeId",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<PrivilegeInfo> getPrivilegeList(@Param("privilegeName")String query, @Param("limit")int limit, @Param("offset")int offset);


    @Select("select * from privilege where privilegeId = #{privilegeId} order by privilegeId")
    public PrivilegeInfo getPrivilegeInfo(@Param("privilegeId")String privilegeId);

    @Select("select delete from privilege where privilegeId = #{privilegeId}")
    public int getEnableDelete(@Param("privilegeId")String privilegeId);

    @Select("select * from module where moduleId in (select moduleId from privilege2module where privilegeId=#{privilegeId})")
    public List<Module> getRelatedModuleWithPrivilege(@Param("privilegeId")String privilegeId);

    /*@Select({" <script>",
             " select count(*)over() total,* from role where edit=1 and valid=true order by roleId",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<Role> getAllPermissionRole(@Param("limit")int limit, @Param("offset")int offset);*/

    @Select("select moduleid from module where modulename=#{moduleName}")
    public String getModluleIdByModuleName(@Param("moduleName")String moduleName);

}
