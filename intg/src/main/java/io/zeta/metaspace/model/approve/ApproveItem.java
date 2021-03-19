package io.zeta.metaspace.model.approve;


import lombok.Data;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * 审批条目实体
 */
@Data
public class ApproveItem {

     /**
      * 审批条目guid
      */
     private String id;
     /**
      * 审批对象id
      */
     private String objectId;
     /**
      * 审批对象名称
      */
     private String objectName;
     /**
      * 审批对象类型
      */
     private String businessType;
     /**
      * 审批类型(发布、下线)
      */
     private String approveType;
     /**
      * 审批状态
      */
     private String status;
     /**
      * 审批组
      */
     private String ApproveGroup;
     /**
      * 提交人
      */
     private String submitter;
     /**
      * 审批人
      */
     private String approver;
     /**
      * 提交时间
      */
     private TimeStamp commitTime;
     /**
      * 驳回原因
      */
     private String reason;
     /**
      * 模块id
      */
     private String module_id;
     /**
      * 审批对象版本
      */
     private int version;
     /**
      * 租户id
      */
     private String tenant_id;
     /**
      * 审批时间
      */
     private TimeStamp approveTime;


}
