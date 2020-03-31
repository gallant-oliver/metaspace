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
 * @date 2019/3/26 16:03
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.share.APIGroup;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 16:03
 */
public interface DataShareGroupDAO {

    @Insert("insert into apiGroup(guid,name,parentGuid,description,generator,generateTime,updater,updateTime,tenantid)values(#{group.guid},#{group.name},#{group.parentGuid},#{group.description},#{group.generator},#{group.generateTime},#{group.updater},#{group.updateTime},#{tenantId})")
    public int insertGroup(@Param("group") APIGroup group,@Param("tenantId") String tenantId);

    @Select("select count(1) from apiGroup where name=#{name} and tenantid=#{tenantId}")
    public int countGroupName(@Param("name")String name,@Param("tenantId")String tenantId);

    @Delete("delete from apiGroup where guid=#{guid}")
    public int deleteGroup(@Param("guid") String guid);

    @Select("select count(1) from apiInfo where groupGuid=#{guid}")
    public int getGroupRelatedAPI(@Param("guid") String guid);

    @Select("select name from apiGroup where guid=#{guid}")
    public String getGroupNameById(@Param("guid") String guid);

    @Update("update apiGroup set name=#{name},description=#{description},updater=#{updater},updateTime=#{updateTime} where guid=#{guid}")
    public int updateGroup(APIGroup group);

    @Select("select * from apiGroup where tenantid=#{tenantId} or tenantid='all' order by generateTime desc")
    public List<APIGroup> getGroupList(@Param("tenantId")String tenantId);
}
