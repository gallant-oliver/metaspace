package io.zeta.metaspace.model.usergroup;

public enum AuthMenuEnum {

    MEMBER("member", 1),
    TECHNICAL_DIRECTORY_PERMISSIONS("technical", 2),
    BUSINESS_DIRECTORY_PERMISSIONS("business", 3),
    DATA_SOURCE_PERMISSIONS("data-source", 4),
    PROJECT_PERMISSIONS("project", 5),
    INDICATOR_DOMAIN("norm", 6),
    DATA_BASE_PERMISSIONS("data-base", 7),
    INDEX_DIRECTORY_PERMISSIONS("index", 9);

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
