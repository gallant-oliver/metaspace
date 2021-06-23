package io.zeta.metaspace.connector.oracle;

import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.COMMITSCN_POSITION_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.COMMIT_SCN_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.OPERATION_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.POSITION_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.ROWID_POSITION_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.ROW_ID_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.SCN_FIELD;
import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.SQL_REDO_FIELD;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.source.SourceRecord;

import io.zeta.metaspace.connector.oracle.models.RdbmsMessage;
import io.zeta.metaspace.connector.oracle.models.Source;

public class SourceRecordUtil {
	
	public static final String LOG_MINER_OFFSET_FIELD="logminer"; 
	
	public static SourceRecord getSourceRecord(ResultSet logMinerData, OracleSourceConnectorConfig config) throws SQLException{
		
		String dbName = config.getDbName();
		String sqlRedo = logMinerData.getString(SQL_REDO_FIELD);
		
		Source source =new Source();
		
		source.setName(config.getName());
		source.setDb(dbName);
		RdbmsMessage.Payload payload = new RdbmsMessage.Payload();
		payload.setSource(source);
		payload.setDatabaseName(dbName);
		payload.setTsMs(System.currentTimeMillis());
		
		
		String operation = logMinerData.getString(OPERATION_FIELD);
		
		switch (operation) {
		case "INSERT":
			payload.setOp("c");					
			break;
		case "DELETE":
			payload.setOp("d");
			break;
		case "UPDATE":
			payload.setOp("u");
			break;
		default:
			payload.setDdl(sqlRedo);
			break;
		}
		RdbmsMessage rdbmsMessage = new RdbmsMessage();
		rdbmsMessage.setPayload(payload);
		Long scn = logMinerData.getLong(SCN_FIELD);
		Long commitScn = logMinerData.getLong(COMMIT_SCN_FIELD);
		String rowId = logMinerData.getString(ROW_ID_FIELD);
		Map<String, String> sourceOffset = sourceOffset(scn, commitScn, rowId);
		Map<String, String> sourcePartition = Collections.singletonMap(LOG_MINER_OFFSET_FIELD, dbName);
		return new SourceRecord(sourcePartition, sourceOffset, config.getTopic(), SchemaBuilder.struct().optional().build(), rdbmsMessage);
		
	}
	
	private static Map<String, String> sourceOffset(Long scnPosition, Long commitScnPosition, String rowId) {
		Map<String, String> offSet = new HashMap<String, String>();
		offSet.put(POSITION_FIELD, scnPosition.toString());
		offSet.put(COMMITSCN_POSITION_FIELD, commitScnPosition.toString());
		offSet.put(ROWID_POSITION_FIELD, rowId);
		return offSet;
	}
}
