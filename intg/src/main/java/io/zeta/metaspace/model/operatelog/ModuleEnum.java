package io.zeta.metaspace.model.operatelog;

/**
 * @author zhuxuetong
 * @date 2019-08-07 18:07
 */
public enum ModuleEnum {
    HOME("概览", "home"),
    TECHNICAL("技术数据", "technical"),
    BUSINESS("业务对象", "business"),
    METADATA("元数据管理", "metadata"),
    BUSINESSMANAGE("业务对象管理", "businessmanage"),
    DATASHARE("数据分享", "datashare"),
    DATASTANDARD("数据标准", "datastandard"),
    DATAQUALITY("数据质量", "dataquality"),
    USER("用户管理", "user"),
    ROLE("角色管理", "role"),
    PRIVILEGE("权限管理", "privilege"),
    ;

    private String name;
    private String alias;

    ModuleEnum(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
