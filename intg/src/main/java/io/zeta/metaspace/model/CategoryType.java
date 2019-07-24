package io.zeta.metaspace.model;

public enum CategoryType {

    TECHNOLOGY(1, "技术数据"),
    BUSINESS(2, "业务数据"),
    DATA_STANDARD(3, "数据标准"),
    DATA_QUALITY_RULE_TEMPLATE(4, "数据质量规则模版"),
    /**
     * 数据质量:规则和告警组公用一个分组
     */
    DATA_QUALITY_RULE(5, "数据质量规则");

    private Integer type;
    private String description;

    CategoryType(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
