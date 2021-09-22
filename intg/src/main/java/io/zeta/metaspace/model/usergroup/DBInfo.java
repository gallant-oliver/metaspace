package io.zeta.metaspace.model.usergroup;

/**
 * @author fanjiajia
 * @Description
 * @date 2021/9/10 13:54
 */
public class DBInfo {
    private String databaseGuid;
    private String databaseName;

    public String getDatabaseGuid() {
        return databaseGuid;
    }

    public void setDatabaseGuid(String databaseGuid) {
        this.databaseGuid = databaseGuid;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

}
