package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.metadata.Parameters;
import io.zeta.metaspace.model.po.requirements.RequirementsPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface RequirementsMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsPO record);

    int insertSelective(RequirementsPO record);

    RequirementsPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsPO record);

    int updateByPrimaryKey(RequirementsPO record);

    int deleteByGuids(@Param("guids") List<String> guids);

    /**
     * 分页查询已反馈需求
     *
     * @param tableId    数据表ID
     * @param parameters 分页参数
     */
    List<RequirementsPO> pagedAlreadyFeedbackRequirements(String tableId, Parameters parameters);
}