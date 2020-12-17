package io.zeta.metaspace.model.share;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.htrace.shaded.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@Data
public class ApiAudit {
    private String id;
    private String apiName;
    private String apiGuid;
    private String apiVersion;
    private int apiVersionNum;
    private String applicant;
    private String applicantName;
    private AuditStatusEnum status;
    private String reason;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    private String updater;
    private boolean obsolete;
    private String apiPolyId;
    @JsonIgnore
    private int total;
}
