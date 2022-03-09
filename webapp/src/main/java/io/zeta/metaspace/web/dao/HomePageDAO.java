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
 * @date 2019/3/4 9:57
 */
package io.zeta.metaspace.web.dao;


import io.zeta.metaspace.model.homepage.*;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:57
 */
public interface HomePageDAO {
    @Select("select count(1) from businessinfo where technicalstatus=1 and businessstatus=1 and tenantid=#{tenantId}")
    public long getAddedBusinessCount(@Param("tenantId")String tenantId);

    @Select("select count(1) from businessinfo where tenantid=#{tenantId} and technicalstatus!=1 or businessstatus!=1 ")
    public long getNoAddedBusinessCount(@Param("tenantId")String tenantId);

    @Select("select count(1) from businessinfo where tenantid=#{tenantId}")
    public long getBusinessCount(@Param("tenantId")String tenantId);

    @Select({" <script>",
            " select tableInfo.tableGuid,tableInfo.tableName,tableInfo.display_name as displayName, count(*) as times from business2Table",
            " join tableInfo on business2table.tableGuid=tableInfo.tableGuid join businessinfo on businessinfo.businessid=business2Table.businessid " +
            " where businessinfo.tenantid=#{tenantId}" +
            " group by tableInfo.tableGuid,tableInfo.tableName",
            " order by times desc",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<TableUseInfo> getTableRelatedInfo(@Param("limit") int limit, @Param("offset") int offset,@Param("tenantId")String tenantId);

    @Select("select count(DISTINCT businessinfo.businessId) as totalBusiness,count(distinct tableGuid) as totalTable from business2table join businessinfo on businessinfo.businessid=business2Table.businessid " +
            "where businessinfo.tenantid=#{tenantId}")
    public Map<String, Long> getTotalInfo(@Param("tenantId")String tenantId);

    @Select("select count(*) from businessInfo where technicalStatus=#{technicalStatus} and tenantid=#{tenantId}")
    public long getTechnicalStatusNumber(@Param("technicalStatus") int type,@Param("tenantId")String tenantId);


    @Insert("insert into statistical(statisticalid,date,statistical,statisticaltypeid,tenantid) values(#{id},#{date},#{statistical},#{type},#{tenantId})")
    public int addStatistical(@Param("id") String id, @Param("date") long date, @Param("statistical") long statistical, @Param("type") int type,@Param("tenantId") String tenantId);

    @Select("select date,statistical from statistical where date<=#{endDate} and date>=#{startDate} and statisticaltypeid=#{type} and tenantid=#{tenantId} order by date desc")
    public List<DateStatistical> getStatisticalByDateType(@Param("startDate") long startDate, @Param("endDate") long endDate, @Param("type") int type,@Param("tenantId")String tenantId);


    @Select("select statistical from statistical where date=#{date} and statisticaltypeid=#{type}")
    public long getStatistical(@Param("date") long date, @Param("type") int type);


    @Select({" <script>",
            " select D.guid,D.level2name as name,D.level3count as logicDBTotal,COALESCE(E.level4count,0) as entityDBTotal from",
            " (select guid,level2name,count(level3name) as level3count from",
            " (select A.guid as guid,A.name as level2name,B.name as level3name from category A left join category B on B.parentCategoryGuid=A.guid where A.parentCategoryGuid=#{guid} and A.tenantid=#{tenantId}) C GROUP BY C.level2name,C.guid) D",
            " left JOIN",
            " (select grandParentGuid,count(*) as level4count from category JOIN ",
            " (select  B.guid as parentGuid, A.guid as grandParentGuid from category A left join category B on B.parentCategoryGuid=A.guid where A.parentCategoryGuid=#{guid} and A.tenantid=#{tenantId}) C ON parentCategoryGuid = C.parentGuid GROUP BY grandParentGuid) E on D.guid = E.grandParentGuid",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<CategoryDBInfo> getCategoryRelatedDBCount(@Param("guid") String guid, @Param("limit") int limit, @Param("offset") int offset,@Param("tenantId")String tenantId);

    @Select("select count(*) from category where parentCategoryGuid=#{guid} and tenantid=#{tenantId}")
    public long getCountCategory(@Param("guid") String guid,@Param("tenantId")String tenantId);

    @Select({" <script>",
            " select A.name,A.guid,count(B.guid) as entityDBTotal from category B right join (SELECT * from category WHERE parentCategoryGuid=#{guid} and tenantid=#{tenantId}) A on B.parentCategoryGuid=A.guid and B.tenantid=#{tenantId} GROUP BY A.guid,A.name",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<CategoryDBInfo> getChildSystemDBCount(@Param("guid") String guid, @Param("limit") int limit, @Param("offset") int offset,@Param("tenantId") String tenantId);

    @Select("select count(1) from category where parentcategoryguid=#{parentGuid} and tenantid=#{tenantId}")
    public long getSubSystemTotal(@Param("parentGuid") String parentGuid,@Param("tenantId")String tenantId);

    @Delete("delete  from statistical where date=#{date} and tenantid=#{tenantId}")
    public int deleteStatistical(@Param("date") long date,@Param("tenantId")String tenantId);

    @Select("SELECT DISTINCT " +
            " pros.ID, " +
            " pros.NAME, " +
            " pros.upApi upApiNum, " +
            " pros.notUpApi notUpApiNum, " +
            " pros.createtime  " +
            "FROM " +
            " ( " +
            " SELECT DISTINCT P " +
            "  .ID, " +
            "  P.NAME, " +
            "  ugr.user_id, " +
            "  P.manager, " +
            "  P.tenantid, " +
            "  ( " +
            "  SELECT COUNT " +
            "   ( 1 )  " +
            "  FROM " +
            "   ( SELECT DISTINCT ap.guid FROM api ap WHERE P.ID = ap.projectid AND status = 'up' ) b  " +
            "  ) upApi, " +
            "  ( " +
            "  SELECT COUNT " +
            "   ( 1 )  " +
            "  FROM " +
            "   ( SELECT DISTINCT ap.guid FROM api ap WHERE P.ID = ap.projectid AND status != 'up' ) b  " +
            "  ) notUpApi, " +
            "  P.createtime  " +
            " FROM " +
            "  project " +
            "  P JOIN project_group_relation pug ON pug.project_id = P. " +
            "  ID LEFT JOIN user_group_relation ugr ON ugr.group_id = pug.group_id  " +
            " WHERE " +
            "  P.VALID = 't' UNION ALL " +
            " SELECT " +
            "  pro.ID, " +
            "  pro.NAME, " +
            "  NULL user_id, " +
            "  manager, " +
            "  pro.tenantid, " +
            "  ( " +
            "  SELECT COUNT " +
            "   ( 1 )  " +
            "  FROM " +
            "   ( SELECT DISTINCT ap.guid FROM api ap WHERE pro.ID = ap.projectid AND status = 'up' ) b  " +
            "  ) upApi, " +
            "  ( " +
            "  SELECT COUNT " +
            "   ( 1 )  " +
            "  FROM " +
            "   ( SELECT DISTINCT ap.guid FROM api ap WHERE pro.ID = ap.projectid AND status != 'up' ) b  " +
            "  ) notUpApi, " +
            "  pro.createtime  " +
            " FROM " +
            "  project pro  " +
            " WHERE " +
            "  pro.VALID = 't'  " +
            " ) pros  " +
            "WHERE " +
            " pros.tenantid = 'a788801785844f71b3009a53595afb26'  " +
            " AND ( pros.user_id = '13ce6b26-5064-4560-8074-ba74c279c0f9' OR pros.manager = '13ce6b26-5064-4560-8074-ba74c279c0f9' )  " +
            "ORDER BY " +
            " pros.createtime DESC")
    public List<HomeProjectInfo> getProjectInfo(@Param("tenantId") String tenantId, @Param("userId") String userId,
                                                @Param("limit") long limit,@Param("offset") long offset);

    @Select("SELECT   count(distinct pros.name) total  " +
            "FROM  " +
            "   (  " +
            "   SELECT DISTINCT   " +
            "      P.ID,  " +
            "      P.\"name\",  " +
            "      ugr.user_id,  " +
            "      P.manager,  " +
            "      P.tenantid,  " +
            "      ( SELECT COUNT ( 1 ) FROM api WHERE P.ID = projectid AND status = 'up' ) upApi,  " +
            "      ( SELECT COUNT ( 1 ) FROM api WHERE P.ID = projectid AND status != 'up' ) notUpApi,  " +
            "      p.createtime  " +
            "   FROM  " +
            "      \"project\" P   " +
            "      JOIN project_group_relation pug ON pug.project_id = P.ID   " +
            "      LEFT JOIN user_group_relation ugr ON ugr.group_id = pug.group_id   " +
            "   WHERE  " +
            "      P.\"valid\" = 't' UNION ALL  " +
            "   SELECT  " +
            "      pro.ID,  " +
            "      pro.\"name\",  " +
            "      NULL user_id,  " +
            "      manager,  " +
            "      pro.tenantid,  " +
            "      ( SELECT COUNT ( 1 ) FROM api WHERE pro.ID = projectid AND status = 'up' ) upApi,  " +
            "      ( SELECT COUNT ( 1 ) FROM api WHERE pro.ID = projectid AND status != 'up' ) notUpApi,  " +
            "      pro.createtime  " +
            "   FROM  " +
            "      project pro   " +
            "   WHERE  " +
            "      pro.\"valid\" = 't'   " +
            "   ) pros   " +
            "WHERE  " +
            "   pros.tenantid = #{tenantId}  " +
            "   AND ( pros.user_id = #{userId} OR pros.manager = #{userId} )")
    public long getProjectCount(@Param("tenantId") String tenantId, @Param("userId") String userId);

    @Select("SELECT SUM " +
            " ( cute.red_warning_count ) redCount, " +
            " SUM ( cute.orange_warning_count ) orangeCount, " +
            " SUM ( cute.general_warning_count ) generalCount  " +
            "FROM " +
            " \"data_quality_task_execute\" cute " +
            " INNER JOIN data_quality_task task ON cute.task_id = task.\"id\"  " +
            "WHERE " +
            " task.tenantid = #{tenantId}")
    public HomeTaskInfo getTaskInfo(@Param("tenantId") String tenantId);
}
