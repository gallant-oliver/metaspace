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
 * @date 2019/2/12 13:52
 */
package io.zeta.metaspace.model.business;

import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/12 13:52
 */
public class BusinessInfo {
    private String businessId;
    private String name;
    private String departmentId;
    private String module;
    private String description;
    private String owner;
    private String manager;
    private String maintainer;
    private String dataAssets;
    private String businessLastUpdate;
    private String businessOperator;
    private String technologyLastUpdate;
    private String technologyOperator;
    private List<Table> tables;

    public class Table {
        private String tableGuid;
        private String dbName;
        private String tableName;
        private String createTime;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public String getDataAssets() {
        return dataAssets;
    }

    public void setDataAssets(String dataAssets) {
        this.dataAssets = dataAssets;
    }

    public String getBusinessLastUpdate() {
        return businessLastUpdate;
    }

    public void setBusinessLastUpdate(String businessLastUpdate) {
        this.businessLastUpdate = businessLastUpdate;
    }

    public String getBusinessOperator() {
        return businessOperator;
    }

    public void setBusinessOperator(String businessOperator) {
        this.businessOperator = businessOperator;
    }

    public String getTechnologyLastUpdate() {
        return technologyLastUpdate;
    }

    public void setTechnologyLastUpdate(String technologyLastUpdate) {
        this.technologyLastUpdate = technologyLastUpdate;
    }

    public String getTechnologyOperator() {
        return technologyOperator;
    }

    public void setTechnologyOperator(String technologyOperator) {
        this.technologyOperator = technologyOperator;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }
}
