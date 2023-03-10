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
 * @date 2019/9/24 14:09
 */
package io.zeta.metaspace.model.metadata;

import java.util.List;
import java.util.Set;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/24 14:09
 */
public class ComparisonColumnMetadata {
    private List<ColumnMetadata> currentMetadata;
    private List<ColumnMetadata> oldMetadata;
    private Set<String> changedSet;

    public List<ColumnMetadata> getCurrentMetadata() {
        return currentMetadata;
    }

    public void setCurrentMetadata(List<ColumnMetadata> currentMetadata) {
        this.currentMetadata = currentMetadata;
    }

    public List<ColumnMetadata> getOldMetadata() {
        return oldMetadata;
    }

    public void setOldMetadata(List<ColumnMetadata> oldMetadata) {
        this.oldMetadata = oldMetadata;
    }

    public Set<String> getChangedSet() {
        return changedSet;
    }

    public void setChangedSet(Set<String> changedSet) {
        this.changedSet = changedSet;
    }
}
