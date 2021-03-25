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

package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.approvegroup.ApproveGroup;
import io.zeta.metaspace.model.approvegroup.ApproveGroupListAndSearchResult;
import io.zeta.metaspace.model.approvegroup.ApproveGroupMemberSearch;
import io.zeta.metaspace.model.approvegroup.ApproveGroupParas;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.datasource.DataSourceIdAndName;
import io.zeta.metaspace.model.datasource.SourceAndPrivilege;
import io.zeta.metaspace.model.metadata.CategoryExport;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.result.*;
import io.zeta.metaspace.model.share.ProjectHeader;
import io.zeta.metaspace.model.table.DataSourceHeader;
import io.zeta.metaspace.model.table.DatabaseHeader;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupPrivileges;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupListAndSearchResult;
import io.zeta.metaspace.model.usergroup.result.UserGroupMemberSearch;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/24 15:54
 */
public interface ApproveGroupDAO {
    /**
     * 一.用户组列表及搜索
     */

    //实现用户组列表及搜索
    @Select("<script>" +
            "select count(*) over() totalSize,u.id,u.name,u.description,case when m.member is NULL then '0' else m.member end member,u.creator,u.create_time as createTime,u.update_time as updateTime " +
            " from approval_group u left join " +
            " (select g.id id,count(*) member " +
            " from approval_group  g " +
            " join approval_group_relation r " +
            " on g.id=r.group_id  where r.user_id in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " GROUP BY g.id) m " +
            " on u.id=m.id " +
            " where u.tenantid=#{tenantId} and valid=true" +
            "<if test='search!=null'>" +
            " and u.name like '%${search}%' ESCAPE '/' " +
            "</if>" +
            "<if test='sortBy!=null'>" +
            "order by ${sortBy} " +
            "</if>" +
            "<if test='order!=null '>" +
            " ${order} " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<ApproveGroupListAndSearchResult> getApproveGroup(@Param("tenantId") String tenantId, @Param("offset") int offset, @Param("limit") int limit,
                                                                 @Param("sortBy") String sortBy, @Param("order") String order, @Param("search") String search,
                                                                 @Param("ids") List<String> ids);

    //基于模块获取对应审批组
    @Select("<script>" +
            "select count(*) over() totalSize,u.id,u.name,u.description" +
            " from approval_group u inner join " +
            " approval_group_module_relation m"+
            " on u.id=m.group_id " +
            " where u.tenantid=#{tenantId} and valid=true and m.module_id = #{paras.moduleId}" +
            "<if test=\"params.query!='' and params.query!=null\">"+
            " and u.name like '%${params.query}%' ESCAPE '/' " +
            "</if>" +
            "<if test='params.sortBy!=null'>" +
            " order by ${params.sortBy} " +
            "</if>" +
            "<if test='params.order!=null '>" +
            " ${params.order} " +
            "</if>" +
            "<if test='params.limit!=-1'>" +
            " limit ${params.limit} " +
            "</if>" +
            "<if test='params.offset!=0'>" +
            " offset ${params.offset} " +
            "</if>" +
            "</script>")
    public List<ApproveGroupListAndSearchResult> getApproveGroupByModuleId(@Param("tenantId") String tenantId, @Param("params") ApproveGroupParas approveGroupParas);



    @Select("select username from users where userid=#{userId}")
    public String getUserNameById(@Param("userId") String userId);


    /**
     * 审批组详情
     */
    @Select("select name,description from approval_group where id=#{id}")
    public ApproveGroup getApproveGroupByID(@Param("id") String id);


    @Select("<script>" +
            " select name,description from approval_group where id in " +
            "    <foreach item='id' index='index' collection='ids'" +
            "    open='(' separator=',' close=')'>" +
            "    #{id}" +
            "    </foreach>" +
            "</script>")
    public List<ApproveGroup> getApproveGroupByIDs(@Param("ids") List<String> ids);




    /**
     * 三.新建用户组
     */

    @Insert("insert into approval_group (id,name,creator,description,create_time,update_time,valid,tenantid) values (#{group.id},#{group.name},#{group.creator},#{group.description},#{group.createTime},#{group.updateTime},true,#{tenantId})")
    public Integer addGroup(@Param("tenantId") String tenantId, @Param("group") ApproveGroup group);

    @Select("select count(*) from approval_group where name=#{name} and id!=#{id} and tenantid=#{tenantId} and valid=true")
    public Integer isNameById(@Param("tenantId") String tenantId, @Param("name") String name, @Param("id") String id);


    /**
     * 四.删除用户组信息
     */
    @Delete("<script>" +
            " delete from approval_group  " +
            " where id in " +
            "<foreach collection='ids' item='groupId' index='index' separator=',' open='(' close=')'>" +
            " #{groupId}" +
            " </foreach>"+
            "</script>")
    public void deleteApproveGroupByIDs(@Param("ids") List<String> ids);


    /**
     * 删除审批组与模块关系
     * @param ids
     */
    @Delete("<script>" +
            "delete from approval_group_module_relation " +
            " where group_id in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>"+
            "</script>")
    public void deleteApproveGroupModule(@Param("ids") List<String> ids);

    /**
     * 审批组与模块关系
     * @param id
     */
    @Select("select module_id from approval_group_module_relation " +
            " where group_id = #{id} "
           )
    public List<String> selectApproveGroupModule(@Param("id") String id);



    /**
     * 审批组成员列表及搜索
     */
    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username,u.account from users u join approval_group_relation g on u.userid=g.user_id " +
            "where g.group_id=#{id} and u.userid in " +
            "<foreach collection='ids' item='userId' index='index' separator=',' open='(' close=')'>" +
            "#{userId}" +
            "</foreach>" +
            "<if test='search!=null'>" +
            " and u.username like '%${search}%' ESCAPE '/' " +
            "</if>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<MemberListAndSearchResult> getMemberListAndSearch(@Param("id") String id, @Param("offset") int offset,
                                                                  @Param("limit") int limit, @Param("search") String search, @Param("ids") List<String> ids);


    /**
     * 六.审批组已有成员列表
     */


    @Select("<script>" +
            " select u.username  " +
            " from approval_group_relation g  " +
            " join users u " +
            " on g.user_id=u.userid " +
            " join approval_group  r " +
            " on g.group_id=r.id " +
            " where g.group_id=#{groupId} " +
            " and r.tenantid=#{tenantId} " +
            "</script>")
    public List<String> getUserNameByGroupId(@Param("tenantId") String tenantId, @Param("groupId") String groupId);


    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username,u.account email from users u " +
            " where u.username in " +
            "<foreach collection='userNameList' item='userName' index='index' separator=',' open='(' close=')'>" +
            "#{userName}" +
            "</foreach>" +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<ApproveGroupMemberSearch> getApproveGroupMemberSearch(@Param("userNameList") List<String> userNameList, @Param("offset") int offset, @Param("limit") int limit);


    /**
     * 审批组添加成员
     */

    @Insert({"<script> insert into approval_group_relation (group_id,user_id) values ",
             "<foreach item='item' index='index' collection='userIds'",
             "open='(' separator='),(' close=')'>",
             "#{groupId},#{item}",
             "</foreach>",
             "</script>"})
    public void addUserGroupByID(@Param("groupId") String groupId, @Param("userIds") List<String> userIds);


    @Insert({"<script>insert into approval_group_module_relation (group_id,module_id) values ",
            "<foreach item='item' index='index' collection='moduleIds'",
            "open='(' separator='),(' close=')'>",
            "#{groupId},#{item}",
            "</foreach>",
            "</script>"})
    public void addModuleToGroupByIDs(@Param("groupId") String groupId, @Param("moduleIds") List<String> moduleIds);

    /**
     * 审批组移除成员
     */
    @Delete({"<script>",
             "delete from approval_group_relation where group_id=#{groupId} and user_id in ",
             "<foreach collection='userIds' item='userId' index='index' separator=',' open='(' close=')'>",
             "#{userId}",
             "</foreach>",
             "</script>"})
    public void deleteUserByGroupId(@Param("groupId") String groupId, @Param("userIds") List<String> userIds);

    /**
     * 十五.修改用户组管理信息
     */
    @Update("update approval_group set " +
            "name=#{group.name} ," +
            "description=#{group.description} ," +
            "updatetime=#{updateTime} " +
            "where id=#{groupId}")
    public void updateApproveGroupInformation(@Param("groupId") String groupId, @Param("group") ApproveGroup group, @Param("updateTime") Timestamp updateTime);


    //判断用户组Id是否已经存在，true为存在，false为不存在
    @Select("select count(*) from approval_group where id=#{groupId}")
    public Integer existGroupId(@Param("groupId") String groupId);

    @Select("select g.*,g.tenant tenantId from approval_group g join user_group_relation u on g.id=u.group_id where u.user_id=#{userId} and g.valid=true and tenant=#{tenantId}")
    public List<UserGroup> getapproveGroupByUsersId(@Param("userId") String userId, @Param("tenantId") String tenantId);


    //获取审批组的用户id
    @Select("select user_id from approval_group_relation where group_id=#{groupId}")
    public List<String> getUserIdByApproveGroup(@Param("groupId") String groupId);




}
