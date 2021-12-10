package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.dto.requirements.RequireListParam;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.ResourcePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface RequirementsMapper {
    int deleteByPrimaryKey(String guid);
    int insert(RequirementsPO record);
    int insertSelective(RequirementsPO record);
    int updateByPrimaryKeySelective(RequirementsPO record);

    int updateByPrimaryKey(RequirementsPO record);

    /**
     * 分页查询已反馈需求下的资源
     *
     * @param tableId 数据表ID 不能为空
     * @param params  分页参数 不能为空
     */
    List<ResourcePO> pagedResources(@Param("tableId") String tableId,
                                    @Param("params") Parameters params);

    /**
     * 分页查询需求管理列表
     * @param creator
     * @param requireListParam
     * @return
     */
    List<RequirementsPO> selectListByCreatorPage(@Param("creator") String creator,@Param("param") RequireListParam requireListParam);

    long countRequirementByName(@Param("name") String name, @Param("tenantId") String tenantId);
    
    long countRequirementByNum(@Param("num") String num, @Param("tenantId") String tenantId);
    
    RequirementsPO getRequirementById(String id);
}