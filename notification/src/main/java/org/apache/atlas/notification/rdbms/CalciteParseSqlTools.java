package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.instance.debezium.RdbmsMessage;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.atlas.utils.PropertiesUtil;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.SqlCreateMaterializedView;
import org.apache.calcite.sql.ddl.SqlCreateTable;
import org.apache.calcite.sql.ddl.SqlCreateView;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.Null;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * DDL 支持的关键字 org.apache.calcite.sql.parser.ddl.SqlDdlParserImplConstants
 * DML 参考 org.apache.calcite.sql.parser.impl.SqlParserImplConstants
 *
 *  支持的sql格式解析： https://calcite.apache.org/docs/reference.html
 * @author Gridsum
 *
 */
public class CalciteParseSqlTools {
    //标记是否控制台打印日志
    private static final Boolean canPrintLog = Boolean.TRUE;
    private static void printLog(String msg) {
        if(canPrintLog) {
            System.out.println(msg);
        }
    }
    /**
     * ddl 和dml 各自对应的工厂实现解析sql为sqlnode
     * @param sql
     * @return
     */
    public static SqlNode getSqlNodeAvailable(String sql) {
        SqlParser parser = null;
        //根据sql语句判断是否属于ddl语句
        boolean isDdl = StringUtils.startsWithAny(sql.trim().replaceAll("\\s+", " ").toUpperCase(),
                new String[] { "CREATE SCHEMA", "CREATE TABLE", "CREATE VIEW", "CREATE FUNCTION","CREATE MATERIALIZED VIEW",
                        "CREATE FOREIGN SCHEMA", "DROP SCHEMA", "DROP TABLE", "DROP VIEW", "DROP FUNCTION",
                        "DROP FOREIGN SCHEMA" ,"DROP MATERIALIZED VIEW"});

        printLog("is DDL:" + isDdl);
        if (isDdl) {
            parser = SqlParser.create(sql, SqlParser.configBuilder().setParserFactory(SqlDdlParserImpl.FACTORY).build() );
        } else {
            parser = SqlParser.create(sql, SqlParser.configBuilder().setParserFactory(SqlParserImpl.FACTORY).build() );
        }
        try {
            SqlNode node = parser.parseQuery();
            return node;
        } catch (SqlParseException e) {
            printLog("parse node error:" + e);
            return null;
        }
    }

    public static RdbmsEntities getSimulationRdbmsEntities(RdbmsNotification notification, Properties connectorProperties) throws IOException {
        RdbmsMessage.Payload payload = notification.getRdbmsMessage().getPayload();
        Boolean isDdl = StringUtils.isBlank(payload.getOp());
        String sql = isDdl ?  payload.getDdl() : payload.getSource().getQuery() ;
        sql = sql.replace("`","");
        String dbname = isDdl ? payload.getDatabaseName() : payload.getSource().getDb() ;
        List<String> tableList = getBloodRelation(getSqlNodeAvailable(sql));
        String sourceTable = isDdl ? (CollectionUtils.isEmpty(tableList) ? "":tableList.get(0)) : payload.getSource().getTable() ;
        printLog("execute sql :"+sql);

        ObjectMapper mapper = new ObjectMapper();
        //1. rdbms_instance 组装
        AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
        AtlasEntity atlasEntity = new AtlasEntity();
        instanceJsonEntity.setEntity(atlasEntity);
        atlasEntity.setTypeName("rdbms_instance");
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port"));
        attributeMap.put("name",connectorProperties.getProperty("name"));
        String connectorClass = connectorProperties.getProperty("connector.class");
        String rdbmsType = connectorClass == null ? "" : connectorClass.substring(connectorClass.lastIndexOf('.')+1).replace("Connector","");
        attributeMap.put("rdbms_type",rdbmsType);
        attributeMap.put("platform","zeta");
        attributeMap.put("cloudOrOnPrem","cloud");
        attributeMap.put("hostname",connectorProperties.getProperty("database.hostname"));
        attributeMap.put("port",connectorProperties.getProperty("database.port"));
        attributeMap.put("protocol","http");
        attributeMap.put("contact_info","jdbc");
        attributeMap.put("comment","rdbms_instance API");
        attributeMap.put("description","rdbms_instance描述");
        attributeMap.put("owner","whz");
        atlasEntity.setAttributes(attributeMap);
        //2. rdbms_db
        AtlasEntity.AtlasEntityWithExtInfo dbEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
        AtlasEntity atlasDbEntity = new AtlasEntity();
        dbEntity.setEntity(atlasEntity);
        atlasDbEntity.setTypeName("rdbms_db");
        Map<String, Object> attributeDbMap = new HashMap<>();
        attributeDbMap.put("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname);
        attributeDbMap.put("name",dbname);
        attributeDbMap.put("owner","whz");
        attributeDbMap.put("description","rdbms_db data API ");
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port"));
        jsonObj.addProperty("typeName","rdbms_instance");
        attributeDbMap.put("instance",jsonObj);
        atlasDbEntity.setAttributes(attributeMap);
        //3.rdbms_table
        List<AtlasEntity.AtlasEntityWithExtInfo> tableEntityList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(tableList)){
            AtlasEntity.AtlasEntityWithExtInfo table_1_Entity = null;
            JsonArray columns = new JsonArray();
            for (String table : tableList){
                table_1_Entity = new AtlasEntity.AtlasEntityWithExtInfo();
                AtlasEntity atlasTableOneEntity = new AtlasEntity();
                table_1_Entity.setEntity(atlasTableOneEntity);
                atlasTableOneEntity.setTypeName("rdbms_table");
                Map<String, Object> attributeTableMap = new HashMap<>();
                attributeTableMap.put("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname+".public."+table);
                attributeTableMap.put("name","public."+table);
                attributeTableMap.put("comment","rdbms table API");
                attributeTableMap.put("description","rdbms_table input");
                attributeTableMap.put("owner","whz");
                attributeTableMap.put("type","table");
                attributeTableMap.put("createTime", DateUtils.currentTimestamp().toString());
                JsonObject jsondb = new JsonObject();
                jsonObj.addProperty("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname);
                jsonObj.addProperty("typeName","rdbms_db");
                attributeTableMap.put("db",jsondb);
                JsonObject jsonColumn = null;
                List<String> columnList = queryTableColDetails("test",table);
                if(!CollectionUtils.isEmpty(columnList)){
                    for(String col : columnList){
                        jsonColumn = new JsonObject();
                        jsonColumn.addProperty("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname+".public."+table+"."+col);
                        jsonColumn.addProperty("typeName","rdbms_column");
                        columns.add(jsonColumn);
                    }

                }

                atlasTableOneEntity.setAttributes(attributeTableMap);
                tableEntityList.add(table_1_Entity);
            }

        }

        AtlasEntity.AtlasEntitiesWithExtInfo atlasBloodEntities = new AtlasEntity.AtlasEntitiesWithExtInfo();
        List<AtlasEntity> entities = new ArrayList<>();
        atlasBloodEntities.setEntities(entities);
        AtlasEntity atlasBloodEntity = new AtlasEntity();
        entities.add(atlasBloodEntity);
        atlasBloodEntity.setTypeName("Process");
        Map<String, Object> attributeBloodMap = new HashMap<>();
        attributeBloodMap.put("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname+".public."+sourceTable+".Process@ms:000");
        attributeBloodMap.put("name",sql);

        JsonArray jsonInputs = new JsonArray();
        JsonArray jsonOutputs = new JsonArray();

        JsonObject input = new JsonObject();
        jsonInputs.add(input);
        input.addProperty("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname+".public."+sourceTable);
        input.addProperty("typeName","rdbms_table");
        attributeBloodMap.put("inputs",jsonInputs);
        if(!CollectionUtils.isEmpty(tableList) && tableList.size() > 1){
            JsonObject obj = null;
            for (int i = 1,len = tableList.size();i<len;i++){
                String tb = tableList.get(i);
                obj = new JsonObject();
                obj.addProperty("qualifiedName",connectorProperties.getProperty("database.hostname")+":"+connectorProperties.getProperty("database.port")+"."+dbname+".public."+tb);
                obj.addProperty("typeName","rdbms_table");
                jsonOutputs.add(obj);
            }

        }
        attributeBloodMap.put("outputs",jsonOutputs);
        attributeBloodMap.put("owner","whz");
        attributeBloodMap.put("type","table");
        atlasBloodEntity.setAttributes(attributeBloodMap);

        RdbmsEntities rdbmsEntities = new RdbmsEntities();
        SortedMap<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entityMap = rdbmsEntities.getEntityMap();
        //添加数据库实例
       // AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntityWithExtInfo = mapper.readValue(instanceJson, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_INSTANCE, Arrays.asList(instanceJsonEntity));
        //添加数据库
       // AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = mapper.readValue(dbJson, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_DB, Arrays.asList(dbEntity));

        //添加列，由于column_2_EntityWithExtInfo依赖于column_1_EntityWithExtInfo，因此，在构建list时,column_1_EntityWithExtInfo排列在column_2_EntityWithExtInfo前面
        /*AtlasEntity.AtlasEntityWithExtInfo column_1_EntityWithExtInfo = mapper.readValue(columnJson1, AtlasEntity.AtlasEntityWithExtInfo.class);
        AtlasEntity.AtlasEntityWithExtInfo column_2_EntityWithExtInfo = mapper.readValue(columnJson2, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_COLUMN, Arrays.asList(column_1_EntityWithExtInfo,column_2_EntityWithExtInfo));*/

        //添加列，由于table_2_EntityWithExtInfo依赖于table_1_EntityWithExtInfo，因此，在构建list时,table_1_EntityWithExtInfo排列在table_2_EntityWithExtInfo前面
//        AtlasEntity.AtlasEntityWithExtInfo table_1_EntityWithExtInfo = mapper.readValue(tableJson1, AtlasEntity.AtlasEntityWithExtInfo.class);
//        AtlasEntity.AtlasEntityWithExtInfo table_2_EntityWithExtInfo = mapper.readValue(tableJson2, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_TABLE, tableEntityList); //Arrays.asList(table_1_EntityWithExtInfo,table_2_EntityWithExtInfo)

       // AtlasEntity.AtlasEntitiesWithExtInfo atlasEntitiesWithExtInfo = mapper.readValue(bloodJson, AtlasEntity.AtlasEntitiesWithExtInfo.class);

        rdbmsEntities.setBloodEntities(atlasBloodEntities);
        return rdbmsEntities;
    }
    /**
     *  获取数据的血缘关系
     *  支持格式： create table|view xxx as select ...
     *          insert into tableXX select ...
     * @param sqlNode
     * @return
     */
    public static List<String> getBloodRelation(SqlNode sqlNode) {
        if(Objects.isNull(sqlNode)) {
            printLog("sql parse error.");
            return null;
        }

        List<String> bloodTableList = new ArrayList<>();
        if (sqlNode.getKind() == SqlKind.CREATE_TABLE) {
            printLog("----create_table");
            SqlCreateTable sqlCreate = (SqlCreateTable) sqlNode;
            bloodTableList.add(sqlCreate.name.toString());
            getDependencies(sqlCreate.query,bloodTableList);
        }else if(sqlNode.getKind() == SqlKind.CREATE_VIEW) {
            printLog("----create_view");
            SqlCreateView sqlCreate = (SqlCreateView) sqlNode;
            bloodTableList.add(sqlCreate.name.toString());
            getDependencies(sqlCreate.query,bloodTableList);
        }else if (sqlNode.getKind() == SqlKind.CREATE_MATERIALIZED_VIEW) {
            printLog("----create_MATERIALIZED VIEW ");
            SqlCreateMaterializedView sqlCreate = (SqlCreateMaterializedView) sqlNode;
            bloodTableList.add(sqlCreate.name.toString());
            getDependencies(sqlCreate.query,bloodTableList);
        }else if (sqlNode.getKind() == SqlKind.INSERT) {
            printLog("----insert");
            SqlInsert sqlCreate = (SqlInsert) sqlNode;
            bloodTableList.add(sqlCreate.getTargetTable().toString());
            getDependencies(sqlCreate.getSource(),bloodTableList);
        }else{
            printLog("no blood relation.");
        }
        return bloodTableList;
    }
    /*
     * 获取表的依赖
     */
    public static void getTableDependencies(SqlNode sqlNode, List<String> result) {
        List<String> dependendTable = new ArrayList<>();
        if (sqlNode.getKind() == SqlKind.SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
            printLog("-----select");
            SqlNode targetNode = sqlKind.getFrom();
            getDependencies(targetNode, dependendTable );
            //printLog(result.toString());
        }
        if (sqlNode.getKind() == SqlKind.ORDER_BY) {
            SqlOrderBy sqlKind = (SqlOrderBy) sqlNode;
            printLog("----order_by");
            getDependencies(sqlKind.getOperandList().get(0), dependendTable);
        }
        if(sqlNode instanceof SqlBasicCall) {
            printLog("----SqlBasicCall ");
            SqlBasicCall baseCallNode = (SqlBasicCall) sqlNode;
            SqlNode[] sqlNodeArray = baseCallNode.operands;
            for(SqlNode node : sqlNodeArray) {
                getTableDependencies(node, result);
            }
        }

        result.addAll(dependendTable);
    }

    public static void main(String[] args) throws IOException {
        String insertSeSql = "insert into EmployeeConfidential  SELECT EmployeeID, Employees.DepartmentID, SocialSecurityNumber, Salary, ManagerID, Departments.DepartmentName, Departments.DepartmentHeadID FROM Employees, Departments WHERE Employees.DepartmentID=Departments.DepartmentID ORDER BY Employees.DepartmentID";;
        String matiSql = "CREATE MATERIALIZED VIEW EmployeeConfidential AS SELECT EmployeeID, Employees.DepartmentID, SocialSecurityNumber, Salary, ManagerID, Departments.DepartmentName, Departments.DepartmentHeadID FROM Employees, Departments WHERE Employees.DepartmentID=Departments.DepartmentID ORDER BY Employees.DepartmentID";
        String createSubsql = "create table test.cmp as select\r\n" +
                "user_id  as uid\r\n" +
                ",user_name  as uname\r\n" +
                "from\r\n" +
                "(\r\n" +
                "    select user_id, concat(\"test\",user_name) as user_name\r\n" +
                "    from test.userss\r\n" +
                ")t";
        String createBigSql = "CREATE TABLE tmp.tmp_a_supp_achievement_an_mom_001 AS\r\n" +
                "  SELECT a1.dim_day_txdate,\r\n" +
                "         a.a_pin,\r\n" +
                "         Sum(Coalesce(b.amount, 0)) AS total_amount\r\n" +
                "         , Sum(Coalesce(c.refund_amt, 0)) AS refund_amt\r\n" +
                "         , Sum(os_prcp_amt) os_prcp_amt\r\n" +
                "  FROM (SELECT dim_day_txdate \r\n" +
                "        FROM dmv.dim_day\r\n" +
                "        WHERE dim_day_txdate>=concat(cast(Year('2018-05-15')-1 AS string),'-', substring('2018-05-15', 6, 2), '-01')\r\n" +
                "          AND dim_day_txdate<='2018-05-15' )a1\r\n" +
                "  JOIN (SELECT DISTINCT a_pin, product_type\r\n" +
                "        FROM dwd.dwd_as_qy_cust_account_s_d\r\n" +
                "        WHERE dt ='2018-05-15' AND product_type='20288' )a\r\n" +
                "  LEFT OUTER JOIN (SELECT substring(tx_time, 1, 10) AS time1, sum(order_amt) AS amount, a_pin\r\n" +
                "                    FROM dwd.dwd_actv_as_qy_iou_receipt_s_d\r\n" +
                "                    WHERE a_order_type='20096' AND a_pin NOT IN ('vep_test', 'VOPVSP测试')\r\n" +
                "                      AND dt='2018-05-15'\r\n" +
                "                    GROUP BY substring(tx_time, 1, 10), a_pin )b\r\n" +
                "              ON cast(a.a_pin AS string)=cast(b.a_pin AS string) AND a1.dim_day_txdate=b.time1\r\n" +
                "  LEFT OUTER JOIN ( SELECT substring(refund_time, 1, 10) AS refund_time, a_pin, sum(refund_amt)AS refund_amt\r\n" +
                "                    FROM dwd.dwd_as_qy_iou_refund_s_d\r\n" +
                "                    WHERE refund_status='20090' AND dt='2018-05-15' AND a_order_no <> '12467657248'\r\n" +
                "                      AND a_refund_no <> '1610230919767139947'\r\n" +
                "                    GROUP BY substring(refund_time, 1, 10), a_pin )c\r\n" +
                "              ON cast(a.a_pin AS string)=cast(c.a_pin AS string) AND a1.dim_day_txdate=c.refund_time\r\n" +
                "  LEFT OUTER JOIN (SELECT dt, a_pin, sum(os_prcp_amt) AS os_prcp_amt\r\n" +
                "                    FROM dwd.dwd_as_qy_cycle_detail_s_d\r\n" +
                "                    WHERE dt>=concat(substr('2018-05-15', 1, 7), '-01') AND dt<='2018-05-15'\r\n" +
                "                    GROUP BY dt, a_pin)e\r\n" +
                "              ON cast(a.jd_pin AS string)=cast(e.a_pin AS string) AND a1.dim_day_txdate=e.dt\r\n" +
                "  GROUP BY a1.dim_day_txdate, a.a_pin" ;

        String selectSQl = "create table aaa as select * from tablex union select * from tablebb";
        String dropSql = "drop table tablex";
        String alterOper = "ALTER TABLE `t_1` ADD INDEX `name_index` (`name`) USING BTREE COMMENT '测试索引' ";//不支持
        StopWatch sw = new StopWatch();
        sw.start();
//        SqlNode node = getSqlNodeAvailable(selectSQl);
//        getBloodRelation(node);
        RdbmsNotification notification = new RdbmsNotification();
        String payloadStr = "{\n" +
                "\t\t\"source\": {\n" +
                "\t\t\t\"version\": \"0.8.3.Final\",\n" +
                "\t\t\t\"name\": \"ATLAS_MYSQL_45000\",\n" +
                "\t\t\t\"server_id\": 1,\n" +
                "\t\t\t\"ts_sec\": 1622776067,\n" +
                "\t\t\t\"gtid\": \"4723ba50-a993-11eb-8ba2-000c29e51789:67\",\n" +
                "\t\t\t\"file\": \"mysql-bin.000018\",\n" +
                "\t\t\t\"pos\": 13544,\n" +
                "\t\t\t\"row\": 0,\n" +
                "\t\t\t\"snapshot\": false,\n" +
                "\t\t\t\"thread\": null,\n" +
                "\t\t\t\"db\": null,\n" +
                "\t\t\t\"table\": null,\n" +
                "\t\t\t\"query\": null\n" +
                "\t\t},\n" +
                "\t\t\"databaseName\": \"my_test\",\n" +
                "\t\t\"ddl\": \"CREATE TABLE `t_1` (\\r\\n`id`  int NOT NULL ,\\r\\n`name`  varchar(255) NULL ,\\r\\nPRIMARY KEY (`id`)\\r\\n)\"\n" +
                "\t}";
        String payload_insert = "{\n" +
                "\t\t\"before\": null,\n" +
                "\t\t\"after\": {\n" +
                "\t\t\t\"id\": 1,\n" +
                "\t\t\t\"name\": \"n1\"\n" +
                "\t\t},\n" +
                "\t\t\"source\": {\n" +
                "\t\t\t\"version\": \"0.8.3.Final\",\n" +
                "\t\t\t\"name\": \"ATLAS_MYSQL_45000\",\n" +
                "\t\t\t\"server_id\": 1,\n" +
                "\t\t\t\"ts_sec\": 1622801539,\n" +
                "\t\t\t\"gtid\": \"4723ba50-a993-11eb-8ba2-000c29e51789:84\",\n" +
                "\t\t\t\"file\": \"mysql-bin.000018\",\n" +
                "\t\t\t\"pos\": 17919,\n" +
                "\t\t\t\"row\": 0,\n" +
                "\t\t\t\"snapshot\": false,\n" +
                "\t\t\t\"thread\": 236,\n" +
                "\t\t\t\"db\": \"my_test\",\n" +
                "\t\t\t\"table\": \"t_3\",\n" +
                "\t\t\t\"query\": \"insert into test01 select * from one_2\"\n" +
                "\t\t},\n" +
                "\t\t\"op\": \"c\",\n" +
                "\t\t\"ts_ms\": 1622801539405\n" +
                "\t}";
        ObjectMapper mapper = new ObjectMapper();
        RdbmsMessage.Payload payload =  mapper.readValue(payload_insert,RdbmsMessage.Payload.class);
        notification.setRdbmsMessage(new RdbmsMessage());
        notification.getRdbmsMessage().setPayload(payload);
        Properties connectorProperties = new Properties();
        connectorProperties.setProperty("connector.class","io.debezium.connector.mysql.MySqlConnector");
        connectorProperties.setProperty("database.hostname","192.168.8.129");
        connectorProperties.setProperty( "database.port","3306");
        connectorProperties.setProperty( "name", "ATLAS_MYSQL_45000");
        RdbmsEntities result = getSimulationRdbmsEntities(notification,connectorProperties);
        sw.stop();
        printLog("total cost time :"+sw.getTotalTimeMillis()+" ms");
        System.out.println(result);
    }
    /**
     *  找出表的依赖关系
     * @param sqlNode
     * @param result
     * @return
     */
    public static List<String> getDependencies(SqlNode sqlNode, List<String> result) {
        if(sqlNode == null){
            return null;
        }
        if (sqlNode.getKind() == SqlKind.JOIN) {
            SqlJoin sqlKind = (SqlJoin) sqlNode;
            printLog("-----join");

            getDependencies(sqlKind.getLeft(), result);
            getDependencies(sqlKind.getRight(), result);
        }

        if (sqlNode.getKind() == SqlKind.UNION) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            printLog("----union");

            getDependencies(sqlKind.getOperandList().get(0), result);
            getDependencies(sqlKind.getOperandList().get(1), result);

        }

        if (sqlNode.getKind() == SqlKind.IDENTIFIER) {
            printLog("-----identifier");
            result.add(sqlNode.toString());
        }

        if (sqlNode.getKind() == SqlKind.SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
            printLog("-----select");
            getDependencies(sqlKind.getFrom(), result);
        }

        if (sqlNode.getKind() == SqlKind.AS) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            printLog("----as");
            getDependencies(sqlKind.getOperandList().get(0), result);
        }

        if (sqlNode.getKind() == SqlKind.ORDER_BY) {
            SqlOrderBy sqlKind = (SqlOrderBy) sqlNode;
            printLog("----order_by");
            getDependencies(sqlKind.getOperandList().get(0), result);
        }
        return result;
    }

    /**
     * 根据连接信息获取table包含的字段 (主要使用DatabaseMetaData来解决)
     * @param schemaPattern
     * @param tableNamePattern 注意:和数据库的表一致，区分大小写(calcite parse sql会转换为大写)
     * @return
     */
    public static List<String> queryTableColDetails(String schemaPattern,String tableNamePattern) {
        try(Connection connection = getConnecion("mysql");
            ResultSet rs = connection.getMetaData().getColumns(null, schemaPattern, tableNamePattern.toLowerCase(), null);) {
            List<String> colTempList = new ArrayList<>();
            while(rs.next()) {
                colTempList.add(rs.getString("COLUMN_NAME"));
            }

            printLog("table "+tableNamePattern+" column: "+Joiner.on(',').join(colTempList));
            return colTempList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 根据数据源获取对应的连接  可以考虑连接池
     * @param dbType
     * @return
     */
    private static Connection getConnecion(String dbType) {
        return mySqlConnection("jdbc:mysql://localhost:3306/test","root","root");
    }

    private static Connection mySqlConnection(String jdbcUrl,String jdbcUser,String jdbcPassword) {
        Properties info = new Properties();
        info.put("model",
                "inline:"
                        + "{\n" + "  version: '1.0',\n"
                        + "  defaultSchema: 'test',\n"
                        + "  schemas: [\n"
                        + "    {\n"
                        + "      name: 'test',\n"
                        + "      type: 'custom',\n"
                        + "      factory: 'org.apache.calcite.adapter.jdbc.JdbcSchema$Factory',\n"
                        + "      operand: {\n"
                        + "        jdbcDriver: 'com.mysql.jdbc.Driver',\n"
                        + "        jdbcUrl:'"+jdbcUrl+"',\n"   //'jdbc:mysql://localhost:3306/test'
                        + "        jdbcUser: '"+jdbcUser+"',\n"
                        + "        jdbcPassword: '"+jdbcPassword+"'\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:calcite:", info);
        } catch (ClassNotFoundException e) {
            printLog("没有找到对应的数据库驱动信息.");
        } catch (SQLException e) {
            printLog("获取数据库连接出错." + e);
        }
        return null;
    }

}
