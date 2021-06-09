
package org.apache.atlas.notification.rdbms;

import org.apache.atlas.AtlasException;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;

import java.util.Map;
import java.util.Properties;

public interface Conversion {

    /**
     * 解析debezium推送的数据，并组装成atlas可识别的实体及血缘关系。
     * 如果debezium推送的数据中不存在元数据变化，并且不存在数据血缘信息，返回null
     * @param notification debezium数据
     * @param connectorConfig debezium配置
     * @return 解析后的atlas实体及数据血缘关系
     */
    RdbmsEntities convert(Notification notification, Properties connectorConfig) throws AtlasException;
}