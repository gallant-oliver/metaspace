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

package io.zeta.metaspace.model.table;

/**
 * hive: show tblproperties {table}
 */
public class TableMetadata {

    private long numFiles;
    private long totalSize;

    public TableMetadata() {
    }

    public TableMetadata(long numFiles, long totalSize) {
        this.numFiles = numFiles;
        this.totalSize = totalSize;
    }

    public long getNumFiles() {
        return numFiles;
    }

    public void setNumFiles(long numFiles) {
        this.numFiles = numFiles;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

}
