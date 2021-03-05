package io.zeta.metaspace.model.modifiermanage;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ReferenceIndex {
    private String qualifierName;
    private String indexName;
    //业务负责人
    private String interfaceUser;
    @JsonIgnore
    private Integer total;

    public String getQualifierName() {
        return qualifierName;
    }

    public void setQualifierName(String qualifierName) {
        this.qualifierName = qualifierName;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getInterfaceUser() {
        return interfaceUser;
    }

    public void setInterfaceUser(String interfaceUser) {
        this.interfaceUser = interfaceUser;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public ReferenceIndex(String qualifierName, String indexName, String interfaceUser, Integer total) {
        this.qualifierName = qualifierName;
        this.indexName = indexName;
        this.interfaceUser = interfaceUser;
        this.total = total;
    }

    public ReferenceIndex() {
    }
}
