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
	public static final String DB_HOST_NAME_CONFIG = "db.hostname";
	public static final String DB_PORT_CONFIG = "db.port";
	public static final String DB_USER_CONFIG = "db.user";
	public static final String DB_USER_PASSWORD_CONFIG = "db.user.password";
	public static final String TABLE_WHITELIST = "table.whitelist";
	public static final String TABLE_BLACKLIST = "table.blacklist";
	public static final String DB_FETCH_SIZE = "db.fetch.size";
	public static final String START_SCN = "start.scn";
	
	public OracleSourceConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
		super(config, parsedConfig);
	}

	public OracleSourceConnectorConfig(Map<String, String> parsedConfig) {
		this(conf(), parsedConfig);
	}

	public static ConfigDef conf() {
		return new ConfigDef()
				.define(NAME, Type.STRING, Importance.HIGH, "Connector name")
				.define(TOPIC_CONFIG, Type.STRING, Importance.HIGH, "Topic")
				.define(DB_NAME_CONFIG, Type.STRING, Importance.HIGH, "Db Name")
				.define(DB_HOST_NAME_CONFIG, Type.STRING, Importance.HIGH, "Db HostName")
				.define(DB_PORT_CONFIG, Type.INT, Importance.HIGH, "Db Port")
				.define(DB_USER_CONFIG, Type.STRING, Importance.HIGH, "Db User")
				.define(DB_USER_PASSWORD_CONFIG, Type.STRING, Importance.HIGH, "Db User Password")
				.define(TABLE_WHITELIST, Type.STRING, "*", Importance.LOW, "TAbles will be mined")
				.define(DB_FETCH_SIZE, Type.INT, 10, Importance.LOW, "Database Record Fetch Size")
				.define(START_SCN, Type.LONG, 0, Importance.LOW, "Start SCN")
				.define(TABLE_BLACKLIST, Type.STRING, "", Importance.LOW, "Table will not be mined");
	}
	
	public String getTopic() {
		return super.getString(TOPIC_CONFIG);
	}

	public String getDbName() {
		return super.getString(DB_NAME_CONFIG);
	}

	public String getDbHostName() {
		return super.getString(DB_HOST_NAME_CONFIG);
	}

	public Integer getDbPort() {
		return super.getInt(DB_PORT_CONFIG);
	}

	public String getDbUser() {
		return super.getString(DB_USER_CONFIG);
	}

	public String getDbUserPassword() {
		return super.getString(DB_USER_PASSWORD_CONFIG);
	}

	public String getTableWhiteList() {
		return super.getString(TABLE_WHITELIST);
	}

	public Integer getDbFetchSize() {
		return super.getInt(DB_FETCH_SIZE);
	}

	public Long getStartScn() {
		return super.getLong(START_SCN);
	}
	public String getTableBlackList() {
		return super.getString(TABLE_BLACKLIST);
	}
	public String getName() {
		return super.getString(NAME);
	}
}
