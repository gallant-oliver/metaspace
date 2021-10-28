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
    List<GroupDeriveTableRelation> getByTableIdAndGroups(@Param("tableId") String tableId,
                                                   @Param("userGroupIds") List<String> userGroups,
                                                   @Param("tenantId")String tenantId);

    @Update("UPDATE group_table_relation  " +
            "SET importance_privilege = NULL  " +
            "WHERE " +
            " user_group_id = #{userGroupId} AND tenant_id = #{tenantId}")
    void updateImportancePrivilegeNullByUserGroupId(@Param("userGroupId") String userGroupId, @Param("tenantId")String tenantId);

    @Update("UPDATE group_table_relation  " +
            "SET security_privilege = NULL  " +
            "WHERE " +
            " user_group_id = #{userGroupId} AND tenant_id = #{tenantId}")
    void updateSecurityPrivilegeNullByUserGroupId(@Param("userGroupId") String userGroupId, @Param("tenantId")String tenantId);

    @Update("<script>" +
            "INSERT INTO group_table_relation ( ID, derive_table_id, importance_privilege, user_group_id, tenant_id ) " +
            "VALUES " +
            " <foreach item='relation' index='index' collection='relations' separator=','>" +
            " ( #{ relation.groupTableRelationId }, #{ relation.deriveTableId }, TRUE, #{ relation.userGroupId }, #{ relation.tenantId } )" +
            "</foreach>" +
            "ON conflict ( user_group_id, derive_table_id, tenant_id ) " +
            "DO " +
            "UPDATE  " +
            " SET ID = excluded.ID, importance_privilege = excluded.importance_privilege" +
            "</script>")
    void updateDeriveTableImportancePrivilege(@Param("relations") List<GroupDeriveTableRelation> relationList);

    @Update("<script>" +
            "INSERT INTO group_table_relation ( ID, derive_table_id, security_privilege, user_group_id, tenant_id ) " +
            "VALUES " +
            " <foreach item='relation' index='index' collection='relations' separator=','>" +
            " ( #{ relation.groupTableRelationId }, #{ relation.deriveTableId }, TRUE, #{ relation.userGroupId }, #{ relation.tenantId } ) " +
            "</foreach>" +
            " ON conflict ( user_group_id, derive_table_id, tenant_id ) " +
            "DO " +
            "UPDATE  " +
            " SET ID = excluded.ID, security_privilege = excluded.security_privilege" +
            "</script>")
    void updateDeriveTableSecurityPrivilege(@Param("relations") List<GroupDeriveTableRelation> relationList);
    
    @Delete("<script>" +
            "DELETE  " +
            "FROM " +
            " group_table_relation  " +
            "WHERE " +
            " id IN " +
            " <foreach item='id' index='ids' collection='ids' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void deleteRelation(@Param("ids") List<String> ids);

    @Delete("<script>" +
            "DELETE  " +
            "FROM " +
            " group_table_relation  " +
            "WHERE " +
            " user_group_id = #{id}" +
            "</script>")
    void deleteRelationByGroupId(@Param("id") String groupId);

    @Select("<script>" +
            "SELECT DISTINCT (derive_table_id)  FROM group_table_relation WHERE user_group_id = #{id} AND " +
            "<if test='type == \"IMPORTANCE\"'>" +
            "   importance_privilege = true  " +
            "</if>"+
            "<if test='type == \"SECURITY\"' >" +
            "   security_privilege = true " +
            "</if>"+
            "</script>")
    List<String> selectTableIdByGroupId(@Param("id") String groupId,@Param("type")String type);

    @Select("<script>" +
            "SELECT " +
            "count(*) over() AS total, " +
            " ti.tablename AS tableNameEn, " +
            " sidti.table_name_zh AS tableNameZn, " +
            " gtr.importance_privilege AS importancePrivilege, " +
            " gtr.security_privilege AS securityPrivilege, " +
            " gtr.\"id\" AS groupTableRelationId, " +
            " string_agg(bi.name,',') AS businessObjectName, " +
            " string_agg(c.name,',') AS businessCategoryName, " +
            " ti.tableguid AS tableId " +
            "FROM " +
            " tableinfo ti  " +
            " INNER JOIN source_info_derive_table_info sidti ON sidti.table_guid = ti.tableguid AND sidti.version = -1 " +
            "<if test='privilegeType == \"IMPORTANCE\"'>" +
            "   AND sidti.importance = true  " +
            "</if>"+
            "<if test='privilegeType == \"SECURITY\"' >" +
            "   AND sidti.security = true " +
            "</if>"+
            "   AND (sidti.security = true OR sidti.importance = true  )" +
            " LEFT JOIN business2table bt ON bt.tableguid = ti.tableguid " +
            " LEFT JOIN businessinfo bi ON bi.businessid = bt.businessid " +
            " LEFT JOIN business_relation br ON br.businessid = bi.businessid " +
            " LEFT JOIN category c ON c.guid = br.categoryguid " +
            " LEFT JOIN group_table_relation gtr ON gtr.derive_table_id = ti.tableguid " +
            "WHERE " +
            " sidti.tenant_id = #{tenantId}  " +
            "<if test='privilegeType == \"IMPORTANCE\" and registerType == false'>" +
            "   AND( ( (gtr.importance_privilege = false OR gtr.importance_privilege is null )  AND  gtr.user_group_id = #{userGroupId}   )" +
            "  OR ( " +
            "<if test='importantList != null and importantList.size() != 0'>" +
            "gtr.derive_table_id NOT IN " +
            " <foreach item='id' index='ids' collection='importantList' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND  " +
            "</if>" +
            " (gtr.user_group_id != #{userGroupId} OR gtr.user_group_id is NULL)))"+
            "</if>"+
            "<if test='privilegeType == \"IMPORTANCE\" and registerType == true'>" +
            "   AND gtr.importance_privilege = true  AND  gtr.user_group_id = #{userGroupId}  " +
            "</if>"+
            "<if test='privilegeType == \"SECURITY\" and registerType == false'>" +
            "   AND( ( (gtr.security_privilege = false OR gtr.security_privilege is null )  AND  gtr.user_group_id = #{userGroupId}   )" +
            "  OR ( " +
            "<if test='securityList != null and securityList.size() != 0'>" +
            "gtr.derive_table_id NOT IN " +
            " <foreach item='id' index='ids' collection='securityList' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND  " +
            "</if>" +
            "(gtr.user_group_id != #{userGroupId} OR gtr.user_group_id is NULL)))"+
            "</if>"+
            "<if test='privilegeType == \"SECURITY\" and registerType  == true'>" +
            "   AND gtr.security_privilege = true  AND  gtr.user_group_id = #{userGroupId}   " +
            "</if>"+
            "<if test='privilegeType == \"ALL\" and registerType == true'>" +
            "   AND (gtr.security_privilege = true OR gtr.importance_privilege = true)  AND  gtr.user_group_id = #{userGroupId}  " +
            "</if>"+
            "<if test='tableName != null and tableName !=\"\"'>" +
            " AND (sidti.table_name_zh like '%'||#{tableName}||'%' ESCAPE '/' OR sidti.table_name_en like '%'||#{tableName}||'%' ESCAPE '/' )" +
            "</if>" +
            "GROUP BY ti.tableguid,gtr.\"id\", sidti.table_name_zh,gtr.importance_privilege,gtr.security_privilege " +
            "<if test = 'limit &gt; 0'>"+
            " LIMIT #{limit} OFFSET #{offset} " +
            "</if>"+
            "</script>")
    List<GroupDeriveTableInfo> getRelationInfos(@Param("tenantId") String tenantId, @Param("privilegeType") String privilegeType,@Param("importantList") List<String> importantList,@Param("securityList") List<String> securityList,
                                                @Param("userGroupId") String userGroupId, @Param("registerType") Boolean registerType,
                                                @Param("tableName") String tableName,@Param("limit")  int limit,@Param("offset")  int offset);
    @Update("<script>" +
            "UPDATE   " +
            " group_table_relation  " +
            " SET importance_privilege = null " +
            "WHERE " +
            " derive_table_id = #{tableId} AND tenant_id=#{tenantId}" +
            "</script>")
    void deleteRelationImportantPrivilegeByTableId(@Param("tenantId") String tenantId,@Param("tableId") String tableGuid);

    @Update("<script>" +
            "UPDATE   " +
            " group_table_relation  " +
            " SET security_privilege = null " +
            "WHERE " +
            " derive_table_id = #{tableId} AND tenant_id=#{tenantId}" +
            "</script>")
    void deleteRelationSecurityPrivilegeByTableId(@Param("tenantId") String tenantId,@Param("tableId") String tableGuid);
}
