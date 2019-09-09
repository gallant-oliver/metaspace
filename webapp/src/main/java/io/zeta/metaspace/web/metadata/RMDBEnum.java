package io.zeta.metaspace.web.metadata;

/**
 * @author zhuxuetong
 * @date 2019-08-26 14:44
 */
public enum RMDBEnum {
    MYSQL("mysql", "jdbc:mysql://%s:%s/%s"),
    ORACLE("oracle", "jdbc:oracle:thin:@%s:%s:%s"),
    ;
    private String name;
    private String connectUrl;

    RMDBEnum(String name, String connectUrl) {
        this.name = name;
        this.connectUrl = connectUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public static RMDBEnum of(String rmdbType) throws IllegalArgumentException {
        for (RMDBEnum rmdbEnum : RMDBEnum.values()) {
            if (rmdbType.toLowerCase().equals(rmdbEnum.getName())) {
                return rmdbEnum;
            }
        }
        throw new IllegalArgumentException("不支持的数据库类型:" + rmdbType);
    }
}
