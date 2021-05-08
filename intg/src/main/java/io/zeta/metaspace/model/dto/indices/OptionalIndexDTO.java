package io.zeta.metaspace.model.dto.indices;
public class OptionalIndexDTO {
    private String indexId;
    private String indexName;
    private int indexType;
    private String indexIdentification;

    public String getIndexId() {
        return indexId;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getIndexType() {
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }

    public String getIndexIdentification() {
        return indexIdentification;
    }

    public void setIndexIdentification(String indexIdentification) {
        this.indexIdentification = indexIdentification;
    }
}
