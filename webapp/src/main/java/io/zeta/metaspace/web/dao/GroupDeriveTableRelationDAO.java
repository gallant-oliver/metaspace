package io.zeta.metaspace.web.dao;

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
}
