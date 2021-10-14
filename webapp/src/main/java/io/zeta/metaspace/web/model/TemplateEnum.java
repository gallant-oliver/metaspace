package io.zeta.metaspace.web.model;

public enum TemplateEnum {

    ALL_CATEGORY_TEMPLATE("all_category_template.xlsx", new String[][]{{"目录id","目录名字","目录描述","同级的上方目录id","同级的下方目录id","父目录id","全名称","级别","排序"}}),
    CATEGORY_TEMPLATE("category_template.xlsx", new String[][]{{"目录名字", "目录描述"}}),
    DATA_STANDARD_TEMPLATE("data_standard_template.xlsx", new String[][]{{"标准编号", "标准内容", "标准描述"}, {"XXXAAA01", "内容内容内容内容内容", "描述描述描述描述"}}),
    BUSINESS_TEMPLATE("business_template.xlsx", new String[][]{{"", "业务对象名称", "业务模块", "业务描述", "所有者", "管理者", "维护者", "相关数据资产"}, {"业务对象1"}}),
    INDEX_FIELD_TEMPLATE("index_field_template.xlsx", new String[][]{{"注释","编码","名称","父指标域编码","描述"},
            {"填写规则（不要删除）","请输入英文和数字，不区分大小写","请输入中文、英文或数字","二级指标域请输入其父指标域的编码；一级指标域不用填写","请输入描述，不超过200个字符"},
            {"范例（不要删除）","XS001","销售域","父指标域编码","描述"}});

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
