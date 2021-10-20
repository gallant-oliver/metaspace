// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/2/21 11:43
 */
package io.zeta.metaspace.model.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/21 11:43
 */
@Data
public class BusinessInfoHeader {
    private String businessId;
    private String name;
    private String level2Category;
    private String path;
    private String businessStatus;
    private String technicalStatus;
    private String submitter;
    private String submitterName;


    private String departmentId;
    private String submissionTime;
    private String ticketNumber;
    private String categoryGuid;
    private String trustTable;
    private String tenantId;
    private List<TechnologyInfo.Table> tables;

    /**
     * 状态：0待发布，1待审批，2审核不通过，3审核通过'
     */
    private String status;

    /**
     * 是否发布
     */
    private Boolean publish;

    private String privateStatus;

    @JsonIgnore
    private int total;
}
