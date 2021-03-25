package io.zeta.metaspace.model.approvegroup;

import lombok.Data;

@Data
public class ApproveGroupParas {


    private String query;
    private int offset;
    private int limit;
    private String sortBy;
    private String order;

    /**
     * 模块ID，用于基于模块获取审批组
     */
    private String moduleId;


}
