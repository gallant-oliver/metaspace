package org.apache.atlas.notification.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.atlas.AtlasException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;


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
            rdbmsEntities = getSimulationRdbmsEntities(rdbmsMessage, connectorProperties);

        }catch (Exception e) {
            throw new AtlasException("解析debezium推送的数据失败");
        }
        return rdbmsEntities;
    }



    public RdbmsEntities getSimulationRdbmsEntities (Notification notification, Properties connectorProperties) throws IOException {

        ObjectMapper mapper = new ObjectMapper();


        String sourceJson = "{" +
                "    \"entity\": {" +
                "\"guid\": \"26bc364b-6f8a-406a-b559-82e86985a122\"," +
                "    \"typeName\":   \"rdbms_instance\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32@ms\"," +
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
                "\"guid\": \"2d23b26c-8578-44de-b80c-caeccb632783\"," +
                "    \"typeName\":   \"rdbms_db\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db@ms\"," +
                "\"owner\":\"whz\"," +
                "    \"name\":          \"test_db \"," +
                "\"description\":   \"rdbms_db datag API input\"," +
                "\"prodOrOther\": \"\"," +
                "\"instance\":{" +
                "                \"guid\": \"26bc364b-6f8a-406a-b559-82e86985a122\"," +
                "                \"typeName\": \"rdbms_instance\"" +
                "            }" +
                "}" +
                "    }" +
                "}";
        String columnJson1 = "{" +
                "    \"entity\": {" +
                "\"guid\": \"ea875ad3-4056-4f9d-8922-f399220d869a\"," +
                "    \"typeName\":   \"rdbms_column\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table.id@ms\"," +
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
                "\"guid\": \"df5c5fed-35f3-4a6c-81e4-cd59062b5dab\"," +
                "    \"typeName\":   \"rdbms_table\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table@ms\"," +
                "\"createTime\":    \"2021-04-25T13:15:25.369Z\"," +
                "\"name\":          \"public.test_table\"," +
                "\"comment\":\"test_table API insert test\"," +
                "\"description\":\"rdbms_table  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"type\":\"table\"," +
                "\"contact_info\":\"mytable_info\"," +
                "\"db\": {" +
                "                \"guid\": \"2d23b26c-8578-44de-b80c-caeccb632783\"," +
                "                \"typeName\": \"rdbms_db\"" +
                "            }," +
                "\"columns\": [{" +
                "                \"guid\": \"ea875ad3-4056-4f9d-8922-f399220d869a\"," +
                "                \"typeName\": \"rdbms_column\"" +
                "            }]" +
                "}" +
                "    }" +
                "}";

        String columnJson2 = "{" +
                "    \"entity\": {" +
                "\"guid\": \"78e63cf1-f0d7-4d74-9960-ab964f1821b5\"," +
                "    \"typeName\":   \"rdbms_column\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1.id@ms\"," +
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
                "\"guid\": \"99fdefb9-b414-449b-a127-c09ae4fa8486\"," +
                "    \"typeName\":   \"rdbms_table\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1@ms\"," +
                "\"createTime\":    \"2021-04-25T13:15:25.369Z\"," +
                "\"name\":          \"public.test_table_1\"," +
                "\"comment\":\"public.test_table_1 API insert test\"," +
                "\"description\":\"public.test_table_1  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"type\":\"table\"," +
                "\"contact_info\":\"mytable_info\"," +
                "\"db\": {" +
                "                \"guid\": \"2d23b26c-8578-44de-b80c-caeccb632783\"," +
                "                \"typeName\": \"rdbms_db\"" +
                "            }," +
                "\"columns\": [{" +
                "                \"guid\": \"78e63cf1-f0d7-4d74-9960-ab964f1821b5\"," +
                "                \"typeName\": \"rdbms_column\"" +
                "            }]" +
                "}" +
                "    }" +
                "}";

        String blukJson = "{" +
                "    \"entity\": {" +
                "\"guid\": \"99fdefb9-b414-449b-a127-c09ae4fa8486\"," +
                "    \"typeName\":   \"rdbms_table\"," +
                "\"attributes\": {" +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1@ms\"," +
                "\"createTime\":    \"2021-04-25T13:15:25.369Z\"," +
                "\"name\":          \"public.test_table_1\"," +
                "\"comment\":\"public.test_table_1 API insert test\"," +
                "\"description\":\"public.test_table_1  手动输入\"," +
                "\"owner\":\"whz\"," +
                "\"type\":\"table\"," +
                "\"contact_info\":\"mytable_info\"," +
                "\"db\": {" +
                "                \"guid\": \"2d23b26c-8578-44de-b80c-caeccb632783\"," +
                "                \"typeName\": \"rdbms_db\"" +
                "            }," +
                "\"columns\": [{" +
                "                \"guid\": \"78e63cf1-f0d7-4d74-9960-ab964f1821b5\"," +
                "                \"typeName\": \"rdbms_column\"" +
                "            }]" +
                "}" +
                "    }" +
                "}" +
                "" +
                "" +
                "" +
                "" +
                "" +
                "{" +
                "\"entities\": [" +
                "{" +
                "\"typeName\": \"Process\"," +
                "\"guid\" : \"4674bad6-6425-4573-9332-0cf89e8750c2\"," +
                "\"attributes\": {" +
                "\"name\": \"create table public.test_table_1 as select * from mytable\"," +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1.Process@ms:000\"," +
                "\"inputs\": [" +
                "{" +
                "\"guid\": \"df5c5fed-35f3-4a6c-81e4-cd59062b5dab\"," +
                "\"typeName\": \"rdbms_table\"" +
                "}" +
                "]," +
                "\"outputs\": [" +
                "{" +
                "\"guid\": \"99fdefb9-b414-449b-a127-c09ae4fa8486\"," +
                "\"typeName\": \"rdbms_table\"" +
                "}" +
                "]" +
                "}" +
                "}," +
                "{" +
                "\"guid\": \"5db638fd-cd68-4308-abc3-373e8a048c0c\"," +
                "\"typeName\": \"Process\"," +
                "\"attributes\": {" +
                "\"name\": \"create table public.test_table_1 as select * from mytable\"," +
                "\"qualifiedName\": \"1443c338-d3b6-4b24-bc05-5de60e5ccb32.test_db.public.test_table_1.Process@ms:001\"," +
                "\"inputs\": [" +
                "{" +
                "\"guid\": \"ea875ad3-4056-4f9d-8922-f399220d869a\"," +
                "\"typeName\": \"rdbms_column\"" +
                "}" +
                "]," +
                "\"outputs\": [" +
                "{" +
                "\"guid\": \"78e63cf1-f0d7-4d74-9960-ab964f1821b5\"," +
                "\"typeName\": \"rdbms_column\"" +
                "}" +
                "]" +
                "}" +
                "}" +
                "" +
                "]" +
                "}";

        AtlasEntity.AtlasEntityWithExtInfo sourceEntityWithExtInfo = mapper.readValue(sourceJson, AtlasEntity.AtlasEntityWithExtInfo.class);

        AtlasEntity.AtlasEntityWithExtInfo dbEntityWithExtInfo = mapper.readValue(dbJson, AtlasEntity.AtlasEntityWithExtInfo.class);



        AtlasEntity.AtlasEntitiesWithExtInfo atlasEntitiesWithExtInfo = mapper.readValue(blukJson, AtlasEntity.AtlasEntitiesWithExtInfo.class);

        return new RdbmsEntities(Arrays.asList(sourceEntityWithExtInfo,dbEntityWithExtInfo), atlasEntitiesWithExtInfo);
    }
}