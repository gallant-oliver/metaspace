package io.zeta.metaspace.connector.oracle;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.zeta.metaspace.connector.oracle.OracleConnectorConstant.*;

public class SourceRecordUtil {

	public static final String LOG_MINER_OFFSET_FIELD="logminer";
	private static final Logger LOG = LoggerFactory.getLogger(OracleSourceTask.class);
	public static SourceRecord getSourceRecord(ResultSet logMinerData,String sqlRedo, OracleSourceConnectorConfig config) throws SQLException{
		String dbName = config.getDbName();
		if(sqlRedo == null || sqlRedo.trim().length() == 0){
			sqlRedo = logMinerData.getString(SQL_REDO_FIELD);
		}

		int operationCode = logMinerData.getInt(OracleConnectorConstant.OPERATION_CODE_FIELD);

		String op = "";
		switch (operationCode) {
			case 1://INSERT
				op = "c";
				break;
			case 2: //DELETE
				op = "d";
				break;
			case 3: //UPDATE
				op = "u";
				break;
			default:
				op = "ddl";
				break;
		}
		Schema sourceSchema = SchemaBuilder.struct().optional().name(config.getName())
				.field("version",SchemaBuilder.string().optional().build())
				.field("name",SchemaBuilder.string().optional().build())
				.field("server_id",SchemaBuilder.int64().optional().build())
				.field("ts_sec",SchemaBuilder.int64().optional().build())
				.field("gtid",SchemaBuilder.string().optional().build())
				.field("file",SchemaBuilder.string().optional().build())
				.field("pos",SchemaBuilder.int64().optional().build())
				.field("row",SchemaBuilder.int32().optional().build())
				.field("snapshot",SchemaBuilder.bool().optional().defaultValue(Boolean.FALSE).build())
				.field("thread",SchemaBuilder.int64().optional().build())
				.field("db",SchemaBuilder.string().optional().build())
				.field("table",SchemaBuilder.string().optional().build())
				.field("query",SchemaBuilder.string().optional().build())
				.build();
		SchemaBuilder payLoadSchemaBuilder = SchemaBuilder.struct().required().name(config.getName())
				.field("before", SchemaBuilder.struct().optional().build())
				.field("after", SchemaBuilder.struct().optional().build())
				.field("source",sourceSchema)
				.field("owner",SchemaBuilder.string().optional().build());

		if("ddl".equals(op)) {
			payLoadSchemaBuilder.field("databaseName",SchemaBuilder.string().optional().build())
					.field("ddl",SchemaBuilder.string().optional().build());
		}else {
			payLoadSchemaBuilder.field("op", SchemaBuilder.string().optional().build())
					.field("ts_ms", SchemaBuilder.int64().optional().build());
		}

		Struct sourceStruct = new Struct(sourceSchema);
		sourceStruct.put("name", config.getName())//.put("server_id", "")
				.put("ts_sec", logMinerData.getDate(OracleConnectorConstant.TIMESTAMP_FIELD).getTime())
				.put("snapshot", Boolean.FALSE)
				.put("thread", logMinerData.getLong(OracleConnectorConstant.THREAD_FIELD));

		Struct payloadStruct = new Struct(payLoadSchemaBuilder.build());
		payloadStruct.put("source", sourceStruct);
		payloadStruct.put("owner", logMinerData.getString(OracleConnectorConstant.SEG_OWNER_FIELD));
		if("ddl".equals(op)) {
			payloadStruct.put("databaseName", dbName).put("ddl", sqlRedo);
		}else {
			sourceStruct.put("db", dbName).put("query",sqlRedo)
					.put("table", logMinerData.getString(OracleConnectorConstant.TABLE_NAME_FIELD));
			payloadStruct.put("op", op).put("ts_ms", logMinerData.getDate(OracleConnectorConstant.TIMESTAMP_FIELD).getTime());
		}

		Long scn = logMinerData.getLong(SCN_FIELD);
		Long commitScn = logMinerData.getLong(COMMIT_SCN_FIELD);
		String rowId = logMinerData.getString(ROW_ID_FIELD);
		Map<String, String> sourceOffset = sourceOffset(scn, commitScn, rowId);
		Map<String, String> sourcePartition = Collections.singletonMap(LOG_MINER_OFFSET_FIELD, dbName);
		LOG.debug("topic is " + config.getTopic());
		return new SourceRecord(sourcePartition, sourceOffset, config.getTopic(), payloadStruct.schema(), payloadStruct);

	}

	private static Map<String, String> sourceOffset(Long scnPosition, Long commitScnPosition, String rowId) {
		Map<String, String> offSet = new HashMap<String, String>();
		offSet.put(POSITION_FIELD, scnPosition.toString());
		offSet.put(COMMITSCN_POSITION_FIELD, commitScnPosition.toString());
		offSet.put(ROWID_POSITION_FIELD, rowId);
		return offSet;
	}

}