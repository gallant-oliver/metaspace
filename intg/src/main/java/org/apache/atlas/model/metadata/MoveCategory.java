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

package org.apache.atlas.model.metadata;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/5/22 10:35
 */
public class MoveCategory {
    private SortCategory init;
    private List<CategoryEntityV2> move;
    private String guid;

    public SortCategory getInit() {
        return init;
    }

    public void setInit(SortCategory init) {
        this.init = init;
    }

    public List<CategoryEntityV2> getMove() {
        return move;
    }

    public void setMove(List<CategoryEntityV2> move) {
        this.move = move;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
