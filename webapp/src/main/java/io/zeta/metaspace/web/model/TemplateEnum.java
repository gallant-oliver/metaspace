package io.zeta.metaspace.web.model;

public enum TemplateEnum {

    CATEGORY_TEMPLATE("category_template.xlsx", new String[][]{{"目录名字", "目录描述"}}),
    DATA_STANDARD_TEMPLATE("data_standard_template.xlsx", new String[][]{{"标准编号", "标准内容", "标准描述"}, {"XXXAAA01", "内容内容内容内容内容", "描述描述描述描述"}}),
    BUSINESS_TEMPLATE("business_template.xlsx", new String[][]{{"", "业务对象名称", "业务模块", "业务描述", "所有者", "管理者", "维护者", "相关数据资产"}, {"业务对象1"}});

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
