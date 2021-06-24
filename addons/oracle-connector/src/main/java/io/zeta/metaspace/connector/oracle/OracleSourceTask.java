package io.zeta.metaspace.connector.oracle;

import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.COMMIT_SCN_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.SQL_REDO_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.TEMPORARY_TABLE;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author T480
 *
 */
public class OracleSourceTask extends SourceTask {
	static final Logger log = LoggerFactory.getLogger(OracleSourceTask.class);
	private Long streamOffsetScn;
	private Long streamOffsetCommitScn;
	public OracleSourceConnectorConfig config;
	private static Connection dbConn;
	List<CallableStatement> callableStatements = new ArrayList<>();
	static PreparedStatement logMinerSelect;
	ResultSet logMinerData;
	ResultSet currentScnResultSet;
	private boolean closed = false;
	static int ix = 0;
	BlockingQueue<SourceRecord> sourceRecordMq = new LinkedBlockingQueue<>();
	String utlDictionary = "";
	List<String> logFiles;
	ExecutorService executor = Executors.newFixedThreadPool(1);

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

	@Override
	public void start(Map<String, String> map) {
		config = new OracleSourceConnectorConfig(map);
		log.info("Oracle Kafka Connector is starting on {}", config.getName());
		try {

			dbConn = new OracleConnection().connect(config);
			String dictionaryLocation = getSingleRowColumnStringResult(OracleConnectorSQL.SELECT_UTL_DICTIONARY, "VALUE");
			CallableStatement dictionaryBuild = dbConn.prepareCall(OracleConnectorSQL.DICTIONARY_BUILD);
			dictionaryBuild.setString(1, OracleConnectorConstant.DICTIONARY_FILENAME);
			dictionaryBuild.setString(2, dictionaryLocation);
			dictionaryBuild.execute();
			dictionaryBuild.close();
			logFiles = getSingleRowMuiltColumnStringResult(OracleConnectorSQL.SELECT_LOG_FILES, "MEMBER");

			streamOffsetScn = config.getStartScn();
			if(0L == streamOffsetScn){
				streamOffsetScn = getSingleRowColumnLongResult(OracleConnectorSQL.CURRENT_DB_SCN_SQL, "CURRENT_SCN");
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
			logMinerStartCall.setString(2, dictionaryLocation + "\\dictionary.ora");
			logMinerStartCall.execute();
			callableStatements.add(logMinerStartCall);
			streamOffsetCommitScn = streamOffsetScn;
			logMinerSelect = dbConn.prepareCall(OracleConnectorSQL.LOGMINER_SELECT_WITHSCHEMA);
			logMinerSelect.setFetchSize(config.getDbFetchSize());
			log.info("LogMiner Session Started");
		} catch (SQLException e) {
			throw new ConnectException("Error at cennector task " + config.getName() + ", Please check : " + e.toString());
		}
	}


	private String getSingleRowColumnStringResult(String sql, String columnName) throws SQLException {
		CallableStatement prepareCall = dbConn.prepareCall(sql);
		ResultSet resultSet = prepareCall.executeQuery();
		String result = null;
		while (resultSet.next()) {
			result = resultSet.getString(columnName);
		}
		resultSet.close();
		return result;
	}

	private Long getSingleRowColumnLongResult(String sql, String columnName) throws SQLException {
		ResultSet resultSet = dbConn.prepareCall(sql).executeQuery();
		Long result = null;
		while (resultSet.next()) {
			result = resultSet.getLong(columnName);
		}
		resultSet.close();
		return result;
	}

	private List<String> getSingleRowMuiltColumnStringResult(String sql, String columnName) throws SQLException {
		ResultSet resultSet = dbConn.prepareCall(sql).executeQuery();
		List<String> results = new ArrayList<>();
		while (resultSet.next()) {
			String result = resultSet.getString(columnName);
			results.add(result);
		}
		resultSet.close();
		return results;
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		ArrayList<SourceRecord> records = new ArrayList<>();
		String sqlRedo = "";
		try {

			long streamEndScn = getSingleRowColumnLongResult(OracleConnectorSQL.CURRENT_DB_SCN_SQL, "CURRENT_SCN");
			logMinerSelect.setLong(1, streamOffsetCommitScn);
			logMinerSelect.setLong(2, streamEndScn);
			logMinerData = logMinerSelect.executeQuery();
			boolean hiveData = false;
			while (!this.closed && logMinerData.next()) {
				if (log.isDebugEnabled()) {
					logRawMinerData();
				}
				hiveData = true;
				sqlRedo = logMinerData.getString(SQL_REDO_FIELD);
				if (sqlRedo.contains(TEMPORARY_TABLE)){
					continue;
				}
				SourceRecord sourceRecord = SourceRecordUtil.getSourceRecord(logMinerData, config);
				records.add(sourceRecord);
				log.debug(sqlRedo);
			}
			if(hiveData){
				streamOffsetCommitScn = streamEndScn + 1;
			}
		} catch (SQLException e) {
			log.error("SQL error during poll", e);
		}  catch (Exception e) {
			log.error("Error during poll on cennector task  topic {} SQL :{}", config.getName(), config.getTopic(), sqlRedo, e);
		}
		return records;

	}

	@Override
	public void stop() {
		log.info("Stop called for logminer");
		this.closed = true;
		try {

			logMinerSelect.cancel();
			CallableStatement s = dbConn.prepareCall(OracleConnectorSQL.STOP_LOGMINER_CMD);
			s.execute();
			s.close();
			logMinerSelect.close();
			for (int i = callableStatements.size() - 1; i >= 0; i--) {
				callableStatements.get(i).close();
			}
			dbConn.close();

		} catch (SQLException e) {
			log.error(e.getMessage());
		}

	}

	private void logRawMinerData() throws SQLException {
		if (log.isDebugEnabled()) {
			StringBuffer b = new StringBuffer();
			for (int i = 1; i < logMinerData.getMetaData().getColumnCount(); i++) {
				String columnName = logMinerData.getMetaData().getColumnName(i);
				Object columnValue = logMinerData.getObject(i);
				b.append("[" + columnName + "=" + (columnValue == null ? "NULL" : columnValue.toString()) + "]");
			}
			log.debug(b.toString());
		}
	}
}