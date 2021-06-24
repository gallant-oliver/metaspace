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

import org.apache.commons.lang.time.DateUtils;
import org.apache.kafka.connect.data.ConnectSchema;
import org.apache.kafka.connect.data.Schema;
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
		source.setTable(logMinerData.getString(OracleConnectorConstant.TABLE_NAME_FIELD));
		source.setThread(logMinerData.getLong(OracleConnectorConstant.THREAD_FIELD));
		source.setSnapshot(Boolean.FALSE);
		source.setServerId(1);
		source.setTsSec(logMinerData.getDate(OracleConnectorConstant.TIMESTAMP_FIELD).getTime());
		RdbmsMessage.Payload payload = new RdbmsMessage.Payload();
		payload.setSource(source);
		payload.setDatabaseName(dbName);
		payload.setTsMs(System.currentTimeMillis());
		
		
		String operation = logMinerData.getString(OPERATION_FIELD);
		
		switch (operation) {
		case "INSERT":
			payload.setOp("c");
			source.setQuery(sqlRedo);
			break;
		case "DELETE":
			payload.setOp("d");
			source.setQuery(sqlRedo);
			break;
		case "UPDATE":
			payload.setOp("u");
			source.setQuery(sqlRedo);
			break;
		default:
			payload.setDdl(sqlRedo);
			break;
		}
		//Schema 组装
		Schema schema = SchemaBuilder.struct().required().name(config.getName())
				.field("databaseName",SchemaBuilder.string().required().build())
				.field("ddl",SchemaBuilder.string().required().build())
				.field("source",SchemaBuilder.struct().required().name(config.getName())
						.field("version",SchemaBuilder.string().optional().build())
						.field("name",SchemaBuilder.string().required().build())
						.field("server_id",SchemaBuilder.int64().required().build())
						.field("ts_sec",SchemaBuilder.int64().required().build())
						.field("gtid",SchemaBuilder.string().optional().build())
						.field("file",SchemaBuilder.string().required().build())
						.field("pos",SchemaBuilder.int64().required().build())
						.field("row",SchemaBuilder.int32().required().build())
						.field("snapshot",SchemaBuilder.bool().optional().defaultValue(Boolean.FALSE).build())
						.field("thread",SchemaBuilder.int64().optional().build())
						.field("db",SchemaBuilder.string().optional().build())
						.field("table",SchemaBuilder.string().optional().build())
						.field("query",SchemaBuilder.string().optional().build())
						.build())
				.build();

		RdbmsMessage rdbmsMessage = new RdbmsMessage();
		rdbmsMessage.setPayload(payload);
		rdbmsMessage.setSchema(schema);
		Long scn = logMinerData.getLong(SCN_FIELD);
		Long commitScn = logMinerData.getLong(COMMIT_SCN_FIELD);
		String rowId = logMinerData.getString(ROW_ID_FIELD);
		Map<String, String> sourceOffset = sourceOffset(scn, commitScn, rowId);
		Map<String, String> sourcePartition = Collections.singletonMap(LOG_MINER_OFFSET_FIELD, dbName);
		return new SourceRecord(sourcePartition, sourceOffset, config.getTopic(), schema, rdbmsMessage);
		
	}
	
	private static Map<String, String> sourceOffset(Long scnPosition, Long commitScnPosition, String rowId) {
		Map<String, String> offSet = new HashMap<String, String>();
		offSet.put(POSITION_FIELD, scnPosition.toString());
		offSet.put(COMMITSCN_POSITION_FIELD, commitScnPosition.toString());
		offSet.put(ROWID_POSITION_FIELD, rowId);
		return offSet;
	}
}
