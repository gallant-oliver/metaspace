package io.zeta.metaspace.model.usergroup;

public enum AuthMenuEnum {

    MEMBER("成员", 1),
    TECHNICAL_DIRECTORY_PERMISSIONS("技术目录权限", 2),
    BUSINESS_DIRECTORY_PERMISSIONS("业务目录权限", 3),
    DATA_SOURCE_PERMISSIONS("数据源权限", 4),
    PROJECT_PERMISSIONS("项目权限", 5),
    INDICATOR_DOMAIN("指标域权限", 6);

    private String name;
    private int num;

    AuthMenuEnum(String name, int num) {

        this.name = name;
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }
}
