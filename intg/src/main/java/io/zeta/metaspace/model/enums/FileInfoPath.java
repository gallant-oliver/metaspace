package io.zeta.metaspace.model.enums;

import lombok.Data;

/**
 * @author huangrongwen
 * @Description: 文件归档导入路径
 * @date 2022/7/1214:59
 */
public enum FileInfoPath {
    TECHNICAL_CATEGORY("技术目录","数据资产/技术目录"),
    BUSINESS_CATEGORY("业务目录","数据资产/业务目录/导入目录"),
    INDICATORS_CATEGORY("指标目录","指标设计/业务指标/导入目录"),
    BUSINESS_INDICATORS("业务指标","指标设计/业务指标/导入指标"),
    BUSINESS_OBJECT("业务对象","数据资产/业务目录/业务对象"),
    DATABASE("数据库登记","数据资产/源信息登记/数据库登记"),
    DRIVE_TABLE("衍生表登记","数据资产/源信息登记/衍生表登记"),
    RULE_CATEGORY("规则目录","数据质量/规则管理"),
    STANDARD_CATEGORY("数据标准目录","数据质量/数据标准"),
    STANDARD("数据标准目录","数据质量/数据标准/标准导入"),
    API_CATEGORY("api目录","数据服务/API项目管理/API管理"),
    DEMAND_MANAGEMENT_EDIT("需求管理","需求管理"),
    DEMAND_MANAGEMENT("需求管理修改","租户资产/业务目录/业务对象详情/资源列表/创建需求"),
    ATOM_INDICATOR("原子指标","指标设计/技术指标/原子指标");


    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
    private String name;
    private String path;
    FileInfoPath(String name, String path) {
        this.name = name;
        this.path = path;
    }
}