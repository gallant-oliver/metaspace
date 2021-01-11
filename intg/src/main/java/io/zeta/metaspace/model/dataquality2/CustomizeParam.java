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

package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

import java.util.List;

/**
 * @author lixiang03
 * @Data 2020/11/16 10:03
 */
@Data
public class CustomizeParam {
    //一致性参数唯一标识
    String id;
    //数据源 id
    String dataSourceId;
    String dataSourceName;
    String schema;
    String table;
    String column;
    String name;
}
