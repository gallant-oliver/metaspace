package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.enums.PrivilegeType;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableInfo;
import io.zeta.metaspace.model.sourceinfo.derivetable.relation.GroupDeriveTableRelation;
import io.zeta.metaspace.model.sourceinfo.derivetable.vo.DeriveTableVersion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * 衍生表权限信息表 Mapper 接口
 * </p>
 *
 * @author wuqianhe
 * @since 2021-10-11
 */
@Mapper
public interface GroupDeriveTableRelationDAO {
    @Select("<script>" +
            "SELECT " +
            " id AS groupTableRelationId," +
            " derive_table_id AS deriveTableId," +
            " user_group_id AS userGroupId," +
            " security_privilege AS securityPrivilege," +
            " importance_privilege AS importancePrivilege " +
            " FROM group_table_relation WHERE derive_table_id = #{tableId} " +
            "AND user_group_id IN " +
            " <foreach item='id' index='index' collection='userGroupIds' separator=',' open='(' close=')'>" +
            "#{id}" +
            " </foreach>" +
            " AND tenant_id = #{tenantId}" +
            " </script>")
    GroupDeriveTableRelation getByTableIdAndGroups(@Param("tableId") String tableId,
                                                   @Param("userGroupIds") List<String> userGroups,
                                                   @Param("tenantId")String tenantId);

    @Update("UPDATE group_table_relation \n" +
            "SET importance_privilege = NULL \n" +
            "WHERE\n" +
            " user_group_id = #{userGroupId} AND tenant_id = #{tenantId}")
    void updateImportancePrivilegeNullByUserGroupId(@Param("userGroupId") String userGroupId, @Param("tenantId")String tenantId);

    @Update("UPDATE group_table_relation \n" +
            "SET security_privilege = NULL \n" +
            "WHERE\n" +
            " user_group_id = #{userGroupId} AND tenant_id = #{tenantId}")
    void updateSecurityPrivilegeNullByUserGroupId(@Param("userGroupId") String userGroupId, @Param("tenantId")String tenantId);

    @Update("<script>" +
            "INSERT INTO group_table_relation ( ID, derive_table_id, importance_privilege, user_group_id, tenant_id )\n" +
            "VALUES\n" +
            " <foreach item='relation' index='index' collection='relations' separator=',' open='(' close=')'>" +
            " ( #{ relation.id }, #{ relation.deriveTableId }, TRUE, #{ relation.userGroupId }, #{ relation.tenantId } ) ON conflict ( user_group_id, derive_table_id, tenant_id ) " +
            "</foreach>" +
            "DO\n" +
            "UPDATE \n" +
            " SET importance_privilege = excluded.importance_privilege" +
            "</script>")
    void updateDeriveTableImportancePrivilege(@Param("relations") List<GroupDeriveTableRelation> relationList);

    @Update("<script>" +
            "INSERT INTO group_table_relation ( ID, derive_table_id, security_privilege, user_group_id, tenant_id )\n" +
            "VALUES\n" +
            " <foreach item='relation' index='index' collection='relations' separator=',' open='(' close=')'>" +
            " ( #{ relation.id }, #{ relation.deriveTableId }, TRUE, #{ relation.userGroupId }, #{ relation.tenantId } ) ON conflict ( user_group_id, derive_table_id, tenant_id ) " +
            "</foreach>" +
            "DO\n" +
            "UPDATE \n" +
            " SET security_privilege = excluded.security_privilege" +
            "</script>")
    void updateDeriveTableSecurityPrivilege(@Param("relations") List<GroupDeriveTableRelation> relationList);
    
    @Delete("<script>" +
            "DELETE \n" +
            "FROM\n" +
            " group_table_relation \n" +
            "WHERE\n" +
            " id IN " +
            " <foreach item='id' index='ids' collection='relations' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteRelation(@Param("ids") List<String> ids);

    @Select("<script>" +
            "SELECT " +
            "count(*) over() AS total,\n" +
            " ti.tableguid AS tableId," +
            " ti.tablename AS tableNameEn,\n" +
            "  sidti.table_name_zh AS tableNameZn,\n" +
            " gtr.importance_privilege AS importancePrivilege,\n" +
            " gtr.security_privilege AS securityPrivilege,\n" +
            " gtr.\"id\" AS groupTableRelationId,\n" +
            " bi.name AS businessObjectName,\n" +
            " c.name AS businessCategoryName\n" +
            "FROM\n" +
            " tableinfo ti \n" +
            " INNER JOIN source_info_derive_table_info sidti ON sidti.table_name_en = ti.tablename AND sidti.db_id = ti.databaseguid \n" +
            " LEFT JOIN business2table bt ON bt.tableguid = ti.tableguid\n" +
            " LEFT JOIN businessinfo bi ON bi.businessid = bt.businessid\n" +
            " LEFT JOIN business_relation br ON br.businessid = bi.businessid\n" +
            " LEFT JOIN category c ON c.guid = br.categoryguid\n" +
            " LEFT JOIN group_table_relation gtr ON gtr.derive_table_id = ti.tableguid AND  gtr.user_group_id = #{userGroupId} \n" +
            "<if test='privilegeType == \"IMPORTANCE\" AND registerType == false'>" +
            "   AND gtr.importance_privilege != true OR gtr.importance_privilege IS NULL\n" +
            "</if>"+
            "<if test='privilegeType == \"IMPORTANCE\" AND registerType == true'>" +
            "   AND gtr.importance_privilege = true \n" +
            "</if>"+
            "<if test='privilegeType == \"SECURITY\" AND registerType == false'>" +
            "   AND gtr.security_privilege != true OR gtr.security_privilege IS NULL\n" +
            "</if>"+
            "<if test='privilegeType == \"SECURITY\" AND registerType  == true'>" +
            "   AND gtr.security_privilege = true \n" +
            "</if>"+
            "<if test='privilegeType == \"ALL\" AND registerType == true'>" +
            "   AND gtr.security_privilege = true OR gtr.importance_privilege = true\n" +
            "</if>"+
            "WHERE\n" +
            " sidti.tenant_id = #{tenantId} \n" +
            " AND sidti.table_name_zh like concat('%',#{tableName},'%') ESCAPE '/'\n" +
            "<if test = 'limit &gt; 0'>"+
            " LIMIT #{limit} OFFSET #{offset}\n" +
            "</if>"+
            "</script>")
    List<GroupDeriveTableInfo> getRelationInfos(@Param("tenantId") String tenantId, @Param("privilegeType") String privilegeType,
                                                @Param("userGroupId") String userGroupId, @Param("registerType") Boolean registerType,
                                                @Param("tableName") String tableName,@Param("limit")  int limit,@Param("offset")  int offset);
}
