package io.zeta.metaspace.web.dao.requirements;

import io.zeta.metaspace.model.dto.requirements.DealDetailDTO;
import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequirementsResultMapper {

    int deleteByPrimaryKey(String guid);

    int insert(RequirementsResultPO record);

    int insertSelective(RequirementsResultPO record);

    RequirementsResultPO selectByPrimaryKey(String guid);

    int updateByPrimaryKeySelective(RequirementsResultPO record);

    int updateByPrimaryKey(RequirementsResultPO record);

    int batchInsert(@Param("records") List<RequirementsResultPO> records);

    DealDetailDTO queryDealDetail(@Param("id") String id);
}