// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/7/24 9:53
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.dataquality2.*;
import io.zeta.metaspace.model.metadata.Column;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.metadata.Table;
import io.zeta.metaspace.model.share.ApiAudit;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.model.timelimit.TimelimitEntity;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/24 9:53
 */
public interface TimeLimitDAO {

    /**
     * 添加时间限定
     *
     * @param timeLimit
     * @param tenantId
     * @return
     */

    @Insert({" insert into timelimit(id,name,desc,grade,start_time,end_time,creator,updater,state,version,delete,create_time,update_time,tenantid，publisher,approveid) ",
            " values(#{timeLimit.id},#{timeLimit.name},#{timeLimit.desc},#{timeLimit.grade},#{timeLimit.startTime},#{timeLimit.endTime},#{timeLimit.creator},#{timeLimit.updater},#{timeLimit.state},(select (case when max(version) is null then 0 else max(version) end)+1 from timelimit where tenantid=#{tenantId}),#{timeLimit.delete},#{timeLimit.createTime},#{timeLimit.updateTime},#{tenantId},null,#{timeLimit.approveId})"})
    int addTimeLimit(@Param("timeLimit") TimelimitEntity timeLimit, @Param("tenantId") String tenantId);

    @Delete("<script>" +
            "delete from timelimit where id in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public void deleteTimeLimit(@Param("ids")List<String> ids);


    @Select({" select id from timelimit where tenantid=#{tenantId} and name=#{timeLimitName}"})
    List<String> getTimeLimitByName(@Param("timeLimitName") String timeLimitName, @Param("tenantId") String tenantId);

    /**
     * 获取任务列表
     *
     * @param params   查询参数
     * @param tenantId 租户ID
     * @param params
     * @return
     */
    @Select({"<script>",
            " select count(*)over() total,timelimit.id,timelimit.name,timelimit.mark,timelimit.status,timelimit.update_time as updateTime,user.username,",
            " timelimit.approveId as approveId,timelimit.start_time as startTime,timelimit.end_time as endTime,timelimit.grade as grade,timelimit.version as version",
            " from timelimit",
            " join (select id ,max(version) as version from timelimit where tenantid = #{tenantId}",
            " <if test='params.startTime!=null and params.endTime!=null'>",
            " and update_time between #{params.startTime} and #{params.endTime} ",
            " </if>",
            " group by id) a on timelimit.id = a.id and timelimit.version = a.version join users on users.userid = timelimit.updater where",
            " <if test='params.status!=null and params.status.size()>0'>",
            " timelimit.status in",
            " <foreach item='item' index='index' collection='params.status'",
            " open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " </if>",
            " <if test=\"param.query != null and param.query!=''\">",
            " timelimit.name like '%${params.query}%' ESCAPE '/'",
            " </if>",
            " <if test=\"param.sortby!=null and param.sortby!='' \">",
            " order by g.${param.sortby} ",
            " <if test=\"param.order!='' and param.order!=null\">",
            " ${param.order} ",
            " </if>",
            " </if>",
            " <if test='params.limit!=null and params.limit!= -1'>",
            " limit #{params.limit}",
            " </if>",
            " <if test='params.offset!=null'>",
            " offset #{params.offset}",
            " </if>",
            " </script>"})
    List<TimelimitEntity> getTimeLimitList(@Param("params") TimeLimitSearch params, @Param("tenantId") String tenantId);


    @Update({"<script>" +
            "UPDATE timelimit" +
            "SET  name=#{timeLimit.name},mark=#{timeLimit.mark},update_time=now(),updater=#{timeLimit.updater},grade=#{timeLimit.grade},start_time = #{timeLimit.startTime} ,end_time=#{timeLimit.endTime},desc=#{timeLimit.desc}" +
            "WHERE id=#{timeLimit.id} and version = #{timeLimit.version} and tenant_id=#{tenantId} " +
            "</script>"})
    int updateTimeLimit(@Param("timeLimit") TimelimitEntity timeLimit, @Param("tenantId") String tenantId);

}
