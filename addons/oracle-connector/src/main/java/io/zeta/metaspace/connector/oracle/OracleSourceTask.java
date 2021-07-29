package io.zeta.metaspace.connector.oracle;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.SQL_REDO_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.TEMPORARY_TABLE;

/**
 *
 * @author T480
 *
 */
public class OracleSourceTask extends SourceTask {
	static final Logger LOG = LoggerFactory.getLogger(OracleSourceTask.class);
	private Long streamOffsetScn = 0L;
	private OracleSourceConnectorConfig config;
	Map<String, String> configMap;
	private static Connection dbConn;
	private List<CallableStatement> callableStatements = new ArrayList<>();
	private static PreparedStatement logMinerSelect;
	private ResultSet logMinerData;
	private boolean closed = false;
	private List<String> logFiles;
	private boolean isInit;
	private int tryTimes = 3;
	private static final Pattern SQL_DOC_PATTERN = Pattern.compile("(?ms)('(?:''|[^'])*')|--(?!(\\s*\\++\\s*\\S+))\\s.*?$|((/\\*)(?!(\\s*\\++\\s*\\S+)).*?(\\*/))");
	private static final Pattern BLACK_LINE_PATTERN = Pattern.compile("(\n|↵)(?=([^\"]*\"[^\"]*\")*[^\"]*$)(?=([^']*'[^']*')*[^']*$)");
	@Override
	public String version() {
		return VersionUtil.getVersion();
	}

	public static Connection getThreadConnection() {
		return dbConn;
	}

	public static void closeDbConn() throws SQLException {
		logMinerSelect.cancel();
		dbConn.close();
	}

	private String getDbName() {
		String dbName = null;
		try {
			dbName = config.getDbName();
		} catch (RuntimeException e) {
			LOG.warn("------------" + config.getName() + " dbName is  null ------------");
		}
		return dbName;
	}

	@Override
	public void start(Map<String, String> map) {
		this.configMap = map;
		config = new OracleSourceConnectorConfig(map);
		if (isInitor()) {
			return;
		}
		try {
			LOG.info("Oracle Kafka Connector {} is starting", config.getName());
			dbConn = new OracleConnection().connect(config);
			logFiles = getSingleRowMuiltColumnStringResult(OracleConnectorSQL.SELECT_LOG_FILES, "MEMBER");
			if (0L == streamOffsetScn) {
				streamOffsetScn = config.getStartScn() < 0L
						? getSingleRowColumnLongResult(OracleConnectorSQL.CURRENT_DB_SCN_SQL, "CURRENT_SCN")
						: config.getStartScn();
			}

			String startLogminerSql = OracleConnectorSQL.NEW_DBMS_LOGMNR.replace("?", logFiles.get(0));
			for (int i = 1; i < logFiles.size(); i++) {
				startLogminerSql = startLogminerSql + OracleConnectorSQL.ADD_DBMS_LOGMNR.replace("?", logFiles.get(i));
			}
			CallableStatement startLogminer = dbConn.prepareCall(startLogminerSql + "END;");
			startLogminer.execute();
			callableStatements.add(startLogminer);
			CallableStatement logMinerStartCall = dbConn.prepareCall(OracleConnectorSQL.START_LOGMINER);
			logMinerStartCall.setLong(1, streamOffsetScn);
			logMinerStartCall.execute();
			callableStatements.add(logMinerStartCall);
			logMinerSelect = dbConn.prepareCall(OracleConnectorSQL.LOGMINER_SELECT_WITHSCHEMA);
			logMinerSelect.setFetchSize(config.getDbFetchSize());
			LOG.info("Oracle Kafka Connector {} is started by streamOffsetScn = {}", config.getName(), streamOffsetScn);
		} catch (Exception e) {
			LOG.info("LogMiner Session Start error", e);
			throw new ConnectException(
					"Error at cennector task " + config.getName() + ", Please check : " + e.toString());
		}
	}

	private boolean isInitor() {
		String dbName = getDbName();

		if (null == dbName || "".equals(dbName)) {
			this.isInit = true;
		} else {
			this.isInit = false;
		}
		return this.isInit;
	}

	private Long getSingleRowColumnLongResult(String sql, String columnName) throws SQLException {
		Long result = null;
		try (CallableStatement callableStatement = dbConn.prepareCall(sql);
			 ResultSet resultSet = callableStatement.executeQuery()) {
			while (resultSet.next()) {
				result = resultSet.getLong(columnName);
			}
			resultSet.close();
		}

		return result;
	}

	private List<String> getSingleRowMuiltColumnStringResult(String sql, String columnName) throws SQLException {
		List<String> results = new ArrayList<>();
		try (CallableStatement callableStatement = dbConn.prepareCall(sql);
			 ResultSet resultSet = callableStatement.executeQuery()) {
			while (resultSet.next()) {
				String result = resultSet.getString(columnName);
				results.add(result);
			}
			resultSet.close();
		}
		return results;
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {

		ArrayList<SourceRecord> records = new ArrayList<>();
		if (closed) {
			return records;
		}
		if (this.isInit) {
			Thread.sleep(3600000);
			return records;
		}
		String sqlRedo = "";
		try {

			LOG.debug("poll start");
			long streamEndScn = getSingleRowColumnLongResult(OracleConnectorSQL.CURRENT_DB_SCN_SQL, "CURRENT_SCN");
			logMinerSelect.setLong(1, streamOffsetScn);
			logMinerSelect.setLong(2, streamEndScn);
			LOG.debug("Oracle Kafka Connector {} is polling data from {} to {}", config.getName(), streamOffsetScn,
					streamEndScn);
			logMinerData = logMinerSelect.executeQuery();

			while (logMinerData.next()) {
				if (LOG.isDebugEnabled()) {
					logRawMinerData();
				}
				sqlRedo = logMinerData.getString(SQL_REDO_FIELD);
				sqlRedo = SQL_DOC_PATTERN.matcher(sqlRedo).replaceAll("$1");
				sqlRedo = BLACK_LINE_PATTERN.matcher(sqlRedo).replaceAll(" ").trim();
				String firstWord = sqlRedo.trim().substring(0, sqlRedo.indexOf(" ")).toLowerCase();
				// info 用来筛选掉数据库内部生成的redo sql || userExecuteFlag :用户操作标记
				if (sqlRedo.contains(TEMPORARY_TABLE) || firstWord.equals("truncate") || firstWord.equals("delete") || firstWord.equals("update")) {
					continue;
				}

				SourceRecord sourceRecord = SourceRecordUtil.getSourceRecord(logMinerData, config);
				records.add(sourceRecord);
			}
			LOG.debug("Oracle Kafka Connector {} polled {} data from {} to {}", config.getName(), records.size(),
					streamOffsetScn, streamEndScn);
			streamOffsetScn = streamEndScn + 1;
		} catch (Exception e) {
			LOG.warn("warn during poll on cennector {} : ", config.getName(), e.getMessage());
			stop();
			LOG.info("try to restart cennector {} by streamOffsetScn = {}", config.getName(), streamOffsetScn);
			try {
				Thread.sleep(3000);
				start(configMap);
				this.closed = false;
				tryTimes = 3;
			} catch (Exception ex) {
				if (tryTimes > 0) {
					LOG.warn("cennector {} restarted error: {}", config.getName(), ex.getMessage());
					tryTimes = tryTimes - 1;
				} else {
					LOG.error("cennector {} restarted error", config.getName(), ex);
					throw ex;
				}
			}
			LOG.info("cennector {} restarted", config.getName());
		}
		LOG.debug("poll end");
		return records;

	}

	@Override
	public void stop() {
		LOG.info("Stop called for logminer");
		this.closed = true;

		if (this.isInit) {
			LOG.info("init logminer stoped");
			return;
		}

		try {
			CallableStatement s = dbConn.prepareCall(OracleConnectorSQL.STOP_LOGMINER_CMD);
			s.execute();
			s.close();
		} catch (Exception e) {
			LOG.error("Stop logminer {} error {}", config.getName(), e.getMessage());
		}
		try {
			logMinerSelect.cancel();
			logMinerSelect.close();
		} catch (Exception e) {
			LOG.error("Stop logminer {} error {}", config.getName(), e.getMessage());
		}
		try {
			for (int i = callableStatements.size() - 1; i >= 0; i--) {
				callableStatements.get(i).close();
			}
		} catch (Exception e) {
			LOG.error("Stop logminer {} error {}", config.getName(), e.getMessage());
		}
		try {
			dbConn.close();
		} catch (Exception e) {
			LOG.error("Stop logminer {} error {}", config.getName(), e.getMessage());
		}
		LOG.info("logminer stoped");

	}

	private void logRawMinerData() throws SQLException {
		if (LOG.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (int i = 1; i < logMinerData.getMetaData().getColumnCount(); i++) {
				String columnName = logMinerData.getMetaData().getColumnName(i);
				Object columnValue = logMinerData.getObject(i);
				b.append("[" + columnName + "=" + (columnValue == null ? "NULL" : columnValue.toString()) + "]");
			}
			LOG.debug(b.toString());
		}
	}
}