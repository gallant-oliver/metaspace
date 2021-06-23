package io.zeta.metaspace.connector.oracle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		config = new OracleSourceConnectorConfig(map);

		String dbName = config.getDbName();
		if (dbName.equals("")) {
			throw new ConnectException("Missing Db Name property");
		}
		String tableWhiteList = config.getTableWhiteList();
		if ((tableWhiteList == null)) {
			throw new ConnectException("Could not find schema or table entry for connector to capture");
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