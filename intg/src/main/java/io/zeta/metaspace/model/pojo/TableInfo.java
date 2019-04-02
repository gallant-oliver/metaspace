package io.zeta.metaspace.model.pojo;

import com.google.gson.Gson;
import io.zeta.metaspace.model.metadata.TableOwner;
import org.postgresql.util.PGobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TableInfo {
    private String tableGuid;
    private String tableName;
    private String dbName;
    private String status;
    private String createTime;
    private PGobject dataOwner;
    private String databaseGuid;

    public String getDatabaseGuid() {
        return databaseGuid;
    }

    public void setDatabaseGuid(String databaseGuid) {
        this.databaseGuid = databaseGuid;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<Map> getDataOwner() {
        List<Map> list = new ArrayList<>();
        if (dataOwner != null) {
            Gson gson = new Gson();
            list = gson.fromJson(dataOwner.getValue(), List.class);
        }
        return list;
    }

    public void setDataOwner(Object dataOwner) {
        this.dataOwner = (PGobject) dataOwner;
    }
}
