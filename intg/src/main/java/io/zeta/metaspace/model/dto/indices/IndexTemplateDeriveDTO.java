package io.zeta.metaspace.model.dto.indices;

import io.zeta.metaspace.model.po.indices.IndexDerivePO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 派生指标模板
 */
@Data
public class IndexTemplateDeriveDTO extends IndexDerivePO {

    public IndexTemplateDeriveDTO() {
        this.indexAtomicName = "";
        this.timeLimitName = "";
        this.modifiers = new ArrayList<>();
        this.modifiersName = "";
        this.modifiersNameList = new ArrayList<>();
        this.indexFieldName = "";
        this.businessLeaderName = "";
        this.technicalLeaderName = "";
        this.approvalGroupName = "";
        super.setIndexName("");
        super.setIndexIdentification("");
        super.setTimeLimitId("");
        super.setDescription("");
        super.setCentral(false);
        super.setBusinessCaliber("");
        super.setTechnicalCaliber("");
    }

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

    public Boolean checkTitle() {
        if (!"依赖原子指标*".equals(this.indexAtomicName)) {
            return false;
        }
        if (!"时间限定".equals(this.timeLimitName)) {
            return false;
        }
        if (!"修饰词(多个以-分隔)".equals(this.modifiersName)) {
            return false;
        }
        if (!"指标名称*".equals(super.getIndexName())) {
            return false;
        }
        if (!"指标标识*".equals(super.getIndexIdentification())) {
            return false;
        }
        if (!"描述".equals(super.getDescription())) {
            return false;
        }
        if (!"指标域*".equals(this.indexFieldName)) {
            return false;
        }
        if (!"业务口径*".equals(super.getBusinessCaliber())) {
            return false;
        }
        if (!"业务负责人*".equals(this.businessLeaderName)) {
            return false;
        }
        if (!"技术口径".equals(super.getTechnicalCaliber())) {
            return false;
        }
        if (!"技术负责人".equals(this.technicalLeaderName)) {
            return false;
        }
        if (!"审批管理*".equals(this.approvalGroupName)) {
            return false;
        }
        return true;
    }

}
