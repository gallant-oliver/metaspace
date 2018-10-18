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
 * @date 2018/10/18 9:09
 */
package org.apache.atlas.model.metadata;

/*
 * @description
 * @author sunhaoning
 * @date 2018/10/18 9:09
 */
public class LineageEntity {

    private String typeName;
    private String guid;
    private String tableName;
    private String dbName;
    private int directUpStreamNum;
    private int directDownStreamNum;
    private int upStreamLevelNum;
    private int downStreamLevelNum;
    private String tableUpdateTime;
    private String displayText;


    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

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

    public int getDirectUpStreamNum() {
        return directUpStreamNum;
    }

    public void setDirectUpStreamNum(int directUpStreamNum) {
        this.directUpStreamNum = directUpStreamNum;
    }

    public int getDirectDownStreamNum() {
        return directDownStreamNum;
    }

    public void setDirectDownStreamNum(int directDownStreamNum) {
        this.directDownStreamNum = directDownStreamNum;
    }

    public int getUpStreamLevelNum() {
        return upStreamLevelNum;
    }

    public void setUpStreamLevelNum(int upStreamLevelNum) {
        this.upStreamLevelNum = upStreamLevelNum;
    }

    public int getDownStreamLevelNum() {
        return downStreamLevelNum;
    }

    public void setDownStreamLevelNum(int downStreamLevelNum) {
        this.downStreamLevelNum = downStreamLevelNum;
    }

    public String getTableUpdateTime() {
        return tableUpdateTime;
    }

    public void setTableUpdateTime(String tableUpdateTime) {
        this.tableUpdateTime = tableUpdateTime;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
