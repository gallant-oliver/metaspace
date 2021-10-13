package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.enums.PrivilegeType;
import io.zeta.metaspace.model.sourceinfo.derivetable.pojo.SourceInfoDeriveTableInfo;
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
}
