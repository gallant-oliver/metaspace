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
package io.zeta.metaspace.model.datastandard;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.enums.DataStandardDataType;
import io.zeta.metaspace.model.enums.DataStandardLevel;
import io.zeta.metaspace.model.enums.DataStandardType;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.sql.Timestamp;

/**
 * 数据质量-数据标准
 *
 * @author 周磊
 * @date 2021/11/15 11:59
 */
@Data
public class DataStandard {
    
    private String id;
    /**
     * 数据标准编码
     */
    @NotBlank
    private String number;
    /**
     * 数据标准名称
     */
    @NotBlank
    private String name;
    /**
     * 数据标准类型 {@link DataStandardType}
     */
    private Integer standardType;
    /**
     * 数据类型 {@link DataStandardDataType} 标准类型为数据标准时有效
     */
    private String dataType;
    /**
     * 数据长度,非0正整数
     */
    private Integer dataLength;
    /**
     * 是否有允许值,默认false
     */
    private boolean allowableValueFlag = false;
    /**
     * 允许值,allowableValueFlag=true时有效,多个值时用';'分割
     */
    private String allowableValue;
    /**
     * 标准层级 {@link DataStandardLevel}
     */
    private Integer standardLevel;
    /**
     * 描述
     */
    private String description;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    private String operator;
    /**
     * 版本号: 默认当前版本为0,历史版本号均为非0正整数
     */
    private Integer version;
    private String categoryId;
    private boolean delete;
    private String path;
    @JsonIgnore
    private int total;
    
    
}
