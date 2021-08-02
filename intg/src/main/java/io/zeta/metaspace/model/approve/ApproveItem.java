package io.zeta.metaspace.model.approve;


import lombok.Data;
import java.sql.Timestamp;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

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
     private String businessTypeText;
     /**
      * 审批类型(发布、下线)
      */
     private String approveType;
     /**
      * 审批状态
      */
     private String approveStatus;
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
     @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
     private Timestamp commitTime;
     /**
      * 驳回原因
      */
     private String reason;
     /**
      * 模块id
      */
     private String moduleId;
     /**
      * 审批对象版本
      */
     private int version;
     /**
      * 租户id
      */
     private String tenantId;
     /**
      * 审批时间
      */
     @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
     private Timestamp approveTime;

     private int totalSize;


}
