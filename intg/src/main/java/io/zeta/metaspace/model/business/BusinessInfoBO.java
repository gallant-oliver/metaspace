package io.zeta.metaspace.model.business;

import lombok.Data;

/**
 * @Author wuyongliang
 * @Date 2021/9/30 10:18
 * @Description 业务对象审核详情
 */

@Data
public class BusinessInfoBO {
    /**
     * 业务对象
     */
    private String businessId;

    private String businessName;

    /**
     * 业务部门
     */
    private String departmentId;

    private String departmentName;

    /**
     * 创建方式：0（手动添加），1（上传文件）
     */
    private int createMode;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 所属主题，即业务对象所在目录路径
     */
    private String theme;

    /**
     * 业务描述
     */
    private String description;

    /**
     * 所有者
     */
    private String owner;

    /**
     * 管理者
     */
    private String manager;

    /**
     * 维护者
     */
    private String maintainer;

    /**
     * 相关资产
     */
    private String dataAssets;

    /**
     * 说明信息
     */
    private String publishDesc;

    /**
     * 是否发布
     */
    private Boolean publish;
}
