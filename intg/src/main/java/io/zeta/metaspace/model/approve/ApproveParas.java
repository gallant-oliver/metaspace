package io.zeta.metaspace.model.approve;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

@Data
public class ApproveParas {

    private String query;

    private int offset;

    private int limit = 10;

    private String sortBy = "commitTime";

    private String order = "desc";

    private String id;

    public Timestamp getStartTime() {
        if(StringUtils.isBlank(this.startTime)){
            return null;
        }
        return new Timestamp(Long.parseLong(this.startTime));
    }

    public Timestamp getEndTime() {
        if(StringUtils.isBlank(this.endTime)){
            return null;
        }
        return new Timestamp(Long.parseLong(this.endTime));
    }

    private String startTime;

    private String endTime;

    private List<String> approveStatus = new LinkedList<>();   //审核状态

    private String businessType;   //业务类型

    private String approveType;   //审核类型

    private String referer;      //查询来源

    private String userId;       //查询用户

    private String result;  //审批动作 1：通过 2.驳回

    private String desc = ""; //审批描述

    private int version;

    private String moduleId;

    private List<ApproveItem> approveList;

}
