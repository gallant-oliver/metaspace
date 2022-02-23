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

    public static final String EXCEL_FORMAT_XLSX = ".xlsx";

    public static final String EXCEL_FORMAT_XLS = ".xls";

    /**
     * 字段长度
     */
    public static final int LENGTH = 128;

    /**
     * 座机号正则表达式
     */
    public static final  String REGEX_MOBILE = "^[0][0-9]{2,3}-[0-9]{5,10}$";

    /**
     * 手机号正则表达式
     */
    public static final  String REGEX_PHONE = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    public final static int ALL = 0;

    public final static int BUSINESS = 1;

    public final static int TABLES = 2;

    public final static int TASKS = 3;

    public final static int STANDARD = 4;
}
