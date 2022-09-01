package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

import java.util.List;

/**
 * @author w
 * 任务质量历史趋势图
 */
@Data
public class QualityTrendChart {
    /**
     * X轴时间列表
     */
    private List<String> executeTimeList;
    /**
     * Y轴任务趋势
     */
    private List<SubTaskContrastRecord> subTaskContrastRecordList;
}
