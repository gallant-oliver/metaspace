package io.zeta.metaspace.model.dto.indices;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 原子指标模板
 */
@Data
public class IndexTemplateAtomDTO {

    public IndexTemplateAtomDTO() {
        this.name = "";
        this.identification = "";
        this.description = "";
        this.central = "";
        this.field = "";
        this.source = "";
        this.dbName = "";
        this.tableName = "";
        this.columnName = "";
        this.businessCaliber = "";
        this.businessLeader = "";
        this.technicalCaliber = "";
        this.technicalLeader = "";
        this.approve = "";
    }

    /**
     * 指标名称
     */
    private String name;

    /**
     * 指标标识
     */
    private String identification;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否核心指标
     */
    private String central;

    /**
     * 指标域
     */
    private String field;

    /**
     * 数据源
     */
    private String source;

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 字段名称
     */
    private String columnName;

    /**
     * 业务口径
     */
    private String businessCaliber;

    /**
     * 业务负责人
     */
    private String businessLeader;

    /**
     * 技术口径
     */
    private String technicalCaliber;

    /**
     * 技术负责人
     */
    private String technicalLeader;

    /**
     * 审批管理
     */
    private String approve;

    /**
     * 判断必填字段是否为空
     *
     * @return
     */
    public String checkFieldsIsNull() {
        if (StringUtils.isBlank(this.name)) {
            return "指标名称不能为空";
        } else if (StringUtils.isBlank(this.identification)) {
            return "指标标识不能为空";
        } else if (StringUtils.isBlank(this.field)) {
            return "指标域不能为空";
        } else if (StringUtils.isBlank(this.source)) {
            return "数据源不能为空";
        } else if (StringUtils.isBlank(this.dbName)) {
            return "数据库不能为空";
        } else if (StringUtils.isBlank(this.tableName)) {
            return "数据表不能为空";
        } else if (StringUtils.isBlank(this.columnName)) {
            return "字段不能为空";
        } else if (StringUtils.isBlank(this.businessCaliber)) {
            return "业务口径不能为空";
        } else if (StringUtils.isBlank(this.businessLeader)) {
            return "业务负责人不能为空";
        } else if (StringUtils.isBlank(this.approve)) {
            return "审批管理不能为空";
        }
        return "";
    }

    public Boolean checkTitle() {
        if (!"指标名称*".equals(this.name)) {
            return false;
        }
        if (!"指标标识*".equals(this.identification)) {
            return false;
        }
        if (!"描述".equals(this.description)) {
            return false;
        }
        if (!"是否核心指标".equals(this.central)) {
            return false;
        }
        if (!"指标域*".equals(this.field)) {
            return false;
        }
        if (!"数据源*".equals(this.source)) {
            return false;
        }
        if (!"数据库*".equals(this.dbName)) {
            return false;
        }
        if (!"数据表*".equals(this.tableName)) {
            return false;
        }
        if (!"字段*".equals(this.columnName)) {
            return false;
        }
        if (!"业务口径*".equals(this.businessCaliber)) {
            return false;
        }
        if (!"业务负责人*".equals(this.businessLeader)) {
            return false;
        }
        if (!"技术口径".equals(this.technicalCaliber)) {
            return false;
        }
        if (!"技术负责人".equals(this.technicalLeader)) {
            return false;
        }
        if (!"审批管理*".equals(this.approve)) {
            return false;
        }
        return true;
    }
}
