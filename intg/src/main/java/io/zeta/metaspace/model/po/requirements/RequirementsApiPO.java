package io.zeta.metaspace.model.po.requirements;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RequirementsApiPO {
    private String guid;

    private String requirementsId;

    private String projectId;

    private String categoryId;

    private String apiId;

    private String description;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    /**
     * 所属租户名称
     */
    private String tenant;

    /**
     * api项目名称
     */
    private String project;

    /**
     * api所属目录
     */
    private String category;

    /**
     * api名称
     */
    private String apiName;

    /**
     * 状态：上线；下线
     */
    private String status;

    /**
     * 创建人
     */
    private String creator;
}