package io.zeta.metaspace.model.homepage;

import java.io.Serializable;

public class TimeDBTB implements Serializable {
    private String date;
    private long databaseTotal;
    private long tableTotal;
    private long subsystemTotal;
    private long sourceLogicDBTotal;
    private long sourceEntityDBTotal;

    public long getSubsystemTotal() {
        return subsystemTotal;
    }

    public void setSubsystemTotal(long subsystemTotal) {
        this.subsystemTotal = subsystemTotal;
    }

    public long getSourceLogicDBTotal() {
        return sourceLogicDBTotal;
    }

    public void setSourceLogicDBTotal(long sourceLogicDBTotal) {
        this.sourceLogicDBTotal = sourceLogicDBTotal;
    }

    public long getSourceEntityDBTotal() {
        return sourceEntityDBTotal;
    }

    public void setSourceEntityDBTotal(long sourceEntityDBTotal) {
        this.sourceEntityDBTotal = sourceEntityDBTotal;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDatabaseTotal() {
        return databaseTotal;
    }

    public void setDatabaseTotal(long databaseTotal) {
        this.databaseTotal = databaseTotal;
    }

    public long getTableTotal() {
        return tableTotal;
    }

    public void setTableTotal(long tableTotal) {
        this.tableTotal = tableTotal;
    }
}
