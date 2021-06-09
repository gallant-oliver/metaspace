package org.apache.atlas.notification.rdbms;

import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;


@Singleton
@Component
public class RdbmsConversion implements Conversion{
    @Override
    public RdbmsEntities convert(Notification notification) {

        if(!(notification instanceof  RdbmsNotification)){
            throw new RuntimeException("RdbmsConversion.convert无法解析非RdbmsNotification类型的数据");
        }
        //解析debezium数据，并分装成atlas实体与血缘
        RdbmsNotification rdbmsMessage = (RdbmsNotification)notification;
        System.out.println("解析rdbms数据：" + rdbmsMessage.toString());
        return new RdbmsEntities();
    }
}