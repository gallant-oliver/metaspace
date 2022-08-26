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
     * 任务对应子任务最后10条趋势
     */
    private List<SubTaskContrast> subTaskContrastList;
}
