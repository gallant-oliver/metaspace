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
import io.zeta.metaspace.model.business.TechnologyInfo;
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

    //添加业务信息
    @Insert("insert into businessinfo(departmentid,businessid,name,module,description,owner,manager,maintainer,dataassets,submitter,submissionTime,businessOperator,businessLastUpdate,ticketNumber)" +
            "values(#{departmentId},#{businessId},#{name},#{module},#{description},#{owner},#{manager},#{maintainer},#{dataAssets},#{submitter},#{submissionTime},#{businessOperator},#{businessLastUpdate},#{ticketNumber})")
    public int insertBusinessInfo(BusinessInfo info);

    //更新业务信息
    @Update("update businessinfo set name=#{name},module=#{module},description=#{description},owner=#{owner},manager=#{manager}," +
            "maintainer=#{maintainer},dataAssets=#{dataAssets},businessOperator=#{businessOperator},businessLastUpdate=#{businessLastUpdate} where businessId=#{businessId}")
    public int updateBusinessInfo(BusinessInfo info);

    //更新业务信息补充状态
    @Update("update businessinfo set businessstatus=#{status} where businessId=#{businessId}")
    public int updateBusinessStatus(@Param("businessId")String businessId, @Param("status")int status);

    //更新技术信息补充状态
    @Update("update businessinfo set technicalstatus=#{status} where businessId=#{businessId}")
    public int updateTechnicalStatus(@Param("businessId")String businessId, @Param("status")int status);

    //查询业务目录关联的业务信息列表
    @Select("select * from businessinfo where departmentId=#{departmentId}")
    public List<BusinessInfo> queryBusinessByDemparmentId(@Param("departmentId")String departmentId,  @Param("limit")int limit,@Param("offset") int offset);

    //查询业务信息详情
    @Select("select * from businessinfo where businessId=#{businessId}")
    public BusinessInfo queryBusinessByBusinessId(@Param("businessId")String businessId);

    @Select("select technicalLastUpdate,technicalOperator from businessinfo where businessId=#{businessId}")
    public TechnologyInfo queryTechnologyInfoByBusinessId(@Param("businessId")String businessId);

    //查询业务信息关联的数据库表
    @Select("select * from tableInfo where tableGuid in(select tableGuid from business2table where businessId=#{businessId})")
    public List<TechnologyInfo.Table> queryTablesByBusinessId(@Param("businessId")String businessId);

    //根据业务信息名称查询列表
    @Select({"<script>",
             " select businessInfo.businessId,businessInfo.name,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber, business_relation.categoryGuid from businessInfo",
             " join business_relation on",
             " business_relation.businessId=businessInfo.businessId",
             " where",
             " businessInfo.name like '%${businessName}%'",
             " and",
             " categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    //@Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber from businessInfo where businessId in (select businessId from business_relation where categoryGuid=#{categoryGuid}) and name like '%${businessName}%' limit #{limit} offset #{offset}")
    public List<BusinessInfoHeader> queryBusinessByName(@Param("businessName")String businessName, @Param("ids") List<String> categoryIds, @Param("limit")int limit, @Param("offset") int offset);

    //查询业务信息所属目录Id
    @Select("select departmentId from businessInfo where businessId = #{businessId}")
    public String queryCategoryIdByBusinessId(@Param("businessId")String businessId);

    //根据业务信息名称查询列表总数
    @Select({"<script>",
             " select count(*) from businessInfo",
             " join business_relation on",
             " business_relation.businessId=businessInfo.businessId",
             " where",
             " businessInfo.name like '%${businessName}%'",
             " and",
             " categoryGuid in",
             " <foreach item='categoryGuid' index='index' collection='ids' separator=',' open='(' close=')'>" ,
             " #{categoryGuid}",
             " </foreach>",
             " </script>"})
    public long queryBusinessCountByName(@Param("businessName")String businessName, @Param("ids") List<String> categoryIds);

    //多条件分页查询业务信息列表
    @Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber from businessInfo where businessId in (select businessId from business_relation where categoryGuid in (select guid from category where guid like '%${level2CategoryId}%' and categorytype=1))" +
            "and technicalStatus=#{status} and ticketNumber like '%${ticketNumber}%' and submitter like '%${submitter}%' limit #{limit} offset #{offset}")
    public List<BusinessInfoHeader> queryBusinessByConditionWithLimit(@Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                                       @Param("level2CategoryId") String level2Category,@Param("submitter") String submitter,@Param("limit")int limit,@Param("offset") int offset);
    //多条件查询业务信息列表
    @Select("select businessId,name,businessStatus,technicalStatus,submitter,submissionTime,ticketNumber from businessInfo where businessId in (select businessId from business_relation where categoryGuid in (select guid from category where guid like '%${level2CategoryId}%' and categorytype=1))" +
            "and technicalStatus=#{status} and ticketNumber like '%${ticketNumber}%' and submitter like '%${submitter}%'")
    public List<BusinessInfoHeader> queryBusinessByCondition(@Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                                                @Param("level2CategoryId") String level2Category,@Param("submitter") String submitter);
    //多条件查询业务信息列表总数
    @Select("select count(*) from businessInfo where businessId in (select businessId from business_relation where categoryGuid in (select guid from category where guid like '%${level2Category}%' and categorytype=1))" +
            "and technicalStatus=#{status} and ticketNumber like '%${ticketNumber}%' and submitter like '%${submitter}%'")
    public long queryBusinessCountByCondition(@Param("status")Integer status, @Param("ticketNumber") String ticketNumber, @Param("businessName")String businessName,
                                              @Param("level2Category") String level2Category,@Param("submitter") String submitter);

    //查询业务目录关系业务信息列表
    @Select({"<script>",
             " select businessInfo.businessId,businessInfo.name,businessInfo.businessStatus,businessInfo.technicalStatus,businessInfo.submitter,businessInfo.submissionTime,businessInfo.ticketNumber, business_relation.categoryGuid from businessInfo",
             " join business_relation",
             " on",
             " businessInfo.businessId = business_relation.businessId",
             " and",
             " business_relation.categoryGuid=#{categoryGuid}",
             " <if test='limit!= -1'>",
             " limit #{limit}",
             " </if>",
             " offset #{offset}",
             " </script>"})
    public List<BusinessInfoHeader> queryBusinessByCatetoryIdWithLimit(@Param("categoryGuid")String categoryGuid, @Param("limit")int limit,@Param("offset") int offset);

    @Select({"<script>",
             " select count(*) from businessInfo",
             " join business_relation",
             " on",
             " businessInfo.businessId = business_relation.businessId",
             " and",
             " business_relation.categoryGuid=#{categoryGuid}",
             " </script>"})
    public long queryBusinessCountByByCatetoryId(@Param("categoryGuid")String categoryGuid);

    //更新技术信息操作者及更新时间
    @Update("update businessInfo set technicalOperator=#{technicalOperator},technicalLastUpdate=#{technicalLastUpdate} where businessId=#{businessId}")
    public int updateTechnicalInfo(@Param("businessId")String businessId, @Param("technicalOperator")String technicalOperator, @Param("technicalLastUpdate")String technicalLastUpdate);

    //删除业务信息与表的关联
    @Delete("delete from business2table where businessId=#{businessId}")
    public int deleteRelationByBusinessId(@Param("businessId")String businessId);

    //添加业务信息与表的关联
    @Insert("insert into business2table(businessId, tableGuid)values(#{businessId}, #{tableGuid})")
    public int insertTableRelation(@Param("businessId")String businessId, @Param("tableGuid")String tableId);

}
