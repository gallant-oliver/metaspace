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
 * @date 2018/11/16 13:34
 */
package io.zeta.metaspace.model.metadata;

/*
 * @description
 * @author sunhaoning
 * @date 2018/11/16 13:34
 */
public class LineageDepthInfo {

    private String guid;
    private String tableName;
    private String dbName;
    private long directUpStreamNum;
    private long directDownStreamNum;
    private long upStreamLevelNum;
    private long downStreamLevelNum;
    private String updateTime;
    private String displayText;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public long getDirectUpStreamNum() {
        return directUpStreamNum;
    }

    public void setDirectUpStreamNum(long directUpStreamNum) {
        this.directUpStreamNum = directUpStreamNum;
    }

    public long getDirectDownStreamNum() {
        return directDownStreamNum;
    }

    public void setDirectDownStreamNum(long directDownStreamNum) {
        this.directDownStreamNum = directDownStreamNum;
    }

    public long getUpStreamLevelNum() {
        return upStreamLevelNum;
    }

    public void setUpStreamLevelNum(long upStreamLevelNum) {
        this.upStreamLevelNum = upStreamLevelNum;
    }

    public long getDownStreamLevelNum() {
        return downStreamLevelNum;
    }

    public void setDownStreamLevelNum(long downStreamLevelNum) {
        this.downStreamLevelNum = downStreamLevelNum;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
