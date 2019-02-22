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
 * @date 2019/2/12 14:57
 */
package io.zeta.metaspace.web.dao;

import io.zeta.metaspace.model.business.BusinessInfo;
import io.zeta.metaspace.model.business.BusinessInfoHeader;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 14:57
 */
public interface BusinessDAO {

    @Insert("insert into businessinfo(departmentid,businessid,name,module,description,owner,manager,maintainer,dataassets,submitter,submissionTime,businessOperator,businessLastUpdate,ticketNumber)" +
            "values(#{departmentId},#{businessId},#{name},#{module},#{description},#{owner},#{manager},#{maintainer},#{dataAssets},#{submitter},#{submissionTime},#{businessOperator},#{businessLastUpdate},#{ticketNumber})")
    public int insertBusinessInfo(BusinessInfo info);

    @Update("update businessinfo set name=#{name},module=#{module},description=#{description},owner=#{owner},manager=#{manager}," +
            "maintainer=#{maintainer},dataAssets=#{dataAssets},businessOperator=#{businessOperator},businessLastUpdate=#{businessLastUpdate} where businessId=#{businessId}")
    public int updateBusinessInfo(BusinessInfo info);

    @Update("update businessinfo set businessstatus=#{status} where businessId=#{businessId}")
    public int updateBusinessStatus(@Param("businessId")String businessId, @Param("status")int status);

    @Update("update businessinfo set technicalstatus=#{status} where businessId=#{businessId}")
    public int updateTechnicalStatus(@Param("businessId")String businessId, @Param("status")int status);

    @Select("select * from businessinfo where departmentId=#{departmentId}")
    public List<BusinessInfo> queryBusinessByDemparmentId(@Param("departmentId")String departmentId,  @Param("limit")int limit,@Param("offset") int offset);

    @Select("select * from businessinfo where businessId=#{businessId}")
    public BusinessInfo queryBusinessByBusinessId(@Param("businessId")String businessId);

    @Select("select * from tableInfo where tableGuid in(select tableGuid from business2table where businessId=#{businessId})")
    public List<BusinessInfo.Table> queryTablesByBusinessId(@Param("businessId")String businessId);

    /*@Select("select * from businessInfo where  businessId like '%${businessId}%' and  name like '%${name}%' and businessOperator like '%${businessOperator}%'")
    public List<BusinessInfo> queryBusinessByCondition(@Param("businessId")String businessId, @Param("name")String businessName, @Param("department")String department, @Param("businessOperator")String businessOperator);*/

    @Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime from businessInfo where businessId in (select businessId from business_relation where categoryGuid=#{categoryGuid}) and name like '%${businessName}%' limit #{limit} offset #{offset}")
    public List<BusinessInfoHeader> queryBusinessByNameWithLimit(@Param("categoryGuid")String categoryGuid, @Param("businessName")String businessName, @Param("limit")int limit, @Param("offset") int offset);

    @Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime from businessInfo where businessId in (select businessId from business_relation where categoryGuid=#{categoryGuid}) and name like '%${businessName}%'")
    public List<BusinessInfoHeader> queryBusinessByName(@Param("categoryGuid")String categoryGuid, @Param("businessName")String businessName);

    @Select("select departmentId from businessInfo where businessId = #{businessId}")
    public String queryCategoryIdByBusinessId(@Param("businessId")String businessId);

    @Select("select name from category where categoryGuid=#{categoryGuid}")
    public String queryCategoryNameById(@Param("categoryGuid")String categoryGuid);

    @Select("select count(*) from businessInfo where businessId in (select businessId from business_relation where categoryGuid=#{categoryGuid}) and name like '%${businessName}%'")
    public long queryBusinessCountByName(@Param("categoryGuid")String categoryGuid, @Param("businessName")String businessName);

    @Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber from businessInfo where businessId in (select businessId from business_relation where categoryGuid in (select guid from category where name like '%${level2Category}%' and categorytype=1))" +
            "and technicalStatus=#{status} and businessId like '%${ticketNumber}%' and submitter like '%${submitter}%' limit #{limit} offset #{offset}")
    public List<BusinessInfoHeader> queryBusinessByConditionWithLimit(@Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                                       @Param("level2Category") String level2Category,@Param("submitter") String submitter,@Param("limit")int limit,@Param("offset") int offset);

    @Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber from businessInfo where businessId in (select businessId from business_relation where categoryGuid in (select guid from category where name like '%${level2Category}%' and categorytype=1))" +
            "and technicalStatus=#{status} and businessId like '%${ticketNumber}%' and submitter like '%${submitter}%'")
    public List<BusinessInfoHeader> queryBusinessByCondition(@Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                                                @Param("level2Category") String level2Category,@Param("submitter") String submitter);

    @Select("select count(*) from businessInfo where businessId in (select businessId from business_relation where categoryGuid in (select guid from category where name like '%${level2Category}%' and categorytype=1))" +
            "and technicalStatus=#{status} and businessId like '%${ticketNumber}%' and submitter like '%${submitter}%'")
    public long queryBusinessCountByCondition(@Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                              @Param("level2Category") String level2Category,@Param("submitter") String submitter);

    @Select("select * from businessInfo where businessId in (select businessId from business_relation where categoryId=#{categoryGuid} limit #{limit} offset #{offset})")
    public List<BusinessInfo> queryBusinessByCatetoryIdWithLimit(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit,@Param("offset") int offset);

    @Update("update businessInfo set technicalOperator=#{technicalOperator},technicalLastUpdate=#{technicalLastUpdate} where businessId=#{businessId}")
    public int updateTechnicalInfo(@Param("businessId")String businessId, @Param("technicalOperator")String technicalOperator, @Param("technicalLastUpdate")String technicalLastUpdate);

    @Delete("delete from business2table where businessId=#{businessId}")
    public int deleteRelationByBusinessId(@Param("businessId")String businessId);

    @Insert("insert into business2table(businessId, tableGuid)values(#{businessId}, #{tableGuid})")
    public int insertTableRelation(@Param("businessId")String businessId, @Param("tableGuid")String tableId);
}
