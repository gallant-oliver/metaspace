package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.ip.restriction.IpRestriction;
import io.zeta.metaspace.model.ip.restriction.IpRestrictionType;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.share.ApiPolyInfo;
import io.zeta.metaspace.web.typeHandler.ListStringTypeHandler;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumTypeHandler;

import java.util.List;

public interface IpRestrictionDAO {
    @Select("select count(*) from ip_restriction where name=#{name} and id != #{id} and tenant_id = #{tenantId}")
    int countByName(@Param("id") String id, @Param("name") String name, @Param("tenantId") String tenantId);


    @Insert("insert into  ip_restriction(id,name,description,type,ip_list,enable,tenant_id,creator_id,create_time,update_time)  " +
            "values(#{ipRestriction.id},#{ipRestriction.name},#{ipRestriction.description},#{ipRestriction.type,typeHandler=org.apache.ibatis.type.EnumTypeHandler},#{ipRestriction.ipList,typeHandler=io.zeta.metaspace.web.typeHandler.ListStringTypeHandler},#{ipRestriction.enable},#{tenantId},#{ipRestriction.creatorId},now(),now())")
    int insert(@Param("ipRestriction") IpRestriction ipRestriction, @Param("tenantId") String tenantId);


    @Update("update ip_restriction set name=#{ipRestriction.name}, " +
            "description =#{ipRestriction.description}, " +
            "type=#{ipRestriction.type,typeHandler=org.apache.ibatis.type.EnumTypeHandler}, " +
            "ip_list=#{ipRestriction.ipList,typeHandler=io.zeta.metaspace.web.typeHandler.ListStringTypeHandler}, " +
            "enable=#{ipRestriction.enable} " +
            "where id=#{ipRestriction.id} ")
    int update(@Param("ipRestriction") IpRestriction ipRestriction);

    @Update("update ip_restriction set enable = #{enable},update_time=now() where id=#{id} ")
    int updateEnable(@Param("id") String id, @Param("enable") boolean enable);


    @Delete({"<script>",
            "delete from ip_restriction where id in ",
            "<foreach collection='ids' item='itemId' index='index' separator=',' open='(' close=')'>",
            "#{itemId}",
            "</foreach>",
            "</script>"})
    int delete(@Param("ids") List<String> ids);


    @Results(id = "base", value = {
            @Result(property = "type", column = "type", typeHandler = EnumTypeHandler.class, javaType = IpRestrictionType.class),
            @Result(property = "ipList", column = "ip_list", typeHandler = ListStringTypeHandler.class)
    })
    @Select("select * from ip_restriction where id = #{id}")
    IpRestriction getIpRestriction(@Param("id") String id);

    @ResultMap("base")
    @Select({"<script>",
            "select count(*) over() as total, t.* ,u.username as creator from ip_restriction  t  left join users u on u.userid = t.creator_id where  tenant_id =#{tenantId} ",
            "<if test='enable!=null'>",
            "and enable = #{enable} ",
            "</if>",
            "<if test='parameters.query!=null'>",
            "and name like concat('%',#{parameters.query},'%') ESCAPE '/' ",
            "</if>",
            "<if test='type!=null'>",
            "and type=#{type,typeHandler=org.apache.ibatis.type.EnumTypeHandler} ",
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
    List<IpRestriction> getIpRestrictions(@Param("parameters") Parameters parameters, @Param("enable") Boolean enable,@Param("type")IpRestrictionType type, @Param("tenantId") String tenantId);


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
                    "       t.createtime\n" +
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
                    "  AND t.poly::jsonb -> 'ipRestriction' -> 'type' is NOT NULL\n" +
                    "  AND t.poly::jsonb -> 'ipRestriction' -> 'ipRestrictionIds' ?? #{id}\n",
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
    List<ApiPolyInfo> getApiPolyInfoList(@Param("id") String id, @Param("userId") String userId, @Param("tenantId") String tenantId, @Param("param") Parameters parameters, @Param("status") String status, @Param("effectiveTime") long effectiveTime);

    @Select("<script>" +
            "select name from ip_restriction where id in " +
            "<foreach collection='ids' item='id' index='index' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<String> getIpRestrictionNames(@Param("ids")List<String> ids);
}
