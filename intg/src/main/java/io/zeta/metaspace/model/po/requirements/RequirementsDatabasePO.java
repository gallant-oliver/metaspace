package io.zeta.metaspace.model.po.requirements;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RequirementsDatabasePO {
    private String guid;

    private String requirementsId;

    private String middleType;

    private String database;

    private String tableNameEn;

    private String tableNameCh;

    private Integer status;

    private String description;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;

    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;

    private String creator;

    /**
     * 所属租户
     */
    private String tenant;
}