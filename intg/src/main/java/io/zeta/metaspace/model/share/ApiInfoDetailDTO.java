package io.zeta.metaspace.model.share;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.zeta.metaspace.model.ip.restriction.ApiIpRestriction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @Author wuyongliang
 * @Date 2021/12/2 11:47
 * @Description
 */

@Data
public class ApiInfoDetailDTO {
    private String id;

    /**
     * api名称
     */
    private String name;

    /**
     * 数据源id
     */
    private String sourceId;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 表id
     */
    private String tableGuid;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 数据库id
     */
    private String dbGuid;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 所属目录id
     */
    private String categoryGuid;

    /**
     * 所属目录
     */
    private String categoryName;

    /**
     * 版本
     */
    private String version;

    /**
     * 描述
     */
    private String description;

    /**
     * 参数协议
     */
    private String protocol;

    /**
     * 请求方式
     */
    private String requestMode;

    /**
     * api路径
     */
    private String path;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 数据源类型
     */
    private String sourceType;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 发布状态
     */
    private String status;

    /**
     * 策略控制
     */
    private ApiIpRestriction apiIpRestriction;

    /**
     * api
     */
    private List<ApiColumnInfoDetail> columns;

    @Data
    public static class ApiColumnInfoDetail {
        /**
         * 名称
         */
        private String name;

        /**
         * 是否筛选字段（‘是’或‘否’）
         */
        private String filter;

        /**
         * 是否必传字段（‘是’或‘否’）
         */
        private String need;

        /**
         * 缺省值（没有展示‘暂无’）
         */
        private String defaultValue;


        /**
         * 字段脱敏规则（没有展示‘暂无’）
         */
        private String rule;
    }
}
