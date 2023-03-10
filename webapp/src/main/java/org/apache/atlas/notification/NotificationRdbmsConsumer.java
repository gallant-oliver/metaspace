package org.apache.atlas.notification;

import com.google.common.annotations.VisibleForTesting;
import io.zeta.metaspace.model.kafkaconnector.KafkaConnector;
import org.apache.atlas.AtlasException;
import org.apache.atlas.listener.ActiveStateChangeHandler;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.debezium.RdbmsEntities;
import org.apache.atlas.model.notification.Notification;
import org.apache.atlas.model.notification.RdbmsNotification;
import org.apache.atlas.notification.rdbms.Conversion;
import org.apache.atlas.notification.rdbms.KafkaConnectorUtil;
import org.apache.atlas.repository.converters.AtlasInstanceConverter;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.atlas.web.filters.AuditLog;
import org.apache.atlas.web.service.ServiceState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

@Component
@Order(5)
@DependsOn(value = {"atlasTypeDefStoreInitializer", "atlasTypeDefGraphStoreV2"})
public class NotificationRdbmsConsumer extends AbstractKafkaNotificationConsumer {
    private static final Logger LOG  = LoggerFactory.getLogger(NotificationRdbmsConsumer.class);
    private Conversion conversion;

    private static final Map<String, String> PARENT_RELATION_MAP = new HashMap<String, String>(){
        {
            put("rdbms_db", "instance");
            put("rdbms_table", "db");
            put("rdbms_column", "table");
        }
    };

    @Inject
    public NotificationRdbmsConsumer(ServiceState serviceState,
                                     AtlasEntityStore entityStore,
                                     AtlasInstanceConverter instanceConverter,
                                     AtlasTypeRegistry typeRegistry, Conversion conversion) throws AtlasException {
        super(serviceState, NotificationInterface.NotificationType.RDBMS, entityStore,instanceConverter,typeRegistry);
        this.conversion = conversion;
    }

    @Override
    public AbstractKafkaConsumerRunnable getConsumerRunnable(NotificationConsumer<Notification> consumer) throws AtlasException {
        return new RdbmsConsumer(consumer,serviceState);
    }

    @Override
    public int getHandlerOrder() {
        return ActiveStateChangeHandler.HandlerOrder.NOTIFICATION_RDBMS_CONSUMER.getOrder();
    }


    @VisibleForTesting
    class RdbmsConsumer extends AbstractKafkaConsumerRunnable {

        public RdbmsConsumer(NotificationConsumer<Notification> consumer, ServiceState serviceState) throws AtlasException {
            super(consumer, serviceState, "atlas-rdbms-consumer-thread");
        }

        @Override
        protected AuditLog dealMessage(Notification message) throws AtlasException{

            RdbmsNotification rdbmsMessage = (RdbmsNotification)message;

            String name = rdbmsMessage.getRdbmsMessage().getPayload().getSource().getName();

            KafkaConnector.Config config = KafkaConnectorUtil.getConnectorConfig(name);
            if(null == config){
                throw new RuntimeException("??????connector??????");
            }
            RdbmsEntities rdbmsEntities = conversion.convert(rdbmsMessage,config);

            LOG.info("atlas????????????????????????"+rdbmsEntities+"?????????JanusGraph????????????????????????????????????????????????PG");
            synchronize(rdbmsEntities, config);
            return null;
        }

        private void synchronize(RdbmsEntities rdbmsEntities, KafkaConnector.Config config){

            dealRenameEntities(rdbmsEntities);

            Map<RdbmsEntities.OperateType, Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> entityMap = rdbmsEntities.getEntityMap();
            Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> modifyMap = entityMap.get(RdbmsEntities.OperateType.MODIFY);
            Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> addMap = entityMap.get(RdbmsEntities.OperateType.ADD);
            List<AtlasEntity.AtlasEntityWithExtInfo> addOrUpdateEntities = mergeEntities(modifyMap,addMap);
            addOrUpdateEntities = sortEntities(addOrUpdateEntities);

            for (AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo: addOrUpdateEntities) {
                atlasEntityStore.createOrUpdate(new AtlasEntityStream(atlasEntityWithExtInfo, config), false);
            }

            AtlasEntity.AtlasEntitiesWithExtInfo bloodEntities = rdbmsEntities.getBloodEntities();
            if(null != bloodEntities){
                atlasEntityStore.createOrUpdate(new AtlasEntityStream(bloodEntities), false);
            }


            Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> dropMap = entityMap.get(RdbmsEntities.OperateType.DROP);
            List<AtlasEntity.AtlasEntityWithExtInfo> dropEntities = mergeEntities(dropMap,null);
            if(CollectionUtils.isNotEmpty(dropEntities)){
                dropEntities(dropEntities);
            }
        }

        private void dropEntities(List<AtlasEntity.AtlasEntityWithExtInfo> dropEntities) {

            for (int i = dropEntities.size() - 1; i >= 0; i--) {
                AtlasEntity entity = dropEntities.get(i).getEntity();
                AtlasEntityType type = typeRegistry.getEntityTypeByName(entity.getTypeName());
                Object qualifiedName = entity.getAttribute("qualifiedName");
                Map<String, Object> emptyMap = new HashMap<>();
                emptyMap.put("qualifiedName", qualifiedName);
                atlasEntityStore.deleteByUniqueAttributes(type, emptyMap);
            }

        }

        private List<AtlasEntity.AtlasEntityWithExtInfo> sortEntities(List<AtlasEntity.AtlasEntityWithExtInfo> originEntities){
            List<AtlasEntity.AtlasEntityWithExtInfo> sortEntities = new LinkedList<>();
            List<AtlasEntity.AtlasEntityWithExtInfo> tmpEntities = new LinkedList<>();
            for (AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo: originEntities) {
                sort(sortEntities, originEntities, tmpEntities, atlasEntityWithExtInfo);
                if(CollectionUtils.isNotEmpty(tmpEntities)){
                    sortEntities.addAll(tmpEntities);
                    tmpEntities.clear();
                }
            }
            return sortEntities;
        }

        private void sort(List<AtlasEntity.AtlasEntityWithExtInfo> sortEntities, List<AtlasEntity.AtlasEntityWithExtInfo> addOrUpdateEntities, List<AtlasEntity.AtlasEntityWithExtInfo> tmpEntities, AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo) {
            //????????????
            if(sortEntities.contains(atlasEntityWithExtInfo)){
                return;
            }
            tmpEntities.add(0,atlasEntityWithExtInfo);
            String parentQualifiedName = getParentQualifiedName(atlasEntityWithExtInfo);
            //??????????????????
            if(null == parentQualifiedName){
                return;
            }
            AtlasEntity.AtlasEntityWithExtInfo parentAtlasEntityWithExtInfo = getEntityByQualifiedName(parentQualifiedName, sortEntities);
            //??????????????????
            if(null != parentAtlasEntityWithExtInfo){
                return;
            }
            //???????????????
            parentAtlasEntityWithExtInfo = getEntityByQualifiedName(parentQualifiedName, addOrUpdateEntities);
            if(null == parentAtlasEntityWithExtInfo){
                Object qualifiedName = atlasEntityWithExtInfo.getEntity().getAttribute("qualifiedName");
                throw new RuntimeException("??????"+qualifiedName+"??????????????????"+parentQualifiedName+"????????????");
            }
            sort(sortEntities, addOrUpdateEntities, tmpEntities, parentAtlasEntityWithExtInfo);
        }

        private String getParentQualifiedName(AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo){
            String parentQualifiedName = null;
            AtlasEntity entity = atlasEntityWithExtInfo.getEntity();
            String typeName = entity.getTypeName();
            if(PARENT_RELATION_MAP.containsKey(typeName)){
                Map attributeMap = (Map)entity.getAttribute(PARENT_RELATION_MAP.get(typeName));
                Map uniqueAttributeMap = (Map)attributeMap.get("uniqueAttributes");
                parentQualifiedName = (String)uniqueAttributeMap.get("qualifiedName");
            }
            return parentQualifiedName;
        }

        private AtlasEntity.AtlasEntityWithExtInfo getEntityByQualifiedName(String qualifiedName, List<AtlasEntity.AtlasEntityWithExtInfo> atlasEntityWithExtInfos){

            AtlasEntity.AtlasEntityWithExtInfo resultEntity = null;
            if(CollectionUtils.isNotEmpty(atlasEntityWithExtInfos)){
                for (AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo:atlasEntityWithExtInfos) {
                    Object name = (String)atlasEntityWithExtInfo.getEntity().getAttribute("qualifiedName");
                    if(name.equals(qualifiedName)){
                        resultEntity =  atlasEntityWithExtInfo;
                        break;
                    }
                }
            }
            return resultEntity;
        }

        private List<AtlasEntity.AtlasEntityWithExtInfo> mergeEntities(Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> modifyMap, Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> addMap){
            List<AtlasEntity.AtlasEntityWithExtInfo> addOrUpdateEntities = new ArrayList<>();
            if(MapUtils.isNotEmpty(modifyMap)){
                modifyMap.values().stream().filter(e -> CollectionUtils.isNotEmpty(e)).forEach(e -> addOrUpdateEntities.addAll(e));
            }
            if(MapUtils.isNotEmpty(addMap)){
                addMap.values().stream().filter(e -> CollectionUtils.isNotEmpty(e)).forEach(e -> addOrUpdateEntities.addAll(e));
            }
            return addOrUpdateEntities;
        }

    }

    /**
     * ????????????????????????????????????????????????????????????????????????guid
     * @param rdbmsEntities
     */
    private void dealRenameEntities(RdbmsEntities rdbmsEntities) {

        Map<RdbmsEntities.OperateType, Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>>> entityMap = rdbmsEntities.getEntityMap();
        Map<RdbmsEntities.EntityType, List<AtlasEntity.AtlasEntityWithExtInfo>> modifyMap = entityMap.get(RdbmsEntities.OperateType.MODIFY);
        Map<String, AtlasEntity.AtlasEntityWithExtInfo> renameMap = rdbmsEntities.getRenameMap();
        renameMap.forEach((k,v) -> {
            String typeName = v.getEntity().getTypeName();
            try{
                AtlasEntityType entityType = typeRegistry.getEntityTypeByName(typeName);
                Map<String, Object> uniqueAttributes = new HashMap<>();
                uniqueAttributes.put("qualifiedName", k);
                AtlasEntity.AtlasEntityWithExtInfo oldEntityWithExtInfo = atlasEntityStore.getByUniqueAttributes(entityType, uniqueAttributes);
                if(null != oldEntityWithExtInfo){
                    String guid = oldEntityWithExtInfo.getEntity().getGuid();
                    Object qualifiedName = v.getEntity().getAttribute("qualifiedName");
                    RdbmsEntities.EntityType type = RdbmsEntities.getType(typeName);
                    List<AtlasEntity.AtlasEntityWithExtInfo> atlasEntityWithExtInfos = modifyMap.get(type);
                    atlasEntityWithExtInfos.forEach(a->{
                        if(a.getEntity().getAttribute("qualifiedName").equals(qualifiedName)){
                            a.getEntity().setGuid(guid);
                        }
                    });
                }
            }catch (Exception e){
                LOG.warn("??????{}??????????????????????????????{}???????????????", k, typeName, k);
            }
        });
    }
}