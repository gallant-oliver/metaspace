package io.zeta.metaspace.connector.oracle;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


public class ConnectorTest {

	static final Logger LOG = LoggerFactory.getLogger(ConnectorTest.class);

	@Test
	public void testLogminer() throws SQLException, InterruptedException{

		Map<String, String> configMap = getConfigMap();

		OracleSourceConnector connctor = new OracleSourceConnector();
		connctor.start(configMap);

		OracleSourceTask task = new OracleSourceTask();
		task.start(configMap);

		while(true){
			Thread.sleep(5000);
			task.poll();
		}
	}
	@Test
	public void testVersion() {
		String version = VersionUtil.getVersion();
		LOG.info(version);
	}

	private Map<String, String> getConfigMap(){
		Map<String, String> configMap = new HashMap<String, String>(){
			private static final long serialVersionUID = 1L;
			{
				put("name", "test");
				put("connector.class", "io.zeta.metaspace.connector.oracle.OracleSourceConnector");
				put("tasks.max", "1");
				put("rest.port", "8083");
				put("db.name", "orcl");
				put("db.ip", "10.141.0.111");
				put("db.port", "1521");
				put("db.user", "myTest");
				put("db.password", "test");
			}
		};
		return configMap;
	}

}
