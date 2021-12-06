// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.model.share;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.apigroup.ApiGroupInfo;
import lombok.Data;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/6/3 16:35
 */
@Data
public class ApiInfoV2 {
    private String guid;
    private String name;
    private String sourceId;
    private String sourceName;
    private String schemaName;
    private String tableGuid;
    private String tableName;
    private String dbGuid;
    private String dbName;
    private String categoryGuid;
    private String categoryName;
    private String version;
    private String description;
    private String protocol;
    private String requestMode;
    private String path;
    private String updater;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    private String sourceType;
    private String pool;
    private String creator;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    private String status;
    private boolean approve;
    private boolean log;
    private int versionNum;
    private ApiPolyEntity apiPolyEntity;
    private List<FieldV2> param;
    private List<FieldV2> returnParam;
    private List<FieldV2> sortParam;
    private String projectId;
    private List<ApiGroupInfo> apiGroup;

    @JsonIgnore
    private Object apiPoly;
    @JsonIgnore
    private Object params;
    @JsonIgnore
    private Object returnParams;
    @JsonIgnore
    private Object sortParams;


    @Data
    public static class FieldV2 {
        //别名
        private String name;
        private String description;
        private String place;
        private String defaultValue;
        private boolean useDefaultValue;
        private boolean fill;
        private String maxSize;
        private String minSize;
        private String example;
        private String type;
        //字段名
        private String columnName;
        private String columnType;
        private String columnDescription;
        private String expressionType;
        private String order;
        private Object value;

        /**
         * 字段脱敏规则
         */
        private String rule;
    }

}
