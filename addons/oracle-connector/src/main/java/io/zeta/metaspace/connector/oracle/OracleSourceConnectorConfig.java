package io.zeta.metaspace.connector.oracle;

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleSourceConnectorConfig extends AbstractConfig {

	static final Logger log = LoggerFactory.getLogger(OracleSourceConnectorConfig.class);

	public static final String NAME = "name";
	public static final String TOPIC_CONFIG = "topic";
	public static final String DB_NAME_CONFIG = "db.name";
	public static final String DB_IP_CONFIG = "db.ip";
	public static final String DB_PORT_CONFIG = "db.port";
	public static final String DB_USER_CONFIG = "db.user";
	public static final String DB_PASSWORD_CONFIG = "db.password";
	public static final String DB_FETCH_SIZE = "db.fetch.size";
	public static final String START_SCN = "start.scn";
	public static final String SERVICE_TYPE = "conn.type"; //oracle SID 或者 SERVICE_NAME

	private static final String ORACLE_CONNECTOR_CLASS = "io.zeta.metaspace.connector.oracle.OracleSourceConnector";
	public OracleSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
		super(config, parsedConfig);
	}

	public OracleSourceConnectorConfig(Map<String, String> parsedConfig) {
		this(conf(), parsedConfig);
	}

	public static ConfigDef conf() {
		return new ConfigDef()
				.define(NAME, Type.STRING, ORACLE_CONNECTOR_CLASS,Importance.LOW, "Connector name")
				.define(TOPIC_CONFIG, Type.STRING, OracleConnectorConstant.DEFAULT_TOPIC, Importance.LOW, "Topic")
				.define(DB_NAME_CONFIG, Type.STRING, "", Importance.LOW, "Db Name")
				.define(DB_IP_CONFIG, Type.STRING,  "", Importance.LOW, "Db Ip")
				.define(DB_PORT_CONFIG, Type.INT,  0, Importance.LOW, "Db Port")
				.define(DB_USER_CONFIG, Type.STRING,  "", Importance.LOW, "Db User")
				.define(DB_PASSWORD_CONFIG, Type.STRING,  "", Importance.LOW, "Db Password")
				.define(DB_FETCH_SIZE, Type.INT, 10, Importance.LOW, "Database Record Fetch Size")
				.define(START_SCN, Type.LONG, -1L, Importance.LOW, "Start SCN")
				.define(SERVICE_TYPE, Type.STRING, "", Importance.LOW, "Db Conn Type");
	}

	public String getTopic() {
		return super.getString(TOPIC_CONFIG);
	}

	public String getDbName() {
		return super.getString(DB_NAME_CONFIG);
	}

	public String getDbIp() {
		return super.getString(DB_IP_CONFIG);
	}

	public int getDbPort() {
		return super.getInt(DB_PORT_CONFIG);
	}

	public String getDbUser() {
		return super.getString(DB_USER_CONFIG);
	}

	public String getDbPassword() {
		return super.getString(DB_PASSWORD_CONFIG);
	}

	public int getDbFetchSize() {
		return super.getInt(DB_FETCH_SIZE);
	}

	public long getStartScn() {
		return super.getLong(START_SCN);
	}

	public String getName() {
		return super.getString(NAME);
	}

	public String getServiceType(){return super.getString(SERVICE_TYPE);}

}
