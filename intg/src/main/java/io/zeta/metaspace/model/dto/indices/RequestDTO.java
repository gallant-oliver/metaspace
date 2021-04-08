package io.zeta.metaspace.model.dto.indices;

import java.util.List;

public class RequestDTO<T> {

    private List<T> dtoList;

    public List<T> getDtoList() {
        return dtoList;
    }

    public void setDtoList(List<T> dtoList) {
        this.dtoList = dtoList;
    }
}

