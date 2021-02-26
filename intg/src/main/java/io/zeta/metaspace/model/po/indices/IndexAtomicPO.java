package io.zeta.metaspace.model.po.indices;

import java.sql.Timestamp;

/**
 * 原子指标
 */
public class IndexAtomicPO {
    /**
     * 指标id
     */
    private  String  indexId ;
    /**
     *指标名称
     */
    private  String  indexName ;
    /**
     *指标标识
     */
    private  String  indexIdentification ;
    /**
     *指标描述
     */
    private  String  description ;
    /**
     *是否核心指标
     */
    private  boolean  central ;
    /**
     *指标域id
     */
    private  String  indexFieldId ;
    /**
     *租户id
     */
    private  String  tenantId ;
    /**
     *审批组id
     */
    private  String  approvalGroupId ;
    /**
     *指标状态；1 新建(未发布过)，2 已发布，3 已下线，4 审核中
     */
    private  int  indexState ;
    /**
     *版本号，下线一次，记录一次历史版本，初始版本为0
     */
    private  int  version ;
    /**
     *数据源id
     */
    private  String  sourceId ;
    /**
     *表id
     */
    private  String  tableId ;
    /**
     *字段id
     */
    private  String  columnId ;
    /**
     *业务口径
     */
    private  String  businessCaliber ;
    /**
     *业务负责人id
     */
    private  String  businessLeader ;
    /**
     *技术口径
     */
    private  String  technicalCaliber ;
    /**
     *技术负责人id
     */
    private  String  technicalLeader ;
    /**
     *创建人id
     */
    private  String  creator ;
    /**
     *创建时间
     */
    private  Timestamp createTime ;
    /**
     *更新人id
     */
    private  String  updater ;
    /**
     *更新时间
     */
    private  Timestamp  updateTime ;
    /**
     *发布人id
     */
    private  String  publisher ;
    /**
     *发布时间
     */
    private  Timestamp  publishTime ;

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

    public String getIndexFieldId() {
        return indexFieldId;
    }

    public void setIndexFieldId(String indexFieldId) {
        this.indexFieldId = indexFieldId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getApprovalGroupId() {
        return approvalGroupId;
    }

    public void setApprovalGroupId(String approvalGroupId) {
        this.approvalGroupId = approvalGroupId;
    }

    public int getIndexState() {
        return indexState;
    }

    public void setIndexState(int indexState) {
        this.indexState = indexState;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Timestamp getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Timestamp publishTime) {
        this.publishTime = publishTime;
    }
}
