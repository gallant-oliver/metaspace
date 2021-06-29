package org.apache.atlas.util;

import org.apache.atlas.notification.rdbms.KafkaConnector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

public class TestKafkaConnector {

    @Test
    public void testConnector(){

        String connectorName = "10.200.64.102:1521:orcl";
        KafkaConnector.Instance instance = new KafkaConnector.Instance();
        instance.setName(connectorName);
        Properties properties = new Properties();
        properties.setProperty("name", connectorName);
        properties.setProperty("connector.class", "io.zeta.metaspace.connector.oracle.OracleSourceConnector");
        properties.setProperty("tasks.max", "1");
        properties.setProperty("db.hostname", "10.200.64.102");
        properties.setProperty("db.user", "test");
        properties.setProperty("db.port", "2181");
        properties.setProperty("db.user.password", "123456");
        properties.setProperty("db.name", "orcl");
        instance.setConfig(properties);


        KafkaConnector.Instance connector = KafkaConnector.addConnector(instance);
        Assert.assertEquals(connector.getName(), instance.getName());

        connector = KafkaConnector.getConnector(connectorName, false);
        Assert.assertEquals(connector.getName(), instance.getName());
        connector = KafkaConnector.getConnector(connectorName);
        Assert.assertEquals(connector.getName(), instance.getName());
        KafkaConnector.removeConnector(connectorName);

        connector = KafkaConnector.getConnector(connectorName);

        Assert.assertNull(connector);
    }
}
