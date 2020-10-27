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

import io.zeta.metaspace.model.apigroup.ApiCategory;
import io.zeta.metaspace.model.apigroup.ApiGroupInfo;
import io.zeta.metaspace.model.apigroup.ApiGroupLog;
import io.zeta.metaspace.model.apigroup.ApiGroupV2;
import io.zeta.metaspace.model.apigroup.ApiVersion;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiInfoV2;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/8/10 18:52
 */
public interface ApiGroupDAO {
    @Insert(" insert into api_group(id,name,description,creator,approve,createtime,publish,updater,updatetime,projectid,tenantid) " +
            " values(#{group.id},#{group.name},#{group.description},#{group.creator},#{group.approve,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},#{group.createTime},#{group.publish},#{group.updater},#{group.updateTime},#{group.projectId},#{tenantId})")
    public Integer insertApiGroup(@Param("group") ApiGroupV2 group, @Param("tenantId")String tenant);

    @Insert("<script>" +
            "insert into api_relation(apiid,groupid,version,update_status) values " +
            "<foreach item='api' index='index' collection='apis' " +
            "open='(' separator='),(' close=')'>" +
            " #{api.guid},#{groupId},#{api.version},false " +
            "</foreach>" +
            "</script>")
    public Integer insertApiRelation(@Param("apis") List<ApiInfoV2> apis, @Param("groupId")String groupId);

    @Select("select count(*) from api_group where id!=#{id} and name=#{name} and tenantid=#{tenantId}")
    public int sameName(@Param("id")String id,@Param("name")String name,@Param("tenantId")String tenantId);

    @Update(" update api_group set " +
            " name=#{group.name},description=#{group.description},approve=#{group.approve,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},updater=#{group.updater},updatetime=#{group.updateTime} " +
            " where id=#{group.id}")
    public Integer updateApiGroup(@Param("group") ApiGroupV2 group);

    @Select("<script>" +
            " select count(*)over() count,s.update_time updateTime,g.*,g.approve approveJson ,case when (s.groupid is null) then false else true end updatestatus from api_group g left join (" +
            "  select  groupid,max(update_time) update_time from api_relation r join api on r.apiid=api.guid and r.version=api.version where r.update_status=true and api.status='up' group by groupid" +
            ") s on g.id=s.groupid " +
            " where " +
            " g.projectid=#{projectId} and g.tenantid=#{tenantId} " +
            "<if test=\"param.query != null and param.query!=''\">" +
            " and g.name like '%${param.query}%' " +
            "</if>" +
            "<if test=\"publish!=null\">" +
            " and g.publish=#{publish} " +
            "</if>" +
            "<if test=\"param.sortby!=null and param.sortby!='' \">" +
            "order by g.${param.sortby} " +
            "<if test=\"param.order!='' and param.order!=null\">" +
            "${param.order} " +
            "</if>" +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            "limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            "offset ${param.offset}" +
            "</if>" +
            "</script>")
    public List<ApiGroupV2> searchApiGroup(@Param("param")Parameters parameters,@Param("projectId")String projectId,@Param("tenantId")String tenantId,@Param("publish")Boolean publish);

    @Update("update api_relation set update_status=false where update_status=true and update_time<#{time}")
    public int updateApiRelationStatusByTime(@Param("time")Timestamp timestamp);

    @Update("update api_relation set update_status=false where update_status=true and update_time<#{time}")
    public int updateApiRelationStatus(@Param("time")Timestamp timestamp);

    @Select("select *,approve approveJson from api_group where id=#{id}")
    public ApiGroupInfo getApiGroupInfo(@Param("id")String id);

    @Select(" select api.guid apiId,api.name apiName,api_category.guid categoryId,api_category.name categoryName,api.version apiVersion,api.status " +
            " from api_relation join api on api_relation.apiid=api.guid and api_relation.version=api.version " +
            " join api_category on api_category.guid=api.categoryguid " +
            " where api_relation.groupid=#{groupId} and api.valid=true ")
    public List<ApiCategory.Api> getAPiByGroup(@Param("groupId")String groupId);

    @Select(" select apiid from api_relation  " +
            " where groupid=#{groupId} ")
    public List<String> getAPiIdByGroup(@Param("groupId")String groupId);

    @Update("<script>" +
            "update api_relation set version=tmp.version,update_status=false from (values " +
            "<foreach item='api' index='index' collection='apis' " +
            "open='(' separator='),(' close=')'>" +
            " #{api.guid},#{api.version} " +
            "</foreach>" +
            ") as tmp (id,version) " +
            " where apiid=tmp.id and groupid=#{groupId} and update_status=true " +
            "</script>")
    public int updateApiRelationVersion(@Param("apis") List<ApiInfoV2> apis, @Param("groupId")String groupId);

    @Update("<script>" +
            "update api_relation set update_status=true,update_time=#{time} " +
            " where apiid=#{id} " +
            "</script>")
    public int updateApiRelationByApi(@Param("id") String id,@Param("time")Timestamp time);

    @Update("<script>" +
            "update api_relation set update_status=false where apiid in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            " and groupid=#{groupId}" +
            "</script>")
    public int unUpdateApiRelationVersion(@Param("ids")List<String> ids, @Param("groupId")String groupId);

    @Update("update api_group set publish=#{publish} where id=#{groupId}")
    public int updatePublish(@Param("groupId")String groupId,@Param("publish") boolean publish);

    @Delete("<script>" +
            "delete from api_relation where groupid in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public void deleteRelationByGroupIds(@Param("ids")List<String> ids);

    @Delete("<script>" +
            "delete from api_relation where apiid in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public void deleteRelationByApi(@Param("ids")List<String> ids);

    @Delete("<script>" +
            "delete from api_relation where apiid =#{api.apiId} and version=#{api.version} " +
            "</script>")
    public void deleteRelationByApiVersion(@Param("api")ApiVersion api);

    @Delete("<script>" +
            "delete from api_relation where groupid=#{groupId} and apiid not in" +
            "<foreach item='id' index='index' collection='apiIds' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public void updateRelation(@Param("apiIds")List<String> apiIds,@Param("groupId")String groupId);


    @Delete("<script>" +
            "delete from api_group where id in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public void deleteApiGroup(@Param("ids")List<String> ids);

    @Select("<script>" +
            "select id from api_group where projectid in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public List<String> getApiGroupIdByProject(@Param("ids")List<String> ids);

    @Update("delete from api_relation where apiid in (select guid from api where categoryguid=#{categoryId})")
    public int deleteRelationByCategory(@Param("categoryId")String categoryId);

    @Update("update api set valid=false where categoryguid=#{categoryId}")
    public int delete(@Param("categoryId")String categoryId);

    @Select("<script>" +
            "select name from api_group where id in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public List<String> getApiNames(@Param("ids")List<String> ids);


    @Select("<script>" +
            "select count(*)over() count,api.name apiName,api.guid apiId,api.version,api.description from api_relation r join api on r.apiid=api.guid and r.version=api.version" +
            " where r.groupid=#{groupId} and r.update_status=true and api.status='up' " +
            "<if test='limit!=-1'>" +
            " limit ${limit} " +
            "</if>" +
            "<if test='offset!=0'>" +
            " offset ${offset} " +
            "</if>" +
            "</script>")
    public List<ApiVersion> getUpdateApi(@Param("groupId")String groupId,@Param("limit") int limit,@Param("offset") int offset);

    @Insert(" <script>" +
            "insert into api_group_log(group_id,type,userid,time)values" +
            " <foreach item='log' index='index' collection='logs' separator='),(' open='(' close=')'>" +
            " #{log.groupId},#{log.type},#{log.creator},#{log.date} " +
            " </foreach>" +
            " </script>")
    public int addApiLogs(@Param("logs")List<ApiGroupLog> apiLogs);

    @Select("<script>" +
            " select count(1)over() total,api_group_log.time date,api_group_log.group_id,api_group_log.type,users.username creator from api_group_log join users on api_group_log.userid=users.userid " +
            " where api_group_log.group_id=#{groupId} " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and (users.username like '%${param.query}%' ESCAPE '/' " +
            "<if test=\"type!=null and type!=''\">" +
            " or  api_group_log.type like '%${type}%' ESCAPE '/'" +
            "</if>" +
            ")" +
            "</if>" +
            " order by api_group_log.time desc" +
            " <if test='param.limit != null and param.limit!=-1'>" +
            " limit #{param.limit}" +
            " </if>" +
            " <if test='param.offset != null'>" +
            " offset #{param.offset}" +
            " </if>" +
            " </script>")
    public List<ApiGroupLog> getApiLog(@Param("param") Parameters parameters,@Param("groupId") String groupId,@Param("type") String type);

    @Select("<script>" +
            " select api.guid apiId,api.name apiName,api_category.guid categoryId,api_category.name categoryName,api.version apiVersion,api.status from" +
            " api join api_category on api_category.guid=api.categoryguid join " +
            " (select guid,max(version_num) version_num from api where " +
            " valid=true and status='up' and projectid=#{projectId} " +
            " group by guid) v on api.guid=v.guid and api.version_num=v.version_num " +
            " where api.valid=true and api.projectid=#{projectId}" +
            " <if test=\"search != null and search!=''\">" +
            " and api.name like #{search}" +
            " </if>" +
            " </script>")
    public List<ApiCategory.Api> getAllApi(@Param("search")String search,@Param("projectId")String projectId);

    @Select(" select u.username creator,* from api_relation a join api_group g on a.groupid=g.id left join users u on u.userid=g.creator " +
            " where a.version=#{version} and a.apiid=#{apiId}")
    public List<ApiGroupInfo> getApiGroupByApiVersion(@Param("apiId")String apiId,@Param("version")String version);

    @Update("update api_group set mobius_id=#{mobiusId} where id=#{guid}")
    public int updateApiMobiusId(@Param("guid")String guid,@Param("mobiusId")String mobiusId);

    @Select("select mobius_id from api_group where id=#{id} ")
    public String getApiGroupMobiusId(@Param("id")String id);

    @Select("<script>" +
            "select mobius_id from api where valid=true and id in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getApiMobiusIdsByIds(@Param("ids")List<String> ids);

    @Select("select api.mobius_id from api_relation join api on api.guid=api_relation.apiid and api.version=api_relation.version where groupid=#{id} ")
    public List<String> getGroupRelationMobiusId(@Param("id")String id);

    @Select("<script>" +
            "select mobius_id from api_group where projectid in " +
            " <foreach item='id' index='index' collection='projectIds' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getMobiusByProjects(@Param("projectIds")List<String> projectIds);
}
