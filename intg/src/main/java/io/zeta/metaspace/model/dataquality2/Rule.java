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
package io.zeta.metaspace.model.dataquality2;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zeta.metaspace.model.datastandard.DataStandard;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Rule {
    
    private String id;
    private String ruleTemplateId;
    /**
     * 参照数据标准ID: {@link DataStandard#getId()}
     */
    private String dataStandardId;
    private String name;
    private String code;
    private Integer scope;
    private String categoryId;
    private boolean enable;
    private String description;
    private Integer checkType;
    private Integer checkExpressionType;
    private Float checkThresholdMinValue;
    private Float checkThresholdMaxValue;
    private String unit;
    private String creator;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(timezone = "Asia/Shanghai", pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp updateTime;
    private boolean delete;
    private String path;
    private String sql;
    @JsonIgnore
    private int total;
    private int type;
    private Integer ruleType;
    private String ruleTypeName;

    public Rule(String name,int scope,String unit,String description,Timestamp createTime,Timestamp updateTime,String id,String categoryId,int type){
        this.name = name;
        this.scope=scope;
        this.unit=unit;
        this.description=description;
        this.createTime=createTime;
        this.updateTime=updateTime;
        this.delete=false;
        this.id=id;
        this.categoryId=categoryId;
        this.type=type;
        this.code=id;
        this.sql=null;
        this.enable=true;
    }

    public Rule(){

    }
}
