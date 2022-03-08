package io.zeta.metaspace.model.homepage;

import lombok.Data;

import java.io.Serializable;

/**
 * @author huangrongwen
 * @Description: 概览-api信息统计
 * @date 2022/3/810:19
 */
@Data
public class HomeProjectInfo implements Serializable {
    private long order;
    private String id;
    private String name;
    private long upApiNum;
    private long notUpApiNum;
}
