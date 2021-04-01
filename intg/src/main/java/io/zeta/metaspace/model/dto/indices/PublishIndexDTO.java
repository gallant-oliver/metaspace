package io.zeta.metaspace.model.dto.indices;
public class PublishIndexDTO {
    private String indexId;
    private String indexName;
    private int indexType;
    private int indexState;
    private String approveType;
    private String approvalGroupId;
    private int version;

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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getApproveType() {
        return approveType;
    }

    public void setApproveType(String approveType) {
        this.approveType = approveType;
    }

    public String getApprovalGroupId() {
        return approvalGroupId;
    }

    public void setApprovalGroupId(String approvalGroupId) {
        this.approvalGroupId = approvalGroupId;
    }
}
