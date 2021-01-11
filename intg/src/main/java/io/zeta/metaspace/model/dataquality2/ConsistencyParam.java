package io.zeta.metaspace.model.dataquality2;

import lombok.Data;

import java.util.List;

@Data
public class ConsistencyParam {
    //一致性参数唯一标识
    String id;
    //数据源 id
    String dataSourceId;
    String schema;
    String table;
    //基准字段
    List<String> joinFields;
    //比较字段
    List<String> compareFields;
    //是否是基准数据源
    boolean isStandard;
    String column;
    String dataSourceName;

}
