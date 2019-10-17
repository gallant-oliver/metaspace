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


import io.zeta.metaspace.model.homepage.CategoryDBInfo;
import io.zeta.metaspace.model.homepage.DateStatistical;
import io.zeta.metaspace.model.homepage.RoleUseInfo;
import io.zeta.metaspace.model.homepage.TableUseInfo;
import io.zeta.metaspace.model.role.Role;
import io.zeta.metaspace.model.user.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/4 9:57
 */
public interface HomePageDAO {
    @Select("select count(1) from businessinfo where technicalstatus=1 and businessstatus=1")
    public long getAddedBusinessCount();

    @Select("select count(1) from businessinfo where technicalstatus!=1 or businessstatus!=1")
    public long getNoAddedBusinessCount();

    @Select("select count(1) from businessinfo")
    public long getBusinessCount();

    @Select({" <script>",
            " select tableInfo.tableGuid,tableInfo.tableName,tableInfo.display_name as displayName, count(*) as times from business2Table",
            " join tableInfo on business2table.tableGuid=tableInfo.tableGuid group by tableInfo.tableGuid,tableInfo.tableName",
            " order by times desc",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<TableUseInfo> getTableRelatedInfo(@Param("limit") int limit, @Param("offset") int offset);

    //@Select("SELECT sum(total) from (select count(*) as total from role join users on role.roleId=users.roleId group by role.roleId) A")
    @Select("select count(DISTINCT businessId) from business2table")
    public long getTotalTableUserTimes();

    @Select("SELECT count(distinct tableGuid) from  business2Table")
    public long getCountBusinessRelatedTable();

    @Select("select count(*) from businessInfo where technicalStatus=#{technicalStatus}")
    public long getTechnicalStatusNumber(@Param("technicalStatus") int type);


    @Insert("insert into statistical(statisticalid,date,statistical,statisticaltypeid) values(#{id},#{date},#{statistical},#{type})")
    public int addStatistical(@Param("id") String id, @Param("date") long date, @Param("statistical") long statistical, @Param("type") int type);

    @Select("select date,statistical from statistical where date<=#{endDate} and date>=#{startDate} and statisticaltypeid=#{type} order by date desc")
    public List<DateStatistical> getStatisticalByDateType(@Param("startDate") long startDate, @Param("endDate") long endDate, @Param("type") int type);


    @Select("select statistical from statistical where date=#{date} and statisticaltypeid=#{type}")
    public long getStatistical(@Param("date") long date, @Param("type") int type);


    @Select({" <script>",
            " select D.guid,D.level2name as name,D.level3count as logicDBTotal,COALESCE(E.level4count,0) as entityDBTotal from",
            " (select guid,level2name,count(level3name) as level3count from",
            " (select A.guid as guid,A.name as level2name,B.name as level3name from category A left join category B on B.parentCategoryGuid=A.guid where A.parentCategoryGuid=#{guid}) C GROUP BY C.level2name,C.guid) D",
            " left JOIN",
            " (select grandParentGuid,count(*) as level4count from category JOIN ",
            " (select  B.guid as parentGuid, A.guid as grandParentGuid from category A left join category B on B.parentCategoryGuid=A.guid where A.parentCategoryGuid=#{guid}) C ON parentCategoryGuid = C.parentGuid GROUP BY grandParentGuid) E on D.guid = E.grandParentGuid",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<CategoryDBInfo> getCategoryRelatedDBCount(@Param("guid") String guid, @Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(*) from category where parentCategoryGuid=#{guid}")
    public long getCountCategory(@Param("guid") String guid);

    @Select({" <script>",
            " select A.name,A.guid,count(B.guid) as entityDBTotal from category B right join (SELECT * from category WHERE parentCategoryGuid=#{guid}) A on B.parentCategoryGuid=A.guid GROUP BY A.guid,A.name",
            " <if test='limit!= -1'>",
            " limit #{limit}",
            " </if>",
            " offset #{offset}",
            " </script>"})
    public List<CategoryDBInfo> getChildSystemDBCount(@Param("guid") String guid, @Param("limit") int limit, @Param("offset") int offset);

    @Select("select count(1) from category where parentcategoryguid=#{parentGuid}")
    public long getSubSystemTotal(String parentGuid);

    @Delete("delete  from statistical where date=#{date} ")
    public int deleteStatistical(long date);
}
