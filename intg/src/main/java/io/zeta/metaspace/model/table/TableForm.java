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

package io.zeta.metaspace.model.table;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class TableForm {

    private String database;
    private String tableName;
    private String comment;
    private int tableType;
    private String expireDate;
    private List<Field> fields;
    private boolean isPartition;
    private List<Field> partitionFields;
    private String storedFormat;
    private String hdfsPath;
    private String fieldsTerminated;
    private String lineTerminated;

    public TableForm() {
    }

    public TableForm(String database, String tableName, String comment, int tableType, String expireDate, List<Field> fields, boolean isPartition, List<Field> partitionFileds, String storedFormat, String hdfsPath, String fieldsTerminated, String lineTerminated) {
        this.database = database;
        this.tableName = tableName;
        this.comment = comment;
        this.tableType = tableType;
        this.expireDate = expireDate;
        this.fields = fields;
        this.isPartition = isPartition;
        this.partitionFields = partitionFileds;
        this.storedFormat = storedFormat;
        this.hdfsPath = hdfsPath;
        this.fieldsTerminated = fieldsTerminated;
        this.lineTerminated = lineTerminated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getTableType() {
        return tableType;
    }

    public void setTableType(int tableType) {
        this.tableType = tableType;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public boolean isPartition() {
        return isPartition;
    }

    public void setIsPartition(boolean isPartition) {
        this.isPartition = isPartition;
    }

    public List<Field> getPartitionFields() {
        return partitionFields;
    }

    public void setPartitionFields(List<Field> partitionFields) {
        this.partitionFields = partitionFields;
    }

    public String getStoredFormat() {
        return storedFormat;
    }

    public void setStoredFormat(String storedFormat) {
        this.storedFormat = storedFormat;
    }

    public String getHdfsPath() {
        return hdfsPath;
    }

    public void setHdfsPath(String hdfsPath) {
        this.hdfsPath = hdfsPath;
    }

    public String getFieldsTerminated() {
        return fieldsTerminated;
    }

    public void setFieldsTerminated(String fieldsTerminated) {
        this.fieldsTerminated = fieldsTerminated;
    }

    public String getLineTerminated() {
        return lineTerminated;
    }

    public void setLineTerminated(String lineTerminated) {
        this.lineTerminated = lineTerminated;
    }

}
