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

package io.zeta.metaspace.model.datasource;

import java.util.List;

public class DataSourceCheckMessage {
    private List<DataSourceCheckInfo> dataSourceCheckInfoList;
    private int errorCount;
    private int totalSize;
    private List<String> errorDataSourceList;


    public static class DataSourceCheckInfo {
        private int row;
        private String sourceName;
        private String sourceType;
        private String description;
        private String ip;
        private String port;
        private String userName;
        private String password;
        private String database;
        private String jdbcParameter;
        private String errorMessage;

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }
        public String getSourceType() {
            return sourceType;
        }
        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }
        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDatabase() {
            return database;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public String getJdbcParameter() {
            return jdbcParameter;
        }

        public void setJdbcParameter(String jdbcParameter) {
            this.jdbcParameter = jdbcParameter;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public String getSourceName() {
            return sourceName;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

    }

    public List<DataSourceCheckMessage.DataSourceCheckInfo> getDataSourceCheckInfoList() {
        return dataSourceCheckInfoList;
    }

    public void setDataSourceCheckInfoList(List<DataSourceCheckMessage.DataSourceCheckInfo> dataSourceCheckInfoList) {
        this.dataSourceCheckInfoList = dataSourceCheckInfoList;
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

    public List<String> getErrorDataSourceList() {
        return errorDataSourceList;
    }

    public void setErrorDataSourceList(List<String> errorDataSourceList) {
        this.errorDataSourceList = errorDataSourceList;
    }
}
