package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.desensitization.DesensitizationAlgorithm;
import io.zeta.metaspace.model.desensitization.DesensitizationRule;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiPolyInfo;
import io.zeta.metaspace.web.typeHandler.ListStringTypeHandler;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumTypeHandler;

import java.util.List;

public interface DesensitizationDAO {


    @Select("select count(*) from desensitization_rule where name=#{name} and id != #{id} and tenant_id = #{tenantId}")
    int countByName(@Param("id") String id, @Param("name") String name, @Param("tenantId") String tenantId);


    @Insert("insert into  desensitization_rule(id,name,description,type,params,enable,tenant_id,creator_id,create_time,update_time)  " +
            "values(#{rule.id},#{rule.name},#{rule.description},#{rule.type,typeHandler=org.apache.ibatis.type.EnumTypeHandler},#{rule.params,typeHandler=io.zeta.metaspace.web.typeHandler.ListStringTypeHandler},#{rule.enable},#{tenantId},#{rule.creatorId},now(),now())")
    int insert( @Param("rule") DesensitizationRule rule, @Param("tenantId") String tenantId);


    @Update("update desensitization_rule set name=#{rule.name}, description =#{rule.description}, type=#{rule.type,typeHandler=org.apache.ibatis.type.EnumTypeHandler}, params=#{rule.params,typeHandler=io.zeta.metaspace.web.typeHandler.ListStringTypeHandler}, enable=#{rule.enable} where id=#{rule.id} ")
    int update(@Param("rule") DesensitizationRule rule);

    @Update("update desensitization_rule set enable = #{enable},update_time=now() where id=#{id} ")
    int updateEnable(@Param("id") String id, @Param("enable") boolean enable);


    @Delete({"<script>",
            "delete from desensitization_rule where id in ",
            "<foreach collection='ids' item='itemId' index='index' separator=',' open='(' close=')'>",
            "#{itemId}",
            "</foreach>",
            "</script>"})
    int delete(@Param("ids") List<String> ids);


    @Results(id = "base", value = {
            @Result(property = "type", column = "type", typeHandler = EnumTypeHandler.class, javaType = DesensitizationAlgorithm.class),
            @Result(property = "params", column = "params", typeHandler = ListStringTypeHandler.class)
    })
    @Select("select * from desensitization_rule where id = #{id}")
    DesensitizationRule getRule(@Param("id") String id);

    @ResultMap("base")
    @Select({"<script>",
            "select count(*) over() as total, t.*,u.username as creator from desensitization_rule  t  left join users u on u.userid = t.creator_id where  tenant_id =#{tenantId}  ",
            "<if test='enable!=null'>",
            "and enable = #{enable} ",
            "</if>",
            "<if test='parameters.query!=null'>",
            "and name like concat('%',#{parameters.query},'%')  ESCAPE '/' ",
            "</if>",
            "order by update_time desc",
            "<if test='parameters.limit!=-1'>",
            "limit ${parameters.limit} ",
            "</if>",
            "<if test='parameters.offset!=0'>",
            "offset ${parameters.offset}",
            "</if>",
            "</script>"
    })
    List<DesensitizationRule> getRules(@Param("parameters") Parameters parameters, @Param("enable") Boolean enable, @Param("tenantId") String tenantId);


    /**
     * 获取使用相关规则的API 并且 API 是用户有权限的
     */
    @Select({"<script>",
            "SELECT count(*) over() as total,\n" +
                    "       t.guid,\n" +
                    "       t.name,\n" +
                    "       t.version,\n" +
                    "       t.description,\n" +
                    "       t.projectid,\n" +
                    "       p.name                                                    as project_name,\n" +
                    "       t.status,\n" +
                    "       u.username                                                as creator_name,\n" +
                    "       t.createtime,\n" +
                    "       (SELECT string_agg(field, ';')\n" +
                    "        FROM jsonb_to_recordset(T.poly :: jsonb -> 'desensitization') AS (field text, \"ruleId\" TEXT)\n" +
                    "        where \"ruleId\" = #{ruleId}) as desensitization_fields\n" +
                    "FROM (\n" +
                    "         SELECT api.*,\n" +
                    "                COALESCE(\n" +
                    "                        (\n" +
                    "                            SELECT api_poly.poly\n" +
                    "                            FROM api_poly\n" +
                    "                            WHERE api_poly.api_id = api.guid\n" +
                    "                              AND api_poly.api_version = api.VERSION\n" +
                    "                              AND api_poly.status = 'AGREE'\n" +
                    "                              AND (now() - update_time > INTERVAL '1 second' * #{effectiveTime})\n" +
                    "                            ORDER BY update_time DESC\n" +
                    "                            LIMIT 1\n" +
                    "                        ),\n" +
                    "                        api.api_poly_entity\n" +
                    "                    ) AS poly -- 查找当前API生效的策略\n" +
                    "         FROM api\n" +
                    "         WHERE VALID\n" +
                    "           AND status != 'draft'\n" +
                    "           AND status != 'audit'\n" +
                    "           AND api.tenantid = #{tenantId}\n" +
                    "           AND projectid IN (  -- 筛选登陆用户有权限的项目\n" +
                    "             SELECT project.\"id\"\n" +
                    "             FROM project\n" +
                    "             WHERE project.tenantid = #{tenantId}\n" +
                    "               AND project.\"valid\"\n" +
                    "               AND (\n" +
                    "                     project.manager = #{userId} OR project.\"id\" IN (SELECT pg.project_id\n" +
                    "                                                                FROM project_group_relation pg\n" +
                    "                                                                         LEFT JOIN user_group_relation ug ON pg.group_id = ug.group_id\n" +
                    "                                                                where ug.user_id = #{userId})\n" +
                    "                 )\n" +
                    "         )\n" +
                    "     ) T\n" +
                    "         left join users u on u.userid = t.creator\n" +
                    "         left join project p on p.id = t.projectid\n" +
                    "WHERE T.poly IS NOT NULL\n" +
                    "  AND #{ruleId} IN  -- 判断API策略包含指定脱敏规则\n" +
                    "      (SELECT * FROM jsonb_to_recordset(T.poly :: jsonb -> 'desensitization') AS (\"ruleId\" TEXT))\n",
            " <if test=\"status!=null and status!=''\">",
            " and t.status=#{status}",
            " </if>",
            " order by t.updatetime desc ",
            " <if test='param.limit != null and param.limit!=-1'>",
            " limit #{param.limit}",
            " </if>",
            " <if test='param.offset != null'>",
            " offset #{param.offset}",
            " </if>",
            " </script>"})
    List<ApiPolyInfo> getApiPolyInfoList(@Param("ruleId") String ruleId, @Param("userId") String userId, @Param("tenantId") String tenantId, @Param("param") Parameters parameters, @Param("status") String status, @Param("effectiveTime") long effectiveTime);

}
