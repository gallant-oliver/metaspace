package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.dto.requirements.RequireListParam;
import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import io.zeta.metaspace.model.po.requirements.ResourcePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface RequirementsMapper {
    RequirementsPO selectByPrimaryKey(String guid);
    int deleteByPrimaryKey(String guid);
    int insert(RequirementsPO record);
    int insertSelective(RequirementsPO record);
    int updateByPrimaryKeySelective(RequirementsPO record);

    int updateByPrimaryKey(RequirementsPO record);

    int deleteByGuids(@Param("guids") List<String> guids);

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
    List<RequirementsPO> selectListByCreatorPage(@Param("creator") String creator, @Param("param") RequireListParam requireListParam);

    long countRequirementByName(@Param("name") String name, @Param("tenantId") String tenantId);

    void batchUpdateStatusByIds(@Param("guids") List<String> guids, @Param("status") int status);

    RequirementsPO getRequirementById(String id);

    /**
     * 需求处理-根据状态查询
     * @param userId
     * @param tenantId
     * @param requireListParam
     * @return
     */
    List<RequirementsPO> selectHandleListByStatusPage(@Param("userId") String userId,@Param("tenantId") String tenantId, @Param("param") RequireListParam requireListParam);

    /**
     * 需求处理无状态查询
     * @param userId
     * @param tenantId
     * @param requireListParam
     * @return
     */
    List<RequirementsPO> selectHandleListPage(@Param("userId") String userId,@Param("tenantId") String tenantId, @Param("param") RequireListParam requireListParam);

    /**
     * 需求反馈列表
     * @param userId
     * @param tenantId
     * @param requireListParam
     * @return
     */
    List<RequirementsPO> selectReturnListPage(@Param("userId") String userId,@Param("tenantId") String tenantId, @Param("param") RequireListParam requireListParam);
}