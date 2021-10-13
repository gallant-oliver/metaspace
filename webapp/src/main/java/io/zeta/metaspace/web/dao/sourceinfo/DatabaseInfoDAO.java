package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.model.metadata.Database;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForCategory;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForList;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DatabaseInfoDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId") String databaseId);

    @Select("<script>" +
            "INSERT INTO source_info_relation2parent_category \n" +
            "( source_info_id, parent_category_id, create_time, modify_time )\n" +
            "VALUES\n" +
            "  <foreach item='dif' index='index' collection='difList' separator=','> " +
            "( #{dif.id}, #{dif.parentCategoryId}, NOW( ), NOW( ) )" +
            "</foreach>" +
            "</script>")
    void insertDatabaseInfoRelationParentCategory(@Param("difList") List<DatabaseInfoForCategory> difList);

    @Insert("<script>" +
            "INSERT INTO  public . source_info  (\n" +
            " id ,\n" +
            " category_id ,\n" +
            " database_id ,\n" +
            " data_source_id ,\n" +
            " database_alias ,\n" +
            " planning_package_code ,\n" +
            " planning_package_name ,\n" +
            " version ,\n" +
            " extract_tool ,\n" +
            " extract_cycle ,\n" +
            " security ,\n" +
            " security_cycle ,\n" +
            " importance ,\n" +
            " description ,\n" +
            " updater ,\n" +
            " creator ,\n" +
            " status ,\n" +
            " annex_id ,\n" +
            " bo_name ,\n" +
            " bo_department_name ,\n" +
            " bo_email ,\n" +
            " bo_tel ,\n" +
            " to_name ,\n" +
            " to_department_name ,\n" +
            " to_email ,\n" +
            " to_tel ,\n" +
            " technical_leader ,\n" +
            " business_leader ,\n" +
            " tenant_id ,\n" +
            " update_time ,\n" +
            " record_time ,\n" +
            " create_time ,\n" +
            " modify_time ,\n" +
            " approve_group_id  \n" +
            ")\n" +
            "VALUES\n" +
            "    <foreach item='dip' index='index' collection='dips' separator=','>" +
            "(" +
            "#{dip.id},\n" +
            "#{dip.categoryId},\n" +
            "#{dip.databaseId},\n" +
            "#{dip.dataSourceId},\n" +
            "#{dip.databaseAlias},\n" +
            "#{dip.planningPackageCode},\n" +
            "#{dip.planningPackageName},\n" +
            "0,\n" +
            "#{dip.extractTool},\n" +
            "#{dip.extractCycle},\n" +
            "#{dip.security},\n" +
            "#{dip.securityCycle},\n" +
            "#{dip.importance},\n" +
            "#{dip.description},\n" +
            "#{dip.updater},\n" +
            "#{dip.creator},\n" +
            "#{dip.status},\n" +
            "#{dip.annexId},\n" +
            "#{dip.boName},\n" +
            "#{dip.boDepartmentName},\n" +
            "#{dip.boEmail},\n" +
            "#{dip.boTel},\n" +
            "#{dip.toName},\n" +
            "#{dip.toDepartmentName},\n" +
            "#{dip.toEmail},\n" +
            "#{dip.toTel},\n" +
            "#{dip.technicalLeader},\n" +
            "#{dip.businessLeader},\n" +
            "#{dip.tenantId},\n" +
            "NOW( ),\n" +
            "NOW( ),\n" +
            "NOW( ),\n" +
            "NOW( )," +
            "#{dip.approveGroupId} )\n" +
            "</foreach>" +
            "</script>")
    void insertDatabaseInfo(@Param("dips") List<DatabaseInfoPO> databaseInfoPOs);

    @Select("SELECT\n" +
            "s.id ,\n" +
            "c.name AS categoryName,\n" +
            "c.guid AS categoryId,\n" +
            "db.database_name AS databaseName,\n" +
            "db.database_guid  AS databaseId,\n" +
            "db.db_type AS databaseTypeName,\n" +
            "ds.database AS databaseInstanceName,\n" +
            "ds.source_name AS dataSourceName,\n" +
            "s.data_source_id AS dataSourceId,\n" +
            "s.database_alias,\n" +
            "s.planning_package_name,\n" +
            "s.planning_package_code,\n" +
            "s.extract_tool,\n" +
            "s.extract_cycle,\n" +
            "s.security,\n" +
            "s.security_cycle,\n" +
            "s.status,\n" +
            "s.importance,\n" +
            "s.description,\n" +
            "s.creator AS recorderGuid,\n" +
            "s.annex_id,\n" +
            "s.bo_name,\n" +
            "s.bo_tel,\n" +
            "s.bo_department_name,\n" +
            "s.bo_email,\n" +
            "s.to_name,\n" +
            "s.to_tel,\n" +
            "s.to_department_name,\n" +
            "s.to_email,\n" +
            "s.technical_leader AS technicalLeaderId,\n" +
            "s.business_leader AS businessLeaderId,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.technical_leader ) AS technicalLeaderName,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.business_leader ) AS businessLeaderName,\n" +
            "ag.name AS approveGroupName,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.updater ) AS updaterName,\n" +
            "s.update_time AS updateTime,\n" +
            "ag.name AS approveGroupName,\n" +
            "ag.id AS approveGroupId,\n" +
            "ai.reason as audit_des ,\n" +
            "ai.approve_time AS auditTime,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = ai.approver ) AS auditorName\n" +
            "FROM\n" +
            "source_info s LEFT JOIN category c ON s.category_id = c.guid AND c.tenantid = s.tenant_id\n" +
            "LEFT JOIN db_info db ON s.database_id = db.database_guid \n" +
            "LEFT JOIN data_source ds ON s.data_source_id = ds.source_id\n" +
            "LEFT JOIN approval_group ag ON s.approve_group_id = ag.\"id\"\n" +
            "LEFT JOIN approval_item ai ON s.approve_id = ai.\"id\"\n" +
            "WHERE\n" +
            "s.id = #{id} AND s.version = #{version}\n")
    DatabaseInfoBO getDatabaseInfoById(@Param("id") String id, @Param("tenantId") String tenantId, @Param("version") int version);

    @Select("<script>" +
            "SELECT id,database_alias " +
            "FROM " +
            " source_info " +
            "WHERE\n" +
            " id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND " +
            " version = 0" +
            "</script>")
    List<DatabaseInfo> getDatabaseIdAndAliasByIds(@Param("ids") List<String> idList);

    @Update("<script>" +
            "UPDATE source_info \n" +
            "SET status = #{status},update_time = NOW(),modify_time = NOW() \n" +
            "WHERE\n" +
            " id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND " +
            " version = 0" +
            "</script>")
    void updateStatusByIds(@Param("ids") List<String> idList, @Param("status") String status);

    @Update("UPDATE source_info \n" +
            "SET approve_id = #{approveId},approve_group_id = #{approveGroupId},update_time = NOW(),modify_time = NOW() \n" +
            "WHERE\n" +
            " id = #{id} " +
            " AND " +
            " version = 0")
    void updateApproveIdAndApproveGroupIdById(@Param("id") String id, @Param("approveId") String approveId, @Param("approveGroupId") String approveGroupId);

    @Select("<script>" +
            "SELECT\n" +
            " s.id ,\n" +
            " s.database_id AS databaseId ,\n" +
            " c.name AS categoryName,\n" +
            " c.guid AS categoryId,\n" +
            " db.database_name AS databaseName,\n" +
            " db.db_type AS databaseTypeName,\n" +
            " s.database_alias,\n" +
            " s.security,\n" +
            " s.status,\n" +
            " (SELECT u.username FROM users u WHERE u.userid = s.updater ) AS updater_name,\n" +
            " s.update_time,\n" +
            " ai.reason as audit_des ,\n" +
            " (SELECT u.username FROM users u WHERE u.userid = ai.approver ) AS auditor_name\n" +
            "FROM\n" +
            " source_info s LEFT JOIN category c ON s.category_id = c.guid AND \n" +
            " c.tenantid = s.tenant_id\n" +
            " LEFT JOIN db_info db ON s.database_id = db.database_guid \n" +
            " LEFT JOIN data_source ds ON s.data_source_id = ds.source_id\n" +
            " LEFT JOIN approval_group ag ON s.approve_group_id = ag.\"id\"\n" +
            " LEFT JOIN approval_item ai ON s.approve_id = ai.\"id\"\n" +
            "WHERE\n" +
            " s.tenant_id = #{tenantId}\n" +
            " AND " +
            " s.version = 0 " +
            "<if test=\"status != null\">" +
            " AND " +
            "  s.status = #{status}" +
            "</if>" +
            "<if test=\"name != null and name != ''\">" +
            " AND " +
            " (s.database_alias like CONCAT('%',#{name},'%') OR db.database_name like CONCAT('%',#{name},'%'))" +
            "</if>" +
            "<if test=\"ids != null\">" +
            " AND " +
            " s.id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</if>" +
            " ORDER BY s.update_time DESC,s.database_alias\n" +
            " LIMIT #{limit} " +
            " OFFSET #{offset}" +
            "</script>")
    List<DatabaseInfoForList> getDatabaseInfoList(@Param("tenantId") String tenantId, @Param("status") String status, @Param("ids") List<String> idList,
                                                  @Param("name") String name, @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "SELECT\n" +
            " COUNT(1)" +
            "FROM\n" +
            " source_info s LEFT JOIN category c ON s.category_id = c.guid AND \n" +
            " c.tenantid = s.tenant_id\n" +
            " LEFT JOIN db_info db ON s.database_id = db.database_guid \n" +
            " LEFT JOIN data_source ds ON s.data_source_id = ds.source_id\n" +
            " LEFT JOIN approval_group ag ON s.approve_group_id = ag.\"id\"\n" +
            " LEFT JOIN approval_item ai ON s.approve_id = ai.\"id\"\n" +
            "WHERE\n" +
            " s.tenant_id = #{tenantId}\n" +
            " AND " +
            " s.version = 0 " +
            "<if test=\"status != null\">" +
            " AND" +
            "  s.status = #{status}" +
            "</if>" +
            "<if test=\"name != null and name != ''\">" +
            "  AND\n" +
            " (s.database_alias like CONCAT('%',#{name},'%') OR db.database_name like CONCAT('%',#{name},'%'))" +
            "</if>" +
            "</script>")
    int getDatabaseInfoListCount(@Param("tenantId") String tenantId, @Param("status") String status, @Param("name") String name);

    @Select("<script>" +
            "SELECT\n" +
            " s.ID,\n" +
            " s.category_id AS categoryId,\n" +
            " s.database_id AS databaseId,\n" +
            " s.database_alias AS name,\n" +
            " s.importance ,\n" +
            " s.creator AS creator,\n" +
            " sirc.parent_category_id AS parentCategoryId \n" +
            "FROM\n" +
            " source_info s\n" +
            " LEFT JOIN source_info_relation2parent_category sirc ON s.\"id\" = sirc.source_info_id \n" +
            "WHERE\n" +
            " s.ID IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND " +
            " version = 0" +
            "</script>")
    List<DatabaseInfoForCategory> getDatabaseInfoByIds(@Param("ids") List<String> idList);


    @Select("<script>" +
            "INSERT INTO source_info " +
            " (\n" +
            " SELECT\n" +
            "  \"id\",\n" +
            "  (select parent_category_id from source_info_relation2parent_category where source_info_id = #{id})category_id,\n" +
            "  database_id,\n" +
            "  database_alias,\n" +
            "  planning_package_code,\n" +
            "  planning_package_name,\n" +
            "  extract_tool,\n" +
            "  extract_cycle,\n" +
            "  SECURITY,\n" +
            "  security_cycle,\n" +
            "  importance,\n" +
            "  description,\n" +
            "  approve_id,\n" +
            "  approve_group_id,\n" +
            "  updater,\n" +
            "  creator,\n" +
            "  status,\n" +
            "  annex_id,\n" +
            "  bo_name,\n" +
            "  bo_department_name,\n" +
            "  bo_email,\n" +
            "  bo_tel,\n" +
            "  to_name,\n" +
            "  to_department_name,\n" +
            "  to_email,\n" +
            "  to_tel,\n" +
            "  technical_leader,\n" +
            "  business_leader,\n" +
            "  tenant_id,\n" +
            "  ( SELECT MAX(version)+1 FROM source_info WHERE id = #{id} ) AS VERSION ,\n" +
            "  update_time,\n" +
            "  record_time,\n" +
            "  create_time,\n" +
            "  modify_time,\n" +
            "  data_source_id \n" +
            " FROM\n" +
            "  source_info \n" +
            " WHERE\n" +
            "  \"id\" = #{id}" +
            " AND \"version\" = 0 \n" +
            " )" +
            "</script>")
    void insertHistoryVersion(@Param("id") String id);

    @Update("UPDATE source_info SET category_id = #{categoryId},update_time = NOW(), modify_time = NOW() WHERE id  = #{id} AND version = #{version}")
    void updateRealCategoryRelation(@Param("id") String sourceInfoId, @Param("categoryId") String categoryId, @Param("version") int version);

    @Delete("<script>" +
            "DELETE FROM source_info_relation2parent_category " +
            "   WHERE source_info_id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteSourceInfoAndParentCategoryRelation(@Param("ids") List<String> idList);

    @Select("<script>" +
            "SELECT\n" +
            " status \n" +
            "FROM\n" +
            " source_info \n" +
            "WHERE\n" +
            " \"id\" IN \n" +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND \"version\" = 0 \n" +
            "GROUP BY\n" +
            " status" +
            "</script>")
    List<String> getStatusByIdList(@Param("ids") List<String> idList);

    @Update("UPDATE source_info  \n" +
            "SET database_alias = #{di.databaseAlias},\n" +
            "planning_package_code = #{di.planningPackageCode},\n" +
            "planning_package_name = #{di.planningPackageName},\n" +
            "extract_tool = #{di.extractTool},\n" +
            "extract_cycle = #{di.extractCycle},\n" +
            "\"security\" = #{di.security},\n" +
            "security_cycle = #{di.securityCycle},\n" +
            "importance = #{di.importance}, description = #{di.description},\n" +
            "bo_name = #{di.boName},\n" +
            "bo_tel = #{di.boTel},\n" +
            "bo_department_name = #{di.boDepartmentName},\n" +
            "bo_email = #{di.boEmail},\n" +
            "to_name = #{di.toName},\n" +
            "to_tel = #{di.toTel},\n" +
            "to_email = #{di.toEmail},\n" +
            "to_department_name = #{di.toDepartmentName}," +
            "technical_leader = #{di.technicalLeader},\n" +
            "business_leader = #{di.businessLeader},\n" +
            "annex_id = #{di.annexId}, \n" +
            "updater  = #{userId}," +
            "update_time = NOW()," +
            "modify_time = NOW() " +
            " WHERE\n" +
            " id = #{di.id} AND \"version\" = 0")
    void updateSourceInfo(@Param("di") DatabaseInfo databaseInfo, @Param("userId") String userId);

    @Select("<script>" +
            "SELECT\n" +
            " \"count\" ( 1 ) > 0 \n" +
            "FROM\n" +
            " source_info \n" +
            "WHERE\n" +
            " tenant_id = #{tenantId}\n" +
            " AND database_alias = #{databaseAlias}" +
            " AND version = 0" +
            "<if test = \"id !=null and id !=''\">" +
            " AND id != #{id}" +
            "</if>" +
            "</script>")
    boolean getDatabaseDuplicateName(@Param("tenantId") String tenantId, @Param("databaseAlias") String databaseAlias, @Param("id") String id);

    @Delete("<script>" +
            " DELETE " +
            " FROM source_info " +
            " WHERE " +
            " id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND version = 0" +
            "</script>")
    void deleteSourceInfoForVersion(@Param("ids") List<String> idList, @Param("version") int version);

    @Delete("<script>" +
            " DELETE " +
            " FROM source_info " +
            " WHERE " +
            " version = #{version}" +
            " AND id = #{id}" +
            "</script>")
    void removeHistoryVersion(@Param("id") String id, @Param("version") int version);

    @Select("SELECT MAX(version) FROM source_info WHERE id = #{id} ")
    int getMaxVersionById(@Param("id") String objectId);

    @Insert("<script>" +
            "INSERT INTO public.source_info(\n" +
            "\t id, category_id, database_id, database_alias, planning_package_code, planning_package_name, extract_tool, extract_cycle," +
            " security, security_cycle, importance, description, creator, status, version, " +
            "bo_name, bo_department_name, bo_email, bo_tel, to_name, to_department_name, to_email, to_tel, technical_leader, business_leader, " +
            "tenant_id, update_time, record_time, create_time, modify_time)\n" +
            "\t VALUES " +
            "<foreach collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n" +
            "(#{item.id},#{item.categoryId},#{item.databaseId},#{item.databaseAlias},#{item.planningPackageCode},#{item.extractTool},#{item.extractCycle}," +
            "#{item.security},#{item.securityCycle},#{item.importance},#{item.description},#{item.creator},#{item.status},0," +
            "#{item.boName},#{item.boDepartmentName},#{item.boEmail},#{item.boTel},#{item.toName},#{item.toDepartmentName},#{item.toEmail},#{item.toTel},#{item.technicalLeader},#{item.businessLeader}," +
            "#{item.tenantId},NOW(),NOW(),NOW(),NOW())\n" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<DatabaseInfoPO> saveList);

    @Select("SELECT COUNT(1)>0 FROM source_info WHERE database_id = #{databaseId} AND tenant_id = #{tenantId} AND version = 0")
    boolean getDatabaseByDbId(@Param("databaseId") String databaseId, @Param("tenantId") String tenantId);

    @Select("<script>" +
            "SELECT\n" +
            " s.ID,\n" +
            " s.category_id AS categoryId,\n" +
            " s.database_id AS databaseId,\n" +
            " s.database_alias AS name\n" +
            "FROM\n" +
            " source_info s\n" +
            "WHERE\n" +
            " version = 0"+
            " AND " +
            " (s.category_id IN "+
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>" +
            " ) AND " +
            " s.tenant_id =#{tenantId}"+
            "</script>")
    List<DatabaseInfoForCategory> getDatabaseInfoByCategoryId(@Param("ids")List<String> ids,@Param("tenantId")String tenantId,@Param("guid")String guid);

    @Select("SELECT parent_category_id FROM source_info_relation2parent_category WHERE source_info_id = #{objectId}")
    String getParentCategoryIdById(@Param("objectId") String objectId);

    @Update("INSERT INTO source_info_relation2parent_category " +
            " VALUES (#{di.id},(select parentcategoryguid FROM category WHERE guid = #{di.categoryId}),NOW(),NOW()) ")
    void insertParentRelation(@Param("di") DatabaseInfo databaseInfo);

    @Select("<script>" +
            " SELECT count(*)over() total,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,sd.source_id,db.database_description,db.owner FROM db_info as db INNER JOIN source_db as sd on db.database_guid = sd.db_guid" +
            " WHERE db.status = 'ACTIVE' AND sd.source_id = #{sourceId}" +
            " <if test='limit != -1'>" +
            "  limit #{limit} " +
            " </if>" +
            " <if test='offset!= 0'>" +
            "  offset #{offset}" +
            " </if>" +
            "</script>")
    List<Database> selectBySourceId(@Param("sourceId") String sourceId, @Param("limit") Long limit, @Param("offset") Long offset);

 @Select("<script>" +
            " SELECT count(*)over() total,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,sd.source_id,db.database_description,db.owner FROM db_info as db INNER JOIN source_db as sd on db.database_guid = sd.db_guid" +
            " WHERE db.status = 'ACTIVE' AND sd.source_id = #{sourceId}" +
            " and db.database_guid in (select database_guid from database_group_relation where source_id=#{sourceId}"+
            " <if test='groupIds!=null and groupIds.size() > 0'>" +
            " and group_id in " +
            "<foreach collection='groupIds' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>" +
            " </if>" +
            ")"+
            " <if test='limit != -1'>" +
            "  limit #{limit} " +
            " </if>" +
            " <if test='offset!= 0'>" +
            "  offset #{offset}" +
            " </if>" +
            "</script>")
    List<Database> selectDataBaseBySourceId(@Param("sourceId") String sourceId,@Param("groupIds") List<String> groupIds, @Param("limit") Long limit, @Param("offset") Long offset);

    @Select("<script>" +
            " SELECT count(*)over() total,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,'hive' as source_id,db.database_description,db.owner FROM db_info as db" +
            " WHERE db.status = 'ACTIVE' AND db.db_type = 'HIVE' AND db.database_name in " +
            " <foreach collection='hiveList' item='item' separator=',' open='(' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            " <if test='limit != -1'>" +
            "  limit #{limit} " +
            " </if>" +
            " <if test='offset!= 0'>" +
            "  offset #{offset}" +
            " </if>" +
            "</script>")
    List<Database> selectByHive(@Param("hiveList") List<String> hiveList, @Param("limit") Long limit, @Param("offset") Long offset);

    @Select("<script>" +
            " SELECT count(t.*)over() total, t.* from (" +
            " SELECT source.tenantid as tenantId,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,sd.source_id,db.database_description,db.owner FROM db_info as db" +
            " INNER JOIN source_db as sd on db.database_guid = sd.db_guid INNER JOIN data_source as source on source.source_id = sd.source_id" +
            " WHERE db.status = 'ACTIVE' AND source.tenantid = #{tenantId} AND database_name like concat('%',#{dbName},'%')" +
            " <if test='groupIds != null and groupIds.size() > 0'>" +
            " and db.database_guid in (select database_guid from database_group_relation where "+
            " group_id in "+
            " <foreach collection='groupIds' item='id' separator=',' open='(' close=')'>" +
            "  #{id}" +
            " </foreach> )" +
            " </if>" +
            " <if test='hiveList != null and hiveList.size() > 0'>" +
            " union" +
            " SELECT '' as tenantId,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,'hive' as source_id,db.database_description,db.owner FROM db_info as db" +
            " WHERE db.status = 'ACTIVE' AND db.db_type = 'HIVE' AND db.database_name like concat('%',#{dbName},'%') AND db.database_name in " +
            " <foreach collection='hiveList' item='item' separator=',' open='(' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            " </if>" +
            " ) as t" +
            " <if test='limit != -1'>" +
            "  limit #{limit} " +
            " </if>" +
            " <if test='offset!= 0'>" +
            "  offset #{offset}" +
            " </if>" +
            "</script>")
    List<Database> selectByDbNameAndTenantId(@Param("tenantId") String tenantId,@Param("groupIds") List<String> groupIds, @Param("dbName") String dbName, @Param("hiveList") List<String> hiveList, @Param("limit") Long limit, @Param("offset") Long offset);

    @Select("<script>" +
            " SELECT count(t.*)over() total, t.* from (" +
            " SELECT source.tenantid as tenantId,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,sd.source_id,db.database_description,db.owner FROM db_info as db" +
            " INNER JOIN source_db as sd on db.database_guid = sd.db_guid INNER JOIN data_source as source on source.source_id = sd.source_id" +
            " WHERE db.status = 'ACTIVE' AND database_name like concat('%',#{dbName},'%') " +
            " <if test='tenantIdList != null and tenantIdList.size() > 0'>" +
            " AND source.tenantid in " +
            " <foreach collection='tenantIdList' item='item' separator=',' open='(' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            " </if>" +
            " <if test='groupIds != null and groupIds.size() > 0'>" +
            " and db.database_guid in (select database_guid from database_group_relation where "+
            " group_id in "+
            " <foreach collection='groupIds' item='id' separator=',' open='(' close=')'>" +
            "  #{id}" +
            " </foreach> )" +
            " </if>" +
            " <if test='hiveList != null and hiveList.size() > 0'>" +
            " union" +
            " SELECT '' as tenantId,db.database_guid as databaseId,db.database_name as databaseName,db.db_type,db.status,'hive' as source_id,db.database_description,db.owner FROM db_info as db" +
            " WHERE db.status = 'ACTIVE' AND db.db_type = 'HIVE' AND db.database_name like concat('%',#{dbName},'%') AND db.database_name in " +
            " <foreach collection='hiveList' item='item' separator=',' open='(' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            " </if>" +
            " ) as t" +
            " <if test='limit != -1'>" +
            "  limit #{limit} " +
            " </if>" +
            " <if test='offset!= 0'>" +
            "  offset #{offset}" +
            " </if>" +
            "</script>")
    List<Database> selectByDbNameAndTenantIdList(@Param("tenantIdList") List<String> tenantIdList,@Param("groupIds") List<String> groupIds, @Param("dbName") String dbName, @Param("hiveList") List<String> hiveList, @Param("limit") Long limit, @Param("offset") Long offset);


    @Select("<script>" +
            " SELECT databaseguid as databaseId, COUNT(tableguid) as tableCount FROM tableinfo" +
            " WHERE status = 'ACTIVE' AND databaseguid IN" +
            " <foreach collection='dbGuidList' item='item' separator=',' open='(' close=')'>" +
            "  #{item.databaseId}" +
            " </foreach>" +
            " GROUP BY databaseguid" +
            "</script>")
    List<Database> selectTableCountByDB(@Param("dbGuidList") List<Database> dbGuidList);

    @Select("SELECT s.id , s.category_id AS categoryId, \n" +
            "db.db_type  AS databaseTypeName,db.database_name AS databaseName, \n" +
            "ds.database AS databaseInstanceName,\n" +
            "ds.source_name AS dataSourceName,\n" +
            "s.database_alias AS databaseAlias,\n" +
            "s.data_source_id AS dataSourceId,"+
            "s.planning_package_name,\n" +
            "s.planning_package_code,\n" +
            "s.extract_tool,\n" +
            "s.extract_cycle,\n" +
            "s.security,\n" +
            "s.security_cycle,\n" +
            "s.status,\n" +
            "s.importance,\n" +
            "s.description,\n" +
            "s.bo_name,\n" +
            "s.bo_tel,\n" +
            "s.bo_department_name,\n" +
            "s.bo_email,\n" +
            "s.to_name,\n" +
            "s.to_tel,\n" +
            "s.to_department_name,\n" +
            "s.to_email,\n" +
            "s.technical_leader AS technicalLeaderId,\n" +
            "s.business_leader AS businessLeaderId,\n" +
            "s.version , s.annex_id, " +
            "(SELECT u.username FROM users u WHERE u.userid = s.technical_leader ) AS technicalLeaderName,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.business_leader ) AS businessLeaderName,\n" +
            "s.creator AS recorderGuid, \n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.creator )AS recorderName "+
            "FROM\n" +
            "source_info s LEFT JOIN db_info db ON s.database_id = db.database_guid  \n" +
            " LEFT JOIN data_source ds ON s.data_source_id = ds.source_id "+
            "WHERE \n" +
            "s.tenant_id=#{tenantId} AND s.data_source_id=#{sourceId} and s.database_id=#{schemaId} and s.version=0 \n")
    List<DatabaseInfoBO> getLastDatabaseInfoByDatabaseId(@Param("schemaId") String schemaId,
                                                   @Param("tenantId") String tenantId, @Param("sourceId") String sourceId);

    @Select("SELECT s.id , s.category_id AS categoryId, \n" +
            "db.db_type  AS databaseTypeName,db.database_name AS databaseName, \n" +
            "ds.database AS databaseInstanceName,\n" +
            "ds.source_name AS dataSourceName,\n" +
            "s.database_alias AS databaseAlias,\n" +
            "s.data_source_id AS dataSourceId,"+
            "s.planning_package_name,\n" +
            "s.planning_package_code,\n" +
            "s.extract_tool,\n" +
            "s.extract_cycle,\n" +
            "s.tenant_id AS tenantId,"+
            "s.security,\n" +
            "s.security_cycle,\n" +
            "s.status,\n" +
            "s.importance,\n" +
            "s.description,\n" +
            "s.bo_name,\n" +
            "s.bo_tel,\n" +
            "s.bo_department_name,\n" +
            "s.bo_email,\n" +
            "s.to_name,\n" +
            "s.to_tel,\n" +
            "s.to_department_name,\n" +
            "s.to_email,\n" +
            "s.technical_leader AS technicalLeaderId,\n" +
            "s.business_leader AS businessLeaderId,\n" +
            "s.version , s.annex_id, " +
            "(SELECT u.username FROM users u WHERE u.userid = s.technical_leader ) AS technicalLeaderName,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.business_leader ) AS businessLeaderName,\n" +
            "s.creator AS recorderGuid, \n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.creator )AS recorderName "+
            "FROM\n" +
            "source_info s LEFT JOIN db_info db ON s.database_id = db.database_guid  \n" +
            " LEFT JOIN data_source ds ON s.data_source_id = ds.source_id "+
            "WHERE \n" +
            "s.data_source_id=#{sourceId} and db.database_name=#{dbName} and  s.version = 0 \n")
    List<DatabaseInfoBO> getDatabaseInfoByDatabaseName(@Param("dbName") String dbName, @Param("sourceId") String sourceId);
}
