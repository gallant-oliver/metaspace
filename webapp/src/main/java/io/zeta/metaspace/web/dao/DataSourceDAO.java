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

import io.zeta.metaspace.model.datasource.DataSourceBody;
import io.zeta.metaspace.model.datasource.DataSourceConnection;
import io.zeta.metaspace.model.datasource.DataSourceHead;
import io.zeta.metaspace.model.datasource.DataSourceInfo;
import io.zeta.metaspace.model.datasource.DataSourcePrivileges;
import io.zeta.metaspace.model.datasource.DataSourceSearch;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.APIIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupAndPrivilege;
import io.zeta.metaspace.model.user.UserIdAndName;
import io.zeta.metaspace.model.usergroup.UserGroupIdAndName;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


import java.sql.Timestamp;
import java.util.List;



public interface DataSourceDAO {
    //添加数据源
    @Insert("insert into data_source(source_id,source_name,source_type,description,create_time,update_time,update_user_id,ip,port,username,password,database,jdbc_parameter,create_user_id,manager,oracle_db,isapi,servicetype,tenantid)" +
            "values(#{dataSourceBody.sourceId},#{dataSourceBody.sourceName},#{dataSourceBody.sourceType},#{dataSourceBody.description},#{dataSourceBody.updateTime},#{dataSourceBody.updateTime},#{updateUserId},#{dataSourceBody.ip},#{dataSourceBody.port},#{dataSourceBody.userName},#{dataSourceBody.password},#{dataSourceBody.database},#{dataSourceBody.jdbcParameter},#{updateUserId},#{dataSourceBody.manager},#{dataSourceBody.oracleDb},#{isapi},#{dataSourceBody.serviceType},#{tenantId})")
    public int add( @Param("updateUserId") String updateUserId, @Param("dataSourceBody") DataSourceBody dataSourceBody,@Param("isapi") boolean isapi,@Param("tenantId")String tenantId);

    //更新数据源
    @Update("<script>" +
            "update data_source set " +
            "source_name=#{dataSourceBody.sourceName}," +
            "description=#{dataSourceBody.description}," +
            "source_type=#{dataSourceBody.sourceType}," +
            "ip=#{dataSourceBody.ip}," +
            "port=#{dataSourceBody.port}," +
            "username=#{dataSourceBody.userName}," +
            "database=#{dataSourceBody.database}," +
            "<if test='dataSourceBody.password!=null'>" +
            "password=#{dataSourceBody.password}," +
            "</if>" +
            "jdbc_parameter=#{dataSourceBody.jdbcParameter}," +
            "update_user_id=#{updateUserId}," +
            "update_time=#{dataSourceBody.updateTime}, " +
            " oracle_db=#{dataSourceBody.oracleDb}," +
            " servicetype=#{dataSourceBody.serviceType} " +
            "where source_id=#{dataSourceBody.sourceId}" +
            "</script>")
    public int updateNoRely(@Param("updateUserId") String updateUserId,@Param("dataSourceBody") DataSourceBody dataSourceBody);

    //更新数据源
    @Update("update data_source set " +
            "source_name=#{dataSourceBody.sourceName}," +
            "description=#{dataSourceBody.description}," +
            "update_user_id=#{updateUserId}," +
            "update_time=#{dataSourceBody.updateTime} " +
            "where source_id=#{dataSourceBody.sourceId}")
    public int updateRely(@Param("updateUserId") String updateUserId,@Param("dataSourceBody") DataSourceBody dataSourceBody);

    //数据源id是否存在
    @Select("select count(1) from data_source where source_id=#{sourceId}")
    public int isSourceId(@Param("sourceId") String sourceId);

    //数据源名称是否存在
    @Select("select count(1) from data_source where source_name=#{sourceName} and source_id!=#{sourceId} and tenantid=#{tenantId}")
    public int isSourceName(@Param("sourceName") String sourceName,@Param("sourceId") String sourceId,@Param("tenantId")String tenantId);

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

    //删除Api授权人
    @Delete({"<script>",
             "delete from data_source_api_authorize where source_id in ",
             "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" ,
             "#{sourceId}",
             "</foreach>",
             "</script>"})
    public int deleteApiAuthorizeBySourceIds(@Param("sourceIds") List<String> sourceIds);

    //获取数据源详情
    @Select("select source_id,source_type sourceType,source_name sourceName,description,ip,port,username userName,password,database,jdbc_parameter jdbcParameter,oracle_db oracleDb,manager managerId,servicetype,create_time,update_time " +
            "from data_source where source_id=#{sourceId};")
    public DataSourceInfo getDataSourceInfo(@Param("sourceId") String sourceId);

    //搜索数据源
    @Select("<script>" +
            "select count(*)over() totalSize,ds.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,ds.description,ds.create_time createTime,ds.update_time updateTime,ds.update_user_id updateUserName,ds.manager as manager,ds.oracle_db oracleDb,serviceType " +
            " from data_source ds left join ( " +
            "  select distinct dgr.source_id from datasource_group_relation dgr join user_group_relation ug on ug.group_id=dgr.group_id " +
            "  where ug.user_id=#{userId} " +
            ") da on da.source_id=ds.source_id " +
            "where (da.source_id is not null or ds.manager=#{userId}) and (isapi=false or isapi is null) and ds.tenantid=#{tenantId} " +
            "<if test='dataSourceSearch.sourceName!=null'>" +
            "and ds.source_name like concat('%',#{dataSourceSearch.sourceName},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null'>" +
            "and ds.source_type like concat('%',#{dataSourceSearch.sourceType},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null'>" +
            "and to_char(ds.create_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.createTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null'>" +
            "and to_char(ds.update_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.updateTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateUserName!=null'>" +
            "and us.username like concat('%',#{dataSourceSearch.updateUserName},'%') ESCAPE '/'" +
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
    public List<DataSourceHead> searchDataSources(@Param("parameters") Parameters parameters,@Param("dataSourceSearch") DataSourceSearch dataSourceSearch,@Param("userId") String userId,@Param("tenantId")String tenantId);

    //搜索Api权限数据源
    @Select("<script>" +
            "select count(*)over() totalSize,ds.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,ds.description,ds.create_time createTime,ds.update_time updateTime,ds.update_user_id updateUserName,ds.manager as manager,ds.oracle_db oracleDb,serviceType " +
            " from data_source ds left join ( " +
            "  select distinct dgr.source_id from datasource_group_relation dgr join user_group_relation ug on ug.group_id=dgr.group_id " +
            "  where ug.user_id=#{userId} " +
            ") da on da.source_id=ds.source_id " +
            "where (da.source_id is not null or ds.manager=#{userId}) and isapi=true and ds.tenantid=#{tenantId} " +
            "<if test='dataSourceSearch.sourceName!=null'>" +
            "and ds.source_name like concat('%',#{dataSourceSearch.sourceName},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null'>" +
            "and ds.source_type like concat('%',#{dataSourceSearch.sourceType},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null'>" +
            "and to_char(ds.create_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.createTime},'%')  ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null'>" +
            "and to_char(ds.update_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.updateTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateUserName!=null'>" +
            "and us.username like concat('%',#{dataSourceSearch.updateUserName},'%') ESCAPE '/'" +
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
    public List<DataSourceHead> searchApiDataSources(@Param("parameters") Parameters parameters,@Param("dataSourceSearch") DataSourceSearch dataSourceSearch,@Param("userId") String userId,@Param("tenantId")String tenantId);

    //搜索数据源
    @Select("<script>" +
            "select count(*)over() totalSize,ds.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,ds.description,ds.create_time createTime,ds.update_time updateTime,us.username updateUserName,ds.manager as manager,ds.oracle_db oracleDb,serviceType " +
            "from data_source ds, users us " +
            "where ds.update_user_id=us.userid and (isapi=false or isapi is null) and tenantid=#{tenantId} " +
            "<if test='dataSourceSearch.sourceName!=null'>" +
            "and ds.source_name like concat('%',#{dataSourceSearch.sourceName},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null'>" +
            "and ds.source_type like concat('%',#{dataSourceSearch.sourceType},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null'>" +
            "and to_char(ds.create_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.createTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null'>" +
            "and to_char(ds.update_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.updateTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateUserName!=null'>" +
            "and us.username like concat('%',#{dataSourceSearch.updateUserName},'%') ESCAPE '/'" +
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
    public List<DataSourceHead> searchAllDataSources(@Param("parameters") Parameters parameters,@Param("dataSourceSearch") DataSourceSearch dataSourceSearch,@Param("tenantId")String tenantId);

    //搜索api数据源
    @Select("<script>" +
            "select count(*)over() totalSize,ds.source_id sourceId,ds.source_name sourceName,ds.source_type sourceType,ds.description,ds.create_time createTime,ds.update_time updateTime,us.username updateUserName,ds.manager as manager,ds.oracle_db oracleDb,serviceType " +
            "from data_source ds, users us " +
            "where ds.update_user_id=us.userid and ds.isapi=true and tenantid=#{tenantId} " +
            "<if test='dataSourceSearch.sourceName!=null'>" +
            "and ds.source_name like concat('%',#{dataSourceSearch.sourceName},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.sourceType!=null'>" +
            "and ds.source_type like concat('%',#{dataSourceSearch.sourceType},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.createTime!=null'>" +
            "and to_char(ds.create_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.createTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateTime!=null'>" +
            "and to_char(ds.update_time,'yyyy-MM-dd') like concat('%',#{dataSourceSearch.updateTime},'%') ESCAPE '/'" +
            "</if>" +
            "<if test='dataSourceSearch.updateUserName!=null'>" +
            "and us.username like concat('%',#{dataSourceSearch.updateUserName},'%') ESCAPE '/'" +
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
    public List<DataSourceHead> searchApiAllDataSources(@Param("parameters") Parameters parameters,@Param("dataSourceSearch") DataSourceSearch dataSourceSearch,@Param("tenantId")String tenantId);


    @Select("select count(*) from data_source where tenantid=#{tenantId} ")
    public int exportDataSource(@Param("tenantId") String tenantId);

    @Select("<script>" +
            "select source_name as sourceName, source_type as sourceType, description as description, ip as ip, port as port, username as username, password as password, database as database, jdbc_parameter as jdbcParameter from data_source" +
            " where source_id in " +
            "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" +
            "#{sourceId}" +
            "</foreach>" +
            "</script>")
    public List<DataSourceBody> getDataSource(@Param("sourceIds") List<String> sourceIds);


    @Select("select source_name from data_source where tenantid=#{tenantId}")
    public List<String> getDataSourceList(@Param("tenantId") String tenantId);

    @Update({" <script>",
            " update data_source set source_type=#{dataSource.sourceType},description=#{dataSource.description},ip=#{dataSource.ip},port=#{dataSource.port},username=#{dataSource.userName},password=#{dataSource.password},database=#{dataSource.database},jdbc_parameter=#{dataSource.jdbcParameter},update_time=#{dataSource.updateTime},update_user_id=#{userId} where source_name=#{dataSource.sourceName} and tenantid=#{tenantId}",
            " </script>"})
    public int updateDataSource(@Param("userId")String userId,@Param("dataSource")DataSourceBody dataSource,@Param("tenantId")String tenantId);


    //获取测试连接参数
    @Select("select source_type sourceType,ip,port,username userName,password,database,jdbc_parameter jdbcParameter,servicetype " +
            "from data_source where source_id=#{sourceId};")
    public DataSourceConnection getConnectionBySourceId(@Param("sourceId") String sourceId);

    //判断是否是管理者
    @Select("select count(1) from data_source where source_id=#{sourceId} and manager=#{userId}")
    public int isManagerUser(@Param("sourceId") String sourceId,@Param("userId") String userId);



    //获取数据源已授权人
    @Select("select count(*)over() totalSize,users.userid,users.userName,users.account from users join data_source_authorize on data_source_authorize.authorize_user_id=users.userid " +
            "where source_id=#{sourceId} and users.userid!=#{userId} and users.valid=true")
    public List<UserIdAndName> getAuthorizeUser(@Param("sourceId") String sourceId,@Param("userId") String userId);

    //判断用户是否是数据源已授权人
    @Select("select count(1) from data_source_authorize " +
            "where source_id=#{sourceId} and authorize_user_id=#{userId}")
    public int isAuthorizeUser(@Param("sourceId") String sourceId,@Param("userId") String userId);

    //判断用户是否是数据源已授权人
    @Select("select count(1) from data_source_api_authorize " +
            "where source_id=#{sourceId} and authorize_user_id=#{userId}")
    public int isApiAuthorizeUser(@Param("sourceId") String sourceId,@Param("userId") String userId);

    //获取数据源未授权人
    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username userName,u.account from " +
            " (select distinct user2role.userid userid from privilege2module p join role r on p.privilegeid=r.privilegeid join user2role on r.roleid=user2role.roleid where p.moduleid='14' and r.status=1 ) us " +
            " join users u on u.userid=us.userid " +
            " where u.valid=true " +
            "and u.userid not in (select authorize_user_id from data_source_authorize where source_id=#{sourceId} union select userid from user2role where roleid='1' or roleid='3') " +
            "<if test='query!=null'>" +
            "and username like concat('%',#{query},'%') ESCAPE '/'" +
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
    public int addAuthorizes(@Param("sourceId") String sourceId,@Param("authorizeUserIds") List<String> authorizeUserIds);

    //获取数据源已授权人
    @Select("select count(*)over() totalSize,users.userid,users.userName,users.account from users join data_source_api_authorize on data_source_api_authorize.authorize_user_id=users.userid " +
            "where source_id=#{sourceId} and users.userid!=#{userId} and users.valid=true")
    public List<UserIdAndName> getApiAuthorizeUser(@Param("sourceId") String sourceId,@Param("userId") String userId);


    @Select("<script>" +
            "select count(*)over() totalSize,u.userid,u.username userName,u.account from " +
            " (select distinct user2role.userid userid from privilege2module p join role r on p.privilegeid=r.privilegeid join user2role on r.roleid=user2role.roleid where p.moduleid='14' and r.status=1 ) us " +
            " join users u on u.userid=us.userid " +
            "where u.valid=true " +
            "and u.userid not in (select authorize_user_id from data_source_api_authorize where source_id=#{sourceId} union select userid from user2role where roleid='1' or roleid='3') " +
            "<if test='query!=null'>" +
            "and username like concat('%',#{query},'%') ESCAPE '/'" +
            "</if>" +
            "</script>")
    public List<UserIdAndName> getApiNoAuthorizeUser(@Param("sourceId") String sourceId,@Param("query") String query);

    //新增授权人
    @Insert("insert into data_source_api_authorize(source_id,authorize_user_id)" +
            "values(#{sourceId},#{authorizeUserId})")
    public int addApiAuthorize(@Param("sourceId") String updateUserId,@Param("authorizeUserId") String authorizeUserId);

    //新增授权人
    @Insert({"<script>",
             "insert into data_source_api_authorize(source_id,authorize_user_id)" ,
             "values",
             "<foreach collection='authorizeUserIds' item='userId' index='index' separator='),(' open='(' close=')'>" ,
             "#{sourceId},#{userId}",
             "</foreach>",
             "</script>"})
    public int addApiAuthorizes(@Param("sourceId") String sourceId,@Param("authorizeUserIds") List<String> authorizeUserIds);

    //根据数据源删除授权
    @Delete({"delete from data_source_api_authorize where source_id=#{sourceId}"})
    public int deleteApiAuthorizeBySourceId(@Param("sourceId") String sourceId);



    //删除授权人
    @Delete({"<script>",
             "delete from data_source_authorize where source_id=#{sourceId} and authorize_user_id in ",
             "<foreach collection='noAuthorizeUserIds' item='userId' index='index' separator=',' open='(' close=')'>" ,
             "#{userId}",
             "</foreach>",
             "</script>"})
    public int deleteAuthorize(@Param("sourceId") String sourceId,@Param("noAuthorizeUserIds") List<String> noAuthorizeUserIds);

    //删除授权人
    @Delete({"<script>",
             "delete from data_source_api_authorize where source_id=#{sourceId} and authorize_user_id in ",
             "<foreach collection='noAuthorizeUserIds' item='userId' index='index' separator=',' open='(' close=')'>" ,
             "#{userId}",
             "</foreach>",
             "</script>"})
    public int deleteApiAuthorize(@Param("sourceId") String sourceId,@Param("noAuthorizeUserIds") List<String> noAuthorizeUserIds);

    //获取数据源已授权人id
    @Select("select authorize_user_id from data_source_authorize " +
            "where source_id=#{sourceId}")
    public List<String> getAuthorizeUserIds(@Param("sourceId") String sourceId);

    //根据数据源名字获取数据源id
    @Select("select source_id from data_source where source_name=#{sourceName}")
    public String getSourceIdBySourceName(@Param("sourceName") String sourceName,@Param("tenantId")String tenantId);

    @Select("select username from users where userid in " +
            "(select distinct s.update_user_id from data_source s " +
            "join data_source_authorize a on s.source_id=a.source_id " +
            "where a.authorize_user_id = #{userId} and s.tenantid=#{tenantId})")
    public List<String> getUpdateUserName(@Param("userId") String userId,@Param("tenantId")String tenantId);

    @Select("select username from users where userid in " +
            "(select distinct s.update_user_id from data_source s " +
            "join data_source_api_authorize a on s.source_id=a.source_id " +
            "where a.authorize_user_id = #{userId} and s.tenantid=#{tenantId})")
    public List<String> getApiUpdateUserName(@Param("userId") String userId,@Param("tenantId")String tenantId);

    @Select("select username from users where userid in " +
            "(select distinct s.update_user_id from data_source s where s.isapi=false or s.isapi is null) and s.tenantid=#{tenantId}")
    public List<String> getAllUpdateUserName(@Param("tenantId")String tenantId);

    @Select("select username from users where userid in " +
            "(select distinct s.update_user_id from data_source s where s.isapi=true) and s.tenantid=#{tenantId}")
    public List<String> getApiAllUpdateUserName(@Param("tenantId")String tenantId);

    //更新数据源
    @Update("update data_source set " +
            "manager=#{managerUserId}," +
            "update_user_id=#{userId}," +
            "update_time=#{updateTime}" +
            "where source_id=#{sourceId}")
    public int updateManager(@Param("userId") String userId,@Param("managerUserId") String managerUserId,@Param("sourceId") String sourceId,@Param("updateTime") Timestamp updateTime);

    //获取可以成为数据源管理者的用户
    @Select("select count(*)over() totalSize,u.userid,u.username userName,u.account " +
            "from (select distinct user2role.userid from privilege2module p join role r on p.privilegeid=r.privilegeid join user2role on r.roleid=user2role.roleid where p.moduleid='14' and r.status=1 ) ro join users u on u.userid=ro.userid " +
            "where u.valid=true ")
    public List<UserIdAndName> getManager();

    //查询数据源管理者
    @Select("select manager from data_source where source_id=#{sourceId}")
    public String getManagerBySourceId(@Param("sourceId") String sourceId);

    //查询数据源是否是api数据源
    @Select("select isapi from data_source where source_id=#{sourceId}")
    public String getIsApi(@Param("sourceId") String sourceId);

    //查询api数据源是否依赖
    @Select("<script>" +
            "select guid,name from apiinfo where sourceId in " +
            "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" +
            "#{sourceId}"+
            "</foreach>"+
            "</script>")
    public List<APIIdAndName> getAPIRely(@Param("sourceIds") List<String> sourceIds);

    @Select("select p.privilege_code from datasource_group_relation p join user_group_relation u on p.group_id=u.group_id " +
            "where p.source_id=#{sourceId} and u.user_id=#{userId}")
    public List<String> getUserPrivilegesDataSource(@Param("userId")String userId,@Param("sourceId") String sourceId);

    @Select("<script> " +
            "select count(*)over() totalSize,u.id,u.name from user_group u left join " +
            "( select group_id from datasource_group_relation where source_id=#{sourceId} ) p on u.id=p.group_id where p.group_id is null and u.tenant=#{tenantId} and u.valid=true " +
            "<if test='parameters.query!=null'>" +
            " and u.name like concat('%',#{parameters.query},'%') ESCAPE '/'  " +
            "</if>" +
            "<if test='parameters.limit!=-1'>" +
            " limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            " offset ${parameters.offset} " +
            "</if> " +
            " </script>")
    public List<UserGroupIdAndName> getNoUserGroupByDataSource(@Param("tenantId")String tenantId, @Param("sourceId")String sourceId, @Param("parameters") Parameters parameters);

    @Select("<script> " +
            "select count(*)over() totalSize,u.id,u.name,u.description,p.privilege_code privilegeCode from user_group u join " +
            " datasource_group_relation p on u.id=p.group_id where p.source_id=#{sourceId} and u.tenant=#{tenantId} " +
            "<if test='parameters.query!=null'>" +
            " and u.name like concat('%',#{parameters.query},'%') ESCAPE '/'  " +
            "</if>" +
            "<if test='parameters.limit!=-1'>" +
            " limit ${parameters.limit} " +
            "</if>" +
            "<if test='parameters.offset!=0'>" +
            " offset ${parameters.offset} " +
            "</if> " +
            " </script>")
    public List<UserGroupAndPrivilege> getUserGroupByDataSource(@Param("tenantId")String tenantId, @Param("sourceId")String sourceId, @Param("parameters") Parameters parameters);

    @Delete({"<script> " +
             "delete from datasource_group_relation where source_id=#{sourceId} and group_id in ",
             "<foreach collection='userGroups' item='userGroup' index='index' separator=',' open='(' close=')'>" ,
             "#{userGroup}",
             "</foreach>",
             " </script>"})
    public Integer deleteUserGroupsByDataSource(@Param("sourceId")String sourceId,@Param("userGroups") List<String> userGroups);

    @Update({"<script> " +
             "update datasource_group_relation set " ,
             "privilege_code=#{privileges.privilegeCode} " ,
             "where source_id=#{sourceId} and group_id in ",
             "<foreach collection='privileges.userGroups' item='userGroup' index='index' separator=',' open='(' close=')'>" ,
             "#{userGroup}",
             "</foreach>",
             " </script>"})
    public Integer updateUserGroupsByDataSource(@Param("sourceId")String sourceId,@Param("privileges") DataSourcePrivileges privileges);

    @Select({"<script>select count(*) from datasource_group_relation where source_id=#{sourceId} and group_id in ",
             "<foreach collection='userGroups' item='userGroup' index='index' separator=',' open='(' close=')'>" ,
             "#{userGroup}",
             "</foreach>",
             "</script>"})
    public int isUserGroup(@Param("sourceId") String sourceId,@Param("userGroups")List<String> userGroup);

    @Insert({"<script>insert into datasource_group_relation(source_id,group_id,privilege_code) values ",
             "<foreach item='item' index='index' collection='groupIds'",
             "open='(' separator='),(' close=')'>",
             "#{sourceId},#{item},#{privilege}",
             "</foreach>",
             "</script>"})
    public Integer addUserGrooup2DataSource(@Param("sourceId")String sourceId,@Param("groupIds") List<String> groupIds,@Param("privilege") String privilege);

    //删除授权
    @Delete({"<script>",
             "delete from datasource_group_relation where source_id in ",
             "<foreach collection='sourceIds' item='sourceId' index='index' separator=',' open='(' close=')'>" ,
             "#{sourceId}",
             "</foreach>",
             "</script>"})
    public int deleteRelationBySourceId(@Param("sourceIds") List<String> sourceIds);

    @Select({" <script> ",
            " select d.* from data_source d join datasource_group_relation dgr on d.source_id=dgr.source_id where d.tenantid=#{tenantId} and dgr.group_id in  ",
            " <foreach item='groupId' index='index' collection='groupIds' separator=',' open='(' close=')'>",
            " #{groupId} ",
            " </foreach>",
            " order by d.source_name asc ",
            " </script>"})
    List<DataSourceBody> getDataSourcesByGroups(@Param("groupIds") List<String> groupIds,@Param("tenantId") String tenantId);
}
