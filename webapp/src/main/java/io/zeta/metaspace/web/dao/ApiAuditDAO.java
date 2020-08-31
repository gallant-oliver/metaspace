package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ApiAuditDAO {

    /**
     * API 上线的创建审核记录
     *
     * @param apiAudit
     * @param tenantId
     * @return
     */
    @Insert({"<script> " +
            "INSERT INTO api_audit ( id,api_guid,api_version,applicant,applicant_name,status,reason,tenant_id )\n" +
            "VALUES (" +
            "    #{audit.id},\n" +
            "    #{audit.apiGuid},\n" +
            "    #{audit.apiVersion},\n" +
            "    #{audit.applicant},\n" +
            "    #{audit.applicantName},\n" +
            "    #{audit.status},\n" +
            "    #{audit.reason},\n" +
            "    #{tenantId}\n" +
            "    );\n" +
            "</script>"})
    int insertApiAudit(@Param("audit") ApiAudit apiAudit, @Param("tenantId") String tenantId);


    @Update({"<script>" +
            "UPDATE api_audit " +
            "SET  status=#{audit.status},reason=#{audit.reason},update_time=now(),updater=#{audit.updater} " +
            "WHERE id=#{audit.id} and tenant_id=#{tenantId} " +
            "</script>"})
    int updateApiAudit(@Param("audit") ApiAudit apiAudit, @Param("tenantId") String tenantId);

    @Update({"<script>" +
            "UPDATE api_audit " +
            "SET  status='CANCEL',update_time=now(),updater=#{updater}  " +
            "WHERE api_guid = #{apiGuid} and api_version = #{apiVersion} and status = 'NEW' and tenant_id=#{tenantId} " +
            "</script>"})
    int cancelApiAudit(@Param("updater") String updater, @Param("apiGuid") String apiGuid, @Param("apiVersion") String apiVersion, @Param("tenantId") String tenantId);

    @Update({"<script>" +
            "UPDATE api_audit " +
            "SET  status='CANCEL',update_time=now(),updater=#{updater}  " +
            "WHERE id =#{auditId} and tenant_id=#{tenantId} " +
            "</script>"})
    int cancelApiAuditById(@Param("updater") String updater, @Param("auditId") String auditId, @Param("tenantId") String tenantId);


    @Select({"<script> " +
            "SELECT  count(1)over() total,audit.*,api.name as api_name\n" +
            "FROM api_audit as audit " +
            "LEFT JOIN api ON audit.api_guid = api.guid and audit.api_version = api.version " +
            "WHERE audit.tenant_id=#{tenantId} " +
            "<if test='statuses !=null and statuses.size() > 0'> " +
            "AND audit.status In" +
            "<foreach item='item' collection='statuses' separator=',' open='(' close=')'> " +
            "  #{item,typeHandler=org.apache.ibatis.type.EnumTypeHandler} " +
            "</foreach> " +
            "</if>" +
            " <if test='applicant != null'> " +
            " AND audit.applicant=#{applicant} " +
            " </if>" +
            "<if test=\"param.query!=null and param.query!=''\"> " +
            " AND  audit.applicant_name like '%${param.query}%' ESCAPE '/' " +
            "</if>" +
            "order by audit.update_time desc " +
            "<if test='param.limit!=-1'> " +
            " limit ${param.limit} " +
            "</if>" +
            "<if test='param.offset!=0'> " +
            " offset ${param.offset} " +
            "</if>" +
            "</script>"})
    List<ApiAudit> getApiAuditList(@Param("tenantId") String tenantId,
                                   @Param("param") Parameters parameters,
                                   @Param("statuses") List<AuditStatusEnum> statuses,
                                   @Param("applicant") String applicant);

    @Select({"<script>" +
            "SELECT * FROM api_audit WHERE id=#{id} and tenant_id=#{tenantId}" +
            "</script>"
    })
    ApiAudit getApiAuditById(@Param("id") String id, @Param("tenantId") String tenantId);
}

