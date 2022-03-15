package io.zeta.metaspace.model.metadata;


import lombok.Data;

/**
 * 从任务调度获取关系型数据血缘对象
 * @author w
 */
@Data
public class SimpleTaskNode {
    /**
     * 节点名称
     */
    private String name;
    /**
     * 状态
     */
    private String state;
    /**
     * 表输入
     */
    private TableInfoVo inputTable;
    /**
     * 表输出
     */
    private TableInfoVo outputTable;
    /**
     * 任务类型
     */
    private String taskType;
    /**
     *  上一节点
     */
    private String preNode;
    /**
     *  描述
     */
    private String desc;
    /**
     *  sql
     */
    private String sql;
}
