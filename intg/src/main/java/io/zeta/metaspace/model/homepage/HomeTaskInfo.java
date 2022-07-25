package io.zeta.metaspace.model.homepage;

import lombok.Data;

import java.io.Serializable;

/**
 * @author huangrongwen
 * @Description: 概览-质量任务详情
 * @date 2022/3/910:55
 */
@Data
public class HomeTaskInfo implements Serializable {
    private static final long serialVersionUID = -7220348770322246885L;
    private long redCount;
    private long orangeCount;
    private long generalCount;
}
