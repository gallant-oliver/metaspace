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

import io.zeta.metaspace.model.business.*;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.metadata.TableHeader;
import org.apache.atlas.model.metadata.RelationEntityV2;
import org.apache.ibatis.annotations.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:57
 */
public interface BusinessDAO {

    //添加业务信息
    @Insert("insert into businessinfo(departmentid,businessid,name,module,description,owner,manager,maintainer,dataassets,submitter,submissionTime,businessOperator,businessLastUpdate,ticketNumber,level2CategoryId,tenantid," +
            "private_status,status,publish,publish_desc,approve_group_id,submitter_read,create_mode) " +
            "values(#{info.departmentId},#{info.businessId},#{info.name},#{info.module},#{info.description},#{info.owner},#{info.manager},#{info.maintainer},#{info.dataAssets},#{info.submitter},#{info.submissionTime},#{info.businessOperator},#{info.businessLastUpdate},#{info.ticketNumber},#{info.level2CategoryId},#{tenantId}," +
            "#{info.privateStatus},#{info.status},#{info.publish},#{info.publishDesc},#{info.approveGroupId},#{info.submitterRead},#{info.createMode})")
    public int insertBusinessInfo(@Param("info") BusinessInfo info,@Param("tenantId")String tenantId);

    @Select("select count(1) from businessInfo where name=#{name} and tenantid=#{tenantId}")
    public int sameNameCount(@Param("name")String businessName,@Param("tenantId")String tenantId);

    //更新业务信息
    @Update("update businessinfo set name=#{name},module=#{module},description=#{description},owner=#{owner},manager=#{manager}," +
            "maintainer=#{maintainer},dataAssets=#{dataAssets},businessOperator=#{businessOperator},businessLastUpdate=#{businessLastUpdate}," +
            "publish=#{publish},publish_desc=#{publishDesc},approve_group_id=#{approveGroupId},status=#{status} " +
            "where businessId=#{businessId}")
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
    @Select("select ti.tableguid tableGuid, ti.tablename tableName, ti.dbname dbName, ti.status, ti.createtime createTime, ti.databaseguid databaseGuid, " +
            "COALESCE(ti.display_name, ti.tablename, '') as displayName, ti.description, ti.source_id sourceId, b2t.relation_type relationType " +
            "from business2table b2t " +
            "join tableInfo ti on ti.tableguid=b2t.tableguid and status='ACTIVE' " +
            "where b2t.businessid=#{businessId}) "
    )
    public List<TechnologyInfo.Table> queryTablesByBusinessId(@Param("businessId")String businessId);

    //查询业务信息关联的数据库表-过滤数据源
    @Select("select tableGuid,tableName,dbName,status,createTime,databaseGuid," +
            "COALESCE(display_name,tableName,'') as displayName,description,source_id AS sourceId from tableInfo where status='ACTIVE' and  tableGuid in(select tableGuid from business2table where businessId=#{businessId}) "
            //"AND (tableInfo.source_id in (SELECT source_id FROM data_source WHERE tenantid = #{tenantId}) or tableInfo.source_id = 'hive')"
    )
    public List<TechnologyInfo.Table> queryTablesByBusinessIdAndTenantId(@Param("businessId") String businessId, @Param("tenantId") String tenantId);

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
             " businessInfo.name like concat('%',#{businessName},'%') ESCAPE '/'",
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
    public List<BusinessInfoHeader> queryBusinessByName(@Param("businessName")String businessName, @Param("ids") List<String> categoryIds, @Param("limit")int limit, @Param("offset") int offset,@Param("tenantId")String tenantId) throws SQLException;

    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,businessInfo.name,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber,business_relation.categoryGuid from businessInfo",
             " join business_relation on",
             " business_relation.businessId=businessInfo.businessId",
             " where",
             " businessInfo.name like concat('%',#{businessName},'%') ESCAPE '/' and businessInfo.tenantid=#{tenantId} ",
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
             " businessInfo.name like concat('%',#{businessName},'%') ESCAPE '/'",
             " </script>"})
    public long queryBusinessCountByNameWithoutPrivilege(@Param("businessName")String businessName);

    //查询业务信息所属目录Id
    @Select("select departmentId from businessInfo where businessId = #{businessId}")
    public String queryCategoryIdByBusinessId(@Param("businessId")String businessId);


    //多条件查询业务信息列表
    @Results({
            @Result(property = "tables",javaType = List.class,column = "{businessId = businessIdVal,tenantId = tenantId}",many = @Many(select = "queryTablesByBusinessIdAndTenantId"))
    })
    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,businessInfo.businessId as businessIdVal,tenantId,businessInfo.departmentId as departmentId,name,businessStatus,technicalStatus,submissionTime,u.username as submitter,ticketNumber,categoryGuid from businessInfo",
             " join business_relation on businessInfo.businessId = business_relation.businessId join users u on u.userid=businessInfo.submitter",
             " where businessInfo.tenantid=#{tenantId} and categoryGuid in(",
             " select guid from category where guid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " and categoryType=1 and tenantid=#{tenantId})",
             " <if test=\"level2CategoryId != null and level2CategoryId!=''\">",
             " and level2CategoryId=#{level2CategoryId}",
             " </if>",
             " and technicalStatus=#{status} and name like concat('%',#{businessName},'%') ESCAPE '/' and ticketNumber like concat('%',#{ticketNumber},'%') ESCAPE '/' and " +
             " u.username like concat('%',#{submitter},'%') ESCAPE '/' " +
             " order by businessInfo.businessLastUpdate desc",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<BusinessInfoHeader> queryBusinessByCondition(@Param("ids") List<String> categoryIds, @Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                                       @Param("level2CategoryId") String level2CategoryId,@Param("submitter") String submitter,@Param("limit")int limit,@Param("offset") int offset,@Param("tenantId")String tenantId);



    //查询业务目录关系业务信息列表
    @Results({
            @Result(property = "tables",javaType = List.class,column = "{businessId = businessIdVal,tenantId = tenantId}",many = @Many(select = "queryTablesByBusinessIdAndTenantId"))
    })
    @Select({"<script>",
             " select count(*)over() total,businessInfo.businessId,businessInfo.trustTable,businessInfo.businessId as businessIdVal,businessInfo.name,businessInfo.tenantId as tenantId,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber, business_relation.categoryGuid from businessInfo",
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
            " insert into business2table(businessId, tableGuid, relation_type)values",
            " <foreach collection='list' item='tableGuid' index='index'  separator=','>",
             " (#{businessId},#{tableGuid},#{relationType})",
             " </foreach>",
            " </script>"})
    public int insertTableRelation(@Param("businessId")String businessId, @Param("list")List<String> list, @Param("relationType")int relationType);

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
             " and (tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/' or tableInfo.display_name like concat('%',#{tableName},'%') ESCAPE '/')",
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
             " and (tableInfo.tableName like concat('%',#{tableName},'%') ESCAPE '/' or tableInfo.display_name like concat('%',#{tableName},'%') ESCAPE '/')",
             " </script>"})
    public long getCountBusinessRelatedTable(@Param("businessId")String businessList, @Param("tableName")String tableName);

    @Update("update businessInfo set trustTable=null where trustTable=#{tableId}")
    public int removeBusinessTrustTableByTableId(@Param("tableId")String tableId);

    @Update("update businessInfo set trustTable=#{tableId} where trustTable=#{OldTableId}")
    int updateBusinessTrustTableByTableId(@Param("tableId")String tableId, @Param("OldTableId")String OldTableId);

    @Select("<script>" +
            "select businessInfo.name,businessInfo.module,businessInfo.description,businessInfo.owner," +
            "businessInfo.manager,businessInfo.maintainer,businessInfo.dataassets," +
            "businessInfo.businesslastupdate,businessInfo.businessoperator " +
            " from businessInfo join business_relation " +
            " on businessInfo.businessId = business_relation.businessId " +
            " where businessInfo.tenantid=#{tenantId} and " +
            " business_relation.categoryGuid=#{categoryId} " +
            " </script>")
    public List<BusinessInfo> getAllBusinessByCategory(@Param("categoryId")String categoryId,@Param("tenantId")String tenantId);

    @Select("<script>" +
            "select name from businessInfo " +
            " where businessInfo.tenantid=#{tenantId} " +
            " </script>")
    public List<String> getBusinessNames(@Param("tenantId")String tenantId);

    //批量添加业务信息
    @Insert("<script>" +
            "insert into businessinfo(departmentid,businessid,name,module,description,owner,manager,maintainer,dataassets,submitter,submissionTime,businessOperator,businessLastUpdate,ticketNumber,level2CategoryId,tenantid,businessstatus,technicalstatus,publish,status,private_status,create_mode,submitter_read) " +
            " values " +
            " <foreach item='info' index='index' collection='infos' separator='),(' open='(' close=')'>" +
            "#{info.departmentId},#{info.businessId},#{info.name},#{info.module},#{info.description},#{info.owner},#{info.manager}," +
            "#{info.maintainer},#{info.dataAssets},#{info.submitter},#{info.submissionTime},#{info.businessOperator}," +
            "#{info.businessLastUpdate},#{info.ticketNumber},#{info.level2CategoryId},#{tenantId},1,0,#{info.publish}," +
            "#{info.status},#{info.privateStatus},#{info.createMode},#{info.submitterRead}" +
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

    @Update("UPDATE businessinfo SET approve_id=#{approveId} WHERE businessid=#{businessId}")
    void updateApproveIdAndApproveGroupId(@Param("businessId") String businessId, @Param("approveId") String approveId);

    @Select("SELECT bi.businessid businessId, bi.name businessName, bi.departmentid departmentId, c.name departmentName, " +
            "bi.create_mode createMode, bi.module module, bi.description description, bi.owner, bi.manager manager, " +
            "bi.maintainer maintainer, bi.dataassets dataAssets, bi.publish publish, bi.publish_desc publishDesc " +
            "FROM businessinfo bi " +
            "INNER JOIN category c ON c.guid = bi.departmentId " +
            "WHERE bi.businessid = #{objectId} AND bi.tenantid=#{tenantId}")
    BusinessInfoBO getBusinessApproveDetails(@Param("objectId")String objectId, @Param("tenantId")String tenantId);

    @Select("<script>" +
            "SELECT businessid businessId, publish publish, status, private_status privateStatus, submitter " +
            "FROM businessinfo " +
            "WHERE tenantid=#{tenantId} AND businessid IN " +
            "<foreach item='id' index='index' collection='businessIds' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<BusinessInfo> getBusinessPublicStatus(@Param("businessIds")List<String> businessIds, @Param("tenantId")String tenantId);

    @Update("<script>" +
            "<foreach item='item' index='index' collection='publishStatus' separator=';'>" +
            "UPDATE businessinfo SET " +
            "publish=#{item.publish}, status=#{item.status}, private_status=#{item.privateStatus} " +
            "WHERE businessid=#{item.businessId}" +
            "</foreach>" +
            "</script>")
    void updateBusinessPublicStatus(@Param("publishStatus")List<BusinessInfo> publishStatus);

    //查询业务目录关系业务信息列表（分页）
    @Results({
            @Result(property = "tables",javaType = List.class,column = "{businessId = businessIdVal,tenantId = tenantId}",many = @Many(select = "queryAllTablesByBusinessId"))
    })
    @Select("<script>" +
            "select bi.businessid businessId, bi.trusttable trustTable, " +
            "bi.businessid businessIdVal, bi.name, bi.tenantid tenantId, " +
            "bi.businessstatus businessStatus, bi.technicalstatus technicalStatus, bi.submitter, " +
            "bi.submissiontime submissionTime, bi.ticketnumber ticketNumber, " +
            "bi.publish, bi.status, bi.private_status privateStatus, " +
            "br.categoryguid categoryGuid, bi.businesslastupdate businessLastUpdate " +
            "from businessinfo bi " +
            "join business_relation br on bi.businessid = br.businessid " +
            "where bi.tenantid=#{tenantId} and br.categoryguid=#{categoryGuid} " +
            "and " +
            "(" +
            "bi.private_status='PUBLIC' or (bi.submitter=#{userId} and bi.submitter_read=true) " +
            "or " +
            "(select count(*) from business_2_group b2g " +
            "join user_group_relation ugr on ugr.group_id = b2g.group_id and ugr.user_id=#{userId} " +
            "where b2g.business_id=bi.businessid and b2g.read=true)>0" +
            ") " +
            "order by technicalStatus, businessLastUpdate desc " +
            "<if test='limit!= -1'>" +
            "limit #{limit} " +
            "</if>" +
            "offset #{offset}" +
            "</script>")
    List<BusinessInfoHeader> queryAuthBusinessByCategoryId(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit, @Param("offset") int offset, @Param("tenantId")String tenantId, @Param("userId")String userId);

    @Select("select max(version) from approval_item where object_id=#{objectId}")
    Integer getMaxVersionById(@Param("objectId")String objectId);

    @Results({
            @Result(property = "tables",javaType = List.class,column = "{businessId = businessIdVal,tenantId = tenantId}",many = @Many(select = "queryAllTablesByBusinessId"))
    })
    @Select("<script>" +
            "select count(*)over() total, bi.businessid businessId, bi.businessid businessIdVal, bi.name, bi.businessstatus businessStatus, bi.technicalstatus technicalStatus, " +
            "bi.submitter, bi.submissiontime submissionTime, bi.ticketnumber ticketNumber, br.categoryguid categoryGuid, bi.tenantid tenantId, " +
            "bi.publish, bi.status, bi.private_status privateStatus " +
            "from businessinfo bi " +
            "join business_relation br on br.businessid=bi.businessid " +
            "where " +
            "bi.name like concat('%',#{businessName},'%') ESCAPE '/' " +
            "<if test=\"tenantId != null and tenantId!=''\">" +
            "and bi.tenantid=#{tenantId} " +
            "</if>" +
            "and br.categoryguid in " +
            "<foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" +
            "#{categoryGuid}" +
            "</foreach>" +
            " and " +
            "(" +
            "bi.private_status='PUBLIC' or (bi.submitter=#{userId} and bi.submitter_read=true) " +
            "or " +
            "(select count(*) from business_2_group b2g " +
            "join user_group_relation ugr on ugr.group_id = b2g.group_id and ugr.user_id=#{userId} " +
            "where b2g.business_id=bi.businessid and b2g.read=true)>0" +
            ") " +
            "order by bi.businesslastupdate desc " +
            "<if test='limit!= -1'>" +
            "limit #{limit} " +
            "</if>" +
            "offset #{offset}" +
            "</script>")
    List<BusinessInfoHeader> queryAuthBusinessByName(@Param("businessName")String businessName, @Param("ids") List<String> categoryIds, @Param("limit")int limit, @Param("offset") int offset, @Param("tenantId")String tenantId, @Param("userId")String userId) throws SQLException;

    //多条件查询业务信息列表
    @Results({
            @Result(property = "tables",javaType = List.class,column = "{businessId = businessIdVal,tenantId = tenantId}",many = @Many(select = "queryAllTablesByBusinessId"))
    })
    @Select("<script>" +
            "select count(*)over() total, bi.businessid businessId, bi.businessid businessIdVal, " +
            "bi.tenantid tenantId, bi.departmentid departmentId, bi.name, bi.businessstatus businessStatus, bi.technicalstatus technicalStatus, " +
            "bi.submissiontime submissionTime, u.username submitter, bi.ticketnumber ticketNumber, br.categoryguid categoryGuid, " +
            "bi.publish, bi.status, bi.private_status privateStatus " +
            "from businessinfo bi " +
            "join business_relation br on br.businessid=bi.businessid " +
            "join users u on u.userid=bi.submitter " +
            "where bi.tenantid=#{tenantId} " +
            "and br.categoryguid in " +
            "(" +
            "select guid from category where guid in " +
            "<foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>"  +
            "#{categoryGuid}" +
            "</foreach>" +
            "and categoryType=1 and tenantid=#{tenantId}" +
            ") " +
            "<if test=\"level2CategoryId != null and level2CategoryId!=''\">" +
            "and bi.level2categoryid=#{level2CategoryId} " +
            "</if>" +
            "and bi.technicalstatus=#{status} " +
            "and bi.name like concat('%',#{businessName},'%') ESCAPE '/' " +
            "and bi.ticketnumber like concat('%',#{ticketNumber},'%') ESCAPE '/' " +
            "and u.username like concat('%',#{submitter},'%') ESCAPE '/' " +
            "and " +
            "(" +
            "bi.private_status='PUBLIC' or (bi.submitter=#{userId} and bi.submitter_read=true) " +
            "or " +
            "(select count(*) from business_2_group b2g " +
            "join user_group_relation ugr on ugr.group_id = b2g.group_id and ugr.user_id=#{userId} " +
            "where b2g.business_id=bi.businessid and b2g.read=true)>0" +
            ") " +
            "order by bi.businesslastupdate desc " +
            "<if test='limit!= -1'>" +
            "limit #{limit} " +
            "</if>" +
            "offset #{offset}" +
            "</script>")
    public List<BusinessInfoHeader> queryAuthBusinessByCondition(@Param("ids") List<String> categoryIds, @Param("status")Integer status, @Param("ticketNumber") String ticketNumber,
                                                                 @Param("businessName")String businessName, @Param("level2CategoryId") String level2CategoryId,
                                                                 @Param("submitter") String submitter,@Param("limit")int limit,@Param("offset") int offset,
                                                                 @Param("tenantId")String tenantId, @Param("userId")String userId);

    //查询业务目录关系业务信息列表
    @Select("select bi.name, bi.module module, " +
            "bi.description description, bi.owner, " +
            "bi.manager manager, bi.maintainer maintainer, bi.dataassets dataAssets, " +
            "bi.businesslastupdate businessLastUpdate, bi.businessoperator businessOperator, " +
            "bi.private_status privateStatus, " +
            "(select name from approval_group where id=bi.approve_group_id) as approveGroupId, bi.publish_desc publishDesc " +
            "from businessinfo bi " +
            "join business_relation br on bi.businessid = br.businessid " +
            "where bi.tenantid=#{tenantId} and br.categoryguid=#{categoryGuid} " +
            "and " +
            "(" +
            "bi.private_status='PUBLIC' or (bi.submitter=#{userId} and bi.submitter_read=true) " +
            "or " +
            "(select count(*) from business_2_group b2g " +
            "join user_group_relation ugr on ugr.group_id = b2g.group_id and ugr.user_id=#{userId} " +
            "where b2g.business_id=bi.businessid and b2g.read=true)>0" +
            ")")
    List<BusinessInfo> queryAllAuthBusinessByCategoryId(@Param("categoryGuid")String categoryGuid, @Param("tenantId")String tenantId, @Param("userId")String userId);

    //查询业务目录下业务对象数量
    @Select("select count(bi.businessid) count " +
            "from businessinfo bi " +
            "join business_relation br on bi.businessid = br.businessid " +
            "where bi.tenantid=#{tenantId} and br.categoryguid=#{categoryGuid} " +
            "and " +
            "(" +
            "bi.private_status='PUBLIC' or (bi.submitter=#{userId} and bi.submitter_read=true) " +
            "or " +
            "(select count(*) from business_2_group b2g " +
            "join user_group_relation ugr on ugr.group_id = b2g.group_id and ugr.user_id=#{userId} " +
            "where b2g.business_id=bi.businessid and b2g.read=true)>0" +
            ")")
    int getBusinessCountByCategoryId(@Param("categoryGuid")String categoryGuid, @Param("tenantId")String tenantId, @Param("userId")String userId);


    @Select("<script>" +
            "select bi.name, bi.module module, " +
            "bi.description description, bi.owner, " +
            "bi.manager manager, bi.maintainer maintainer, bi.dataassets dataAssets, " +
            "bi.businesslastupdate businessLastUpdate, bi.businessoperator businessOperator, " +
            "bi.private_status privateStatus, " +
            "(select name from approval_group where id=bi.approve_group_id) as approveGroupId, bi.publish_desc publishDesc " +
            "from businessinfo bi " +
            "join business_relation br on bi.businessid = br.businessid " +
            "where bi.tenantid=#{tenantId} and br.categoryguid=#{categoryGuid} " +
            "and bi.businessid in " +
            " <foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            " #{id}" +
            " </foreach>" +
            "</script>")
    List<BusinessInfo> getBusinessByIds(@Param("ids")List<String> ids, @Param("categoryGuid")String categoryGuid, @Param("tenantId")String tenantId);

    // 设置业务对象’创建人是否可见‘
    @Update("<script>" +
            "update businessinfo set submitter_read=#{submitterRead} " +
            "where businessid in " +
            "<foreach item='id' index='index' collection='ids' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void updateBusinessSubmitterRead(@Param("submitterRead")boolean submitterRead, @Param("ids")List<String> ids);

    @Delete("delete from business2table where businessid=#{businessId} and tableguid=#{tableId}")
    void deleteRelationByBusinessIdAndTableId(@Param("businessId")String businessId, @Param("tableId")String tableId);

    //查询业务信息关联的数据库表（包含衍生表登记所关联的表）
    @Select("select ti.tableguid tableGuid, ti.tablename tableName, ti.dbname dbName, ti.status, ti.createtime createTime, ti.databaseguid databaseGuid, " +
            "COALESCE(ti.display_name, ti.tablename, '') as displayName, ti.description, ti.source_id sourceId, b2t.relation_type relationType, " +
            "case when " +
            "(select count(*) from source_info_derive_table_info sidti " +
            "inner join source_info_derive_column_info sidci on sidci.table_guid= sidti.table_guid and sidci.important=true " +
            "where sidti.source_table_guid=ti.tableguid)>0 then true else false end as important, " +
            "case when " +
            "(select count(*) from source_info_derive_table_info sidti " +
            "inner join source_info_derive_column_info sidci on sidci.table_guid= sidti.table_guid and sidci.secret=true " +
            "where sidti.source_table_guid=ti.tableguid)>0 then true else false end as secret " +
            "from business2table b2t " +
            "join tableInfo ti on ti.tableguid=b2t.tableguid and status='ACTIVE' " +
            "where b2t.businessid=#{businessId}"
    )
    List<TechnologyInfo.Table> queryAllTablesByBusinessId(@Param("businessId") String businessId, @Param("tenantId") String tenantId);

    @Select("<script>" +
            "SELECT DISTINCT\n" +
                    " ti.tableGuid,\n" +
                    " COUNT ( * ) OVER () total,\n" +
                    " COALESCE(tdsr.data_source_id,(SELECT data_source_id FROM source_info WHERE \"version\" = 0 AND category_id = #{categoryGuid} AND tenant_id = #{tenantId} ),'ID') AS sourceId,\n" +
                    "\n" +
                    " COALESCE (\n" +
                    "  ( SELECT source_name FROM data_source WHERE source_id = tdsr.data_source_id ),\n" +
                    "  (\n" +
                    "  SELECT\n" +
                    "   ds.source_name \n" +
                    "  FROM\n" +
                    "   source_info si\n" +
                    "   LEFT JOIN data_source ds ON si.data_source_id = ds.source_id \n" +
                    "  WHERE\n" +
                    "   VERSION = 0 \n" +
                    "   AND category_id = #{categoryGuid} \n" +
                    "   AND tenant_id = #{tenantId} \n" +
                    "  ),\n" +
                    "  'hive' \n" +
                    " ) AS sourceName," +
                    " (SELECT id FROM source_info WHERE \"version\" = 0 AND category_id = #{categoryGuid} AND tenant_id = #{tenantId} ) AS sourceInfoId, " +
                    " tdsr.category_id AS categoryGuid,\n" +
                    " ti.tableName,\n" +
                    " ti.dbName,\n" +
                    " ti.databaseguid AS dbId,\n" +
                    " ti.tableGuid,\n" +
                    " ti.status,\n" +
                    " tdsr.update_time AS generateTime,\n" +
                    " ti.description \n" +
                    "FROM\n" +
                    " tableinfo ti\n" +
                    " LEFT JOIN table_data_source_relation tdsr ON tdsr.table_id = ti.tableGuid \n" +
                    "WHERE\n" +
                    " ((\n" +
                    "   tdsr.category_id = #{categoryGuid} \n" +
                    "   AND tdsr.tenant_id = #{tenantId} \n" +
                    "   ) \n" +
                    "  OR ti.databaseguid = ( SELECT db_guid FROM db_category_relation dcr WHERE dcr.category_id = #{categoryGuid} AND dcr.tenant_id = #{tenantId} ) \n" +
                    " ) \n" +
                    " AND ti.status = 'ACTIVE'" +
                    " <if test=\"tableName != '' and tableName != null\">" +
            " AND ti.tablename like concat('%',#{tableName},'%')" +
                    " </if>" +
            " AND ti.tableguid not in (select tableguid from business2table where businessid=#{businessId}) " +
            " ORDER BY" +
                    " ti.status,\n" +
                    " tdsr.update_time DESC,\n" +
                    " ti.tablename" +
            " <if test='limit!= -1'>" +
            " limit #{limit}" +
            " </if>" +
            " offset #{offset}" +
            " </script>")
    List<RelationEntityV2> queryRelationByCategoryGuidAndBusinessIdFilterV2(@Param("categoryGuid")String categoryGuid, @Param("businessId")String businessId, @Param("tenantId") String tenantId, @Param("limit") int limit, @Param("offset") int offset,@Param("tableName") String tableName);

    @Select("<script>" +
            "select count(*) " +
            "from tableinfo ti " +
            "join database_group_relation dbgr on dbgr.database_guid=ti.databaseguid " +
            "where ti.tableguid=#{tableId} " +
            "and dbgr.group_id in " +
            "<foreach item='id' index='index' collection='userGroupIds' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int getDatabaseAuth(@Param("tableId")String tableId, @Param("userGroupIds")List<String> userGroupIds);

    @Select("<script>" +
            "select count(*) " +
            "from tableinfo ti " +
            "join datasource_group_relation dsgr on dsgr.source_id=ti.source_id " +
            "where ti.tableguid=#{tableId} " +
            "and dsgr.group_id in " +
            "<foreach item='id' index='index' collection='userGroupIds' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int getDataSourceAuth(@Param("tableId")String tableId, @Param("userGroupIds")List<String> userGroupIds);
}
