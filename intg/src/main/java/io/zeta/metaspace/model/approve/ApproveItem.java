package io.zeta.metaspace.model.approve;


import lombok.Data;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * 审批条目实体
 */
@Data
public class ApproveItem {

     private String id;

     private String objectId;

     private String objectName;

     private String businessType;

     private String approveType;

     private String status;

     private String ApproveGroup;

     private String submitter;

     private String approver;

     private TimeStamp commitTime;

     private String reason;

     private String module_id;

     private int version;

     private String tenant_id;

     private TimeStamp approveTime;


}
