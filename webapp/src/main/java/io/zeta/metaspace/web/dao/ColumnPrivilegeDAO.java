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
 * @date 2019/3/20 10:46
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.ColumnPrivilege;
import io.zeta.metaspace.model.business.ColumnPrivilegeRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/20 10:46
 */
public interface ColumnPrivilegeDAO {

    @Insert("insert into columnPrivilege(name)values(#{name})")
    public int addColumnPrivilege(@Param("name") String privilegeName);

    @Select("select count(1) from columnPrivilege where name=#{name}")
    public int queryNameCount(@Param("name") String privilegeName);

    @Delete("delete from columnPrivilege where guid=#{guid}")
    public int deleteColumnPrivilege(@Param("guid") int guid);

    @Update("update columnPrivilege set name=#{name} where guid=#{guid}")
    public int updateColumnPrivilege(@Param("guid") int guid, @Param("name") String privilegeName);

    @Delete("select * from columnPrivilege where guid=#{guid}")
    public List<String> queryColumnPrivilege(@Param("guid") int guid);

    @Insert("insert into column2privilege(columnGuid,columnPrivilegeGuid)values(#{columnGuid},#{columnPrivilegeGuid})")
    public int addColumnPrivilegeRelation(ColumnPrivilegeRelation relation);

    @Select("select count(1) from column2privilege where columnGuid=#{columnGuid}")
    public int queryCountName(@Param("columnGuid") String columnGuid);

    @Select("select * from column2privilege where columnPrivilegeGuid=#{columnPrivilegeGuid}")
    public ColumnPrivilegeRelation queryPrivilegeRelation(@Param("columnPrivilegeGuid") int columnPrivilegeGuid);

    @Update("update column2privilege set columnGuid=#{columnGuid} where columnPrivilegeGuid=#{columnPrivilegeGuid}")
    public int updateColumnPrivilegeRelation(ColumnPrivilegeRelation relation);

    @Select("select * from column2privilege join columnPrivilege on column2privilege.columnPrivilegeGuid=columnPrivilege.guid where columnGuid=#{columnGuid}")
    public ColumnPrivilegeRelation queryPrivilegeRelationByColumnGuid(@Param("columnGuid") String columnGuid);
}
