package io.zeta.metaspace.connector.oracle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.sql.DriverManager;

import oracle.jdbc.driver.OracleDriver;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;

public class OracleSourceConnector extends SourceConnector {

	private OracleSourceConnectorConfig config;

	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	@Override
	public void start(Map<String, String> map) {

		try{
			OracleDriver driver= new OracleDriver();
			DriverManager.registerDriver(driver);
			config = new OracleSourceConnectorConfig(map);
		}catch (SQLException e){
			throw new ConnectException("OracleDriver is not found");
		}
	}

	@Override
	public Class<? extends Task> taskClass() {
		return OracleSourceTask.class;
	}

	@Override
	public List<Map<String, String>> taskConfigs(int i) {
		ArrayList<Map<String, String>> configs = new ArrayList<>(1);
		configs.add(config.originalsStrings());
		return configs;
	}

	@Override
	public void stop() {
		if (OracleSourceTask.getThreadConnection() != null) {
			try {
				OracleSourceTask.closeDbConn();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public ConfigDef config() {
		return OracleSourceConnectorConfig.conf();
	}
}