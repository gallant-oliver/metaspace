package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author w
 */
@Data
public class SubTaskContrast {
    /**
     * 执行总结果
     */
    private Double result;
    /**
     * 执行时间
     */
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp executeTime;
    /**
     * 子任务对应的多个规则执行结果
     */
    private List<SubTaskRuleResultHistory> subTaskRuleList;

    @Data
    public static class SubTaskRuleResultHistory {
        /**
         * 子任务ID
         */
        private String subTaskId;
        /**
         * 子任务规则名称（子任务+规则名称）
         */
        private String subTaskRuleName;
        /**
         * 当前子任务对应规则的结果
         */
        private String result;
    }
}
