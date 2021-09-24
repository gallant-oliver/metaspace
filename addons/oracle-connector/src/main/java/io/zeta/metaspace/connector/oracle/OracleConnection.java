package io.zeta.metaspace.connector.oracle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *  
 * @author Erdem Cer (erdemcer@gmail.com)
 */

public class OracleConnection{
    private static final String SID = "SID";
    static final Logger LOG = LoggerFactory.getLogger(OracleConnection.class);
    static{
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }catch (ClassNotFoundException e){
            LOG.error("加载oracle.jdbc.driver.OracleDriver驱动失败", e);
        }
    }
    
    public Connection connect(OracleSourceConnectorConfig config) throws SQLException {

        String dbPassword = PassWordUtils.aesDecode(config.getDbPassword());
        String symbol = SID.equalsIgnoreCase(config.getServiceType()) ? ":" : "/";
        String jdbcUrl = "jdbc:oracle:thin:@"+config.getDbIp()+":"+config.getDbPort()+symbol+config.getDbName();
        LOG.info("serviceType:{} ,连接url:{}",config.getServiceType(), jdbcUrl);
        return DriverManager.getConnection(
                jdbcUrl,
            config.getDbUser(),
                dbPassword);
    }
}