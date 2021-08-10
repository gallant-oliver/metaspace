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
	private boolean closed = false;
	private List<String> logFiles;
	private int tryTimes = 3;
	private static final long MIN_POLL_PERIOD = 5000;
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
		
		config = new OracleSourceConnectorConfig(map);
		LOG.info("connector {} start", config.getName());
		this.closed = false;
		this.configMap = map;
		if (isNullDbName()) {
			LOG.warn("Connector was started, but not init connect to db cause of empity db.name");
			return;
		}
		
		try {
			
			dbConn = new OracleConnection().connect(config);
			LOG.info("connector {} get connection success", config.getName());
			logFiles = getSingleRowMuiltColumnStringResult(OracleConnectorSQL.SELECT_LOG_FILES, "MEMBER");
			if(null == logFiles || logFiles.isEmpty()){
				throw new RuntimeException(
						"connector " + config.getName() + " not find any log file by sql : " + OracleConnectorSQL.SELECT_LOG_FILES);
			} 
			LOG.info("connector {} find {} log files", config.getName(), logFiles.size());
			if (0L == streamOffsetScn) {
				streamOffsetScn = config.getStartScn() < 0L
						? getSingleRowColumnLongResult(OracleConnectorSQL.CURRENT_DB_SCN_SQL, "CURRENT_SCN")
						: config.getStartScn();
			}
		} catch (Exception e) {
			LOG.info("LogMiner Session Start error", e);
			throw new ConnectException(
					"Error at cennector task " + config.getName() + ", Please check : " + e.toString());
		}
	}

	private boolean isNullDbName() {
		String dbName = getDbName();

		if (null == dbName || "".equals(dbName)) {
			return true;
		} else {
			return false;
		}
	}

	private Long getSingleRowColumnLongResult(String sql, String columnName) throws SQLException {
		Long result = null;
		try (CallableStatement callableStatement = dbConn.prepareCall(sql);
			 ResultSet resultSet = callableStatement.executeQuery()) {
			while (resultSet.next()) {
				result = resultSet.getLong(columnName);
			}
		}

		return result;
	}
	
	private void executeSql(String sql) throws SQLException {
		try (CallableStatement callableStatement = dbConn.prepareCall(sql)){
			callableStatement.execute();
			
		}
	}
	
	private void executeSql(String sql, Long param) throws SQLException {
		try (CallableStatement callableStatement = dbConn.prepareCall(sql)){
			callableStatement.setLong(1, param);
			callableStatement.execute();
			callableStatement.close();
		}
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
		LOG.info("poll start");
		long startTime = System.currentTimeMillis();
		List<SourceRecord> records = new ArrayList<>();
		
		PreparedStatement logMinerSelect = null;
		try {
			if (closed) {
				return records;
			}
			if (isNullDbName()) {
				LOG.info("config is unavailable, db.name is empty");
				Thread.sleep(10000);
				return records;
			}
			logMinerSelect = execute(records);
		} catch (Throwable e) {
			LOG.error("during poll on connector {} : ", config.getName(), e.getMessage(), e);
			retry(records);
		}finally{
			if(null != logMinerSelect){
				try {
					logMinerSelect.close();
				} catch (Exception e) {
				}
			}
		}
		long endTime = System.currentTimeMillis();

		long sleepTime = MIN_POLL_PERIOD - endTime + startTime ;
		if(sleepTime > 100){
			Thread.sleep(sleepTime);
		}
		LOG.info("poll end");
		return records;

	}

	private List<SourceRecord> retry(List<SourceRecord> records) {
		stop();
		int times = 1;
		while(times <= tryTimes){
			LOG.info("try to execute connector task {} {} times by streamOffsetScn = {}", config.getName(), times, streamOffsetScn);
			times = times+1;
			PreparedStatement logMinerSelect = null;
			try{
				Thread.sleep(5000);
				start(configMap);
				logMinerSelect = execute(records);
				LOG.info("connector {} execute success after trying {}  times", times -1, config.getName());
			} catch (Exception ex) {
				LOG.error("{} : connector {} execute error: {}", times, config.getName(), ex);
			}finally {
				if(null != logMinerSelect){
					try {
						logMinerSelect.close();
					} catch (Exception e) {
					}
				}
			}
		}
		return records;
	}

	private PreparedStatement execute(List<SourceRecord> records) throws SQLException {
		PreparedStatement logMinerSelect;
		String newAndAddLogFilesSql = OracleConnectorSQL.NEW_DBMS_LOGMNR.replace("?", logFiles.get(0));
		for (int i = 1; i < logFiles.size(); i++) {
			newAndAddLogFilesSql = newAndAddLogFilesSql + OracleConnectorSQL.ADD_DBMS_LOGMNR.replace("?", logFiles.get(i));
		}
		executeSql(newAndAddLogFilesSql + "END;");
		LOG.info("connector {} add log files success", config.getName());
		executeSql(OracleConnectorSQL.START_LOGMINER,streamOffsetScn);
		LOG.info("connector {} logminer start success", config.getName());
		
		logMinerSelect = dbConn.prepareCall(OracleConnectorSQL.LOGMINER_SELECT_WITHSCHEMA);
		logMinerSelect.setFetchSize(config.getDbFetchSize());

		long streamEndScn = getSingleRowColumnLongResult(OracleConnectorSQL.CURRENT_DB_SCN_SQL, "CURRENT_SCN");
		logMinerSelect.setLong(1, streamOffsetScn);
		logMinerSelect.setLong(2, streamEndScn);
		
		ResultSet logMinerData = logMinerSelect.executeQuery();
		LOG.info("connector {} poll data from {} to {} success", config.getName(), streamOffsetScn,
				streamEndScn);
		explainData(records, logMinerData);
		LOG.info("connector {} explain {} item(s) from {} to {} success", config.getName(), records.size(), streamOffsetScn,
				streamEndScn);
		streamOffsetScn = streamEndScn + 1;
		return logMinerSelect;
	}

	private void explainData(List<SourceRecord> records, ResultSet logMinerData) throws SQLException {
		String sqlRedo = "";
		while (logMinerData.next()) {
			sqlRedo = logMinerData.getString(SQL_REDO_FIELD);
			sqlRedo = SQL_DOC_PATTERN.matcher(sqlRedo).replaceAll("$1");
			sqlRedo = BLACK_LINE_PATTERN.matcher(sqlRedo).replaceAll(" ").trim();
			String firstWord = sqlRedo.trim().substring(0, sqlRedo.indexOf(" ")).toLowerCase();
			if (sqlRedo.contains(TEMPORARY_TABLE) || firstWord.equals("truncate") || firstWord.equals("delete") || firstWord.equals("update")) {
				continue;
			}

			SourceRecord sourceRecord = SourceRecordUtil.getSourceRecord(logMinerData, config);
			records.add(sourceRecord);
		}
	}

	
	@Override
	public void stop() {
		LOG.info("Stop called for logminer");
		this.closed = true;

		if(null != dbConn){
			try{
				CallableStatement s = dbConn.prepareCall(OracleConnectorSQL.STOP_LOGMINER_CMD);
				s.execute();
				s.close();
			}catch (Exception e) {
				LOG.error("Stop logminer {} error {}", config.getName(), e.getMessage());
			}
			try {
				dbConn.close();
			} catch (Exception e) {
				LOG.error("Stop logminer {} error {}", config.getName(), e.getMessage());
			}
			
		}
		LOG.info("logminer stoped");
	}
	
}