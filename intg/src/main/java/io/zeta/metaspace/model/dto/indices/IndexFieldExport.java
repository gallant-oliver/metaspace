package io.zeta.metaspace.model.dto.indices;
public class IndexFieldExport {

    /**
     * 指标域编码
     */
    private String code;
    /**
     * 指标域名称
     */
    private String name;
    /**
     * 父指标域编码
     */
    private String parentCode;
    /**
     * 父指标域guid
     */
    private String parentGuid;
    /**
     * 描述
     */
    private String description;

    public String getParentGuid() {
        return parentGuid;
    }

    public void setParentGuid(String parentGuid) {
        this.parentGuid = parentGuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
