package org.apache.atlas.notification.rdbms;

import org.apache.atlas.AtlasException;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import java.util.Properties;


@Singleton
@Component
public class RdbmsConversion implements Conversion{
    @Override
    public RdbmsEntities convert(Notification notification, Properties connectorProperties) throws AtlasException {
        RdbmsEntities rdbmsEntities = null;
        if(!(notification instanceof  RdbmsNotification)){
            throw new RuntimeException("RdbmsConversion.convert无法解析非RdbmsNotification类型的数据");
        }
        try{//解析debezium数据，并分装成atlas实体与血缘
            RdbmsNotification rdbmsMessage = (RdbmsNotification)notification;
            rdbmsEntities = CalciteParseSqlTools.getSimulationRdbmsEntities(rdbmsMessage, connectorProperties);

        }catch (Exception e) {
            throw new AtlasException("解析debezium推送的数据失败");
        }
        return rdbmsEntities;
    }



   /* public RdbmsEntities getSimulationRdbmsEntities (Notification notification, Properties connectorProperties) throws IOException {

        ObjectMapper mapper = new ObjectMapper();


        String instanceJson = "{" +
                "    \"entity\": {" +
                "    \"typeName\":   \"rdbms_instance\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"192.168.8.129:3306\"," +
                "\"name\":\"10.200.50.69:15432\"," +
                "\"rdbms_type\":\"POSTGRE\"," +
                "\"platform\":\"ms\"," +
                "\"cloudOrOnPrem\":\"cloud\"," +
                "\"hostname\":\"10.200.50.69\"," +
                "\"port\":\"15432\"," +
                "\"protocol\":\"http\"," +
                "\"contact_info\":\"jdbc\"," +
                "\"comment\":\"rdbms_instance API insert test\"," +
                "\"description\":\"rdbms_instance描述\"," +
                "\"owner\":\"whz\"" +
                "}" +
                "    }" +
                "}";

        String dbJson = "{" +
                "    \"entity\": {" +
                "    \"typeName\":   \"rdbms_db\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db\"," +
                "\"owner\":\"whz\"," +
                "    \"name\":          \"test_db \"," +
                "\"description\":   \"rdbms_db datag API input\"," +
                "\"prodOrOther\": \"\"," +
                "\"instance\":{" +
                "                \"qualifiedName\": \"26bc364b-6f8a-406a-b559-82e86985a122\"," +
                "                \"typeName\": \"rdbms_instance\"" +
                "            }" +
                "}" +
                "    }" +
                "}";


        String columnJson1 = "{" +
                "    \"entity\": {" +
                "    \"typeName\":   \"rdbms_column\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table.id\"," +
                "\"name\":          \"id\"," +
                "\"comment\":\"rdbms_column API insert test\"," +
                "\"description\":\"rdbms_column  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"data_type\":\"String\"," +
                "\"length\":20," +
                "\"default_value\":\"0\"," +
                "\"isNullable\":false," +
                "\"isPrimaryKey\":true" +
                "}" +
                "    }" +
                "}";

        String tableJson1 = "{" +
                "    \"entity\": {" +
                "    \"typeName\":   \"rdbms_table\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table\"," +
                "\"createTime\":    \"2021-04-25T13:15:25.369Z\"," +
                "\"name\":          \"public.test_table\"," +
                "\"comment\":\"test_table API insert test\"," +
                "\"description\":\"rdbms_table  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"type\":\"table\"," +
                "\"contact_info\":\"mytable_info\"," +
                "\"db\": {" +
                "                \"qualifiedName\": \"192.168.8.129:3306.test_db\"," +
                "                \"typeName\": \"rdbms_db\"" +
                "            }," +
                "\"columns\": [{" +
                "                \"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table.id\"," +
                "                \"typeName\": \"rdbms_column\"" +
                "            }]" +
                "}" +
                "    }" +
                "}";

        String columnJson2 = "{" +
                "    \"entity\": {" +
                "    \"typeName\":   \"rdbms_column\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table_1.id\"," +
                "\"name\":          \"id\"," +
                "\"comment\":\"rdbms_column API insert test\"," +
                "\"description\":\"rdbms_column  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"data_type\":\"String\"," +
                "\"length\":20," +
                "\"default_value\":\"0\"," +
                "\"isNullable\":false," +
                "\"isPrimaryKey\":true" +
                "}" +
                "    }" +
                "}";

        String tableJson2 = "{" +
                "    \"entity\": {" +
                "    \"typeName\":   \"rdbms_table\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table_1\"," +
                "\"createTime\":    \"2021-04-25T13:15:25.369Z\"," +
                "\"name\":          \"public.test_table_1\"," +
                "\"comment\":\"public.test_table_1 API insert test\"," +
                "\"description\":\"public.test_table_1  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"type\":\"table\"," +
                "\"contact_info\":\"mytable_info\"," +
                "\"db\": {" +
                "                \"qualifiedName\": \"192.168.8.129:3306.test_db\"," +
                "                \"typeName\": \"rdbms_db\"" +
                "            }," +
                "\"columns\": [{" +
                "                \"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table_1.id\"," +
                "                \"typeName\": \"rdbms_column\"" +
                "            }]" +
                "}" +
                "    }" +
                "}";

        String bloodJson = "{" +
                "\"entities\": [" +
                "{" +
                "\"typeName\": \"Process\"," +
                "\"attributes\": {" +
                "\"name\": \"create table public.test_table_1 as select * from mytable\"," +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1.Process@ms:000\"," +
                "\"inputs\": [" +
                "{" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table\"," +
                "\"typeName\": \"rdbms_table\"" +
                "}" +
                "]," +
                "\"outputs\": [" +
                "{" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table_1\"," +
                "\"typeName\": \"rdbms_table\"" +
                "}" +
                "]" +
                "}" +
                "}," +
                "{" +
                "\"typeName\": \"Process\"," +
                "\"attributes\": {" +
                "\"name\": \"create table public.test_table_1 as select * from mytable\"," +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1.Process@ms:001\"," +
                "\"inputs\": [" +
                "{" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table.id\"," +
                "\"typeName\": \"rdbms_column\"" +
                "}" +
                "]," +
                "\"outputs\": [" +
                "{" +
                "\"qualifiedName\": \"192.168.8.129:3306.test_db.public.test_table_1.id\"," +
                "\"typeName\": \"rdbms_column\"" +
                "}" +
                "]" +
                "}" +
                "}" +
                "" +
                "]" +
                "}";

        RdbmsEntities rdbmsEntities = new RdbmsEntities();
        SortedMap<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> entityMap = rdbmsEntities.getEntityMap();
        //添加数据库实例
        AtlasEntity.AtlasEntityWithExtInfo instanceJsonEntityWithExtInfo = mapper.readValue(instanceJson, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_INSTANCE, Arrays.asList(instanceJsonEntityWithExtInfo));
       //添加数据库
        AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = mapper.readValue(dbJson, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_DB, Arrays.asList(dbEntityWithExtInfo));

        //添加列，由于column_2_EntityWithExtInfo依赖于column_1_EntityWithExtInfo，因此，在构建list时,column_1_EntityWithExtInfo排列在column_2_EntityWithExtInfo前面
        AtlasEntity.AtlasEntityWithExtInfo column_1_EntityWithExtInfo = mapper.readValue(columnJson1, AtlasEntity.AtlasEntityWithExtInfo.class);
        AtlasEntity.AtlasEntityWithExtInfo column_2_EntityWithExtInfo = mapper.readValue(columnJson2, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_COLUMN, Arrays.asList(column_1_EntityWithExtInfo,column_2_EntityWithExtInfo));

        //添加列，由于table_2_EntityWithExtInfo依赖于table_1_EntityWithExtInfo，因此，在构建list时,table_1_EntityWithExtInfo排列在table_2_EntityWithExtInfo前面
        AtlasEntity.AtlasEntityWithExtInfo table_1_EntityWithExtInfo = mapper.readValue(tableJson1, AtlasEntity.AtlasEntityWithExtInfo.class);
        AtlasEntity.AtlasEntityWithExtInfo table_2_EntityWithExtInfo = mapper.readValue(tableJson2, AtlasEntity.AtlasEntityWithExtInfo.class);
        entityMap.put(RdbmsEntities.EntityType.RDBMS_TABLE, Arrays.asList(table_1_EntityWithExtInfo,table_2_EntityWithExtInfo));

        AtlasEntity.AtlasEntitiesWithExtInfo atlasEntitiesWithExtInfo = mapper.readValue(bloodJson, AtlasEntity.AtlasEntitiesWithExtInfo.class);

        rdbmsEntities.setBloodEntities(atlasEntitiesWithExtInfo);
        return rdbmsEntities;
    }*/
}