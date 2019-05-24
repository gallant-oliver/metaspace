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
 * @date 2019/4/18 15:09
 */
package io.zeta.metaspace.model.table;

/*
 * @description
 * @author sunhaoning
 * @date 2019/4/18 15:09
 * check 0 未勾选，1 全选 ，2 半选
 */
public class DatabaseHeader {

    private String databaseGuid;
    private String dbName;
    private String databasestatus;
    private int check;

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public String getDatabasestatus() {
        return databasestatus;
    }

    public void setDatabasestatus(String databasestatus) {
        this.databasestatus = databasestatus;
    }

    public String getDatabaseGuid() {
        return databaseGuid;
    }

    public void setDatabaseGuid(String databaseGuid) {
        this.databaseGuid = databaseGuid;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
