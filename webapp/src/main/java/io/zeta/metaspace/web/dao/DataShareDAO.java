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

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableOwner;
import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.ProjectInfo;
import io.zeta.metaspace.model.share.QueryParameter;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

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
             " apiInfo.groupGuid=apiGroup.guid and apiInfo.name like '%${query}%' ESCAPE '/'",
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

    @Select({" <script>",
             " select count(1)over() total,apiInfo.guid,apiInfo.name,apiInfo.tableGuid,apiInfo.groupGuid,apiInfo.publish,users.username as keeper,",
             " tableInfo.tableName,apiGroup.name as groupName, tableInfo.display_name as tableDisplayName",
             " from apiInfo,tableInfo,apiGroup,users where",
             " apiInfo.tableGuid in",
             " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>" ,
             " #{tableGuid}",
             " </foreach>",
             " and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid and apiInfo.tenantid=#{tenantId} ",
             " and users.userId=apiInfo.keeper order by apiInfo.updateTime desc",
             " <if test='limit != null and limit!=-1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset != null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<APIInfoHeader> getTableRelatedAPI(@Param("tableList")List<String> tableList, @Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId);
    

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
            " select count(*)over() total,p.*,uc.usercount from (" +
            "select project.id,project.name,project.description,project.createtime,project.tenantid,project.valid,user1.username creator,user2.username manager,project.manager managerId from " +
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
            " ) uc group by project_id ) uc on p.id=uc.id " +
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
    public List<ProjectInfo> searchProject(@Param("parameters")Parameters parameters,@Param("userId")String userId,@Param("tenantId")String tenantId);

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
}
