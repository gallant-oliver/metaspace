package org.apache.atlas.model.metadata;

public class Cloumn {
    private String cloumnId;
    private String cloumnName;
    private String type;
    private String description;

    public String getCloumnId() {
        return cloumnId;
    }

    public void setCloumnId(String cloumnId) {
        this.cloumnId = cloumnId;
    }

    public String getCloumnName() {
        return cloumnName;
    }

    public void setCloumnName(String cloumnName) {
        this.cloumnName = cloumnName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
