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
import io.zeta.metaspace.model.dataSource.DataSourceHead;
import io.zeta.metaspace.model.dataSource.DataSourceInfo;
import io.zeta.metaspace.model.dataSource.DataSourceSearch;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.TableHeader;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.util.List;



public interface DataSourceDAO {
    //添加数据源
    @Insert("insert into data_source(source_id,source_name,source_type,description,create_time,update_time,update_user_id,ip,port,username,password,database,jdbc_parameter)" +
            "values(#{dataSourceBody.sourceId},#{dataSourceBody.sourceName},#{dataSourceBody.sourceType},#{dataSourceBody.description},#{dataSourceBody.updateTime},#{dataSourceBody.updateTime},#{updateUserId},#{dataSourceBody.ip},#{dataSourceBody.port},#{dataSourceBody.userName},#{dataSourceBody.password},#{dataSourceBody.database},#{dataSourceBody.jdbcParameter})")
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
    public int update(@Param("updateUserId") String updateUserId,@Param("dataSourceBody") DataSourceBody dataSourceBody);

    //数据源id是否存在
    @Select("select count(1) from data_source where source_id=#{sourceId}")
    public int isSourceId(@Param("sourceId") String sourceId);

    //数据源名称是否存在
    @Select("select count(1) from data_source where source_name=#{sourceName}")
    public int isSourceName(@Param("sourceName") String sourceName);

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

    @Select("select source_type sourceType,source_name sourceName,description,ip,port,username userName,password,database,jdbc_parameter jdbcParameter " +
            "from data_source where source_id=#{sourceId};")
    public DataSourceInfo getDataSourceInfo(@Param("sourceId") String sourceId);

    @Select("<script>" +
            "select count(*)over() count,ds.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,description,to_char(create_time,'yyyy-MM-dd HH:mm:ss') createTime,to_char(update_time,'yyyy-MM-dd HH:mm:ss') updateTime,us.username updateUserName from data_source ds join users us on ds.update_user_id=us.userid " +
            "<if test='dataSourceSearch.sourceName!=null or dataSourceSearch.sourceType!=null or dataSourceSearch.createTime!=null or dataSourceSearch.updateTime!=null or dataSourceSearch.updateUserName!=null'>" +
            "where " +
            "</if>" +
            "<if test='dataSourceSearch.sourceName!=null'>" +
            "source_name like '%${dataSourceSearch.sourceName}%' " +
            "</if>" +
            "<if test='dataSourceSearch.sourceName!=null and (dataSourceSearch.sourceType!=null or dataSourceSearch.createTime!=null or dataSourceSearch.updateTime!=null or dataSourceSearch.updateUserName!=null)'>" +
            "and " +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null'>" +
            "source_type like '%${dataSourceSearch.sourceType}%' " +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null and (dataSourceSearch.createTime!=null or dataSourceSearch.updateTime!=null or dataSourceSearch.updateUserName!=null)'>" +
            "and " +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null'>" +
            "to_char(create_time,'yyyy-MM-dd HH-mm-ss') like '%${dataSourceSearch.createTime}%' " +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null and (dataSourceSearch.updateTime!=null or dataSourceSearch.updateUserName!=null)'>" +
            "and " +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null'>" +
            "to_char(update_time,'yyyy-MM-dd HH-mm-ss') like '%${dataSourceSearch.updateTime}%' " +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null and dataSourceSearch.updateUserName!=null'>" +
            "and " +
            "</if>" +
            "<if test='dataSourceSearch.updateUserName!=null'>" +
            "us.username like '%${dataSourceSearch.updateUserName}%' " +
            "</if>" +
            "<if test='parameters.sortby!=null'>" +
            "order by ${parameters.sortby} " +
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
    public List<DataSourceHead> searchDataSources(@Param("parameters") Parameters parameters,@Param("dataSourceSearch") DataSourceSearch dataSourceSearch);


    @Select("select count(*) from data_source ")
    public int exportDataSource();

    @Select("select source_name as sourceName, source_id as sourceId, source_type as sourceType, description as description, ip as ip, port as port, username as username, password as password, database as database, jdbc_parameter as jdbcParameter from data_source")
    public List<DataSource> getDataSource();

    @Select("select source_name from data_source ")
    public List<String> getDataSourceList();

    @Update({" <script>",
            " update data_source set source_type=#{dataSource.sourceType},description=#{dataSource.description},ip=#{dataSource.ip},port=#{dataSource.port},username=#{dataSource.userName},password=#{dataSource.password},database=#{dataSource.database},jdbc_parameter=#{dataSource.jdbcParameter} where source_name=#{dataSource.sourceName}",
            " </script>"})
    public int updateDataSource(@Param("dataSource")DataSource info);


}
