package io.zeta.metaspace.model.sourceinfo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.zeta.metaspace.model.enums.Status;
import io.zeta.metaspace.model.enums.SubmitType;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class DatabaseInfoForList {
    /**
    * 源信息id
     * **/
    private String id;
    /**
     * 目录名
     * **/
    private String categoryName;

    private String categoryId;
    /**
     * 数据库名
     * **/
    private String databaseName;
    /**
     * 数据库id
     * **/
    private String databaseId;
    /**
     * 数据库类型
     * **/
    private String databaseTypeName;
    /**
     * 数据库中文名
     * **/
    private String databaseAlias;
    /**
     * 是否保密
     * **/
    private Boolean security;
    /**
     * 源信息状态
     * **/
    private String status;

    /**
     * 更新人
     * **/
    private String updaterName;

    /**
     * 更新时间
     * **/
    private String updateTime;

    /**
     * 审核员姓名
     * **/
    private String auditorName;

    /**
     * 审核意见
     * **/
    private String auditDes;


    /**
     * 业务Owner部门名称
     * **/
    private String boDepartmentName;


}
