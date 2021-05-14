package io.zeta.metaspace.model.dto.indices;

import io.zeta.metaspace.model.po.indices.IndexCompositePO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class IndexTemplateCompositeDTO extends IndexCompositePO {

    /**
     * 依赖指标
     */
    private List<String> dependentIndicesId;

    /**
     * 依赖指标名称列表
     */
    private List<String> dependentIndicesNameS;

    /**
     * 依赖指标
     */
    private String dependentIndicesName;

    /**
     * 指标域名称
     */
    private String indexFieldName;

    /**
     * 业务负责人名称
     */
    private String businessLeaderName;

    /**
     * 技术负责人名称
     */
    private String technicalLeaderName;

    /**
     * 审批管理名称
     */
    private String approvalGroupName;

    public String checkFieldsIsNull() {
        if (StringUtils.isBlank(this.dependentIndicesName)) {
            return "依赖派生指标不能为空";
        } else if (StringUtils.isBlank(super.getIndexName())) {
            return "指标名称不能为空";
        } else if (StringUtils.isBlank(super.getIndexIdentification())) {
            return "指标标识不能为空";
        } else if (StringUtils.isBlank(this.indexFieldName)) {
            return "指标域不能为空";
        } else if (StringUtils.isBlank(super.getExpression())) {
            return "设定表达式不能为空";
        } else if (StringUtils.isBlank(super.getBusinessCaliber())) {
            return "业务口径不能为空";
        } else if (StringUtils.isBlank(this.businessLeaderName)) {
            return "业务负责人不能为空";
        } else if (StringUtils.isBlank(this.approvalGroupName)) {
            return "审批管理不能为空";
        }
        return "";
    }
}
