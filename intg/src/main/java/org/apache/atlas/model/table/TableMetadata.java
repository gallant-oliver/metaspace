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

package org.apache.atlas.model.table;

/**
 * hive: show tblproperties {table}
 */
public class TableMetadata {

    private String numFiles;
    private String numRows;
    private String totalSize;

    public String getNumFiles() {
        return numFiles;
    }

    public void setNumFiles(String numFiles) {
        this.numFiles = numFiles;
    }

    public String getNumRows() {
        return numRows;
    }

    public void setNumRows(String numRows) {
        this.numRows = numRows;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }
}
