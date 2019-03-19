package io.zeta.metaspace.model.homepage;

import java.io.Serializable;
import java.util.List;

public class BrokenLine implements Serializable {
    private List<String> date;
    private List<String> name;
    private List<List<Long>> data;

    public List<String> getDate() {
        return date;
    }

    public void setDate(List<String> date) {
        this.date = date;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<List<Long>> getData() {
        return data;
    }

    public void setData(List<List<Long>> data) {
        this.data = data;
    }
}
