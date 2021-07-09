package org.apache.atlas.notification.rdbms;

import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.instance.debezium.RdbmsMessage;
import org.apache.atlas.model.notification.RdbmsNotification;
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

    public static RdbmsEntities getSimulationRdbmsEntities(RdbmsNotification notification, Properties connectorProperties) throws IOException {
        RdbmsMessage.Payload payload = notification.getRdbmsMessage().getPayload();
        Boolean isDdl = StringUtils.isBlank(payload.getOp());
        String sql = isDdl ?  payload.getDdl() : payload.getSource().getQuery() ;
        sql = sql.replace("`","").replaceAll("\"","").replaceAll("\'","");

        String rdbmsType = getRdbmsType(connectorProperties);
        Map<String, TreeSet<String>> resultTableMap = DruidAnalyzerUtil.getFromTo(sql,rdbmsType);
        TreeSet<String> fromSet = resultTableMap.get("from");
        TreeSet<String> toSet = resultTableMap.get("to");
        String entityType = resultTableMap.get("type").first(); //table or view
        if(resultTableMap == null
                || (fromSet.isEmpty() && toSet.isEmpty()) ){
            log.info("sql解析没有表对象，不需要处理..");
            return new RdbmsEntities();
        }
        String upperSql = sql.trim().replaceAll("\\s+", " ").toUpperCase();
        String alterType = upperSql.startsWith("ALTER TABLE") && upperSql.split("\\s+").length > 4 ? upperSql.split("\\s+")[3] : "DEFAULT";
        RdbmsEntities.OperateType operateType =
                StringUtils.startsWithAny(upperSql, new String[] { "CREATE", "INSERT"}) || StringUtils.equalsIgnoreCase("ADD",alterType) ? RdbmsEntities.OperateType.ADD :
                        (StringUtils.startsWithAny(upperSql,new String[] { "DROP", "DELETE"}) || StringUtils.equalsIgnoreCase("DROP",alterType) ? RdbmsEntities.OperateType.DROP :
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
        //表.列 格式的list 结构进行组装
        List<String> allColumnInfo = new ArrayList<>();
        connectorProperties.put("table.type",entityType);
        dealTableColumnEntity(fromTableList, connectorProperties, fromTableEntityList, fromColumnEntityList,allColumnInfo);
        connectorProperties.put("isDropTable",operateType);
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
        addMapOrNot(modifyMap,RdbmsEntities.EntityType.RDBMS_TABLE,fromTableEntityList);
        addMapOrNot(modifyMap,RdbmsEntities.EntityType.RDBMS_COLUMN,fromColumnEntityList);

        //添加输出（最终）表 列 column table
        if(operateType == RdbmsEntities.OperateType.MODIFY){
            addMapOrNot(modifyMap,RdbmsEntities.EntityType.RDBMS_TABLE,toTableEntityList);
            addMapOrNot(modifyMap,RdbmsEntities.EntityType.RDBMS_COLUMN,toColumnEntityList);
        }else{
            addMapOrNot(operateMap,RdbmsEntities.EntityType.RDBMS_TABLE,toTableEntityList);
            addMapOrNot(operateMap,RdbmsEntities.EntityType.RDBMS_COLUMN,toColumnEntityList);
        }
        //血缘关系
        if(!CollectionUtils.isEmpty(atlasBloodEntities.getEntities())){
            rdbmsEntities.setBloodEntities(atlasBloodEntities);
        }

        return rdbmsEntities;
    }

    private static void addMapOrNot(Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> map,
                                    RdbmsEntities.EntityType type, List<AtlasEntity.AtlasEntityWithExtInfo> entityList){
        if(!CollectionUtils.isEmpty(entityList)){
            map.put(type, entityList);
        }
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
                String dropTable = connectorProperties.getProperty("isDropTable");
                if(RdbmsEntities.OperateType.DROP.name().equalsIgnoreCase(dropTable)){
                    log.info("drop 操作，不需要获取列字段信息。");
                    continue;
                }
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
        //处理目标字段以及来源字段的映射
        TreeSet<String> fromColumnSet = resultTableMap.get("fromColumn");
        TreeSet<String> toColumnSet = resultTableMap.get("toColumn");
        TreeSet<String> toSet = resultTableMap.get("to");

        //List<String> fromTableList = new ArrayList<>(fromSet);
        List<String> toTableList = new ArrayList<>(toSet);
        addOwnerPrefixToTable(owner,toTableList);

        //fromColumnSet中的a.* unknown.id这种格式需转化(unknown对应具体的某个表字段) 转化
        List<String> fromColumnList = new ArrayList<>();
        for (String fromColumn : fromColumnSet){
            String[] columnAliasArray = fromColumn.split("=>");
            String[] tableColumn = columnAliasArray[1].split(":");

            if("unknown".equalsIgnoreCase(tableColumn[0])){
                fromColumnList.add(allColumnInfo.stream().filter(v->tableColumn[1].equalsIgnoreCase(v.split(":")[1]))
                        .findFirst().map(v->columnAliasArray[0]+"=>"+v).get());
                continue;
            }
            if("*".equalsIgnoreCase(tableColumn[1])){
                fromColumnList.addAll(
                        allColumnInfo.stream().filter(v->v.split(":")[0].toLowerCase().equalsIgnoreCase(tableColumn[0].contains(".") ? tableColumn[0].toLowerCase() : (owner+"."+tableColumn[0]).toLowerCase()))
                                .map(v->columnAliasArray[0]+"=>"+v).collect(Collectors.toList())
                );
                continue;
            }
            fromColumnList.add(columnAliasArray[0]+"=>"+(columnAliasArray[1].contains(".") ? columnAliasArray[1] : owner+"."+columnAliasArray[1]));
        }
        //增加去重判断
        fromColumnList = fromColumnList.stream().distinct().collect(Collectors.toList());

        if(toColumnSet.isEmpty()){//获取全部字段信息
            log.info("需要处理所有字段的映射。");
            List<String> destColumnList = allColumnInfo.stream().filter(v->toTableList.contains(v.split(":")[0])).collect(Collectors.toList());
            return getFieldLineageMap(destColumnList,fromColumnList);
        }else{
            log.info("只处理血缘需要字段的映射。");
            return getFieldLineageMap(toColumnSet,fromColumnList);
        }
    }
    /*
    * 处理表血缘 字段映射关系
     */
    private static Map<String,List<String>> getFieldLineageMap(Collection<String> destColumnList,List<String> fromColumnList){
        Map<String,List<String>> resultMap = new HashMap<>();
        for (String str : destColumnList){
            //优先别名相同，否则使用原始名称
            List<String> mapList = fromColumnList.stream()
                    .filter(v->v.split("=>")[0].equalsIgnoreCase(str.split(":")[1]))
                    .map(v->v.split("=>")[1])
                    .collect(Collectors.toList());
            if(CollectionUtils.isEmpty(mapList)){
                mapList = fromColumnList.stream()
                        .filter(v->v.split(":")[1].equalsIgnoreCase(str.split(":")[1]))
                        .map(v->v.split("=>")[1])
                        .collect(Collectors.toList());
            }
            resultMap.put(str,mapList);
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
        List<String> fromTableList = new ArrayList<>(fromSet);
        List<String> toTableList = new ArrayList<>(toSet);
        addOwnerPrefixToTable(owner,fromTableList,toTableList);

        AtlasEntity.AtlasEntitiesWithExtInfo atlasBloodEntities = new AtlasEntity.AtlasEntitiesWithExtInfo();
        //存在血缘关系
        log.info("存在表数据血缘关系...");
        String dbHostname = connectorProperties.getProperty("db.hostname");
        String dbPort = connectorProperties.getProperty("db.port");
        String dbname = connectorProperties.getProperty("db.name");

        AtlasEntity atlasBloodEntity = new AtlasEntity();
        atlasBloodEntity.setTypeName("Process");

        StringBuilder qualifiedProcessNameFromTo = new StringBuilder();
        List<Map<String, Object>> jsonInputs = dealInputOrOutput(fromTableList,dbHostname, dbPort, dbname, qualifiedProcessNameFromTo);
        qualifiedProcessNameFromTo.append("@");
        List<Map<String, Object>> jsonOutputs = dealInputOrOutput(toTableList,dbHostname, dbPort, dbname, qualifiedProcessNameFromTo);

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
            List<Map<String, Object>> jsonColumnInputs = processColumnRelation(entry.getValue(),dbHostname, dbPort, dbname,qualifiedProcessColumn);
            qualifiedProcessColumn.append("@");
            List<Map<String, Object>> jsonColumnOutputs = processColumnRelation(Arrays.asList(entry.getKey()),dbHostname, dbPort, dbname,qualifiedProcessColumn);
            attributeMap.put("qualifiedName",qualifiedProcessColumn.toString());
            attributeMap.put("inputs",jsonColumnInputs);
            attributeMap.put("outputs",jsonColumnOutputs);
            atlasColumnBloodEntity.setAttributes(attributeMap);
            entities.add(atlasColumnBloodEntity);
        }


        atlasBloodEntities.setEntities(entities);

        return atlasBloodEntities;
    }

    private static List<Map<String,Object>> processColumnRelation(List<String> valueList, String dbHostname,
                                                   String dbPort, String dbname,
                                                   StringBuilder qualifiedProcessColumn) {
        List<Map<String,Object>> result = new ArrayList<>();

        valueList.forEach(value->{
            qualifiedProcessColumn.append(dbHostname+":"+dbPort+":"+dbname+":"+value+" ");

            Map<String,Object> instance = new HashMap<>();
            instance.put("typeName","rdbms_column");
            Map<String,Object> uniqueAttributes  = new HashMap<>();
            uniqueAttributes.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+value);
            instance.put("uniqueAttributes",uniqueAttributes);

            result.add(instance);
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
    private static List<Map<String,Object>> dealInputOrOutput(List<String> list,String dbHostname,String dbPort,String dbname,
                                               StringBuilder qualifiedProcessNameFromTo){
        List<Map<String,Object>> result = new ArrayList<>();
        list.forEach(toTable->{
            qualifiedProcessNameFromTo.append(dbHostname+":"+dbPort+":"+dbname+":"+toTable+" ");

            Map<String,Object> instance = new HashMap<>();
            instance.put("typeName","rdbms_table");
            Map<String,Object> uniqueAttributes  = new HashMap<>();
            uniqueAttributes.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+toTable);
            instance.put("uniqueAttributes",uniqueAttributes);

            result.add(instance);
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
            attributeMap.put("name",dbHostname+":"+dbPort);
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

            Map<String,Object> instance = new HashMap<>();
            instance.put("typeName","rdbms_instance");
            Map<String,Object> uniqueAttributes  = new HashMap<>();
            uniqueAttributes.put("qualifiedName",dbHostname+":"+dbPort);
            instance.put("uniqueAttributes",uniqueAttributes);
            attributeDbMap.put("instance",instance);
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
            attributeTableMap.put("createTime", new Date());
            attributeTableMap.put("comment","rdbms table API");
            attributeTableMap.put("description","rdbms_table input");
            attributeTableMap.put("owner",username);
            String type = connectorProperties.getProperty("table.type");
            log.info("当前操作的实体类型是 ====> {}",type);
            attributeTableMap.put("type",type);

            Map<String,Object> instance = new HashMap<>();
            instance.put("typeName","rdbms_db");
            Map<String,Object> uniqueAttributes  = new HashMap<>();
            uniqueAttributes.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname);
            instance.put("uniqueAttributes",uniqueAttributes);

            attributeTableMap.put("db",instance);
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

                    Map<String,Object> instance = new HashMap<>();
                    instance.put("typeName","rdbms_table");
                    Map<String,Object> uniqueAttributes  = new HashMap<>();
                    uniqueAttributes.put("qualifiedName",dbHostname+":"+dbPort+":"+dbname+":"+table);
                    instance.put("uniqueAttributes",uniqueAttributes);
                    attributeTableMap.put("table",instance);

                    atlasEntity.setAttributes(attributeTableMap);
                    AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntity = new  AtlasEntity.AtlasEntityWithExtInfo();
                    instanceJsonEntity.setEntity(atlasEntity);
                    resultList.add(instanceJsonEntity);
                }
            }
        }
        return resultList;
    }


}
