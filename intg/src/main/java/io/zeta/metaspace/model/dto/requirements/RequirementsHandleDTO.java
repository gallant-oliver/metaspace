package io.zeta.metaspace.model.dto.requirements;

import io.zeta.metaspace.model.po.requirements.RequirementsResultPO;
import lombok.Data;

import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/12/8 15:09
 * @Description 需求处理请求参数
 */

@Data
public class RequirementsHandleDTO {
    /**
     * 需求id
     */
    private List<String> guids;

    /**
     * 处理结果
     */
    private DealDetailDTO result;
}
