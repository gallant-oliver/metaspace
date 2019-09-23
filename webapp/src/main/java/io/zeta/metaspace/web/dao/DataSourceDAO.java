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

import io.zeta.metaspace.model.dataSource.DataSource;
import io.zeta.metaspace.model.dataSource.DataSourceBody;
import io.zeta.metaspace.model.dataSource.DataSourceConnection;
import io.zeta.metaspace.model.dataSource.DataSourceHead;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.model.dataSource.DataSourceSearch;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.TableHeader;
import io.zeta.metaspace.model.user.UserIdAndName;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.List;



public interface DataSourceDAO {
    //添加数据源
    @Insert("insert into data_source(source_id,source_name,source_type,description,create_time,update_time,update_user_id,ip,port,username,password,database,jdbc_parameter,create_user_id)" +
            "values(#{dataSourceBody.sourceId},#{dataSourceBody.sourceName},#{dataSourceBody.sourceType},#{dataSourceBody.description},#{dataSourceBody.updateTime},#{dataSourceBody.updateTime},#{updateUserId},#{dataSourceBody.ip},#{dataSourceBody.port},#{dataSourceBody.userName},#{dataSourceBody.password},#{dataSourceBody.database},#{dataSourceBody.jdbcParameter},#{updateUserId})")
    public int add( @Param("updateUserId") String updateUserId, @Param("dataSourceBody") DataSourceBody dataSourceBody);

    //更新数据源
    @Update("update data_source set " +
            "source_name=#{dataSourceBody.sourceName}," +
            "description=#{dataSourceBody.description}," +
            "ip=#{dataSourceBody.ip}," +
            "port=#{dataSourceBody.port}," +
            "username=#{dataSourceBody.userName}," +
            "password=#{dataSourceBody.password}," +
            "database=#{dataSourceBody.database}," +
            "jdbc_parameter=#{dataSourceBody.jdbcParameter}," +
            "update_user_id=#{updateUserId}," +
            "update_time=#{dataSourceBody.updateTime}" +
            "where source_id=#{dataSourceBody.sourceId}")
    public int updateNoRely(@Param("updateUserId") String updateUserId,@Param("dataSourceBody") DataSourceBody dataSourceBody);

    //更新数据源
    @Update("update data_source set " +
            "source_name=#{dataSourceBody.sourceName}," +
            "description=#{dataSourceBody.description}," +
            "update_user_id=#{updateUserId}," +
            "update_time=#{dataSourceBody.updateTime}" +
            "where source_id=#{dataSourceBody.sourceId}")
    public int updateRely(@Param("updateUserId") String updateUserId,@Param("dataSourceBody") DataSourceBody dataSourceBody);

    //数据源id是否存在
    @Select("select count(1) from data_source where source_id=#{sourceId}")
    public int isSourceId(@Param("sourceId") String sourceId);

    //数据源名称是否存在
    @Select("select count(1) from data_source where source_name=#{sourceName} and source_id!=#{sourceId}")
    public int isSourceName(@Param("sourceName") String sourceName,@Param("sourceId") String sourceId);

    //查询数据源名字
    @Select("select source_name from data_source where source_id=#{sourceId}")
    public String getSourceNameForSourceId(@Param("sourceId") String sourceId);

    //删除数据源
    @Delete({"<script>",
             "delete from data_source where source_id in ",
             "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" ,
             "#{sourceId}",
             "</foreach>",
             "</script>"})
    public int deleteDataSource(@Param("sourceIds") List<String> sourceIds);

    //删除授权人
    @Delete({"<script>",
             "delete from data_source_authorize where source_id in ",
             "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" ,
             "#{sourceId}",
             "</foreach>",
             "</script>"})
    public int deleteAuthorizeBySourceId(@Param("sourceIds") List<String> sourceIds);

    //获取数据源详情
    @Select("select source_type sourceType,source_name sourceName,description,ip,port,username userName,password,database,jdbc_parameter jdbcParameter " +
            "from data_source where source_id=#{sourceId};")
    public DataSourceInfo getDataSourceInfo(@Param("sourceId") String sourceId);

    //搜索数据源
    @Select("<script>" +
            "select count(*)over() totalSize,ds.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,ds.description,to_char(ds.create_time,'yyyy-MM-dd HH:mm:ss') createTime,to_char(ds.update_time,'yyyy-MM-dd HH:mm:ss') updateTime,us.username updateUserName from data_source ds join users us on ds.update_user_id=us.userid join data_source_authorize dsa on dsa.source_id=ds.source_id " +
            "where dsa.authorize_user_id=#{userId}" +
            "<if test='dataSourceSearch.sourceName!=null'>" +
            "and ds.source_name like '%${dataSourceSearch.sourceName}%' ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null'>" +
            "and ds.source_type like '%${dataSourceSearch.sourceType}%' ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null'>" +
            "and to_char(ds.create_time,'yyyy-MM-dd HH-mm-ss') like '%${dataSourceSearch.createTime}%' ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null'>" +
            "and to_char(ds.update_time,'yyyy-MM-dd HH-mm-ss') like '%${dataSourceSearch.updateTime}%' ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateUserName!=null'>" +
            "and us.username like '%${dataSourceSearch.updateUserName}%' ESCAPE '/'" +
            "</if>" +
            "<if test='parameters.sortby!=null'>" +
            "order by ds.${parameters.sortby} " +
            "</if>" +
            "<if test='parameters.order!=null and parameters.sortby!=null'>" +
            "${parameters.order} " +
            "</if>" +
            "<if test='parameters.limit!=-1'>" +
            "limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            "offset ${parameters.offset}" +
            "</if>" +
            "</script>")
    public List<DataSourceHead> searchDataSources(@Param("parameters") Parameters parameters,@Param("dataSourceSearch") DataSourceSearch dataSourceSearch,@Param("userId") String userId);


    @Select("select count(*) from data_source ")
    public int exportDataSource();

    @Select("<script>" +
            "select source_name as sourceName, source_type as sourceType, description as description, ip as ip, port as port, username as username, password as password, database as database, jdbc_parameter as jdbcParameter from data_source" +
            " where source_id in " +
            "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" +
            "#{sourceId}" +
            "</foreach>" +
            "</script>")
    public List<DataSourceBody> getDataSource(@Param("sourceIds") List<String> sourceIds);


    @Select("select source_name from data_source ")
    public List<String> getDataSourceList();

    @Update({" <script>",
            " update data_source set source_type=#{dataSource.sourceType},description=#{dataSource.description},ip=#{dataSource.ip},port=#{dataSource.port},username=#{dataSource.userName},password=#{dataSource.password},database=#{dataSource.database},jdbc_parameter=#{dataSource.jdbcParameter},update_time=#{dataSource.updateTime},update_user_id=#{userId} where source_name=#{dataSource.sourceName}",
            " </script>"})
    public int updateDataSource(@Param("userId")String userId,@Param("dataSource")DataSourceBody dataSource);


    //获取测试连接参数
    @Select("select source_type sourceType,ip,port,username userName,password,database,jdbc_parameter jdbcParameter " +
            "from data_source where source_id=#{sourceId};")
    public DataSourceConnection getConnectionBySourceId(@Param("sourceId") String SourceId);

    //判断是否是创建用户
    @Select("select count(1) from data_source where source_id=#{sourceId} and create_user_id=#{userId}")
    public int isCreateUser(@Param("sourceId") String sourceId,@Param("userId") String userId);



    //获取数据源已授权人
    @Select("select count(*)over() totalSize,users.userid,users.userName,users.account from users join data_source_authorize on data_source_authorize.authorize_user_id=users.userid " +
            "where source_id=#{sourceId} and users.userid!=#{userId}")
    public List<UserIdAndName> getAuthorizeUser(@Param("sourceId") String sourceId,@Param("userId") String userId);

    //获取数据源未授权人
    @Select("<script>" +
            "select count(*)over() totalSize,userid,username userName,account from users " +
            "where userid not in (select authorize_user_id from data_source_authorize where source_id=#{sourceId}) " +
            "<if test='query!=null'>" +
            "and username like '%${query}%' ESCAPE '/'" +
            "</if>" +
            "</script>")
    public List<UserIdAndName> getNoAuthorizeUser(@Param("sourceId") String sourceId,@Param("query") String query);

    //新增授权人
    @Insert("insert into data_source_authorize(source_id,authorize_user_id)" +
            "values(#{sourceId},#{authorizeUserId})")
    public int addAuthorize(@Param("sourceId") String updateUserId,@Param("authorizeUserId") String authorizeUserId);

    //新增授权人
    @Insert({"<script>",
            "insert into data_source_authorize(source_id,authorize_user_id)" ,
            "values",
            "<foreach collection='authorizeUserIds' item='userId' index='index' separator='),(' open='(' close=')'>" ,
            "#{sourceId},#{userId}",
            "</foreach>",
            "</script>"})
    public int addAuthorizes(@Param("sourceId") String updateUserId,@Param("authorizeUserIds") List<String> authorizeUserIds);



    //删除授权人
    @Delete({"<script>",
             "delete from data_source_authorize where source_id=#{sourceId} and authorize_user_id in ",
             "<foreach collection='noAuthorizeUserIds' item='userId' index='index' separator=',' open='(' close=')'>" ,
             "#{userId}",
             "</foreach>",
             "</script>"})
    public int deleteAuthorize(@Param("sourceId") String sourceId,@Param("noAuthorizeUserIds") List<String> noAuthorizeUserIds);

    //获取数据源已授权人id
    @Select("select authorize_user_id from data_source_authorize " +
            "where source_id=#{sourceId}")
    public List<String> getAuthorizeUserIds(@Param("sourceId") String sourceId);

    //根据数据源名字获取数据源id
    @Select("select source_id from data_source where source_name=#{sourceName}")
    public String getSourceIdBySourceName(@Param("sourceName") String sourceName);
}
