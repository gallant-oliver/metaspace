package org.apache.atlas.notification.rdbms;

import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import org.apache.atlas.AtlasException;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;


@Singleton
@Component
public class RdbmsConversion implements Conversion{
    @Override
    public RdbmsEntities convert(Notification notification, KafkaConnector.Config config) throws AtlasException {
        RdbmsEntities rdbmsEntities = null;
        if(!(notification instanceof  RdbmsNotification)){
            throw new RuntimeException("RdbmsConversion.convert无法解析非RdbmsNotification类型的数据");
        }
        try{//解析sql数据，并分装成atlas实体与血缘
            RdbmsNotification rdbmsMessage = (RdbmsNotification)notification;
            rdbmsEntities = CalciteParseSqlTools.getSimulationRdbmsEntities(rdbmsMessage, config);
        }catch (Exception e) {
            throw new AtlasException("解析connector推送的数据失败",e);
        }
        return rdbmsEntities;
    }

}