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

package io.zeta.metaspace.web.common.filetable;

import com.google.common.base.Objects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class UploadConfig {

    @XmlElement
    private String database;
    @XmlElement
    private String tableName;
    @XmlElement
    private boolean includeHeaders;
    @XmlElement
    private String fieldDelimiter;
    @XmlElement
    private String fileEncode;
    @XmlElementWrapper(name = "columns")
    @XmlElement(name = "column")
    private List<ColumnExt> columns;
    @XmlElement
    private String sheetName;
    @XmlElement
    private FileType fileType;
    @XmlElement
    private ActionType actionType;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UploadConfig)) {
            return false;
        }
        UploadConfig that = (UploadConfig) o;
        return Objects.equal(database, that.database) &&
               Objects.equal(tableName, that.tableName) &&
               Objects.equal(fieldDelimiter, that.fieldDelimiter) &&
               Objects.equal(fileEncode, that.fileEncode) &&
               Objects.equal(columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(database, tableName, fieldDelimiter, fileEncode, columns);
    }

    public boolean isIncludeHeaders() {
        return includeHeaders;
    }

    public UploadConfig setIncludeHeaders(boolean includeHeaders) {
        this.includeHeaders = includeHeaders;
        return this;
    }

    public String getDatabase() {
        return database;
    }

    public UploadConfig setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public UploadConfig setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public UploadConfig setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
        return this;
    }

    public String getFileEncode() {
        return fileEncode;
    }

    public UploadConfig setFileEncode(String fileEncode) {
        this.fileEncode = fileEncode;
        return this;
    }

    public List<ColumnExt> getColumns() {
        return columns;
    }

    public UploadConfig setColumns(List<ColumnExt> columns) {
        this.columns = columns;
        return this;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
}
