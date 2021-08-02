package org.apache.atlas.util;

import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import org.apache.atlas.notification.rdbms.KafkaConnectorUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestKafkaConnector {

    @Test
    public void testConnector(){

        String connectorName = "10.200.64.102:1521:orcl";
        KafkaConnector kafkaConnector = new KafkaConnector();
        kafkaConnector.setName(connectorName);
        KafkaConnector.Config config = new KafkaConnector.Config();
        config.setName(connectorName);
        config.setConnectorClass("io.zeta.metaspace.connector.oracle.OracleSourceConnector");
        config.setTasksMax(1);
        config.setDbIp("10.200.64.102");
        config.setDbPort(1521);
        config.setDbUser("test");
        config.setDbPassword("123456");
        config.setDbName("orcl");
        kafkaConnector.setConfig(config);


        boolean isStart = KafkaConnectorUtil.startConnector(kafkaConnector);
        Assert.assertEquals(isStart, true);

        KafkaConnector connector = KafkaConnectorUtil.getConnector(connectorName, false);
        Assert.assertEquals(connector.getName(), kafkaConnector.getName());
        connector = KafkaConnectorUtil.getConnector(connectorName);
        Assert.assertEquals(connector.getName(), kafkaConnector.getName());
        KafkaConnectorUtil.stopConnector(connectorName);

        connector = KafkaConnectorUtil.getConnector(connectorName);

        Assert.assertNull(connector);
    }
}
