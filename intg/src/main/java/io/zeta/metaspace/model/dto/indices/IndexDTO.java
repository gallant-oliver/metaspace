package io.zeta.metaspace.model.dto.indices;

import java.util.List;

public class IndexDTO {
    /**
     * 指标id
     */
    private String indexId;
    /**
     * 指标名称
     */
    private String indexName;
    /**
     * 指标标识
     */
    private String indexIdentification;
    /**
     * 指标类型
     */
    private int indexType;
    /**
     * 依赖指标
     */
    private List<String> dependentIndices;
    /**
     * 指标描述
     */
    private String description;
    /**
     * 是否核心指标
     */
    private boolean central;
    /**
     * 时间限定id
     */
    private String timeLimitId;
    /**
     * 指标域id
     */
    private String indexFieldId;
    /**
     * 审批组id
     */
    private String approvalGroupId;
    /**
     * 修饰词列表
     */
    private List<String> modifiers;
    /**
     * 数据源id
     */
    private String sourceId;
    /**
     * 数据库名称
     */
    private String dbName;
    /**
     * 表id
     */
    private String tableId;
    /**
     * 字段id
     */
    private String columnId;
    /**
     * 表达式
     */
    private String expression;
    /**
     * 业务口径
     */
    private String businessCaliber;
    /**
     * 业务负责人id
     */
    private String businessLeader;
    /**
     * 技术口径
     */
    private String technicalCaliber;
    /**
     * 技术负责人id
     */
    private String technicalLeader;

    private int version;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

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

    public int getIndexType() {
        return indexType;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
    }

    public List<String> getDependentIndices() {
        return dependentIndices;
    }

    public void setDependentIndices(List<String> dependentIndices) {
        this.dependentIndices = dependentIndices;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCentral() {
        return central;
    }

    public void setCentral(boolean central) {
        this.central = central;
    }

    public String getTimeLimitId() {
        return timeLimitId;
    }

    public void setTimeLimitId(String timeLimitId) {
        this.timeLimitId = timeLimitId;
    }

    public String getIndexFieldId() {
        return indexFieldId;
    }

    public void setIndexFieldId(String indexFieldId) {
        this.indexFieldId = indexFieldId;
    }

    public String getApprovalGroupId() {
        return approvalGroupId;
    }

    public void setApprovalGroupId(String approvalGroupId) {
        this.approvalGroupId = approvalGroupId;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getBusinessCaliber() {
        return businessCaliber;
    }

    public void setBusinessCaliber(String businessCaliber) {
        this.businessCaliber = businessCaliber;
    }

    public String getBusinessLeader() {
        return businessLeader;
    }

    public void setBusinessLeader(String businessLeader) {
        this.businessLeader = businessLeader;
    }

    public String getTechnicalCaliber() {
        return technicalCaliber;
    }

    public void setTechnicalCaliber(String technicalCaliber) {
        this.technicalCaliber = technicalCaliber;
    }

    public String getTechnicalLeader() {
        return technicalLeader;
    }

    public void setTechnicalLeader(String technicalLeader) {
        this.technicalLeader = technicalLeader;
    }

}
