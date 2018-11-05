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

package org.apache.atlas.web.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.atlas.web.common.filetable.ActionType;
import org.apache.atlas.web.common.filetable.ColumnExt;
import org.apache.atlas.web.common.filetable.FileType;

import java.util.List;

public class UploadJobInfo {

    /**
     * 目标数据库,一般是用户自己的数据库
     */
    private String database;
    /**
     * 目标表名，前端传递的表名
     */
    private String tableName;
    /**
     * 上传的原始文件
     */
    private String filePath;
    /**
     * 文件编码
     */
    private String fileEncode;

    /**
     * 是否包含文件头
     */
    private boolean includeHeaderLine;
    /**
     * 字段分隔符
     */
    private String fieldDelimiter;

    /**
     * 目标表结构描述
     */
    private String fieldDescribe;

    /**
     * 文件类型
     */
    private FileType fileType;

    /**
     * 导入的Sheet名称
     */
    private String sheetName;

    /**
     * 上传数据任务类型: insert, append, overwrite
     */
    private ActionType actionType;

    /**
     * 表的列 数据文件中对应列的mapping
     */
    private List<ColumnExt> columns;

    public String getFieldDescribe() {
        return fieldDescribe;
    }

    public UploadJobInfo setFieldDescribe(String fieldDescribe) {
        this.fieldDescribe = fieldDescribe;
        return this;
    }


    public String getDatabase() {
        return database;
    }

    public UploadJobInfo setDatabase(String database) {
        this.database = database;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public UploadJobInfo setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public UploadJobInfo setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getFileEncode() {
        return fileEncode;
    }

    public UploadJobInfo setFileEncode(String fileEncode) {
        this.fileEncode = fileEncode;
        return this;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public UploadJobInfo setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
        return this;
    }

    public boolean isIncludeHeaderLine() {
        return includeHeaderLine;
    }

    public UploadJobInfo setIncludeHeaderLine(boolean includeHeaderLine) {
        this.includeHeaderLine = includeHeaderLine;
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

    public List<ColumnExt> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnExt> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fileEncode", fileEncode)
                .add("database", database)
                .add("tableName", tableName)
                .add("filePath", filePath)
                .add("includeHeaderLine", includeHeaderLine)
                .add("fieldDelimiter", fieldDelimiter)
                .add("fieldDescribe", fieldDescribe)
                .add("fileType", fileType)
                .add("sheetName", sheetName)
                .add("actionType", actionType.type())
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UploadJobInfo)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UploadJobInfo that = (UploadJobInfo) o;
        return Objects.equal(includeHeaderLine, that.includeHeaderLine) &&
               Objects.equal(database, that.database) &&
               Objects.equal(tableName, that.tableName) &&
               Objects.equal(filePath, that.filePath) &&
               Objects.equal(fileEncode, that.fileEncode) &&
               Objects.equal(fieldDelimiter, that.fieldDelimiter) &&
               Objects.equal(fieldDescribe, that.fieldDescribe) &&
               Objects.equal(fileType, that.fileType) &&
               Objects.equal(actionType, that.actionType) &&
               Objects.equal(sheetName, that.sheetName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), database, tableName, filePath, fileEncode, includeHeaderLine, fieldDelimiter, fieldDescribe, fileType, sheetName);
    }

}
