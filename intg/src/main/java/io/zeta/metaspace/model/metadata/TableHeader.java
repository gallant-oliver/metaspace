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
 * @date 2019/2/27 9:53
 */
package io.zeta.metaspace.model.metadata;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
 * @description
 * @author sunhaoning
 * @date 2019/2/27 9:53
 */
@Data
public class TableHeader {

    private String tableId;
    private String tableName;
    private String databaseId;
    private String databaseName;
    private String createTime;
    private String status;
    private String displayName;
    private String displayUpdateTime;
    private String displayOperator;
    private String comment;
    private String databaseStatus;

}
