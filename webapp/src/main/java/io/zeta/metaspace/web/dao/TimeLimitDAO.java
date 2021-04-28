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
import io.zeta.metaspace.model.timelimit.TimeLimitRelation;
import io.zeta.metaspace.model.timelimit.TimeLimitSearch;
import io.zeta.metaspace.model.timelimit.TimelimitEntity;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/*
 * @description
 * @author
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

    @Insert({"insert into time_limit(id,name,mark,description,grade,start_time,end_time,creator,updater,state,version,delete,create_time,update_time,tenantid,publisher,approveid,time_type,time_range) ",
            " values(#{timeLimit.id},#{timeLimit.name},#{timeLimit.mark},#{timeLimit.description},#{timeLimit.grade},#{timeLimit.startTime},#{timeLimit.endTime},#{timeLimit.creator},#{timeLimit.updater},#{timeLimit.state},1,#{timeLimit.delete},now(),now(),#{tenantId},null,#{timeLimit.approveId},#{timeLimit.timeType},#{timeLimit.timeRange})"})
    int addTimeLimit(@Param("timeLimit") TimelimitEntity timeLimit, @Param("tenantId") String tenantId);

    @Delete("<script>" +
            "delete from time_limit where id in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    public void deleteTimeLimit(@Param("ids")List<String> ids);
    @Select("<script>" +
            "select * from time_limit where tenantid=#{tenantId} and id in " +
            "<foreach item='id' index='index' collection='ids' " +
            "open='(' separator=',' close=')'>" +
            " #{id} " +
            "</foreach>" +
            "</script>")
    List<TimelimitEntity> getTimeLimitByIds(@Param("ids")List<String> ids, @Param("tenantId")String tenantId);

    @Select({" select id from time_limit where tenantid=#{tenantId} and name=#{timeLimitName}"})
    List<String> getTimeLimitByName(@Param("timeLimitName") String timeLimitName, @Param("tenantId") String tenantId);


    @Select({" select * from time_limit where tenantid=#{tenantId} and id=#{id}"})
    TimelimitEntity getTimeLimitById(@Param("id") String id, @Param("tenantId") String tenantId);
    /**
     * 获取任务列表
     *
     * @param params   查询参数
     * @param tenantId 租户ID
     * @param params
     * @return
     */
    @Select({"<script>",
            " select count(*)over() total,time_limit.id,time_limit.name,time_limit.mark,time_limit.description,time_limit.state,time_limit.update_time as updateTime,c.username as updater,time_limit.create_time as createTime,b.username as creator,",
            " time_limit.approveId as approveId,time_limit.start_time as startTime,time_limit.end_time as endTime,time_limit.grade as grade,time_limit.version as version,time_limit.time_type as timeType,time_limit.time_range as timeRange",
            " from time_limit",
            " join (select id ,max(version) as version from time_limit ",
            " group by id) a on time_limit.id = a.id and time_limit.version = a.version join users c on c.userid = time_limit.updater join users b on b.userid = time_limit.creator where time_limit.tenantid = #{tenantId}",
            " <if test='params.startTime!=null and params.endTime!=null'>",
            "    and time_limit.update_time between #{params.startTime} and #{params.endTime} ",
            " </if>",
            " <if test='params.status!=null and params.status.size()>0'>",
            "    and time_limit.state in",
            " <foreach item='item' index='index' collection='params.status'",
            " open='(' separator=',' close=')'>",
            " #{item}",
            " </foreach>",
            " </if>",
            " <if test=\"params.query != null and params.query!=''\">",
            "    and (time_limit.name like '%${params.query}%' ESCAPE '/' or time_limit.mark like '%${params.query}%' ESCAPE '/')",
            " </if>",
            " <if test=\"params.sortby!=null and params.sortby!='' \">",
            " order by ${params.sortby} ",
            " <if test=\"params.order!='' and params.order!=null\">",
            " ${params.order} ",
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


    /**
     * 获取发布历史列表
     *
     * @param
     * @param tenantId 租户ID
     * @param
     * @return
     */
    @Select({"<script>",
            " select count(*)over() total,time_limit.id,time_limit.name,time_limit.mark,time_limit.state,time_limit.update_time as updateTime,time_limit.update_time as publishTime,user.username as publisher,",
            " time_limit.approveId as approveId,time_limit.start_time as startTime,time_limit.end_time as endTime,time_limit.grade as grade,time_limit.version as version",
            " from time_limit",
            " left join users on users.userid = time_limit.publisher",
            " where time_limit.state = '3' time_limit.id = #{params.id} and time_limit.tenantid = #{params.tenantId} ",
            " <if test=\"params.query != null and params.query!=''\">",
            " and time_limit.name like '%${params.query}%' ESCAPE '/'",
            " </if>",
            " order by version desc",
            " <if test='params.limit!=null and params.limit!= -1'>",
            " limit #{params.limit}",
            " </if>",
            " <if test='params.offset!=null'>",
            " offset #{params.offset}",
            " </if>",
            " </script>"})
    List<TimelimitEntity> getTimeLimitHistory(@Param("params") TimeLimitSearch params, @Param("tenantId") String tenantId);


    @Select({"<script>",
            "select  count(*) over () total, b.name as name, a.index_name as indexName,users.username as interfaceUser  from",
            " (select a.index_name as index_name ,a.time_limit_id ,a.business_leader as business_leader  from index_derive_info a inner join (select index_id,max(version) as version from index_derive_info where tenant_id = #{tenantId} and time_limit_id = #{params.id} group by index_id) b on a.index_id = b.index_id and a.version = b.version) a ",
            " inner join users on users.userid = a.business_leader ",
            " inner join (select id,name from time_limit where id = #{params.id} and tenantid = #{tenantId}",
                    " <if test=\"params.query != null and params.query!=''\">",
                       " and name like '%${params.query}%' ESCAPE '/' or time_limit.mark like '%${params.query}%' ESCAPE '/'",
                    " </if>",
            ") b on a.time_limit_id = b.id ",
            " <if test=\"params.sortBy!=null and params.sortBy!='' \">",
            " order by ${params.sortBy} ",
            " <if test=\"params.order!='' and params.order!=null\">",
            " ${params.order} ",
            " </if>",
            " </if>",
            " <if test='params.limit!=null and params.limit!= -1'>",
            " limit #{params.limit}",
            " </if>",
            " <if test='params.offset!=null'>",
            " offset #{params.offset}",
            " </if>",
            " </script>"})
    List<TimeLimitRelation> getTimeLimitRelations(@Param("params") TimeLimitSearch params, @Param("tenantId") String tenantId);



    @Select("select count(*) from time_limit where name=#{name} and id!=#{id} and tenantid=#{tenantId}")
    public Integer isNameById(@Param("tenantId") String tenantId, @Param("name") String name, @Param("id") String id);



    @Update({"<script>" +
            "UPDATE time_limit " +
            "SET  name=#{timeLimit.name},mark=#{timeLimit.mark},update_time=now(),updater=#{timeLimit.updater},grade=#{timeLimit.grade},start_time=#{timeLimit.startTime},end_time=#{timeLimit.endTime},description=#{timeLimit.description},approveId=#{timeLimit.approveId},time_type=#{timeLimit.timeType},time_range=#{timeLimit.timeRange} " +
            "WHERE id=#{timeLimit.id} and version = #{timeLimit.version} and tenantid=#{tenantId} " +
            "</script>"})
    int updateTimeLimit(@Param("timeLimit") TimelimitEntity timeLimit, @Param("tenantId") String tenantId);


}
