package io.zeta.metaspace.model.metadata;

public class CheckingInfo {
    private String tableGuid;
    private String namingConvention;
    private Integer fillRate;
    private String messageIntegrity;

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(String namingConvention) {
        this.namingConvention = namingConvention;
    }

    public Integer getFillRate() {
        return fillRate;
    }

    public void setFillRate(Integer fillRate) {
        this.fillRate = fillRate;
    }

    public String getMessageIntegrity() {
        return messageIntegrity;
    }

    public void setMessageIntegrity(String messageIntegrity) {
        this.messageIntegrity = messageIntegrity;
    }
}
