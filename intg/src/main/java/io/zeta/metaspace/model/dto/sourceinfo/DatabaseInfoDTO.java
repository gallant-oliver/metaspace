package io.zeta.metaspace.model.dto.sourceinfo;

import io.zeta.metaspace.model.dto.indices.ApprovalGroupMember;
import io.zeta.metaspace.model.user.User;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DatabaseInfoDTO {
    /**
    * 源信息id
     * **/
    private String id;
    /**
     * 目录名
     * **/
    private String categoryName;
    /**
     * 数据库名
     * **/
    private String databaseName;
    /**
     * 数据库类型名
     * **/
    private String databaseTypeName;
    /**
     * 数据库类型名
     * **/
    private String databaseInstanceName;
    /**
     * 数据源名
     * **/
    private String dataSourceName;
    /**
     * 数据库中文名
     * **/
    private String databaseAlias;
    /**
     * 规划包名称
     * **/
    private String planningPackageName;
    /**
     * 规划包编号
     * **/
    private String planningPackageCode;
    /**
     * 抽取工具
     * **/
    private String extractTool;
    /**
     * 抽取周期
     * **/
    private String extractCycle;
    /**
     * 是否保密
     * **/
    private Boolean security;
    /**
     * 保密期限
     * **/
    private String securityCycle;
    /**
     * 是否重要
     * **/
    private Boolean importance;
    /**
     * 描述
     * **/
    private String description;
    /**
     * 记录人id
     * **/
    private String recorderGuid;
    /**
     * 附件id
     * **/
    private String annexId;
    /**
     * 业务对接人姓名
     * **/
    private String boName;
    /**
     * 业务对接人电话
     * **/
    private String boTel;
    /**
     * 业务对接人部门名
     * **/
    private String boDepartmentName;
    /**
     * 业务对接人邮箱
     * **/
    private String boEmail;
    /**
     * 技术对接人姓名
     * **/
    private String toName;
    /**
     * 技术对接人电话
     * **/
    private String toTel;
    /**
     * 技术对接人部门名
     * **/
    private String toDepartmentName;
    /**
     * 技术对接人邮箱地址
     * **/
    private String toEmail;
    /**
     * 技术负责人名
     * **/
    private String technicalLeader;
    /**
     * 业务负责人名
     * **/
    private String businessLeader;
    /**
     * 审核组名
     * **/
    private String approveGroupName;

    private List<ApprovalGroupMember> approveGroupMembers;
    /**
     * 提交人姓名
     * **/
    private String publisherName;
    /**
     * 提交时间
     * **/
    private Timestamp publishTime;

    private String auditorName;
}
