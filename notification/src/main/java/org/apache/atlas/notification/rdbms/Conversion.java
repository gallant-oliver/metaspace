
package org.apache.atlas.notification.rdbms;

import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import org.apache.atlas.AtlasException;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;

public interface Conversion {

    /**
     * 解析Connector推送的数据，并组装成atlas可识别的实体及血缘关系。
     * 如果Connector推送的数据中不存在元数据变化，并且不存在数据血缘信息，返回null
     * @param notification Connector数据
     * @param config Connector配置
     * @return 解析后的atlas实体及数据血缘关系
     */
    RdbmsEntities convert(Notification notification, KafkaConnector.Config config) throws AtlasException;
}