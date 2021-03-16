package io.zeta.metaspace.model.timelimit;


import lombok.Data;

/**
 * 时间限定引用
 */
@Data
public class TimeLimitRelation {

    /**
     * 时间限定名称
     */
    private String name;

    /**
     * 指标名称
     */

    private String indexName;

    /**
     * 接口人
     */
    private String interfaceUser;

    /**
     * 总数
     */

    private int total;
}
