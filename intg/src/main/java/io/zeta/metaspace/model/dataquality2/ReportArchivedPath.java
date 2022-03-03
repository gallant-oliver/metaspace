package io.zeta.metaspace.model.dataquality2;


import lombok.Data;

/**
 * 报告归档路径实体
 * @author wh
 */
@Data
public class ReportArchivedPath {
    /**
     * 规则模板ID
     */
    private String ruleTemplateId;
    /**
     * 任务执行id
     */
    private String executeId;
    /**
     * 规则模板名称
     */
    private String ruleTemplateName;
    /**
     * 报告归档路径名称
     */
    private String archivedPath;

}
