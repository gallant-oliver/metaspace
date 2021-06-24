package io.zeta.metaspace.connector.oracle;

import java.sql.DriverManager;
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
		oracle.jdbc.OracleDriver driver= new oracle.jdbc.OracleDriver();
		DriverManager.registerDriver(driver);
		Map<String, String> configMap = getConfigMap();

		OracleSourceConnector connctor = new OracleSourceConnector();
		connctor.start(configMap);

		OracleSourceTask task = new OracleSourceTask();
		task.start(configMap);

//		while(true){
//			Thread.sleep(30000);
			task.poll();
//		}
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
				put("name", "oracle-logminer-connector");
				put("connector.class", "com.ecer.kafka.connect.oracle.OracleSourceConnector");
				put("tasks.max", "1");
				put("topic", "testTopic");
				put("db.name", "orcl");
				put("db.hostname", "localhost");
				put("db.port", "1521");
				put("db.user", "myTest");
				put("db.user.password", "test");
			}
		};
		return configMap;
	}

}
