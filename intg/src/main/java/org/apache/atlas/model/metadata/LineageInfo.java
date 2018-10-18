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
 * @date 2018/10/18 9:44
 */
package org.apache.atlas.model.metadata;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/18 9:44
 */
public class LineageInfo {

    private String guid;
    private Integer lineageDepth;
    private List<LineageEntity> entities;
    private List<LineageRelation> relations;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Integer getLineageDepth() {
        return lineageDepth;
    }

    public void setLineageDepth(Integer lineageDepth) {
        this.lineageDepth = lineageDepth;
    }

    public List<LineageEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<LineageEntity> entities) {
        this.entities = entities;
    }

    public List<LineageRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<LineageRelation> relations) {
        this.relations = relations;
    }
}
