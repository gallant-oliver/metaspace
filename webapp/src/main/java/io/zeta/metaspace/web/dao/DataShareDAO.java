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
 * @date 2019/3/26 19:42
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.apigroup.ApiVersion;
import io.zeta.metaspace.model.metadata.CategoryEntity;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.result.CategoryPrivilege;
import io.zeta.metaspace.model.share.*;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler;
import org.apache.atlas.model.metadata.CategoryEntityV2;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface DataShareDAO {

    @Insert({" <script>",
             " insert into apiInfo(guid,name,sourceType,sourceId,schemaName,tableName,tableGuid,dbGuid,groupGuid,keeper,maxRowNumber,fields,version,description,",
             " protocol,requestMode,returnType,path,generateTime,updater,updateTime,publish,star,used_count,manager,tenantid,pool",
             " <if test='info.desensitize != null'>",
             " ,desensitize",
             " </if>",
             " )values(",
             " #{info.guid},#{info.name},#{info.sourceType},#{info.sourceId},#{info.schemaName},#{info.tableName},#{info.tableGuid},#{info.dbGuid},#{info.groupGuid},#{info.keeper},#{info.maxRowNumber},#{info.fields,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},",
             " #{info.version},#{info.description},#{info.protocol},#{info.requestMode},#{info.returnType},#{info.path},#{info.generateTime},#{info.updater},#{info.updateTime},#{info.publish},#{info.star},#{info.usedCount},#{info.manager},#{tenantId},#{info.pool}",
             " <if test='info.desensitize != null'>",
             " ,#{info.desensitize}",
             " </if>",
             " )",
             " </script>"})
    public int insertAPIInfo(@Param("info") APIInfo info,@Param("tenantId")String tenantId);

    @Select("select count(1) from apiInfo where path=#{path}")
    public int samePathCount(@Param("path")String path);

    @Update({" <script>",
             " update apiInfo set name=#{name},tableGuid=#{tableGuid},dbGuid=#{dbGuid},groupGuid=#{groupGuid},maxRowNumber=#{maxRowNumber},",
             " fields=#{fields,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},",
             " version=#{version},description=#{description},protocol=#{protocol},requestMode=#{requestMode},returnType=#{returnType},path=#{path},",
             " updater=#{updater},updateTime=#{updateTime},pool=#{pool}",
             " where",
             " guid=#{guid}",
             " </script>"})
    public int updateAPIInfo(APIInfo info);

    @Select({" <script>",
            " select apiInfo.*,apiGroup.name as groupName from apiInfo",
            " join apiGroup on apiInfo.groupGuid = apiGroup.guid",
            " where apiInfo.guid=#{guid}",
            " </script>"})
    public APIInfo getAPIInfoByGuid(@Param("guid")String guid);

    @Select({" <script>",
            " select tableName,dbName as databaseName,display_name as displayName from tableInfo ",
            " where tableGuid=#{guid}",
            " </script>"})
    public Table getTableByGuid(@Param("guid")String guid);

    @Select({" <script>",
             " select count(1)over() total,apiInfo.guid,apiInfo.name,apiInfo.tableGuid,apiInfo.groupGuid,apiInfo.publish,apiInfo.keeper,apiInfo.version,apiInfo.updater,apiInfo.updateTime,",
             " apiGroup.name as groupName,apiInfo.used_count as usedCount,manager",
             " from apiInfo,apiGroup where apiInfo.tenantid=#{tenantId} and ",
             " apiInfo.groupGuid=apiGroup.guid and apiInfo.name like concat('%',#{query},'%') ESCAPE '/'",
             " <if test=\"groupGuid!='1'.toString()\">",
             " and apiInfo.groupGuid=#{groupGuid}",
             " </if>",
             " <if test='my==0'>",
             " and keeper=#{keeper}",
             " </if>",
             " <choose>",
             " <when test=\"publish=='unpublish'\"> and publish=false </when>",
             " <when test=\"publish=='publish'\"> and publish=true </when>",
             " </choose>",
             " order by updateTime desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<APIInfoHeader> getAPIList(@Param("groupGuid")String guid, @Param("my")Integer my, @Param("publish")String publish, @Param("keeper")String keeper, @Param("query")String query, @Param("limit")int limit, @Param("offset")int offset,@Param("tenantId")String tenantId);



    @Select("select fields from apiInfo where guid=#{guid}")
    public Object getQueryFiledsByGuid(@Param("guid")String guid);

    @Select("select dataOwner from tableInfo where tableGuid=#{guid}")
    public Object getDataOwnerByGuid(@Param("guid")String guid);

    @Select("select count(1) from apiInfo where name=#{name} and tenantid=#{tenantId}")
    public int querySameName(@Param("name")String name,@Param("tenantId")String tenantId);

    @Delete("delete from apiInfo where guid=#{guid}")
    public int deleteAPIInfo(@Param("guid")String guid);

    @Select("select count(*) from apiInfo where guid=#{guid} and (keeper=#{userId} or manager=#{userId})")
    public int countManager(@Param("guid")String guid, @Param("userId")String userId);

    @Update("update apiInfo set star=#{star} where guid=#{guid}")
    public int updateStarStatus(@Param("guid")String guid, @Param("star")Boolean starStatus);

    @Insert("insert into user2apistar(apiGuid,userId)values(#{apiGuid},#{userId})")
    public int insertAPIStar(@Param("userId")String userId, @Param("apiGuid")String apiGuid);

    @Insert("delete from user2apistar where apiGuid=#{apiGuid} and userId=#{userId}")
    public int deleteAPIStar(@Param("userId")String userId, @Param("apiGuid")String apiGuid);

    @Select("select user2apistar.apiGuid from user2apistar join apiinfo on apiinfo.guid=user2apistar.apiGuid where user2apistar.userId=#{userId} and apiinfo.tenantid=#{tenantId}")
    public List<String> getUserStarAPI(@Param("userId")String userId,@Param("tenantId")String tenantId);

    @Select("select count(1) from user2apistar where apiGuid=#{apiGuid} and userId=#{userId}")
    public int getStarCount(@Param("userId")String userId, @Param("apiGuid")String apiGuid);

    @Update({" <script>",
             " update apiInfo set publish=#{publish} where guid in",
             " <foreach item='guid' index='index' collection='guidList' separator=',' open='(' close=')'>" ,
             " #{guid}",
             " </foreach>",
             " </script>"})
    public int updatePublishStatus(@Param("guidList")List<String> guidList, @Param("publish")Boolean publishStatus);

    @Select("select tableName from tableInfo where tableGuid=#{tableGuid}")
    public String queryTableNameByGuid(@Param("tableGuid")String guid);

    @Select("select dbName from tableInfo where tableGuid=#{tableGuid}")
    public String querydbNameByGuid(@Param("tableGuid")String guid);

    @Select({" <script>",
             " select guid,name,maxRowNumber,publish,sourceType,sourceId,schemaName,tableGuid,tableName,pool,manager ",
             " from apiInfo",
             " where path=#{path}",
             " </script>"})
    public APIInfo getAPIInfo(@Param("path")String path);

    @Update("update apiInfo set used_count=used_count+1 where path=#{path}")
    public int updateUsedCount(@Param("path")String path);

    @Select("select fields from apiInfo where path=#{path}")
    public Object getAPIFields(@Param("path")String path);

//    @Select({" <script>",
//             " select count(1)over() total,apiInfo.guid,apiInfo.name,apiInfo.tableGuid,apiInfo.groupGuid,apiInfo.publish,users.username as keeper,",
//             " tableInfo.tableName,apiGroup.name as groupName, tableInfo.display_name as tableDisplayName",
//             " from apiInfo,tableInfo,apiGroup,users where",
//             " apiInfo.tableGuid in",
//             " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>" ,
//             " #{tableGuid}",
//             " </foreach>",
//             " and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid and apiInfo.tenantid=#{tenantId} ",
//             " and users.userId=apiInfo.keeper order by apiInfo.updateTime desc",
//             " <if test='limit != null and limit!=-1'>",
//             " limit #{limit}",
//             " </if>",
//             " <if test='offset != null'>",
//             " offset #{offset}",
//             " </if>",
//             " </script>"})
//    public List<APIInfoHeader> getTableRelatedAPI(@Param("tableList")List<String> tableList, @Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId);

    @Select({" <script>",
            " SELECT COUNT ( 1 ) OVER ( ) total,api.guid,api.name,api.tableguid,users.username AS keeper,tableInfo.tableName,tableInfo.display_name AS tableDisplayName",
            " FROM api INNER JOIN tableInfo ON api.tableguid=tableinfo.tableguid INNER JOIN users ON users.userId = api.creator",
            " WHERE api.tenantid=#{tenantId} AND api.tableguid IN",
            " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>",
            " #{tableGuid}",
            " </foreach>",
            " ORDER BY api.updateTime DESC",
            " <if test='limit != null and limit!=-1'>",
            " limit #{limit}",
            " </if>",
            " <if test='offset != null'>",
            " offset #{offset}",
            " </if>",
            " </script>"})
    public List<APIInfoHeader> getTableRelatedAPI(@Param("tableList")List<String> tableList, @Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId);


    @Select({" <script>",
             " select count(1)over() total,api.guid id,api.name,api.tableGuid,api.status,users.username as creator,",
             " tableInfo.tableName, tableInfo.display_name as tableDisplayName,api.version ",
             " from api join tableInfo on api.tableGuid=tableInfo.tableGuid join users on users.userId=api.creator ",
             " <if test='isNew'>",
             " join ( select guid,max(version_num) max from api where valid=true and status!='draft' and status!='audit' ",
             " group by guid) v on v.guid=api.guid and v.max=api.version_num ",
             " </if>",
             " where ",
             " api.tableGuid in ",
             " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>" ,
             " #{tableGuid}",
             " </foreach>",
             " and api.tenantid=#{tenantId} and api.status!='draft' and api.status!='audit' AND api.valid = true ",
             " <if test='!up'>",
             " and api.status!='up'",
             " </if>",
             " <if test='!down'>",
             " and api.status!='down'",
             " </if>",
             " order by api.createtime desc",
             " <if test='limit != null and limit!=-1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset != null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<ApiHead> getTableRelatedDataServiceAPI(@Param("tableList")List<String> tableList, @Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId,
                                                       @Param("up")boolean up,@Param("down")boolean down,@Param("isNew")boolean isNew);

    @Select("select count(1) from apiInfo where manager=#{manager} and guid=#{guid}")
    public int countUserAPI(@Param("manager")String keeper, @Param("guid")String apiGuid);

    @Select("select apiGroup.name from apiGroup,apiInfo where apiInfo.groupGuid=apiGroup.guid and apiInfo.guid=#{guid}")
    public String getGroupByAPIGuid(@Param("guid")String apiGuid);

    @Select("select dataOwner from tableInfo,apiInfo where apiInfo.guid=#{guid} and tableInfo.tableGuid=apiInfo.tableGuid")
    public Object getDataOwnerByApiGuid(@Param("guid")String apiGuid);

    @Select({"<script>",
             "select apiInfo.guid,users.account as manager from apiInfo join users on users.userid=apiInfo.manager where tableGuid in",
             " <foreach item='tableGuid' index='index' collection='tableGuidList' separator=',' open='(' close=')'>",
             " #{tableGuid}",
             " </foreach>",
             " and publish=true and tenantid=#{tenantId}",
             " </script>"})
    public List<APIInfoHeader> getAPIByRelatedTable(@Param("tableGuidList")List<String> tableList,@Param("tenantId")String tenantId);

    @Select("select status from tableInfo where tableGuid = (select tableGuid from apiInfo where guid=#{apiGuid})")
    public String getTableStatusByAPIGuid(@Param("apiGuid")String apiGuid);

    @Select("select status from tableInfo where tableGuid = #{tableGuid}")
    public String getTableStatusByGuid(@Param("tableGuid")String tableGuid);

    @Select({"<script>",
             "select guid from apiInfo where tenantid=#{tenantId} and tableGuid in",
             " <foreach item='tableGuid' index='index' collection='tableGuidList' separator=',' open='(' close=')'>",
             " #{tableGuid}",
             " </foreach>",
             " </script>"})
    public List<String> getAPIIdsByRelatedTable(@Param("tableGuidList")List<String> tableList,@Param("tenantId")String tenantId);

    @Update("update apiInfo set manager=#{userId} where guid=#{apiGuid}")
    public int updateManager(@Param("apiGuid")String apiGuid, @Param("userId")String userId);

    @Select("select id,type,pkid from organization where pkid in (select pkid from table2owner where tableguid=(select tableguid from apiinfo where guid=#{apiGuid}))")
    public List<TableOwner.Owner> getOwnerList(@Param("apiGuid")String apiGuid);

    @Select("select count(*) from project where id!=#{id} and name=#{name} and tenantid=#{tenantId} and valid=true")
    public  int sameProjectName(@Param("id")String id,@Param("name")String name,@Param("tenantId")String tenantId);

    @Insert(" insert into project(id,name,creator,description,createtime,manager,tenantid,valid) " +
            " values(#{project.id},#{project.name},#{project.creator},#{project.description},#{project.createTime},#{project.manager},#{tenantId},true) ")
    public int insertProject(@Param("project")ProjectInfo projectInfo,@Param("tenantId")String tenantId);

    @Insert("<script>" +
            " insert into project_group_relation(project_id,group_id) " +
            " values" +
            " <foreach item='userGroup' index='index' collection='userGroups' separator='),(' open='(' close=')'>" +
            " #{id},#{userGroup}" +
            " </foreach>" +
            "</script>")
    public int addProjectToUserGroup(@Param("id")String id,@Param("userGroups") List<String> userGroups);

    @Select("<script>" +
            " select count(*)over() total,p.*,uc.usercount,ac.count apiCount from (" +
            "select project.id,project.name,project.description,project.createtime,project.tenantid,project.valid,user1.username creator,user2.username manager,manager managerId,project.manager managerId from " +
            "project join users user1 on project.creator=user1.userid join users user2 on project.manager=user2.userid  " +
            ") p  join " +
            " (select distinct project_group_relation.project_id from project_group_relation join " +
            " (select user_group.id id from user_group_relation join user_group on user_group_relation.group_id=user_group.id " +
            " where user_group_relation.user_id=#{userId} and user_group.tenant=#{tenantId}) ug " +
            " on project_group_relation.group_id=ug.id " +
            " union " +
            " select id from project where manager=#{userId}) pi " +
            " on p.id=pi.project_id  join " +
            " (select count(distinct uc.user_id) usercount,project_id id from (" +
            "  select g.user_id ,p.project_id from project_group_relation p join user_group_relation g on p.group_id=g.group_id\n" +
            "  union " +
            "  select manager user_id,id project_id from project " +
            " ) uc where uc.user_id in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "group by project_id ) uc on p.id=uc.id left join " +
            "(select count(distinct guid) count,projectid from api where tenantid=#{tenantId} and valid=true group by projectid) ac on ac.projectid=p.id" +
            " where p.tenantId=#{tenantId} and p.valid=true " +
            "<if test=\"parameters.query!=null and parameters.query!=''\">" +
            " and p.name like '%${parameters.query}%' ESCAPE '/' " +
            "</if>" +
            "<if test='parameters.sortby!=null'>" +
            " order by p.${parameters.sortby} " +
            "</if>" +
            "<if test='parameters.order!=null and parameters.sortby!=null'>" +
            " ${parameters.order} " +
            "</if>" +
            "<if test='parameters.limit!=-1'>" +
            " limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            " offset ${parameters.offset} " +
            "</if>" +
            "</script>")
    public List<ProjectInfo> searchProject(@Param("parameters")Parameters parameters,@Param("userId")String userId,
                                           @Param("tenantId")String tenantId,@Param("ids")List<String> ids) throws SQLException;

    @Update("update project set name=#{name},description=#{description},manager=#{manager} where id=#{id}")
    public int updateProject(ProjectInfo project);

    @Select("select manager from project where id=#{id}")
    public String getProjectManager(String id);

    @Select("<script>" +
            "select count(*)over() totalSize,g.id,g.name,g.description from project_group_relation p join user_group g on p.group_id=g.id where " +
            "p.project_id=#{projectId} and g.tenant=#{tenantId} and g.valid=true " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and g.name like '%${param.query}%' ESCAPE '/' " +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            " offset ${param.offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupIdAndName> getRelationUserGroups(@Param("projectId")String projectId, @Param("param")Parameters parameters,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select count(*)over() totalSize,g.id,g.name,g.description from user_group g left join (" +
            " select * from project_group_relation where project_id=#{projectId} ) p " +
            " on p.group_id=g.id where g.tenant=#{tenantId} and p.group_id is null and g.valid=true " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and g.name like '%${param.query}%' ESCAPE '/' " +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            " offset ${param.offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupIdAndName> getNoRelationUserGroups(@Param("projectId")String projectId,@Param("param")Parameters parameters, @Param("tenantId")String tenantId);

    @Select("<script>" +
            "select count(*)over() totalSize,id,name,description from user_group where tenant=#{tenantId} and valid=true " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and name like '%${param.query}%' ESCAPE '/' " +
            "</if>" +
            "<if test='param.limit!=-1'>" +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'>" +
            " offset ${param.offset} " +
            "</if>" +
            "</script>")
    public List<UserGroupIdAndName> getAllUserGroups(@Param("param")Parameters parameters,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select count(*) from project_group_relation where project_id=#{id} and group_id in " +
            " <foreach item='userGroup' index='index' collection='userGroups' separator=',' open='(' close=')'>" +
            " #{userGroup}" +
            " </foreach>" +
            "</script>")
    public  int sameProjectToUserGroup(@Param("id")String id,@Param("userGroups")List<String> userGroups);

    @Delete("<script>" +
            " delete from project_group_relation where  project_id=#{id} and group_id in " +
            " <foreach item='userGroup' index='index' collection='userGroups' separator=',' open='(' close=')'>" +
            "#{userGroup}" +
            " </foreach>" +
            "</script>")
    public int deleteProjectToUserGroup(@Param("id")String id,@Param("userGroups") List<String> userGroups);

    @Update("<script>" +
            " update project set valid=false where  id in " +
            " <foreach item='project' index='index' collection='projects' separator=',' open='(' close=')'>" +
            " #{project}" +
            " </foreach>" +
            "</script>")
    public int deleteProject(@Param("projects") List<String> projects);

    @Delete("<script>" +
            " delete from project_group_relation where project_id in " +
            " <foreach item='project' index='index' collection='projects' separator=',' open='(' close=')'>" +
            " #{project}" +
            " </foreach>" +
            "</script>")
    public int deleteProjectRelation(@Param("projects") List<String> projects);

    @Select("<script>" +
            "select manager from project where id in  " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getProjectsManager(@Param("ids")List<String> ids);

    @Select("select * from project where id=#{id}")
    public ProjectInfo getProjectInfoById(String id);

    @Select("<script>" +
            "select * from project where id in  " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<ProjectInfo> getProjectInfoByIds(@Param("ids")List<String> ids);

    @Insert({" <script>",
             " insert into api(guid,name,sourcetype,sourceid,schemaname,tablename,tableguid,dbguid,categoryguid,creator,createtime,version,description,",
             " protocol,requestmode,path,updater,updatetime,tenantid,pool,status,approve,log,version_num,param,returnparam,sortparam,api_poly_entity,projectid,valid",
             " )values(",
             " #{info.guid},#{info.name},#{info.sourceType},#{info.sourceId},#{info.schemaName},#{info.tableName},#{info.tableGuid},#{info.dbGuid}," +
             " #{info.categoryGuid},#{info.creator},#{info.createTime},#{info.version},#{info.description},#{info.protocol}," +
             " #{info.requestMode},#{info.path},#{info.updater},#{info.updateTime},#{tenantId},#{info.pool},#{info.status},#{info.approve},#{info.log}," +
             " 1," +
             " #{info.param,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
             " #{info.returnParam,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
             " #{info.sortParam,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
             " #{info.apiPolyEntity,jdbcType=OTHER, typeHandler=io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler}," +
             "#{info.projectId},true",
             " )",
             " </script>"})
    public int insertAPIInfoV2(@Param("info") ApiInfoV2 info, @Param("tenantId")String tenantId);

    @Insert({" <script>",
             " insert into api(guid,name,sourcetype,sourceid,schemaname,tablename,tableguid,dbguid,categoryguid,creator,createtime,version,description,",
             " protocol,requestmode,path,updater,updatetime,tenantid,pool,status,approve,log,version_num,param,returnparam,sortparam,api_poly_entity,projectid,valid",
             " )values(",
             " #{info.guid},#{info.name},#{info.sourceType},#{info.sourceId},#{info.schemaName},#{info.tableName},#{info.tableGuid},#{info.dbGuid}," +
             " #{info.categoryGuid},#{info.creator},#{info.createTime},#{info.version},#{info.description},#{info.protocol}," +
             " #{info.requestMode},#{info.path},#{info.updater},#{info.updateTime},#{tenantId},#{info.pool},#{info.status},#{info.approve},#{info.log}," +
             " (select COALESCE(max(cast(version_num as integer)) + 1, 1) from api where guid=#{info.guid})," +
             " #{info.param,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
             " #{info.returnParam,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
             " #{info.sortParam,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
             " #{info.apiPolyEntity,jdbcType=OTHER, typeHandler=io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler}," +
             "#{info.projectId},true",
             " )",
             " </script>"})
    public int updateAPIInfoV2(@Param("info") ApiInfoV2 info, @Param("tenantId")String tenantId);

    @Update(" update api set name=#{info.name},sourcetype=#{info.sourceType},sourceid=#{info.sourceId},schemaname=#{info.schemaName},tablename=#{info.tableName}," +
            " tableguid=#{info.tableGuid},dbguid=#{info.dbGuid},categoryguid=#{info.categoryGuid},version=#{info.version},description=#{info.description}," +
            " protocol=#{info.protocol},requestmode=#{info.requestMode},path=#{info.path},updater=#{info.updater},updatetime=#{info.updateTime},pool=#{info.pool}," +
            " status=#{info.status},approve=#{info.approve},log=#{info.log}," +
            " param=#{info.param,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
            " returnparam=#{info.returnParam,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}," +
            " sortparam=#{info.sortParam,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg}, " +
            " api_poly_entity=#{info.apiPolyEntity,jdbcType=OTHER, typeHandler=io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler}, " +
            " version_num=(select COALESCE(max(cast(version_num as integer)) + 1, 1) from api where guid=#{info.guid}) " +
            " where guid=#{info.guid} and version_num=#{versionNum}")
    public int updateApiInfoV2OnDraft(@Param("info") ApiInfoV2 info, @Param("tenantId")String tenantId,@Param("versionNum")int versionNum);

    @Select("select version from api where guid=#{id} and version_num=(select max(version_num) from api where guid=#{id} and valid=true)")
    public String getMaxVersion(@Param("id") String id);

    @Select("select version from api where guid=#{id} and version_num=(select max(version_num) from api where guid=#{id} and status!='draft' and status!='audit' and valid=true)")
    public String getMaxVersionNoDraft(@Param("id") String id);

    @Select("select count(*) from api where guid=#{id} and version=#{version} and status!='draft' and valid=true")
    public int queryApiSameVersion(@Param("id") String id,@Param("version")String version);

    @Results(id="poly", value ={
            @Result(property = "apiPolyEntity", column = "api_poly_entity", typeHandler = ApiPolyEntityTypeHandler.class, javaType = ApiPolyEntity.class)
    })
    @Select("select * from api where guid=#{id} and version=#{version} and valid=true")
    public ApiInfoV2 getApiInfoByVersion(@Param("id") String id, @Param("version")String version);


    @Select("select guid apiId,name apiName,description,version,status,updateTime,u.username, " +
            "(select count(1) > 0 from api_relation where apiid = a.guid and version = a.version) as used " +
            "from api a left join  users u on a.updater = u.userid where a.guid=#{id} and a.valid=true order by version_num desc")
    public List<ApiVersion> getApiVersion(@Param("id") String id);

    @Select("select count(1) from api where name=#{name} and tenantid=#{tenantId} and projectid=#{projectId} and guid!=#{guid} and valid=true")
    public int queryApiSameName(@Param("name")String name,@Param("tenantId")String tenantId,@Param("projectId")String projectId,@Param("guid")String guid);

    @Select(" select count(1) from api_category where name=#{name} " +
            " and projectid=#{projectId} and tenantid=#{tenantId} and guid!=#{id}")
    public int querySameNameCategory(@Param("name") String categoryName, @Param("projectId")String projectId, @Param("tenantId")String tenantId,@Param("id") String categoryId);

    @Select("select guid from api_category where projectid=#{projectId} and (downBrotherCategoryGuid is NULL or downBrotherCategoryGuid='') and level=1 and tenantid=#{tenantId}")
    public String queryLastCategory(@Param("projectId")String projectId,@Param("tenantId")String tenantId);

    @Insert("insert into api_category(guid,name,description,upBrotherCategoryGuid,downBrotherCategoryGuid,parentCategoryGuid,qualifiedName,level,tenantid,createtime,projectid)" +
            "values(#{category.guid},#{category.name},#{category.description},#{category.upBrotherCategoryGuid},#{category.downBrotherCategoryGuid},#{category.parentCategoryGuid},#{category.qualifiedName},#{category.level},#{tenantId},#{category.createTime},#{projectId})")
    public int add(@Param("category") CategoryEntityV2 category, @Param("projectId") String projectId, @Param("tenantId") String tenantId);

    @Update("update api_category set downBrotherCategoryGuid=#{downBrotherCategoryGuid} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateDownBrotherCategoryGuid(@Param("guid")String guid, @Param("downBrotherCategoryGuid")String downBrothCatalogGuid,@Param("tenantId")String tenantId);

    @Update("update api_category set name=#{category.name},description=#{category.description},qualifiedName=#{category.qualifiedName} where guid=#{category.guid} and tenantid=#{tenantId}")
    public int updateCategoryInfo(@Param("category") CategoryEntity category, @Param("tenantId")String tenantId);

    @Select("select * from api_category where guid=#{guid} and tenantid=#{tenantId}")
    public CategoryEntityV2 queryByGuid(@Param("guid") String categoryGuid,@Param("tenantId")String tenantId);

    @Update("update api_category set upBrotherCategoryGuid=#{upBrotherCategoryGuid} where guid=#{guid} and tenantid=#{tenantId}")
    public int updateUpBrotherCategoryGuid(@Param("guid")String guid, @Param("upBrotherCategoryGuid")String upBrothCatalogGuid,@Param("tenantId")String tenantId);

    @Delete("delete from api_category where guid=#{guid} and tenantid=#{tenantId}")
    public int deleteCategory(@Param("guid")String guid,@Param("tenantId")String tenantId);

    @Select("select * from api_category left join(" +
            "  select count(distinct guid) count,categoryguid from api where valid=true group by categoryguid " +
            ") c on c.categoryguid=api_category.guid where api_category.projectid=#{projectId} and tenantid=#{tenantId}")
    public List<CategoryPrivilege> getCategoryByProject(@Param("projectId")String projectId,@Param("tenantId")String tenantId);

    @Update("update api set valid=false where categoryguid=#{categoryId}")
    public int deleteApiByCategory(@Param("categoryId")String categoryId);

    @Update("update api set categoryguid=#{newCategoryId} where categoryguid=#{oldCategoryId}")
    public int upDateApiByCategory(@Param("oldCategoryId")String oldCategoryId,@Param("newCategoryId")String newCategoryId);

    @Select("select * from api_category where name=#{name} and projectid=#{projectId} and tenantid=#{tenantId}")
    public CategoryEntityV2 getCategoryByName(@Param("name")String name,@Param("projectId") String projectId,@Param("tenantId")String tenantId);

    @Delete("<script>" +
            " delete from api_category where projectid in " +
            " <foreach item='project' index='index' collection='projects' separator=',' open='(' close=')'>" +
            " #{project}" +
            " </foreach>" +
            "</script>")
    public int deleteCategoryByProject(@Param("projects") List<String> projects);

    @Delete("<script>" +
            " update api set valid=false where projectid in " +
            " <foreach item='project' index='index' collection='projects' separator=',' open='(' close=')'>" +
            " #{project}" +
            " </foreach>" +
            "</script>")
    public int deleteApiByProject(@Param("projects") List<String> projects);

    @Select("select * from api_category where guid=#{id}")
    public CategoryEntityV2 getCategoryById(@Param("id")String id);

    @Select("select param from api where guid=#{guid} and version=#{version} and valid=true")
    public Object getParamByGuid(@Param("guid")String guid,@Param("version")String version);

    @Select("select returnparam from api where guid=#{guid} and version=#{version} and valid=true")
    public Object getReturnParamByGuid(@Param("guid")String guid,@Param("version")String version);

    @Select("select sortparam from api where guid=#{guid} and version=#{version} and valid=true")
    public Object getSortParamByGuid(@Param("guid")String guid,@Param("version")String version);

    @Select({" <script>",
             " select count(1)over() total,api.guid id,api.description,api.name,api.approve,api.status,api.createtime,api.categoryguid categoryId,api.version,users.username creator,ac.name categoryName ",
             " from api join users on api.creator=users.userid join api_category ac on api.categoryguid=ac.guid join ",
             "( select guid,max(version_num) max from api where valid=true and ((status!='draft' and status!='audit' ) or " +
             " guid in (select guid from api where valid=true group by guid having count(*)=1)" +
             ") group by guid) v on v.guid=api.guid and v.max=api.version_num ",
             "where api.projectid=#{projectId} and  api.tenantid=#{tenantId} ",
             "<if test=\"param.query!=null and param.query!=''\">" +
             " and api.name like '%${param.query}%' ESCAPE '/' " +
             "</if>" +
             " <if test=\"categoryId!=null and categoryId !='all'\">",
             " and api.categoryguid=#{categoryId}",
             " </if>",
             " <if test=\"status!=null and status!=''\">",
             " and api.status=#{status}",
             " </if>",
             " <if test='approve != null'>",
             " and api.approve=#{approve}",
             " </if>",
             "<if test=\"param.sortby!=null and param.sortby!=''\">",
             " order by api.${param.sortby} ",
             "<if test=\"param.order!=null and param.order!=''\">",
             " ${param.order} ",
             "</if>",
             "</if>",
             " <if test='param.limit != null and param.limit!=-1'>",
             " limit #{param.limit}",
             " </if>",
             " <if test='param.offset != null'>",
             " offset #{param.offset}",
             " </if>",
             " </script>"})
    public List<ApiHead> searchApi(@Param("param") Parameters parameters,@Param("projectId") String projectId,@Param("categoryId")String categoryId,
                                   @Param("status")String status,@Param("approve")Boolean approve,@Param("tenantId")String tenantId) throws SQLException;

    @Select("<script>" +
            " select api.guid id,api.description,api.name,api.approve,api.status,api.createtime,api.categoryguid categoryId,api.version from api join " +
            " (select guid,max(version_num) version_num from api where guid=#{id} " +
            " and valid=true and status!='draft' and status!='audit' " +
            " group by guid) v on api.guid=v.guid and api.version_num=v.version_num " +
            "</script>")
    public ApiHead getSubmitApiHeadById(@Param("id")String id);

    @Update("<script>" +
            "update api set categoryguid=#{moveApi.newCategoryId} where categoryguid=#{moveApi.oldCategoryId} and guid in " +
            " <foreach item='apiId' index='index' collection='moveApi.apiIds' separator=',' open='(' close=')'>" +
            " #{apiId}" +
            " </foreach>" +
            "</script>")
    public int moveApi(@Param("moveApi") MoveApi moveApi);

    @Select("select distinct guid from api where categoryguid=#{categoryId}")
    public List<String> getApiIdByCategory(@Param("categoryId")String categoryId);

    @Select("select count(*) from api where categoryguid=#{categoryId} and status='up'")
    public int getApiUpByCategory(@Param("categoryId")String categoryId);

    @Update("update api set status=#{status},updatetime=#{updateTime} where guid=#{guid}  and status!='draft' and status!='audit'")
    public int updateApiStatus(@Param("guid")String guid, @Param("status")String status, @Param("updateTime")Timestamp updateTime);

    @Update("update api set status=#{status},updatetime=#{updateTime} where guid=#{guid}  and version=#{version}")
    public int updateApiVersionStatus(@Param("guid")String guid, @Param("version")String version,@Param("status")String status, @Param("updateTime")Timestamp updateTime);

    @Insert("insert into api_log(apiid,type,userid,time)values(#{apiLog.apiId},#{apiLog.type},#{apiLog.creator},#{apiLog.date})")
    public int addApiLog(@Param("apiLog")ApiLog apiLog);

    @Insert(" <script>" +
            "insert into api_log(apiid,type,userid,time)values" +
            " <foreach item='apiLog' index='index' collection='apiLogs' separator='),(' open='(' close=')'>" +
            " #{apiLog.apiId},#{apiLog.type},#{apiLog.creator},#{apiLog.date} " +
            " </foreach>" +
            " </script>")
    public int addApiLogs(@Param("apiLogs")List<ApiLog> apiLogs);

    @Select("<script>"+
            " select count(1)over() total,api_log.time date,api_log.apiid,api_log.type,users.username creator from api_log join users on api_log.userid=users.userid " +
            " where api_log.apiid=#{apiId} " +
            "<if test=\"param.query!=null and param.query!=''\">" +
            " and (users.username like '%${param.query}%' ESCAPE '/' " +
            "<if test=\"type!=null and type!=''\">" +
            " or  api_log.type like '%${type}%' ESCAPE '/'" +
            "</if>" +
            ")" +
            "</if>" +
            " order by api_log.time desc" +
            " <if test='param.limit != null and param.limit!=-1'>" +
            " limit #{param.limit}" +
            " </if>" +
            " <if test='param.offset != null'>" +
            " offset #{param.offset}" +
            " </if>" +
            " </script>")
    public List<ApiLog> getApiLog(@Param("param") Parameters parameters,@Param("apiId") String apiId,@Param("type") String type);

    @ResultMap("poly")
    @Select("<script>" +
            " select api.* from api join " +
            " (select guid,max(version_num) version_num from api where guid in  " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            " and valid=true " +
            " group by guid) v on api.guid=v.guid and api.version_num=v.version_num " +
            "</script>")
    public List<ApiInfoV2> getApiInfoByIds(@Param("ids")List<String> ids);

    @ResultMap("poly")
    @Select("<script>" +
            " select api.* from api join " +
            " (select guid,max(version_num) version_num from api where guid in  " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            " and valid=true and status!='draft' and status!='audit'" +
            " group by guid) v on api.guid=v.guid and api.version_num=v.version_num " +
            "</script>")
    public List<ApiInfoV2> getNoDraftApiInfoByIds(@Param("ids")List<String> ids);

    @Select("<script>" +
            " select count(*) from api where guid in  " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            " and valid=true and status='up' " +
            "</script>")
    public int getStatusCountByIds(@Param("ids")List<String> ids);

    @Update("<script>" +
            " update api set valid=false where guid in  " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public int deleteApiByIds(@Param("ids")List<String> ids);

    @Update("<script>" +
            " update api set valid=false where guid=#{api.apiId} and api.version=#{api.version} " +
            "</script>")
    public int deleteApiVersion(@Param("api")ApiVersion api);

    @Select("select count(*) from user_group_relation u join project_group_relation p on u.group_id=p.group_id where u.user_id=#{userId} and p.project_id=#{projectId}")
    public int projectPrivateByProject(@Param("userId")String userId,@Param("projectId")String projectId);

    @Select("select projectid from api_category where guid=#{id}")
    public String getProjectIdByCategory(@Param("id")String id);

    @Update("update api set mobius_id=#{mobiusId} where guid=#{guid}  and version=#{version}")
    public int updateApiMobiusId(@Param("guid")String guid, @Param("version")String version,@Param("mobiusId")String mobiusId);

    @Select("select mobius_id from api where guid=#{id} and valid=true")
    public List<String> getApiMobiusIds(@Param("id")String id);

    @Select("select mobius_id from api where guid=#{id} and version={version}}")
    public String getApiMobiusIdByVersion(@Param("id")String id,@Param("version")String version);

    @Select("<script>" +
            "select mobius_id from api where valid=true and guid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getApiMobiusIdsByIds(@Param("ids")List<String> ids);

    @Select("select mobius_id from api where categoryguid=#{categoryId}")
    public List<String> getApiMobiusByCategory(@Param("categoryId")String categoryId);

    @Select("<script>" +
            "select mobius_id from api where projectid in " +
            " <foreach item='id' index='index' collection='projectIds' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public List<String> getApiMobiusByProjects(@Param("projectIds")List<String> projectIds);

    @Select("select api_group.mobius_id from api join api_relation on api.guid=api_relation.apiid and api.version=api_relation.version join api_group on api_relation.groupid=api_group.id where api.mobius_id=#{id}")
    public List<String> getMobiusApiGroupIds(@Param("id")String id);

    @Select("<script>" +
            "select count(*) from api where status='up' and projectid in " +
            " <foreach item='id' index='index' collection='projectIds' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public int getApiUpNumByProjects(@Param("projectIds")List<String> projectIds);

    @Select("SELECT DISTINCT tableInfo.dbName FROM table_relation,tableInfo, data_source WHERE " +
            "table_relation.categoryGuid IN (SELECT DISTINCT t2.guid FROM category_group_relation t1 " +
            "JOIN category t2 ON t1.category_id = t2.guid JOIN user_group t3 ON (t1.group_id = t3.id AND t3.valid = TRUE )" +
            "JOIN user_group_relation t4 ON t3. ID = t4.group_id WHERE t2.tenantid = t3.tenant AND t3.tenant = #{tenantId} " +
            "AND t4.user_id = #{userId} AND t2.categorytype = 0) " +
            "AND tableInfo.tableGuid = table_relation.tableGuid " +
            "AND tableinfo.source_id = data_source.source_id " +
            "AND tableInfo.status = 'ACTIVE' " +
            "AND tableinfo.source_id = #{sourceId}")
    List<String> getUserRelationDatabases(@Param("tenantId") String tenantId,@Param("sourceId") String sourceId,@Param("userId") String userId);

    @Select("SELECT DISTINCT tableInfo.dbName FROM table_relation, tableInfo " +
            "WHERE table_relation.categoryGuid in(SELECT DISTINCT t2.guid FROM " +
            "category_group_relation t1 JOIN category t2 ON t1.category_id = t2.guid " +
            "JOIN user_group t3 ON ( t1.group_id = t3. ID AND t3. VALID = TRUE) " +
            "JOIN user_group_relation t4 ON t3. ID = t4.group_id WHERE t2.tenantid = t3.tenant " +
            "AND t3.tenant = #{tenantId} AND t4.user_id = #{userId} AND t2.categorytype = 0) " +
            "AND tableInfo.tableGuid = table_relation.tableGuid " +
            "AND tableInfo.status = 'ACTIVE' " +
            "AND tableinfo.source_id = 'hive'")

    List<String> getUserHiveDatabases(@Param("tenantId") String tenantId, @Param("userId") String userId);

    @Select("select DISTINCT tableInfo.tablename from tableinfo where tableinfo.status = 'ACTIVE' and tableinfo.source_id = #{sourceId} and tableinfo.dbname = #{database}")
    List<String> getDatabaseTables(@Param("sourceId") String sourceId, @Param("database") String database);

    @Select("select DISTINCT column_info.column_name from tableinfo join column_info on tableinfo.tableguid = column_info.table_guid " +
            "where tableinfo.status = 'ACTIVE' and tableinfo.source_id = #{sourceId} and tableinfo.dbname = #{database} and tableinfo.tablename = #{tableName}")
    List<String> getUserColumns(@Param("sourceId") String sourceId, @Param("database") String database, @Param("tableName") String tableName);

}
