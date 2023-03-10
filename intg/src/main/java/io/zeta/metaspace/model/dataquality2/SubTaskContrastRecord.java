package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

import java.util.List;

/**
 * @author w
 */
@Data
public class SubTaskContrastRecord {
    /**
     * 子任务ID
     */
    private String subTaskId;
    /**
     * 子任务执行规则ID
     */
    private String subtaskRuleId;
    /**
     * 子任务规则名称（子任务+规则名称）
     */
    private String subTaskRuleName;
    /**
     * 任务对应子任务最后10条趋势
     */
    private List<SubTaskContrast> subTaskContrastList;
}
