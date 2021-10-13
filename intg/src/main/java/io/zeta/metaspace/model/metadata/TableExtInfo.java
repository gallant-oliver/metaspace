package io.zeta.metaspace.model.metadata;

public class TableExtInfo {
    private boolean importance;
    private boolean security;

    public boolean isImportance() {
        return importance;
    }

    public void setImportance(boolean importance) {
        this.importance = importance;
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }
}
