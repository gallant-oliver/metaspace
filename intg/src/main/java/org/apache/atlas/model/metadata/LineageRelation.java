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
 * @date 2018/10/18 9:36
 */
package org.apache.atlas.model.metadata;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/18 9:36
 */
public class LineageRelation {

    private String fromEntityId;
    private String toEntityId;
    private String relationshiId;

    public String getFromEntityId() {
        return fromEntityId;
    }

    public void setFromEntityId(String fromEntityId) {
        this.fromEntityId = fromEntityId;
    }

    public String getToEntityId() {
        return toEntityId;
    }

    public void setToEntityId(String toEntityId) {
        this.toEntityId = toEntityId;
    }

    public String getRelationshiId() {
        return relationshiId;
    }

    public void setRelationshiId(String relationshiId) {
        this.relationshiId = relationshiId;
    }
}
