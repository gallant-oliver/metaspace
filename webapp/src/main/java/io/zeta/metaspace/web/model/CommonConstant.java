package io.zeta.metaspace.web.model;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务操作公共属性值
 *
 * @author g
 */
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

    public static final int TECHNICAL_CATEGORY_TYPE = 0;

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
    public static final String REGEX_MOBILE = "^[0][0-9]{2,3}-[0-9]{5,10}$";

    /**
     * 手机号正则表达式
     */
    public static final String REGEX_PHONE = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    public final static int ALL = 0;

    public final static int BUSINESS = 1;

    public final static int TABLES = 2;

    public final static int TASKS = 4;

    public final static int STANDARD = 5;

    /**
     * 关系型血缘查找默认双向
     */
    public static final String DEFAULT_DIRECTION = "BOTH";
    /**
     * 关系型血缘查找双向
     */
    public static final String BOTH_DIRECTION = "BOTH";
    /**
     * 关系型血缘查找向上
     */
    public static final String INPUT_DIRECTION = "INPUT";
    /**
     * 关系型血缘查找向下
     */
    public static final String OUTPUT_DIRECTION = "OUTPUT";
    /**
     * 默认血缘深度
     */
    public static final String DEFAULT_DEPTH = "-1";
    /**
     * 默认时间字段
     * 生成DDL或DML
     */
    public static final String ETL_DATE = "etl_date";
    /**
     * 数据源类型不符合规范提示
     */
    public static final String DATA_SOURCE_NOT_PROPERLY_DESCRIBED = "数据源类型不符合规范";
    /**
     * UTF-8编码字符串
     */
    public static final String CHARACTER_CODE_UTF = "UTF-8";

    /**
     * 字段长度
     */
    public static final int LENGTH_1000 = 1000;

    /**
     * 数据服务api参数类型
     */
    public static final String HEADER_PARAM="HEADER";
    public static final String QUERY_PARAM = "QUERY";
    public static final String PATH_PARAM = "PATH";
    public static final String API_PATH = "metaspace";
    /**
     * 文件归档临时保存文件名
     */
    public static final ConcurrentHashMap<String, String> FILE_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();
}
