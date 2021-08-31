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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Database implements Serializable {
    private String sourceId;
    private String sourceName;
    private String instanceId;
    private String databaseId;
    private String databaseName;
    private String databaseDescription;
    private String status;
    private List tableList;
    private String dbType;
    private long tableCount;
    private String owner;
    @JsonIgnore
    private int total;
}
