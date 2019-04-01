package io.zeta.metaspace.web.model;

public class Progress {
    int total;
    int updated;

    public Progress(int total, int updated) {
        this.total = total;
        this.updated = updated;
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
}
