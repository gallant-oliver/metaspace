package io.zeta.metaspace.web.dao.sourceinfo;

import io.zeta.metaspace.model.po.sourceinfo.DatabaseInfoPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseInfoDAO {
    @Select("SELECT COUNT(1) FROM db_info WHERE database_guid = #{databaseId}")
    int getDatabaseById(@Param("databaseId")String databaseId);

    @Insert("INSERT INTO \"public\".\"source_info\" (\n" +
            "\"id\",\n" +
            "\"category_id\",\n" +
            "\"database_id\",\n" +
            "\"database_alias\",\n" +
            "\"planning_package_code\",\n" +
            "\"planning_package_name\",\n" +
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
}
