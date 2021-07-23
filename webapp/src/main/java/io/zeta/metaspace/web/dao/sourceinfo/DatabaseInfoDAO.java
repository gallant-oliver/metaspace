package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.bo.DatabaseInfoBO;
import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfo;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForCategory;
import io.zeta.metaspace.model.sourceinfo.DatabaseInfoForList;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DatabaseInfoDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);

    @Select("INSERT INTO source_info_relation2parent_category \n" +
            "( source_info_id, parent_category_id, create_time, modify_time )\n" +
            "VALUES\n" +
            "( #{id}, #{categoryId}, NOW( ), NOW( ) )")
    void insertDatabaseInfoRelationParentCategory(@Param("id")String id,@Param("categoryId")String categoryId);

    @Insert("INSERT INTO \"public\".\"source_info\" (\n" +
            "\"id\",\n" +
            "\"category_id\",\n" +
            "\"database_id\",\n" +
            "\"database_alias\",\n" +
            "\"planning_package_code\",\n" +
            "\"planning_package_name\",\n" +
            "\"version\",\n" +
            "\"extract_tool\",\n" +
            "\"extract_cycle\",\n" +
            "\"security\",\n" +
            "\"security_cycle\",\n" +
            "\"importance\",\n" +
            "\"description\",\n" +
            "\"updater\",\n" +
            "\"creator\",\n" +
            "\"status\",\n" +
            "\"annex_id\",\n" +
            "\"bo_name\",\n" +
            "\"bo_department_name\",\n" +
            "\"bo_email\",\n" +
            "\"bo_tel\",\n" +
            "\"to_name\",\n" +
            "\"to_department_name\",\n" +
            "\"to_email\",\n" +
            "\"to_tel\",\n" +
            "\"technical_leader\",\n" +
            "\"business_leader\",\n" +
            "\"tenant_id\",\n" +
            "\"update_time\",\n" +
            "\"record_time\",\n" +
            "\"create_time\",\n" +
            "\"modify_time\",\n" +
            "\"approve_group_id\" \n" +
            ")\n" +
            "VALUES\n" +
            "(\n" +
            "#{dip.id},\n" +
            "#{dip.categoryId},\n" +
            "#{dip.databaseId},\n" +
            "#{dip.databaseAlias},\n" +
            "#{dip.planningPackageCode},\n" +
            "#{dip.planningPackageName},\n" +
            "0,\n" +
            "#{dip.extractTool},\n" +
            "#{dip.extractCycle},\n" +
            "#{dip.security},\n" +
            "#{dip.securityCycle}',\n" +
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
            "NOW( ),\n")
    void insertDatabaseInfo(@Param("dip") DatabaseInfoPO databaseInfoPO);

    @Select("SELECT\n" +
            "s.id ,\n" +
            "c.name AS categoryName,\n" +
            "c.guid AS categoryId,\n" +
            "db.database_name AS databaseName,\n" +
            "db.database_guid  AS databaseId,\n" +
            "ds.source_type AS databaseTypeName,\n" +
            "ds.database AS databaseInstanceName,\n" +
            "ds.source_name AS dataSourceName,\n" +
            "ds.source_id AS dataSourceId,\n" +
            "s.database_alias,\n" +
            "s.planning_package_name,\n" +
            "s.planning_package_code,\n" +
            "s.extract_tool,\n" +
            "s.extract_cycle,\n" +
            "s.security,\n" +
            "s.security_cycle,\n" +
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
            "(SELECT u.username FROM users u WHERE u.userid = s.technical_leader ) AS technicalLeader,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.business_leader ) AS business_leader,\n" +
            "ag.name AS approve_group_name,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = s.updater ) AS updaterName,\n" +
            "s.update_time,\n" +
            "ag.name AS approveGroupName,\n" +
            "ag.id AS approveGroupId,\n" +
            "ai.reason as audit_des ,\n" +
            "(SELECT u.username FROM users u WHERE u.userid = ai.approver ) AS auditorName\n" +
            "FROM\n" +
            "source_info s LEFT JOIN category c ON s.category_id = c.guid AND c.tenantid = s.tenant_id\n" +
            "LEFT JOIN db_info db ON s.database_id = db.database_guid \n" +
            "LEFT JOIN source_db sd ON sd.db_guid = db.database_guid \n" +
            "LEFT JOIN data_source ds ON sd.source_id = ds.source_id\n" +
            "LEFT JOIN approval_group ag ON s.approve_group_id = ag.\"id\"\n" +
            "LEFT JOIN approval_item ai ON s.approve_id = ai.\"id\"\n" +
            "WHERE\n" +
            "s.id = #{id} AND s.version = #{version}\n")
    DatabaseInfoBO getDatabaseInfoById(@Param("id") String id,@Param("tenantId") String tenantId,@Param("version") int version);

    @Select("<script>"+
            "SELECT id,database_alias " +
            "FROM " +
            " source_info "+
            "WHERE\n" +
            " id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>" +
            " AND " +
            " version = 0"+
            "</script>")
    List<DatabaseInfo> getDatabaseIdAndAliasByIds(@Param("ids") List<String> idList);

    @Update("<script>" +
            "UPDATE source_info \n" +
            "SET status = #{status} \n" +
            "WHERE\n" +
            " id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>"+
            " AND " +
            " version = 0"+
            "</script>")
    void updateStatusByIds(@Param("ids") List<String> idList,@Param("status") String status);

    @Update("UPDATE source_info \n" +
            "SET approve_id = #{approveId},approve_group_id = #{approveGroupId} \n" +
            "WHERE\n" +
            " id = #{id} " +
            " AND " +
            " version = 0")
    void updateApproveIdAndApproveGroupIdById(@Param("id") String id,@Param("approveId") String approveId,@Param("approveGroupId") String approveGroupId);

    @Select("<script>" +
            "SELECT\n" +
            " s.id ,\n" +
            " c.name AS categoryName,\n" +
            " db.database_name AS databaseName,\n" +
            " ds.source_type AS databaseTypeName,\n" +
            " s.database_alias,\n" +
            " s.security,\n" +
            " (SELECT u.username FROM users u WHERE u.userid = s.updater ) AS updater_name,\n" +
            " s.update_time,\n" +
            " ai.reason as audit_des ,\n" +
            " (SELECT u.username FROM users u WHERE u.userid = ai.approver ) AS auditor_name\n" +
            "FROM\n" +
            " source_info s LEFT JOIN category c ON s.category_id = c.guid AND \n" +
            " c.tenantid = s.tenant_id\n" +
            " LEFT JOIN db_info db ON s.database_id = db.database_guid \n" +
            " LEFT JOIN source_db sd ON sd.db_guid = db.database_guid \n" +
            " LEFT JOIN data_source ds ON sd.source_id = ds.source_id\n" +
            " LEFT JOIN approval_group ag ON s.approve_group_id = ag.\"id\"\n" +
            " LEFT JOIN approval_item ai ON s.approve_id = ai.\"id\"\n" +
            "WHERE\n" +
            " s.tenant_id = #{tenantId}\n" +
            " AND " +
            " s.version = 0 "+
            "<if>" +
            " AND " +
            "  s.status = #{status}" +
            "</if>" +
            "<if>" +
            " AND " +
            " (s.database_alias like '%#{name}%' OR db.database_name like '%#{name}%')" +
            "</if>" +
            " ORDER BY s.update_time DESC,s.database_alias\n" +
            " LIMIT #{limit} " +
            " OFFSET #{offset}" +
            "</script>")
    List<DatabaseInfoForList> getDatabaseInfoList(@Param("tenantId") String tenantId, @Param("status") String status,
                                                  @Param("name") String name,@Param("offset")  int offset,@Param("limit") int limit);

    @Select("<script>" +
            "SELECT\n" +
            " COUNT(1)"+
            "FROM\n" +
            " source_info s LEFT JOIN category c ON s.category_id = c.guid AND \n" +
            " c.tenantid = s.tenant_id\n" +
            " LEFT JOIN db_info db ON s.database_id = db.database_guid \n" +
            " LEFT JOIN source_db sd ON sd.db_guid = db.database_guid \n" +
            " LEFT JOIN data_source ds ON sd.source_id = ds.source_id\n" +
            " LEFT JOIN approval_group ag ON s.approve_group_id = ag.\"id\"\n" +
            " LEFT JOIN approval_item ai ON s.approve_id = ai.\"id\"\n" +
            "WHERE\n" +
            " s.tenant_id = #{tenantId}\n" +
            " AND " +
            " version = 0 "+
            "<if>" +
            " AND" +
            "  s.status = #{status}" +
            "</if>" +
            "<if>" +
            "AND" +
            "  AND\n" +
            " (s.database_alias like '%#{name}%' OR db.database_name like '%#{name}%')" +
            "</if>" +
            "</script>")
    int getDatabaseInfoListCount(@Param("tenantId") String tenantId, @Param("status") String status,@Param("name") String name);

    @Select("<script>" +
            "SELECT\n" +
            " s.ID,\n" +
            " s.database_alias AS NAME,\n" +
            " sirc.parent_category_id \n" +
            "FROM\n" +
            " source_info s\n" +
            " LEFT JOIN source_info_relation2parent_category sirc ON s.\"id\" = sirc.source_info_id \n" +
            "WHERE\n" +
            " s.ID IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>"+
            " AND " +
            " version = 0"+
            "</script>")
    List<DatabaseInfoForCategory> getDatabaseInfoByIds(@Param("ids")List<String> idList);


    @Select("<script>" +
            "INSERT INTO source_info (\n" +
            " SELECT\n" +
            "  \"id\",\n" +
            "  category_id,\n" +
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
            "  update_time,\n" +
            "  record_time,\n" +
            "  create_time,\n" +
            "  modify_time,\n" +
            "  ( VERSION + 1 ) AS VERSION \n" +
            " FROM\n" +
            "  source_info \n" +
            " WHERE\n" +
            "  \"id\" IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>"+
            " AND \"version\" = 0 \n" +
            " )" +
            "</script>")
    void insertHistoryVersion(@Param("ids") List<String> idList);

    @Update("UPDATE source_info SET category_id = #{categoryId} WHERE id  = #{id} AND version = 0")
    void updateRealCategoryRelation(@Param("id")String sourceInfoId,@Param("categoryId")String categoryId);

    @Delete("<script>" +
            "DELETE source_info_relation2parent_category " +
            "   WHERE source_info_id IN " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>"+
            "</script>")
    void deleteSourceInfoAndParentCategoryRelation(@Param("ids") List<String> idList);

    @Select("<script>" +
            "SELECT\n" +
            " status \n" +
            "FROM\n" +
            " source_info \n" +
            "WHERE\n" +
            " ID IN \n" +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>"+
            "#{id}"+
            "</foreach>"+
            " AND \"version\" = 0 \n" +
            "GROUP BY\n" +
            " status" +
            "</script>")
    List<String> getStatusByIdList(@Param("ids") List<String> idList);

    @Update("")
    void updateSourceInfo(DatabaseInfo databaseInfo);
}
