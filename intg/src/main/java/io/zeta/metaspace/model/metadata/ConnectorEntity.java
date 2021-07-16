package io.zeta.metaspace.model.metadata;

import lombok.Data;

@Data
public class ConnectorEntity {

    private String id;

    private String connectorName;

    private String connectorClass;

    private String type;

    private String dbIp;

    private String dbPort;

    private String pdbName;

    private String connectorUnit;

    private String userName;

    private String passWord;

}
