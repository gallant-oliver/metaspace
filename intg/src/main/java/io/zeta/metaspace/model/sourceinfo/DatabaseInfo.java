package io.zeta.metaspace.model.sourceinfo;

import io.zeta.metaspace.model.enums.SubmitType;
import lombok.Data;

@Data
public class DatabaseInfo {
    /**
    * 源信息id
     * **/
    private String id;
    /**
     * 目录id
     * **/
    private String categoryId;
    /**
     * 数据库id
     * **/
    private String databaseId;
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
     * 技术负责人id
     * **/
    private String technicalLeader;
    /**
     * 业务负责人id
     * **/
    private String businessLeader;

}
