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
 * @date 2019/9/19 9:44
 */
package io.zeta.metaspace.model.metadata;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;

/*
 * @description
 * @author sunhaoning
 * @date 2019/9/19 9:44
 */
public class ColumnMetadata extends BasicMetadata {
    private String type;
    private String tableGuid;
    private Boolean partitionField;
    private Boolean hasChange = false; // 字段是否变更
    private String changeCause; // 字段变更原因 ADD(字段新增)  DELETE (字段删除) TYPECHANGE (字段类型变更)

    public ColumnMetadata() { }

    public ColumnMetadata(String guid, String name, String type, String tableGuid, String description, String status, Boolean partitionField,
                          String creator, String updater, Timestamp createTime, Timestamp updateTime) {
        this.guid = guid;
        this.name = name;
        this.type = type;
        this.tableGuid = tableGuid;
        this.description = description;
        this.status = status;
        this.partitionField = partitionField;
        this.creator = creator;
        this.updater = updater;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTableGuid() {
        return tableGuid;
    }

    public void setTableGuid(String tableGuid) {
        this.tableGuid = tableGuid;
    }

    public Boolean getHasChange() {
        return hasChange;
    }

    public void setHasChange(Boolean hasChange) {
        this.hasChange = hasChange;
    }

    public String getChangeCause() {
        return changeCause;
    }

    public void setChangeCause(String changeCause) {
        this.changeCause = changeCause;
    }

    public Boolean getPartitionField() {
        return partitionField;
    }

    public void setPartitionField(Boolean partitionField) {
        this.partitionField = partitionField;
    }

    public Boolean compareColumn(ColumnMetadata columnMetadata) {
        if (StringUtils.isNotBlank(this.guid) && !this.guid.equals(columnMetadata.getGuid())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.name) && !this.name.equals(columnMetadata.getName())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.type) && !this.type.equals(columnMetadata.getType())) {
            return false;
        }
        if (StringUtils.isNotBlank(this.status) && !this.status.equals(columnMetadata.getStatus())) {
            return false;
        }
        return true;
    }

}
