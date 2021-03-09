package io.zeta.metaspace.model.dto.indices;

import java.util.List;

public class DeleteRequestDTO<T> {

    private List<T> deleteList;

    public List<T> getDeleteList() {
        return deleteList;
    }

    public void setDeleteList(List<T> deleteList) {
        this.deleteList = deleteList;
    }
}

