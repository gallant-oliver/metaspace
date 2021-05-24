package io.zeta.metaspace.model.dto.indices;

import io.zeta.metaspace.model.po.indices.IndexCompositePO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
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

    public IndexTemplateCompositeDTO() {
        this.dependentIndicesId = new ArrayList<>();
        this.dependentIndicesNameS = new ArrayList<>();
        this.dependentIndicesName = "";
        this.indexFieldName = "";
        this.businessLeaderName = "";
        this.technicalLeaderName = "";
        this.approvalGroupName = "";
        super.setDescription("");
        super.setIndexName("");
        super.setIndexIdentification("");
        super.setExpression("");
        super.setBusinessCaliber("");
        super.setTechnicalCaliber("");
    }

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

    public Boolean checkTitle() {
        if ("依赖派生指标(多个以-分隔)*".equals(this.dependentIndicesName)) {
            return false;
        }
        if ("指标名称*".equals(super.getIndexName())) {
            return false;
        }
        if ("指标标识*".equals(super.getIndexIdentification())) {
            return false;
        }
        if ("描述".equals(super.getDescription())) {
            return false;
        }
        if ("指标域*".equals(this.indexFieldName)) {
            return false;
        }
        if ("设定表达式*".equals(super.getExpression())) {
            return false;
        }
        if ("业务口径*".equals(super.getBusinessCaliber())) {
            return false;
        }
        if ("业务负责人*".equals(super.getBusinessLeader())) {
            return false;
        }
        if ("技术口径".equals(super.getTechnicalCaliber())) {
            return false;
        }
        if ("技术负责人".equals(super.getTechnicalLeader())) {
            return false;
        }
        if ("审批管理*".equals(this.approvalGroupName)) {
            return false;
        }
        return true;
    }
}
