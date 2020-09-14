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

package io.zeta.metaspace.model.datasource;

import lombok.Data;

@Data
public class DataSourceInfo {
    private String sourceId;
    private String sourceName;
    private String sourceType;
    private String description;
    private String ip;
    private String port;
    private String userName;
    private String password;
    private String database;
    private String jdbcParameter;
    private String oracleDb;
    private String manager;
    private String managerId;
    private String serviceType;
}
