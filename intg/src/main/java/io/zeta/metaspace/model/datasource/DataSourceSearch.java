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

public class DataSourceSearch {
    private String sourceName;
    private String sourceType;
    private String createTime;
    private String updateTime;
    private String updateUserName;

    public DataSourceSearch(){}

    public DataSourceSearch(String sourceName, String sourceType, String createTime, String updateTime, String updateUserName) {
        this.sourceName = sourceName;
        if (sourceType!=null){
            this.sourceType = sourceType.toUpperCase();
        }
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.updateUserName = updateUserName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    @Override
    public String toString() {
        return "DataSourceSearch{" +
               "sourceName='" + sourceName + '\'' +
               ", sourceType='" + sourceType + '\'' +
               ", createTime='" + createTime + '\'' +
               ", updateTime='" + updateTime + '\'' +
               ", updateUserName='" + updateUserName + '\'' +
               '}';
    }
}
