package io.zeta.metaspace.model.dto.indices;

import io.zeta.metaspace.model.po.indices.IndexDerivePO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 派生指标模板
 */
@Data
public class IndexTemplateDeriveDTO extends IndexDerivePO {

    /**
     * 依赖原子指标名称
     */
    private String indexAtomicName;

    /**
     * 时间限定名称
     */
    private String timeLimitName;

    /**
     * 修饰词列表
     */
    private List<String> modifiers;

    /**
     * 修饰词名称
     */
    private String modifiersName;

    /**
     * 修饰词名称列表
     */
    private List<String> modifiersNameList;

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
     * 审批组名称
     */
    private String approvalGroupName;

    public String checkFieldsIsNull() {
        if (StringUtils.isBlank(this.indexAtomicName)) {
            return "依赖原子指标不能为空";
        } else if (StringUtils.isBlank(super.getIndexName())) {
            return "指标名称不能为空";
        } else if (StringUtils.isBlank(super.getIndexIdentification())) {
            return "指标标识不能为空";
        } else if (StringUtils.isBlank(this.indexFieldName)) {
            return "指标域不能为空";
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
