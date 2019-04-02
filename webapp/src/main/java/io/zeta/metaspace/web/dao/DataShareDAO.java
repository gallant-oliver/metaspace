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

import io.zeta.metaspace.model.share.APIInfo;
import io.zeta.metaspace.model.share.APIInfoHeader;
import io.zeta.metaspace.model.share.QueryParameter;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

public interface DataShareDAO {

    @Insert({" <script>",
             " insert into apiInfo(guid,name,tableGuid,dbGuid,groupGuid,keeper,maxRowNumber,fields,",
             " version,description,protocol,requestMode,returnType,path,generateTime,updater,updateTime,publish,star",
             " )values(",
             " #{guid},#{name},#{tableGuid},#{dbGuid},#{groupGuid},#{keeper},#{maxRowNumber},#{fields,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},",
             " #{version},#{description},#{protocol},#{requestMode},#{returnType},#{path},#{generateTime},#{updater},#{updateTime},#{publish},#{star})",
             " </script>"})
    public int insertAPIInfo(APIInfo info);

    @Update({" <script>",
             " update apiInfo set name=#{name},tableGuid=#{tableGuid},dbGuid=#{dbGuid},groupGuid=#{groupGuid},maxRowNumber=#{maxRowNumber},",
             " fields=#{fields,jdbcType=OTHER, typeHandler=io.zeta.metaspace.model.metadata.JSONTypeHandlerPg},",
             " version=#{version},description=#{description},protocol=#{protocol},requestMode=#{requestMode},returnType=#{returnType},path=#{path},",
             " updater=#{updater},updateTime=#{updateTime}",
             " where",
             " guid=#{guid}",
             " </script>"})
    public int updateAPIInfo(APIInfo info);

    @Select("select apiInfo.*,tableInfo.tableName,tableInfo.dbName,apiGroup.name as groupName from apiInfo,tableInfo,apiGroup where apiInfo.guid=#{guid} and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid")
    public APIInfo getAPIInfoByGuid(@Param("guid")String guid);

    @Select({" <script>",
             " select apiInfo.guid,apiInfo.name,apiInfo.tableGuid,apiInfo.groupGuid,apiInfo.publish,apiInfo.keeper,apiInfo.version,apiInfo.updater,apiInfo.updateTime,apiInfo.star,",
             " tableInfo.tableName,apiGroup.name as groupName",
             " from apiInfo,tableInfo,apiGroup where",
             " apiInfo.groupGuid=#{groupGuid}",
             " and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid",
             " <if test='my==0'>",
             " and keeper=#{keeper}",
             " </if>",
             " <choose>",
             " <when test=\"publish=='unpublish'\"> and publish=0 </when>",
             " <when test=\"publish=='publish'\"> and publish=1 </when>",
             " </choose>",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<APIInfoHeader> getAPIList(@Param("groupGuid")String guid, @Param("my")Integer my, @Param("publish")String publish, @Param("keeper")String keeper, @Param("limit")int limit, @Param("offset")int offset);


    @Select({" <script>",
             " select count(1)",
             " from apiInfo,tableInfo,apiGroup where",
             " apiInfo.groupGuid=#{groupGuid}",
             " and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid",
             " <if test='my==0'>",
             " and keeper=#{keeper}",
             " </if>",
             " <choose>",
             " <when test=\"publish=='unpublish'\"> and publish=0 </when>",
             " <when test=\"publish=='publish'\"> and publish=1 </when>",
             " </choose>",
             " </script>"})
    public int getAPICount(@Param("groupGuid")String guid, @Param("my")Integer my, @Param("publish")String publish, @Param("keeper")String keeper);

    @Select("select fields from apiInfo where guid=#{guid}")
    public Object getQueryFiledsByGuid(@Param("guid")String guid);

    @Select("select dataOwner from tableInfo where tableGuid=#{guid}")
    public Object getDataOwnerByGuid(@Param("guid")String guid);

    @Select("select count(1) from apiInfo where name=#{name}")
    public int querySameName(@Param("name")String name);

    @Delete("delete from apiInfo where guid=#{guid}")
    public int deleteAPIInfo(@Param("guid")String guid);

    @Update("update apiInfo set star=#{star} where guid=#{guid}")
    public int updateStarStatus(@Param("guid")String guid, @Param("star")Boolean starStatus);

    @Update({" <script>",
             " update apiInfo set publish=#{publish} where guid in",
             " <foreach item='guid' index='index' collection='guidList' separator=',' open='(' close=')'>" ,
             " #{guid}",
             " </foreach>",
             " </script>"})
    public int updatePublishStatus(@Param("guidList")List<String> guidList, @Param("publish")Boolean publishStatus);

    @Update({" <script>",
             " select ",
             " <foreach item='column' index='index' collection='columns' separator=','>" ,
             " ${column}",
             " </foreach>",
             " from ${tableName}",
             " where ",
             " <foreach item='kv' index='index' collection='kvList' separator=' and '>" ,
             " ${kv.columnName}=${kv.value}",
             " </foreach>",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<Object> queryDataByAPI(@Param("tableName")String tableName, @Param("columns")List<String> columns, @Param("kvList")List<QueryParameter.Parameter> kvList, @Param("limit")long limit, @Param("offset")long offset);

    @Select("select tableName from tableInfo where tableGuid=#{tableGuid}")
    public String queryTableNameByGuid(@Param("tableGuid")String guid);

    @Select("select dbName from tableInfo where tableGuid=#{tableGuid}")
    public String querydbNameByGuid(@Param("tableGuid")String guid);

    @Select({" <script>",
             " select apiInfo.guid,apiInfo.name,apiInfo.tableGuid,apiInfo.maxRowNumber,apiInfo.publish,tableInfo.tableName",
             " from apiInfo,tableInfo",
             " where apiInfo.tableGuid=tableInfo.tableGuid",
             " and path=#{path}",
             " </script>"})
    public APIInfo getAPIInfo(@Param("path")String path);

    @Select("select fields from apiInfo where path=#{path}")
    public Object getAPIFields(@Param("path")String path);

    @Select({" <script>",
             " select apiInfo.guid,apiInfo.name,apiInfo.tableGuid,apiInfo.groupGuid,apiInfo.publish,apiInfo.keeper,",
             " tableInfo.tableName,apiGroup.name as groupName",
             " from apiInfo,tableInfo,apiGroup where",
             " apiInfo.tableGuid in",
             " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>" ,
             " #{tableGuid}",
             " </foreach>",
             " and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<APIInfoHeader> getTableRelatedAPI(@Param("tableList")List<String> tableList, @Param("limit")int limit,@Param("offset") int offset);

    @Select({" <script>",
             " select count(1)",
             " from apiInfo,tableInfo,apiGroup where",
             " apiInfo.tableGuid in",
             " <foreach item='tableGuid' index='index' collection='tableList' separator=',' open='(' close=')'>" ,
             " #{tableGuid}",
             " </foreach>",
             " and apiInfo.tableGuid=tableInfo.tableGuid and apiInfo.groupGuid=apiGroup.guid",
             " </script>"})
    public int countTableRelatedAPI(@Param("tableList")List<String> tableList);
}
