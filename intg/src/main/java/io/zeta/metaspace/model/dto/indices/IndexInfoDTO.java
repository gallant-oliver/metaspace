package io.zeta.metaspace.model.dto.indices;

import io.zeta.metaspace.model.po.indices.IndexAtomicPO;

import java.util.List;

public class IndexInfoDTO {
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
     * 指标类型
     */
    private int indexType;
    /**
     * 依赖指标
     */
    private List<DependentIndex> dependentIndices;
    /**
     *指标描述
     */
    private  String  description ;
    /**
     *是否核心指标
     */
    private  boolean  central ;
    /**
     * 时间限定id
     */
    private  String  timeLimitId;
    /**
     * 时间限定名称
     */
    private  String  timeLimitName;
    /**
     *指标域id
     */
    private  String  indexFieldId ;
    /**
     * 指标域名称
     */
    private  String  indexFieldName;
    /**
     *审批组id
     */
    private  String  approvalGroupId ;
    /**
     * 审批组名称
     */
    private  String  approvalGroupName;
    /**
     * 审批组成员列表
     */
    private  List<ApprovalGroupMember> approvalGroupMembers;
    /**
     * 修饰词列表
     */
    private List<Modifier> modifiers;
    /**
     * 指标状态；1 新建(未发布过)，2 已发布，3 已下线，4 审核中
     */
    private int indexState;
    /**
     * 版本号，每下线一次，记录一次历史版本，初始版本号为0，每下线一次，版本号+1
     */
    private int version;
    /**
     *数据源id
     */
    private  String  sourceId ;
    /**
     * 数据源名称
     */
    private String sourceName;
    /**
     * 数据库名称
     */
    private  String  dbName;
    /**
     *表id
     */
    private  String  tableId ;
    /**
     * 表名
     */
    private String tableName;
    /**
     *字段id
     */
    private  String  columnId ;
    /**
     * 字段名
     */
    private String columnName;
    /**
     *业务口径
     */
    private  String  businessCaliber ;
    /**
     *业务负责人id
     */
    private  String  businessLeader ;
    /**
     * 业务负责人名称
     */
    private  String businessLeaderName;
    /**
     *技术口径
     */
    private  String  technicalCaliber ;
    /**
     *技术负责人id
     */
    private  String  technicalLeader ;
    /**
     *技术负责人名称
     */
    private  String  technicalLeaderName ;
    /**
     * 创建人id
     */
    private String creator;
    /**
     * 创建人名称
     */
    private String creatorName;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新人id
     */
    private String updater;
    /**
     *更新人名称
     */
    private String updaterName;
    /**
     *更新时间
     */
    private String updateTime;
    /**
     *发布人id
     */
    private String publisher;
    /**
     *发布人名称
     */
    private String publisherName;
    /**
     *发布时间
     *
     */
    private String publishTime;

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

    public List<DependentIndex> getDependentIndices() {
        return dependentIndices;
    }

    public void setDependentIndices(List<DependentIndex> dependentIndices) {
        this.dependentIndices = dependentIndices;
    }

    public void setIndexType(int indexType) {
        this.indexType = indexType;
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

    public String getTimeLimitName() {
        return timeLimitName;
    }

    public void setTimeLimitName(String timeLimitName) {
        this.timeLimitName = timeLimitName;
    }

    public String getIndexFieldId() {
        return indexFieldId;
    }

    public void setIndexFieldId(String indexFieldId) {
        this.indexFieldId = indexFieldId;
    }

    public String getIndexFieldName() {
        return indexFieldName;
    }

    public void setIndexFieldName(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    public String getApprovalGroupId() {
        return approvalGroupId;
    }

    public void setApprovalGroupId(String approvalGroupId) {
        this.approvalGroupId = approvalGroupId;
    }

    public String getApprovalGroupName() {
        return approvalGroupName;
    }

    public void setApprovalGroupName(String approvalGroupName) {
        this.approvalGroupName = approvalGroupName;
    }

    public List<ApprovalGroupMember> getApprovalGroupMembers() {
        return approvalGroupMembers;
    }

    public void setApprovalGroupMembers(List<ApprovalGroupMember> approvalGroupMembers) {
        this.approvalGroupMembers = approvalGroupMembers;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
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

    public String getBusinessLeaderName() {
        return businessLeaderName;
    }

    public void setBusinessLeaderName(String businessLeaderName) {
        this.businessLeaderName = businessLeaderName;
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

    public String getTechnicalLeaderName() {
        return technicalLeaderName;
    }

    public void setTechnicalLeaderName(String technicalLeaderName) {
        this.technicalLeaderName = technicalLeaderName;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getUpdaterName() {
        return updaterName;
    }

    public void setUpdaterName(String updaterName) {
        this.updaterName = updaterName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public class ApprovalGroupMember{
        private String userId;
        private String username;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
    public class Modifier{
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    public static class DependentIndex{
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
}
