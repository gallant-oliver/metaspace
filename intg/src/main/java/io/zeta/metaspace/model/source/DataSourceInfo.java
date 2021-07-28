package io.zeta.metaspace.model.source;

import lombok.Data;

@Data
public class DataSourceInfo {
    /**
     * 数据源Id
     */
    private String datasourceId;

    /**
     * 数据源名称
     */
    private String datasourceName;
}
