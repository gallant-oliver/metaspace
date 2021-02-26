package io.zeta.metaspace.model.approvegroup;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApproveGroup {

    private String id;

    private String name; //审批组名称

    private String description; //描述

    List<String> modules; //作用范围（模块）

    private String creator;

    private String updater;

    private String tenantId;

    private boolean valid;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;









}
