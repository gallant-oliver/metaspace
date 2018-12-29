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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@JsonAutoDetect(getterVisibility = PUBLIC_ONLY, setterVisibility = PUBLIC_ONLY, fieldVisibility = NONE)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class TableStat implements Cloneable {

    private String tableId;
    private String tableName;
    private String date;
    private String dateType;
    private Integer fieldNum;
    private Long fileNum;
    private Long recordNum;
    private String dataVolume;
    private Long dataVolumeBytes;
    private String dataIncrement;
    private Long dataIncrementBytes;

    private Double dataVolumeNum;
    private String dataVolumeNumUnit;

    private Double dataIncrementNum;
    private String dataIncrementNumUnit;
    private List<Table> sourceTable = new ArrayList<>();

    public TableStat() {
    }

    public TableStat(String tableId, String tableName, String date, String dateType, Integer fieldNum, Long fileNum, Long recordNum, String dataVolume, Long dataVolumeBytes, String dataIncrement, Long dataIncrementBytes, List<Table> sourceTable) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.date = date;
        this.dateType = dateType;
        this.fieldNum = fieldNum;
        this.fileNum = fileNum;
        this.recordNum = recordNum;
        this.dataVolume = dataVolume;
        this.dataVolumeBytes = dataVolumeBytes;
        this.dataIncrement = dataIncrement;
        this.dataIncrementBytes = dataIncrementBytes;
        this.sourceTable = sourceTable;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDateType() {
        return dateType;
    }

    public void setDateType(String dateType) {
        this.dateType = dateType;
    }

    public Integer getFieldNum() {
        return fieldNum;
    }

    public void setFieldNum(Integer fieldNum) {
        this.fieldNum = fieldNum;
    }

    public Long getFileNum() {
        return fileNum;
    }

    public void setFileNum(Long fileNum) {
        this.fileNum = fileNum;
    }

    public Long getRecordNum() {
        return recordNum;
    }

    public void setRecordNum(Long recordNum) {
        this.recordNum = recordNum;
    }

    public String getDataVolume() {
        return dataVolume;
    }

    public void setDataVolume(String dataVolume) {
        this.dataVolume = dataVolume;
    }

    public Long getDataVolumeBytes() {
        return dataVolumeBytes;
    }

    public void setDataVolumeBytes(Long dataVolumeBytes) {
        this.dataVolumeBytes = dataVolumeBytes;
    }

    public String getDataIncrement() {
        return dataIncrement;
    }

    public void setDataIncrement(String dataIncrement) {
        this.dataIncrement = dataIncrement;
    }

    public Double getDataVolumeNum() {
        return dataVolumeNum;
    }

    public void setDataVolumeNum(Double dataVolumeNum) {
        this.dataVolumeNum = dataVolumeNum;
    }

    public String getDataVolumeNumUnit() {
        return dataVolumeNumUnit;
    }

    public void setDataVolumeNumUnit(String dataVolumeNumUnit) {
        this.dataVolumeNumUnit = dataVolumeNumUnit;
    }

    public Double getDataIncrementNum() {
        return dataIncrementNum;
    }

    public void setDataIncrementNum(Double dataIncrementNum) {
        this.dataIncrementNum = dataIncrementNum;
    }

    public String getDataIncrementNumUnit() {
        return dataIncrementNumUnit;
    }

    public void setDataIncrementNumUnit(String dataIncrementNumUnit) {
        this.dataIncrementNumUnit = dataIncrementNumUnit;
    }

    public List<Table> getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(List<Table> sourceTable) {
        this.sourceTable = sourceTable;
    }

    public Long getDataIncrementBytes() {
        return dataIncrementBytes;
    }

    public void setDataIncrementBytes(Long dataIncrementBytes) {
        this.dataIncrementBytes = dataIncrementBytes;
    }

    @Override
    public String toString() {
        return "TableStat{" +
               "tableId='" + tableId + '\'' +
               ", tableName='" + tableName + '\'' +
               ", date='" + date + '\'' +
               ", dateType='" + dateType + '\'' +
               ", fieldNum=" + fieldNum +
               ", fileNum=" + fileNum +
               ", recordNum=" + recordNum +
               ", dataVolume='" + dataVolume + '\'' +
               ", dataVolumeBytes=" + dataVolumeBytes +
               ", dataIncrement='" + dataIncrement + '\'' +
               ", dataVolumeNum=" + dataVolumeNum +
               ", dataVolumeNumUnit='" + dataVolumeNumUnit + '\'' +
               ", dataIncrementNum=" + dataIncrementNum +
               ", dataIncrementNumUnit='" + dataIncrementNumUnit + '\'' +
               ", sourceTable=" + sourceTable +
               '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        TableStat ret = (TableStat) super.clone();
        List<Table> sourceTable = getSourceTable().stream().map(table -> {
            try {
                return (Table) table.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }).collect(Collectors.toList());
        ret.setSourceTable(sourceTable);
        return ret;
    }
}
