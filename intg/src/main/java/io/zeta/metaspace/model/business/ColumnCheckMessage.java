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
 * @date 2019/6/3 14:01
 */
package io.zeta.metaspace.model.business;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/6/3 14:01
 */
public class ColumnCheckMessage {

    private List<ColumnCheckInfo> columnCheckInfoList;
    private int status;
    private int errorCount;
    private int totalSize;
    private List<String> errorColumnList;

    public enum Status {
        SUCCESS(1), FAILURE(0);

        private int status;
        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static class ColumnCheckInfo {
        private int row;
        private String columnName;
        private String displayText;
        private String errorMessage;
        private String updateTime;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDisplayText() {
            return displayText;
        }

        public void setDisplayText(String displayText) {
            this.displayText = displayText;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }
    }

    public List<ColumnCheckInfo> getColumnCheckInfoList() {
        return columnCheckInfoList;
    }

    public void setColumnCheckInfoList(List<ColumnCheckInfo> columnCheckInfoList) {
        this.columnCheckInfoList = columnCheckInfoList;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status.getStatus();
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public List<String> getErrorColumnList() {
        return errorColumnList;
    }

    public void setErrorColumnList(List<String> errorColumnList) {
        this.errorColumnList = errorColumnList;
    }
}
