// ======================================================================
//
//      Copyright (C) 北京国双科技有限公司
//                    http://www.gridsum.com
//
//      保密性声明：此文件属北京国双科技有限公司所有，仅限拥有由国双科技
//      授予了相应权限的人所查看和所修改。如果你没有被国双科技授予相应的
//      权限而得到此文件，请删除此文件。未得国双科技同意，不得查看、修改、
//      散播此文件。
//
//
// ======================================================================

package io.zeta.metaspace.web.metadata;

import io.zeta.metaspace.model.sync.SyncTaskDefinition;
import io.zeta.metaspace.utils.AbstractMetaspaceGremlinQueryProvider;
import io.zeta.metaspace.web.service.DataManageService;
import io.zeta.metaspace.web.util.ZkLockUtils;
import org.apache.atlas.AtlasConfiguration;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntityHeader;
import org.apache.atlas.model.instance.AtlasObjectId;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.atlas.model.instance.EntityMutations;
import org.apache.atlas.repository.graphdb.AtlasGraph;
import org.apache.atlas.repository.store.graph.AtlasEntityStore;
import org.apache.atlas.repository.store.graph.v2.AtlasEntityStream;
import org.apache.atlas.repository.store.graph.v2.EntityGraphRetriever;
import org.apache.atlas.type.AtlasEntityType;
import org.apache.atlas.type.AtlasTypeRegistry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static io.zeta.metaspace.web.util.BaseHiveEvent.*;
/**
 * @author lixiang03
 * @Data 2020/10/15 11:28
 */
@Component
public abstract class MetaStoreBridgeUtils implements IMetaDataProvider{
    private static final Logger LOG = LoggerFactory.getLogger(MetaStoreBridgeUtils.class);
    protected  String         clusterName;
    @Autowired
    protected  AtlasEntityStore atlasEntityStore;

    @Autowired
    protected  AtlasTypeRegistry atlasTypeRegistry;
    protected volatile AtomicInteger totalTables = new AtomicInteger(0);
    protected volatile AtomicInteger updatedTables = new AtomicInteger(0);
    protected volatile AtomicLong startTime = new AtomicLong(0);
    protected volatile AtomicLong endTime = new AtomicLong(0);
    protected  AbstractMetaspaceGremlinQueryProvider gremlinQueryProvider;
    @Autowired
    protected  AtlasGraph graph;
    protected  EntityGraphRetriever entityRetriever;
    @Autowired
    protected DataManageService dataManageService;
    @Autowired
    protected ZkLockUtils zkLockUtils;
    protected static final int LOCK_TIME_OUT_TIME = AtlasConfiguration.LOCK_TIME_OUT_TIME.getInt(); //M

    @PostConstruct
    public void init(){
        this.entityRetriever = new EntityGraphRetriever(atlasTypeRegistry);
    }

    /**
     * 共用
     * @param typeName
     * @param qualifiedName
     * @return
     */
    protected AtlasEntity.AtlasEntityWithExtInfo findEntity(final String typeName, final String qualifiedName){
        AtlasEntity.AtlasEntityWithExtInfo ret = null;
        try {
            ret = atlasEntityStore.getByUniqueAttributes(atlasTypeRegistry.getEntityTypeByName(typeName), Collections.singletonMap(ATTRIBUTE_QUALIFIED_NAME, qualifiedName));
            clearRelationshipAttributes(ret);
        } catch (AtlasBaseException e) {
            LOG.warn("{}是新的元数据，保存元数据", qualifiedName);
        }
        return ret;
    }

    protected void clearRelationshipAttributes(AtlasEntity.AtlasEntityWithExtInfo entity) {
        if (entity != null) {
            clearRelationshipAttributes(entity.getEntity());

            if (entity.getReferredEntities() != null) {
                clearRelationshipAttributes(entity.getReferredEntities().values());
            }
        }
    }

    protected void clearRelationshipAttributes(Collection<AtlasEntity> entities) {
        if (entities != null) {
            for (AtlasEntity entity : entities) {
                clearRelationshipAttributes(entity);
            }
        }
    }

    protected void clearRelationshipAttributes(AtlasEntity entity) {
        if (entity != null && entity.getRelationshipAttributes() != null) {
            entity.getRelationshipAttributes().clear();
        }
    }

    /**
     * Registers an entity in atlas
     * @param entity
     * @return
     * @throws Exception
     */
    protected AtlasEntity.AtlasEntityWithExtInfo registerInstance(AtlasEntity.AtlasEntityWithExtInfo entity, SyncTaskDefinition definition) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating {} entity: {}", entity.getEntity().getTypeName(), entity);
        }

        AtlasEntity.AtlasEntityWithExtInfo ret             = null;
        EntityMutationResponse response        = atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false);
        List<AtlasEntityHeader> createdEntities = response.getEntitiesByOperation(EntityMutations.EntityOperation.CREATE);

        if (CollectionUtils.isNotEmpty(createdEntities)) {
            for (AtlasEntityHeader createdEntity : createdEntities) {
                if (ret == null) {
                    ret = atlasEntityStore.getById(createdEntity.getGuid());
                    dataManageService.addEntity(Arrays.asList(ret.getEntity()), definition, null);
                    LOG.info("Created {} entity: name={}, guid={}", ret.getEntity().getTypeName(), ret.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), ret.getEntity().getGuid());
                } else if (ret.getEntity(createdEntity.getGuid()) == null) {
                    AtlasEntity.AtlasEntityWithExtInfo newEntity = atlasEntityStore.getById(createdEntity.getGuid());

                    ret.addReferredEntity(newEntity.getEntity());

                    if (MapUtils.isNotEmpty(newEntity.getReferredEntities())) {
                        for (Map.Entry<String, AtlasEntity> entry : newEntity.getReferredEntities().entrySet()) {
                            ret.addReferredEntity(entry.getKey(), entry.getValue());
                        }
                    }
                    dataManageService.addEntity(Arrays.asList(newEntity.getEntity()), definition, null);
                    LOG.info("Created {} entity: name={}, guid={}", newEntity.getEntity().getTypeName(), newEntity.getEntity().getAttribute(ATTRIBUTE_QUALIFIED_NAME), newEntity.getEntity().getGuid());
                }
            }
        }

        clearRelationshipAttributes(ret);

        return ret;
    }

    /**
     * @param entity
     * @throws AtlasBaseException
     */
    protected void updateInstance(AtlasEntity.AtlasEntityWithExtInfo entity) throws AtlasBaseException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updating {} entity: {}", entity.getEntity().getTypeName(), entity);
        }
        atlasEntityStore.createOrUpdate(new AtlasEntityStream(entity), false);
    }

    protected void deleteEntity(AtlasEntity tableEntity) throws AtlasBaseException {
        AtlasEntityType type = (AtlasEntityType) atlasTypeRegistry.getType(tableEntity.getTypeName());
        final AtlasObjectId objectId = getObjectId(tableEntity);
        atlasEntityStore.deleteByUniqueAttributes(type, objectId.getUniqueAttributes());
        dataManageService.updateStatus(Arrays.asList(tableEntity));
    }
}
