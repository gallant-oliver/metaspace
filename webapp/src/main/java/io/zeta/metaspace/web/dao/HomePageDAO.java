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

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    @Insert("insert into statistical(statisticalid,date,statistical,staticaltypeid) values(#{id},#{date},#{statistical},#{type})")
    public int addStatistical(@Param("id") String id, @Param("date") long date, @Param("statistical") long statistical, @Param("type") int type);

    @Select("select date,statistical from statistical where date<=#{endDate} and date>=#{startDate} and statisticaltypeid=#{type} order by date" )
    public List<> getStatisticalByDateType(@Param("startDate")long startDate,@Param("endDate")long endDate,@Param("type")int type);
}
