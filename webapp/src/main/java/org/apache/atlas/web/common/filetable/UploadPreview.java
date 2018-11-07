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

package org.apache.atlas.web.common.filetable;

import com.gridsum.gdp.library.commons.data.generic.GenericData;
import com.gridsum.gdp.library.commons.data.schema.Column;
import com.gridsum.gdp.library.commons.data.response.RowsXmlAdapter;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class UploadPreview {
    @XmlElement
    private String fieldDelimiter = "";
    @XmlElement
    private String fileEncode = "";
    @XmlElement
    private boolean includeHeader;
    @XmlElementWrapper(name = "headers")
    @XmlElement(name = "header")
    private List<ColumnExt> headers;
    @XmlElement
    private Integer size;
    @XmlElement(name = "rows")
    @XmlJavaTypeAdapter(RowsXmlAdapter.class)
    private List<List<String>> rows;
    @XmlElement
    private FileType fileType;
    @XmlElement
    private List<String> sheets = new ArrayList<>();
    @XmlElement
    private String previewInfo = "";
    @XmlElement
    private List<String> tableHeads;

    public List<String> getTableHeads() {
        return tableHeads;
    }

    public void setTableHeads(List<String> tableHeads) {
        this.tableHeads = tableHeads;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public UploadPreview setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
        return this;
    }

    public String getFileEncode() {
        return fileEncode;
    }

    public UploadPreview setFileEncode(String fileEncode) {
        this.fileEncode = fileEncode;
        return this;
    }

    public List<ColumnExt> getHeaders() {
        return headers;
    }

    public UploadPreview setHeaders(List<ColumnExt> headers) {
        this.headers = headers;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public UploadPreview setSize(Integer size) {
        this.size = size;
        return this;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public UploadPreview setRows(List<List<String>> rows) {
        this.rows = rows;
        return this;
    }

    public boolean isIncludeHeader() {
        return includeHeader;
    }

    public UploadPreview setIncludeHeader(boolean includeHeader) {
        this.includeHeader = includeHeader;
        return this;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public List<String> getSheets() {
        return sheets;
    }

    public void setSheets(List<String> sheets) {
        this.sheets = sheets;
    }

    public String getPreviewInfo() {
        return previewInfo;
    }

    public void setPreviewInfo(String previewInfo) {
        this.previewInfo = previewInfo;
    }


}
