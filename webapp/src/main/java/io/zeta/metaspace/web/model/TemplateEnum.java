package io.zeta.metaspace.web.model;

public enum TemplateEnum {
    
    ALL_CATEGORY_TEMPLATE("all_category_template.xlsx",
            new String[][]{{"目录id", "目录名字", "目录描述", "同级的上方目录id", "同级的下方目录id", "父目录id", "全名称", "级别", "排序"}}),
    CATEGORY_TEMPLATE("category_template.xlsx", new String[][]{{"目录名字", "目录描述"}}),
    DATA_STANDARD_TEMPLATE("data_standard_template.xlsx",
            new String[][]{
                    {"标准编号", "标准名称", "标准类型", "数据类型", "数据长度", "是否有允许值", "允许值", "标准层级", "标准描述"},
                    {"只允许英文、数字、下划线、中划线", "只允许中文、英文、数字、下划线、中划线", "枚举值:\n数据标准\n命名标准",
                            "枚举值:\nSTRING\nDOUBLE\nBIGINT\nBOOLEAN\nDECIMAL\nDATE\nTIMESTAMP",
                            "只允许正整数", "布尔型: true 或 false", "多个值以;分割", "枚举值:\n贴源层\n基础层\n通用层\n应用层", ""}
            }),
    BUSINESS_TEMPLATE("business_template.xlsx",
            new String[][]{{"业务对象名称", "业务模块", "业务描述", "所有者", "管理者", "维护者", "相关数据资产"}}),
    INDEX_FIELD_TEMPLATE("index_field_template.xlsx",
            new String[][]{{"注释", "编码", "名称", "父指标域编码", "描述"},
                    {"填写规则（不要删除）", "请输入英文和数字，不区分大小写", "请输入中文、英文或数字", "二级指标域请输入其父指标域的编码；一级指标域不用填写", "请输入描述，不超过200个字符"},
                    {"范例（不要删除）", "XS001", "销售域", "父指标域编码", "描述"}});
    
    private String fileName;
    private String[][] content;
    
    TemplateEnum(String fileName, String[][] content) {
        this.fileName = fileName;
        this.content = content;
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String[][] getContent() {
        return content;
    }

    public void setContent(String[][] content) {
        this.content = content;
    }
}
