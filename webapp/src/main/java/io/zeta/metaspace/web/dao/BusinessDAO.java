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
 * @date 2019/2/12 14:57
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import io.zeta.metaspace.model.business.BusinessRelationEntity;
import io.zeta.metaspace.model.business.TechnologyInfo;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:57
 */
public interface BusinessDAO {

    //添加业务信息
    @Insert("insert into businessinfo(departmentid,businessid,name,module,description,owner,manager,maintainer,dataassets,submitter,submissionTime,businessOperator,businessLastUpdate,ticketNumber,level2CategoryId,tenantid)" +
            "values(#{info.departmentId},#{info.businessId},#{info.name},#{info.module},#{info.description},#{info.owner},#{info.manager},#{info.maintainer},#{info.dataAssets},#{info.submitter},#{info.submissionTime},#{info.businessOperator},#{info.businessLastUpdate},#{info.ticketNumber},#{info.level2CategoryId},#{tenantId})")
    public int insertBusinessInfo(@Param("info") BusinessInfo info,@Param("tenantId")String tenantId);

    @Select("select count(1) from businessInfo where name=#{name} and tenantid=#{tenantId}")
    public int sameNameCount(@Param("name")String businessName,@Param("tenantId")String tenantId);

    //更新业务信息
    @Update("update businessinfo set name=#{name},module=#{module},description=#{description},owner=#{owner},manager=#{manager}," +
            "maintainer=#{maintainer},dataAssets=#{dataAssets},businessOperator=#{businessOperator},businessLastUpdate=#{businessLastUpdate} where businessId=#{businessId}")
    public int updateBusinessInfo(BusinessInfo info);

    //更新业务信息补充状态
    @Update("update businessinfo set businessstatus=#{status} where businessId=#{businessId}")
    public int updateBusinessStatus(@Param("businessId")String businessId, @Param("status")int status);

    //更新技术信息补充状态
    @Update("update businessinfo set technicalstatus=#{status} where businessId=#{businessId}")
    public int updateTechnicalStatus(@Param("businessId")String businessId, @Param("status")int status);

    //查询业务目录关联的业务信息列表
    @Select("select * from businessinfo where departmentId=#{departmentId}")
    public List<BusinessInfo> queryBusinessByDemparmentId(@Param("departmentId")String departmentId,  @Param("limit")int limit,@Param("offset") int offset);

    //查询业务信息详情
    @Select("select * from businessinfo where businessId=#{businessId}")
    public BusinessInfo queryBusinessByBusinessId(@Param("businessId")String businessId);

    @Select("select technicalLastUpdate,technicalOperator from businessinfo where businessId=#{businessId}")
    public TechnologyInfo queryTechnologyInfoByBusinessId(@Param("businessId")String businessId);

    //查询业务信息关联的数据库表
    @Select("select tableGuid,tableName,dbName,status,createTime,databaseGuid,display_name as displayName,description from tableInfo where tableGuid in(select tableGuid from business2table where status='ACTIVE' and businessId=#{businessId})")
    public List<TechnologyInfo.Table> queryTablesByBusinessId(@Param("businessId")String businessId);

    //查询业务信息关联的数据库表-过滤数据源
    @Select("select tableGuid,tableName,dbName,status,createTime,databaseGuid,display_name as displayName,description,source_id AS sourceId from tableInfo where tableGuid in(select tableGuid from business2table where status='ACTIVE' and businessId=#{businessId}) AND (tableInfo.source_id in (SELECT source_id FROM data_source WHERE tenantid = #{tenantId}) or tableInfo.source_id = 'hive')")
    public List<TechnologyInfo.Table> queryTablesByBusinessIdAndTenantId(@Param("businessId") String businessId, @Param("tenantId") String tenantId);

    @Select({"<script>",
            " SELECT COUNT(*), businessId FROM tableInfo INNER JOIN business2table ON tableInfo.tableGuid = business2table.tableguid WHERE tableInfo.status = 'ACTIVE' AND businessId IN",
            " <foreach item='item' index='index' collection='businessIdS' separator=',' open='(' close=')'>" ,
            " #{item}",
            " </foreach>",
            " AND ( tableInfo.source_id IN ( SELECT source_id FROM data_source WHERE tenantid = #{tenantId} ) OR tableInfo.source_id = 'hive' ) ",
            " GROUP BY businessId",
            " </script>"})
    List<TechnologyInfo> getCountByBusinessIdAndTenantId(@Param("businessIdS") List<String> businessIdS, @Param("tenantId") String tenantId);

    //添加目录/业务对象关联
    @Insert("insert into business_relation(relationshipGuid,categoryGuid,businessId,generateTime)values(#{relationshipGuid},#{categoryGuid},#{businessId},#{generateTime})")
    public int addRelation(BusinessRelationEntity entity) throws SQLException;

    @Select("select trustTable from businessInfo where businessId=#{businessId}")
    public String getTrustTableGuid(@Param("businessId")String businessId);


    //根据业务信息名称查询列表(有权限)
    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,businessInfo.name,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber, business_relation.categoryGuid from businessInfo",
             " join business_relation on",
             " business_relation.businessId=businessInfo.businessId",
             " where",
             " businessInfo.name like '%${businessName}%' ESCAPE '/'",
             " and businessInfo.tenantid=#{tenantId} and ",
             " categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " order by businessInfo.businessLastUpdate desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    //@Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber from businessInfo where businessId in (select businessId from business_relation where categoryGuid=#{categoryGuid}) and name like '%${businessName}%' limit #{limit} offset #{offset}")
    public List<BusinessInfoHeader> queryBusinessByName(@Param("businessName")String businessName, @Param("ids") List<String> categoryIds, @Param("limit")int limit, @Param("offset") int offset,@Param("tenantId")String tenantId) throws SQLException;


    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,businessInfo.name,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber,business_relation.categoryGuid from businessInfo",
             " join business_relation on",
             " business_relation.businessId=businessInfo.businessId",
             " where",
             " businessInfo.name like '%${businessName}%' ESCAPE '/' and businessInfo.tenantid=#{tenantId} ",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<BusinessInfoHeader> queryBusinessByNameWithoutPrivilege(@Param("businessName")String businessName, @Param("limit")int limit, @Param("offset") int offset,@Param("tenantId")String tenantId);


    //根据业务信息名称查询列表总数
    @Select({"<script>",
             " select count(*) from businessInfo",
             " where",
             " businessInfo.name like '%${businessName}%' ESCAPE '/'",
             " </script>"})
    public long queryBusinessCountByNameWithoutPrivilege(@Param("businessName")String businessName);

    //查询业务信息所属目录Id
    @Select("select departmentId from businessInfo where businessId = #{businessId}")
    public String queryCategoryIdByBusinessId(@Param("businessId")String businessId);


    //多条件查询业务信息列表
    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber,categoryGuid from businessInfo",
             " join business_relation on businessInfo.businessId = business_relation.businessId join users on users.userid=businessInfo.submitter",
             " where businessInfo.tenantid=#{tenantId} and categoryGuid in(",
             " select guid from category where guid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " and categoryType=1 and tenantid=#{tenantId})",
             " <if test=\"level2CategoryId != null and level2CategoryId!=''\">",
             " and level2CategoryId=#{level2CategoryId}",
             " </if>",
             " and technicalStatus=#{status} and name like '%${businessName}%' ESCAPE '/' and ticketNumber like '%${ticketNumber}%' ESCAPE '/' and " +
             " users.username like '%${submitter}%' ESCAPE '/' " +
             " order by businessInfo.businessLastUpdate desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<BusinessInfoHeader> queryBusinessByCondition(@Param("ids") List<String> categoryIds, @Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                                       @Param("level2CategoryId") String level2CategoryId,@Param("submitter") String submitter,@Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId);



    //查询业务目录关系业务信息列表
    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,businessInfo.name,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber, business_relation.categoryGuid from businessInfo",
             " join business_relation",
             " on",
             " businessInfo.businessId = business_relation.businessId",
             " where businessInfo.tenantid=#{tenantId} and ",
             " business_relation.categoryGuid=#{categoryGuid} order by technicalStatus,businessInfo.businessLastUpdate desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<BusinessInfoHeader> queryBusinessByCatetoryId(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId);


    //更新技术信息操作者及更新时间
    @Update("update businessInfo set technicalOperator=#{technicalOperator},technicalLastUpdate=#{technicalLastUpdate} where businessId=#{businessId}")
    public int updateTechnicalInfo(@Param("businessId")String businessId, @Param("technicalOperator")String technicalOperator, @Param("technicalLastUpdate")String technicalLastUpdate);

    //删除业务信息与表的关联
    @Delete("delete from business2table where businessId=#{businessId}")
    public int deleteRelationByBusinessId(@Param("businessId")String businessId);

    @Update("UPDATE businessinfo SET trusttable=null where businessId=#{businessId}")
    public int updateTrustTable(@Param("businessId")String businessId);

    //添加业务信息与表的关联
    @Insert({" <script>",
            " insert into business2table(businessId, tableGuid)values",
            " <foreach collection='list' item='tableGuid' index='index'  separator=','>",
             " (#{businessId},#{tableGuid})",
             " </foreach>",
            " </script>"})
    public int insertTableRelation(@Param("businessId")String businessId, @Param("list")List<String> list);

    @Delete("delete from businessInfo where businessId=#{businessId}")
    public int deleteBusinessById(@Param("businessId")String businessId);

    @Update("update businessInfo set trustTable=#{trustTable} where businessId=#{businessId}")
    public int setBusinessTrustTable(@Param("businessId")String businessId, @Param("trustTable")String trustTable);

    @Delete("delete from business_relation where businessId=#{businessId}")
    public int deleteRelationById(@Param("businessId")String businessId);

    @Select("select businessId from businessInfo where (trustTable is null or trustTable='') and tenantid=#{tenantId}")
    public List<String> getNonTrustBusiness(@Param("tenantId")String tenantId);

    @Delete("delete from business2Table where tableGuid=#{tableGuid}")
    public int deleteBusinessRelationByTableGuid(@Param("tableGuid")String tableGuid);

    @Update("update business2Table set tableGuid = #{tableGuid} where tableGuid=#{oldTableGuid}")
    public int UpdateBusinessRelationByTableGuid(@Param("tableGuid")String tableGuid, @Param("oldTableGuid")String oldTableGuid);



    @Select({"<script>",
             " SELECT DISTINCT tableInfo.tableGuid as tableId,tableInfo.tableName,tableInfo.databaseGuid as databaseId,dbName as databaseName, tableInfo.createTime,tableInfo.status,",
             " tableInfo.display_name as displayName, tableInfo.display_updateTime as displayUpdateTime, tableInfo.display_operator as displayOperator",
             " from tableInfo,business2table ",
             " WHERE business2table.businessid=#{businessId}",
             " and tableInfo.tableGuid=business2table.tableGuid",
             " and (tableInfo.tableName like '%${tableName}%' ESCAPE '/' or tableInfo.display_name like '%${tableName}%' ESCAPE '/')",
             " order by tableInfo.status",
             " <if test='limit != null and limit!=-1'>",
             " limit #{limit}",
             " </if>",
             " <if test='offset != null'>",
             " offset #{offset}",
             " </if>",
             " </script>"})
    public List<TableHeader> getBusinessRelatedTableList(@Param("businessId")String businessList, @Param("tableName")String tableName, @Param("limit")Integer limit,@Param("offset") Integer offset);

    @Select({"<script>",
             " SELECT count(DISTINCT tableInfo.tableGuid)",
             " from tableInfo,business2table ",
             " WHERE business2table.businessid=#{businessId}",
             " and tableInfo.tableGuid=business2table.tableGuid",
             " and (tableInfo.tableName like '%${tableName}%' ESCAPE '/' or tableInfo.display_name like '%${tableName}%' ESCAPE '/')",
             " </script>"})
    public long getCountBusinessRelatedTable(@Param("businessId")String businessList, @Param("tableName")String tableName);

    @Update("update businessInfo set trustTable=null where trustTable=#{tableId}")
    public int removeBusinessTrustTableByTableId(@Param("tableId")String tableId);

    @Update("update businessInfo set trustTable=#{tableId} where trustTable=#{OldTableId}")
    int updateBusinessTrustTableByTableId(@Param("tableId")String tableId, @Param("OldTableId")String OldTableId);

    @Select("<script>" +
            "select businessInfo.name,businessInfo.module,businessInfo.description,businessInfo.owner,businessInfo.manager,businessInfo.maintainer,businessInfo.dataassets,businessInfo.businesslastupdate,businessInfo.businessoperator " +
            " from businessInfo join business_relation " +
            " on businessInfo.businessId = business_relation.businessId " +
            " where businessInfo.tenantid=#{tenantId} and " +
            " business_relation.categoryGuid=#{categoryId} " +
            " </script>")
    public List<BusinessInfo> getAllBusinessByCategory(@Param("categoryId")String categoryId,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select businessInfo.name,businessInfo.module,businessInfo.description,businessInfo.owner,businessInfo.manager,businessInfo.maintainer,businessInfo.dataassets,businessInfo.businesslastupdate,businessInfo.businessoperator " +
            " from businessInfo join business_relation " +
            " on businessInfo.businessId = business_relation.businessId " +
            " where businessInfo.tenantid=#{tenantId} and " +
            " business_relation.categoryGuid=#{categoryGuid} and " +
            " businessInfo.businessid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            " </script>")
    public List<BusinessInfo> getBusinessByIds(@Param("ids")List<String> ids,@Param("categoryGuid")String categoryGuid,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select name from businessInfo " +
            " where businessInfo.tenantid=#{tenantId} " +
            " </script>")
    public List<String> getBusinessNames(@Param("tenantId")String tenantId);

    //批量添加业务信息
    @Insert("<script>" +
            "insert into businessinfo(departmentid,businessid,name,module,description,owner,manager,maintainer,dataassets,submitter,submissionTime,businessOperator,businessLastUpdate,ticketNumber,level2CategoryId,tenantid,businessstatus,technicalstatus) " +
            " values " +
            " <foreach item='info' index='index' collection='infos' separator='),(' open='(' close=')'>" +
            "#{info.departmentId},#{info.businessId},#{info.name},#{info.module},#{info.description},#{info.owner},#{info.manager},#{info.maintainer},#{info.dataAssets},#{info.submitter},#{info.submissionTime},#{info.businessOperator},#{info.businessLastUpdate},#{info.ticketNumber},#{info.level2CategoryId},#{tenantId},1,0 " +
            " </foreach>" +
            " </script>")
    public int insertBusinessInfos(@Param("infos") List<BusinessInfo> infos,@Param("tenantId")String tenantId);

    //批量添加目录/业务对象关联
    @Insert("<script>" +
            " insert into business_relation(relationshipGuid,categoryGuid,businessId,generateTime) values " +
            " <foreach item='entity' index='index' collection='entitys' separator='),(' open='(' close=')'>" +
            " #{entity.relationshipGuid},#{entity.categoryGuid},#{entity.businessId},#{entity.generateTime} " +
            " </foreach>" +
            " </script>")
    public int addRelations(@Param("entitys")List<BusinessRelationEntity> entitys);

    @Delete("<script>" +
            "delete from businessInfo where businessId in " +
            " <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int deleteBusinessesByIds(@Param("businessIds")List<String> businessIds);

    //删除业务信息与表的关联
    @Delete("<script>" +
            "delete from business2table where businessId in " +
            " <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int deleteRelationByBusinessIds(@Param("businessIds")List<String> businessIds);

    @Delete("<script>" +
            "delete from business_relation where businessId in " +
            " <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int deleteRelationByIds(@Param("businessIds")List<String> businessIds);

    @Select("<script>" +
            "select name from businessInfo " +
            " where tenantid=#{tenantId} and businessId in " +
            " <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public List<String> getBusinessNamesByIds(@Param("businessIds")List<String> businessIds,@Param("tenantId")String tenantId);

    //更新业务信息
    @Update("<script>" +
            "update businessinfo set departmentid=#{newCategoryId} where businessId in " +
            " <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            " #{id} " +
            " </foreach>" +
            " </script>")
    public int updateBusinessInfoCategory(@Param("businessIds") List<String> ids,@Param("newCategoryId")String newCategoryId);

    @Update("<script>" +
            "update business_relation set categoryGuid=#{newCategoryId} where businessid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    public int updateBusinessRelation(@Param("ids") List<String> businessIds,@Param("newCategoryId")String newCategoryId);

    @Select("<script>" +
            " select count(*)over() total,* from (" +
            "select distinct t.tableguid tableId,t.tablename,t.status,t.databaseguid databaseId,t.dbname databaseName,t.databasestatus from business2table b " +
            " join tableinfo t on b.tableGuid=t.tableguid where " +
            " (t.description is null or t.description='') and " +
            " b.businessid in " +
            " <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach> " +
            ") ta " +
            " <if test='limit!= -1'>" +
            " limit #{limit}" +
            " </if>" +
            " offset #{offset} " +
            "</script>")
    public List<Table> getTablesByBusiness(@Param("businessIds")List<String> businessIds, @Param("limit")int limit, @Param("offset") int offset);

    @Select("<script>" +
            "select count(*)over() total,tableguid tableId,tablename,status,databaseguid databaseId,dbname databaseName,databasestatus from tableinfo where status='ACTIVE' and tableguid " +
            " in (select c.table_guid " +
            "   from column_info c join ( " +
            "     select tableguid from business2table where businessid in " +
            "     <foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            "     #{id} " +
            "     </foreach> " +
            "   ) b on b.tableguid=c.table_guid where (c.description is null or c.description='') " +
            "   group by table_guid " +
            " ) " +
            " <if test='limit!= -1'>" +
            " limit #{limit} " +
            " </if>" +
            " offset #{offset} " +
            "</script>")
    public List<Table> getTablesByBusinessAndColumn(@Param("businessIds")List<String> businessIds, @Param("limit")int limit, @Param("offset") int offset);
}
