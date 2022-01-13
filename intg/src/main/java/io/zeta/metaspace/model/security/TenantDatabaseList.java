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

package io.zeta.metaspace.model.security;

import lombok.Data;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/3/18 14:44
 */
public class TenantDatabaseList {
    List<TenantDatabase> tenantDatabaseList;

    public List<TenantDatabase> getTenantDatabaseList() {
        return tenantDatabaseList;
    }

    public void setTenantDatabaseList(List<TenantDatabase> tenantDatabaseList) {
        this.tenantDatabaseList = tenantDatabaseList;
    }

    public static class TenantDatabase{
        private String tenantId;
        private List<Database> databases;

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public List<Database> getDatabases() {
            return databases;
        }

        public void setDatabases(List<Database> databases) {
            this.databases = databases;
        }
    }

    @Data
    public static class Database{
        private String name;
        private String tableCount;
        private String ownerName;
        private String tenantId;
        private String projectName;
    }

}
