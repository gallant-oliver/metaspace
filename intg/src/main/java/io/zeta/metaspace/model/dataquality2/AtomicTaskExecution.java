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
 * @date 2019/7/25 18:52
 */
package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/7/25 18:52
 */
@Data
public class AtomicTaskExecution {
    private String id;
    private String taskExecuteId;
    private String ruleTemplateId;
    private String objectId;
    private String objectName;
    private String dbName;
    private String tableName;
    private String objectType;
    private String taskId;
    private String subTaskId;
    private String subTaskRuleId;
    private Integer taskType;
    private Integer scope;
    private String ruleId;
    private Long timeStamp;
    private List<ConsistencyParam> consistencyParams;
}
