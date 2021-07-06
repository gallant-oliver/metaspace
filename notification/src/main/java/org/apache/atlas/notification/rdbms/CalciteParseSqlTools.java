package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.zeta.metaspace.utils.DateUtils;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.instance.debezium.RdbmsMessage;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.ddl.*;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.ddl.SqlDdlParserImpl;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DDL 支持的关键字 org.apache.calcite.sql.parser.ddl.SqlDdlParserImplConstants
 * DML 参考 org.apache.calcite.sql.parser.impl.SqlParserImplConstants
 *
 *  支持的sql格式解析： https://calcite.apache.org/docs/reference.html
 * @author Gridsum
 *
 */
public class CalciteParseSqlTools {
    private static final Logger log = LoggerFactory.getLogger(CalciteParseSqlTools.class);
    /**
     * ddl 和dml 各自对应的工厂实现解析sql为sqlnode
     * @param sql
     * @return
     */
    @Deprecated
    private static SqlNode getSqlNodeAvailable(String sql) {
        SqlParser parser = null;
        //根据sql语句判断是否属于ddl语句
        boolean isDdl = StringUtils.startsWithAny(sql.trim().replaceAll("\\s+", " ").toUpperCase(),
                new String[] { "CREATE SCHEMA", "CREATE TABLE", "CREATE VIEW", "CREATE FUNCTION","CREATE MATERIALIZED VIEW",
                        "CREATE FOREIGN SCHEMA", "DROP SCHEMA", "DROP TABLE", "DROP VIEW", "DROP FUNCTION",
                        "DROP FOREIGN SCHEMA" ,"DROP MATERIALIZED VIEW"});

        log.info("is DDL: {}" , isDdl);
        if (isDdl) {
            parser = SqlParser.create(sql, SqlParser.configBuilder().setParserFactory(SqlDdlParserImpl.FACTORY).build() );
        } else {
            parser = SqlParser.create(sql, SqlParser.configBuilder().setParserFactory(SqlParserImpl.FACTORY).build() );
        }
        try {
            SqlNode node = parser.parseQuery();
            return node;
        } catch (SqlParseException e) {
            log.error("parse node error: {}" , e);
            return null;
        }
    }

    public static RdbmsEntities getSimulationRdbmsEntities(RdbmsNotification notification, Properties connectorProperties) throws IOException {
        RdbmsMessage.Payload payload = notification.getRdbmsMessage().getPayload();
        Boolean isDdl = StringUtils.isBlank(payload.getOp());
        String sql = isDdl ?  payload.getDdl() : payload.getSource().getQuery() ;
        sql = sql.replace("`","").replace(";","");
        //String dbname = isDdl ? payload.getDatabaseName() : payload.getSource().getDb() ;
        /*SqlNode sqlParseNode = getSqlNodeAvailable(sql);
        if(sqlParseNode == null){
            throw new RuntimeException("sql 语法解析处理错误.");
        }
        Map<String,Object>  bloodTableMap= getBloodRelation(sqlParseNode);
        List<String> tableList = (List<String>) bloodTableMap.get("tableBlood");*/

        String rdbmsType = getRdbmsType(connectorProperties);
        Map<String, TreeSet<String>> resultTableMap = DruidAnalyzerUtil.getFromTo(sql,rdbmsType);
        TreeSet<String> fromSet = resultTableMap.get("from");
        TreeSet<String> toSet = resultTableMap.get("to");
        if(resultTableMap == null
                || (fromSet.isEmpty() && toSet.isEmpty()) ){
            log.info("sql解析没有表对象，不需要处理..");
            return new RdbmsEntities();
        }

        RdbmsEntities.OperateType operateType =
                StringUtils.startsWithAny(sql.trim().toUpperCase(), new String[] { "CREATE", "INSERT"}) ? RdbmsEntities.OperateType.ADD :
                        (StringUtils.startsWithAny(sql.trim().toUpperCase(),new String[] { "DROP", "DELETE"}) ? RdbmsEntities.OperateType.DROP :
                        RdbmsEntities.OperateType.MODIFY);

        log.info("execute sql :{},\n operateType:{}",sql,operateType);

        //1. rdbms_instance 组装
        List<AtlasEntity.AtlasEntityWithExtInfo> instanceEntityList
                = makeAtlasEntity(RdbmsEntities.EntityType.RDBMS_INSTANCE,connectorProperties,null,null);

        //2. rdbms_db
        List<AtlasEntity.AtlasEntityWithExtInfo> dbEntityList
                = makeAtlasEntity(RdbmsEntities.EntityType.RDBMS_DB,connectorProperties,null,null);

        //3.rdbms_table 操作表的对象(表名处理为 user.table 格式)
        List<String> fromTableList = new ArrayList<>(fromSet);
        List<String> toTableList = new ArrayList<>(toSet);
        String owner = payload.getOwner();
        addOwnerPrefixToTable(owner,fromTableList,toTableList);

        List<AtlasEntity.AtlasEntityWithExtInfo> fromTableEntityList = new ArrayList<>();
        List<AtlasEntity.AtlasEntityWithExtInfo> fromColumnEntityList = new ArrayList<>();
        List<AtlasEntity.AtlasEntityWithExtInfo> toTableEntityList = new ArrayList<>();
        List<AtlasEntity.AtlasEntityWithExtInfo> toColumnEntityList = new ArrayList<>();
        //记录表.列格式的list
        List<String> allColumnInfo = new ArrayList<>();
        dealTableColumnEntity(fromTableList, connectorProperties, fromTableEntityList, fromColumnEntityList,allColumnInfo);
        dealTableColumnEntity(toTableList, connectorProperties, toTableEntityList, toColumnEntityList,allColumnInfo);

        //blood relation
        AtlasEntity.AtlasEntitiesWithExtInfo atlasBloodEntities = new AtlasEntity.AtlasEntitiesWithExtInfo();
        if(CollectionUtils.isEmpty(fromSet) || CollectionUtils.isEmpty(toSet)){
            log.info("该 sql {} ...不存在血缘关系。",sql.substring(0,30));
        }else{
            atlasBloodEntities = makeAtlasBloodRelation(resultTableMap, sql, connectorProperties,allColumnInfo,owner);
        }


        RdbmsEntities rdbmsEntities = new RdbmsEntities();
        Map<RdbmsEntities.OperateType, Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> operateEntityMap = rdbmsEntities.getEntityMap();
        Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> modifyMap = operateEntityMap.get(RdbmsEntities.OperateType.MODIFY);
        Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> operateMap = operateEntityMap.get(operateType);
        //添加数据库实例
        modifyMap.put(RdbmsEntities.EntityType.RDBMS_INSTANCE, instanceEntityList);
        //添加数据库
        modifyMap.put(RdbmsEntities.EntityType.RDBMS_DB, dbEntityList);
        //添加来源的 表 列 column table
        modifyMap.put(RdbmsEntities.EntityType.RDBMS_TABLE, fromTableEntityList);
        modifyMap.put(RdbmsEntities.EntityType.RDBMS_COLUMN, fromColumnEntityList);

        //添加输出（最终）表 列 column table
        if(operateType == RdbmsEntities.OperateType.MODIFY){
            modifyMap.put(RdbmsEntities.EntityType.RDBMS_TABLE, toTableEntityList);
            modifyMap.put(RdbmsEntities.EntityType.RDBMS_COLUMN, toColumnEntityList);
        }else{
            operateMap.put(RdbmsEntities.EntityType.RDBMS_TABLE, toTableEntityList);
            operateMap.put(RdbmsEntities.EntityType.RDBMS_COLUMN, toColumnEntityList);
        }
        //血缘关系
        Map<RdbmsEntities.OperateType, AtlasEntity.AtlasEntitiesWithExtInfo> bloodMap = rdbmsEntities.getBloodEntities();
        if(!CollectionUtils.isEmpty(atlasBloodEntities.getEntities())){
            bloodMap.put(operateType,atlasBloodEntities);
        }

        return rdbmsEntities;
    }

    /**
     * 处理表和列的entity 数据
     * @param tableList
     * @param connectorProperties
     * @param tableEntityList 返回的table对象
     * @param columnEntityList 返回的column对象
     */
    private static void dealTableColumnEntity(List<String> tableList,Properties connectorProperties,
                                              List<AtlasEntity.AtlasEntityWithExtInfo> tableEntityList,
                                              List<AtlasEntity.AtlasEntityWithExtInfo> columnEntityList,List<String> allColumnInfo){
        if(!CollectionUtils.isEmpty(tableList)){
            for (String table : tableList){
                //table oracle 需要增加用户
                tableEntityList.addAll(makeAtlasEntity(RdbmsEntities.EntityType.RDBMS_TABLE,connectorProperties,table,null));
                //增加该表的列 entity
                columnEntityList.addAll(makeAtlasEntity(RdbmsEntities.EntityType.RDBMS_COLUMN,connectorProperties,table,allColumnInfo));
            }
        }
    }

    /**
     * 处理源字段以及目标字段的映射关系  （建立列级别血缘关系）
     * @param resultTableMap
     * @param allColumnInfo
     * @return
     */
    private static Map<String,List<String>> makeAtlasColumnMap(Map<String, TreeSet<String>> resultTableMap,List<String> allColumnInfo,String owner){
        Map<String,List<String>> resultMap = new HashMap<>();
        //处理目标字段以及来源字段的映射
        TreeSet<String> fromColumnSet = resultTableMap.get("fromColumn");
        TreeSet<String> toColumnSet = resultTableMap.get("toColumn");
        TreeSet<String> toSet = resultTableMap.get("to");
        //fromColumnSet中的a.* unknown.id这种格式需转化(unknown对应具体的某个表字段) 转化
        List<String> fromColumnList = new ArrayList<>();
        for (String fromColumn : fromColumnSet){
            String[] tableColumn = fromColumn.split(":");
            if("unknown".equalsIgnoreCase(tableColumn[0])){
                fromColumnList.add(allColumnInfo.stream().filter(v->tableColumn[1].equalsIgnoreCase(v.split(":")[1]))
                        .findFirst().get());
                continue;
            }
            if("*".equalsIgnoreCase(tableColumn[1])){
                fromColumnList.addAll(
                        allColumnInfo.stream().filter(v->v.split(":")[0].toLowerCase().equalsIgnoreCase((owner+"."+tableColumn[0]).toLowerCase())).collect(Collectors.toList())
                );
                continue;
            }
            fromColumnList.add(owner+"."+fromColumn);
        }
        //增加去重判断
        fromColumnList = fromColumnList.stream().distinct().collect(Collectors.toList());

        if(toColumnSet.isEmpty()){//获取全部字段信息
            log.info("需要处理所有字段的映射。");
            List<String> destColumnList = allColumnInfo.stream().filter(v->toSet.contains(v.split(":")[0].split("\\.")[1])).collect(Collectors.toList());
            for (String str : destColumnList){
                resultMap.put(str,fromColumnList.stream()
                        .filter(v->v.split(":")[1].equalsIgnoreCase(str.split(":")[1])).collect(Collectors.toList()));
            }
        }else{
            log.info("只处理血缘需要字段的映射。");
            for (String str : toColumnSet){
                resultMap.put(owner+"."+str,fromColumnList.stream()
                        .filter(v->v.split(":")[1].equalsIgnoreCase(str.split(":")[1])).collect(Collectors.toList()));
            }
        }

        return resultMap;
    }
    /**
     * 组装表数据血缘关系（列级别暂定）
     * @param sql
     * @param connectorProperties
     * @return
     */
    private static AtlasEntity.AtlasEntitiesWithExtInfo makeAtlasBloodRelation(Map<String, TreeSet<String>> resultTableMap,
                                                                               String sql,Properties connectorProperties,
                                                                               List<String> allColumnInfo,String owner){
        TreeSet<String> fromSet = resultTableMap.get("from");
        TreeSet<String> toSet = resultTableMap.get("to");
        AtlasEntity.AtlasEntitiesWithExtInfo atlasBloodEntities = new AtlasEntity.AtlasEntitiesWithExtInfo();
        //存在血缘关系
        log.info("存在表数据血缘关系...");
        String dbHostname = connectorProperties.getProperty("db.hostname");
        String dbPort = connectorProperties.getProperty("db.port");
        String dbname = connectorProperties.getProperty("db.name");

        AtlasEntity atlasBloodEntity = new AtlasEntity();
        atlasBloodEntity.setTypeName("Process");

        StringBuilder qualifiedProcessNameFromTo = new StringBuilder();
        JsonArray jsonInputs = dealInputOrOutput(fromSet,dbHostname, dbPort, dbname, qualifiedProcessNameFromTo,owner);
        qualifiedProcessNameFromTo.append("@");
        JsonArray jsonOutputs = dealInputOrOutput(toSet,dbHostname, dbPort, dbname, qualifiedProcessNameFromTo,owner);

        Map<String, Object> attributeBloodMap = new HashMap<>();
        attributeBloodMap.put("inputs",jsonInputs);
        attributeBloodMap.put("outputs",jsonOutputs);
        attributeBloodMap.put("qualifiedName",qualifiedProcessNameFromTo.toString());
        attributeBloodMap.put("name",sql);

        atlasBloodEntity.setAttributes(attributeBloodMap);
        List<AtlasEntity> entities = new ArrayList<>();
        //添加表的血缘关系
        entities.add(atlasBloodEntity);
        //添加列的血缘关系
        Map<String,List<String>> resultMap = makeAtlasColumnMap(resultTableMap,allColumnInfo,owner);
        AtlasEntity atlasColumnBloodEntity = null;
        for(Map.Entry<String,List<String>> entry:resultMap.entrySet()){
            atlasColumnBloodEntity = new AtlasEntity();
            atlasColumnBloodEntity.setTypeName("Process");

            Map<String, Object> attributeMap = new HashMap<>();
            attributeMap.put("name",sql);
            StringBuilder qualifiedProcessColumn = new StringBuilder();
            JsonArray jsonColumnInputs = processColumnRelation(entry.getValue(),dbHostname, dbPort, dbname,qualifiedProcessColumn);
            qualifiedProcessColumn.append("@");
            JsonArray jsonColumnOutputs = processColumnRelation(Arrays.asList(entry.getKey()),dbHostname, dbPort, dbname,qualifiedProcessColumn);
            attributeMap.put("qualifiedName",qualifiedProcessColumn.toString());
            attributeMap.put("inputs",jsonColumnInputs);
            attributeMap.put("outputs",jsonColumnOutputs);
            atlasColumnBloodEntity.setAttributes(attributeMap);
            entities.add(atlasColumnBloodEntity);
        }


        atlasBloodEntities.setEntities(entities);

        return atlasBloodEntities;
    }

    private static JsonArray processColumnRelation(List<String> valueList, String dbHostname,
                                                   String dbPort, String dbname,
                                                   StringBuilder qualifiedProcessColumn) {
        JsonArray result = new JsonArray();

        valueList.forEach(value->{
            qualifiedProcessColumn.append(dbHostname+":"+dbPort+":"+dbname+":"+"."+value+" ");

            JsonObject output = new JsonObject();
            JsonObject uniqueAttributesJsonObj = new JsonObject();
            uniqueAttributesJsonObj.addProperty("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+"."+value);
            output.addProperty("uniqueAttributes",uniqueAttributesJsonObj.toString());
            output.addProperty("typeName","rdbms_column");
            result.add(output);
        });
        return result;
    }

    private static void addOwnerPrefixToTable(String owner,List<String>... tableList){
        for(List<String> list : tableList){
            for(int i = 0,len = list.size(); i < len; i++) {
                String v = list.get(i);
                list.set(i, v.indexOf('.') == -1 ? owner+"."+v : v);
            }
        }

    }
    /*
     * 处理数据血缘的from（inputs）、to （outputs）的字段结构数据
     */
    private static JsonArray dealInputOrOutput(TreeSet<String> set,String dbHostname,String dbPort,String dbname,
                                               StringBuilder qualifiedProcessNameFromTo,String owner){
        JsonArray result = new JsonArray();
        set.forEach(toTable->{
            qualifiedProcessNameFromTo.append(dbHostname+":"+dbPort+":"+dbname+":"+owner+"."+toTable+" ");

            JsonObject output = new JsonObject();
            JsonObject uniqueAttributesJsonObj = new JsonObject();
            uniqueAttributesJsonObj.addProperty("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+owner+"."+toTable);
            output.addProperty("uniqueAttributes",uniqueAttributesJsonObj.toString());
            output.addProperty("typeName","rdbms_table");
            result.add(output);
        });
        return result;
    }
    /**
     * 根据connector class获取数据源类型
     * @param connectorProperties
     * @return
     */
    private static String getRdbmsType(Properties connectorProperties){
        String rdbmsType = "";
        String connectorClass = connectorProperties.getProperty("connector.class");
        if(connectorClass != null){
            String[] arr = connectorClass.split("\\.");
            rdbmsType = arr.length > 1 ? arr[arr.length-2] : arr[0];
        }
        return rdbmsType;
    }

    /**
     * 组装atlas 实体
     * @param entityType instance、db、table etc
     * @param connectorProperties
     * @return
     */
    private static List<AtlasEntity.AtlasEntityWithExtInfo>  makeAtlasEntity(RdbmsEntities.EntityType entityType,Properties connectorProperties,
                                                                             String table,List<String> allColumnInfo){
        List<AtlasEntity.AtlasEntityWithExtInfo> resultList = new ArrayList<>();

        String dbHostname = connectorProperties.getProperty("db.hostname");
        String dbPort = connectorProperties.getProperty("db.port");
        String dbPassword = connectorProperties.getProperty("db.user.password");
        String name = connectorProperties.getProperty("name");
        String username = connectorProperties.getProperty("db.user");
        String dbname = connectorProperties.getProperty("db.name");
        String rdbmsType = getRdbmsType(connectorProperties);

        AtlasEntity atlasEntity = null;
        // rdbms_instance
        if(entityType == RdbmsEntities.EntityType.RDBMS_INSTANCE){
            atlasEntity = new AtlasEntity();
            atlasEntity.setTypeName("rdbms_instance");
            Map<String, Object> attributeMap = new HashMap<>();
            attributeMap.put("qualifiedName",dbHostname+":"+dbPort);
            attributeMap.put("name",name);
            attributeMap.put("rdbms_type",rdbmsType);
            attributeMap.put("platform","zeta");
            attributeMap.put("cloudOrOnPrem","cloud");
            attributeMap.put("hostname",dbHostname);
            attributeMap.put("port",dbPort);
            attributeMap.put("protocol","http");
            attributeMap.put("contact_info","jdbc");
            attributeMap.put("comment","rdbms_instance API");
            attributeMap.put("description","rdbms_instance描述");
            attributeMap.put("owner",username);
            atlasEntity.setAttributes(attributeMap);

            AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
            instanceJsonEntity.setEntity(atlasEntity);
            resultList.add(instanceJsonEntity);
        }else if(entityType == RdbmsEntities.EntityType.RDBMS_DB){
            atlasEntity = new AtlasEntity();
            atlasEntity.setTypeName("rdbms_db");
            Map<String, Object> attributeDbMap = new HashMap<>();
            attributeDbMap.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname);
            attributeDbMap.put("name",dbname);
            attributeDbMap.put("owner",username);
            attributeDbMap.put("description","rdbms_db data API ");
            attributeDbMap.put("prodOrOther","");
            JsonObject jsonObj = new JsonObject();
            JsonObject uniqueAttributesJsonObj = new JsonObject();
            uniqueAttributesJsonObj.addProperty("qualifiedName",dbHostname+":"+dbPort);
            jsonObj.addProperty("uniqueAttributes",uniqueAttributesJsonObj.toString());
            jsonObj.addProperty("typeName","rdbms_instance");
            attributeDbMap.put("instance",jsonObj);
            atlasEntity.setAttributes(attributeDbMap);

            AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
            instanceJsonEntity.setEntity(atlasEntity);
            resultList.add(instanceJsonEntity);
        }else if(entityType == RdbmsEntities.EntityType.RDBMS_TABLE){
            atlasEntity = new AtlasEntity();
            atlasEntity.setTypeName("rdbms_table");
            Map<String, Object> attributeTableMap = new HashMap<>();
            attributeTableMap.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+table);
            attributeTableMap.put("name",table);
            attributeTableMap.put("createTime", DateUtils.currentTimestamp().toString());
            attributeTableMap.put("comment","rdbms table API");
            attributeTableMap.put("description","rdbms_table input");
            attributeTableMap.put("owner",username);
            attributeTableMap.put("type","table");

            JsonObject jsonObj = new JsonObject();
            JsonObject uniqueAttributesJsonObj = new JsonObject();
            uniqueAttributesJsonObj.addProperty("qualifiedName",dbHostname+":"+dbPort+":"+dbname);
            jsonObj.addProperty("typeName","rdbms_db");
            jsonObj.addProperty("uniqueAttributes",uniqueAttributesJsonObj.toString());
            attributeTableMap.put("db",jsonObj);
            atlasEntity.setAttributes(attributeTableMap);

            AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
            instanceJsonEntity.setEntity(atlasEntity);
            resultList.add(instanceJsonEntity);
        }else if(entityType == RdbmsEntities.EntityType.RDBMS_COLUMN){
            //获取表字段信息
            String jdbcDriver = null,jdbcURL = null;
            if("mysql".equalsIgnoreCase(rdbmsType)){
                jdbcDriver = "com.mysql.jdbc.Driver";
                jdbcURL = "jdbc:mysql://"+dbHostname+":"+dbPort+"/"+dbname;
            }
            if("oracle".equalsIgnoreCase(rdbmsType)){
                jdbcDriver = "oracle.jdbc.driver.OracleDriver";
                jdbcURL = "jdbc:oracle:thin:@"+dbHostname+":"+dbPort+":"+dbname;
            }

            Map<String, Object> attributeTableMap = null;
            List<DatabaseUtil.TableColumnInfo> columnList = DatabaseUtil.getInstance(jdbcDriver).getColumnNames(table,jdbcURL,username,dbPassword);
            if(!CollectionUtils.isEmpty(columnList)){
                for(DatabaseUtil.TableColumnInfo colInfo : columnList){
                    allColumnInfo.add(table+":"+colInfo.getColumnName());
                    attributeTableMap = new HashMap<>();
                    atlasEntity = new AtlasEntity();
                    atlasEntity.setTypeName("rdbms_column");
                    attributeTableMap.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+table+":"+colInfo.getColumnName());
                    attributeTableMap.put("name",colInfo.getColumnName());
                    attributeTableMap.put("comment","rdbms_column input");
                    attributeTableMap.put("owner",username);
                    attributeTableMap.put("data_type",colInfo.getDataType());
                    attributeTableMap.put("length",colInfo.getLength());
                    attributeTableMap.put("default_value","0");
                    attributeTableMap.put("isNullable",colInfo.isNullable());
                    attributeTableMap.put("isPrimaryKey",false);
                    JsonObject tableJson = new JsonObject();
                    JsonObject uniqueAttributesJsonObj = new JsonObject();
                    uniqueAttributesJsonObj.addProperty("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+table);
                    tableJson.addProperty("uniqueAttributes",uniqueAttributesJsonObj.toString());
                    tableJson.addProperty("typeName","rdbms_table");
                    attributeTableMap.put("table",tableJson);

                    atlasEntity.setAttributes(attributeTableMap);
                    AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
                    instanceJsonEntity.setEntity(atlasEntity);
                    resultList.add(instanceJsonEntity);
                }
            }
        }
        return resultList;
    }
    /**
     *  获取数据的血缘关系
     *  支持格式： create table|view xxx as select ...
     *          insert into tableXX select ...
     * @param sqlNode
     * @return
     */
    private static Map<String,Object> getBloodRelation(SqlNode sqlNode) {
        Map<String,Object> resultMap = new HashMap<>();
        List<String> bloodTableList = new ArrayList<>();
        resultMap.put("operType",RdbmsEntities.OperateType.ADD);
        resultMap.put("tableBlood",bloodTableList);

        if(Objects.isNull(sqlNode)) {
            log.info("sql parse error.");
            return null;
        }

        if (sqlNode.getKind() == SqlKind.CREATE_TABLE) {
            log.info("----create_table");
            resultMap.put("operType",RdbmsEntities.OperateType.ADD);
            SqlCreateTable sqlCreate = (SqlCreateTable) sqlNode;
            bloodTableList.add(sqlCreate.name.toString());
            getDependencies(sqlCreate.query,bloodTableList);
        }else if(sqlNode.getKind() == SqlKind.CREATE_VIEW) {
            log.info("----create_view");
            resultMap.put("operType",RdbmsEntities.OperateType.ADD);
            SqlCreateView sqlCreate = (SqlCreateView) sqlNode;
            bloodTableList.add(sqlCreate.name.toString());
            getDependencies(sqlCreate.query,bloodTableList);
        }else if (sqlNode.getKind() == SqlKind.CREATE_MATERIALIZED_VIEW) {
            log.info("----create_MATERIALIZED VIEW ");
            resultMap.put("operType",RdbmsEntities.OperateType.ADD);
            SqlCreateMaterializedView sqlCreate = (SqlCreateMaterializedView) sqlNode;
            bloodTableList.add(sqlCreate.name.toString());
            getDependencies(sqlCreate.query,bloodTableList);
        }else if (sqlNode.getKind() == SqlKind.INSERT) {
            log.info("----insert");
            resultMap.put("operType",RdbmsEntities.OperateType.ADD);
            SqlInsert sqlCreate = (SqlInsert) sqlNode;
            bloodTableList.add(sqlCreate.getTargetTable().toString());
            getDependencies(sqlCreate.getSource(),bloodTableList);
        }else if(sqlNode.getKind() == SqlKind.DROP_TABLE) {
            log.info("----drop table");
            resultMap.put("operType",RdbmsEntities.OperateType.DROP);
            SqlDropTable sqlDrop = (SqlDropTable) sqlNode;
            bloodTableList.add(sqlDrop.name.toString());
        }else if(sqlNode.getKind() == SqlKind.DROP_VIEW) {
            log.info("----drop view");
            resultMap.put("operType",RdbmsEntities.OperateType.DROP);
            SqlDropView sqlDrop = (SqlDropView) sqlNode;
            bloodTableList.add(sqlDrop.name.toString());
        }else if(sqlNode.getKind() == SqlKind.DROP_MATERIALIZED_VIEW) {
            log.info("----drop MATERIALIZED_VIEW");
            resultMap.put("operType",RdbmsEntities.OperateType.DROP);
            SqlDropMaterializedView sqlDrop = (SqlDropMaterializedView) sqlNode;
            bloodTableList.add(sqlDrop.name.toString());
        }else if(sqlNode.getKind() == SqlKind.UPDATE) {
            log.info("----drop UPDATE");
            resultMap.put("operType",RdbmsEntities.OperateType.MODIFY);
            SqlUpdate sqlUpdate = (SqlUpdate) sqlNode;
            bloodTableList.add(sqlUpdate.getTargetTable().toString());
            getDependencies(sqlUpdate.getSourceSelect(),bloodTableList);
        }else{
            log.info("no blood relation.");
        }
        return resultMap;
    }

    /**
     *  找出表的依赖关系
     * @param sqlNode
     * @param result
     * @return
     */
    private static List<String> getDependencies(SqlNode sqlNode, List<String> result) {
        if(sqlNode == null){
            return null;
        }
        if (sqlNode.getKind() == SqlKind.JOIN) {
            SqlJoin sqlKind = (SqlJoin) sqlNode;
            log.info("-----join");

            getDependencies(sqlKind.getLeft(), result);
            getDependencies(sqlKind.getRight(), result);
        }

        if (sqlNode.getKind() == SqlKind.UNION) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            log.info("----union");

            getDependencies(sqlKind.getOperandList().get(0), result);
            getDependencies(sqlKind.getOperandList().get(1), result);

        }

        if (sqlNode.getKind() == SqlKind.IDENTIFIER) {
            log.info("-----identifier");
            result.add(sqlNode.toString());
        }

        if (sqlNode.getKind() == SqlKind.SELECT) {
            SqlSelect sqlKind = (SqlSelect) sqlNode;
            log.info("-----select");
            getDependencies(sqlKind.getFrom(), result);
        }

        if (sqlNode.getKind() == SqlKind.AS) {
            SqlBasicCall sqlKind = (SqlBasicCall) sqlNode;
            log.info("----as");
            getDependencies(sqlKind.getOperandList().get(0), result);
        }

        if (sqlNode.getKind() == SqlKind.ORDER_BY) {
            SqlOrderBy sqlKind = (SqlOrderBy) sqlNode;
            log.info("----order_by");
            getDependencies(sqlKind.getOperandList().get(0), result);
        }
        return result;
    }

}
