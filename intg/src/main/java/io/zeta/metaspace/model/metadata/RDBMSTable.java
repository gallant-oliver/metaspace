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

package io.zeta.metaspace.model.metadata;

import io.zeta.metaspace.model.table.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lixiang03
 * @Data 2019/10/12 14:40
 */
@Data
public class RDBMSTable implements Serializable {
    private String tableId;
    private String tableName;
    private String tableDescription;
    private String status;
    private String updateTime;
    private String createTime;
    private String databaseId;
    private String databaseName;
    private String databaseStatus;
    private String sourceId;
    private String sourceName;
    private String sourceStatus;
    private String sourceType;
    private String displayName;
    private List<RDBMSColumn> columns;
    private List<RDBMSForeignKey> foreignKeys;
    private List<RDBMSIndex> indexes;
    private boolean subscribeTo;
    private String owner;
    private Boolean importancePrivilege;
    private Boolean securityPrivilege;
    private String subordinateSystem;
    private String subordinateDatabase;
    private String systemAdmin;

    private String dataWarehouseAdmin;
    private String dataWarehouseDescription;

    private List<String> relations;
    private String catalogAdmin;
    private String relationTime;

    private List<Table.BusinessObject> businessObjects;
    private boolean edit;
    private List<DataOwnerHeader> dataOwner;
    private List<Tag> tags;
    private boolean hasDerivetable;
    private String sourceTreeId;
    private String dbTreeId;
    //是否重要
    private boolean importance;
    //是否保密
    private boolean security;
}
