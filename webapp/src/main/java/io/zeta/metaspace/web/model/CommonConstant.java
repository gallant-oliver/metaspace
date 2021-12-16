package io.zeta.metaspace.web.model;

public class CommonConstant {
    
    /**
     * 正常
     */
    public static final String UP = "UP";
    
    /**
     * 异常
     */
    public static final String DOWN = "DOWN";
    
    /**
     * 请求头参数: 租户ID
     */
    public static final String HEADER_TENANT_ID = "tenantId";

    /**
     * 业务目录类型
     */
    public static final Integer BUSINESS_CATEGORY_TYPE = 1;

    /**
     * 需求状态-待下发
     */
    public static final Integer REQUIREMENTS_STATUS_ONE = 1;

    /**
     * 需求状态-已下发
     */
    public static final Integer REQUIREMENTS_STATUS_TWO = 2;

    /**
     * 需求状态-待反馈
     */
    public static final Integer REQUIREMENTS_STATUS_THREE = 3;
}
