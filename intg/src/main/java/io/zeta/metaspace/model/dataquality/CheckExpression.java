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
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/1/10 17:17
 */
package io.zeta.metaspace.model.dataquality;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CheckExpression {

    EQU(0,"等于") , NEQ(1,"不等于"), GTR(2, "大于"), GER(3, "大于等于"), LSS(4, "小于"), LEQ(5, "小于等于");
    public Integer code;
    public String desc;

    CheckExpression(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static CheckExpression getExpressionByCode(Integer code) {
        CheckExpression defaultExpression = CheckExpression.EQU;
        for(CheckExpression ce : CheckExpression.values()) {
            if(ce.code == code)
                return ce;
        }
        return defaultExpression;
    }

    public static String getDescByCode(Integer code) {
        return getExpressionByCode(code).desc;
    }
}
