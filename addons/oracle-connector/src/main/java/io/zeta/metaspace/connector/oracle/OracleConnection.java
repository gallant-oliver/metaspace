package io.zeta.metaspace.connector.oracle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author T480
 */
public class OracleConnection{    
    
    public Connection connect(OracleSourceConnectorConfig config) throws SQLException{
        return DriverManager.getConnection(
            "jdbc:oracle:thin:@"+config.getDbHostName()+":"+config.getDbPort()+"/"+config.getDbName(),
            config.getDbUser(),
            config.getDbUserPassword());
    }
}