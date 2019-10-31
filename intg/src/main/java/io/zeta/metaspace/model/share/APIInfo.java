// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================
/**
 * @author sunhaoning@gridsum.com
 * @date 2019/3/26 19:45
 */
package io.zeta.metaspace.model.share;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/3/26 19:45
 */
public class APIInfo {
    private String guid;
    private String name;
    private String tableGuid;
    private String tableName;
    private String dbGuid;
    private String dbName;
    private String groupGuid;
    private String groupName;
    private Boolean publish;
    private String keeper;
    private List<String> dataOwner;
    private Long maxRowNumber;
    private List<Field> fields;
    private String version;
    private String description;
    private String protocol;
    private String requestMode;
    private String returnType;
    private String path;
    private String generateTime;
    private String updater;
    private String updateTime;
    private Boolean star;
    private Boolean edit;
    private String tableDisplayName;
    private String manager;
    private Integer usedCount;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDbGuid() {
        return dbGuid;
    }

    public void setDbGuid(String dbGuid) {
        this.dbGuid = dbGuid;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getGroupGuid() {
        return groupGuid;
    }

    public void setGroupGuid(String groupGuid) {
        this.groupGuid = groupGuid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public String getKeeper() {
        return keeper;
    }

    public void setKeeper(String keeper) {
        this.keeper = keeper;
    }

    public List<String> getDataOwner() {
        return dataOwner;
    }

    public void setDataOwner(List<String> dataOwner) {
        this.dataOwner = dataOwner;
    }

    public Long getMaxRowNumber() {
        return maxRowNumber;
    }

    public void setMaxRowNumber(Long maxRowNumber) {
        this.maxRowNumber = maxRowNumber;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getRequestMode() {
        return requestMode;
    }

    public void setRequestMode(String requestMode) {
        this.requestMode = requestMode;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(String generateTime) {
        this.generateTime = generateTime;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getStar() {
        return star;
    }

    public void setStar(Boolean star) {
        this.star = star;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public String getTableDisplayName() {
        return tableDisplayName;
    }

    public void setTableDisplayName(String tableDisplayName) {
        this.tableDisplayName = tableDisplayName;
    }


    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }
    public Integer getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(Integer usedCount) {
        this.usedCount = usedCount;
    }

    public static class Field {
        private String columnName;
        private Boolean filter;
        private Boolean fill;
        private String defaultValue;
        private Boolean useDefaultValue;
        private String type;

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public Boolean getFilter() {
            return filter;
        }

        public void setFilter(Boolean filter) {
            this.filter = filter;
        }

        public Boolean getFill() {
            return fill;
        }

        public void setFill(Boolean fill) {
            this.fill = fill;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Boolean getUseDefaultValue() {
            return useDefaultValue;
        }

        public void setUseDefaultValue(Boolean useDefaultValue) {
            this.useDefaultValue = useDefaultValue;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public static class FieldWithDisplay extends Field {
        private String displayName;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setFieldInfo(Field field) {
            this.setType(field.getType());
            this.setFilter(field.getFilter());
            this.setFill(field.getFill());
            this.setDefaultValue(field.getDefaultValue());
            this.setUseDefaultValue(field.getUseDefaultValue());
            this.setColumnName(field.getColumnName());
        }
    }
}
