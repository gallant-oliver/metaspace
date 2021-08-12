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

import io.zeta.metaspace.model.approve.ApproveItem;
import io.zeta.metaspace.model.approve.ApproveParas;
import io.zeta.metaspace.model.approvegroup.ApproveGroup;
import io.zeta.metaspace.model.approvegroup.ApproveGroupListAndSearchResult;
import io.zeta.metaspace.model.approvegroup.ApproveGroupMemberSearch;
import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.user.User;
import io.zeta.metaspace.model.usergroup.UserGroup;
import io.zeta.metaspace.model.usergroup.result.MemberListAndSearchResult;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/2/24 15:54
 */
public interface ApproveDAO {
    /**
     * 一.用户组列表及搜索
     */

    //实现用户组列表及搜索
    @Select("<script>" +
            "select count(*) over() totalSize,a.id,a.object_id as objectId,a.object_name as objectName,a.business_type as businessType,a.commit_time as commitTime,a.approve_type as approveType, " +
            "a.status as approveStatus,a.approve_group as approveGroup,f.username as approver,a.approve_time as approveTime,g.username as submitter,a.reason as reason,a.module_id as moduleId,a.version as version,a.tenant_id as tenantId"+
            " from approval_item a " +
            " left join users f on a.approver = f.userid " +
            " left join users g on a.submitter = g.userid " +
            " where a.tenant_id=#{tenantId}" +
            "<if test='groups!=null and groups.size()!=0'>" +
            " and a.approve_group in " +
            "<foreach collection='groups' item='groupId' index='index' separator=',' open='(' close=')'>" +
            " #{groupId}" +
            "</foreach>"+
            "</if>" +
            "<if test='paras.approveStatus!=null and paras.approveStatus.size()!=0'>" +
            " and a.status in " +
            " <foreach collection='paras.status' item='stat' index='index' separator=',' open='(' close=')'>" +
            " #{stat}" +
            " </foreach>" +
            "</if>" +
            "<if test='paras.startTime!=null and paras.endTime!=null'>"+
            " and a.approve_time between #{paras.startTime} and #{paras.endTime} "+
            "</if>"+
            "<if test=\"paras.userId!=null and paras.userId!=''\">"+
            " and a.submitter = #{paras.userId} " +
            "</if>" +
            "<if test=\"paras.approveType!=null and paras.approveType!=''\">"+
            " and a.approve_type = #{paras.approveType} " +
            "</if>" +
            "<if test=\"paras.businessType!=null and paras.businessType!=''\">"+
            " and a.business_type = #{paras.businessType} " +
            "</if>" +
            "<if test='paras.query!=null'>" +
            " and a.object_name like concat('%',#{paras.query},'%') ESCAPE '/' " +
            "</if>" +
            "order by a.commit_time DESC,a.object_name " +
            "<if test=\"paras.sortBy!=null and paras.sortBy!=''\">"+
            ", ${paras.sortBy} " +
            "</if>" +

            "<if test='paras.limit!=-1'>" +
            " limit ${paras.limit} " +
            "</if>" +
            "<if test='paras.offset!=0'>" +
            " offset ${paras.offset} " +
            "</if>" +

            "</script>")
    List<ApproveItem> getApproveItems(@Param("tenantId") String tenantId, @Param("paras") ApproveParas paras,
                                                   @Param("groups") List<String> groups);


    /**
     * 审批组与模块关系
     * @param userId
     */
    @Select("select distinct(module_id) from (select group_id from approval_group_relation a join approval_group b on a.group_id = b.id and a.user_id = #{userId} and b.tenantid = #{tenantId} ) a join (select group_id,module_id from approval_group_module_relation) b on  a.group_id = b.group_id")
    List<String> selectApproveModuleByUserId(@Param("userId") String userId,@Param("tenantId") String tenantId);


    /**
     * 用户所在审批组
     * @param userId
     */
    @Select("select a.id from approval_group a inner join approval_group_relation b on a.id = b.group_id where a.tenantid = #{tenantId} and b.user_id = #{userId}")
    List<String> selectApproveGroupByUserId(@Param("userId") String userId,@Param("tenantId") String tenantId);




    /**
     * 添加审批条目
     */

    @Insert({"<script> insert into approval_item (id,object_id,object_name,business_type,approve_type,status,approve_group,approver,approve_time,submitter,commit_time,reason,module_id,version,tenant_id) values ",
            "(#{item.id},#{item.objectId},#{item.objectName},#{item.businessType},#{item.approveType},'1',#{item.approveGroup},#{item.approver},#{item.approveTime},#{item.submitter},now(),#{item.reason},#{item.moduleId},#{item.version},#{item.tenantId})",
             "</script>"})
    void addApproveItem(@Param("item") ApproveItem item);


    //更新业务信息
    @Update("update approval_item set status=#{item.approveStatus},approver=#{item.approver},approve_time=now(),reason=#{item.reason} where id=#{item.id} and tenant_id=#{item.tenantId}")
    int updateStatus(@Param("item") ApproveItem item);

    //更新业务信息
    @Update("delete from approval_item where id=#{item.id} and tenant_id=#{item.tenantId}")
    int deleteItemById(@Param("item") ApproveItem item);

    @Delete("<script>" +
            "delete from approval_item " +
            "where tenant_id=#{item.tenantId} and id in " +
            "<foreach collection='item' item='id' index='index' separator=',' open='(' close=')'>" +
            " #{id}" +
            "</foreach>"+
            "</script>")
    int deleteItem(@Param("item") List<String> item);

    /**
     * 获取审批成员
     */
    @Select({" <script>",
            " select * from users where userid in " ,
            " ( " ,
            " select  user_id from approval_group_relation where group_id=#{approvalGroupId}" ,
            " ) " ,
            " </script>"})
    List<User> getApproveUsers(@Param("approvalGroupId")String approvalGroupId);


    @Select("<script>" +
            "select a.id,a.object_id as objectId,a.object_name as objectName,a.business_type as businessType,a.commit_time as commitTime,a.approve_type as approveType, " +
            "a.status,a.approve_group as approveGroup,a.approve_time as approveTime,a.submitter as submitter,a.reason as reason,a.module_id as moduleId,a.version as version,a.tenant_id as tenantId"+
            " from approval_item a" +
            " where a.tenant_id=#{tenantId} and a.id = #{id}" +
            "</script>")
    ApproveItem getApproveItemById(@Param("id") String id,@Param("tenantId") String tenantId);

    @Update("delete from approval_item where object_id=#{item.objectId} and tenant_id=#{item.tenantId}")
    void deleteItemByObjectId(@Param("item") ApproveItem item);
}
