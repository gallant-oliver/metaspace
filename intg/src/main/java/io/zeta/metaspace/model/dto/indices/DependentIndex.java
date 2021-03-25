package io.zeta.metaspace.model.dto.indices;
public class DependentIndex {
    private String indexId;
    private String indexName;
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

    public String getIndexIdentification() {
        return indexIdentification;
    }

    public void setIndexIdentification(String indexIdentification) {
        this.indexIdentification = indexIdentification;
    }
}
