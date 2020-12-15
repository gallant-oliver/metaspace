package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.share.ApiPoly;
import io.zeta.metaspace.model.share.AuditStatusEnum;
import io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.EnumTypeHandler;

/**
 * Api 策略，该表主要用于保存历史策略和用于审核记录显示
 */
public interface ApiPolyDao {

    //创建策略

    @Insert("insert into api_poly(id,api_id,api_version,poly,status,create_time,update_time)" +
            "values(#{apiPoly.id},#{apiPoly.apiId},#{apiPoly.apiVersion}," +
            "#{apiPoly.poly, typeHandler=io.zeta.metaspace.web.typeHandler.ApiPolyEntityTypeHandler}," +
            "#{apiPoly.status,typeHandler=org.apache.ibatis.type.EnumTypeHandler}," +
            "#{apiPoly.createTime},#{apiPoly.updateTime})")
    int insert(@Param("apiPoly") ApiPoly apiPoly);

    //更新策略的更新时间、状态
    @Update("update api_poly set status=#{status,typeHandler=org.apache.ibatis.type.EnumTypeHandler},update_time = now() where id = #{id}")
    int updateStatus(@Param("id") String apiPolyId, @Param("status") AuditStatusEnum status);

    //获取策略详情
    @Results(id = "base", value = {
            @Result(property = "status", column = "status", typeHandler = EnumTypeHandler.class, javaType = AuditStatusEnum.class),
            @Result(property = "poly", column = "poly", typeHandler = ApiPolyEntityTypeHandler.class)
    })
    @Select("select * from api_poly where id = #{id}")
    ApiPoly getApiPoly(@Param("id") String id);


    //获取指定状态的策略数目
    @Select("select count(1) from api_poly where  api_id = #{apiId} and api_version=#{apiVersion} and status=#{status,typeHandler=org.apache.ibatis.type.EnumTypeHandler}")
    long countApiPolyByStatus(@Param("apiId") String apiId, @Param("apiVersion") String apiVersion, @Param("status") AuditStatusEnum status);

    //获取生效策略
    @ResultMap("base")
    @Select("select * from api_poly where api_id = #{apiId} and api_version=#{apiVersion} and status='AGREE' and ( now() - update_time > interval '1 second' * #{effectiveTime}) order by update_time desc limit 1")
    ApiPoly getEffectiveApiPoly(@Param("apiId") String apiId, @Param("apiVersion") String apiVersion, @Param("effectiveTime") long effectiveTime);

}
