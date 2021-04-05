package io.zeta.metaspace.model.dto.indices;
public class DeleteIndexInfoDTO {

    private String indexId;
    private int indexType;
    private int indexState;

    public int getIndexState() {
        return indexState;
    }

    public void setIndexState(int indexState) {
        this.indexState = indexState;
    }

    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    public int getIndexType() {
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }
}
