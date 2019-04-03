package io.zeta.metaspace.web.model;

public class Progress {
    int total;
    int updated;
    String error;
    long startTime;
    long endTime;


    public Progress(int total, int updated) {
        this.total = total;
        this.updated = updated;
        this.error = "";
    }

    public Progress(int total, int updated, String error) {
        this.total = total;
        this.updated = updated;
        this.error = error;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
