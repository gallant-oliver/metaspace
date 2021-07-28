package io.zeta.metaspace.model.sourceinfo;

import lombok.Data;

/**
 * 导入文件的解析冲突结果
 */
@Data
public class AnalyticResult {
    private String databaseTypeName;
    private String dataSourceName;
    private String databaseInstanceName;
    private String databaseName;
    private String databaseAlias;
    private String categoryName;
    private String errorMessage;
}
