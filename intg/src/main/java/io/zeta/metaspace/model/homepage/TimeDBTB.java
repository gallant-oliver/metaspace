package io.zeta.metaspace.model.homepage;

public class TimeDBTB {
    private String date;
    private long databaseTotal;
    private long tableTotal;

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
